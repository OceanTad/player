package com.hxrainbow.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

public class ExoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo);
        ExoPlayerView playerView = findViewById(R.id.playerView);
//        http://vr-m.oss-cn-beijing.aliyuncs.com/22f1c63612634fc495ae167d073f5e8e%20-%20%E5%89%AF%E6%9C%AC.mov
//        http://flv.bn.netease.com/videolib3/1707/03/bGYNX4211/SD/movie_index.m3u8
//        http://pub.wanbawanba.com/22f1c63612634fc495ae167d073f5e8e.mp4
        PlayerHelp.getInstance().initPlayer(this, playerView, "http://pub.wanbawanba.com/22f1c63612634fc495ae167d073f5e8e.mp4", new PlayerHelp.IPlayerStateListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onError() {

            }
        });
        findViewById(R.id.tv_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerHelp.getInstance().start();
            }
        });
        findViewById(R.id.tv_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerHelp.getInstance().stop();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        PlayerHelp.getInstance().start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PlayerHelp.getInstance().stop();
    }

}
