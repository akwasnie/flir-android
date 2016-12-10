package com.example.ala.camerahandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.ala.eventaggregator.Event;
import com.example.ala.eventaggregator.EventAggregator;
import com.example.ala.glassescameraviewer.FaceView;
import com.example.ala.glassescameraviewer.R;
import com.example.ala.regiondetector.RegionDetector;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by ala on 14/07/16.
 */
public class VisibleFrameCapture implements FrameCapture {

    private EventAggregator eventAggregator;

    public VisibleFrameCapture(EventAggregator eventAggregator) {
        this.eventAggregator = eventAggregator;
    }

    @Override
    public void captureFrame() {
        Thread t = new Thread() {
            @Override
            public void run() {

            }
        };
        t.start();

    }
}
