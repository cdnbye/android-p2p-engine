package com.cdnbye.sdk;

import android.content.Context;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cdnbye.core.logger.LoggerUtil;
import com.cdnbye.core.m3u8.Parser;
import com.cdnbye.core.p2p.DataChannel;
import com.cdnbye.core.p2p.PCFactory;
import com.cdnbye.core.segment.HttpLoader;
import com.cdnbye.core.segment.Segment;
import com.cdnbye.core.segment.SegmentLoaderCallback;
import com.cdnbye.core.tracking.TrackerClient;
import com.cdnbye.core.utils.CBTimer;
import com.cdnbye.core.utils.HttpHelper;
import com.cdnbye.core.utils.UtilFunc;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.orhanobut.logger.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

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
    private AsyncHttpServer localServer;
    private boolean isServerRunning;
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

        long startTime =  System.currentTimeMillis();

        try {
            this.originalURL = new URL(url);

            // 重启p2p
            restartP2p();

            if (!isvalid) {
                return url;
            }
            if (!config.getP2pEnabled()) {
                Logger.i("P2p is disabled");
                return url;
            }
            if (!originalURL.getPath().endsWith(".m3u8")) {
                Logger.e("Media type is not supported");
                return url;
            }

            if (isServerRunning == false) {
                Logger.e("Local server is not running");
                return url;
            }

//            // 启动本地服务器
//            try {
//                startLocalServer();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return url;
//            }

            String m3u8Name = originalURL.getPath();
            localUrlStr = String.format(Locale.ENGLISH, "%s:%d%s", LOCAL_IP, currentPort, m3u8Name);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Logger.e("Start local server failed");
            localUrlStr = url;
        }
        Logger.d("localUrlStr: " + localUrlStr);

        long endTime =  System.currentTimeMillis();
        long usedTime = endTime-startTime;
        Logger.i("parseStreamUrl usedTime " + usedTime);

        return localUrlStr;
    }

    public void addP2pStatisticsListener(P2pStatisticsListener listener) {
        this.listener = listener;
    }

    public void stopP2p() {
        Logger.w("engine stop p2p");
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

        AsyncHttpServer server = new AsyncHttpServer();

        this.localServer = server;

        // m3u8处理器
        server.get("^/.*\\.m3u8$", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                Logger.i("request m3u8 " + request.getPath());


                long startTime =  System.currentTimeMillis();

                try {
                    if (request.getPath().equals(currPlaylist)) {
                        // 非第一次m3u8请求

                    } else {
                        // 第一次m3u8请求
                        parser = new Parser(originalURL.toString());
                        currPlaylist = request.getPath();
                    }
//                    Parser parser = new Parser(originalURL.toString());

                    String sPlaylist = parser.getMediaPlaylist();
//                    Logger.d("playlist: " + sPlaylist);
                    Logger.i("receive m3u8");
                    // 获取直播或者点播
                    TrackerClient.setIsLive(parser.isLive());
                    if (!parser.isLive()) {
                        TrackerClient.setEndSN(parser.getEndSN());
                    }

                    long endTime =  System.currentTimeMillis();
                    long usedTime = endTime-startTime;
                    Logger.i("m3u8 request usedTime " + usedTime);

                    response.send("application/vnd.apple.mpegurl", sPlaylist);

                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.w("m3u8 request redirect to " + originalURL.toString());
                    response.redirect(originalURL.toString());
                }
            }
        });

        // ts处理器
        server.get("^/.*\\.(ts|jpg|js)$", new HttpServerRequestCallback() {
            @Override
            public void onRequest(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
                String lastPath = request.getPath().substring(request.getPath().lastIndexOf('/') + 1);
                Logger.i("player request ts: %s", lastPath);
                final String segId = lastPath.split("\\.")[0];
                final String rawTSUrl = request.getQuery().getString("url");
                float duration = Float.parseFloat(request.getQuery().getString("duration"));
//                String tsUrl = UtilFunc.decodeURIComponent(rawTSUrl);
//                Logger.d("ts url: %s segId: %s", rawTSUrl, segId);
                final Segment seg = new Segment(segId, rawTSUrl, duration);

                if (isConnected() && config.getP2pEnabled()) {
                    // scheduler loadSegment
                    Logger.i("scheduler load " + segId);

                    synchronized (seg) {
                        try {
                            tracker.getScheduler().loadSegment(seg, request);
//                            seg.wait(config.getDownloadTimeout());
                            seg.wait();
                            if (seg.getBuffer() != null && seg.getBuffer().length > 0) {
                                Logger.i("engine onResponse: " + seg.getBuffer().length + " contentType: " + seg.getContentType() + " segId " + seg.getSegId());
//                                Logger.i(segId + " sha1:" + UtilFunc.getStringSHA1(seg.getBuffer()));
                                response.send(seg.getContentType(), seg.getBuffer());
                            } else {
                                Logger.w("request ts failed, redirect to " + rawTSUrl);
                                response.redirect(rawTSUrl);
                            }
                        } catch (InterruptedException e) {
                            Logger.i("wait InterruptedException");
                            e.printStackTrace();
                            response.redirect(rawTSUrl);
                        }
                    }

                } else {

                    prefetchSegs ++;
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

                    HttpLoader.loadSegmentSync(seg, request, new SegmentLoaderCallback() {
                        @Override
                        public void onFailure(String segId) {
                            Logger.w("request ts failed, redirect to " + rawTSUrl);
                            response.redirect(rawTSUrl);
                        }

                        @Override
                        public void onResponse(final byte[] data, String contentType) {
                            Logger.i("engine onResponse: " + data.length + " contentType: " + contentType);
//                            respCount ++;
                            if (listener != null) listener.onHttpDownloaded(data.length/1024);
                            response.send(contentType, data);
                        }
                    });
                }


            }
        });

        // 其他文件处理器(key)
        server.get("^/.*(?<!(.ts|.m3u8|.jpg|.js))$", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
//                Logger.d("request key");
                URL url = null;
                try {
                    url = new URL(originalURL, request.getPath());
                    Logger.d("key url: " + url.toString());
//                    response.send(url.toString());
                    response.redirect(url.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });


        AsyncServerSocket socket = server.listen(currentPort);

        while(null == socket && currentPort <=65535){
            currentPort ++;
            socket = server.listen(currentPort);
        }
        if(currentPort > 65535){
            throw  new RuntimeException("port number is greater than 65535");
        }

        isServerRunning = true;


        Logger.d("Listen at port: " + currentPort);

    }



    private void initTrackerClient() throws Exception {
        if (tracker != null) return;
        Logger.i("Init tracker");
        // 拼接channelId，并进行url编码和base64编码
        String encodedChannelId = UtilFunc.getChannelId(originalURL.toString(), config.getWsSignalerAddr(), DataChannel.DC_VERSION, config.getChannelId());
        Logger.i("encodedChannelId: " + encodedChannelId);
        TrackerClient trackerClient = new TrackerClient(token, encodedChannelId, config, listener);
        this.tracker = trackerClient;
        trackerClient.doChannelReq();
    }

}
