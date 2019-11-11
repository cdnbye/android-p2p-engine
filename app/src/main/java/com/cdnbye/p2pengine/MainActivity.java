package com.cdnbye.p2pengine;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import java.util.List;

import com.cdnbye.sdk.ChannelIdCallback;
import com.cdnbye.sdk.P2pEngine;
import com.cdnbye.sdk.P2pConfig;
import com.cdnbye.sdk.P2pStatisticsListener;
import com.cdnbye.sdk.LogLevel;


public class MainActivity extends Activity {

    private VideoView videoView;

    private final String VOD = "https://iqiyi.com-t-iqiyi.com/20190722/5120_0f9eec31/index.m3u8";
    private final String LIVE = "http://aplay.gztv.com/sec/zhonghe.m3u8?txSecret=a777cb396c8c9c82251f4c8c389cf141&txTime=1560699724934";

    private Button replayBtn;
    private Button switchBtn;
    private Button vodBtn;
    private Button liveBtn;
    private String currentUrl = VOD;
//    private String currentUrl = LIVE;

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
                .build();

        // Instantiate P2pEngine，which is a singleton
        P2pEngine engine = P2pEngine.initEngine(getApplicationContext(), "free", config);
        engine.addP2pStatisticsListener(new P2pStatisticsListener() {
            @Override
            public void onHttpDownloaded(long value) {
                totalHttpDownloaded += (double) value;
                refreshRatio();
                checkIfConnected();
            }

            @Override
            public void onP2pDownloaded(long value) {
                totalP2pDownloaded += (double) value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView offloadV = findViewById(R.id.offload);
                        String text = String.format("Offload: %.2fMB", totalP2pDownloaded / 1024);
                        offloadV.setText(text);

                        refreshRatio();
                    }
                });
                checkIfConnected();
            }

            @Override
            public void onP2pUploaded(long value) {
                totalP2pUploaded += (double) value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView uploadV = findViewById(R.id.upload);
                        String text = String.format("Upload: %.2fMB", totalP2pUploaded / 1024);
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
        // Convert original playback address (m3u8) to the address of the local proxy server
        String parsedUrl = P2pEngine.getInstance().parseStreamUrl(url);

        videoView = findViewById(R.id.player);

        videoView.setVideoURI(Uri.parse(parsedUrl));

        MediaController controller = new MediaController(this);

        videoView.setMediaController(controller);
        controller.setMediaPlayer(videoView);

        videoView.start();

    }

    private void checkIfConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView connectedV = findViewById(R.id.connected);
                String text = String.format("Connected: %s", P2pEngine.getInstance().isConnected() ? "Yes" : "No");
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
                if (totalHttpDownloaded + totalP2pDownloaded != 0) {
                    ratio = totalP2pDownloaded / (totalHttpDownloaded + totalP2pDownloaded);
                }
                TextView ratioV = findViewById(R.id.ratio);
                String text = String.format("P2P Ratio: %.0f%%", ratio * 100);
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

}
