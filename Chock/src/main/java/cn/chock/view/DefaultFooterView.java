package cn.chock.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import cn.chock.R;

public class DefaultFooterView extends RecyclerFooterView {
    private static final String TAG = DefaultFooterView.class.getSimpleName();
    private static final int CIRCLE_DURATION = 500;
    public int mMeasuredHeight;
    private ImageView ivArrow;
    private TextView tvHint;
    private Animation circleAnim;
    private static final int[] hints = {R.string.footer_normal, R.string.footer_release,
            R.string.footer_loading, R.string.footer_success, R.string.footer_failure, R.string.footer_nomore};

    public DefaultFooterView(Context context) {
        super(context);
    }

    public DefaultFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultFooterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DefaultFooterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        ivArrow.setImageResource(R.mipmap.ic_refresh);

        circleAnim = createAnimation(0f, 359f, Animation.INFINITE, CIRCLE_DURATION);
    }

    @Override
    public void setFooterState(int state) {
        tvHint.setText(hints[state]);
        ivArrow.setVisibility(VISIBLE);
        setVisibility(VISIBLE);
        switch (state) {
            case STATE_NORMAL:
                break;
            case STATE_RELEASE:
                break;
            case STATE_LOADING:
                ivArrow.startAnimation(circleAnim);
                break;
            case STATE_SUCCESS:
                setVisibility(GONE);
                break;
            case STATE_FAILURE:
                break;
            case STATE_NOMORE:
                ivArrow.setVisibility(GONE);
                break;
        }
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
