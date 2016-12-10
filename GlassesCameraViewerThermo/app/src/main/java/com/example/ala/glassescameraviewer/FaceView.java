package com.example.ala.glassescameraviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ala on 16/07/16.
 */
public class FaceView extends View {

    private Bitmap mBitmap;
    private int mColor;

    public FaceView(Context context) {
        super(context);
        mColor = Color.RED;
    }
    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColor = Color.RED;
    }

    public void setBitmap( Bitmap bitmap ) {
        mBitmap = bitmap;
    }
    public void setColor( int color ) {
        mColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) {
            drawBitmap(canvas);
        }
    }

    private void drawBitmap( Canvas canvas ) {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min( viewWidth / imageWidth, viewHeight / imageHeight );

        Rect destBounds = new Rect( 0, 0, (int) ( imageWidth * scale ), (int) ( imageHeight * scale ) );
        drawBorder(canvas, destBounds);
        canvas.drawBitmap( mBitmap, null, destBounds, null );
    }

    private void drawBorder(Canvas canvas, Rect destBounds) {
        Paint paint = new Paint();
        paint.setColor( mColor );
        paint.setStrokeWidth( 10f );
        paint.setStyle( Paint.Style.STROKE );

        canvas.drawRect( 0, 0, destBounds.width()+10, destBounds.height()+10, paint );
    }
}
