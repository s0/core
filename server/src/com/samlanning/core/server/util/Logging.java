package com.samlanning.core.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logging {

    public static Logger logger(Class<?> cls){
        return LoggerFactory.getLogger(cls.getCanonicalName());
    }
    
}
