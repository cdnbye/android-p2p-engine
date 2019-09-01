package com.cdnbye.sdk;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.cdnbye.core.logger.LoggerUtil;
import com.cdnbye.core.m3u8.Parser;
import com.cdnbye.core.p2p.DataChannel;
import com.cdnbye.core.p2p.PCFactory;
import com.cdnbye.core.segment.HttpLoader;
import com.cdnbye.core.segment.Segment;
import com.cdnbye.core.tracking.TrackerClient;
import com.cdnbye.core.utils.CBTimer;
import com.cdnbye.core.utils.HttpHelper;
import com.cdnbye.core.utils.UtilFunc;
import com.orhanobut.logger.Logger;
import fi.iki.elonen.NanoHTTPD;

public final class P2pEngine {

    public static final String Version = BuildConfig.VERSION_NAME;      // SDK版本号

    private final String LOCAL_IP = "http://127.0.0.1";
    private final int PREFETCH_SEGMENTS = 5;            // 通过http预加载的ts数量，之后再初始化tracker

    private P2pConfig config;
    private String token;
    private URL originalURL;
    private String localUrlStr;
    private int prefetchSegs = 0;
    private boolean isvalid = true;
    private HttpServer localServer;
    private boolean isServerRunning;               // 代理服务器是否正常运行
    private int currentPort;
    private TrackerClient tracker;
    private P2pStatisticsListener listener;
    private String currPlaylist;
    private Parser parser;

    public boolean isConnected() {
        return tracker != null && tracker.isConnected();
//        return false;
    }

    public String getPeerId() {
        if (tracker != null && tracker.getPeerId() != null) {
            return tracker.getPeerId();
        } else {
            return "";
        }
    }

    private volatile static P2pEngine INSTANCE = null;

    private P2pEngine(Context ctx, String token, P2pConfig config) {
        if (ctx == null) {
            Logger.e("Context is required");
            isvalid = false;
        }
        if (token == null || token.length() == 0) {
            Logger.e("Token is required");
            isvalid = false;
        }
        if (config.getCustomTag().length() > 20) {
            Logger.e("Tag is too long");
            isvalid = false;
        }
        if (config.getAgent().length() > 20) {
            Logger.e("Agent is too long");
            isvalid = false;
        }

        this.token = token;
        this.config = config;
        currentPort = config.getLocalPort();
        init();
        Logger.d("P2pEngine created!");

        TrackerClient.setCacheDir(UtilFunc.getDiskCacheDir(ctx, "cdnbye"));
        TrackerClient.setBundleId(ctx.getPackageName());
        TrackerClient.setAppName(UtilFunc.getAppName(ctx));

        PCFactory.init(ctx);

    }

    public static P2pEngine initEngine(@NonNull Context ctx, @NonNull String token, @Nullable P2pConfig config) {
        if (INSTANCE == null) {
            synchronized (P2pEngine.class) {
                if (INSTANCE == null) {
                    if (config == null) {
                        config = new P2pConfig.Builder().build();
                    }
                    INSTANCE = new P2pEngine(ctx, token, config);
                }
            }
        }
        return INSTANCE;
    }

    // 如果之前没有实例化，用默认参数实例化
    public static P2pEngine getInstance() {
        if (INSTANCE == null) {
            Logger.wtf("Please call P2pEngine.initEngine before calling this method!");
        }
        return INSTANCE;
    }

    // 将原始m3u8转换成本地地址
    public String parseStreamUrl(@NonNull String url) {
        Logger.d("parseStreamUrl");

        long startTime = System.currentTimeMillis();

        try {
            this.originalURL = new URL(url);

            // 重启p2p
            restartP2p();

            if (!isvalid) {
                return url;
            }
            if (originalURL.getPath() == null || originalURL.getPath().equals("")) {
                Logger.e("Url path is null!");
                return url;
            }
            if (!config.getP2pEnabled()) {
                Logger.i("P2p is disabled");
                return url;
            }
            if (!originalURL.getPath().endsWith(".m3u8")) {
                Logger.w("Media type is not supported");
                return url;
            }

            if (!isServerRunning) {
                Logger.e("Local server is not running");
                return url;
            }

            String m3u8Name = originalURL.getPath();
            localUrlStr = String.format(Locale.ENGLISH, "%s:%d%s", LOCAL_IP, currentPort, m3u8Name);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Logger.e("Start local server failed");
            localUrlStr = url;
        }
        Logger.d("localUrlStr: " + localUrlStr);

        long endTime = System.currentTimeMillis();
        long usedTime = endTime - startTime;
        Logger.i("parseStreamUrl usedTime " + usedTime);

        return localUrlStr;
    }

    public void addP2pStatisticsListener(P2pStatisticsListener listener) {
        this.listener = listener;
    }

    public void stopP2p() {
        Logger.i("engine stop p2p");
        if (isConnected()) {
            tracker.stopP2p();
        }
    }

    public void restartP2p() {
        Logger.i("engine restart p2p");
        if (tracker != null) {
            stopP2p();
            tracker = null;
        }
        prefetchSegs = 0;
        currPlaylist = "";
        if (!isServerRunning) {
            // 重启代理服务器
            try {
                startLocalServer();
            } catch (Exception e) {
                e.printStackTrace();
//            return url;
            }
        }
    }

    // 初始化各个组件
    private void init() {
        //初始化logger
        LoggerUtil loggerUtil = new LoggerUtil(config.isDebug(), config.getLogLevel().value());
        loggerUtil.init();

        // 初始化HttpHelper
        HttpHelper.init(config.getDownloadTimeout());

        // 启动本地服务器
        try {
            startLocalServer();
        } catch (Exception e) {
            e.printStackTrace();
//            return url;
        }
    }

    private void startLocalServer() {

        if (isServerRunning && localServer != null) {
            localServer.stop();
        }

        while (true) {
            try {
                localServer = new HttpServer(currentPort);
                if (localServer.wasStarted()) {
                    isServerRunning = true;
                }
                break;
            } catch (IOException e) {
                e.printStackTrace();
                currentPort++;
                if (currentPort > 65535) {
                    throw new RuntimeException("port number is greater than 65535");
                }
            }
        }

        Logger.i("Listen at port: " + currentPort);
    }


    private void initTrackerClient() throws Exception {
        if (tracker != null) return;
        Logger.i("Init tracker");
        // 拼接channelId，并进行url编码和base64编码
        String encodedChannelId = UtilFunc.getChannelId(originalURL.toString(), config.getWsSignalerAddr(), DataChannel.DC_VERSION, config.getChannelId());
//        Logger.i("encodedChannelId: " + encodedChannelId);
        TrackerClient trackerClient = new TrackerClient(token, encodedChannelId, config, listener);
        this.tracker = trackerClient;
        trackerClient.doChannelReq();
    }

    class HttpServer extends NanoHTTPD {

        public HttpServer(int port) throws IOException {
            super(port);
            start();
        }

        @Override
        public Response serve(IHTTPSession session) {

            String uri = session.getUri();
            Logger.d("session uri " + uri);
            if (uri.endsWith(".m3u8")) {
                // m3u8处理器
                String mimeType = "application/vnd.apple.mpegurl";
                try {
                    if (session.getUri().equals(currPlaylist)) {
                        // 非第一次m3u8请求
                        Logger.d("非第一次m3u8请求");
                    } else {
                        // 第一次m3u8请求
                        Logger.d("第一次m3u8请求");
                        parser = new Parser(originalURL.toString());
                        currPlaylist = session.getUri();
                    }
                    String sPlaylist = parser.getMediaPlaylist();
//                    Logger.d("playlist: " + sPlaylist);
                    Logger.i("receive m3u8");
                    // 获取直播或者点播
                    TrackerClient.setIsLive(parser.isLive());
                    if (!parser.isLive()) {
                        TrackerClient.setEndSN(parser.getEndSN());
                    }
                    return newFixedLengthResponse(Response.Status.OK, mimeType, sPlaylist);

                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.w("m3u8 request redirect to " + originalURL.toString());
                    Response resp = newFixedLengthResponse(Response.Status.REDIRECT, mimeType, null);
                    resp.addHeader("Location", parser.getOriginalURL().toString());
                    return resp;
                }
            } else if (uri.endsWith("ts") || uri.endsWith("jpg") || uri.endsWith("js")) {
                // ts处理器
                String lastPath = uri.substring(uri.lastIndexOf('/') + 1);
                Logger.i("player request ts: %s", lastPath);
                final String segId = lastPath.split("\\.")[0];
                final String rawTSUrl = session.getParameters().get("url").get(0);
                float duration = Float.parseFloat(session.getParameters().get("duration").get(0));
                String tsUrl = UtilFunc.decodeURIComponent(rawTSUrl);
                Logger.d("ts url: %s segId: %s tsUrl: %s", rawTSUrl, segId, tsUrl);
                Segment seg = new Segment(segId, rawTSUrl, duration);
                Map<String, String> headers = new HashMap<>();
                if (session.getHeaders().get("range") != null) {
                    headers.put("Range", session.getHeaders().get("range"));
                    Logger.i("Range: " + headers.get("Range"));
                }
                if (isConnected() && config.getP2pEnabled()) {
                    // scheduler loadSegment
                    Logger.i("scheduler load " + segId);

                    synchronized (seg) {
                        try {
                            tracker.getScheduler().loadSegment(seg, headers);
//                            seg.wait(config.getDownloadTimeout());
                            seg.wait();
                            if (seg.getBuffer() != null && seg.getBuffer().length > 0) {
                                Logger.i("scheduler onResponse: " + seg.getBuffer().length + " contentType: " + seg.getContentType() + " segId " + seg.getSegId());
//                                Logger.i(segId + " sha1:" + UtilFunc.getStringSHA1(seg.getBuffer()));
                                return newFixedLengthResponse(Response.Status.OK, seg.getContentType(), new ByteArrayInputStream(seg.getBuffer()), seg.getBuffer().length);
//                                return newChunkedResponse(Response.Status.OK, seg.getContentType(), new ByteArrayInputStream(seg.getBuffer()));
                            } else {
                                Logger.w("request ts failed, redirect to " + rawTSUrl);
                                Response resp = newFixedLengthResponse(Response.Status.REDIRECT, "", null);
                                resp.addHeader("Location", rawTSUrl);
                                return resp;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Response resp = newFixedLengthResponse(Response.Status.REDIRECT, "", null);
                            resp.addHeader("Location", rawTSUrl);
                            return resp;
                        }
                    }

                } else {
                    prefetchSegs++;
                    if (tracker == null && config.getP2pEnabled() && prefetchSegs == PREFETCH_SEGMENTS && isvalid) {
                        synchronized (this) {
                            try {
                                initTrackerClient();
                            } catch (Exception e) {
                                e.printStackTrace();
                                isvalid = false;
                            }
                        }
                    }
                    // 如果tracker还没连上ws则直接请求ts
                    Logger.d("engine loadSegment " + segId);

                    // 更新CBTimmer
                    float bufferTime = CBTimer.getInstance().getBufferTime();
                    CBTimer.getInstance().updateBaseTime();
                    CBTimer.getInstance().updateAvailableSpanWithBufferTime(bufferTime);
                    Segment segment = HttpLoader.loadSegmentSync(seg, headers);
                    if (segment.getBuffer() != null && segment.getBuffer().length > 0) {
                        Logger.i("engine onResponse: " + segment.getBuffer().length + " contentType: " + segment.getContentType() + " segId " + segment.getSegId());
//                                Logger.i(segId + " sha1:" + UtilFunc.getStringSHA1(seg.getBuffer()));
                        if (listener != null) {
                            listener.onHttpDownloaded(segment.getBuffer().length / 1024);
                        }
                        return newFixedLengthResponse(Response.Status.OK, segment.getContentType(), new ByteArrayInputStream(segment.getBuffer()), segment.getBuffer().length);
//                        return newChunkedResponse(Response.Status.OK, segment.getContentType(), new ByteArrayInputStream(segment.getBuffer()));
                    } else {
                        Logger.w("engine request ts failed, redirect to " + rawTSUrl);
                        Response resp = newFixedLengthResponse(Response.Status.REDIRECT, "", null);
                        resp.addHeader("Location", rawTSUrl);
                        return resp;
                    }
                }
            } else {
                // 其他文件处理器(key)
                URL url = null;
                try {
                    url = new URL(originalURL, session.getUri());
                    Logger.d("key url: " + url.toString());

                    Response resp = newFixedLengthResponse(Response.Status.REDIRECT, "", null);
                    resp.addHeader("Location", url.toString());
                    return resp;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "", "");
        }
    }
}
