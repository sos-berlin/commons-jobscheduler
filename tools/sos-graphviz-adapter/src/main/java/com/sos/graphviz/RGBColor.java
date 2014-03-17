package com.sos.graphviz;

/**
 * Class to convert the hex representation of a color to the red, green and blue part and vice versa.
 *
 * @uthor Stefan Schaedlich
 * at 25.09.13 15:44
 */
public class RGBColor {

    private final Integer red;
    private final Integer green;
    private final Integer blue;
    private final String hexString;

    public RGBColor(String hexString) {
        Integer bigint = Integer.parseInt(hexString.substring(1), 16);
        this.red = (bigint >> 16) & 255;
        this.green = (bigint >> 8) & 255;
        this.blue = bigint & 255;
        this.hexString = hexString;
    }

    public RGBColor(Integer red, Integer green, Integer blue) {
        this.hexString = "#" + componentToHex(red) + componentToHex(green) + componentToHex(blue);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    private String componentToHex(Integer c) {
        String hex = c.toString(16);
        return (hex.length() == 1) ? "0" + hex : hex;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public String getHexString() {
        return hexString;
    }

}
