package com.zzx.libzzxdemo.audio;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.ResourceSubscriber;

//recorder -> encode -> send
//receive -> decode -> play
public class AudioTransManager {

    public interface SendAudio {
        void sendAudioData(byte[] data);
    }

    public static void Log(String a, String b) {
//        Log.i("lzcdemosend", a + " " + b);
    }

    public static void Lor(String a, String b) {
//        Log.i("lzcdemoreceive", a + " " + b);
    }

    private AudioValue.CodecType mCodecType = AudioValue.CodecType.AAC;
    private boolean mNeedSplit = true;

    private AudioInterface.Send mSend;
    private AudioInterface.Receive mReceive;

    private Disposable mSendDisposable = Disposables.empty();
    private Disposable mReceiveDisposable = Disposables.empty();
    private volatile boolean onSend = false;
    private volatile boolean onReceive = false;

    private AudioInterface.Recorder mRecorder;
    private AudioInterface.Play mPlay;
    private AudioInterface.Encode mEncode = null;
    private AudioInterface.Decode mDecode = null;
    private AudioInterface.DataSplit mSplit;

    private BlockingQueue<byte[]> mReceiveQueue;
    private SendAudio mSendAudio;

    public AudioTransManager(@Nonnull SendAudio sendAudio) {
        this(sendAudio, 10);
    }

    public AudioTransManager(@Nonnull SendAudio sendAudio, int size) {
        this.mSendAudio = sendAudio;
        mReceiveQueue = new ArrayBlockingQueue<>(size);
    }

    private void initSend() {
        AudioFactory factory = new AudioFactory();
        if (mEncode == null) {
            setEncode(factory.getEncode(mCodecType));
        }
        if (mRecorder == null) {
            setRecorder(factory.getRecord());
        }
    }

    public void startToSend() {
        Log("start", null);
        initSend();
        onSend = true;
        if (mSend != null) {
            mSend.start();
            mSendDisposable = Flowable
                    .create(new FlowableOnSubscribe<byte[]>() {
                        @Override
                        public void subscribe(FlowableEmitter<byte[]> e) throws Exception {
                            byte[] data = mSend.sendData();
                            e.onNext(data);
                            e.onComplete();
                        }
                    }, BackpressureStrategy.DROP)
                    .repeat()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io())
                    .subscribe(new Consumer<byte[]>() {
                        @Override
                        public void accept(@NonNull byte[] bytes) throws Exception {
                            mSendAudio.sendAudioData(bytes);
                        }
                    });
        } else {
            mEncode.start();
            mRecorder.start();
            Flowable
                    .create(new FlowableOnSubscribe<byte[]>() {
                        @Override
                        public void subscribe(FlowableEmitter<byte[]> e) throws Exception {
                            byte[] data = mRecorder.recorder();
                            Log("send create", data.length + "");
                            e.onNext(data);
                            Log("send create next", data.length + "");
                            e.onComplete();
                        }
                    }, BackpressureStrategy.DROP)
                    .repeat()
                    .subscribeOn(Schedulers.computation())
                    .flatMap((new Function<byte[], Flowable<byte[]>>() {
                        @Override
                        public Flowable<byte[]> apply(@NonNull byte[] bytes) throws Exception {
                            Log("send decode", bytes.length + "");
                            Object object = mEncode.encode(bytes);
                            if (object instanceof Flowable) {
                                return (Flowable<byte[]>) object;
                            } else if (object instanceof byte[]) {
                                return Flowable.just((byte[]) object);
                            }
                            return null;
                        }
                    }))
                    .observeOn(Schedulers.io())
                    .subscribe(new ResourceSubscriber<byte[]>() {

                        @Override
                        public void onStart() {
                            request(1);
                        }

                        @Override
                        public void onNext(byte[] bytes) {
                            Log("send send", bytes.length + "");
                            mSendAudio.sendAudioData(bytes);
                            if (!onSend) {
                                if (!isDisposed()) {
                                    dispose();
                                }
                                endToSend2();
                            } else {
                                request(1);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onComplete() {

                        }

                    });
        }
    }

    public void endToSend() {
        onSend = false;
    }

    private void endToSend2() {
        Log("end", null);
        if (!mSendDisposable.isDisposed()) {
            mSendDisposable.dispose();
        }
        if (mSend != null) {
            mSend.release();
        } else {
            mRecorder.release();
            mEncode.release();
        }
        onSend = false;
    }

    private void initReceive() {
        AudioFactory factory = new AudioFactory();
        if (mDecode == null) {
            setDecode(factory.getDecode(mCodecType));
        }
        if (mPlay == null) {
            setPlay(factory.getPlay());
        }
        if (mSplit == null) {
            setSplit(factory.getSplit(mCodecType, true));
        }
    }

    public void startToReceive() {
        Lor("start", null);
        initReceive();
        onReceive = true;
        if (mReceive != null) {
            mReceive.start();
            mReceiveDisposable = Flowable
                    .create(new FlowableOnSubscribe<byte[]>() {
                        @Override
                        public void subscribe(FlowableEmitter<byte[]> e) throws Exception {
                            try {
                                byte[] data = mReceiveQueue.take();
                                e.onNext(data);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            e.onComplete();
                        }
                    }, BackpressureStrategy.BUFFER)
                    .repeat()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.newThread())
                    .subscribe(new Consumer<byte[]>() {
                        @Override
                        public void accept(@NonNull byte[] bytes) throws Exception {
                            if (bytes != null) {
                                mReceive.receiveData(bytes);
                            }
                        }
                    });
        } else {
            final BlockingQueue<byte[]> splitQueue = mSplit.dataSplit(mReceiveQueue);
            mDecode.start();
            mPlay.start();
            mSplit.start();
            Flowable
                    .create(new FlowableOnSubscribe<byte[]>() {
                        @Override
                        public void subscribe(FlowableEmitter<byte[]> e) {
                            try {
                                byte[] data = splitQueue.take();
                                e.onNext(data);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            e.onComplete();
                        }
                    }, BackpressureStrategy.BUFFER)
                    .repeat()
                    .subscribeOn(Schedulers.computation())
                    .flatMap(new Function<byte[], Flowable<byte[]>>() {
                        @Override
                        public Flowable<byte[]> apply(@NonNull byte[] bytes) throws Exception {
                            Lor("decode", bytes.length + "");
//                            writeFile(bytes);//保存aac 文件
                            Object object = mDecode.decode(bytes);
                            if (object instanceof Flowable) {
                                return (Flowable<byte[]>) object;
                            } else if (object instanceof byte[]) {
                                return Flowable.just((byte[]) object);
                            }
                            return Flowable.empty();
                        }
                    })
                    .observeOn(Schedulers.io())
                    .subscribe(new ResourceSubscriber<byte[]>() {

                        @Override
                        public void onStart() {
                            request(1);
                        }

                        @Override
                        public void onNext(byte[] bytes) {
                            Lor("play", bytes.length + "");
                            mPlay.play(bytes);
                            if (!onReceive) {
                                if (!isDisposed()) {
                                    dispose();
                                }
                                endToReceive2();
                            } else {
                                request(1);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

    }

    public void endToReceive() {
        onReceive = false;
    }

    private void endToReceive2() {
        Lor("end", null);
        if (!mReceiveDisposable.isDisposed()) {
            mReceiveDisposable.dispose();
        }
        if (mReceive != null) {
            mReceive.release();
        } else {
            mDecode.release();
            mPlay.release();
            mSplit.release();
        }
        mReceiveQueue.clear();
        onReceive = false;
    }

    public void receiveAudioData(byte[] data) {
        Lor("recevie", data.length + "");
        try {
            mReceiveQueue.offer(data, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            mReceiveQueue.clear();
            e.printStackTrace();
        }
    }

    public void release() {
        endToSend();
        endToReceive();
    }

    public void setCodecType(AudioValue.CodecType type) {
        this.mCodecType = type;
    }

    public void setNeedSplit(boolean need) {
        this.mNeedSplit = need;
    }

    public void setRecorder(AudioInterface.Recorder recorder) {
        if (!onSend && recorder != null) {
            this.mRecorder = recorder;
        }
    }

    public void setPlay(AudioInterface.Play play) {
        if (!onReceive && play != null) {
            this.mPlay = play;
        }
    }

    public void setEncode(AudioInterface.Encode encode) {
        if (!onSend && encode != null) {
            this.mEncode = encode;
        }
    }

    public void setDecode(AudioInterface.Decode decode) {
        if (!onReceive && decode != null) {
            this.mDecode = decode;
        }
    }

    public void setSend(AudioInterface.Send mSend) {
        if (!onSend && mSend != null) {
            this.mSend = mSend;
        }
    }

    public void setReceive(AudioInterface.Receive mReceive) {
        if (!onReceive && mReceive != null) {
            this.mReceive = mReceive;
        }
    }

    public void setSplit(AudioInterface.DataSplit mSplit) {
        if (!onReceive && mSplit != null) {
            this.mSplit = mSplit;
        }
    }
    /**
     * 把数据流写入文件
     * @param bytes
     */
//    String DEFAULT_PATH = Environment.getExternalStorageDirectory() + File.separator + "zzx.aac";
//    FileOutputStream out;//指定写到哪个路径中
//
//    {
//        try {
//            out = new FileOutputStream(DEFAULT_PATH);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void writeFile( byte[] bytes) {
//        try {
//            out.write(bytes);
//            out.flush();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
