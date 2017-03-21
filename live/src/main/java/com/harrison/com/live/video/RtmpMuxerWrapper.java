package com.harrison.com.live.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.harrison.com.live.rtmp.rtmpModel;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by heyunpeng on 2017/2/8.
 */

public class RtmpMuxerWrapper extends MediaMuxerWrapper {
    private static final boolean DEBUG = false;
    private static final String TAG = "RtmpMuxerWrapper";
    private static final int IDR = 5;
    private static final int SPS = 7;
    private static final int PPS = 8;

    private int videoWidth;
    private int videoHeight;
    private int videoFramerate = 25;
    private int videoBitrate = 120*1000;
    private byte[] sps;
    private byte[] pps;

    private byte[] aacSpec;
    private boolean spsFlag = false;
    private boolean aacFlag = false;
    private int audioChannels = 2;

    private int videoIndex = 0;
    private int audioIndex = 1;

    private boolean isStart = false;
    private long startStamp = 0;

    public RtmpMuxerWrapper() {
        super();
    }

    public String getOutputPath() {
        return super.getOutputPath();
    }

    public void prepare() throws IOException {
        super.prepare();
    }

    public void startRecording() {
        super.startRecording();
    }

    public void stopRecording() {
        super.stopRecording();
    }

    public synchronized boolean isStarted() {
        return super.isStarted();
    }


    synchronized void addEncoder(final MediaEncoder encoder) {
        super.addEncoder(encoder);

        if (encoder instanceof MediaVideoEncoder) {
            //
        } else if (encoder instanceof  MediaAudioEncoder) {
            MediaAudioEncoder audioEncoder = (MediaAudioEncoder)encoder;

            aacSpec = new byte[2];
            int samplerateIndex = getSamplerateIndex(audioEncoder.getSamplerate());
            aacSpec[0] = (byte)(0x10 | ((samplerateIndex >> 1) & 0x7));
            aacSpec[1] = (byte)((samplerateIndex & 0x1) << 7 | ((audioEncoder.getChannel() & 0xF) << 3));
        }
    }

    synchronized boolean start() {

        if (!isStart) {
            isStart = true;
            rtmpModel.connectStream("rtmp://10.0.5.20/live/stream");
            rtmpModel.sendMetadata(videoWidth, videoHeight, videoFramerate, videoBitrate, 44100, 16, audioChannels);
            startStamp = System.currentTimeMillis();
        }

        return isStart;
    }

    synchronized void stop() {
        if (isStart) {
            isStart = false;
            rtmpModel.closeStream();
        }
    }

    synchronized int addTrack(final MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_WIDTH)) {
            videoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
            videoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);

            ByteBuffer spsBuffer = format.getByteBuffer("csd-0");
            sps = new byte[spsBuffer.limit()];
            spsBuffer.get(sps);

            ByteBuffer ppsBuffer = format.getByteBuffer("csd-1");
            pps = new byte[ppsBuffer.limit()];
            ppsBuffer.get(pps);
            return videoIndex;
        } else {

            audioChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            return audioIndex;
        }
    }

    synchronized void writeSampleData(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo) {

        if (trackIndex == videoIndex) {

            if (spsFlag == false) {
                spsFlag = true;

                //send video header
                rtmpModel.sendH264Header(sps, pps);
            } else {
                boolean keyframe = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) == 1;

                if (byteBuf.get(0) == 0 && byteBuf.get(1) == 0 && byteBuf.get(2) == 0 && byteBuf.get(3) == 1) {

                    byte[] data = new byte[bufferInfo.size];
                    byteBuf.get(data);

                    long ts = System.currentTimeMillis() - startStamp;
                    rtmpModel.sendH264(data, keyframe, ts);

                }
            }
        } else if (trackIndex == audioIndex) {
            if (aacFlag == false) {
                aacFlag = true;

                //send audio header
                rtmpModel.sendAACHeader(aacSpec);
            } else {
                byte[] outData = new byte[bufferInfo.size+7];
                byteBuf.get(outData, 7, bufferInfo.size);
                addADTStoPacket(outData, bufferInfo.size);

                //send audio data
                rtmpModel.sendAAC(outData, System.currentTimeMillis() - startStamp);
            }
        }
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE

        // fill in ADTS data
        packet[0] = (byte)0xFF;
        packet[1] = (byte)0xF9;
        packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
        packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
        packet[4] = (byte)((packetLen&0x7FF) >> 3);
        packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
        packet[6] = (byte)0xFC;
    }

    public Surface getInputSurface() {
        return super.getInputSurface();
    }

    static private int getSamplerateIndex(int rate) {
        int index = 0;
        switch (rate) {
            case 96000:
                index = 0;
                break;
            case 64000:
                index = 2;
                break;
            case 44100:
                index = 4;
                break;
            case 8000:
                index = 11;
                break;
            default:
                index = 15;
        }

        return index;
    }
}
