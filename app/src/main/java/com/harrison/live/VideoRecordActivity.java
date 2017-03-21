package com.harrison.live;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.harrison.com.live.CameraController;
import com.harrison.com.live.CameraRecordRenderer;
import com.harrison.com.live.filter.FilterManager.FilterType;
import com.harrison.com.live.video.EncoderConfig;
import com.harrison.com.live.video.TextureMovieEncoder;
import com.harrison.com.live.widget.CameraSurfaceView;

public class VideoRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraSurfaceView mCameraSurfaceView;
    private Button mRecordButton;
    private boolean mIsRecordEnabled = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera);
        mCameraSurfaceView.setAspectRatio(3, 4);

        findViewById(R.id.filter_normal).setOnClickListener(this);
        findViewById(R.id.switchCamera).setOnClickListener(this);

        mRecordButton = (Button) findViewById(R.id.record);
        mRecordButton.setOnClickListener(this);

        mIsRecordEnabled = TextureMovieEncoder.getInstance().isRecording();
    }

    @Override protected void onResume() {
        super.onResume();

        if (mIsRecordEnabled) {
            mCameraSurfaceView.onResume();
        }
    }

    @Override protected void onPause() {
        super.onPause();

        if (mIsRecordEnabled) {
            mCameraSurfaceView.onPause();
        }
    }

    @Override protected void onDestroy() {
        mCameraSurfaceView.onDestroy();
        super.onDestroy();
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_normal:
                mCameraSurfaceView.changeFilter(FilterType.Normal);
                break;
            case R.id.switchCamera:
                CameraController.getInstance().switchCamera();
                break;
            case R.id.record:
                if (mIsRecordEnabled == false) {
                    mIsRecordEnabled = true;

                    CameraRecordRenderer renderer = mCameraSurfaceView.getRenderer();
                    renderer.setEncoderConfig(new EncoderConfig(null, 480, 640, 1024 * 1024));
                } else {
                    mIsRecordEnabled = false;
                }

                mCameraSurfaceView.getRenderer().setRecordingEnabled(mIsRecordEnabled);
                mRecordButton.setText(mIsRecordEnabled ? "停止" : "开始");
                break;
        }
    }
}
