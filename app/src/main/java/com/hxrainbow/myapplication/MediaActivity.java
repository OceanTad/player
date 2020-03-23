package com.hxrainbow.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

public class MediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        TextureView textureView = findViewById(R.id.video);
        MediaPlayerHelp.getInstance().initPlayer(textureView, new MediaPlayerHelp.IMediaPlayerListener() {
            @Override
            public void onError() {
                Log.e("lht", "onerror");
            }

            @Override
            public void onPrepared() {
                Log.e("lht", "onPrepared");
            }

            @Override
            public void onFinish() {
                Log.e("lht", "onFinish");
            }
        });
        MediaPlayerHelp.getInstance().startPlayer("http://flv.bn.netease.com/videolib3/1707/03/bGYNX4211/SD/movie_index.m3u8");
    }
}
