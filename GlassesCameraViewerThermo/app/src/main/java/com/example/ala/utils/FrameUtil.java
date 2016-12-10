package com.example.ala.utils;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Created by ala on 16/07/16.
 */
public final class FrameUtil {

    public static Bitmap matToBitmap(Mat image) {
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(image, bmp);
        }
        catch (CvException e){
            Log.d("Exception",e.getMessage());}
        return bmp;
    }

    public static ArrayList<Mat> addToBuffer(Mat image, ArrayList<Mat> buffer){
        buffer.add(image);
        return buffer;
    }
}
