package com.samlanning.core.server.lighting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bff.javampd.song.MPDSong;
import org.slf4j.Logger;

import com.samlanning.core.server.util.InterruptableBufferedInputStreamWrapper;
import com.samlanning.core.server.util.Listenable;
import com.samlanning.core.server.util.Logging;
import com.samlanning.synesthesia.player.EventMarker;
import com.samlanning.synesthesia.player.EventPlayer;
import com.samlanning.synesthesia.player.EventSheet;

public class LightingControl extends Listenable<LightingControl.Listener> {

    private static final Logger log = Logging.logger(LightingControl.class);

    private final String host;
    private final int port;

    private LightingThread thread;

    public LightingControl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void start() {
        if (thread == null) {
            thread = new LightingThread();
            thread.start();
        } else {
            throw new RuntimeException("thread already started");
        }
    }

    public synchronized void setColor(RGBLightValue color) {
        thread.currentLightColorSetting = color;
        thread.doInterrupt();
    }

    public synchronized void setStaticBrightness(float brightness) {
        thread.staticBrightness = brightness;
        thread.doInterrupt();
    }

    public synchronized void setStateStatic() {
        log.info("setting state static");
        thread.state = LightState.STATIC;
        thread.doInterrupt();
    }

    public synchronized void setStatePlayingMusic(long songStartTime) {
        thread.songStartTime = songStartTime;
        thread.state = LightState.MUSIC;
        thread.doInterrupt();
    }

    public synchronized void setCurrentSong(MPDSong song, long songStartTime) {
        thread.songStartTime = songStartTime;
        thread.currentSong = song;
        thread.doInterrupt();
    }

    private enum LightState {
        STATIC,
        MUSIC
    }

    private class LightingThread extends Thread {

        private RGBLightValue currentLightColorSetting = new RGBLightValue(0, 0, 0);
        private LightState state = LightState.STATIC;
        private float staticBrightness = 0.0f;
        private float musicBrightness = 1.0f;

        private MPDSong currentSong;
        private long songStartTime;

        private InterruptableBufferedInputStreamWrapper bisw;

        private LightThread light = new LightThread();

        private void doInterrupt() {
            this.interrupt();
            if (bisw != null) {
                bisw.interrupt();
                bisw = null;
            }
        }

        @Override
        public void run() {
            light.start();
            while (true) {
                switch (state) {
                    case STATIC:
                        light.setLight(currentLightColorSetting, staticBrightness, 1000);
                        break;
                    case MUSIC:
                        light.setLight(currentLightColorSetting, musicBrightness, 1000);
                        linkLightToMusic(musicBrightness);
                        continue;
                }
                // Sleep for 10 seconds, and re-set the light
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }

        private void linkLightToMusic(float brightness) {
            if (this.currentSong != null && this.currentSong.getArtistName().equals("Feed Me")
                && this.currentSong.getTitle().equals("Onstuh"))
                this.playCueSheet(getFeedMeOnstuhEventSheet());
            else
                this.playFromFifo(brightness);
        }

        private void playFromFifo(float brightness) {
            try (FileInputStream is = new FileInputStream("/run/mpd/mpd.fifo")) {
                bisw = new InterruptableBufferedInputStreamWrapper(is, 4);
                byte[] bytes = new byte[4]; // buffer to read bytes into
                ByteBuffer bb = ByteBuffer.allocate(4);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                int count = 0;
                int maxLeft = 0;
                int maxRight = 0;
                while (true) {
                    bisw.read(bytes);
                    bb.rewind();
                    bb.put(bytes);
                    int left = bb.getShort(0);
                    int right = bb.getShort(2);
                    maxLeft = Math.max(left, maxLeft);
                    maxRight = Math.max(right, maxRight);
                    count++;
                    if (count >= 3000) {
                        light.setBrightnessNow(Math.max(maxRight, maxLeft)
                            / (float) Short.MAX_VALUE * brightness);
                        count = 0;
                        maxLeft = 0;
                        maxRight = 0;
                    }

                }
            } catch (FileNotFoundException e) {
                log.error("unable to open fifo");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    return;
                }
            } catch (InterruptedException | IOException e) {
                // IOException may be caused by closing the fifo due to a
                // different interrupt
                log.info("Stopped using fifo");
            }
        }

        private void playCueSheet(EventSheet<LightFlash> sheet) {
            System.out.println("playing cue sheet");
            
            EventPlayer.EventHandler<LightFlash> handler = new EventPlayer.EventHandler<LightFlash>() {

                @Override
                public void handle(LightFlash event) {
                    System.out.println("flash! "+ event.duration);
                    light.flashLight(event.color, 1f, event.duration);
                }
                
            };

            EventPlayer<LightFlash> player = new EventPlayer<>(sheet, handler);

            long startTime = this.songStartTime;
            try {
                player.playAndWait(startTime);
                // Player finished, wait until interrupted
                while (true)
                    Thread.sleep(10000);
            } catch (InterruptedException e1) {
                player.stop();
                return;
            }
        }

    }

    /**
     * Thread to control a single light
     */
    private class LightThread extends Thread {

        private OutputStream lightingOutputStream;
        private long lastHostError;

        private RGBLightValue currentLightColorValue = new RGBLightValue(0, 0, 0);
        private float currentLightBrightness = 1f;

        private RGBLightValue targetLightColorValue = new RGBLightValue(0, 0, 0);
        private float targetLightBrightness = 1f;
        private long transitionDuration = 0;

        /** If an notify should be used instead of an interrupt */
        private boolean waiting = false;
        private boolean updated = false;

        public void run() {
            outer_loop: while (true) {

                // Next Operation
                final RGBLightValue targetLightColorValue;
                final float targetLightBrightness;
                final long transitionDuration;

                synchronized (this) {
                    while (!updated)
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            continue;
                        }
                    targetLightColorValue = this.targetLightColorValue;
                    targetLightBrightness = this.targetLightBrightness;
                    transitionDuration = this.transitionDuration;
                    this.updated = false;
                }

                // Check if light needs to be updated
                if (!targetLightColorValue.equals(currentLightColorValue)
                    || targetLightBrightness != currentLightBrightness) {

                    if (transitionDuration == 0) {
                        // Update immediately
                        currentLightColorValue = targetLightColorValue;
                        currentLightBrightness = targetLightBrightness;
                        this.updateLight();
                    } else {
                        // Transition slowly
                        RGBLightValue oldColor = this.currentLightColorValue;
                        float oldBrightness = this.currentLightBrightness;
                        long start = System.currentTimeMillis();
                        long end = start + transitionDuration;
                        long now = start;
                        while ((now = System.currentTimeMillis()) < end) {
                            float transitionAmt = (float) (now - start) / transitionDuration;
                            synchronized(this) {
                                this.currentLightColorValue =
                                    oldColor.transition(targetLightColorValue, transitionAmt);
                                this.currentLightBrightness =
                                    transitionAmt * targetLightBrightness + (1f - transitionAmt)
                                        * oldBrightness;
                            }
                            updateLight();
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                // If interrupted, need to cancel transition
                                System.out.println("interrupted, cancelling transition");
                                continue outer_loop;
                            }
                            if (this.updated) {
                                // If updated, need to cancel transition
                                continue outer_loop;
                            }
                        }
                        synchronized(this) {
                            currentLightColorValue = targetLightColorValue;
                            currentLightBrightness = targetLightBrightness;
                        }
                        this.updateLight();
                    }
                }
            }
        }

        public synchronized void setBrightnessNow(float brightness) {
            this.targetLightBrightness = brightness;
            this.transitionDuration = 0;
            this.updated = true;
            this.notify();
        }

        public synchronized void setLight(RGBLightValue color, float brightness,
            long transitionDuration) {
            this.targetLightBrightness = brightness;
            this.targetLightColorValue = color;
            this.transitionDuration = transitionDuration;
            this.updated = true;
            this.notify();
        }

        public synchronized void flashLight(RGBLightValue color, float brightness, long transitionDuration) {
            this.currentLightColorValue = color;
            this.currentLightBrightness = brightness;
            this.targetLightBrightness = 0f;
            this.targetLightColorValue = color;
            this.transitionDuration = transitionDuration;
            this.updated = true;
            this.notify();
        }

        private void updateLight() {
            Visitor<Listener> visitor =
                l -> l.newLightColor(currentLightColorValue, currentLightBrightness);
            LightingControl.this.updateNewListenerVisitor(visitor);
            LightingControl.this.visitListeners(visitor);
            if (this.lightingOutputStream == null) {
                if (this.lastHostError > System.currentTimeMillis() - 5000) {
                    // Don't try and reconnect more than every 5 seconds
                    return;
                }
                try {
                    Socket clientSocket = new Socket();
                    clientSocket.connect(new InetSocketAddress(LightingControl.this.host,
                        LightingControl.this.port), 1000);
                    this.lightingOutputStream = clientSocket.getOutputStream();
                    log.info("Connected to light");
                } catch (IOException e) {
                    log.warn("Error connecting to host");
                    lastHostError = System.currentTimeMillis();
                    return;
                }
            }
            try {
                RGBLightValue light = currentLightColorValue.brightness(currentLightBrightness);
                lightingOutputStream.write(new byte[] { (byte) light.red, (byte) light.green,
                    (byte) light.blue, (byte) 255 });
                lightingOutputStream.flush();
            } catch (IOException e) {
                log.warn("Error updating light, resetting");
                this.lightingOutputStream = null;
            }
        }
    }

    public interface Listener {
        public void newLightColor(RGBLightValue color, float brightness);
    }
    
    private static class LightFlash {
        public final long duration;
        public final RGBLightValue color;

        public LightFlash(long duration, RGBLightValue color) {
            this.duration = duration;
            this.color = color;
        }
    }

    private static EventSheet<LightFlash> getFeedMeOnstuhEventSheet() {

        List<EventMarker<LightFlash>> markers = new ArrayList<>();

        int step = 935;
        int offset = 900;
        
        
        
        for (int i = 0; i< 32; i += 2){
            markers.add(new EventMarker<>(offset + i * step, new LightFlash(100, new RGBLightValue(0xff, 0, 0xff))));
            markers.add(new EventMarker<>(offset + (i + 1) * step, new LightFlash(100, new RGBLightValue(0, 0xff, 0xff))));
        }
        
        return new EventSheet<>(markers);
    }

}
