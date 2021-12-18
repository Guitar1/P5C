package com.zzx.libzzxdemo.audio;

import java.util.concurrent.BlockingQueue;

//recorder -> encode -> send
//receive -> decode -> play
public interface AudioInterface {


    void start();

    void release();

    interface DataSplit extends AudioInterface {
        BlockingQueue<byte[]> dataSplit(BlockingQueue<byte[]> queue);
    }

    interface Encode<T> extends AudioInterface {

        T encode(byte[] data);

    }

    interface Decode<T> extends AudioInterface {

        T decode(byte[] data);

    }

    interface Recorder extends AudioInterface {

        byte[] recorder();

    }

    interface Play extends AudioInterface {

        void play(byte[] data);

    }

    interface Send extends AudioInterface {

        byte[] sendData();

    }

    interface Receive extends AudioInterface {
        void receiveData(byte[] data);
    }

}
