package com.harrison.live;

import android.app.Application;
import com.harrison.com.live.video.TextureMovieEncoder;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        TextureMovieEncoder.initialize(getApplicationContext());
    }
}
