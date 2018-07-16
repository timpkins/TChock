package cn.chock.adapter;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

/**
 * {@link RecyclerView}万能ViewHolder，实现Item布局中View的获取和赋值
 * @author timpkins
 */
public class BaseViewHolder extends ViewHolder {

    private SparseArray<View> views;

    public BaseViewHolder(View itemView) {
        super(itemView);
        views = new SparseArray<>();
    }

    /**
     * 根据ID获取对应的View
     * @param resId View的ID
     * @param <T> View对应的类型
     * @return View的子类
     */
    public <T extends View> T getView(@IdRes int resId) {
        View view = views.get(resId);
        if (view == null) {
            view = itemView.findViewById(resId);
            views.put(resId, view);
        }
        return (T) view;
    }

    /**
     * 设置TextView的内容
     * @param viewId TextView的ID
     * @param text TextView的内容
     */
    public void setText(int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
    }
}
