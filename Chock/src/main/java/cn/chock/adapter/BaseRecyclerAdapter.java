package cn.chock.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.chock.bean.BaseBean;

/**
 * {@link RecyclerView}数据适配器，暂仅支持同一类数据类型
 * @author timpkins
 */
public abstract class BaseRecyclerAdapter<VH extends BaseViewHolder, D extends BaseBean> extends Adapter<VH> {
    private List<D> datas; // 存储列表数据，永不为null
    @LayoutRes
    private int layoutId;
    private LayoutInflater inflater;
    private OnItemClickListener itemClickListener;
    private OnItemLongClickLister itemLongClickListener;

    public BaseRecyclerAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        layoutId = setLayoutRes();
        datas = new ArrayList<>();
    }

    @LayoutRes
    protected abstract int setLayoutRes();

    public void setItemClickListener(@NonNull OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(@NonNull OnItemLongClickLister itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(layoutId, parent, false);
        return (VH) new BaseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        onBindViewHolder(holder, position, datas.get(position));
        if (itemClickListener != null) {
            holder.itemView.setOnClickListener(view -> itemClickListener.onItemClickListener(view, position,
                    datas.get(position)));
        }
        if (itemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(view -> {
                itemLongClickListener.onItemLongClickListener(view, position, datas.get(position));
                return true;
            });
        }
    }

    protected abstract void onBindViewHolder(@NonNull VH holder, int position, @NonNull D d);

    public boolean isEmpty() {
        return datas.isEmpty();
    }

    public boolean add(D d) {
        return datas.add(d);
    }

    public boolean remove(D d) {
        return datas.remove(d);
    }


    public boolean addAll(@NonNull List<D> lists) {
        return datas.addAll(lists);
    }

    public boolean addAll(int index, @NonNull List<D> lists) {
        return datas.addAll(index, lists);
    }

    public boolean removeAll(@NonNull List<D> lists) {
        return datas.retainAll(lists);
    }

    public void clear() {
        datas.clear();
    }

    public D get(int index) {
        return datas.get(index);
    }

    public D set(int index, D element) {
        return datas.set(index, element);
    }

    public void add(int index, D element) {
        datas.add(index, element);
    }

    public D remove(int index) {
        return datas.remove(index);
    }

    /**
     * item点击事件监听
     * @param <D> item中相应的数据
     */
    public interface OnItemClickListener<D> {

        /**
         * item点击事件监听
         * @param view item相应的布局
         * @param position item相应的位置
         * @param data item中相应的数据
         */
        void onItemClickListener(View view, int position, D data);
    }

    /**
     * item长按事件监听
     * @param <D> item中相应的数据
     */
    public interface OnItemLongClickLister<D> {

        /**
         * item长按事件监听
         * @param view item相应的布局
         * @param position item相应的位置
         * @param data item中相应的数据
         */
        void onItemLongClickListener(View view, int position, D data);
    }
}
