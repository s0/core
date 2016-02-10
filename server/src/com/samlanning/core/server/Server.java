package com.samlanning.core.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import org.bff.javampd.MPD;
import org.bff.javampd.Player.Status;
import org.bff.javampd.exception.MPDConnectionException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.samlanning.core.server.client_protocol.transports.WebSocketTransport;
import com.samlanning.core.server.config.ConfigurationException;
import com.samlanning.core.server.config.ServerConfig;
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
            MPD.Builder builder = new MPD.Builder();
            builder.server(config.mpdHost());
            if (config.mpdPort() > 0) {
                builder.port(config.mpdPort());
            }

            MPDMonitor monitor = new MPDMonitor(builder.build());
            monitor.start();
            
            switchboard.addMPDMonitor(monitor);
        }

        // Setup Websocket
        {
            WebSocketTransport webSocket =
                new WebSocketTransport(switchboard, config.websocketHost(), config.websocketPort());
            webSocket.start();
        }

    }

}
