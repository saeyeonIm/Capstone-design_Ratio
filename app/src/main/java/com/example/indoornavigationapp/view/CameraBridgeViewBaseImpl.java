package com.example.indoornavigationapp.view;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import org.opencv.BuildConfig;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public abstract class CameraBridgeViewBaseImpl extends CameraBridgeViewBase {

    private final Matrix mMatrix = new Matrix();

    private static final String TAG = "CameraBridge";
    protected static final int MAX_UNSPECIFIED = -1;
    private static final int STOPPED = 0;
    private static final int STARTED = 1;

    private int mState = STOPPED;
    private Bitmap mCacheBitmap;
    private MyCvCameraViewListener2 mListener;
    private boolean mSurfaceExist;
    private final Object mSyncObject = new Object();
    protected double focalLength;

    public CameraBridgeViewBaseImpl(Context context, int cameraId) {
        super(context, cameraId);
    }

    public CameraBridgeViewBaseImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public interface MyCvCameraViewListener2 {
        /**
         * This method is invoked when camera preview has started. After this method is invoked
         * the frames will start to be delivered to client via the onCameraFrame() callback.
         * @param width -  the width of the frames that will be delivered
         * @param height - the height of the frames that will be delivered
         */
        void onCameraViewStarted(int width, int height, double focalLengths);

        /**
         * This method is invoked when camera preview has been stopped for some reason.
         * No frames will be delivered via onCameraFrame() callback after this method is called.
         */
        void onCameraViewStopped();

        /**
         * This method is invoked when delivery of the frame needs to be done.
         * The returned values - is a modified frame which needs to be displayed on the screen.
         * TODO: pass the parameters specifying the format of the frame (BPP, YUV or RGB and etc)
         */
        Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame);
    };

    protected class CvCameraViewListenerAdapter implements CameraBridgeViewBaseImpl.MyCvCameraViewListener2 {
        public CvCameraViewListenerAdapter(CameraBridgeViewBase.CvCameraViewListener oldStypeListener) {
            mOldStyleListener = oldStypeListener;
        }

        @Override
        public void onCameraViewStarted(int width, int height, double focalLength) {
            mOldStyleListener.onCameraViewStarted(width, height);
        }

        public void onCameraViewStarted(int width, int height) {
            mOldStyleListener.onCameraViewStarted(width, height);
        }

        public void onCameraViewStopped() {
            mOldStyleListener.onCameraViewStopped();
        }

        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            Mat result = null;
            switch (mPreviewFormat) {
                case RGBA:
                    result = mOldStyleListener.onCameraFrame(inputFrame.rgba());
                    break;
                case GRAY:
                    result = mOldStyleListener.onCameraFrame(inputFrame.gray());
                    break;
                default:
                    Log.e(TAG, "Invalid frame format! Only RGBA and Gray Scale are supported!");
            };

            return result;
        }

        public void setFrameFormat(int format) {
            mPreviewFormat = format;
        }

        private int mPreviewFormat = RGBA;
        private CameraBridgeViewBase.CvCameraViewListener mOldStyleListener;
    };

    public void setCvCameraViewListener(CameraBridgeViewBaseImpl.MyCvCameraViewListener2 listener) {
        mListener = listener;
    }

    public void setCvCameraViewListener(CameraBridgeViewBase.CvCameraViewListener listener) {
        CameraBridgeViewBaseImpl.CvCameraViewListenerAdapter adapter = new CameraBridgeViewBaseImpl.CvCameraViewListenerAdapter(listener);
        adapter.setFrameFormat(mPreviewFormat);
        mListener = adapter;
    }

    @Override
    // NOTE: On Android 4.1.x the function must be called before SurfaceTexture constructor!
    protected void AllocateCache()
    {
        mCacheBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        Log.d(TAG, "call surfaceChanged event");
        synchronized(mSyncObject) {
            if (!mSurfaceExist) {
                mSurfaceExist = true;
                checkCurrentState();
            } else {
                /** Surface changed. We need to stop camera and restart with new parameters */
                /* Pretend that old surface has been destroyed */
                mSurfaceExist = false;
                checkCurrentState();
                /* Now use new surface. Say we have it now */
                mSurfaceExist = true;
                checkCurrentState();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized(mSyncObject) {
            mSurfaceExist = false;
            checkCurrentState();
        }
    }

    /**
     * This method is provided for clients, so they can signal camera permission has been granted.
     * The actual onCameraViewStarted callback will be delivered only after setCameraPermissionGranted
     * and enableView have been called and surface is available
     */
    @Override
    public void setCameraPermissionGranted() {
        synchronized(mSyncObject) {
            mCameraPermissionGranted = true;
            checkCurrentState();
        }
    }

    /**
     * This method is provided for clients, so they can enable the camera connection.
     * The actual onCameraViewStarted callback will be delivered only after setCameraPermissionGranted
     * and enableView have been called and surface is available
     */
    @Override
    public void enableView() {
        synchronized(mSyncObject) {
            mEnabled = true;
            checkCurrentState();
        }
    }

    /**
     * This method is provided for clients, so they can disable camera connection and stop
     * the delivery of frames even though the surface view itself is not destroyed and still stays on the screen
     */
    @Override
    public void disableView() {
        synchronized (mSyncObject) {
            mEnabled = false;
            checkCurrentState();
        }
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
        updateMatrix();
    }

    /**
     * This method shall be called by the subclasses when they have valid
     * object and want it to be delivered to external client (via callback) and
     * then displayed on the screen.
     * @param frame - the current frame to be delivered
     */
    @Override
    protected void deliverAndDrawFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        Mat modified;

        if (mListener != null) {
            modified = mListener.onCameraFrame(frame);
        } else {
            modified = frame.rgba();
        }

        boolean bmpValid = true;
        if (modified != null) {
            try {
                Utils.matToBitmap(modified, mCacheBitmap);
            } catch(Exception e) {
                Log.e(TAG, "Mat type: " + modified);
                Log.e(TAG, "Bitmap type: " + mCacheBitmap.getWidth() + "*" + mCacheBitmap.getHeight());
                Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
                bmpValid = false;
            }
        }

        if (bmpValid && mCacheBitmap != null) {
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                int saveCount = canvas.save();
                canvas.setMatrix(mMatrix);
                if (BuildConfig.DEBUG){}

                if (mScale != 0) {
                    canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                            new Rect((int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2),
                                    (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2),
                                    (int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2 + mScale*mCacheBitmap.getWidth()),
                                    (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2 + mScale*mCacheBitmap.getHeight())), null);
                } else {
                    canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                            new Rect((canvas.getWidth() - mCacheBitmap.getWidth()) / 2,
                                    (canvas.getHeight() - mCacheBitmap.getHeight()) / 2,
                                    (canvas.getWidth() - mCacheBitmap.getWidth()) / 2 + mCacheBitmap.getWidth(),
                                    (canvas.getHeight() - mCacheBitmap.getHeight()) / 2 + mCacheBitmap.getHeight()), null);
                }

                //Restore canvas after draw bitmap
                canvas.restoreToCount(saveCount);

                if (mFpsMeter != null) {
                    mFpsMeter.measure();
                    mFpsMeter.draw(canvas, 20, 30);
                }
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }


    /**
     * Called when mSyncObject lock is held
     */
    private void checkCurrentState() {
        Log.d(TAG, "call checkCurrentState");
        int targetState;

        if (mEnabled && mCameraPermissionGranted && mSurfaceExist && getVisibility() == VISIBLE) {
            targetState = STARTED;
        } else {
            targetState = STOPPED;
        }

        if (targetState != mState) {
            /* The state change detected. Need to exit the current state and enter target state */
            processExitState(mState);
            mState = targetState;
            processEnterState(mState);
        }
    }

    private void processEnterState(int state) {
        Log.d(TAG, "call processEnterState: " + state);
        switch(state) {
            case STARTED:
                onEnterStartedState();
                if (mListener != null) {
                    mListener.onCameraViewStarted(mFrameWidth, mFrameHeight, focalLength);
                }
                break;
            case STOPPED:
                onEnterStoppedState();
                if (mListener != null) {
                    mListener.onCameraViewStopped();
                }
                break;
        };
    }

    private void processExitState(int state) {
        Log.d(TAG, "call processExitState: " + state);
        switch(state) {
            case STARTED:
                onExitStartedState();
                break;
            case STOPPED:
                onExitStoppedState();
                break;
        };
    }

    private void onEnterStoppedState() {
        /* nothing to do */
    }

    private void onExitStoppedState() {
        /* nothing to do */
    }

    // NOTE: The order of bitmap constructor and camera connection is important for android 4.1.x
    // Bitmap must be constructed before surface
    private void onEnterStartedState() {
        Log.d(TAG, "call onEnterStartedState");
        /* Connect camera */
        if (!connectCamera(getWidth(), getHeight())) {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false); // This blocks the 'BACK' button
            ad.setMessage("It seems that your device does not support camera (or it is locked). Application will be closed.");
            ad.setButton(DialogInterface.BUTTON_NEUTRAL,  "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ((Activity) getContext()).finish();
                }
            });
            ad.show();

        }
    }

    private void onExitStartedState() {
        disconnectCamera();
        if (mCacheBitmap != null) {
            mCacheBitmap.recycle();
        }
    }

    //카메라가 90도 회전해서 보이는 문제를 해결하는 방법
    private void updateMatrix() {
        float mw = this.getWidth();
        float mh = this.getHeight();

        float hw = this.getWidth() / 2.0f;
        float hh = this.getHeight() / 2.0f;

        float cw  = (float) Resources.getSystem().getDisplayMetrics().widthPixels; //Make sure to import Resources package
        float ch  = (float)Resources.getSystem().getDisplayMetrics().heightPixels;

        float scale = cw / (float)mh;
        float scale2 = ch / (float)mw;
        if(scale2 > scale){
            scale = scale2;
        }

        boolean isFrontCamera = mCameraIndex == CAMERA_ID_FRONT;

        mMatrix.reset();
        if (isFrontCamera) {
            mMatrix.preScale(-1, 1, hw, hh); //MH - this will mirror the camera
        }
        mMatrix.preTranslate(hw, hh);
        if (isFrontCamera){
            mMatrix.preRotate(270);
        } else {
            mMatrix.preRotate(90);
        }
        mMatrix.preTranslate(-hw, -hh);
        mMatrix.preScale(scale,scale,hw,hh);
    }
}
