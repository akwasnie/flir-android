package com.example.ala.glassescameraviewer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ala.config.ColorSpace;
import com.example.ala.config.Config;
import com.example.ala.config.ConfigActivity;
import com.example.ala.eventaggregator.Event;
import com.example.ala.eventaggregator.EventAggregator;
import com.example.ala.eventaggregator.EventListener;
import com.example.ala.regiondetector.RegionDetector;
import com.example.ala.utils.ColorUtil;
import com.example.ala.utils.FrameUtil;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.flir.flironesdk.Device;
import com.flir.flironesdk.FlirUsbDevice;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.LoadedFrame;
import com.flir.flironesdk.RenderedImage;
import com.flir.flironesdk.SimulatedDevice;


public class MainActivity extends AppCompatActivity implements EventListener, CameraBridgeViewBase.CvCameraViewListener, Device.Delegate, FrameProcessor.Delegate, Device.StreamDelegate, Device.PowerUpdateDelegate{

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;
    // Flir One.
    ImageView thermalImageView;
    private FaceView mFaceView;
    private GraphView graph;
    private TextView textView;
    private TextView textPulse;
    private CameraBridgeViewBase mOpenCvCameraView;
    private RegionDetector regionDetector;
    private Button buttonRGB;
    private Button buttonYUV;
    private Button buttonch1;
    private Button buttonch2;
    private Button buttonch3;
    private Button buttonConfig;
    private EventAggregator eventAggregator;
    private CascadeClassifier faceCascadeClassifier;
    private CascadeClassifier eyeCascadeClassifier;
    public BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    loadCascades();
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setAlpha(0);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private double heightProportion = 0.6;
    private double widthProportion = 0.3;
    private String tooFarMsg = "Move closer";
    private int lastX = 0;
    private LineGraphSeries<DataPoint> series;
    private volatile boolean imageCaptureRequested = false;
    private volatile Socket streamSocket = null;
    private boolean chargeCableIsConnected = true;
    private int deviceRotation= 0;
    private OrientationEventListener orientationEventListener;
    private volatile Device flirOneDevice;
    private FrameProcessor frameProcessor;
    private String lastSavedPath;
    private Device.TuningState currentTuningState = Device.TuningState.Unknown;
    private Bitmap thermo_frame;
    private Bitmap thermalBitmap = null;
    private ColorFilter originalChargingIndicatorColor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mFaceView = (FaceView) findViewById( R.id.face_overlay );
        textView = (TextView) findViewById(R.id.textView);
        textView.setText(tooFarMsg);
        textPulse = (TextView) findViewById(R.id.textPulse);
        textPulse.setText("not known");

        buttonRGB = (Button) findViewById(R.id.button);
        buttonRGB.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Config.SetColorSpace(ColorSpace.RGB);
            }
        });

        buttonYUV = (Button) findViewById(R.id.button2);
        buttonYUV.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Config.SetColorSpace(ColorSpace.YUV);
            }
        });

        buttonch1 = (Button) findViewById(R.id.button3);
        buttonch1.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Config.SetChannel(1);
            }
        });
        buttonch2 = (Button) findViewById(R.id.button4);
        buttonch2.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Config.SetChannel(2);
            }
        });
        buttonch3 = (Button) findViewById(R.id.button5);
        buttonch3.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Config.SetChannel(3);
            }
        });

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        eventAggregator = new EventAggregator();
        eventAggregator.addEventListener(Event.PROCESSED_IMAGE, this);
        eventAggregator.addEventListener(Event.NEW_FACE_DETECTED, this);
        eventAggregator.addEventListener(Event.TAIL_TO_DRAW, this);
        eventAggregator.addEventListener(Event.NEW_PULSE, this);
        eventAggregator.addEventListener(Event.PROCESSED_THERMO_IMAGE, this);

        regionDetector = new RegionDetector(eventAggregator);


        graph = (GraphView) findViewById(R.id.graphView1);
        series = new LineGraphSeries<>(new DataPoint[] {});
        graph.addSeries(series);
        Viewport viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(60);
        viewport.setMinY(0);
        viewport.setMaxY(300);
        viewport.setScrollable(true);

        buttonConfig = (Button) findViewById(R.id.config);








        HashMap<Integer, String> imageTypeNames = new HashMap<>();
        // Massage the type names for display purposes and skip any deprecated
        for (Field field : RenderedImage.ImageType.class.getDeclaredFields()){
            if (field.isEnumConstant() && !field.isAnnotationPresent(Deprecated.class)) {
                RenderedImage.ImageType t = RenderedImage.ImageType.valueOf(field.getName());
                String name = t.name().replaceAll("(RGBA)|(YCbCr)|(8)","").replaceAll("([a-z])([A-Z])", "$1 $2");
                imageTypeNames.put(t.ordinal(), name);
            }
        }
        String[] imageTypeNameValues = new String[imageTypeNames.size()];
        for (Map.Entry<Integer, String> mapEntry : imageTypeNames.entrySet()) {
            int index = mapEntry.getKey();
            imageTypeNameValues[index] = mapEntry.getValue();
        }

        RenderedImage.ImageType defaultImageType = RenderedImage.ImageType.BlendedMSXRGBA8888Image;
        frameProcessor = new FrameProcessor(this, this, EnumSet.of(defaultImageType, RenderedImage.ImageType.ThermalRadiometricKelvinImage));




        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceRotation = (orientation);
            }
        };


    }

    private void loadCascades() {
        faceCascadeClassifier = initializeOpenCVDependencies(R.raw.lbpcascade_frontalface, "lbpcascade_frontalface.xml");
        eyeCascadeClassifier = initializeOpenCVDependencies(R.raw.haarcascade_mcs_eyepair_big, "haarcascade_mcs_eyepair_big.xml");
        eventAggregator.triggerEvent(Event.VISIBLE_CASCADES, faceCascadeClassifier, eyeCascadeClassifier);

    }

    private CascadeClassifier initializeOpenCVDependencies(int i, String name) {
        CascadeClassifier cascadeClassifier = null;
        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(i);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, name);
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            cascadeClassifier.load(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }
        return cascadeClassifier;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    @Override
    public void onEventOccurred(Event event, final Object parameter, Object parameter2) {
       if(event==Event.PROCESSED_IMAGE){
           final Bitmap bitmap = FrameUtil.matToBitmap((Mat)parameter);
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   mFaceView.setBitmap(bitmap);
                   mFaceView.invalidate();
               }
           });
        }
        else if(event==Event.NEW_FACE_DETECTED){
           setDistanceMsg((Rect) parameter);
       }
        else if (event==Event.TAIL_TO_DRAW){
           drawGraph((double)parameter);
       }
        else if(event==Event.NEW_PULSE){
           setPulseMsg((Double)parameter);
       }
       else if(event==Event.PROCESSED_THERMO_IMAGE){
           thermo_frame = (Bitmap)parameter;
       }

    }

    private void drawGraph(final double colorValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    DataPoint dp = new DataPoint(lastX, colorValue);
                    series.appendData(dp, true, 60);
                lastX ++;
                graph.addSeries(series);
            }
        });
    }

    private void setDistanceMsg(Rect parameter) {
        final Rect faceRect = parameter;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(faceRect.size().height/(double)mFaceView.getHeight() < heightProportion &&
                        faceRect.size().width/(double)mFaceView.getWidth() < widthProportion) {
                    textView.setText(tooFarMsg);
                    mFaceView.setColor(Color.RED);
                }
                else {
                    textView.setText("");
                    mFaceView.setColor(Color.BLUE);
                }
            }
        });
    }

    private void setPulseMsg(double pulse) {
        int pulseInt = (int) pulse;
        final String pulseMsg = String.valueOf(pulseInt);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
textPulse.setText(pulseMsg);
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        eventAggregator.triggerEvent(Event.NEW_IMAGE, inputFrame, null);
        return inputFrame;
    }

    /** Called when the user clicks the Send button */
    public void openConfig(View view) {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);
    }

    public void onTuningStateChanged(Device.TuningState tuningState){
        Log.i("ExampleApp", "Tuning state changed changed!");

        currentTuningState = tuningState;
        if (tuningState == Device.TuningState.InProgress){
            runOnUiThread(new Thread(){
                @Override
                public void run() {
                    super.run();
                    thermalImageView.setColorFilter(Color.DKGRAY, PorterDuff.Mode.DARKEN);

                }
            });
        }else {
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    super.run();
                    thermalImageView.clearColorFilter();

                }
            });
        }
    }

    @Override
    public void onAutomaticTuningChanged(boolean b) {

    }

    public void onDeviceConnected(Device device){
        Log.i("ExampleApp", "Device connected!");

        flirOneDevice = device;
        flirOneDevice.setPowerUpdateDelegate(this);
        flirOneDevice.startFrameStream(this);
        orientationEventListener.enable();
    }

    public void onDeviceDisconnected(Device device){
        Log.i("ExampleApp", "Device disconnected!");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                thermalImageView.setImageBitmap(Bitmap.createBitmap(1,1, Bitmap.Config.ALPHA_8));

                thermalImageView.clearColorFilter();

            }
        });

        flirOneDevice = null;
        orientationEventListener.disable();
    }

    private void updateThermalImageView(final Bitmap frame){
        eventAggregator.triggerEvent(Event.NEW_IMAGE_THERMO, frame, null);
        final Bitmap imageToPresent = thermo_frame;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thermalImageView.setRotation(-90);
                thermalImageView.setImageBitmap(imageToPresent);
            }
        });
    }

    // StreamDelegate method
    public void onFrameReceived(Frame frame){
        Log.v("ExampleApp", "Frame received!");
        if (currentTuningState != Device.TuningState.InProgress){
            frameProcessor.processFrame(frame);
        }
    }

    // Frame Processor Delegate method, will be called each time a rendered frame is produced
    public void onFrameProcessed(final RenderedImage renderedImage) {
        if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {
            // Note: this code is not optimized

            int[] thermalPixels = renderedImage.thermalPixelValues();
            // average the center 9 pixels for the spot meter

            int width = renderedImage.width();
            int height = renderedImage.height();
            int centerPixelIndex = width * (height / 2) + (width / 2);
            int[] centerPixelIndexes = new int[]{
                    centerPixelIndex, centerPixelIndex - 1, centerPixelIndex + 1,
                    centerPixelIndex - width,
                    centerPixelIndex - width - 1,
                    centerPixelIndex - width + 1,
                    centerPixelIndex + width,
                    centerPixelIndex + width - 1,
                    centerPixelIndex + width + 1
            };

            double averageTemp = 0;

            for (int i = 0; i < centerPixelIndexes.length; i++) {
                // Remember: all primitives are signed, we want the unsigned value,
                // we've used renderedImage.thermalPixelValues() to get unsigned values
                int pixelValue = (thermalPixels[centerPixelIndexes[i]]);
                averageTemp += (((double) pixelValue) - averageTemp) / ((double) i + 1);
            }
            double averageC = (averageTemp / 100) - 273.15;
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setMinimumFractionDigits(2);

            // if radiometric is the only type, also show the image
            if (frameProcessor.getImageTypes().size() == 1) {
                // example of a custom colorization, maps temperatures 0-100C to 8-bit gray-scale
                byte[] argbPixels = new byte[width * height * 4];
                final byte aPixValue = (byte) 255;
                for (int p = 0; p < thermalPixels.length; p++) {
                    int destP = p * 4;
                    byte pixValue = (byte) (Math.min(0xff, Math.max(0x00, (thermalPixels[p] - 27315) * (255.0 / 10000.0))));

                    argbPixels[destP + 3] = aPixValue;
                    // red pixel
                    argbPixels[destP] = argbPixels[destP + 1] = argbPixels[destP + 2] = pixValue;
                }
                final Bitmap demoBitmap = Bitmap.createBitmap(width, renderedImage.height(), Bitmap.Config.ARGB_8888);

                demoBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbPixels));

                updateThermalImageView(demoBitmap);
            }
        } else {
            if (thermalBitmap == null) {
                thermalBitmap = renderedImage.getBitmap();
            } else {
                try {
                    renderedImage.copyToBitmap(thermalBitmap);
                } catch (IllegalArgumentException e) {
                    thermalBitmap = renderedImage.getBitmap();
                }
            }
            updateThermalImageView(thermalBitmap);
        }

        /*
        Capture this image if requested.
        */
        if (this.imageCaptureRequested) {
            imageCaptureRequested = false;
            final Context context = this;
            new Thread(new Runnable() {
                public void run() {
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
                    String formatedDate = sdf.format(new Date());
                    String fileName = "FLIROne-" + formatedDate + ".jpg";
                    try {
                        lastSavedPath = path + "/" + fileName;
                        renderedImage.getFrame().save(new File(lastSavedPath), RenderedImage.Palette.Iron, RenderedImage.ImageType.BlendedMSXRGBA8888Image);

                        MediaScannerConnection.scanFile(context,
                                new String[]{path + "/" + fileName}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.i("ExternalStorage", "Scanned " + path + ":");
                                        Log.i("ExternalStorage", "-> uri=" + uri);
                                    }

                                });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            thermalImageView.animate().setDuration(50).scaleY(0).withEndAction((new Runnable() {
                                public void run() {
                                    thermalImageView.animate().setDuration(50).scaleY(1);
                                }
                            }));
                        }
                    });
                }
            }).start();
        }

        if (streamSocket != null && streamSocket.isConnected()) {
            try {
                // send PNG file over socket in another thread
                final OutputStream outputStream = streamSocket.getOutputStream();
                // make a output stream so we can get the size of the PNG
                final ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();

                thermalBitmap.compress(Bitmap.CompressFormat.WEBP, 100, bufferStream);
                bufferStream.flush();
                (new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            /*
                             * Header is 6 bytes indicating the length of the image data and rotation
                             * of the device
                             * This could be expanded upon by adding bytes to have more metadata
                             * such as image format
                             */
                            byte[] headerBytes = ByteBuffer.allocate((Integer.SIZE + Short.SIZE) / 8).putInt(bufferStream.size()).putShort((short) deviceRotation).array();
                            synchronized (streamSocket) {
                                outputStream.write(headerBytes);
                                bufferStream.writeTo(outputStream);
                                outputStream.flush();
                            }
                            bufferStream.close();


                        } catch (IOException ex) {
                            Log.e("STREAM", "Error sending frame: " + ex.toString());
                        }
                    }
                }).start();
            } catch (Exception ex) {
                Log.e("STREAM", "Error creating PNG: " + ex.getMessage());

            }

        }
    }

        @Override
        protected void onStart(){

            super.onStart();
            thermalImageView = (ImageView) findViewById(R.id.imageView);
            try {
                Device.startDiscovery(this, this);
            }catch(IllegalStateException e){
                // it's okay if we've already started discovery
            }catch (SecurityException e){
                // On some platforms, we need the user to select the app to give us permisison to the USB device.
                Toast.makeText(this, "Please insert FLIR One and select "+getString(R.string.app_name), Toast.LENGTH_LONG).show();
                // There is likely a cleaner way to recover, but for now, exit the activity and
                // wait for user to follow the instructions;
                finish();
            }
        }

    @Override
    public void onBatteryChargingStateReceived(final Device.BatteryChargingState batteryChargingState) {
        Log.i("ExampleApp", "Battery charging state received!");
    }

    @Override
    public void onBatteryPercentageReceived(byte b) {

    }

    public void onTuneClicked(View v){
        if (flirOneDevice != null){
            flirOneDevice.performTuning();
        }

    }
}

