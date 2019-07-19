package com.cdnbye.p2pengine;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videoplayer.exo.ExoMediaPlayerFactory;
import com.dueeeke.videoplayer.ijk.IjkPlayerFactory;
import com.dueeeke.videoplayer.player.AndroidMediaPlayerFactory;
import com.dueeeke.videoplayer.player.VideoView;

import com.cdnbye.sdk.ChannelIdCallback;
import com.cdnbye.sdk.P2pEngine;
import com.cdnbye.sdk.P2pConfig;
import com.cdnbye.sdk.P2pStatisticsListener;
import com.cdnbye.sdk.LogLevel;

import java.util.List;


public class MainActivity extends Activity {

    private VideoView videoView;
    private StandardVideoController controller;
    private final String TAG = "MainActivity";

//    private final String VOD = "https://www.solezy.me/20190328/SrgSISNS/index.m3u8";
    private final String VOD = "https://youku.rebo5566.com/20190718/WGiwgA41/index.m3u8";
//    private final String VOD = "http://opentracker.cdnbye.com:2100/20190513/Hm8R9WIB/index.m3u8";
    private final String LIVE = "http://hefeng.live.tempsource.cjyun.org/videotmp/s10100-hftv.m3u8";
//    private final String LIVE = "https://p2p.o8.cx/live/jade.m3u8";
//    private final String LIVE = "http://120.79.208.124:8080/live/jade.m3u8";

    private Button replayBtn;
    private Button switchBtn;
    private Button vodBtn;
    private Button liveBtn;
    private String currentUrl = VOD;

    private double totalHttpDownloaded = 0;
    private double totalP2pDownloaded = 0;
    private double totalP2pUploaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        TextView versionV = findViewById(R.id.version);
        versionV.setText("Version: " + P2pEngine.Version);

        P2pConfig config = new P2pConfig.Builder()
                .p2pEnabled(true)
                .logEnabled(true)
                .logLevel(LogLevel.DEBUG)
                .announce("https://tracker.cdnbye.com:8090/v1")
                .build();
        P2pEngine engine = P2pEngine.initEngine(this, "free", config);
        engine.addP2pStatisticsListener(new P2pStatisticsListener() {
            @Override
            public void onHttpDownloaded(long value) {
//                Log.d("TAG", "httpDownloaded: " + value);
                totalHttpDownloaded += (double)value;
                refreshRatio();
                checkIfConnected();
            }

            @Override
            public void onP2pDownloaded(long value) {
                totalP2pDownloaded += (double)value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView offloadV = findViewById(R.id.offload);
                        String text = String.format("Offload: %.2fMB", totalP2pDownloaded/1024);
                        offloadV.setText(text);

                        refreshRatio();
                    }
                });
                checkIfConnected();
            }

            @Override
            public void onP2pUploaded(long value) {
                totalP2pUploaded += (double)value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView uploadV = findViewById(R.id.upload);
                        String text = String.format("Upload: %.2fMB", totalP2pUploaded/1024);
                        uploadV.setText(text);
                    }
                });
                checkIfConnected();
            }

            @Override
            public void onPeers(List<String> peers) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView peersV = findViewById(R.id.peers);
                        String text = String.format("Peers: %d", peers.size());
                        peersV.setText(text);
                    }
                });
                checkIfConnected();
            }
        });

        startPlay(currentUrl);

        replayBtn = findViewById(R.id.replay);
        switchBtn = findViewById(R.id.switcher);
        vodBtn = findViewById(R.id.vod);
        liveBtn = findViewById(R.id.live);

        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 清空数据
                clearData();
                startPlay(currentUrl);
            }
        });

        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUrl.equals(VOD)) {
                    currentUrl = LIVE;
                } else {
                    currentUrl = VOD;
                }
                // 清空数据
                clearData();
                startPlay(currentUrl);
            }
        });
        vodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 清空数据
                clearData();
                currentUrl = VOD;
                startPlay(currentUrl);
            }
        });

        liveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 清空数据
                clearData();
                currentUrl = LIVE;
                startPlay(currentUrl);
            }
        });
    }

    private void startPlay(String url) {
        String parsedUrl = P2pEngine.getInstance().parseStreamUrl(url);
        videoView = findViewById(R.id.player);

        videoView.release();

        videoView.setUrl(parsedUrl); //设置视频地址

        // 使用IjkPlayer解码
//        videoView.setPlayerFactory(IjkPlayerFactory.create(this));
        // 使用ExoPlayer解码
        videoView.setPlayerFactory(ExoMediaPlayerFactory.create(this));
        // 使用MediaPlayer解码
//        videoView.setPlayerFactory(AndroidMediaPlayerFactory.create(this));

        if (controller == null) {
            controller = new StandardVideoController(this);
        }

        videoView.setVideoController(controller); //设置控制器，如需定制可继承BaseVideoController
        videoView.start();
    }

    private void checkIfConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView connectedV = findViewById(R.id.connected);
                String text = String.format("Connected: %s", P2pEngine.getInstance().isConnected()?"Yes":"No");
                connectedV.setText(text);

                TextView peerIdV = findViewById(R.id.peerId);
                String text2 = String.format("Peer ID: %s", P2pEngine.getInstance().getPeerId());
                peerIdV.setText(text2);
            }
        });
    }

    private void refreshRatio() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double ratio = 0;
                if (totalHttpDownloaded+totalP2pDownloaded != 0) {
                    ratio = totalP2pDownloaded/(totalHttpDownloaded+totalP2pDownloaded);
                }
                TextView ratioV = findViewById(R.id.ratio);
                String text = String.format("P2P Ratio: %.0f%%", ratio*100);
                ratioV.setText(text);
            }
        });
    }

    private void clearData() {
        totalHttpDownloaded = 0;
        totalP2pDownloaded = 0;
        totalP2pUploaded = 0;
        checkIfConnected();
        refreshRatio();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.release();
    }


    @Override
    public void onBackPressed() {
        if (!videoView.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
