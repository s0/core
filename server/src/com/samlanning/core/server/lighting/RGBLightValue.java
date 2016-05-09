package com.samlanning.core.server.lighting;

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
        return String.format("r:%x g:%x b:%x", this.red, this.green, this.blue);
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

}
