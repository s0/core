package com.samlanning.core.server.config;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.samlanning.core.server.lighting.RGBLightValue;

public class ServerConfig {

    private final MPDConfig mpd;

    private final LightingConfig lighting;

    private final HueConfig hue;

    private final String websocketHost;
    private final int websocketPort;

    private final Map<String, String> commandLineActions;

    public static class LightingConfig {
        public final String host;
        public final int port;
        public final RGBLightValue defaultColor;

        public LightingConfig(String host, int port, RGBLightValue defaultColor) {
            this.host = host;
            this.port = port;
            this.defaultColor = defaultColor;
        }
    }

    public static class MPDConfig {
        public final String host;
        public final int port;

        public MPDConfig(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    public static class HueConfig {
        public final String host;
        public final String username;
        /**
         * If set, use this light to pick lighting
         */
        public final String watchLight;

        public HueConfig(String host, String username, String watchLight) {
            this.host = host;
            this.username = username;
            this.watchLight = watchLight;
        }
    }

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

            Map<?, ?> mpdConfig = getMap(configRoot, "mpd", false);
            if (mpdConfig != null) {
                String host = getRequiredString(mpdConfig, "host", "mpd host");
                int port = getPort(mpdConfig, "port", -1, "mpd port");
                this.mpd = new MPDConfig(host, port);
            } else {
                this.mpd = null;
            }
        }

        // Lighting Config
        {

            Map<?, ?> lightingConfig = getMap(configRoot, "lighting", false);
            if (lightingConfig != null) {
                String host = getRequiredString(lightingConfig, "host", "lighting host");
                int port = getPort(lightingConfig, "port", -1, "lighting port");
                RGBLightValue defaultColor = RGBLightValue.fromString(
                    getRequiredString(lightingConfig, "defaultColor", "lighting port"));
                this.lighting = new LightingConfig(host, port, defaultColor);
            } else {
                this.lighting = null;
            }
        }

        // Hue Config
        {
            Map<?, ?> hueConfig = getMap(configRoot, "hue", false);
            if (hueConfig != null) {
                String host = getRequiredString(hueConfig, "host", "hue host");
                String username = getRequiredString(hueConfig, "username", "hue username");
                String watchLight = getString(hueConfig, "watchLight", "hue watchLight", false);
                this.hue = new HueConfig(host, username, watchLight);
            } else {
                this.hue = null;
            }
        }

        // WebSocket Config
        {
            Map<?, ?> websocketConfig = getMap(configRoot, "websocket", true);
            this.websocketHost = getRequiredString(websocketConfig, "host", "websocket host");
            this.websocketPort = getPort(websocketConfig, "port", null, "websocket port");
        }

        // Command-Line Actions Config
        {
            Map<String, String> commandLineActions = new LinkedHashMap<>();
            Map<?, ?> actionsConfig = getMap(configRoot, "actions", false);
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

    private Map<?, ?> getMap(Map<?, ?> map, String key, boolean required)
        throws ConfigurationException {
        Object object = map.get(key);
        if (object == null) {
            if (required)
                throw new ConfigurationException("missing required config: " + key);
            else
                return null;
        } else if (object instanceof Map) {
            return (Map<?, ?>) object;
        } else {
            throw new ConfigurationException(key + " is not a map");
        }
    }

    private static String getRequiredString(Map<?, ?> map, String key, String configIdentifier)
        throws ConfigurationException {
        return getString(map, key, configIdentifier, true);
    }

    private static String getString(Map<?, ?> map, String key, String configIdentifier, boolean required)
        throws ConfigurationException {
        Object object = map.get(key);
        if (object == null) {
            if (required)
                throw new ConfigurationException("missing required config: " + configIdentifier);
            else
                return null;
        } else {
            return object.toString();
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

    public MPDConfig mpd() {
        return mpd;
    }

    public LightingConfig lighting() {
        return lighting;
    }

    public HueConfig hue() {
        return hue;
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
