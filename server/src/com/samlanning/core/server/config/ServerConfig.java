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
                this.mpdHost = getRequiredString(mpdConfig, "host", "mpd host");
                this.mpdPort = getPort(mpdConfig, "port", -1, "mpd port");
            } else {
                throw new ConfigurationException("mpd is not a map");
            }
        }
    }

    private static String getRequiredString(Map<?, ?> map, String key, String configIdentifier)
        throws ConfigurationException {
        Object object = map.get(key);
        if (object instanceof String) {
            return (String) object;
        } else {
            throw new ConfigurationException("invalid " + configIdentifier);
        }
    }

    private static int getPort(Map<?, ?> map, String key, Integer defaultValue,
        String configIdentifier) throws ConfigurationException {
        Object object = map.get(key);
        if (object != null) {
            if (object instanceof Number) {
                int portInt = ((Number) object).intValue();
                if (portInt > 0) {
                    return portInt;
                } else {
                    throw new ConfigurationException(configIdentifier + " must be greater than 0");
                }
            } else {
                throw new ConfigurationException(configIdentifier + " must be a number");
            }
        } else if (defaultValue != null) {
            return defaultValue.intValue();
        } else {
            throw new ConfigurationException("missing required " + configIdentifier);
        }

    }

    public String mpdHost() {
        return mpdHost;
    }

    public int mpdPort() {
        return mpdPort;
    }

    public String websocketHost() {
        // TODO Auto-generated method stub
        return null;
    }

    public int websocketPort() {
        // TODO Auto-generated method stub
        return 0;
    }

}
