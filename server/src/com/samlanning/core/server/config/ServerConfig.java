package com.samlanning.core.server.config;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.samlanning.core.server.lighting.RGBLightValue;

public class ServerConfig {

    private final String mpdHost;
    private final int mpdPort;

    private final String lightingHost;
    private final int lightingPort;
    private final RGBLightValue lightingDefaultColor;

    private final String websocketHost;
    private final int websocketPort;

    private final Map<String, String> commandLineActions;

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

            Map<?, ?> mpdConfig = getMap(configRoot, "mpd", true, "mpd");
            this.mpdHost = getRequiredString(mpdConfig, "host", "mpd host");
            this.mpdPort = getPort(mpdConfig, "port", -1, "mpd port");
        }

        // Lighting Config
        {

            Map<?, ?> lightingConfig = getMap(configRoot, "lighting", true, "lighting");
            this.lightingHost = getRequiredString(lightingConfig, "host", "lighting host");
            this.lightingPort = getPort(lightingConfig, "port", -1, "lighting port");
            this.lightingDefaultColor = RGBLightValue.fromString(
                getRequiredString(lightingConfig, "defaultColor", "lighting port"));
        }

        // WebSocket Config
        {
            Map<?, ?> websocketConfig = getMap(configRoot, "websocket", true, "websocket");
            this.websocketHost = getRequiredString(websocketConfig, "host", "websocket host");
            this.websocketPort = getPort(websocketConfig, "port", null, "websocket port");
        }

        // Command-Line Actions Config
        {
            Map<String, String> commandLineActions = new LinkedHashMap<>();
            Map<?, ?> actionsConfig = getMap(configRoot, "actions", false, "actions");
            if (actionsConfig != null) {
                for (Map.Entry<?, ?> entry : actionsConfig.entrySet()) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    if (key instanceof String && value instanceof String) {
                        commandLineActions.put((String) key, (String) value);
                    } else {
                        throw new ConfigurationException(
                            "Invalid action, they must all be strings: " + key);
                    }
                }
            }
            this.commandLineActions = Collections.unmodifiableMap(commandLineActions);
        }
    }

    private Map<?, ?> getMap(Map<?, ?> map, String key, boolean required, String configIdentifier)
        throws ConfigurationException {
        Object object = map.get(key);
        if (object == null) {
            if (required)
                throw new ConfigurationException("missing required config: " + configIdentifier);
            else
                return null;
        } else if (object instanceof Map) {
            return (Map<?, ?>) object;
        } else {
            throw new ConfigurationException(configIdentifier + " is not a map");
        }
    }

    private static String getRequiredString(Map<?, ?> map, String key, String configIdentifier)
        throws ConfigurationException {
        Object object = map.get(key);
        if (object == null) {
            throw new ConfigurationException("missing required config: " + configIdentifier);
        } else if (object instanceof String) {
            return (String) object;
        } else {
            throw new ConfigurationException("invalid " + configIdentifier);
        }
    }

    /**
     * @param defaultValue - null if config is required, otherwise the value to
     *            default to.
     */
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
            throw new ConfigurationException("missing required config: " + configIdentifier);
        }

    }

    public String mpdHost() {
        return mpdHost;
    }

    public int mpdPort() {
        return mpdPort;
    }

    public String lightingHost() {
        return lightingHost;
    }

    public int lightingPort() {
        return lightingPort;
    }

    public RGBLightValue lightingDefaultColor() {
        return lightingDefaultColor;
    }

    public String websocketHost() {
        return websocketHost;
    }

    public int websocketPort() {
        return websocketPort;
    }
    
    public Map<String, String> commandLineAction(){
        return commandLineActions;
    }

}
