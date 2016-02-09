package com.samlanning.core.server.config;

import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class ServerConfig {

    public ServerConfig(InputStream configFile) throws ConfigurationException {
        Yaml yaml = new Yaml();
        try {
            Object data = yaml.load(configFile);
        } catch (YAMLException e) {
            throw new ConfigurationException("Unable to load configuration file", e);
        }
    }

}
