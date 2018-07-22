package cn.chock.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * {@link ExRecyclerView}上拉刷新View基类
 * @author timpkins
 */
public abstract class RecyclerHeaderView extends LinearLayout implements RecyclerCommon {

    @HeaderState
    protected int headerState = STATE_NORMAL;

    public RecyclerHeaderView(Context context) {
        super(context);
    }

    public RecyclerHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public RecyclerHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 设置下拉刷新View的状态{@link HeaderState}
     * @param state 下拉刷新View的状态{@link HeaderState}
     */
    protected abstract void setHeaderState(@HeaderState int state);

    /**
     * 获取下拉刷新View的状态{@link HeaderState}
     * @return 下拉刷新View的状态{@link HeaderState}
     */
    @HeaderState
    protected abstract int getHeaderState();

    /**
     * {@link ExRecyclerView}移动时的操作
     * @param deltaY Y轴移动的增量
     */
    protected abstract void onMove(float deltaY);

    /**
     * 当前是否处于刷新状态
     * @return true：处于刷新状态，false：不处于刷新状态
     */
    protected abstract boolean releaseAction();

}
