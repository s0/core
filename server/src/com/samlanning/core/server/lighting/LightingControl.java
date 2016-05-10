package com.samlanning.core.server.lighting;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;

import com.samlanning.core.server.util.Logging;

public class LightingControl {

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
        thread.interrupt();
    }

    public synchronized void setStaticBrightness(float brightness) {
        thread.staticBrightness = brightness;
        thread.interrupt();
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
        private long lastHostError;

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
                    // Just turn on (TODO: use music)
                    RGBLightValue musicColor = currentLightSetting.brightness(musicBrightness);
                    if (!musicColor.equals(currentLightValue)) {
                        transitionLight(musicColor);
                        continue;
                    }
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

        private void updateLight() {
            if (this.lightingOutputStream == null) {
                if (this.lastHostError > System.currentTimeMillis() - 5000) {
                    // Don't try and reconnect more than every 5 seconds
                    return;
                }
                try {
                    Socket clientSocket =
                        new Socket(LightingControl.this.host, LightingControl.this.port);
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

}
