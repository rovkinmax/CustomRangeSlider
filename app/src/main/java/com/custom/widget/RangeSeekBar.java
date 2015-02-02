package com.custom.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created UseIT for  ExampleCustomRangeSlider
 * User: maxrovkin
 * Date: 20.11.13
 * Time: 11:02
 */
public class RangeSeekBar extends View
{
    private static final String TAG = "SEEK_TAG";

    private static final int BACKGROUND_COLOR = Color.parseColor("#D3CDC3");
    private static final int PROGRESS_COLOR = Color.parseColor("#8BC7EC");
    private static final int SCALE_COLOR = Color.parseColor("#7B7369");

    private int heightProgress;
    private int thumbSize;
    private int thumbInnerSize;
    private int scaleHeight;
    private int pointSize;
    private int labelPointH;
    private int labelTextSize;

    private int mScaledTouchSlop;
    private float padding;

    private int minValue;
    private int maxValue;
    private int currentMin;
    private int currentMax;
    private int[] allValues = new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
    private int multiplicity = 5;
    private int currentIncrementPoint = 1;
    private float currentStep = 0;

    private String units = "°C";

    public static final int INVALID_POINTER_ID = 255;
    private int mActivePointerId = INVALID_POINTER_ID;
    private float mDownMotionX;
    private Thumb pressedThumb = null;
    private boolean mIsDragging = false;
    // with API < 8 "Froyo".
    public static final int ACTION_POINTER_UP = 0x6, ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

    private OnRangeChangeListener onRangeChangeListener;

    public RangeSeekBar(final Context context)
    {
        this(new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30}, 5, context);
    }

    /**
     * Создает экземпляр двойного прогресс бара
     *
     * @param values       значения, которые будет принимать
     * @param multiplicity кратность меток, которые буду подписаны
     * @param context      контектс приложения
     */
    public RangeSeekBar(final int[] values, int multiplicity, Context context)
    {
        this(context, null);

        this.multiplicity = multiplicity;
        allValues = values;

    }

    public RangeSeekBar(final Context context, final AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public RangeSeekBar(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
        initDef();
        if (attrs != null)
        {
            final TypedArray attrsArray = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);
            heightProgress = attrsArray.getDimensionPixelOffset(R.styleable.RangeSeekBar_height_progress, heightProgress);
            thumbSize = attrsArray.getDimensionPixelOffset(R.styleable.RangeSeekBar_thumb_size, thumbSize);
            thumbInnerSize = attrsArray.getDimensionPixelOffset(R.styleable.RangeSeekBar_thumb_inner_size, thumbInnerSize);
            scaleHeight = attrsArray.getDimensionPixelOffset(R.styleable.RangeSeekBar_scale_height, scaleHeight);
            pointSize = attrsArray.getDimensionPixelOffset(R.styleable.RangeSeekBar_point_size, pointSize);
            labelPointH = attrsArray.getDimensionPixelOffset(R.styleable.RangeSeekBar_label_point_height, labelPointH);
            labelTextSize = attrsArray.getDimensionPixelOffset(R.styleable.RangeSeekBar_label_text_size, labelTextSize);
            attrsArray.recycle();
        }

    }

    private void initDef()
    {
        heightProgress = getResources().getDimensionPixelOffset(R.dimen.heightProgress);
        thumbSize = getResources().getDimensionPixelOffset(R.dimen.size_thumb);
        thumbInnerSize = getResources().getDimensionPixelOffset(R.dimen.thumbInnerSize);
        scaleHeight = getResources().getDimensionPixelOffset(R.dimen.scaleHeight);
        pointSize = getResources().getDimensionPixelOffset(R.dimen.pointSize);
        labelPointH = getResources().getDimensionPixelOffset(R.dimen.labelPointH);
        labelTextSize = getResources().getDimensionPixelOffset(R.dimen.labelTextSize);
        padding = thumbSize;

        maxValue = allValues.length - 1;
        minValue = 0;
    }


    /**
     * Текущее значение нижней границы диапазона, возвращается занчение из заданного набора
     *
     * @return значени нижней границы диапазона
     */
    public int getMinProgress()
    {
        return allValues[currentMin];
    }

    /**
     * Устаналивает индех минимального значения из заданного набора
     *
     * @param minIndexValue индек в массиве заданных занчений
     */
    public void setMinProgress(final int minIndexValue)
    {
        Log.d(TAG, String.format("minProgress %d", minIndexValue));
        this.currentMin = minIndexValue;
        invalidate();
    }

    /**
     * Устанавливает идекс максимального значения из заданного набора
     *
     * @param maxIndexValue идекс в массиве заданных значений
     */
    public void setMaxProgress(final int maxIndexValue)
    {
        Log.d(TAG, String.format("maxProgress %d", maxIndexValue));
        this.currentMax = maxIndexValue;
        invalidate();
    }

    /**
     * Текущее значение верхней границы диапазона, возвращается занчение из заданного набора
     *
     * @return значени верхней границы диапазона
     */
    public int getMaxProgress()
    {
        return allValues[currentMax];
    }

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    protected void onDraw(Canvas canvas)
    {

        calculateNewStep();
        //рисуем фоновую линию
        final RectF rect = new RectF(padding, getHeight() * 0.5f - heightProgress / 2, getWidth() - padding, getHeight() * 0.5f + heightProgress / 2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(BACKGROUND_COLOR);
        canvas.drawRect(rect, paint);

        //рисуем прогресс
        rect.left = valueToScreen(currentMin);
        rect.right = valueToScreen(currentMax);
        paint.setColor(PROGRESS_COLOR);
        canvas.drawRect(rect, paint);

        drawScale(canvas);
        drawThumb(canvas, currentMin);
        drawThumb(canvas, currentMax);

    }

    /**
     * Переводит значение в координаты на шкале
     *
     * @param value
     *
     * @return
     */
    private int valueToScreen(int value)
    {
        final float step = getCurrentStep();
        if (minValue < 0)
            return (int) (step * (Math.abs(minValue) + value));
        else
            return (int) (step * (value - minValue));
    }

    /**
     * Переводит координаты экрана в значение шкалы
     *
     * @param point координаты экрана
     *
     * @return
     */
    private int screenToValue(float point)
    {
        int width = getWidth();
        if (width <= padding * 2)
            return minValue;
        else
        {
            final double truePoint = (point - padding * 2);
            if (truePoint < 0)
                return minValue;
            if (point > width - padding)
                return maxValue;

            final float step = getCurrentStep();
            final int integerPart = ((int) truePoint) / ((int) step * currentIncrementPoint);
            final int fractionalPart = ((int) truePoint) % ((int) step);
            Log.d(TAG, String.format("integer part %d || step %f", integerPart, step));

            final int result = fractionalPart > (int) (step / 2) ? (integerPart + 1) : integerPart;
            return result + minValue;
        }
    }

    /**
     * Рисуем бегунок
     *
     * @param canvas
     * @param value  значение, к которму бегунок будет привязан
     */
    private void drawThumb(Canvas canvas, final int value)
    {
        final float hCenter = getHeight() * 0.5f;
        final float wCenter = valueToScreen(value) + padding;
        paint.setColor(PROGRESS_COLOR);
        canvas.drawCircle(wCenter, hCenter, thumbSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(wCenter, hCenter, thumbInnerSize, paint);
    }

    /**
     * Рисует шкалу
     *
     * @param canvas
     */
    private void drawScale(Canvas canvas)
    {
        int value = minValue;
        float cX;
        int radius;
        final float cY = thumbSize * 2 + scaleHeight * 2 + pointSize / 2;
        do
        {
            cX = valueToScreen(value) + padding;
            paint.setColor(SCALE_COLOR);
            final boolean isBigPoint = value % multiplicity == 0;
            radius = isBigPoint ? pointSize : pointSize / 2;
            if (isBigPoint)
                drawLabel(canvas, value);

            canvas.drawCircle(cX, cY, radius, paint);
            Log.d(TAG, String.format("value = %d  cX = %f  cY = %f ", value, cX, cY));
            value += currentIncrementPoint;
        } while (cX + getCurrentStep() < getWidth() - padding);
    }

    /**
     * Делает подписи к шкале
     *
     * @param value
     */
    private void drawLabel(final Canvas canvas, final int value)
    {
        paint.setColor(SCALE_COLOR);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(labelTextSize);
        float cX;
        if (value == maxValue)
            cX = valueToScreen(value) + padding / 2;
        else
            if (value == minValue)
                cX = valueToScreen(value) + padding + padding / 2;
            else
                cX = valueToScreen(value) + padding;

        final float cY = thumbSize * 2 + scaleHeight + labelPointH * 2 + pointSize;
        canvas.drawText(String.format("%d%s", allValues[value], units), cX, cY, paint);
    }


    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec))
        {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = thumbSize * 2 + scaleHeight * 2 + labelPointH * 2;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec))
        {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!isEnabled())
            return false;
        int pointerIndex;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                //запомним, где началось действие
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);
                pressedThumb = determPressedThumb(mDownMotionX);
                if (pressedThumb == null)
                    return super.onTouchEvent(event);
                setPressed(true);
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();
                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null)
                {
                    if (mIsDragging)
                        trackTouchEvent(event);
                    else
                    {
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop)
                        {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }
                    if (onRangeChangeListener != null)
                        onRangeChangeListener.onRangeChange(getMinProgress(), getMaxProgress());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging)
                {
                    trackTouchEvent(event);
                    onStartTrackingTouch();
                    setPressed(false);
                }
                else
                {
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                pressedThumb = null;
                invalidate();

                if (onRangeChangeListener != null)
                    onRangeChangeListener.onRangeChange(getMinProgress(), getMaxProgress());
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = event.getPointerCount() - 1;
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging)
                {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate();
                break;
        }

        return true;
    }

    /**
     * Определяет на какой бегунок было нажатие
     *
     * @param mDownMotionX координаты нажатия
     *
     * @return возвращает, Min или Max если было куда то нажато, если на бегунок не попали, то null
     */
    private Thumb determPressedThumb(final float mDownMotionX)
    {
        Thumb result = null;
        final boolean minThumbPressed = isInThumbRange(mDownMotionX, currentMin);
        final boolean maxThumbPressed = isInThumbRange(mDownMotionX, currentMax);

        if (minThumbPressed && maxThumbPressed)
        {
            result = (mDownMotionX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        }
        else
            if (minThumbPressed)
                result = Thumb.MIN;
            else
                if (maxThumbPressed)
                    result = Thumb.MAX;

        return result;
    }

    private boolean isInThumbRange(float touchX, final int value)
    {
        return Math.abs(touchX - valueToScreen(value)) <= thumbSize * 2;
    }

    /**
     * Thumb constants(min and max)
     */
    private static enum Thumb
    {
        MIN,
        MAX
    }

    /**
     * Вызывается, когда юзер начинает трогать этот элемент
     */
    void onStartTrackingTouch()
    {
        mIsDragging = true;
    }

    /**
     * Вызввается, когда юзер заканчивает трогать этот элемент
     */
    void onStopTrackingTouch()
    {
        mIsDragging = false;
    }

    private final void trackTouchEvent(MotionEvent event)
    {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);
        final int newValue = screenToValue(x);
        if (Thumb.MIN.equals(pressedThumb))
        {
            if (newValue <= currentMax)
                setMinProgress(newValue);
        }
        else
        {
            if (Thumb.MAX.equals(pressedThumb))
                if (newValue >= currentMin)
                    setMaxProgress(newValue);
        }
    }

    private final void onSecondaryPointerUp(MotionEvent ev)
    {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId)
        {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void attemptClaimDrag()
    {
        if (getParent() != null)
        {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    public void setCurrentStep(float currentStep)
    {
        this.currentStep = currentStep;
    }

    public float getCurrentStep()
    {
        return currentStep;
    }

    private void calculateNewStep()
    {
        final int absCount = Math.abs(maxValue - minValue);
        final float step = ((getWidth() - 2 * padding) / absCount);
        setCurrentStep(step);
    }

    public interface OnRangeChangeListener
    {
        public void onRangeChange(final int minValue, final int maxValue);
    }

    public void setOnRangeChangeListener(final OnRangeChangeListener onRangeChangeListener)
    {
        this.onRangeChangeListener = onRangeChangeListener;
    }

    /**
     * Устанавливает кратность меток, т.е. какие метки будут подписываться
     */
    public void setMultiplicity(final int multiplicity)
    {
        this.multiplicity = multiplicity;
        invalidate();
    }

    /**
     * Возвращает кратность подписанных меток
     *
     * @return
     */
    public int getMultiplicity()
    {
        return multiplicity;
    }

    /**
     * Устанавливает идиницы измерения
     *
     * @param units символы единицы измерения
     */
    public void setUnits(final String units)
    {
        this.units = units;
        invalidate();
    }

}
