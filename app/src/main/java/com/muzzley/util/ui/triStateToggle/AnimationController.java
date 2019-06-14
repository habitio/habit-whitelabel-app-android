package com.muzzley.util.ui.triStateToggle;
import android.os.Handler;
import android.os.Message;

/**
 * controller of view animation
 * Based on https://github.com/kyleduo/SwitchButton
 *
 * <p/>
 * Created by ruigoncalo on 09/09/15.
 */
class AnimationController {

    private static int ANI_WHAT = 0x100;
    private static int DEFAULT_VELOCITY = 7;
    private static int DEFAULT_FRAME_DURATION = 1000 / 60;

    private AnimationHandler mHandler;
    private OnAnimateListener mOnAnimateListener;
    private boolean isAnimating = false;

    private int mFrame, mFrom, mTo;
    private int mVelocity = DEFAULT_VELOCITY;

    private AnimationController() {
        mHandler = new AnimationHandler();
    }

    /**
     * get default AnimationController
     *
     * @return
     */
    static AnimationController getDefault() {
        return new AnimationController();
    }

    /**
     * initial an AnimationController with a listener
     *
     * @param onAnimateListener NOT NULL
     * @return
     */
    AnimationController init(OnAnimateListener onAnimateListener) {
        if (onAnimateListener == null) {
            throw new IllegalArgumentException("onAnimateListener can not be null");
        }
        this.mOnAnimateListener = onAnimateListener;
        return this;
    }

    private static class AnimationHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ANI_WHAT) {
                if (msg.obj != null) {
                    ((Runnable) msg.obj).run();
                }
            }
        }
    }

    void startAnimation(int from, int to) {
        boolean direction;
        this.isAnimating = true;
        this.mFrom = from;
        this.mTo = to;
        this.mFrame = mVelocity;
        if (mTo > mFrom) {
            this.mFrame = Math.abs(this.mVelocity);
            direction = true;
        } else if (mTo < mFrom) {
            this.mFrame = -Math.abs(this.mVelocity);
            direction = false;
        } else {
            this.isAnimating = false;
            this.mOnAnimateListener.onAnimateComplete();
            return;
        }
        this.mOnAnimateListener.onAnimationStart();
        new RequireNextFrame(direction, from, to).run();
    }

    void stopAnimation() {
        isAnimating = false;
    }

    /**
     * configure the velocity of animation
     *
     * @param velocity a positive number
     */
    public void setVelocity(int velocity) {
        if (velocity <= 0) {
            mVelocity = DEFAULT_VELOCITY;
            return;
        }
        mVelocity = velocity;
    }

    /**
     * calculate next frame in child thread
     *
     * @author kyleduo
     */
    class RequireNextFrame implements Runnable {

        private int from;
        private int to;
        private boolean direction;
        private int interpolator;

        public RequireNextFrame(boolean direction, int from, int to){
            this.direction = direction;
            this.to = to;
            this.from = from;
        }

        @Override
        public void run() {
            if (!isAnimating) {
                return;
            }

            calcNextFrame();
            mOnAnimateListener.onFrameUpdate(mFrame);
            if (mOnAnimateListener.continueAnimating(direction, from, to)) {
                requireNextFrame();
            } else {
                stopAnimation();
                mOnAnimateListener.onAnimateComplete();
                return;
            }
        }

        private void calcNextFrame() {
            if(direction) {
                interpolator = (to - from) / mFrame;
                if(interpolator == 1){
                    if(mFrame > 1) {
                        mFrame = mFrame / 2;
                    }
                }
                from += mFrame;
            } else {
                // mFrame is negative
                int positiveFrame = mFrame * -1;
                interpolator = (from - to) / positiveFrame;
                if(interpolator == 1){
                    if(positiveFrame > 1){
                        positiveFrame = positiveFrame / 2;
                    }
                }
                from -= positiveFrame;
                mFrame = -positiveFrame;
            }
        }

        private void requireNextFrame() {
            Message msg = mHandler.obtainMessage();
            msg.what = ANI_WHAT;
            msg.obj = this;
            mHandler.sendMessageDelayed(msg, DEFAULT_FRAME_DURATION);
        }
    }

    /**
     * interface for view animation
     *
     * @author kyle
     */
    interface  OnAnimateListener {
        /**
         * invoked when the animation start
         */
        void onAnimationStart();

        /**
         * ask view whether continue Animating
         *
         * @return boolean true for continueAnimating
         */
        boolean continueAnimating(boolean direction, int from, int to);

        /**
         * a new frame is ready.
         *
         * @param frame next step of the animation, for linear animation, it is equal to velocity
         */
        void onFrameUpdate(int frame);

        /**
         * invoked when the animation complete
         */
        void onAnimateComplete();
    }
}
