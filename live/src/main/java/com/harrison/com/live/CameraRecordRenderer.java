package com.harrison.com.live;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import com.harrison.com.live.filter.FilterManager;
import com.harrison.com.live.filter.FilterManager.FilterType;
import com.harrison.com.live.gles.FullFrameRect;
import com.harrison.com.live.gles.GlUtil;
import com.harrison.com.live.video.EncoderConfig;
import com.harrison.com.live.video.TextureMovieEncoder;
import com.harrison.com.live.widget.CameraSurfaceView;

public class CameraRecordRenderer implements GLSurfaceView.Renderer {

    private final Context mApplicationContext;
    private final CameraSurfaceView.CameraHandler mCameraHandler;
    private int mTextureId = GlUtil.NO_TEXTURE;
    private FullFrameRect mFullScreen;
    private SurfaceTexture mSurfaceTexture;
    private final float[] mSTMatrix = new float[16];

    private FilterType mCurrentFilterType;
    private FilterType mNewFilterType;
    private TextureMovieEncoder mVideoEncoder;
    private EGLContext mGLContext;
    private EncoderConfig mEncoderConfig;

    private float mMvpScaleX = 1f, mMvpScaleY = 1f;
    private int mSurfaceWidth, mSurfaceHeight;
    private int mIncomingWidth, mIncomingHeight;

    public CameraRecordRenderer(Context applicationContext, CameraSurfaceView.CameraHandler cameraHandler) {
        mApplicationContext = applicationContext;
        mCameraHandler = cameraHandler;
        mCurrentFilterType = mNewFilterType = FilterType.Normal;
        mVideoEncoder = TextureMovieEncoder.getInstance();
    }

    public void setEncoderConfig(EncoderConfig encoderConfig) {
        mEncoderConfig = encoderConfig;
    }

    public void setRecordingEnabled(boolean recordingEnabled) {
        if (recordingEnabled) {
            mEncoderConfig.updateEglContext(mGLContext);
            mVideoEncoder.startRecording(mEncoderConfig);
            mVideoEncoder.setTextureId(mTextureId);
            mVideoEncoder.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
        } else {
            mVideoEncoder.stopRecording();
        }
    }

    public void setCameraPreviewSize(int width, int height) {
        mIncomingWidth = width;
        mIncomingHeight = height;

        float scaleHeight = mSurfaceWidth / (width * 1f / height * 1f);
        float surfaceHeight = mSurfaceHeight;

        if (mFullScreen != null) {
            mMvpScaleX = 1f;
            mMvpScaleY = scaleHeight / surfaceHeight;
            mFullScreen.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
        }
    }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Matrix.setIdentityM(mSTMatrix, 0);
        mVideoEncoder.initFilter(mCurrentFilterType);
        mFullScreen = new FullFrameRect(FilterManager.getCameraFilter(mCurrentFilterType, mApplicationContext));
        mTextureId = mFullScreen.createTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        if (gl != null) {
            gl.glViewport(0, 0, width, height);
        }

        mCameraHandler.sendMessage(mCameraHandler.obtainMessage(CameraSurfaceView.CameraHandler.SETUP_CAMERA, width, height, mSurfaceTexture));
    }

    @Override public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.updateTexImage();
        if (mNewFilterType != mCurrentFilterType) {
            mFullScreen.changeProgram(FilterManager.getCameraFilter(mNewFilterType, mApplicationContext));
            mCurrentFilterType = mNewFilterType;
        }

        mFullScreen.getFilter().setTextureSize(mIncomingWidth, mIncomingHeight);
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);
        mGLContext = EGL14.eglGetCurrentContext();

        videoOnDrawFrame(mSTMatrix, mSurfaceTexture.getTimestamp());
    }

    private void videoOnDrawFrame(float[] texMatrix, long timestamp) {

        mVideoEncoder.updateFilter(mCurrentFilterType);
        mVideoEncoder.frameAvailable(texMatrix, timestamp);
    }

    public void notifyPausing() {

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        if (mFullScreen != null) {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             // to be destroyed
        }
    }

    public void changeFilter(FilterType filterType) {
        mNewFilterType = filterType;
    }
}
