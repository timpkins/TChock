package cn.chock.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * {@link ExRecyclerView} 上拉加载View
 * @author timpkins
 */
public abstract class RecyclerFooterView extends LinearLayout implements RecyclerCommon {

    public RecyclerFooterView(Context context) {
        super(context);
    }

    public RecyclerFooterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerFooterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public RecyclerFooterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 设置上拉加载View的状态
     * @param state 上拉加载View的状态
     */
    protected abstract void setFooterState(@FooterState int state);
}
