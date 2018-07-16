package cn.chock.view;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import cn.chock.R;
import cn.chock.adapter.BaseViewHolder;
import cn.chock.util.LogUtils;

/**
 * 可下拉刷新和上拉加载的RecyclerView
 * @author timpkins
 */
public class ExRecyclerView extends RecyclerView {
    private static final String TAG = ExRecyclerView.class.getSimpleName();
    public static final int STATE_DEFAULT = 0x01;
    public static final int STATE_RELEASE = 0x02;
    public static final int STATE_REFRESH = 0x03;
    public static final int STATE_SUCCESS = 0x04;
    public static final int STATE_FAILUR = 0x05;

    @IntDef({STATE_DEFAULT, STATE_RELEASE, STATE_REFRESH, STATE_SUCCESS, STATE_FAILUR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RefreshState {
    }

    private boolean enableRefresh = true; // 默认可以下拉刷新
    private RecyclerHeaderView headerView; // 下拉刷新View
    private int mRefreshFinalMoveOffset = 0;
    private int lastX, lastY;
    private AdapterWrapper adapterWrapper;
    private final AdapterDataObserver mDataObserver = new DataObserver();

    public ExRecyclerView(Context context) {
        super(context);
        initExRecyclerView();
    }

    public ExRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initExRecyclerView();
    }

    public ExRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initExRecyclerView();
    }

    private void initExRecyclerView() {
        headerView = (RecyclerHeaderView) View.inflate(getContext(), R.layout.recycler_header_view, null);
    }

    public void refreshSuccess() {
        headerView.setRefreshState(STATE_SUCCESS);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (headerView.getMeasuredHeight() > mRefreshFinalMoveOffset) {
            mRefreshFinalMoveOffset = 0;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) e.getRawX();
                lastY = (int) e.getRawY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                LogUtils.e(TAG, "MotionEvent.ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                LogUtils.e(TAG, "MotionEvent.ACTION_POINTER_UP");
                break;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogUtils.e(TAG, "onTouchEvent MotionEvent.ACTION_DOWN");
                lastX = (int) e.getRawX();
                lastY = (int) e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
//                LogUtils.e(TAG, "onTouchEvent MotionEvent.ACTION_MOVE");
                int tempX = (int) e.getRawX();
                int tempY = (int) e.getRawY();
                int deltaX = (tempX - lastX) / 2;
                int deltaY = (tempY - lastY) / 2;
                lastX = tempX;
                lastY = tempY;
                if (enableRefresh && headerView.getRefreshState() < STATE_REFRESH) {
                    headerView.onMove(deltaX, deltaY);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                LogUtils.e(TAG, "onTouchEvent MotionEvent.ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                LogUtils.e(TAG, "onTouchEvent MotionEvent.ACTION_POINTER_UP");
                break;
            case MotionEvent.ACTION_UP:
                if (enableRefresh && headerView.getRefreshState() == STATE_RELEASE) {
                    LogUtils.e(TAG, "onTouchEvent MotionEvent.ACTION_UP");
                    headerView.setRefreshState(STATE_REFRESH);
                    postDelayed(this::refreshSuccess, 3000);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                LogUtils.e(TAG, "onTouchEvent MotionEvent.ACTION_CANCEL");
                break;
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        adapterWrapper = new AdapterWrapper(adapter);
        super.setAdapter(adapterWrapper);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }

    private class AdapterWrapper extends Adapter<ViewHolder> {
        private static final int TYPE_HEADER = 0xF1; // 头部布局类型
        private static final int TYPE_FOOTER = 0xF2; // 底部布局类型
        @NonNull
        private Adapter adapter;

        private AdapterWrapper(@NonNull Adapter adapter) {
            this.adapter = adapter;
        }

        private Adapter getOriginalAdapter() {
            return adapter;
        }

        private boolean isFooterView(int position) {
            return /*loadMoreEnable &&*/ position == getItemCount() - 1;
        }

        private boolean isHeaderView(int position) {
            return position == 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (isHeaderView(position)) {
                return TYPE_HEADER;
            } else if (isFooterView(position)) {
                return TYPE_FOOTER;
            }
            int adjPosition = position - 1;
            int adapterCount = adapter.getItemCount();
            if (adjPosition < adapterCount) {
                return adapter.getItemViewType(adjPosition);
            }
            return 0;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new BaseViewHolder(headerView);
            } else if (viewType == TYPE_FOOTER) {
                return new BaseViewHolder(headerView); // TODO: 2018-07-16  
            }
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            this.onBindViewHolder(holder, position, new ArrayList<>());
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (isHeaderView(position)) {
                return;
            }
            int adjPosition = position - 1;
            int adapterCount = adapter.getItemCount();
            if (adjPosition < adapterCount) {
                if (payloads.isEmpty()) {
                    adapter.onBindViewHolder(holder, adjPosition);
                } else {
                    adapter.onBindViewHolder(holder, adjPosition, payloads);
                }
            }
        }

        @Override
        public int getItemCount() {
//            int adjLen = (loadMoreEnable ? 2 : 1); // TODO: 2018-07-16
            int adjLen = 2;
            return adapter.getItemCount() + adjLen;
        }

        @Override
        public long getItemId(int position) {
            if (position >= 1) {
                int adjPosition = position - 1;
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
        public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
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
        public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            adapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
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

    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            if (adapterWrapper != null) {
                adapterWrapper.notifyDataSetChanged();
                ExRecyclerView.this.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            adapterWrapper.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            adapterWrapper.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            adapterWrapper.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            adapterWrapper.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            adapterWrapper.notifyItemMoved(fromPosition, toPosition);
        }
    }
}
