package com.samlanning.core.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import org.bff.javampd.server.MPD;
import org.bff.javampd.server.MPDConnectionException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.samlanning.core.server.behaviours.Behaviours;
import com.samlanning.core.server.client_protocol.transports.WebSocketTransport;
import com.samlanning.core.server.config.ConfigurationException;
import com.samlanning.core.server.config.ServerConfig;
import com.samlanning.core.server.lighting.LightingControl;
import com.samlanning.core.server.mpd.MPDMonitor;
import com.samlanning.core.server.switchboard.ServerSwitchboard;
import com.samlanning.core.server.util.Logging;

public class Server {

    private static final org.slf4j.Logger logger = Logging.logger(Server.class);

    private static final File CONFIG_FILE = new File("config.yaml");

    public static void main(String[] args) throws MPDConnectionException, UnknownHostException {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        // Load Config
        ServerConfig config;
        try (FileInputStream is = new FileInputStream(CONFIG_FILE)) {
            config = new ServerConfig(is);
        } catch (FileNotFoundException e) {
            logger.error("Config File Not Found: " + CONFIG_FILE);
            return;
        } catch (IOException e) {
            logger.error("Error loading config file", e);
            return;
        } catch (ConfigurationException e) {
            logger.error("Error in config", e);
            return;
        }

        ServerSwitchboard switchboard = new ServerSwitchboard(config);

        // Setup MPD
        {
            ServerConfig.MPDConfig mpdConfig = config.mpd();
            if (mpdConfig != null) {
                MPD.Builder builder = new MPD.Builder();
                builder.server(mpdConfig.host);
                if (mpdConfig.port > 0) {
                    builder.port(mpdConfig.port);
                }

                MPDMonitor monitor = new MPDMonitor(builder.build());
                monitor.start();

                switchboard.addMPDMonitor(monitor);
            }
        }

        // Setup Lighting
        {
            ServerConfig.LightingConfig lightingConfig = config.lighting();
            if (lightingConfig != null) {
                LightingControl lighting =
                    new LightingControl(lightingConfig.host, lightingConfig.port);
                lighting.start();
                lighting.setColor(lightingConfig.defaultColor);

                switchboard.addLightingControl(lighting);
            }
            
            // Lighting Test
//            new Thread(){
//                public void run() {
//                    lighting.setStaticBrightness(1);
//                    try {
//                        while (true) {
//                            lighting.setColor(new RGBLightValue(0, 255, 255));
//                            Thread.sleep(2000);
//                            lighting.setColor(new RGBLightValue(0, 0, 255));
//                            Thread.sleep(2000);
//                            lighting.setColor(new RGBLightValue(255, 0, 0));
//                            Thread.sleep(2000);
//                        }
//                    } catch (InterruptedException e) {
//                    }
//                }
//            }.start();
//            new Thread(){
//                public void run() {
//                    lighting.setStaticBrightness(1);
//                    try {
//                        while (true) {
//                            Thread.sleep(1000);
//                            lighting.setStaticBrightness(0f);
//                            Thread.sleep(1000);
//                            lighting.setStaticBrightness(1f);
//                        }
//                    } catch (InterruptedException e) {
//                    }
//                }
//            }.start();
        }

        // Setup Websocket
        {
            WebSocketTransport webSocket =
                new WebSocketTransport(switchboard, config.websocketHost(), config.websocketPort());
            webSocket.start();
        }

        Behaviours.initialise(switchboard);

    }

}
