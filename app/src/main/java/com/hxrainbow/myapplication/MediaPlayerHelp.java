package com.hxrainbow.myapplication;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;

public class MediaPlayerHelp {

    private static final String TAG = "player";

    private static volatile MediaPlayerHelp instance;

    private MediaPlayerHelp() {

    }

    public static MediaPlayerHelp getInstance() {
        if (instance == null) {
            synchronized (MediaPlayerHelp.class) {
                if (instance == null) {
                    instance = new MediaPlayerHelp();
                }
            }
        }
        return instance;
    }

    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnPreparedListener preparedListener;
    private MediaPlayer.OnCompletionListener completionListener;
    private MediaPlayer.OnErrorListener errorListener;
    private IMediaPlayerListener mediaPlayerListener;

    public void initPlayer(TextureView playerView, IMediaPlayerListener mediaPlayerListener) {
        createPlayer();
        setMediaPlayerListener(mediaPlayerListener);
        playerView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (mediaPlayer != null) {
                    mediaPlayer.setSurface(new Surface(surface));
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    public void startPlayer(String url) {
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            if (mediaPlayerListener != null) {
                mediaPlayerListener.onError();
            }
        }

    }

    public void setMediaPlayerListener(IMediaPlayerListener mediaPlayerListener) {
        this.mediaPlayerListener = mediaPlayerListener;
    }

    private void createPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnErrorListener(createErrorListener());
            mediaPlayer.setOnCompletionListener(createCompletionListener());
            mediaPlayer.setOnPreparedListener(createPreparedListener());
        }
    }

    private MediaPlayer.OnCompletionListener createCompletionListener() {
        if (completionListener == null) {
            completionListener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayerListener != null) {
                        mediaPlayerListener.onFinish();
                    }
                }
            };
        }
        return completionListener;
    }

    private MediaPlayer.OnErrorListener createErrorListener() {
        if (errorListener == null) {
            errorListener = new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (mediaPlayerListener != null) {
                        mediaPlayerListener.onError();
                    }
                    return false;
                }
            };
        }
        return errorListener;
    }

    private MediaPlayer.OnPreparedListener createPreparedListener() {
        if (preparedListener == null) {
            preparedListener = new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                        mediaPlayer.stop();
                    }
                    if (mediaPlayerListener != null) {
                        mediaPlayerListener.onPrepared();
                    }
                }
            };
        }
        return preparedListener;
    }

    public interface IMediaPlayerListener {

        void onError();

        void onPrepared();

        void onFinish();

    }

}
