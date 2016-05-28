package com.samlanning.core.server.lighting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;

import com.samlanning.core.server.mpd.MPDMonitor;
import com.samlanning.core.server.util.InterruptableBufferedInputStreamWrapper;
import com.samlanning.core.server.util.Listenable;
import com.samlanning.core.server.util.Logging;

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
        thread.currentLightSetting = color;
        thread.doInterrupt();
    }

    public synchronized void setStaticBrightness(float brightness) {
        thread.staticBrightness = brightness;
        thread.doInterrupt();
    }

    public synchronized void setStateStatic(){
        log.info("setting state static");
        thread.state = LightState.STATIC;
        thread.doInterrupt();
    }

    public synchronized void setStatePlayingMusic(){
        thread.state = LightState.MUSIC;
        thread.doInterrupt();
    }
    
    private enum LightState {
        STATIC,
        MUSIC
    }

    private class LightingThread extends Thread {

        private RGBLightValue currentLightSetting = new RGBLightValue(0, 0, 0); 
        private RGBLightValue currentLightValue = new RGBLightValue(0, 0, 0);
        private LightState state = LightState.STATIC;
        private float staticBrightness = 0.0f;
        private float musicBrightness = 1.0f;

        private OutputStream lightingOutputStream;
        private InterruptableBufferedInputStreamWrapper bisw;
        private long lastHostError;
        
        private void doInterrupt(){
            this.interrupt();
            if (bisw != null) {
                bisw.interrupt();
                bisw = null;
            }
        }

        @Override
        public void run() {
            updateLight();
            while (true) {
                switch(state) {
                case STATIC:
                    RGBLightValue staticColor = currentLightSetting.brightness(staticBrightness);
                    // Colour May have changed, if so, transition
                    if (!staticColor.equals(currentLightValue)) {
                        transitionLight(staticColor);
                        continue;
                    }
                    break;
                case MUSIC:
                    linkLightToMusic(currentLightSetting.brightness(musicBrightness));
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

        private void transitionLight(RGBLightValue desiredColour) {
            RGBLightValue old = this.currentLightValue;
            for (int i = 0; i <= 100; i++) {
                this.currentLightValue = old.transition(desiredColour, i / 100f);
                updateLight();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // If interrupted, need to cancel operation
                    return;
                }
            }
            this.currentLightValue = desiredColour;
            updateLight();
        }

        private void linkLightToMusic(RGBLightValue colour) {
            try(FileInputStream is = new FileInputStream("/run/mpd/mpd.fifo")){
                bisw = new InterruptableBufferedInputStreamWrapper(is, 4);
                log.info("Opened fifo");
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
                    count ++;
                    if (count >= 3000){
                        float brightness = Math.max(maxRight, maxLeft) / (float) Short.MAX_VALUE;
                        currentLightValue = colour.brightness(brightness);
                        updateLight();
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
                // IOException may be caused by closing the fifo due to a different interrupt
                log.info("Stopped using fifo");
            }
        }

        private void updateLight() {
            LightingControl.this.updateNewListenerVisitor(l -> l.newLightValue(currentLightValue));
            LightingControl.this.visitListeners(l -> l.newLightValue(currentLightValue));
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
                lightingOutputStream.write(new byte[] { (byte) currentLightValue.red,
                    (byte) currentLightValue.green, (byte) currentLightValue.blue, (byte) 255 });
                lightingOutputStream.flush();
            } catch (IOException e) {
                log.warn("Error updating light, resetting");
                this.lightingOutputStream = null;
            }
        }

    }

    public interface Listener {
        public void newLightValue(RGBLightValue light);
    }

}
