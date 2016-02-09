package com.samlanning.core.server.config;

public class ConfigurationException extends Exception {

    public ConfigurationException(String message, Throwable e) {
        super(message, e);
    }

    public ConfigurationException(String message) {
        super(message);
    }

}
