package com.harrison.com.live;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.Collections;
import java.util.List;

public class CameraController implements CommonHandlerListener {

    private static volatile CameraController sInstance;

    public final static float sCameraRatio = 4f / 3f;
    private final CameraControllerHandler mHandler;

    private Camera mCamera = null;
    private SurfaceTexture mSurfaceTexture;
    private int mDesiredPictureWidth;
    private Camera.Size mPreviewSize;

    public int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
    public boolean mCameraMirrored = false;
    public Camera.Size mCameraPictureSize;

    private final Object mLock = new Object();

    private CameraPictureSizeComparator mCameraPictureSizeComparator = new CameraPictureSizeComparator();

    //////////
    public static CameraController getInstance() {
        if (sInstance == null) {
            synchronized (CameraController.class) {
                if (sInstance == null) {
                    sInstance = new CameraController();
                }
            }
        }
        return sInstance;
    }

    private CameraController() {
        mHandler = new CameraControllerHandler(this);
    }

    public void switchCamera() {
        if (mCameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        release();
        setupCamera(mSurfaceTexture, mDesiredPictureWidth);
        configureCameraParameters(mPreviewSize);
        startCameraPreview();
    }

    public void setupCamera(SurfaceTexture surfaceTexture, int desiredPictureWidth) {
        if (mCamera != null) {
            release();
        }

        synchronized (mLock) {
            try {
                if (Camera.getNumberOfCameras() > 0) {
                    mCamera = Camera.open(mCameraIndex);
                } else {
                    mCamera = Camera.open();
                }

                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(mCameraIndex, cameraInfo);

                mCameraMirrored = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewTexture(surfaceTexture);

                mSurfaceTexture = surfaceTexture;
                mDesiredPictureWidth = desiredPictureWidth;
            } catch (Exception e) {
                e.printStackTrace();
                mCamera = null;
            }

            try {
                findCameraSupportValue(desiredPictureWidth);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void configureCameraParameters(Camera.Size previewSize) {

        try {
            Camera.Parameters cp = getCameraParameters();
            if (cp == null || mCamera == null) {
                return;
            }
            // 对焦模式
            synchronized (mLock) {
                List<String> focusModes = cp.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    cp.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 自动连续对焦
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    cp.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 自动对焦
                }

                // 预览尺寸
                if (previewSize != null) {
                    cp.setPreviewSize(previewSize.width, previewSize.height);
                }

                mCamera.setParameters(cp);
                mPreviewSize = previewSize;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean startCameraPreview() {
        if (mCamera != null) {
            synchronized (mLock) {
                try {
                    mCamera.startPreview();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public boolean stopCameraPreview() {

        if (mCamera != null) {
            synchronized (mLock) {
                try {
                    mCamera.stopPreview();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public void release() {
        if (mCamera != null) {
            synchronized (mLock) {
                try {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.release();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mCamera = null;
                }
            }
        }
    }

    public Camera.Parameters getCameraParameters() {
        if (mCamera != null) {
            synchronized (mLock) {
                try {
                    return mCamera.getParameters();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private void findCameraSupportValue(int desiredWidth) {
        Camera.Parameters cp = getCameraParameters();
        List<Camera.Size> cs = cp.getSupportedPictureSizes();
        if (cs != null && !cs.isEmpty()) {
            Collections.sort(cs, mCameraPictureSizeComparator);
            for (Camera.Size size : cs) {
                if (size.width < desiredWidth && size.height < desiredWidth) {
                    break;
                }
                float ratio = (float) size.width / size.height;
                if (ratio == sCameraRatio) {
                    mCameraPictureSize = size;
                }
            }
        }
    }

    public Camera getCamera() {
        return mCamera;
    }

    private static class CameraControllerHandler extends Handler {

        private CommonHandlerListener listener;

        public CameraControllerHandler(CommonHandlerListener listener) {
            super(Looper.getMainLooper());
            this.listener = listener;
        }

        @Override public void handleMessage(Message msg) {
            listener.handleMessage(msg);
        }
    }

    @Override public void handleMessage(Message msg) {
    }
}
