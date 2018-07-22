package cn.chock.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import cn.chock.R;

/**
 * 默认下拉刷新View
 * @author timpkins
 */
public class DefaultHeaderView extends RecyclerHeaderView {
    private static final String TAG = DefaultHeaderView.class.getSimpleName();
    private static final int TURN_DURATION = 180;
    private static final int CIRCLE_DURATION = 500;
    private static final int[] hints = {R.string.header_normal, R.string.header_release,
            R.string.header_refresh, R.string.header_success, R.string.header_failure};
    private ImageView ivArrow;
    private TextView tvHint;
    private Animation upAnim, downAnim, circleAnim;
    public int mMeasuredHeight;

    public DefaultHeaderView(Context context) {
        super(context);
    }

    public DefaultHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DefaultHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        measure(WRAP_CONTENT, WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();

        ivArrow = findViewById(R.id.ivRecyclerArrow);
        tvHint = findViewById(R.id.tvRecyclerHint);

        upAnim = createAnimation(0.0f, -180.0f, 0, TURN_DURATION);
        downAnim = createAnimation(-180.0f, 0.0f, 0, TURN_DURATION);
        circleAnim = createAnimation(0f, 359f, Animation.INFINITE, CIRCLE_DURATION);
    }

    @Override
    public void setHeaderState(@HeaderState int state) {
        if (state == headerState) return;

        ivArrow.clearAnimation();
        ivArrow.setVisibility(VISIBLE);
        tvHint.setText(hints[state]);
        switch (state) {
            case STATE_NORMAL:
                ivArrow.setImageResource(R.mipmap.ic_arrow);
                if (headerState == STATE_RELEASE) {
                    ivArrow.startAnimation(downAnim);
                }
                break;
            case STATE_RELEASE:
                ivArrow.setImageResource(R.mipmap.ic_arrow);
                ivArrow.startAnimation(upAnim);
                break;
            case STATE_REFRESH:
                ivArrow.setImageResource(R.mipmap.ic_refresh);
                ivArrow.startAnimation(circleAnim);
                smoothScrollTo(mMeasuredHeight);
                break;
            case STATE_SUCCESS:
                ivArrow.setVisibility(INVISIBLE);
                postDelayed(() ->{
                    smoothScrollTo(0);
                    postDelayed(() -> setHeaderState(STATE_NORMAL), 500);
                }, 1000);
                break;
            case STATE_FAILURE:
                ivArrow.setVisibility(INVISIBLE);
                break;
        }
        headerState = state;
    }

    @Override
    @HeaderState
    public int getHeaderState() {
        return headerState;
    }

    @Override
    public void onMove(float deltaY) {
        if (getVisibleHeight() > 0 || deltaY > 0) {
            setVisibleHeight((int) deltaY + getVisibleHeight());
            if (headerState <= STATE_RELEASE) { // 未处于刷新状态，更新箭头
                if (getVisibleHeight() > mMeasuredHeight) {
                    setHeaderState(STATE_RELEASE);
                } else {
                    setHeaderState(STATE_NORMAL);
                }
            }
        }
    }

    @Override
    public boolean releaseAction() {
        boolean isOnRefresh = false;
        int height = getVisibleHeight();
        if (height == 0) // not visible.
            isOnRefresh = false;
        if (getVisibleHeight() > mMeasuredHeight && headerState < STATE_REFRESH) {
            setHeaderState(STATE_REFRESH);
            isOnRefresh = true;
        }
//        // refreshing and header isn't shown fully. do nothing.
//        if (headerState == STATE_REFRESH && height <= mMeasuredHeight) {
//            //return;
//        }
        if (headerState != STATE_REFRESH) {
            smoothScrollTo(0);
        }
        if (headerState == STATE_REFRESH) {
            int destHeight = mMeasuredHeight;
            smoothScrollTo(destHeight);
        }
        return isOnRefresh;
    }

    public int getVisibleHeight() {
        return getLayoutParams().height;
    }

    private void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) getLayoutParams();
        lp.height = height;
        setLayoutParams(lp);
    }

    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(animation -> setVisibleHeight((int) animation.getAnimatedValue()));
        animator.start();
    }

    private RotateAnimation createAnimation(float from, float to, int repeatCount, long duration) {
        RotateAnimation rotateAnimation = new RotateAnimation(from, to,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(duration);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(repeatCount);
        return rotateAnimation;
    }
}