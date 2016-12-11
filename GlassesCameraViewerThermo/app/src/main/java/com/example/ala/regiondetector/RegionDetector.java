package com.example.ala.regiondetector;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.ala.config.ColorSpace;
import com.example.ala.config.Config;
import com.example.ala.eventaggregator.Event;
import com.example.ala.eventaggregator.EventAggregator;
import com.example.ala.eventaggregator.EventListener;
import com.example.ala.utils.ColorUtil;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.List;

import pl.gda.pg.eti.dbe.dsp.FilterUsingCoeff;
import pl.gda.pg.eti.dbe.dsp.SignalParameters;
import pl.gda.pg.eti.dbe.dsp.Spectrum;
import pl.gda.pg.eti.dbe.dsp.TimeDomainFilters;

/**
 * Created by ala on 14/07/16.
 */
public class RegionDetector implements EventListener {
    private final EventAggregator eventAggregator;
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyeCascade;
    private ArrayList<Double> listAvg = new ArrayList<>();
    private ArrayList<Mat> imageBuffer = new ArrayList<>();

    public RegionDetector(EventAggregator eventAggregator) {
        this.eventAggregator = eventAggregator;
        eventAggregator.addEventListener(Event.NEW_IMAGE, this);
        eventAggregator.addEventListener(Event.VISIBLE_CASCADES, this);
        eventAggregator.addEventListener(Event.NEW_IMAGE_THERMO, this);
    }

    @Override
    public void onEventOccurred(Event event, Object parameter, Object parameter2) {

        if (event == Event.VISIBLE_CASCADES) {
            faceCascade = (CascadeClassifier) parameter;
            eyeCascade = (CascadeClassifier) parameter2;
        } else if (event == Event.NEW_IMAGE) {
            Mat mat = detectRegion((Mat) parameter, faceCascade, "face");
            mat = detectRegion(mat, eyeCascade, "eyes");
            if (Config.GetColorSpace() == ColorSpace.YUV) {
                mat = ColorUtil.RGBtoYUV(mat);
            }

            eventAggregator.triggerEvent(Event.PROCESSED_IMAGE, mat, null);
        } else if (event == Event.NEW_IMAGE_THERMO) {
            Bitmap bmp = (Bitmap) parameter;

            Mat mat = new Mat (bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
            Utils.bitmapToMat(bmp, mat);

           // mat = detectRegion(mat, faceCascade, "face");
           // mat = detectRegion(mat, eyeCascade, "eyes");

            Bitmap resultedBmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, resultedBmp);

            eventAggregator.triggerEvent(Event.PROCESSED_THERMO_IMAGE, resultedBmp, null);
        }

    }

    private Mat detectRegion(Mat mat, CascadeClassifier cascade, String region) {
        if (!mat.empty()) {
            Mat grayscale = mat.clone();
            Imgproc.cvtColor(mat, grayscale, Imgproc.COLOR_RGBA2RGB);

            MatOfRect regions = new MatOfRect();

            // Use the classifier to detect regions
            if (cascade != null) {
                cascade.detectMultiScale(grayscale, regions);
            }


            // If there are any regions found, draw a rectangle around it
            Rect[] objectArray = regions.toArray();
            if (objectArray.length > 0) {
                drawRect(mat, region, objectArray);
                Mat submat = mat.submat(objectArray[0]);
                imageBuffer.add(submat);
                ArrayList<Mat> region_channels = new ArrayList<>();
                Core.split(submat, region_channels);

//                for (int x = 0; x < region_channels.get(0).width(); x++) {
//                    for (int y = 0; y < region_channels.get(0).height(); y++) {
//                        if (ColorUtil.isSkin(region_channels, x, y)) {
//                            byte[] pixel = {0, 0, 0, 0};
//                            submat.put(y, x, pixel);
//                        }
//                    }
//                }
            }
            if (imageBuffer.size() > 30) {
                calculatePulse(imageBuffer);
                imageBuffer.clear();
            }
        }
        return mat;
    }

    private void drawRect(Mat mat, String region, Rect[] objectArray) {
        Rect rect = new Rect(objectArray[0].tl(), objectArray[0].br());
        double avg;

        if (region.equals("face")) {
            avg = ColorUtil.AverageForRect(mat, rect, Config.GetChannel());
            eventAggregator.triggerEvent(Event.TAIL_TO_DRAW, avg, null);
            eventAggregator.triggerEvent(Event.NEW_FACE_DETECTED, rect, null);
        } else if (region.equals("eyes")) {
            rect = new Rect(rect.x, rect.y - 2 * rect.height, rect.width, rect.height);
        }
        Imgproc.rectangle(mat, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 3);
    }

    private void calculatePulse(ArrayList<Mat> buffer){
        double fg=0.5;
        int poles=4;
        int fs=5;
        double eR_ac=0.0;
        double eR_as=0.0;
        double eR_pk=0.0;
        double [] s_moments=new double [3];
        double [] a_moments=new double [3];
        double [][] avgVector = new double[3][buffer.size()];

        for (int i=0; i<buffer.size(); i++){
            Mat m = buffer.get(i);
            double avgCh1 = ColorUtil.Average(m, 1);
            double avgCh2 = ColorUtil.Average(m, 2);
            double avgCh3 = ColorUtil.Average(m, 3);
            avgVector[0][i]=avgCh1;
            avgVector[1][i]=avgCh2;
            avgVector[2][i]=avgCh3;
        }

        Spectrum spectrum = new Spectrum(avgVector[0].clone(), fs, 2, avgVector[0].length);

        double eR_sp= SignalParameters.getPulse(spectrum.getFreqResponse(), fs, spectrum.getN());

        double[] y = new double[avgVector[0].length];
        y= FilterUsingCoeff.filter(avgVector[0], fs, fg, poles, false);
        System.arraycopy(y, 0, avgVector[0], 0, y.length);
        y=FilterUsingCoeff.filter(avgVector[1], fs, fg, poles, false);
        System.arraycopy(y, 0, avgVector[1], 0, y.length);
        y=FilterUsingCoeff.filter(avgVector[2], fs, fg, poles, false);
        System.arraycopy(y, 0, avgVector[2], 0, y.length);

        //fg=1.2;
        fg=4.0;
        y=FilterUsingCoeff.filter(avgVector[0], fs, fg, poles, true);
        System.arraycopy(y, 0, avgVector[0], 0, y.length);

        //CALCULATE SPECTRAL MOMENTS FOR SIGNAL 0 (should be for the chosen one)
        s_moments= TimeDomainFilters.calculateSpectralMoments(avgVector[0]);

        y=FilterUsingCoeff.filter(avgVector[1], fs, fg, poles, true);
        System.arraycopy(y, 0, avgVector[1], 0, y.length);
        y=FilterUsingCoeff.filter(avgVector[2], fs, fg, poles, true);

        //System.arraycopy(y, 0, avgVector[2], 0, y.length);
        //THIS IS FOR TEMPORARY TESTS ONLY - to show in the 3rd panel
        //the Autocorrelation function
        y=TimeDomainFilters.xcorr(y, y, y.length-1);
        System.arraycopy(y, y.length/4, avgVector[2], 0, avgVector[2].length);

        //CALCULATE SPECTRAL MOMENTS FOR THE AUTOCORRELATION SIGNAL
        a_moments=TimeDomainFilters.calculateSpectralMoments(avgVector[2]);

        //CALCULATE RATE USING SLOPES DETECTION RATE IN THE  AUTOCORRELATION SIGNAL
        double xcorrRate=TimeDomainFilters.simpleSlopeDetector3(avgVector[2], TimeDomainFilters.getMaxValue(avgVector[2]), TimeDomainFilters.getMinValue(avgVector[2]), 0.3);
        System.out.println("AUTO RATE: "+((1/(xcorrRate/fs))*60));

        eR_ac=((1/(xcorrRate/fs))*60);
        //CALCULATE RATE USING SLOPES DETECTION RATE IN THE  SIGNAL 0 (should be for the chosen one)
        double xcorrRate2=TimeDomainFilters.simpleSlopeDetector3(avgVector[0], TimeDomainFilters.getMaxValue(avgVector[0]), TimeDomainFilters.getMinValue(avgVector[0]), 0.3);
        eR_pk=((1/(xcorrRate2/fs))*60);
        System.out.println("aaaaaaaa"+eR_pk);
        eventAggregator.triggerEvent(Event.NEW_PULSE, eR_pk, null);

    }
}
