package com.muzzley.util.ui.triStateToggle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;

import com.muzzley.R;

import timber.log.Timber;

/**
 * Custom switch button that have three states
 * Based on https://github.com/kyleduo/SwitchButton
 *
 * <p/>
 * Created by ruigoncalo on 09/09/15.
 */
public class TriStateToggle extends Button implements Statable {

    private int state;

    private static boolean SHOW_RECT = false;
    private Configuration configuration;

    /**
     * zone for thumb to move inside
     */
    private Rect safeZone;

    /**
     * zone for background
     */
    private Rect backZone;
    private Rect thumbZone;
    private RectF saveLayerZone;
    private int thumbWidth, thumbHeight;
    private float startX, startY, lastX;
    private int centerPos;

    private AnimationController mAnimationController;
    private SBAnimationListener mOnAnimateListener = new SBAnimationListener();
    private boolean isAnimating = false;

    private int mTouchSlop;
    private int mClickTimeout;
    private Paint mRectPaint;
    private Rect mBounds = null;
    private int undefDrawableAlpha;
    private OnStateChangeListener onStateChangeListener;

    public TriStateToggle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton);

        configuration.setThumbMarginInPixel(
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_thumb_margin,
                        configuration.getDefaultThumbMarginInPixel()));

        configuration.setThumbMarginInPixel(
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_thumb_marginTop, configuration.getThumbMarginTop()),
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_thumb_marginBottom, configuration.getThumbMarginBottom()),
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_thumb_marginLeft, configuration.getThumbMarginLeft()),
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_thumb_marginRight, configuration.getThumbMarginRight()));

        configuration.setRadius(typedArray.getInt(R.styleable.SwitchButton_radius, Configuration.Default.DEFAULT_RADIUS));

        configuration.setThumbWidthAndHeightInPixel(
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_thumb_width, -1),
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_thumb_height, -1));

        configuration.setMeasureFactor(typedArray.getFloat(R.styleable.SwitchButton_measureFactor, -1));

        configuration.setInsetBounds(
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_insetLeft, 0),
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_insetTop, 0),
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_insetRight, 0),
                typedArray.getDimensionPixelSize(R.styleable.SwitchButton_insetBottom, 0));

        int velocity = typedArray.getInteger(R.styleable.SwitchButton_animationVelocity, -1);
        mAnimationController.setVelocity(velocity);

        fetchDrawableFromAttr(typedArray);
        typedArray.recycle();
    }

    public TriStateToggle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TriStateToggle(Context context) {
        this(context, null);
    }

    class SBAnimationListener implements AnimationController.OnAnimateListener {

        @Override
        public void onAnimationStart() {
            isAnimating = true;
        }

        @Override
        public boolean continueAnimating(boolean direction, int from, int to) {

            if (direction) {
                // go right
                return thumbZone.left < to;
            } else {
                // going left
                return thumbZone.left > to;
            }
        }

        @Override
        public void onFrameUpdate(int frame) {
            moveThumb(frame);
            postInvalidate();
        }

        @Override
        public void onAnimateComplete() {
            setStateInClass(getStateBasedOnPos());
            isAnimating = false;
            //caan
            if (onStateChangeListener != null) {
                onStateChangeListener.onStateChanged(TriStateToggle.this, state);
            }

        }
    }

    private void initView() {
        configuration = Configuration.getDefault(getContext().getResources().getDisplayMetrics().density);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mClickTimeout = ViewConfiguration.getPressedStateDuration() + ViewConfiguration.getTapTimeout();
        mAnimationController = AnimationController.getDefault().init(mOnAnimateListener);
        mBounds = new Rect();
        if (SHOW_RECT) {
            mRectPaint = new Paint();
            mRectPaint.setStyle(Paint.Style.STROKE);
        }
    }

    /**
     * fetch drawable resources from attrs, drop them to conf, AFTER the size
     * has been confirmed
     *
     * @param typedArray to fecth resources
     */
    private void fetchDrawableFromAttr(TypedArray typedArray) {
        if (configuration == null) {
            return;
        }

        configuration.setOffDrawable(fetchDrawable(
                typedArray, R.styleable.SwitchButton_offDrawable,
                R.styleable.SwitchButton_offColor,
                Configuration.Default.DEFAULT_OFF_COLOR));

        configuration.setOnDrawable(fetchDrawable(
                typedArray, R.styleable.SwitchButton_onDrawable,
                R.styleable.SwitchButton_onColor,
                Configuration.Default.DEFAULT_ON_COLOR));

        configuration.setUndefDrawable(fetchDrawable(
                typedArray, R.styleable.SwitchButton_undefDrawable,
                R.styleable.SwitchButton_undefColor,
                Configuration.Default.DEFAULT_UNDEF_COLOR));

        configuration.setThumbDrawable(fetchDrawable(
                typedArray, R.styleable.SwitchButton_thumbDrawable,
                R.styleable.SwitchButton_thumbColor,
                Configuration.Default.DEFAULT_THUMB_COLOR));
    }


    private Drawable fetchDrawable(TypedArray typedArray, int attrId, int alterColorId, int defaultColor) {
        Drawable tempDrawable = typedArray.getDrawable(attrId);
        if (tempDrawable == null) {
            int tempColor = typedArray.getColor(alterColorId, defaultColor);
            tempDrawable = new GradientDrawable();
            ((GradientDrawable) tempDrawable).setCornerRadius(configuration.getRadius());
            ((GradientDrawable) tempDrawable).setColor(tempColor);
        }
        return tempDrawable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    private void setup() {
        setupBackZone();
        setupSafeZone();
        setupThumbZone();
        setupDrawableBounds();

        if (this.getMeasuredWidth() > 0 && this.getMeasuredHeight() > 0) {
            saveLayerZone = new RectF(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
        }

        ViewGroup parent = (ViewGroup) this.getParent();
        if (parent != null) {
            parent.setClipChildren(false);
        }
    }

    /**
     * setup zone for thumb to move
     */
    private void setupSafeZone() {
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        if (w > 0 && h > 0) {
            if (safeZone == null) {
                safeZone = new Rect();
            }
            int left, right, top, bottom;
            left = getPaddingLeft() + (configuration.getThumbMarginLeft() > 0 ? configuration.getThumbMarginLeft() : 0);
            right = w - getPaddingRight() - (configuration.getThumbMarginRight() > 0 ? configuration.getThumbMarginRight() : 0) + (-configuration.getShrinkX());
            top = getPaddingTop() + (configuration.getThumbMarginTop() > 0 ? configuration.getThumbMarginTop() : 0);
            bottom = h - getPaddingBottom() - (configuration.getThumbMarginBottom() > 0 ? configuration.getThumbMarginBottom() : 0) + (-configuration.getShrinkY());
            safeZone.set(left, top, right, bottom);
            centerPos = safeZone.left + (safeZone.right - safeZone.left) / 2;
        } else {
            safeZone = null;
        }
    }

    private void setupBackZone() {
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        if (w > 0 && h > 0) {
            if (backZone == null) {
                backZone = new Rect();
            }
            int left, right, top, bottom;
            left = getPaddingLeft() + (configuration.getThumbMarginLeft() > 0 ? 0 : -configuration.getThumbMarginLeft());
            right = w - getPaddingRight() - (configuration.getThumbMarginRight() > 0 ? 0 : -configuration.getThumbMarginRight()) + (-configuration.getShrinkX());
            top = getPaddingTop() + (configuration.getThumbMarginTop() > 0 ? 0 : -configuration.getThumbMarginTop());
            bottom = h - getPaddingBottom() - (configuration.getThumbMarginBottom() > 0 ? 0 : -configuration.getThumbMarginBottom()) + (-configuration.getShrinkY());
            backZone.set(left, top, right, bottom);
        } else {
            backZone = null;
        }
    }

    private int calculateLeftFromState(int state) {
        int result;
        switch (state) {
            case STATE_OFF: //off -> is on the left
                result = safeZone.left;
                break;

            case STATE_ON: //on -> is on the right
                result = safeZone.right - thumbWidth;
                break;

            default: // STATE_UNDEF
                result = centerPos - thumbWidth / 2;
        }

        return result;
    }

    private void setupThumbZone() {
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        if (w > 0 && h > 0) {
            if (thumbZone == null) {
                thumbZone = new Rect();
            }

            thumbWidth = configuration.getThumbWidth();
            thumbHeight = configuration.getThumbHeight();
            int left, right, top, bottom;
            left = calculateLeftFromState(state);
            right = left + thumbWidth;
            top = safeZone.top;
            bottom = top + thumbHeight;
            thumbZone.set(left, top, right, bottom);
        } else {
            thumbZone = null;
        }
    }

    private void setupDrawableBounds() {
        if (backZone != null) {
            configuration.getOnDrawable().setBounds(backZone);
            configuration.getOffDrawable().setBounds(backZone);
            configuration.getUndefDrawable().setBounds(backZone);
        }
        if (thumbZone != null) {
            configuration.getThumbDrawable().setBounds(thumbZone);
        }
    }

    private int measureWidth(int measureSpec) {
        int measuredWidth;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int minWidth = (int) (configuration.getThumbWidth() * configuration.getMeasureFactor() + getPaddingLeft() + getPaddingRight());
        int innerMarginWidth = configuration.getThumbMarginLeft() + configuration.getThumbMarginRight();
        if (innerMarginWidth > 0) {
            minWidth += innerMarginWidth;
        }

        if (specMode == MeasureSpec.EXACTLY) {
            measuredWidth = Math.max(specSize, minWidth);
        } else {
            measuredWidth = minWidth;
            if (specMode == MeasureSpec.AT_MOST) {
                measuredWidth = Math.min(specSize, minWidth);
            }
        }

        // bounds are negative numbers
        measuredWidth += (configuration.getInsetBounds().left + configuration.getInsetBounds().right);
        return measuredWidth;
    }

    private int measureHeight(int measureSpec) {
        int measuredHeight;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int minHeight = configuration.getThumbHeight() + getPaddingTop() + getPaddingBottom();
        int innerMarginHeight = configuration.getThumbMarginTop() + configuration.getThumbMarginBottom();

        if (innerMarginHeight > 0) {
            minHeight += innerMarginHeight;
        }

        if (specMode == MeasureSpec.EXACTLY) {
            measuredHeight = Math.max(specSize, minHeight);
        } else {
            measuredHeight = minHeight;
            if (specMode == MeasureSpec.AT_MOST) {
                measuredHeight = Math.min(specSize, minHeight);
            }
        }

        measuredHeight += (configuration.getInsetBounds().top + configuration.getInsetBounds().bottom);
        return measuredHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.getClipBounds(mBounds);
        if (mBounds != null && configuration.needShrink()) {
            mBounds.inset(configuration.getInsetX(), configuration.getInsetY());
            canvas.clipRect(mBounds, Region.Op.REPLACE);
            canvas.translate(configuration.getInsetBounds().left, configuration.getInsetBounds().top);
        }

        boolean useGeneralDisableEffect = !isEnabled() && this.notStatableDrawable();
        if (useGeneralDisableEffect) {
            canvas.saveLayerAlpha(
                    saveLayerZone, 255 / 2,
                    Canvas.ALL_SAVE_FLAG);
//                    Canvas.MATRIX_SAVE_FLAG
//                            | Canvas.CLIP_SAVE_FLAG
//                            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
//                            | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
//                            | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        }

        configuration.getOffDrawable().draw(canvas);
        configuration.getOnDrawable().setAlpha(calcAlpha());
        configuration.getOnDrawable().draw(canvas);
        configuration.getUndefDrawable().setAlpha(undefDrawableAlpha);
        configuration.getUndefDrawable().draw(canvas);
        configuration.getThumbDrawable().draw(canvas);

        if (useGeneralDisableEffect) {
            canvas.restore();
        }

        if (SHOW_RECT) {
            mRectPaint.setColor(Color.parseColor("#AA0000"));
            canvas.drawRect(backZone, mRectPaint);
            mRectPaint.setColor(Color.parseColor("#00FF00"));
            canvas.drawRect(safeZone, mRectPaint);
            mRectPaint.setColor(Color.parseColor("#0000FF"));
            canvas.drawRect(thumbZone, mRectPaint);
        }
    }

    private boolean notStatableDrawable() {
        boolean thumbStatable = (configuration.getThumbDrawable() instanceof StateListDrawable);
        boolean onStatable = (configuration.getOnDrawable() instanceof StateListDrawable);
        boolean offStatable = (configuration.getOffDrawable() instanceof StateListDrawable);
        boolean undefStatable = (configuration.getUndefDrawable() instanceof StateListDrawable);
        return !thumbStatable || !onStatable || !offStatable || !undefStatable;
    }

    /**
     * calculate the alpha value for on layer
     *
     * @return 0 ~ 255
     */
    private int calcAlpha() {
        int alpha = 255;
        if (safeZone == null || safeZone.right == safeZone.left) {

        } else {
            int backWidth = safeZone.right - thumbWidth - safeZone.left;
            if (backWidth > 0) {
                alpha = (thumbZone.left - safeZone.left) * 255 / backWidth;
            }
        }

        return alpha;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isAnimating || !isEnabled()) {
            return false;
        }
        int action = event.getAction();

        float deltaX = event.getX() - startX;
        float deltaY = event.getY() - startY;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                catchView();
                startX = event.getX();
                startY = event.getY();
                lastX = startX;
                setPressed(true);
                break;

            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                moveThumb((int) (x - lastX));
                lastX = x;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setPressed(false);

                int currentState = getStateBasedOnPos();
                if (currentState == STATE_UNDEF) {
                    currentState = STATE_ON;
                }

                float time = event.getEventTime() - event.getDownTime();
                if (deltaX < mTouchSlop && deltaY < mTouchSlop && time < mClickTimeout) {
                    performClick();
                } else {
                    slideToState(currentState);
                }

                break;

            default:
                break;
        }
        invalidate();
        return true;
    }

    /**
     * return the state based on position of thumb
     *
     * @return state on/off/undef
     */
    private int getStateBasedOnPos() {
        if (thumbZone.left > (centerPos - thumbWidth / 2)) {
            return STATE_ON;
        } else if (thumbZone.left < (centerPos - thumbWidth / 2)) {
            return STATE_OFF;
        } else {
            return STATE_UNDEF;
        }
    }

    /**
     * ON -> OFF; UNDEF -> ON; OFF -> ON
     *
     * @param currentState initial state
     * @return final state
     */
    private int getNextState(int currentState) {
        return currentState == 1 ? 0 : 1;
    }

    @Override
    public void invalidate() {
        if (mBounds != null && configuration.needShrink()) {
            mBounds.inset(configuration.getInsetX(), configuration.getInsetY());
            invalidate(mBounds);
        } else {
            super.invalidate();
        }
    }

    /**
     * Check performClick from class CompoundButton
     *
     * @return superclass boolean
     */
    @Override
    public boolean performClick() {
        toggle();
        //caan
//        if (onStateChangeListener != null) {
//            onStateChangeListener.onStateChanged(this, state);
//        }
        return super.performClick();
    }

    private void catchView() {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * @param state thumb position
     * @return delta
     */
    private int getDeltaFromState(int state) {
        int result;
        switch (state) {
            case STATE_OFF:
                result = -getMeasuredWidth();
                break;

            case STATE_ON:
                result = getMeasuredWidth();
                break;

            default:
                result = getMeasuredWidth() / 2 - thumbWidth / 2;
        }

        return result;
    }

    @Override
    public void setState(final int state) {
        if (thumbZone != null) {
            //moving without animation
            moveThumb(getDeltaFromState(state));
        }
//        Timber.d("TriState: thumbZone is " + thumbZone+", delta="+getDeltaFromState(state));
        setStateInClass(state);
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean animated) {
        int nextState = getNextState(state);
        if (animated) {
            slideToState(nextState);
        } else {
            setState(nextState);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        setDrawableState(configuration.getThumbDrawable());
        setDrawableState(configuration.getOnDrawable());
        setDrawableState(configuration.getOffDrawable());
        setDrawableState(configuration.getUndefDrawable());
    }

    private void setDrawableState(Drawable drawable) {
        if (drawable != null) {
            int[] myDrawableState = getDrawableState();
            drawable.setState(myDrawableState);
            invalidate();
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    private void setStateInClass(int state) {
        if (this.state == state) {
            return;
        }
        this.state = state;
        if (state == STATE_UNDEF) {
            undefDrawableAlpha = 255;
        } else {
            undefDrawableAlpha = 0;
        }

        refreshDrawableState();
        invalidate();

//        if (onStateChangeListener != null) {
//            onStateChangeListener.onStateChanged(this, state);
//        }
    }

    /**
     * If state is off or undef, go to on
     *
     * @param state state that will be reached
     */
    public void slideToState(int state) {
        if (isAnimating) {
            return;
        }
        int from = thumbZone.left;
        int to = getStateX(state);

        mAnimationController.startAnimation(from, to);
    }

    private int getStateX(int state) {
        int result;
        switch (state) {
            case STATE_ON:
                result = safeZone.right - thumbWidth;
                break;

            case STATE_OFF:
                result = safeZone.left;
                break;

            default:
                result = centerPos - thumbWidth/2;
        }

        return result;
    }

    private void moveThumb(int delta) {
        int newLeft = thumbZone.left + delta;
        int newRight = thumbZone.right + delta;
        if (newLeft < safeZone.left) {
            newLeft = safeZone.left;
            newRight = newLeft + thumbWidth;
        }
        if (newRight > safeZone.right) {
            newRight = safeZone.right;
            newLeft = newRight - thumbWidth;
        }

        moveThumbTo(newLeft, newRight);
    }

    private void moveThumbTo(int newLeft, int newRight) {
        thumbZone.set(newLeft, thumbZone.top, newRight, thumbZone.bottom);
        configuration.getThumbDrawable().setBounds(thumbZone);
    }
}