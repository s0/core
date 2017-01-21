package com.samlanning.core.server.hue.api.objects;

public class HueLight {

    public HueLightState state;

    public String type;
    public String name;
    public String modelid;
    public String swversion;

    public static class HueLightState {
        public boolean on;
        /**
         * Brightness, 8-bit int: a value between 0-255
         */
        public int bri;
        /**
         * Hue, 16-bin int: a value between 0-65535
         */
        public int hue;
        /**
         * Saturation, 8-bin int: a value between 0-255
         */
        public int sat;
        // TODO: xy
        public int ct;
        public String alert;
        public String effect;
        public String colormode;
        public boolean reachable;
    }
    
}
