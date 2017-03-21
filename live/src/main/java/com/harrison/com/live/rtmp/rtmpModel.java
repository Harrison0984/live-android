package com.harrison.com.live.rtmp;

/**
 * Created by heyunpeng on 2017/2/20.
 */

public class rtmpModel {
    static {
        System.loadLibrary("rtmpmodel");
    }
    public static native boolean connectStream(String svr);
    public static native void sendMetadata(int width, int height, int frameRate, int videoRate, int audioRate, int audioSample, int audioChannels);
    public static native void sendH264Header(byte[] sps, byte[] pps);
    public static native void sendH264(byte[] data, boolean keyFrame, long timestamp);
    public static native void sendAACHeader(byte[] data);
    public static native void sendAAC(byte[] data, long timestamp);
    public static native void closeStream();
}
