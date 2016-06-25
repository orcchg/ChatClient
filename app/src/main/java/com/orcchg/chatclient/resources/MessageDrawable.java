package com.orcchg.chatclient.resources;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MessageDrawable extends Drawable {
    private static final float RADIUS_FACTOR = 8.0f;
    private final int TRIANGLE_WIDTH;
    private final int TRIANGLE_HEIGHT;

    public static final int NO_SIDE = -1;
    public static final int TOP_LEFT = 0;
    public static final int TOP_RIGHT = 1;
    public static final int BOTTOM_LEFT = 2;
    public static final int BOTTOM_RIGHT = 3;
    @IntDef({NO_SIDE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Side {}

    private @Side int mSide = TOP_LEFT;

    private Paint mPaint;

    public MessageDrawable() {
        this(24, 24, Color.WHITE);
    }

    public MessageDrawable(@ColorInt int color) {
        this(24, 24, color);
    }

    public MessageDrawable(int triangleWidth, int triangleHeight) {
        this(triangleWidth, triangleHeight, Color.WHITE);
    }

    public MessageDrawable(int triangleWidth, int triangleHeight, @ColorInt int color) {
        super();
        TRIANGLE_WIDTH = triangleWidth;
        TRIANGLE_HEIGHT = triangleHeight;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        float radius = Math.min(width, height) / RADIUS_FACTOR;

        Path path = new Path();

        switch (mSide) {
            case NO_SIDE:
                path.moveTo(0, 0);
                path.lineTo(width, 0);
                path.lineTo(width, height);
                path.lineTo(0, height);
                path.lineTo(0, 0);
                break;
            case TOP_LEFT:
                path.moveTo(TRIANGLE_WIDTH, 0);
                path.lineTo(width - radius, 0);
                path.quadTo(width, 0, width, radius);
                path.lineTo(width, height - radius);
                path.quadTo(width, height, width - radius, height);
                path.lineTo(TRIANGLE_WIDTH + radius, height);
                path.quadTo(TRIANGLE_WIDTH, height, TRIANGLE_WIDTH, height - radius);
                path.lineTo(TRIANGLE_WIDTH, TRIANGLE_HEIGHT);
                path.lineTo(0, 0);
                path.lineTo(TRIANGLE_WIDTH, 0);
                break;
            case TOP_RIGHT:
                path.moveTo(radius, 0);
                path.lineTo(width, 0);
                path.lineTo(width - TRIANGLE_WIDTH, TRIANGLE_HEIGHT);
                path.lineTo(width - TRIANGLE_WIDTH, height - radius);
                path.quadTo(width - TRIANGLE_WIDTH, height, width - TRIANGLE_WIDTH - radius, height);
                path.lineTo(radius, height);
                path.quadTo(0, height, 0, height - radius);
                path.lineTo(0, radius);
                path.quadTo(0, 0, radius, 0);
                break;
            case BOTTOM_LEFT:
                path.moveTo(TRIANGLE_WIDTH + radius, 0);
                path.lineTo(width - radius, 0);
                path.quadTo(width, 0, width, radius);
                path.lineTo(width, height - radius);
                path.quadTo(width, height, width - radius, height);
                path.lineTo(0, height);
                path.lineTo(TRIANGLE_WIDTH, height - TRIANGLE_HEIGHT);
                path.lineTo(TRIANGLE_WIDTH, height - radius);
                path.quadTo(TRIANGLE_WIDTH, 0, TRIANGLE_WIDTH + radius, 0);
                break;
            case BOTTOM_RIGHT:
                path.moveTo(radius, 0);
                path.lineTo(width - TRIANGLE_WIDTH - radius, 0);
                path.quadTo(width - TRIANGLE_WIDTH, 0, width - TRIANGLE_WIDTH, radius);
                path.lineTo(width - TRIANGLE_WIDTH, height - TRIANGLE_HEIGHT);
                path.lineTo(width, height);
                path.lineTo(radius, height);
                path.quadTo(0, height, 0, height - radius);
                path.lineTo(0, radius);
                path.quadTo(0, 0, radius, 0);
                break;
        }

        path.close();
        canvas.drawPath(path, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public void setSide(@Side int side) {
        mSide = side;
    }
}
