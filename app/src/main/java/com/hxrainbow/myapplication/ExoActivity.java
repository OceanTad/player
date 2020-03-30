package com.hxrainbow.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

public class ExoActivity extends AppCompatActivity {

    boolean isNeedRestart = false;

    private CExoPlayerView playerView;
    private ExoPlayerHelp playerHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo);
        playerView = findViewById(R.id.playerView);
//        http://vr-m.oss-cn-beijing.aliyuncs.com/22f1c63612634fc495ae167d073f5e8e%20-%20%E5%89%AF%E6%9C%AC.mov
//        http://flv.bn.netease.com/videolib3/1707/03/bGYNX4211/SD/movie_index.m3u8
//        http://pub.wanbawanba.com/22f1c63612634fc495ae167d073f5e8e.mp4
        playerView.setPlayerStateListener(new ExoPlayerHelp.IPlayerStateListener() {
            @Override
            public void onFinish() {
                playerHelp.player("http://pub.wanbawanba.com/22f1c63612634fc495ae167d073f5e8e.mp4");
                playerHelp.start();
            }

            @Override
            public void onError() {

            }
        });
        playerHelp = new ExoPlayerHelp(this);

        playerHelp.setPlayerView(playerView);
        playerView.setControllerView(findViewById(R.id.controller));
        playerView.setShowController(true);
        playerHelp.player("http://pub.wanbawanba.com/22f1c63612634fc495ae167d073f5e8e.mp4");
        playerHelp.start();

        findViewById(R.id.tv_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerHelp.start();
            }
        });
        findViewById(R.id.tv_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerHelp.seekTo(2000);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedRestart) {
            playerHelp.restart(playerView);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isNeedRestart = true;
        playerHelp.pause();
    }

}
