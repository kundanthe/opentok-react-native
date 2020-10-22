package com.opentokreactnative;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Handler;
import com.opentok.android.BaseVideoCapturer;

public class OTScreenCapturerMediaProjection extends BaseVideoCapturer {

    private boolean capturing = false;
    private Activity currentActivity;

    private int fps = 15;
    private int width = 20;
    private int height = 20;
    private int[] frame;

    private Canvas canvas;
    private Handler mHandler = new Handler();

    private Runnable newFrame = new Runnable() {
        @Override
        public void run() {
            if (capturing && ScreenCaptureImageActivity.latestBitmap != null &&
            !ScreenCaptureImageActivity.latestBitmap.isRecycled()) {

                if (frame == null ||
                        ScreenCaptureImageActivity.latestBitmap.getWidth() != width ||
                        ScreenCaptureImageActivity.latestBitmap.getHeight() != height) {

                    OTScreenCapturerMediaProjection.this.width = ScreenCaptureImageActivity.latestBitmap.getWidth();
                    OTScreenCapturerMediaProjection.this.height = ScreenCaptureImageActivity.latestBitmap.getHeight();

                    //canvas = new Canvas(ScreenCaptureImageActivity.latestBitmap);
                    frame = new int[width * height];
                }
                ScreenCaptureImageActivity.latestBitmap.getPixels(frame, 0, width, 0, 0, width, height);
                provideIntArrayFrame(frame, ARGB, width, height, 0, false);
            }
            mHandler.postDelayed(newFrame, 1000 / fps);
        }
    };

    public OTScreenCapturerMediaProjection(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    @Override
    public void init() {

    }

    @Override
    public int startCapture() {
        capturing = true;
        Intent i = new Intent(this.currentActivity, ScreenCaptureImageActivity.class);
        currentActivity.startActivity(i);
        mHandler.postDelayed(newFrame, 1000 / fps);
        return 0;
    }

    @Override
    public int stopCapture() {
        capturing = false;
        mHandler.removeCallbacks(newFrame);
        ScreenCaptureImageActivity.captureActivity.stopProjection();
        return 0;
    }

    @Override
    public boolean isCaptureStarted() {
        return capturing;
    }

    @Override
    public CaptureSettings getCaptureSettings() {

        CaptureSettings settings = new CaptureSettings();
        settings.fps = fps;
        settings.width = width;
        settings.height = height;
        settings.format = ARGB;
        return settings;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }
}