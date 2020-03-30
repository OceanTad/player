package com.hxrainbow.myapplication;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheEvictor;
import com.google.android.exoplayer2.upstream.cache.CacheSpan;
import com.google.android.exoplayer2.upstream.cache.CachedRegionTracker;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;


public class ExoPlayerHelp {

    private static final String TAG = "player";

    private Context context;
    private SimpleExoPlayer player;
    private DefaultDataSourceFactory dataSourceFactory;
    private Handler mainHandler;

    public ExoPlayerHelp(Context context){
        this.context = context;
        if (player == null) {
            createPlayer();
        }
    }

    public void setPlayerView(CExoPlayerView playerView) {
        playerView.createPlayerView(player);
    }

    public void player(String url) {
        this.url = url;
        player.prepare(buildMediaSource(url));
    }

    public void start() {
        if (player != null && !player.getPlayWhenReady()) {
            player.setPlayWhenReady(true);
        }
    }

    public void stop() {
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        }
    }

    public void seekTo(long seek) {
        if (player != null) {
            long currentPosition = player.getCurrentPosition();
            if ((currentPosition + seek) >= 0 && (currentPosition + seek) < player.getDuration()) {
                player.seekTo(currentPosition + seek);
            }
        }
    }

    public void controll() {
        if (player != null) {
            player.setPlayWhenReady(!player.getPlayWhenReady());
        }
    }

    private long currentPosition = 0;
    private String url = "";

    public void pause() {
        player.setPlayWhenReady(false);
        currentPosition = getCurrentPosition();
    }

    public void restart(CExoPlayerView playerView) {
        if (player != null) {
//            playerView.getPlayerView().setPlayer(player);
//            playerView.createPlayerView();
            player.prepare(buildMediaSource(url));
            player.seekTo(currentPosition);
            currentPosition = 0;
        }
    }

    public long getDuration() {
        if (player != null) {
            return player.getDuration();
        }
        return 0;
    }

    public long getCurrentPosition() {
        if (player != null) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public void release() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.release();
        }
    }

    public void distroy() {
        release();
        if (player != null) {
            player = null;
        }
        if (dataSourceFactory != null) {
            dataSourceFactory = null;
        }
        if (mainHandler != null) {
            mainHandler = null;
        }
    }

    private void createPlayer() {
        // 创建带宽
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // 创建轨道选择工厂
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        // 创建轨道选择实例
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        // 创建播放器实例
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, getApplicationName(context)), null);
        mainHandler = new Handler(Looper.getMainLooper());

    }

    private MediaSource buildMediaSource(String url) {
        // 创建加载数据的工厂
        Uri uri = Uri.parse(url);
        int type = getUrlType(uri.toString());
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory(TAG, defaultBandwidthMeter);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, dataSourceFactory, new DefaultSsChunkSource.Factory(new DefaultDataSourceFactory(context, defaultBandwidthMeter, factory)), mainHandler, null);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, dataSourceFactory, new DefaultDashChunkSource.Factory(new DefaultDataSourceFactory(context, defaultBandwidthMeter, factory)), mainHandler, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, dataSourceFactory, mainHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, dataSourceFactory, new DefaultExtractorsFactory(), mainHandler, null);
            default: {
                return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            }
        }
    }

    private int getUrlType(String url) {
        if (url.contains(".mpd")) {
            return C.TYPE_DASH;
        } else if (url.contains(".ism") || url.contains(".isml")) {
            return C.TYPE_SS;
        } else if (url.contains(".m3u8")) {
            return C.TYPE_HLS;
        } else {
            return C.TYPE_OTHER;
        }
    }

    private String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    public interface IPlayerStateListener {

        void onFinish();

        void onError();

    }

}
