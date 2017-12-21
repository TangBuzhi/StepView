package com.qb.code.stepview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * http://git.oschina.net/tangbuzhi
 *
 * @author Tangbuzhi
 * @version V1.0
 * @Package
 * @Description:
 * @date: 2017/12/18
 */

public class StepView extends View {

    /*attr value*/
    private int defaultDotCount = 2;
    private int dotCount;
    private int defaultMaxDotCount = 5;
    private int maxDotCount;
    private int defaultStepNum = 0;
    private int stepNum;
    private int defaultLineLength = 0;
    private int lineLength;
    private int defaultTextLocation = 0;
    private int textLocation;
    private int defaultNormalLineColor;
    private int normalLineColor;
    private int defaultPassLineColor;
    private int passLineColor;
    private float defaultLineStikeWidth;
    private float lineStikeWidth;
    private int defaultTextColor;
    private int textColor;
    private float defaultTextSize;
    private float textSize;
    private float defaultText2DotMargin;
    private float text2LineMargin;
    private int defalutMargin;
    private int margin;
    private float defaultLine2TopMargin;
    private float line2TopMargin;
    private float defaultText2BottomMargin;
    private float text2BottomMargin;

    /*view messured size*/
    private int width, height;
    private int perLineLength;

    private Paint linePaint;
    private Paint textPaint;
    private Rect bounds;
    private Bitmap normal_pic;
    private Bitmap target_pic;
    private Bitmap passed_pic;
    private String[] texts = {"ABCD", "EFGH", "LMJK", "XYZO", "QPRT"};
    private boolean isTextBelowLine = true;
    private float line2BottomMargin;
    private float text2TopMargin;
    private boolean defaultViewClickable = false;
    private boolean clickable;
    private int[] passWH;
    private int[] normalWH;
    private int[] targetWH;

    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        defaultNormalLineColor = Color.parseColor("#545454");
        defaultPassLineColor = Color.WHITE;
        defaultTextColor = Color.WHITE;
        defaultLineStikeWidth = dp2px(context, 1);
        defaultTextSize = sp2px(context, 80);
        defaultText2DotMargin = dp2px(context, 15);
        defalutMargin = dp2px(context, 100);
        defaultLine2TopMargin = dp2px(context, 30);
        defaultText2BottomMargin = dp2px(context, 20);

        normal_pic = BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal);
        target_pic = BitmapFactory.decodeResource(getResources(), R.drawable.ic_target);
        passed_pic = BitmapFactory.decodeResource(getResources(), R.drawable.ic_passed);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StepView, defStyleAttr, 0);
        dotCount = a.getInt(R.styleable.StepView_count, defaultDotCount);
        if (dotCount < 2) {
            throw new IllegalArgumentException("Steps can't be less than 2");
        }
        stepNum = a.getInt(R.styleable.StepView_step, defaultStepNum);
        lineLength = a.getInt(R.styleable.StepView_line_length, defaultLineLength);
        maxDotCount = a.getInt(R.styleable.StepView_max_dot_count, defaultMaxDotCount);
        if (maxDotCount < dotCount) {//当最多点小于设置点数量时，设置线条长度可变
            lineLength = defaultLineLength;
        }
        textLocation = a.getInt(R.styleable.StepView_text_location, defaultTextLocation);
        isTextBelowLine = textLocation == defaultTextLocation;

        normalLineColor = a.getColor(R.styleable.StepView_normal_line_color, defaultNormalLineColor);
        passLineColor = a.getColor(R.styleable.StepView_passed_line_color, defaultPassLineColor);
        lineStikeWidth = a.getDimension(R.styleable.StepView_line_stroke_width, defaultLineStikeWidth);
        textColor = a.getColor(R.styleable.StepView_text_color, defaultTextColor);
        textSize = a.getDimension(R.styleable.StepView_text_size, defaultTextSize);
        text2LineMargin = a.getDimension(R.styleable.StepView_text_to_line_margin, defaultText2DotMargin);
        margin = (int) a.getDimension(R.styleable.StepView_margin, defalutMargin);
        line2TopMargin = a.getDimension(R.styleable.StepView_line_to_top_margin, defaultLine2TopMargin);
        text2BottomMargin = a.getDimension(R.styleable.StepView_text_to_bottom_margin, defaultText2BottomMargin);
        clickable = a.getBoolean(R.styleable.StepView_is_view_clickable, defaultViewClickable);
        a.recycle();
        //线条画笔
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(lineStikeWidth);
        //文字画笔
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        //存放说明文字的矩形
        bounds = new Rect();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w - margin * 2;
        height = h;
        //线条长度是否可变
        if (lineLength == defaultLineLength) {//可变
            perLineLength = width / (dotCount - 1);
        } else {//固定
            perLineLength = width / (maxDotCount - 1);
        }
        passWH = calculateWidthAndHeight(passed_pic);
        normalWH = calculateWidthAndHeight(normal_pic);
        targetWH = calculateWidthAndHeight(target_pic);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //当文字在线条上面时，参数倒置
        if (!isTextBelowLine) {
            line2BottomMargin = line2TopMargin;
            text2TopMargin = text2BottomMargin;
        }
        drawConnectLine(canvas, stepNum);
        drawNormalSquar(canvas, stepNum);
        drawTargetSquar(canvas, stepNum);
        drawDescText(canvas);
    }

    /*绘制链接步骤点之间的线条*/
    private void drawConnectLine(Canvas canvas, int stepNum) {
        float startX = 0;
        float stopX = 0;
        for (int i = 0; i < dotCount - 1; i++) {
            /*设置线条起点X轴坐标*/
            if (i == stepNum) {
                startX = margin + perLineLength * i + targetWH[0] / 2;
            } else if (i > stepNum) {
                startX = margin + perLineLength * i + normalWH[0] / 2;
            } else {
                startX = margin + perLineLength * i + passWH[0] / 2;
            }
            /*设置线条终点X轴坐标*/
            if (i + 1 == stepNum) {
                stopX = margin + perLineLength * (i + 1) - targetWH[0] / 2;
            } else if (i + 1 < stepNum) {
                stopX = margin + perLineLength * (i + 1) - passWH[0] / 2;
            } else {
                stopX = margin + perLineLength * (i + 1) - normalWH[0] / 2;
            }
            /*当目标步骤超过i时，线条设置为已过颜色，不超过时，设置为普通颜色*/
            if (stepNum > i) {
                linePaint.setColor(passLineColor);
            } else {
                linePaint.setColor(normalLineColor);
            }
            if (isTextBelowLine) {
                /*当文字在线条下方时，设置线条y轴的位置并绘制*/
                canvas.drawLine(startX, line2TopMargin, stopX, line2TopMargin, linePaint);
            } else {
                canvas.drawLine(startX, height - line2BottomMargin, stopX, height - line2BottomMargin, linePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (clickable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Point point = new Point();
                    point.x = (int) event.getX();
                    point.y = (int) event.getY();
                    int stepInDots = getStepInDots(point);
                    if (stepInDots != -1) {
                        stepNum = stepInDots;
                        invalidate();
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    /*获取手指触摸点为第几个步骤点，异常时返回-1*/
    private int getStepInDots(Point point) {
        for (int i = 0; i < dotCount; i++) {
            Rect rect = getSetpSquarRects()[i];
            int x = point.x;
            int y = point.y;
            if (rect.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    /*获取所有步骤点的矩阵区域*/
    private Rect[] getSetpSquarRects() {
        Rect[] rects = new Rect[dotCount];
        int left, top, right, bottom;
        for (int i = 0; i < dotCount; i++) {
            /*此处默认所有点的区域范围为被选中图片的区域范围*/
            Rect rect = new Rect();
            left = margin + perLineLength * i - targetWH[0] / 2;
            right = margin + perLineLength * i + targetWH[0] / 2;
            if (isTextBelowLine) {
                top = (int) (line2TopMargin - targetWH[1] / 2);
                bottom = (int) (line2TopMargin + targetWH[1] / 2);
            } else {
                top = (int) (height - line2BottomMargin - targetWH[1] / 2);
                bottom = (int) (height - line2BottomMargin + targetWH[1] / 2);
            }
            rect.set(left, top, right, bottom);
            rects[i] = rect;
        }
        return rects;
    }

    /*绘制一般情况下的步骤点图片*/
    private void drawNormalSquar(Canvas canvas, int stepNum) {
        for (int i = 0; i < dotCount; i++) {
            /*在目标点状态时，普通图片不绘制，跳过，继续下一次循环*/
            if (stepNum == i) {
                continue;
            }
            if (stepNum > i) {
                float left = margin + perLineLength * i - passWH[0] / 2;
                float top = 0;
                if (isTextBelowLine) {
                    top = line2TopMargin - passWH[1] / 2;
                } else {
                    top = height - line2BottomMargin - passWH[1] / 2;
                }
                canvas.drawBitmap(passed_pic, left, top, null);
            } else {
                float left = margin + perLineLength * i - normalWH[0] / 2;
                float top = 0;
                if (isTextBelowLine) {
                    top = line2TopMargin - normalWH[1] / 2;
                } else {
                    top = height - line2BottomMargin - normalWH[1] / 2;
                }
                canvas.drawBitmap(normal_pic, left, top, null);
            }
        }
    }

    /*计算bitmap宽高*/
    private int[] calculateWidthAndHeight(Bitmap bitmap) {
        int[] wh = new int[2];
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        wh[0] = width;
        wh[1] = height;
        return wh;
    }

    /*绘制目标步骤图片*/
    private void drawTargetSquar(Canvas canvas, int i) {
        float left = margin + perLineLength * i - targetWH[0] / 2;
        float top = 0;
        if (isTextBelowLine) {
            top = line2TopMargin - targetWH[1] / 2;
        } else {
            top = height - line2BottomMargin - targetWH[1] / 2;
        }
        canvas.drawBitmap(target_pic, left, top, null);
    }

    /*绘制各步骤说明文字*/
    private void drawDescText(Canvas canvas) {
        for (int i = 0; i < dotCount; i++) {
            String text = texts[i];
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            int textWidth = bounds.width();
            int textHeight = bounds.height();
            float x = margin + perLineLength * i - textWidth / 2;
            float y;
            if (isTextBelowLine) {
                y = height - text2BottomMargin;
            } else {
                y = text2TopMargin + textHeight;
            }
            canvas.drawText(text, x, y, textPaint);
        }
    }

    public void recycle() {
        recycleBitmap(normal_pic);
        recycleBitmap(passed_pic);
        recycleBitmap(target_pic);
        System.gc();
    }

    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /*给外部调用接口，设置步骤总数*/
    public void setDotCount(int count) {
        if (count < 2) {
            throw new IllegalArgumentException("dot count can't be less than 2.");
        }
        dotCount = count;
    }

    /*给外部调用接口，设置说明文字信息*/
    public void setDescription(String[] descs) {
        if (descs == null || descs.length < dotCount) {
            throw new IllegalArgumentException("Descriptions can't be null or its length must maore than dot count");
        }
        texts = descs;
    }

    /*给外部调用接口，设置该view是否可点击*/
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    /*给外部调用接口，设置文字是否在线条下面*/
    public void setTextBelowLine(boolean is) {
        this.isTextBelowLine = is;
        invalidate();
    }

    /*给外部调用接口，设置步骤*/
    public void setStep(Step step) {
        switch (step) {
            case ONE:
                stepNum = 0;
                break;
            case TWO:
                stepNum = 1;
                break;
            case THREE:
                stepNum = 2;
                break;
            case FOUR:
                stepNum = 3;
                break;
            case FIVE:
                stepNum = 4;
                break;
            default:
                break;
        }
        invalidate();
    }

    /*此处默认最多为5个步骤*/
    public enum Step {
        ONE, TWO, THREE, FOUR, FIVE
    }

    private int dp2px(Context context, int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * value + 0.5f);
    }

    private int sp2px(Context context, int value) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (value / scaledDensity + 0.5f);
    }
}
