package com.infocam;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/** A basic Camera preview class */
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder h) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            //mCamera.setDisplayOrientation(90);
            if(mCamera == null)
                Log.d("TAG", "mcamera null");
            if(mHolder == null)
                Log.d("TAG", "mHolder null");
            int screenWidth = (int) getResources().getDisplayMetrics().widthPixels;
            int screenHeight = (int) getResources().getDisplayMetrics().heightPixels;
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, (int) getResources().getDisplayMetrics().widthPixels, (int) getResources().getDisplayMetrics().heightPixels);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d("TAG", "Error setting camera preview: " + e.getMessage());
        }
    }
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();

            try {

                List<Camera.Size> supportedSizes = null;
                // On older devices (<1.6) the following will fail
                // the camera will work nevertheless
                supportedSizes = Compatibility.getSupportedPreviewSizes(parameters);

                // preview form factor
                float ff = (float) w / h;
                Log.d("InfoCam", "Screen res: w:" + w + " h:" + h
                        + " aspect ratio:" + ff);

                // holder for the best form factor and size
                float bff = 0;
                int bestw = 0;
                int besth = 0;
                Iterator<Camera.Size> itr = supportedSizes.iterator();

                // we look for the best preview size, it has to be the closest
                // to the
                // screen form factor, and be less wide than the screen itself
                // the latter requirement is because the HTC Hero with update
                // 2.1 will
                // report camera preview sizes larger than the screen, and it
                // will fail
                // to initialize the camera
                // other devices could work with previews larger than the screen
                // though
                while (itr.hasNext()) {
                    Camera.Size element = itr.next();
                    // current form factor
                    float cff = (float) element.width / element.height;
                    // check if the current element is a candidate to replace
                    // the best match so far
                    // current form factor should be closer to the bff
                    // preview width should be less than screen width
                    // preview width should be more than current bestw
                    // this combination will ensure that the highest resolution
                    // will win
                    Log.d("InfoCam", "Candidate camera element: w:"
                            + element.width + " h:" + element.height
                            + " aspect ratio:" + cff);
                    if ((ff - cff <= ff - bff) && (element.width <= w)
                            && (element.width >= bestw)) {
                        bff = cff;
                        bestw = element.width;
                        besth = element.height;
                    }
                }
                Log.d("InfoCam", "Chosen camera element: w:" + bestw + " h:"
                        + besth + " aspect ratio:" + bff);
                // Some Samsung phones will end up with bestw and besth = 0
                // because their minimum preview size is bigger then the screen
                // size.
                // In this case, we use the default values: 480x320
                if ((bestw == 0) || (besth == 0)) {
                    Log.d("InfoCam", "Using default camera parameters!");
                    bestw = 480;
                    besth = 320;
                }
                parameters.setPreviewSize(bestw, besth);
            } catch (Exception ex) {
                parameters.setPreviewSize(480, 320);
            }

            mCamera.setParameters(parameters);
            mCamera.startPreview();
            /*camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera cam) {
                    Camera.Size previewSize = cam.getParameters().getPreviewSize();
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
                    byte[] jdata = baos.toByteArray();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
                    int rgbColor = bitmap.getPixel(200, 300);
                    Log.v("pixell", "color : " + rgbColor);
                }
            });*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}