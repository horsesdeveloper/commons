package com.horses.camera.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Brian Salvattore
 */
public class CameraGrid extends View {

    private int topBannerWidth = 0;
    private Paint paint;

    public CameraGrid(Context context) {
        this(context,null);
    }

    public CameraGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(120);
        paint.setStrokeWidth(1f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = this.getWidth();
        int height = this.getHeight();

        if (width < height) {

            topBannerWidth = height - width;
        }
        if (showGrid) {

            canvas.drawLine(width / 3, 0, width / 3, height, paint);
            canvas.drawLine(width * 2 / 3, 0, width * 2 / 3, height, paint);
            canvas.drawLine(0, height / 3, width, height / 3, paint);
            canvas.drawLine(0, height * 2 / 3, width, height * 2 / 3, paint);
        }
    }

    private boolean showGrid = true;

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public int getTopWidth() {
        return topBannerWidth;
    }
}
