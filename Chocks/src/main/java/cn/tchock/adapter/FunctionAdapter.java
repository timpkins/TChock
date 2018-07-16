package cn.tchock.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import cn.chock.adapter.BaseRecyclerAdapter;
import cn.chock.adapter.BaseViewHolder;
import cn.tchock.R;
import cn.tchock.bean.Function;

/**
 * @author timpkins
 */
public class FunctionAdapter extends BaseRecyclerAdapter<BaseViewHolder, Function> {
    private static final int[] images = {R.mipmap.ic_bike, R.mipmap.ic_boat, R.mipmap.ic_flight, R.mipmap.ic_shipping,
            R.mipmap.ic_subway, R.mipmap.ic_taxi, R.mipmap.ic_tram, R.mipmap.ic_walk};
    private static final String[] colors = {"#FF0000", "#FF7F00", "#FFFF00", "#00FF00", "#00FFFF", "#0000FF",
            "#8B00FF","#000000"};


    public FunctionAdapter(Context context) {
        super(context);
    }

    @Override
    public int setLayoutRes() {
        return R.layout.item_function_layout;
    }

    @Override
    protected void onBindViewHolder(@NonNull BaseViewHolder holder, int position, @NonNull Function function) {
        ImageView ivHead = holder.getView(R.id.ivItemHead);
        TextView tvContent = holder.getView(R.id.tvItemContent);

        ivHead.setColorFilter(Color.parseColor(colors[position%8]));
        ivHead.setImageResource(images[position%8]);
        tvContent.setText(String.format(Locale.getDefault(), "%s    %s    %04d", function.getName(), function.getNum()
                , position + 1));
    }
}
