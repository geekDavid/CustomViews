package com.wellleo.clock.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.wellleo.common.Common;
import com.wellleo.common.utils.CLockUtils;
import com.wellleo.common.utils.CommonLog;
import com.wellleo.common.utils.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * This is a simulation clock view
 */
public class ClockView extends View {

    private static final int DEFAULT_SIZE = (int) (ScreenUtils.getScreenWidth() * 0.7f + 0.5f);
    private static final int DEFAULT_WIDTH = DEFAULT_SIZE;
    private static final int DEFAULT_HEIGHT = DEFAULT_SIZE;
    private static final long DELAYM_ILLIS = 1000;

    private int bgPaintSize = ScreenUtils.dip2px(Common.sAppContext, 3);
    private int scalePaintSize = ScreenUtils.dip2px(Common.sAppContext, 3);
    private int indexPaintSize = ScreenUtils.dip2px(Common.sAppContext, 3);

    private int mWidth;
    private int mHeight;
    //View margin offset view 边缘缓冲宽度3dp默认，防止绘制被边缘切割
    private float marginOffset = ScreenUtils.dip2px(Common.sAppContext, 3);
    //Long scale size 长刻度的长度，代表小时的刻度
    private float mScaleLongSize = DEFAULT_SIZE * 0.1f - marginOffset;
    //Normal scale size 正常刻度的长度 简体
    private float mScaleSize = DEFAULT_SIZE * 0.1f * 0.7f - marginOffset;
    //指针线默认长度
    private float mIndexLineSize = DEFAULT_SIZE * 0.5f;
    //中点值
    private float centerPoint;
    //true表示秒动，false表示持续动
    private boolean isSecondAnimator = false;
    //秒针角度
    private float secondDegrees;
    //分针角度
    private float minuteDegrees;
    //时针角度
    private float hourDegrees;
    //当前秒数
    private int currentSecond;


    private Paint mClockBgPaint;
    private Paint mClockScalePaint;
    private Paint mClockHoursPaint;
    private Paint mClockMinutesPaint;
    private Paint mClockSecondsPaint;

    private Runnable mClockRunnable;
    private Handler mHandler;

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        mClockRunnable = new ClockRunnable(this);
        mHandler = new Handler();
        rotatePointer();
    }

    //初始化画笔
    private void initPaint() {

        CommonLog.DEBUG("init Paint");
        mClockBgPaint = new Paint();
        mClockBgPaint.setAntiAlias(true);
        mClockBgPaint.setColor(Color.DKGRAY);
        mClockBgPaint.setStrokeWidth(bgPaintSize);
        mClockBgPaint.setStyle(Paint.Style.STROKE);

        mClockScalePaint = new Paint();
        mClockScalePaint.setAntiAlias(true);
        mClockScalePaint.setColor(Color.WHITE);
        mClockScalePaint.setStrokeWidth(scalePaintSize);
        mClockScalePaint.setStyle(Paint.Style.FILL);

        mClockHoursPaint = new Paint();
        mClockHoursPaint.setAntiAlias(true);
        mClockHoursPaint.setColor(Color.WHITE);
        mClockHoursPaint.setStrokeWidth(indexPaintSize * 2);
        mClockHoursPaint.setStyle(Paint.Style.FILL);

        mClockMinutesPaint = new Paint();
        mClockMinutesPaint.setAntiAlias(true);
        mClockMinutesPaint.setColor(Color.GRAY);
        mClockMinutesPaint.setStrokeWidth(indexPaintSize * 2);
        mClockMinutesPaint.setStyle(Paint.Style.FILL);

        mClockSecondsPaint = new Paint();
        mClockSecondsPaint.setAntiAlias(true);
        mClockSecondsPaint.setColor(Color.RED);
        mClockSecondsPaint.setStrokeWidth(indexPaintSize);
        mClockSecondsPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST ||
                heightMode == MeasureSpec.AT_MOST) {
            mWidth = DEFAULT_WIDTH;
            mHeight = DEFAULT_HEIGHT;
        } else {
            mWidth = MeasureSpec.getSize(widthMeasureSpec);
            mHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
        CommonLog.DEBUG("width : " + mWidth + " , height : " + mHeight);
        if (centerPoint == 0) {
            centerPoint = Math.min(mWidth, mHeight) / 2f;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw clock bg
        drawClockBg(canvas);
        //draw clock scale line
        drawScaleLine(canvas);
        //draw clock hours index
        drawPointer(canvas, hourDegrees, centerPoint, (mScaleLongSize + marginOffset) * 2f,
                centerPoint, mIndexLineSize + marginOffset, mClockHoursPaint);
        //draw clock minutes index
        drawPointer(canvas, minuteDegrees, centerPoint, mScaleLongSize + marginOffset,
                centerPoint, mIndexLineSize + marginOffset, mClockMinutesPaint);
        //draw clock seconds index
        drawPointer(canvas, secondDegrees, centerPoint, (mScaleLongSize + marginOffset) * 1.5f,
                centerPoint, mIndexLineSize + marginOffset * 2f, mClockSecondsPaint);
    }


    private void drawClockBg(Canvas canvas) {
        mClockBgPaint.setStyle(Paint.Style.FILL);
        mClockBgPaint.setColor(0xFF6200EE);
        canvas.drawCircle(
                mWidth / 2f,
                mHeight / 2f,
                centerPoint - marginOffset,
                mClockBgPaint
        );
    }

    private void drawPointer(Canvas canvas, float degress, float startX, float startY,
                             float stopX, float stopY, @androidx.annotation.NonNull Paint paint) {
        //保存画布当前状态
        canvas.save();
        //旋转秒针画布
        canvas.rotate(degress, centerPoint, centerPoint);
        //绘制秒针
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        //恢复画布状态
        canvas.restore();
    }

    //绘制刻度
    private void drawScaleLine(Canvas canvas) {
        canvas.save();
        //循环绘制刻度线
        for (int i = 0; i < 60; i++) {
            canvas.drawLine(
                    centerPoint,
                    marginOffset,
                    centerPoint,
                    (i % 5 == 0) ? mScaleLongSize : mScaleSize,
                    mClockScalePaint
            );
            canvas.rotate(CLockUtils.CLOCK_CYCLE / CLockUtils.CLOCK_SCALE_SIZE,
                    Math.min(mWidth, mHeight) / 2f,
                    Math.min(mWidth, mHeight) / 2f);
        }
        canvas.restore();
    }

    //旋转指针
    private void rotatePointer() {
        calculateDegrees();
        //秒动
        if (isSecondAnimator) {
            invalidate();
            mHandler.postDelayed(mClockRunnable, DELAYM_ILLIS);
            return;
        }
        //非秒动
        long delayMillts = DELAYM_ILLIS * (60 - currentSecond);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(secondDegrees, CLockUtils.CLOCK_CYCLE);
        valueAnimator.setDuration(delayMillts);
        valueAnimator.setInterpolator(null);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                secondDegrees = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.start();
        mHandler.postDelayed(mClockRunnable, delayMillts);
    }

    //根据时间计算时针、分针、秒针的旋转角度
    private void calculateDegrees() {
        minuteDegrees = 0;
        hourDegrees = 0;

        //获取当前小时数
        //当前小时数
        int currentHour = Calendar.getInstance().get(Calendar.HOUR);
        //获取当前分钟数
        //当前分钟数
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
        //计算分针旋转的角度（当前分钟数 / 刻度数60° * 周期数360°）
        minuteDegrees = currentMinute / CLockUtils.CLOCK_SCALE_SIZE * CLockUtils.CLOCK_CYCLE;
        //根据分针旋转角度，计算时针偏移量（分钟旋转角度 / 周期数360° * 一小时间隔角度30°） + (一小时间隔角度30° * 当前小时数)
        float minutesDegressOffset = (minuteDegrees / CLockUtils.CLOCK_CYCLE * 30) + (30 * currentHour);
        //计算时针旋转的角度（当前小时数 / 一周小时总数12小时 / 一周刻度数60 * 周期数360° + 分钟偏移量）
        hourDegrees = currentHour / CLockUtils.CLOCK_HOURS_SIZE /
                CLockUtils.CLOCK_SCALE_SIZE * CLockUtils.CLOCK_CYCLE + minutesDegressOffset;

        //获取当前秒数
        currentSecond = Calendar.getInstance().get(Calendar.SECOND);
        //计算秒针旋转的角度（当前秒数/刻度数60°/周期数360°）
        secondDegrees = currentSecond / CLockUtils.CLOCK_SCALE_SIZE * CLockUtils.CLOCK_CYCLE;
    }

    static class ClockRunnable implements Runnable {

        WeakReference<ClockView> clockViewWeakReference;

        public ClockRunnable(ClockView clockView) {
            if (clockViewWeakReference == null) {
                clockViewWeakReference = new WeakReference<>(clockView);
            }
        }

        @Override
        public void run() {
            if (clockViewWeakReference == null || clockViewWeakReference.get() == null) {
                return;
            }
            ClockView clockView = clockViewWeakReference.get();
            clockView.rotatePointer();
        }
    }

}
