package cn.chock.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.chock.R;
import cn.chock.util.LogUtils;

public class RecyclerFooterView extends LinearLayout {
    private static final String TAG = RecyclerFooterView.class.getSimpleName();
    public final static int STATE_LOADING = 0;
    public final static int STATE_COMPLETE = 1;
    public final static int STATE_NOMORE = 2;
    private ProgressBar progressCon;
    private TextView mText;

    public RecyclerFooterView(Context context) {
        super(context);
        initView();
    }

    public RecyclerFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void destroy() {
        progressCon = null;
    }

    public void initView() {
        int footerHeight = getResources().getDimensionPixelOffset(R.dimen.recycler_footer_height);
        int width$height = getResources().getDimensionPixelOffset(R.dimen.recycler_footer_progress);
        RecyclerView.LayoutParams footerParams = new RecyclerView.LayoutParams(LayoutParams.MATCH_PARENT, footerHeight);
        footerParams.bottomMargin = (footerHeight - width$height) / 2;
        footerParams.topMargin = (footerHeight - width$height) / 2;
        LogUtils.e(TAG, "footerHeight = " + footerHeight + "   width$height = " + width$height + "    topMargin = " +
                ((footerHeight - width$height) / 2));
        setGravity(Gravity.CENTER);
        setLayoutParams(footerParams);

        progressCon = new ProgressBar(getContext());
        LayoutParams progressParams = new LayoutParams(width$height, width$height);
        progressParams.rightMargin = width$height / 2;
        progressCon.setLayoutParams(progressParams);
        addView(progressCon);

        mText = new TextView(getContext());
        mText.setText(R.string.listview_loading);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mText.setLayoutParams(layoutParams);
        addView(mText);
    }

    public void setState(int state) {
        switch (state) {
            case STATE_LOADING:
                progressCon.setVisibility(View.VISIBLE);
                mText.setText(R.string.listview_loading);
                this.setVisibility(View.VISIBLE);
                break;
            case STATE_COMPLETE:
                mText.setText(R.string.loading_done);
                this.setVisibility(View.GONE);
                break;
            case STATE_NOMORE:
                mText.setText(R.string.nomore_loading);
                progressCon.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
                break;
        }
    }
}
