package com.example.ala.config;

/**
 * Created by ala on 18/07/16.
 */
public final class Config {
    static ColorSpace colorspace = ColorSpace.RGB;
    static int channel = 1;

    public static ColorSpace GetColorSpace(){
        return colorspace;
    }

    public static void SetColorSpace(ColorSpace colorspace){
        Config.colorspace = colorspace;
    }

    public static int GetChannel(){
        return channel;
    }

    public static void SetChannel(int channel){
        Config.channel = channel;
    }
}
