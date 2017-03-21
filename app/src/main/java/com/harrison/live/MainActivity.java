package com.harrison.live;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.harrison.live.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.video_anchor).setOnClickListener(this);
        findViewById(R.id.video_audience).setOnClickListener(this);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_anchor:
                startActivity(new Intent(this, VideoRecordActivity.class));
                break;
            case R.id.video_audience:
                startActivity(new Intent(this, VideoPlayActivity.class));
                break;
        }
    }
}
