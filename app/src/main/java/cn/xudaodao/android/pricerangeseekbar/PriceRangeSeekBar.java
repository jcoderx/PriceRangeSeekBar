package cn.xudaodao.android.pricerangeseekbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class PriceRangeSeekBar extends View {
    public interface LabelGenerator {
        String generateLabel(int originalLabel);
    }

    public interface OnRangeSelectedListener {
        void onRangeSelected(PriceRangeSeekBar PriceRangeSeekBar, int minValue, int maxValue);
    }

    private LabelGenerator mLabelGenerator;
    private OnRangeSelectedListener mOnRangeSelectedListener;
    private int mWidth;
    private int[] mLabels;

    private int mTrackHeight;
    private int mTrackUnselectedColor;
    private int mTrackSelectedColor;
    private Paint mTrackPaint;
    private int mTrackMargin;

    private Drawable mThumbDrawable;

    //左边滑块label字体大小
    private int mLabelMinTextSize;
    //右边滑块label字体大小
    private int mLabelMaxTextSize;

    private int mLabelNormalTextSize;
    private int mLabelPressedTextSize;
    private int mLabelTextColor;
    private Paint mMinLabelPaint;
    private Paint mMaxLabelPaint;
    private int mLabelTextMargin;

    private float mPartLength;

    private int mMinThumbIndex;
    private int mMaxThumbIndex;

    private Rect mMinThumbRect;
    private Rect mMaxThumbRect;

    public PriceRangeSeekBar(Context context) {
        this(context, null);
    }

    public PriceRangeSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PriceRangeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        initPaints();

        setWillNotDraw(false);
        setFocusable(true);
        setClickable(true);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        Resources resources = getResources();
        int trackHeight = resources.getDimensionPixelOffset(R.dimen.range_seek_bar_track_height_);
        int trackUnselectedColor = resources.getColor(R.color.range_seek_bar_track_unselected_color);
        int trackSelectedColor = resources.getColor(R.color.range_seek_bar_track_selected_color);
        Drawable thumbDrawable = resources.getDrawable(R.drawable.range_seek_bar_thumb_drawable);
        int trackMargin = resources.getDimensionPixelOffset(R.dimen.range_seek_bar_track_margin);
        int labelNormalTextSize = resources.getDimensionPixelOffset(R.dimen.range_seek_bar_label_normal_text_size);
        int labelPressedTextSize = resources.getDimensionPixelOffset(R.dimen.range_seek_bar_label_pressed_text_size);
        int labelTextColor = resources.getColor(R.color.range_seek_bar_label_text_color);
        int labelTextMargin = resources.getDimensionPixelOffset(R.dimen.range_seek_bar_label_margin);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PriceRangeSeekBar);
        mTrackHeight = a.getDimensionPixelOffset(R.styleable.PriceRangeSeekBar_track_height, trackHeight);
        mTrackUnselectedColor = a.getColor(R.styleable.PriceRangeSeekBar_track_unselected_color, trackUnselectedColor);
        mTrackSelectedColor = a.getColor(R.styleable.PriceRangeSeekBar_track_selected_color, trackSelectedColor);
        mThumbDrawable = a.getDrawable(R.styleable.PriceRangeSeekBar_thumb_drawable);
        if (mThumbDrawable == null) {
            mThumbDrawable = thumbDrawable;
        }
        mTrackMargin = a.getDimensionPixelOffset(R.styleable.PriceRangeSeekBar_track_margin, trackMargin);
        mLabelNormalTextSize = a.getDimensionPixelOffset(R.styleable.PriceRangeSeekBar_label_normal_text_size, labelNormalTextSize);
        mLabelPressedTextSize = a.getDimensionPixelOffset(R.styleable.PriceRangeSeekBar_label_pressed_text_size, labelPressedTextSize);
        mLabelTextColor = a.getColor(R.styleable.PriceRangeSeekBar_label_text_color, labelTextColor);
        mLabelTextMargin = a.getDimensionPixelOffset(R.styleable.PriceRangeSeekBar_label_text_margin, labelTextMargin);

        a.recycle();
    }

    private void initPaints() {
        mTrackPaint = new Paint();
        mTrackPaint.setAntiAlias(true);
        mTrackPaint.setStyle(Paint.Style.FILL);

        mMinLabelPaint = new Paint();
        mMinLabelPaint.setAntiAlias(true);
        mLabelMinTextSize = mLabelNormalTextSize;
        mMinLabelPaint.setTextSize(mLabelMinTextSize);
        mMinLabelPaint.setColor(mLabelTextColor);
        mMinLabelPaint.setTextAlign(Paint.Align.CENTER);
        mMinLabelPaint.setStyle(Paint.Style.FILL);

        mMaxLabelPaint = new Paint();
        mMaxLabelPaint.setAntiAlias(true);
        mLabelMaxTextSize = mLabelNormalTextSize;
        mMaxLabelPaint.setTextSize(mLabelMaxTextSize);
        mMaxLabelPaint.setColor(mLabelTextColor);
        mMaxLabelPaint.setTextAlign(Paint.Align.CENTER);
        mMaxLabelPaint.setStyle(Paint.Style.FILL);
    }

    private void initPartLength() {
        mPartLength = 0;
        if (mLabels != null && mLabels.length > 1) {
            mPartLength = (float) (mWidth - mThumbDrawable.getIntrinsicWidth()) / (mLabels.length - 1);
        }
    }

    private void setupMinThumbRect() {
        mMinThumbRect = new Rect();
        mMinThumbRect.left = (int) (mPartLength * mMinThumbIndex);
        mMinThumbRect.right = (int) (mPartLength * mMinThumbIndex + mThumbDrawable.getIntrinsicWidth());
        mMinThumbRect.top = mLabelPressedTextSize + mLabelTextMargin;
        mMinThumbRect.bottom = mMinThumbRect.top + mThumbDrawable.getIntrinsicHeight();
    }

    private void setupMaxThumbRect() {
        mMaxThumbRect = new Rect();
        mMaxThumbRect.left = (int) (mPartLength * mMaxThumbIndex);
        mMaxThumbRect.right = (int) (mPartLength * mMaxThumbIndex + mThumbDrawable.getIntrinsicWidth());
        mMaxThumbRect.top = mLabelPressedTextSize + mLabelTextMargin;
        mMaxThumbRect.bottom = mMaxThumbRect.top + mThumbDrawable.getIntrinsicHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mLabelPressedTextSize + mLabelTextMargin + mThumbDrawable.getIntrinsicHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        initPartLength();
        setupMinThumbRect();
        setupMaxThumbRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLabels != null && mLabels.length > 1 && mMinThumbIndex >= 0 && mMinThumbIndex < mLabels.length && mMaxThumbIndex >= 0
                && mMaxThumbIndex < mLabels.length && mMinThumbIndex < mMaxThumbIndex) {
            drawTrack(canvas);
            drawThumb(canvas);
            drawLabel(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                handleTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                handleTouchUp(event);
                break;
        }
        return super.onTouchEvent(event);

    }

    private void drawTrack(Canvas canvas) {
        mTrackPaint.setColor(mTrackUnselectedColor);

        RectF rectTrack = new RectF();
        rectTrack.left = mTrackMargin;
        rectTrack.right = getWidth() - mTrackMargin;
        rectTrack.top = mLabelPressedTextSize + mLabelTextMargin + (mThumbDrawable.getIntrinsicHeight() - mTrackHeight) / 2;
        rectTrack.bottom = rectTrack.top + mTrackHeight;

        canvas.drawRoundRect(rectTrack, 5, 5, mTrackPaint);

        mTrackPaint.setColor(mTrackSelectedColor);
        rectTrack.left = mMinThumbRect.left + mThumbDrawable.getIntrinsicWidth() / 2;
        rectTrack.right = mMaxThumbRect.left + mThumbDrawable.getIntrinsicWidth() / 2;

        canvas.drawRect(rectTrack, mTrackPaint);
    }

    private void drawThumb(Canvas canvas) {
        mThumbDrawable.setBounds(mMinThumbRect);
        mThumbDrawable.draw(canvas);

        mThumbDrawable.setBounds(mMaxThumbRect);
        mThumbDrawable.draw(canvas);
    }

    private void drawLabel(Canvas canvas) {
        int x = mMinThumbRect.left + mThumbDrawable.getIntrinsicWidth() / 2;
        int tmpMinThumbIndex = getIndexByX(x);
        float offset = getModByX(x);
        if (offset > mPartLength / 2) {
            tmpMinThumbIndex += 1;
        }

        if (tmpMinThumbIndex < 0 || tmpMinThumbIndex >= mLabels.length) {
            return;
        }
        String minLabel;
        if (mLabelGenerator != null) {
            minLabel = mLabelGenerator.generateLabel(mLabels[tmpMinThumbIndex]);
        } else {
            minLabel = String.valueOf(mLabels[tmpMinThumbIndex]);
        }
        mMinLabelPaint.setTextSize(mLabelMinTextSize);
        //精确计算label绘制位置
        Rect rect = new Rect();
        mMinLabelPaint.getTextBounds(minLabel, 0, minLabel.length(), rect);
        canvas.drawText(minLabel, x, mLabelPressedTextSize - rect.bottom, mMinLabelPaint);

        int maxLabelX = mMaxThumbRect.left + mThumbDrawable.getIntrinsicWidth() / 2;
        int tempMaxThumbIndex = getIndexByX(maxLabelX);
        float maxOffset = getModByX(maxLabelX);
        if (maxOffset >= mPartLength / 2) {
            tempMaxThumbIndex += 1;
        }
        if (tempMaxThumbIndex < 0 || tempMaxThumbIndex >= mLabels.length) {
            return;
        }
        String maxLabel;
        if (mLabelGenerator != null) {
            maxLabel = mLabelGenerator.generateLabel(mLabels[tempMaxThumbIndex]);
        } else {
            maxLabel = String.valueOf(mLabels[tempMaxThumbIndex]);
        }

        //精确计算label绘制位置
        mMaxLabelPaint.setTextSize(mLabelMaxTextSize);
        mMaxLabelPaint.getTextBounds(maxLabel, 0, maxLabel.length(), rect);
        canvas.drawText(maxLabel, maxLabelX, mLabelPressedTextSize - rect.bottom, mMaxLabelPaint);
    }

    public void setLabels(int[] labels, int minIndex, int maxIndex) {
        if (labels == null || labels.length <= 1) {
            throw new IllegalArgumentException("Length of labels must be greater than 1");
        }
        if (minIndex < 0 || minIndex > labels.length - 1) {
            throw new IllegalArgumentException("minIndex must be greater than 0 and less than length of lables");
        }
        if (maxIndex < 0 || maxIndex > labels.length - 1) {
            throw new IllegalArgumentException("maxIndex must be greater than 0 and less than length of lables");
        }
        if (minIndex >= maxIndex) {
            throw new IllegalArgumentException("maxIndex must be greater than minIndex");
        }
        mLabels = labels;
        mMinThumbIndex = minIndex;
        mMaxThumbIndex = maxIndex;
        initPartLength();
        setupMinThumbRect();
        setupMaxThumbRect();
        invalidate();
    }

    private boolean mMinThumbDown;
    private float mMinPointerLastX;
    private int mMinPointerId = -1;

    private boolean mMaxThumbDown;
    private float mMaxPointerLastX;
    private int mMaxPointerId = -1;

    private void handleTouchDown(MotionEvent event) {
        final int actionIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int downX = (int) event.getX(actionIndex);
        final int downY = (int) event.getY(actionIndex);
        if (mMinThumbRect.contains(downX, downY)) {
            if (mMinThumbDown) {
                return;
            }
            mMinPointerLastX = downX;
            // TODO state
            mMinPointerId = event.getPointerId(actionIndex);
            mMinThumbDown = true;
            // TODO invalidate
            // invalidate();

            zoomInTextSize(true);
        } else if (mMaxThumbRect.contains(downX, downY)) {
            if (mMaxThumbDown) {
                return;
            }
            mMaxPointerLastX = downX;
            mMaxPointerId = event.getPointerId(actionIndex);
            mMaxThumbDown = true;
            // TODO invalidate
            // invalidate();

            zoomInTextSize(false);
        }
    }

    private void handleTouchMove(MotionEvent event) {
        if (mMinThumbDown && mMinPointerId != -1) {
            final int index = event.findPointerIndex(mMinPointerId);
            final float x = event.getX(index);
            float deltaX = x - mMinPointerLastX;
            mMinPointerLastX = (int) x;

            if (deltaX <= 0 && mMinThumbRect.left + deltaX <= 0) {
                return;
            }
            if (deltaX >= 0 && (mMaxThumbRect.left - (mMinThumbRect.left + deltaX) <= mPartLength)) {
                return;
            }

            mMinThumbRect.left += deltaX;
            mMinThumbRect.right += deltaX;
            invalidate();
        }

        if (mMaxThumbDown && mMaxPointerId != -1) {
            final int index = event.findPointerIndex(mMaxPointerId);
            final float x = event.getX(index);
            float deltaX = x - mMaxPointerLastX;
            mMaxPointerLastX = (int) x;

            if (deltaX >= 0 && mMaxThumbRect.right + deltaX >= getWidth()) {
                return;
            }
            if (deltaX <= 0 && (mMaxThumbRect.left + deltaX - mMinThumbRect.left <= mPartLength)) {
                return;
            }

            mMaxThumbRect.left += deltaX;
            mMaxThumbRect.right += deltaX;
            invalidate();
        }
    }

    private void handleTouchUp(MotionEvent event) {
        final int actionIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int actionId = event.getPointerId(actionIndex);
        if (actionId == mMinPointerId) {
            if (!mMinThumbDown) {
                return;
            }

            int x = mMinThumbRect.left + mThumbDrawable.getIntrinsicWidth() / 2;
            mMinThumbIndex = getIndexByX(x);
            float offset = getModByX(x);
            if (offset > mPartLength / 2) {
                mMinThumbIndex += 1;
            }

            //setupMinThumbRect();
            //invalidate();
            //滑动滑块使用动画
            moveMinThumb();
            zoomOutTextSize(true);

            mMinThumbDown = false;
            mMinPointerLastX = 0;
            mMinPointerId = -1;
            if (mOnRangeSelectedListener != null) {
                mOnRangeSelectedListener.onRangeSelected(this, mLabels[mMinThumbIndex], mLabels[mMaxThumbIndex]);
            }
        } else if (actionId == mMaxPointerId) {
            if (!mMaxThumbDown) {
                return;
            }
            int x = mMaxThumbRect.left + mThumbDrawable.getIntrinsicWidth() / 2;
            mMaxThumbIndex = getIndexByX(x);
            float offset = getModByX(x);
            if (offset >= mPartLength / 2) {
                mMaxThumbIndex += 1;
            }

            //setupMaxThumbRect();
            //invalidate();
            //滑动滑块使用动画
            moveMaxThumb();
            zoomOutTextSize(false);

            mMaxThumbDown = false;
            mMaxPointerLastX = 0;
            mMaxPointerId = -1;
            if (mOnRangeSelectedListener != null) {
                mOnRangeSelectedListener.onRangeSelected(this, mLabels[mMinThumbIndex], mLabels[mMaxThumbIndex]);
            }
        }
    }

    private int getIndexByX(float x) {
        return (int) ((x - mThumbDrawable.getIntrinsicHeight() / 2) / mPartLength);
    }

    private float getModByX(float x) {
        return x - mThumbDrawable.getIntrinsicWidth() / 2 - mPartLength * getIndexByX(x);
    }

    private void moveMinThumb() {
        ValueAnimator animator = ValueAnimator.ofInt(mMinThumbRect.left, (int) (mPartLength * mMinThumbIndex));
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mMinThumbRect.left = (int) valueAnimator.getAnimatedValue();
                mMinThumbRect.right = mMinThumbRect.left + mThumbDrawable.getIntrinsicWidth();
                invalidate();
            }
        });
        animator.start();
    }

    private void moveMaxThumb() {
        ValueAnimator animator = ValueAnimator.ofInt(mMaxThumbRect.left, (int) (mPartLength * mMaxThumbIndex));
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mMaxThumbRect.left = (int) valueAnimator.getAnimatedValue();
                mMaxThumbRect.right = mMaxThumbRect.left + mThumbDrawable.getIntrinsicWidth();
                invalidate();
            }
        });
        animator.start();
    }

    /**
     * 放大字体
     */
    private void zoomInTextSize(final boolean min) {
        ValueAnimator animator = ValueAnimator.ofInt(mLabelNormalTextSize, mLabelPressedTextSize);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (min) {
                    //左边滑块
                    mLabelMinTextSize = (int) valueAnimator.getAnimatedValue();
                } else {
                    //右边滑块
                    mLabelMaxTextSize = (int) valueAnimator.getAnimatedValue();
                }
                invalidate();
            }
        });
        animator.start();
    }

    /**
     * 缩小字体
     */
    private void zoomOutTextSize(final boolean min) {
        ValueAnimator animator = ValueAnimator.ofInt(mLabelPressedTextSize, mLabelNormalTextSize);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (min) {
                    //左边滑块
                    mLabelMinTextSize = (int) valueAnimator.getAnimatedValue();
                } else {
                    //右边滑块
                    mLabelMaxTextSize = (int) valueAnimator.getAnimatedValue();
                }
                invalidate();
            }
        });
        animator.start();
    }

    public int getMinValue() {
        return mLabels[mMinThumbIndex];
    }

    public int getMaxValue() {
        return mLabels[mMaxThumbIndex];
    }

    public void setLabelGenerator(LabelGenerator labelGenerator) {
        mLabelGenerator = labelGenerator;
    }

    public void setOnRangeSelectedListener(OnRangeSelectedListener onRangeSelectedListener) {
        mOnRangeSelectedListener = onRangeSelectedListener;
    }
}
