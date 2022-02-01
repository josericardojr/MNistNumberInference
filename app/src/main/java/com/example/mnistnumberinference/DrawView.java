package com.example.mnistnumberinference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {

    private Path mPath, circlePath;
    Canvas mCanvas;
    Bitmap mBitmap;
    Paint drawPaint;
    float mX, mY;

    public DrawView(Context context) {
        super(context);

        setup();
    }


    public DrawView(Context context, AttributeSet attrs){
        super(context, attrs);

        setup();
    }

    /*
    Setup interface components
     */
    private void setup(){
        mPath = new Path();
        circlePath = new Path();

        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setColor(Color.WHITE);
        drawPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touch_start(x,y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touch_move(x,y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }

        return true;
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();

        mCanvas.drawPath(mPath, drawPaint);
        mPath.reset();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.BLACK);
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= 4 || dy >= 4){
                mPath.quadTo(mX, mY, (x + mX) / 2.0f, (y + mY) / 2.0f);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);

        mX = x; mY = y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, null);
        canvas.drawPath(mPath, drawPaint);
        canvas.drawPath(circlePath, drawPaint);
    }

    public void setDrawStroke(float value) {
        drawPaint.setStrokeWidth(value);
    }

    public void clearCanvas() {
        mCanvas.drawColor(Color.BLACK);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
