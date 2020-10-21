package com.opentokreactnative;

import android.content.Context;
import android.content.MutableContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.opentok.android.BaseVideoCapturer;

import org.w3c.dom.Text;

import java.security.spec.ECField;

public class OTScreenCapturer extends BaseVideoCapturer {

    private boolean capturing = false;
    private View contentView;

    private int fps = 15;
    private int width = 20;
    private int height = 20;
    private int[] frame;

    private Bitmap bmp;
    private Canvas canvas;

    private Handler mHandler = new Handler();

    private class CameraViewHolder<TexturedView> {
        TextureView cameraView;
    }

    private Runnable newFrame = new Runnable() {
        @Override
        public void run() {
            if (capturing) {

                int width = contentView.getWidth();
                int height = contentView.getHeight();

                // Is the camera view showing? If yes, render only that
                CameraViewHolder cameraViewHolder = new CameraViewHolder();
                findCameraView(contentView, cameraViewHolder);
                if (cameraViewHolder.cameraView != null) {
                    width = cameraViewHolder.cameraView.getWidth();
                    height = cameraViewHolder.cameraView.getHeight();
                }

                if (frame == null ||
                        OTScreenCapturer.this.width != width ||
                        OTScreenCapturer.this.height != height) {

                    OTScreenCapturer.this.width = width;
                    OTScreenCapturer.this.height = height;

                    if (bmp != null) {
                        bmp.recycle();
                        bmp = null;
                    }
                    bmp = Bitmap.createBitmap(width,
                            height, Bitmap.Config.ARGB_8888);

                    canvas = new Canvas(bmp);
                    frame = new int[width * height];
                }

                // No need to draw on canvas if rendering camera
                if (cameraViewHolder.cameraView == null) {
                    try {
                        canvas.save();
                        canvas.translate(-contentView.getScrollX(), - contentView.getScrollY());
                        contentView.draw(canvas);
                        bmp.getPixels(frame, 0, width, 0, 0, width, height);
                    } catch (Exception e) {
                        Log.w("OTScreenCapturer", e.getMessage());
                        canvas.restore();
                        startCapture();
                        return;
                    }
                } else {
                    try {
                        cameraViewHolder.cameraView.getBitmap().getPixels(
                                frame, 0, width, 0, 0, width, height);
                    } catch (Exception e) {
                        startCapture();
                        return;
                    }
                }

                provideIntArrayFrame(frame, ARGB, width, height, 0, false);
                if (cameraViewHolder.cameraView == null) {
                    canvas.restore();
                }

                mHandler.postDelayed(newFrame, 1000 / fps);

            }
        }
    };

    public OTScreenCapturer(View view) {
        this.contentView = view;
    }

    @Override
    public void init() {

    }

    @Override
    public int startCapture() {
        capturing = true;

        mHandler.postDelayed(newFrame, 1000 / fps);
        return 0;
    }

    @Override
    public int stopCapture() {
        capturing = false;
        mHandler.removeCallbacks(newFrame);
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

    private void findCameraView(View rootView, CameraViewHolder cameraViewHolder) {
        if (cameraViewHolder.cameraView == null) {
            if (rootView instanceof ViewGroup) {
                int count = ((ViewGroup) rootView).getChildCount();
                for (int i = 0; i < count; i++) {
                    View v = ((ViewGroup) rootView).getChildAt(i);
                    System.out.println("Root" + rootView.getClass() + " - (" + (i + 1) + " out of " + count + ") - " + v.getClass());
                    if (v instanceof ViewGroup) {
                        findCameraView(v, cameraViewHolder);
                    } else if (v instanceof TextureView) {
                        cameraViewHolder.cameraView = (TextureView)v;
                    }
                }
            }
        }
    }
}