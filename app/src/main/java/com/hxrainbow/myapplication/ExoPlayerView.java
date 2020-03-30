package com.hxrainbow.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.video.VideoListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ExoPlayerView extends FrameLayout implements View.OnLayoutChangeListener {

    TextureView playerView;
    private int textureViewRotation;

    public interface RatioListener {

        void onRatioUpdated(float targetAspectRatio, float naturalAspectRatio, boolean aspectRatioMismatch);

    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            RESIZE_MODE_FIT,
            RESIZE_MODE_FIXED_WIDTH,
            RESIZE_MODE_FIXED_HEIGHT,
            RESIZE_MODE_FILL,
            RESIZE_MODE_ZOOM
    })
    public @interface ResizeMode {
    }

    public static final int RESIZE_MODE_FIT = 0;
    public static final int RESIZE_MODE_FIXED_WIDTH = 1;
    public static final int RESIZE_MODE_FIXED_HEIGHT = 2;
    public static final int RESIZE_MODE_FILL = 3;
    public static final int RESIZE_MODE_ZOOM = 4;
    private static final float MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f;

    private final AspectRatioUpdateDispatcher aspectRatioUpdateDispatcher;
    private RatioListener aspectRatioListener;
    private float videoAspectRatio;
    private @ResizeMode
    int resizeMode;

    public ExoPlayerView(Context context) {
        this(context, null);
    }

    public ExoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resizeMode = RESIZE_MODE_FIT;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExoPlayerView, 0, 0);
            try {
                resizeMode = a.getInt(R.styleable.ExoPlayerView_resize_mode, RESIZE_MODE_FIT);
            } finally {
                a.recycle();
            }
        }
        aspectRatioUpdateDispatcher = new AspectRatioUpdateDispatcher();
    }

    public void setAspectRatio(float widthHeightRatio) {
        if (this.videoAspectRatio != widthHeightRatio) {
            this.videoAspectRatio = widthHeightRatio;
            requestLayout();
        }
    }

    public void setAspectRatioListener(RatioListener listener) {
        this.aspectRatioListener = listener;
    }

    @ResizeMode
    public int getResizeMode() {
        return resizeMode;
    }

    public void setResizeMode(@ResizeMode int resizeMode) {
        if (this.resizeMode != resizeMode) {
            this.resizeMode = resizeMode;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (videoAspectRatio <= 0) {
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float viewAspectRatio = (float) width / height;
        float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
        if (Math.abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
            aspectRatioUpdateDispatcher.scheduleUpdate(videoAspectRatio, viewAspectRatio, false);
            return;
        }

        switch (resizeMode) {
            case RESIZE_MODE_FIXED_WIDTH:
                height = (int) (width / videoAspectRatio);
                break;
            case RESIZE_MODE_FIXED_HEIGHT:
                width = (int) (height * videoAspectRatio);
                break;
            case RESIZE_MODE_ZOOM:
                if (aspectDeformation > 0) {
                    width = (int) (height * videoAspectRatio);
                } else {
                    height = (int) (width / videoAspectRatio);
                }
                break;
            case RESIZE_MODE_FIT:
                if (aspectDeformation > 0) {
                    height = (int) (width / videoAspectRatio);
                } else {
                    width = (int) (height * videoAspectRatio);
                }
                break;
            case RESIZE_MODE_FILL:
            default:
                break;
        }
        aspectRatioUpdateDispatcher.scheduleUpdate(videoAspectRatio, viewAspectRatio, true);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    private final class AspectRatioUpdateDispatcher implements Runnable {

        private float targetAspectRatio;
        private float naturalAspectRatio;
        private boolean aspectRatioMismatch;
        private boolean isScheduled;

        public void scheduleUpdate(
                float targetAspectRatio, float naturalAspectRatio, boolean aspectRatioMismatch) {
            this.targetAspectRatio = targetAspectRatio;
            this.naturalAspectRatio = naturalAspectRatio;
            this.aspectRatioMismatch = aspectRatioMismatch;

            if (!isScheduled) {
                isScheduled = true;
                post(this);
            }
        }

        @Override
        public void run() {
            isScheduled = false;
            if (aspectRatioListener == null) {
                return;
            }
            aspectRatioListener.onRatioUpdated(targetAspectRatio, naturalAspectRatio, aspectRatioMismatch);
        }
    }

    public SimpleExoPlayer player;

    public void setPlayer(SimpleExoPlayer player) {
        this.player = player;
        setResizeMode(RESIZE_MODE_FIT);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        playerView = new TextureView(getContext());
        playerView.setLayoutParams(params);
        addView(playerView, 0);
        player.setVideoTextureView(playerView);
        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                float videoAspectRatio = (height == 0 || width == 0) ? 1 : (width * pixelWidthHeightRatio) / height;
//                if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
//                    if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
//                        int viewHeight = playerView.getHeight();
//                        int viewWidth = viewHeight * width / height;
//                        float pivotX = viewWidth / 2f;
//                        float pivotY = viewHeight / 2f;
//                        Matrix transform = new Matrix();
//                        transform.postRotate(unappliedRotationDegrees, pivotX, pivotY);
//                        playerView.setTransform(transform);
//                    }
//                }
                if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
                    videoAspectRatio = 1 / videoAspectRatio;
                }
                if (textureViewRotation != 0) {
                    playerView.removeOnLayoutChangeListener(ExoPlayerView.this);
                }
                textureViewRotation = unappliedRotationDegrees;
                if (textureViewRotation != 0) {
                    playerView.addOnLayoutChangeListener(ExoPlayerView.this);
                }
                applyTextureViewRotation(playerView, textureViewRotation);
                setAspectRatio(videoAspectRatio);
            }

            @Override
            public void onRenderedFirstFrame() {

            }
        });
    }

    private static void applyTextureViewRotation(TextureView textureView, int textureViewRotation) {
        float textureViewWidth = textureView.getWidth();
        float textureViewHeight = textureView.getHeight();
        if (textureViewWidth == 0 || textureViewHeight == 0 || textureViewRotation == 0) {
            textureView.setTransform(null);
        } else {
            Matrix transformMatrix = new Matrix();
            float pivotX = textureViewWidth / 2;
            float pivotY = textureViewHeight / 2;
            transformMatrix.postRotate(textureViewRotation, pivotX, pivotY);

            // After rotation, scale the rotated texture to fit the TextureView size.
            RectF originalTextureRect = new RectF(0, 0, textureViewWidth, textureViewHeight);
            RectF rotatedTextureRect = new RectF();
            transformMatrix.mapRect(rotatedTextureRect, originalTextureRect);
            transformMatrix.postScale(
                    textureViewWidth / rotatedTextureRect.width(),
                    textureViewHeight / rotatedTextureRect.height(),
                    pivotX,
                    pivotY);
            textureView.setTransform(transformMatrix);
        }
    }

    @Override
    public void onLayoutChange(
            View view,
            int left,
            int top,
            int right,
            int bottom,
            int oldLeft,
            int oldTop,
            int oldRight,
            int oldBottom) {
        applyTextureViewRotation((TextureView) view, textureViewRotation);
    }

    public SimpleExoPlayer getPlayer(){
        return player;
    }

}
