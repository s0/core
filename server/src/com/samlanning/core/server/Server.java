package com.samlanning.core.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import org.bff.javampd.MPD;
import org.bff.javampd.StandAloneMonitor;
import org.bff.javampd.Player.Status;
import org.bff.javampd.events.PlayerBasicChangeEvent;
import org.bff.javampd.events.PlayerBasicChangeListener;
import org.bff.javampd.exception.MPDConnectionException;
import org.bff.javampd.monitor.MPDPlayerMonitor;
import org.slf4j.LoggerFactory;

import com.samlanning.core.server.mpd.MPDMonitor;
import com.samlanning.core.server.util.Logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Server {
    
    private static final org.slf4j.Logger logger = Logging.logger(Server.class);
    
    private static final File CONFIG_FILE = new File("config.yaml");

    public static void main(String[] args) throws MPDConnectionException, UnknownHostException {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        
        // Load Config
        try(FileInputStream is = new FileInputStream(CONFIG_FILE)){
            
        } catch (FileNotFoundException e) {
            logger.error("Config File Not Found: " + CONFIG_FILE);
            return;
        } catch (IOException e) {
            logger.error("Error loading config file", e);
            return;
        }

        MPD mpd = new MPD.Builder().server("quorra.sparknet").build();

        MPDMonitor monitor = new MPDMonitor(mpd);
        monitor.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        monitor.addListener(new MPDMonitor.Listener() {

            @Override
            public void statusChanged(Status status) {
                System.out.println(status);
            }
        });

    }

}
