package com.zavier.androidfunctiontestv2.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.Nullable;

public class CustomPaintView extends View {
    private List<Point> allPoints=new ArrayList<Point>();
    private int mToolType = -1;

    public CustomPaintView(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        super.setOnTouchListener(new OnTouchListenerImp());
    }

    public void setToolType(int type){
        this.mToolType = type;
    }

    private class OnTouchListenerImp implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int toolType = event.getToolType(0);
            if (toolType != MotionEvent.TOOL_TYPE_STYLUS && CustomPaintView.this.mToolType == MotionEvent.TOOL_TYPE_STYLUS) {
                return true;
            }
            if (toolType != MotionEvent.TOOL_TYPE_FINGER && CustomPaintView.this.mToolType == MotionEvent.TOOL_TYPE_FINGER) {
                return true;
            }
            Point p=new Point((int)event.getX(),(int)event.getY());
            if(event.getAction()== MotionEvent.ACTION_DOWN){
                //用户按下，表示重新开始保存点
                CustomPaintView.this.allPoints=new ArrayList<Point>();
                CustomPaintView.this.allPoints.add(p);
            }
            else if(event.getAction()== MotionEvent.ACTION_UP){
                //用户松开
                CustomPaintView.this.allPoints.add(p);
                CustomPaintView.this.postInvalidate();//重绘图像
            }
            else if(event.getAction()== MotionEvent.ACTION_MOVE){
                CustomPaintView.this.allPoints.add(p);
                CustomPaintView.this.postInvalidate();//重绘图像
            }
            return true;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p = new Paint();//依靠此类开始画线
        p.setColor(Color.RED);
        p.setStrokeWidth((float) 2.0);
        if (CustomPaintView.this.allPoints.size() > 1) {
            //如果有坐标点，开始绘图
            Iterator<Point> iter = CustomPaintView.this.allPoints.iterator();
            Point first = null;
            Point last = null;
            while (iter.hasNext()) {
                if (first == null) {
                    first = (Point) iter.next();
                } else {
                    if (last != null) {
                        first = last;
                    }
                    last = (Point) iter.next();//结束
                    canvas.drawLine(first.x, first.y, last.x, last.y, p);
                }
            }
        }
    }
}
