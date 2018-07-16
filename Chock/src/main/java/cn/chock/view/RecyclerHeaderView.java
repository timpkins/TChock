package cn.chock.view;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.chock.R;
import cn.chock.util.LogUtils;
import cn.chock.view.ExRecyclerView.RefreshState;

/**
 * {@link ExRecyclerView}下拉刷新的头部
 * @author timpkins
 */
public class RecyclerHeaderView extends LinearLayout {
    private static final String TAG = RecyclerHeaderView.class.getSimpleName();




    private int refreshThreshold;  // 刷新的阈值
    private ImageView ivArrow;
    private TextView tvHint;
    @RefreshState
    private int refreshState = ExRecyclerView.STATE_DEFAULT;

    public RecyclerHeaderView(Context context) {
        super(context);
        initHeaderView();
    }

    public RecyclerHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initHeaderView();
    }

    public RecyclerHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHeaderView();
    }

    @TargetApi(21)
    public RecyclerHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initHeaderView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ivArrow = findViewById(R.id.ivHeaderArrow);
        tvHint = findViewById(R.id.tvHeaderHint);

        ivArrow.setColorFilter(Color.parseColor("#3F51B5"));

        ivArrow.clearAnimation();
        ivArrow.setImageResource(R.mipmap.ic_arrow);
        tvHint.setText(getStateDesc(refreshState));
        setVisibleHeight(0);
    }

    private void initHeaderView() {
        LogUtils.e(TAG, "initHeaderView");
        measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        refreshThreshold = getResources().getDimensionPixelOffset(R.dimen.recycler_header_height);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        setBackgroundColor(Color.CYAN);
    }

    public int getRefreshState() {
        return refreshState;
    }

    public void setRefreshState(@RefreshState int refreshState) {
        LogUtils.e(TAG, "下拉刷新：当前状态 = " + getStateDesc(this.refreshState) + "    更改状态 = " + getStateDesc(refreshState));
        if (this.refreshState == refreshState) {
            return;
        }
        LogUtils.e(TAG, "==");
        switch (refreshState) {
            case ExRecyclerView.STATE_DEFAULT:
                ivArrow.clearAnimation();
                ivArrow.setVisibility(VISIBLE);
                if (this.refreshState == ExRecyclerView.STATE_RELEASE) {
                    rotateAnim(0, 180, 200, 0);
                    tvHint.setText(getStateDesc(refreshState));
                }
                break;
            case ExRecyclerView.STATE_RELEASE:
                if (this.refreshState == ExRecyclerView.STATE_DEFAULT) {
                    rotateAnim(0, 180, 200, 0);
                    tvHint.setText(getStateDesc(refreshState));
                }
                break;
            case ExRecyclerView.STATE_REFRESH:
                if (this.refreshState == ExRecyclerView.STATE_RELEASE) {
                    LogUtils.e(TAG, "STATE_REFRESH");
                    ivArrow.setImageResource(R.mipmap.ic_refresh);
                    rotateAnim(0, 359, 500, Animation.INFINITE);
                    autoScrollTo(refreshThreshold);
                    tvHint.setText(getStateDesc(refreshState));
                }
                break;
            case ExRecyclerView.STATE_SUCCESS:
                if (this.refreshState == ExRecyclerView.STATE_REFRESH) {
                    ivArrow.clearAnimation();
                    ivArrow.setVisibility(INVISIBLE);
                    ivArrow.setImageResource(R.mipmap.ic_arrow);
                    autoScrollTo(0);
                    tvHint.setText(getStateDesc(refreshState));
                }
                break;
            case ExRecyclerView.STATE_FAILUR:
                if (this.refreshState == ExRecyclerView.STATE_REFRESH) {
                    ivArrow.clearAnimation();
                    ivArrow.setVisibility(INVISIBLE);
                    ivArrow.setImageResource(R.mipmap.ic_arrow);
                    autoScrollTo(0);
                    tvHint.setText(getStateDesc(refreshState));
                }
                break;
        }
        this.refreshState = refreshState;
    }

    public void onMove(int deltaX, int deltaY) {
        LogUtils.e(TAG, "deltaY = " + deltaY + "  VisibleHeight = " + getVisibleHeight());
        setVisibleHeight(deltaY + getVisibleHeight());

        int visibleHeight = getVisibleHeight();
        if (visibleHeight >= refreshThreshold) {
            setRefreshState(ExRecyclerView.STATE_RELEASE);
        } else {
            setRefreshState(ExRecyclerView.STATE_DEFAULT);
//            autoScrollTo(0);
        }
    }

    private void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        try {
            LayoutParams lp = (LayoutParams) getLayoutParams();
            lp.height = height;
            setLayoutParams(lp);
        } catch (Exception e) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) getLayoutParams();
            lp.height = height;
            setLayoutParams(lp);
        }
    }

    private int getVisibleHeight() {
        try {
            LayoutParams lp = (LayoutParams) getLayoutParams();
            return lp.height;
        } catch (Exception e) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) getLayoutParams();
            return lp.height;
        }
    }

    /**
     * View以自身中心为圆心不停旋转
     * @param from 开始的角度
     * @param to 结束的角度
     * @param duration 动画持续时间
     * @param repeatCount 动画持续次数
     */
    private void rotateAnim(float from, float to, long duration, int repeatCount) {
        ivArrow.clearAnimation();
        ivArrow.setVisibility(VISIBLE);
        RotateAnimation animation = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(duration);
        animation.setRepeatCount(repeatCount);
        animation.setFillAfter(true);
        ivArrow.startAnimation(animation);
    }

    private void autoScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> setVisibleHeight((int) animation.getAnimatedValue()));
        animator.start();
    }

    private String getStateDesc(@RefreshState int refreshState) {
        String desc = "默认状态";
        switch (refreshState) {
            case ExRecyclerView.STATE_DEFAULT:
                desc = "默认状态";
                break;
            case ExRecyclerView.STATE_FAILUR:
                desc = "刷新失败";
                break;
            case ExRecyclerView.STATE_REFRESH:
                desc = "正在刷新";
                break;
            case ExRecyclerView.STATE_RELEASE:
                desc = "松开刷新";
                break;
            case ExRecyclerView.STATE_SUCCESS:
                desc = "刷新完成";
                break;
        }
        return desc;
    }
}
