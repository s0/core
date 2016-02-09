package com.samlanning.core.server.config;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class ServerConfig {

    private final String mpdHost;
    private final int mpdPort;

    public ServerConfig(InputStream configFile) throws ConfigurationException {
        Yaml yaml = new Yaml();
        Object configData;
        try {
            configData = yaml.load(configFile);
        } catch (YAMLException e) {
            throw new ConfigurationException("Unable to load configuration file", e);
        }

        Map<?, ?> configRoot;
        if (configData instanceof Map) {
            configRoot = (Map<?, ?>) configData;
        } else {
            throw new ConfigurationException("Root is not a map");
        }

        // MPD Config
        {
            Object mpd = configRoot.get("mpd");
            if (mpd instanceof Map) {
                Map<?, ?> mpdConfig = (Map<?, ?>) mpd;
                Object mpdHost = mpdConfig.get("host");
                Object mpdPort = mpdConfig.get("port");
                if (mpdHost instanceof String) {
                    this.mpdHost = (String) mpdHost;
                } else {
                    throw new ConfigurationException("mpd host invalid");
                }
                if (mpdPort != null) {
                    if (mpdPort instanceof Number) {
                        int mpdPortInt = ((Number) mpdPort).intValue();
                        if (mpdPortInt > 0) {
                            this.mpdPort = mpdPortInt;
                        } else {
                            throw new ConfigurationException("mpd port must be greater than 0");
                        }
                    } else {
                        throw new ConfigurationException("mpd port must be a number");
                    }
                } else {
                    this.mpdPort = -1;
                }
            } else {
                throw new ConfigurationException("mpd is not a map");
            }
        }
    }

    public String mpdHost() {
        return mpdHost;
    }

    public int mpdPort() {
        return mpdPort;
    }

}
