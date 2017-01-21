package com.samlanning.core.server.lighting;

import java.awt.Color;

public class RGBLightValue {

    public final int red;
    public final int green;
    public final int blue;

    public RGBLightValue(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public RGBLightValue brightness(float brightness) {
        return new RGBLightValue(Math.round(Math.max(0, Math.min(255, red * brightness))),
            Math.round(Math.max(0, Math.min(255, green * brightness))), Math.round(Math.max(0,
                Math.min(255, blue * brightness))));
    }

    public RGBLightValue transition(RGBLightValue newValue, float amnt) {
        return new RGBLightValue((int) Math.round(newValue.red * amnt + red * (1.0 - amnt)),
            (int) Math.round(newValue.green * amnt + green * (1.0 - amnt)),
            (int) Math.round(newValue.blue * amnt + blue * (1.0 - amnt)));
    }

    @Override
    public String toString() {
        return "RGBLightValue(#" + toHexString() + ")";
    }

    public static RGBLightValue fromString(String colorString) throws IllegalArgumentException {
        if (colorString.length() != 6) {
            throw new IllegalArgumentException("Not a hexadecimal colour");
        }
        try {
            return new RGBLightValue(Integer.parseInt(colorString.substring(0, 2), 16),
                Integer.parseInt(colorString.substring(2, 4), 16), Integer.parseInt(
                    colorString.substring(4, 6), 16));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a hexadecimal colour");
        }
    }
    
    public static RGBLightValue fromHSB(float hue, float saturation, float brightness) {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb >> 0) & 0xff;
        return new RGBLightValue(r, g, b);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + blue;
        result = prime * result + green;
        result = prime * result + red;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RGBLightValue other = (RGBLightValue) obj;
        if (blue != other.blue)
            return false;
        if (green != other.green)
            return false;
        if (red != other.red)
            return false;
        return true;
    }

    public String toHexString() {
        return String.format("%02x%02x%02x", this.red, this.green, this.blue);
    }

}
