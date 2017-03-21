package com.harrison.live;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.harrison.live.R;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by heyunpeng on 2017/3/15.
 */

public class VideoPlayActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static String TAG = "VideoPlayActivity";
    private IjkMediaPlayer player;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surface;
    private Button startButton;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        startButton = (Button)findViewById(R.id.start);
        startButton.setOnClickListener(new ButtonListener());

        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        surface = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.start();
        }
    }

    @Override protected void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onStop();

        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (player != null) {
            player.setDisplay(surfaceHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    class ButtonListener implements View.OnClickListener {

        public void onClick(View v) {
            if (player == null) {
                player = new IjkMediaPlayer();

                try {
                    String url = ((TextView) findViewById(R.id.editText)).getText().toString();

                    player.setDataSource(url);

                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setScreenOnWhilePlaying(true);
                    player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                    player.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                            return false;
                        }
                    });
                    player.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

                        }
                    });
                    player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(IMediaPlayer iMediaPlayer) {

                        }
                    });
                    player.setDisplay(surfaceHolder);
                    player.prepareAsync();
                    player.start();
                } catch (Exception e) {
                    player.release();
                    player = null;
                }
            } else {
                player.release();
                player = null;
            }
        }
    }
}
