package com.hxrainbow.myapplication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class CExoPlayerView extends RelativeLayout {

    private ExoPlayerView playerView;
    private Player player;

    private TimerHelp timerHelp;
    private Handler handler;

    public CExoPlayerView(Context context) {
        this(context, null);
    }

    public CExoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        timerHelp = new TimerHelp(1000, 1000);
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1000) {
                    removeCallbacksAndMessages(null);
                    setShowController(false);
                }
            }
        };
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.view_player, null);

        playerView = view.findViewById(R.id.player);

        rlLoading = view.findViewById(R.id.rl_loading);
        ivLoading = view.findViewById(R.id.iv_loading);
        tvLoading = view.findViewById(R.id.tv_loading);

        addView(view);
    }

    public void createPlayerView(SimpleExoPlayer player) {
        this.player = player;
        playerView.setPlayer(player);
        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
                controller();
                if (isLoading) {
                    showLoading();
                } else {
                    dismiss();
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (listener != null && playWhenReady && playbackState == Player.STATE_ENDED) {
                    listener.onFinish();
                }
                if (isAddController && isShowController) {
                    ivImg.setImageResource(CExoPlayerView.this.player.getPlayWhenReady() ? R.mipmap.ic_player_start : R.mipmap.ic_player_stop);
                }
                Log.e("lht", "^^^^^^^^^^^^^^" + playWhenReady + "*************" + playbackState);
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                showError();
                if (listener != null) {
                    listener.onError();
                }
            }

            @Override
            public void onSeekProcessed() {
                controller();
            }
        });
    }

    private LinearLayout llController;
    private ImageView ivImg;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private SeekBar sbProgress;

    private boolean isAddController = false;
    private boolean isShowController = false;

    public void setShowController(boolean showController) {
        isShowController = showController;
        controller();
        if (isShowController) {
            timerHelp.execute(new TimerHelp.ExecuteTask() {
                @Override
                public void update(int count) {
                    controller();
                }
            });
            handler.removeCallbacksAndMessages(null);
            handler.sendEmptyMessageDelayed(1000, 5000);
        } else {
            timerHelp.cancel();
        }
    }

    public void setControllerView(View view) {
        if (view != null) {
            isAddController = true;
            llController = view.findViewById(R.id.ll_controller);
            ivImg = view.findViewById(R.id.iv_stop_start);
            tvCurrentTime = view.findViewById(R.id.tv_current);
            tvTotalTime = view.findViewById(R.id.tv_total);
            sbProgress = view.findViewById(R.id.sb_progress);
        }
    }

    private void controller() {
        if (isAddController) {
            if (isShowController && player != null) {
                ivImg.setImageResource(player.getPlayWhenReady() ? R.mipmap.ic_player_start : R.mipmap.ic_player_stop);
                tvCurrentTime.setText(Util.numFormat(player.getCurrentPosition()));
                tvTotalTime.setText(Util.numFormat(player.getDuration()));
                sbProgress.setProgress((int) (player.getCurrentPosition() * 100 / player.getDuration()));
                sbProgress.setSecondaryProgress((int) (player.getBufferedPosition() * 100 / player.getDuration()));
            }
            llController.setVisibility(isShowController ? VISIBLE : GONE);
        }
    }

    private RelativeLayout rlLoading;
    private ImageView ivLoading;
    private TextView tvLoading;

    public void showLoading() {
        rlLoading.setVisibility(VISIBLE);
        ivLoading.setVisibility(VISIBLE);
        tvLoading.setVisibility(GONE);
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(2000);//设置动画持续周期
        rotate.setRepeatCount(Animation.INFINITE);//设置重复次数
        rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        ivLoading.setAnimation(rotate);
    }

    public void showError() {
        rlLoading.setVisibility(VISIBLE);
        ivLoading.clearAnimation();
        ivLoading.setVisibility(GONE);
        tvLoading.setVisibility(VISIBLE);
    }

    public void dismiss() {
        rlLoading.setVisibility(GONE);
        ivLoading.clearAnimation();
        ivLoading.setVisibility(GONE);
        tvLoading.setVisibility(GONE);
    }

    private ExoPlayerHelp.IPlayerStateListener listener;

    public void setPlayerStateListener(ExoPlayerHelp.IPlayerStateListener listener) {
        this.listener = listener;
    }

}
