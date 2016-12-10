package com.example.ala.utils;

import com.example.ala.config.ColorSpace;
import com.example.ala.config.Config;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public final class ColorUtil {
    public static Mat RGBtoYUV(Mat mat_rgb){
        Mat mat_yuv = new Mat();
        Imgproc.cvtColor(mat_rgb, mat_yuv, Imgproc.COLOR_RGB2YUV);
        return mat_yuv;
    }

    public static Mat YUVtoRGB(Mat mat_yuv){
        Mat mat_rgb = new Mat();
        Imgproc.cvtColor(mat_yuv, mat_rgb, Imgproc.COLOR_YUV2BGR);
        return mat_rgb;
    }

    public static double AverageForRect(Mat mat, Rect rect, int channel_id){
        Mat mat_region = mat.submat(rect);
        double value=0;
        value = Average(mat, channel_id);
        return value;
    }
    public static double Average(Mat mat, int channel_id){
        double sum=0, count=0;
        ArrayList<Mat> region_channels = new ArrayList<>();
        Core.split(mat, region_channels);


        for(int x=0; x<region_channels.get(channel_id).width(); x++){
            for(int y=0; y<region_channels.get(channel_id).height(); y++){
                double val = region_channels.get(channel_id).get(y, x)[0];
                if (isSkin(region_channels, x, y)){
                    sum += val;
                    count++;
                }
            }
        }
        return sum/count;
    }

    public static boolean isSkin(ArrayList<Mat> channels, int x, int y){
        double color1 = channels.get(0).get(y,x)[0],
               color2 = channels.get(1).get(y,x)[0],
               color3 = channels.get(2).get(y,x)[0];
        double minColor = Math.min(Math.min(color1, color2), Math.min(color2, color3));
        double maxColor = Math.max(Math.max(color1, color2), Math.max(color2, color3));

        if (Config.GetColorSpace() == ColorSpace.RGB) {
//            if (maxColor == color3 && minColor == color1)
            if (maxColor-minColor>50)
            {
                return true;
            }
        }
        else if(Config.GetColorSpace() == ColorSpace.YUV){
            if (maxColor == color1 && minColor == color3)
            {
                return true;
            }
        }
        return false;
    }
}
