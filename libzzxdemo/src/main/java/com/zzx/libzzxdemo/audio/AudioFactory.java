package com.zzx.libzzxdemo.audio;

import java.util.concurrent.BlockingQueue;

class AudioFactory {

    AudioInterface.Play getPlay() {
        return new AudioPlayerAdmin(8000);
    }

    AudioInterface.Recorder getRecord() {
        return new AudioRecorderAdmin(8000);
    }

    AudioInterface.Decode getDecode(AudioValue.CodecType type) {
        switch (type) {
            case AAC:
                return new AudioAACCodecAdmin(8000, false);
            case G726:
                return new AudioG726CodecAdmin(false);
            default:
                return null;
        }
    }

    AudioInterface.Encode getEncode(AudioValue.CodecType type) {
        switch (type) {
            case AAC:
                return new AudioAACCodecAdmin(8000, true);
            case G726:
                return new AudioG726CodecAdmin(true);
            default:
                return null;
        }
    }

    AudioInterface.DataSplit getSplit(AudioValue.CodecType type, boolean need) {
        if (need) {
            switch (type) {
                case AAC:
                    return new AudioAACCodecAdmin.AACdataSplit();
                case G726:
                    return new SplitEmpty();
                default:
                    return null;
            }
        } else {
            return new SplitEmpty();
        }
    }

    private class SplitEmpty implements AudioInterface.DataSplit {
        @Override
        public void start() {

        }

        @Override
        public void release() {

        }

        @Override
        public BlockingQueue<byte[]> dataSplit(BlockingQueue<byte[]> queue) {
            return queue;
        }
    }
}
