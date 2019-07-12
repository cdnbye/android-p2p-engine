package com.cdnbye.p2pengine;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videoplayer.exo.ExoMediaPlayerFactory;
import com.dueeeke.videoplayer.ijk.IjkPlayerFactory;
import com.dueeeke.videoplayer.player.AndroidMediaPlayerFactory;
import com.dueeeke.videoplayer.player.VideoView;

import com.cdnbye.sdk.P2pEngine;
import com.cdnbye.sdk.P2pConfig;
import com.cdnbye.sdk.P2pStatisticsListener;

public class PlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    private final String VOD = "http://opentracker.cdnbye.com:2100/20190513/Hm8R9WIB/index.m3u8";
    private final String LIVE = "http://222.186.50.155/hls/test2.m3u8";

    private String currentUrl = VOD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        P2pConfig config = new P2pConfig.Builder()
                .enableLog(true)
                .logLevel(Log.DEBUG)
                .build();
        P2pEngine.initEngine(this, "free", config);
        String parsedUrl = P2pEngine.getInstance().parseStreamUrl("https://www.solezy.me/20190328/SrgSISNS/index.m3u8");
        videoView = findViewById(R.id.player);
        videoView.setUrl(parsedUrl); //设置视频地址
        StandardVideoController controller = new StandardVideoController(this);
        videoView.setVideoController(controller); //设置控制器，如需定制可继承BaseVideoController

        // 使用IjkPlayer解码
//        videoView.setPlayerFactory(IjkPlayerFactory.create(this));
        // 使用ExoPlayer解码
        videoView.setPlayerFactory(ExoMediaPlayerFactory.create(this));
        // 使用MediaPlayer解码
//        videoView.setPlayerFactory(AndroidMediaPlayerFactory.create(this));

        videoView.start(); //开始播放，不调用则不自动播放

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
