package com.samlanning.core.server.util;

import java.io.IOException;
import java.io.InputStream;

public class InterruptableBufferedInputStreamWrapper {

    private final Object mutex = new Object();
    private final InputStream is;
    private final byte[] bytes;

    private Thread thread;
    private ReaderState state = ReaderState.IDLE;

    public InterruptableBufferedInputStreamWrapper(InputStream is, int buffer) {
        this.is = is;
        this.bytes = new byte[buffer];
    }

    public void interrupt() {
        synchronized (mutex) {
            state = ReaderState.INTERRUPTED;
            try {
                is.close();
            } catch (IOException e) {
                // Failure is fine
            }
            mutex.notifyAll();
        }
    }

    public void read(byte[] output) throws InterruptedException {
        synchronized (mutex) {
            if (output.length != bytes.length)
                throw new InternalError("Invalid array size");
            if (thread == null)
                startThread();
            loop: while (true) {
                switch (state) {
                    case IDLE:
                        state = ReaderState.WAITING;
                        mutex.notifyAll();
                        continue loop;
                    case WAITING:
                        mutex.wait();
                        continue loop;
                    case READY:
                        // Copy value
                        for (int i = 0; i < bytes.length; i++)
                            output[i] = bytes[i];
                        state = ReaderState.IDLE;
                        return;
                    case INTERRUPTED:
                        throw new InterruptedException();
                }
            }
        }
    }

    private void startThread() {
        thread = new Thread() {
            @Override
            public void run() {
                InterruptableBufferedInputStreamWrapper parent =
                    InterruptableBufferedInputStreamWrapper.this;
                byte[] bytes = new byte[parent.bytes.length]; // buffer to read
                                                              // bytes into
                while (true) {
                    int bytesRead = 0;
                    while (bytesRead < bytes.length) {
                        int ret;
                        try {
                            ret = is.read(bytes, bytesRead, bytes.length - bytesRead);
                        } catch (IOException e) {
                            parent.interrupt();
                            return;
                        }
                        if (ret < 0) {
                            // no more data
                            parent.interrupt();
                            return;
                        }
                        bytesRead += ret;
                    }
                    try {
                        // Bytes Ready
                        synchronized (mutex) {
                            loop: while (true) {
                                switch (state) {
                                    case IDLE:
                                        mutex.wait();
                                        continue loop;
                                    case WAITING:
                                        for (int i = 0; i < bytes.length; i++)
                                            parent.bytes[i] = bytes[i];
                                        state = ReaderState.READY;
                                        mutex.notifyAll();
                                        break loop;
                                    case READY:
                                        mutex.wait();
                                        continue loop;
                                    case INTERRUPTED:
                                        return;
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        parent.interrupt();
                        return;
                    }
                }
            }
        };
        thread.start();
    }

    private static enum ReaderState {
        IDLE,
        WAITING,
        READY,
        INTERRUPTED
    }

}
