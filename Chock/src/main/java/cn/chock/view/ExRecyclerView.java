package cn.chock.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.chock.R;
import cn.chock.adapter.BaseViewHolder;


/**
 * 可下拉刷新和上拉加载的RecyclerView
 * @author timpkins
 */
public class ExRecyclerView extends RecyclerView {
    private static final String TAG = ExRecyclerView.class.getSimpleName();
    private static final float DRAG_RATE = 3; // 拖拽难度，越大越难
    private static final int HEADER_VIEW_SIZE = 1;
    private static final int FOOTER_VIEW_SIZE = 1;
    private static final int limitNumberToCallLoadMore = 3;  // 剩余多少条的时候调用 onLoadMore TODO: 2018-07-17 暂不开放接口进行修改
    private OnRefreshLoadListener onRefreshLoadListener; // 下拉刷新和上拉加载监听器
    private boolean enableRefresh = true; // 是否可下拉刷新
    private boolean enableLoading = true; // 是否可上拉加载
    private float lastY = -1; // Y轴初始值
    private WrapperAdapter wrapperAdapter; // adapter包装类
    private RecyclerHeaderView headerView; //下拉刷新View
    private RecyclerFooterView footerView; // 上拉加载View

    private boolean isLoadingData = false;
    private final AdapterDataObserver mDataObserver = new DataObserver();

    public ExRecyclerView(Context context) {
        super(context);
        initExRecylerView();
    }

    public ExRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initExRecylerView();
    }

    public ExRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initExRecylerView();
    }

    private void initExRecylerView() {
        headerView = (DefaultHeaderView) View.inflate(getContext(), R.layout.default_header_view, null);
        footerView = (DefaultFooterView) View.inflate(getContext(), R.layout.default_footer_view, null);
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
            footerView.setFooterState(RecyclerCommon.STATE_SUCCESS);
        }
    }

    public <HEADER extends RecyclerHeaderView> HEADER getHeaderView() {
        return (HEADER) headerView;
    }

    public void setHeaderView(@NonNull RecyclerHeaderView headerView) {
        this.headerView = headerView;
    }

    public <FOOTER extends DefaultFooterView> FOOTER getFooterView() {
        return (FOOTER) footerView;
    }

    public void setFooterView(@NonNull DefaultFooterView footerView) {
        this.footerView = footerView;
    }

    //region 刷新和加载状态控制
    public void onRefreshStart() {
        if (enableRefresh && onRefreshLoadListener != null) {
            headerView.setHeaderState(RecyclerCommon.STATE_REFRESH);
            onRefreshLoadListener.onRefresh();
        }
    }

    public void onRefreshComplete() {
        headerView.setHeaderState(RecyclerCommon.STATE_SUCCESS);
        onLoadingComplete();
    }

    public void onRefreshError() {
        headerView.setHeaderState(RecyclerCommon.STATE_FAILURE);
        onLoadingComplete();
    }

    public void onLoadingComplete() {
        isLoadingData = false;
        footerView.setFooterState(RecyclerCommon.STATE_SUCCESS);
    }

    public void onLoadingError() {
        isLoadingData = false;
        footerView.setFooterState(RecyclerCommon.STATE_FAILURE);
    }

    public void onLoadingNoMore() {
        isLoadingData = false;
        footerView.setFooterState(RecyclerCommon.STATE_NOMORE);
    }

    public void reset() {
        onLoadingComplete();
        onRefreshComplete();
    }

    //endregion

    @Override
    public void setAdapter(Adapter adapter) {
        wrapperAdapter = new WrapperAdapter(adapter);
        super.setAdapter(wrapperAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }

    @Override
    public Adapter getAdapter() {
        return wrapperAdapter != null ? wrapperAdapter.getAdapter() : null;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (wrapperAdapter != null && layout instanceof GridLayoutManager) {
            GridLayoutManager gridManager = (GridLayoutManager) layout;
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (wrapperAdapter.isFooterView(position) || wrapperAdapter.isHeaderView(position))
                            ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    //===== try to adjust the position for ExRecyclerView when you call those functions below =====
    public <T> void notifyItemRemoved(List<T> listData, int position) {
        if (wrapperAdapter == null) {
            return;
        }
        int adjPos = position + HEADER_VIEW_SIZE;
        wrapperAdapter.adapter.notifyItemRemoved(adjPos);
        wrapperAdapter.adapter.notifyItemRangeChanged(HEADER_VIEW_SIZE, listData.size(), new Object());
    }

    public <T> void notifyItemInserted(List<T> listData, int position) {
        if (wrapperAdapter == null) {
            return;
        }
        int adjPos = position + HEADER_VIEW_SIZE;
        wrapperAdapter.adapter.notifyItemInserted(adjPos);
        wrapperAdapter.adapter.notifyItemRangeChanged(HEADER_VIEW_SIZE, listData.size(), new Object());
    }

    public void notifyItemChanged(int position) {
        if (wrapperAdapter == null) {
            return;
        }
        int adjPos = position + HEADER_VIEW_SIZE;
        wrapperAdapter.adapter.notifyItemChanged(adjPos);
    }

    public void notifyItemChanged(int position, Object o) {
        if (wrapperAdapter == null) {
            return;
        }
        int adjPos = position + HEADER_VIEW_SIZE;
        wrapperAdapter.adapter.notifyItemChanged(adjPos, o);
    }
    // ===== end =====

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
            int adjAdapterItemCount = layoutManager.getItemCount() + 1;
            int status = headerView.getHeaderState();
            if (layoutManager.getChildCount() > 0 && lastVisibleItemPosition >= adjAdapterItemCount - limitNumberToCallLoadMore
                    && adjAdapterItemCount >= layoutManager.getChildCount() && status < RecyclerCommon.STATE_REFRESH) {
                isLoadingData = true;
                footerView.setFooterState(RecyclerCommon.STATE_LOADING);
                onRefreshLoadListener.onLoading();
            }
        }
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

//    onInterceptTouchEvent

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchEvent(e);
                break;
        }
        return super.onInterceptTouchEvent(e);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - lastY;
                lastY = ev.getRawY();
                if (enableRefresh && headerView.getParent() != null) {
                    headerView.onMove(deltaY / DRAG_RATE);
                    //必须daltaY > 0，否则下拉无法响应
                    if (deltaY > 0 && headerView.getHeaderState() < RecyclerCommon.STATE_REFRESH) {
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (enableRefresh && headerView.getParent() != null && headerView.releaseAction() && onRefreshLoadListener != null) {
                    onRefreshLoadListener.onRefresh();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            if (wrapperAdapter != null) {
                wrapperAdapter.notifyDataSetChanged();
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
        private static final int TYPE_HEADER = 0xF1;
        private static final int TYPE_FOOTER = 0xF2;
        private Adapter adapter;

        WrapperAdapter(@NonNull Adapter adapter) {
            this.adapter = adapter;
        }

        Adapter getAdapter() {
            return this.adapter;
        }

        public boolean isFooterView(int position) {
            return enableLoading && position == getItemCount() - 1;
        }

        public boolean isHeaderView(int position) {
            return position == 0;
        }

        @NonNull
        @Override
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new BaseViewHolder(headerView);
            } else if (viewType == TYPE_FOOTER) {
                return new BaseViewHolder(footerView);
            }
            return (BaseViewHolder) adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
            onBindViewHolder(holder, position, new ArrayList<>());
        }

        @Override
        public void onBindViewHolder(@NonNull BaseViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (!isHeaderView(position)) {
                int adjPosition = position - HEADER_VIEW_SIZE;
                if (adapter != null && adjPosition < adapter.getItemCount()) {
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
            int itemCount = (enableLoading ? HEADER_VIEW_SIZE + FOOTER_VIEW_SIZE : HEADER_VIEW_SIZE);
            if (adapter != null) {
                itemCount += adapter.getItemCount();
            }
            return itemCount;
        }

        @Override
        public int getItemViewType(int position) {
            int adjPosition = position - HEADER_VIEW_SIZE;
            if (isHeaderView(position)) {
                return TYPE_HEADER;
            }
            if (isFooterView(position)) {
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
            if (adapter != null && position >= 1) {
                int adjPosition = position - HEADER_VIEW_SIZE;
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
                        return (isFooterView(position) || isHeaderView(position))
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
                    && (isHeaderView(holder.getLayoutPosition()) || isFooterView(holder.getLayoutPosition()))) {
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