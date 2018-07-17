package cn.chock.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.chock.adapter.BaseViewHolder;

import static cn.chock.view.BaseRefreshHeader.STATE_DONE;

/**
 * 可下拉刷新和上拉加载的RecyclerView
 * @author timpkins
 */
public class ExRecyclerView extends RecyclerView {
    private static final String TAG = ExRecyclerView.class.getSimpleName();
    private static final float DRAG_RATE = 3; // 拖拽难度，越大越难
    private static final int limitNumberToCallLoadMore = 3;  // 剩余多少条的时候调用 onLoadMore TODO: 2018-07-17 暂不开放接口进行修改
    private OnRefreshLoadListener onRefreshLoadListener; // 下拉刷新和上拉加载监听器
    private boolean enableRefresh = true; // 是否可下拉刷新
    private boolean enableLoading = true; // 是否可上拉加载
    private float lastY = -1; // Y轴初始值
    private WrapperAdapter wrapperAdapter; // adapter包装类
    private RecyclerHeaderView headerView; //下拉刷新View
    private RecyclerFooterView footerView; // 上拉加载View

    private boolean isLoadingData = false;
    private boolean isNoMore = false;
    private static final int TYPE_REFRESH_HEADER = 10000;//设置一个很大的数字,尽可能避免和用户的adapter冲突
    private static final int TYPE_FOOTER = 10001;
    private final AdapterDataObserver mDataObserver = new DataObserver();


    public ExRecyclerView(Context context) {
        this(context, null);
    }

    public ExRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        headerView = new RecyclerHeaderView(getContext());
        footerView = new RecyclerFooterView(getContext());
        footerView.setVisibility(GONE);
    }

    /**
     * 设置下拉刷新和上拉加载监听器
     * @param onRefreshLoadListener 下拉刷新和上拉加载监听器
     */
    public void setOnRefreshLoadListener(OnRefreshLoadListener onRefreshLoadListener) {
        this.onRefreshLoadListener = onRefreshLoadListener;
    }

    /**
     * 设置是否可下拉刷新
     * @param enableRefresh 是否可下拉刷新，true为可下拉刷新，false则不可下拉刷新
     */
    public void setEnableRefresh(boolean enableRefresh) {
        this.enableRefresh = enableRefresh;
    }

    /**
     * 设置是否可上拉加载
     * @param enableLoading 是否可上拉加载，true为可上拉加载，false则不可上拉加载
     */
    public void setEnableLoading(boolean enableLoading) {
        this.enableLoading = enableLoading;
        if (!enableLoading) {
            footerView.setState(RecyclerFooterView.STATE_COMPLETE);
        }
    }

    /**
     * call it when you finish the activity,
     * when you call this,better don't call some kind of functions like
     * RefreshHeader,because the reference of mHeaderViews is NULL.
     */
    public void destroy() {
        footerView.destroy();
        footerView = null;
        if (headerView != null) {
            headerView.destroy();
            headerView = null;
        }
    }

    public RecyclerHeaderView getDefaultRefreshHeaderView() {
        if (headerView == null) {
            return null;
        }
        return headerView;
    }

    public RecyclerFooterView getDefaultFootView() {
        return footerView;
    }

    public View getFooterView() {
        return footerView;
    }

    @SuppressWarnings("all")
    public void setFootView(@NonNull RecyclerFooterView view) {
        footerView = view;
    }

    public void loadMoreComplete() {
        isLoadingData = false;
        footerView.setState(RecyclerFooterView.STATE_COMPLETE);
    }

    public void setNoMore(boolean noMore) {
        isLoadingData = false;
        isNoMore = noMore;
        footerView.setState(isNoMore ? RecyclerFooterView.STATE_NOMORE : RecyclerFooterView.STATE_COMPLETE);
    }

    public void refresh() {
        if (enableRefresh && onRefreshLoadListener != null) {
            headerView.setState(RecyclerHeaderView.STATE_REFRESHING);
            onRefreshLoadListener.onRefresh();
        }
    }

    public void reset() {
        setNoMore(false);
        loadMoreComplete();
        refreshComplete();
    }

    public void refreshComplete() {
        if (headerView != null)
            headerView.refreshComplete();
        setNoMore(false);
    }

    public void setRefreshHeader(RecyclerHeaderView refreshHeader) {
        headerView = refreshHeader;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        wrapperAdapter = new WrapperAdapter(adapter);
        super.setAdapter(wrapperAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }

    //避免用户自己调用getAdapter() 引起的ClassCastException
    @Override
    public Adapter getAdapter() {
        if (wrapperAdapter != null)
            return wrapperAdapter.getOriginalAdapter();
        else
            return null;
    }


    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (wrapperAdapter != null) {
            if (layout instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) layout);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (wrapperAdapter.isFooter(position) || wrapperAdapter.isRefreshHeader(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }
    }

    /**
     * ===================== try to adjust the position for XR when you call those functions below ======================
     */
    // which cause "Called attach on a child which is not detached" exception info.
    // {reason analyze @link:http://www.cnblogs.com/linguanh/p/5348510.html}
    // by lgh on 2017-11-13 23:55

    // example: listData.remove(position); You can also see a demo on LinearActivity
    public <T> void notifyItemRemoved(List<T> listData, int position) {
        if (wrapperAdapter.adapter == null)
            return;
        int headerSize = getHeaders_includingRefreshCount();
        int adjPos = position + headerSize;
        wrapperAdapter.adapter.notifyItemRemoved(adjPos);
        wrapperAdapter.adapter.notifyItemRangeChanged(headerSize, listData.size(), new Object());
    }

    public <T> void notifyItemInserted(List<T> listData, int position) {
        if (wrapperAdapter.adapter == null)
            return;
        int headerSize = getHeaders_includingRefreshCount();
        int adjPos = position + headerSize;
        wrapperAdapter.adapter.notifyItemInserted(adjPos);
        wrapperAdapter.adapter.notifyItemRangeChanged(headerSize, listData.size(), new Object());
    }

    public void notifyItemChanged(int position) {
        if (wrapperAdapter.adapter == null)
            return;
        int adjPos = position + getHeaders_includingRefreshCount();
        wrapperAdapter.adapter.notifyItemChanged(adjPos);
    }

    public void notifyItemChanged(int position, Object o) {
        if (wrapperAdapter.adapter == null)
            return;
        int adjPos = position + getHeaders_includingRefreshCount();
        wrapperAdapter.adapter.notifyItemChanged(adjPos, o);
    }

    private int getHeaders_includingRefreshCount() {
        return wrapperAdapter.getHeadersCount() + 1;
    }

    /**
     * ======================================================= end =======================================================
     */

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && onRefreshLoadListener != null && !isLoadingData && enableLoading) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            int adjAdapterItemCount = layoutManager.getItemCount() + getHeaders_includingRefreshCount();

            int status = STATE_DONE;

            if (headerView != null)
                status = headerView.getState();
            if (
                    layoutManager.getChildCount() > 0
                            && lastVisibleItemPosition >= adjAdapterItemCount - limitNumberToCallLoadMore
                            && adjAdapterItemCount >= layoutManager.getChildCount()
                            && !isNoMore
                            && status < RecyclerHeaderView.STATE_REFRESHING
                    ) {
                isLoadingData = true;
                footerView.setState(RecyclerFooterView.STATE_LOADING);
                onRefreshLoadListener.onLoading();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (lastY == -1) {
            lastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - lastY;
                lastY = ev.getRawY();
                if (isOnTop() && enableRefresh) {
                    if (headerView == null)
                        break;
                    headerView.onMove(deltaY / DRAG_RATE);
                    if (headerView.getVisibleHeight() > 0 && headerView.getState() < RecyclerHeaderView.STATE_REFRESHING) {
                        return false;
                    }
                }
                break;
            default:
                lastY = -1; // reset
                if (isOnTop() && enableRefresh) {
                    if (headerView != null && headerView.releaseAction()) {
                        if (onRefreshLoadListener != null) {
                            onRefreshLoadListener.onRefresh();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private boolean isOnTop() {
        return headerView.getParent() != null;
    }

    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            if (wrapperAdapter != null) {
                wrapperAdapter.notifyDataSetChanged();
            }
            if (wrapperAdapter != null) {
                int emptyCount = 1 + wrapperAdapter.getHeadersCount();
                if (enableLoading) {
                    emptyCount++;
                }
                if (wrapperAdapter.getItemCount() == emptyCount) {
                    ExRecyclerView.this.setVisibility(View.GONE);
                } else {
                    ExRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            wrapperAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            wrapperAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            wrapperAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            wrapperAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            wrapperAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    /**
     * adapter包装类
     */
    private class WrapperAdapter extends Adapter<BaseViewHolder> {
        private Adapter adapter;

        WrapperAdapter(Adapter adapter) {
            this.adapter = adapter;
        }

        Adapter getOriginalAdapter() {
            return this.adapter;
        }

        public boolean isFooter(int position) {
            return enableLoading && position == getItemCount() - 1;
        }

        public boolean isRefreshHeader(int position) {
            return position == 0;
        }

        public int getHeadersCount() {
            return 0;
        }

        @NonNull
        @Override
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER) {
                return new BaseViewHolder(headerView);
            } else if (viewType == TYPE_FOOTER) {
                return new BaseViewHolder(footerView);
            }
            return (BaseViewHolder) adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
            if (isRefreshHeader(position)) {
                return;
            }
            int adjPosition = position - (getHeadersCount() + 1);
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    adapter.onBindViewHolder(holder, adjPosition);
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull BaseViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (isRefreshHeader(position)) {
                return;
            }

            int adjPosition = position - (getHeadersCount() + 1);
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    if (payloads.isEmpty()) {
                        adapter.onBindViewHolder(holder, adjPosition);
                    } else {
                        adapter.onBindViewHolder(holder, adjPosition, payloads);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            int adjLen = (enableLoading ? 2 : 1);
            if (adapter != null) {
                return getHeadersCount() + adapter.getItemCount() + adjLen;
            } else {
                return getHeadersCount() + adjLen;
            }
        }

        @Override
        public int getItemViewType(int position) {
            int adjPosition = position - (getHeadersCount() + 1);
            if (isRefreshHeader(position)) {
                return TYPE_REFRESH_HEADER;
            }
            if (isFooter(position)) {
                return TYPE_FOOTER;
            }
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    return adapter.getItemViewType(adjPosition);
                }
            }
            return 0;
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null && position >= getHeadersCount() + 1) {
                int adjPosition = position - (getHeadersCount() + 1);
                if (adjPosition < adapter.getItemCount()) {
                    return adapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isFooter(position) || isRefreshHeader(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onViewAttachedToWindow(@NonNull BaseViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null
                    && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && (isRefreshHeader(holder.getLayoutPosition()) || isFooter(holder.getLayoutPosition()))) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull BaseViewHolder holder) {
            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(@NonNull BaseViewHolder holder) {
            adapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(@NonNull BaseViewHolder holder) {
            return adapter.onFailedToRecycleView(holder);
        }

        @Override
        public void unregisterAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            adapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void registerAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            adapter.registerAdapterDataObserver(observer);
        }
    }

    /**
     * 下拉刷新和上拉加载监听
     */
    public interface OnRefreshLoadListener {

        /**
         * 下拉刷新
         */
        void onRefresh();

        /**
         * 上拉加载
         */
        void onLoading();
    }
}