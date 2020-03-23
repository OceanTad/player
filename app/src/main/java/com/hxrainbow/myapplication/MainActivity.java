package com.hxrainbow.myapplication;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.exoplayer2.ui.PlayerView;

public class MainActivity extends AppCompatActivity {

//    PlayerView playerView;
    ExoPlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        http://hjh-ys.wanbawanba.com/JYGY/task/yZweX7EBAdTZpBYcCPSHKynBsnkHrc45.mp4
//        http://211.73.19.201/live/E6290DC0-BE6A-B7C4-79F5-114BDB417F9E?fmt=x264_500K_ts&cpid=admin&size=1280X720&toflv=15
        playerView = findViewById(R.id.player);
//        playerView.setUseController(false);
//        PlayerHelp.getInstance().initPlayer(this, playerView, "http://hjh-ys.wanbawanba.com/JYGY/task/yZweX7EBAdTZpBYcCPSHKynBsnkHrc45.mp4", new PlayerHelp.IPlayerStateListener() {
//            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//
//            }
//
//            @Override
//            public void onError() {
//
//            }
//        });
        PlayerHelp.getInstance().initPlayer(this, playerView, "http://hjh-ys.wanbawanba.com/JYGY/task/yZweX7EBAdTZpBYcCPSHKynBsnkHrc45.mp4", new PlayerHelp.IPlayerStateListener() {
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
        findViewById(R.id.tv_jump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MediaActivity.class));
            }
        });
        findViewById(R.id.tv_jump1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ExoActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
//        PlayerHelp.getInstance().start();
        PlayerHelp.getInstance().reset();

    }

    @Override
    protected void onPause() {
        super.onPause();
        PlayerHelp.getInstance().pause();
    }

}
