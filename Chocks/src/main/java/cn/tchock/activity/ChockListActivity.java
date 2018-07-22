package cn.tchock.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.chock.view.DividerItemDecoration;
import cn.chock.view.ExRecyclerView;
import cn.chock.view.ExRecyclerView.OnRefreshLoadListener;
import cn.tchock.R;
import cn.tchock.adapter.FunctionAdapter;
import cn.tchock.bean.Function;

public class ChockListActivity extends BaseTChockActivity implements OnRefreshLoadListener {
    private static final String TAG = ChockListActivity.class.getSimpleName();
    private ExRecyclerView ervContent;
    private FunctionAdapter adapter;
    private static final int PAGE_SIZE = 10;
    private int page = 1;
    private static final String format = "%04d";
    private int index = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chock_list);

        initRecyclerView();
    }

    private void initRecyclerView() {
        ervContent = $(R.id.ervContent);
        LinearLayoutManager linear = new LinearLayoutManager(this);
        ervContent.setLayoutManager(linear);
        adapter = new FunctionAdapter(this);
        ervContent.setAdapter(adapter);
        ervContent.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ervContent.setOnRefreshLoadListener(this);
        ervContent.onRefreshStart();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                page = 1;
                List<Function> lists = getData();
                adapter.addAll(lists);
                adapter.notifyDataSetChanged();
                if (ervContent != null)
                    ervContent.onRefreshComplete();
            }
        }, 3000);
    }

    @Override
    public void onLoading() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                page += 1;
                List<Function> lists = getData();
                adapter.addAll(lists);
                adapter.notifyDataSetChanged();
                if (ervContent != null) {
                    ervContent.onLoadingComplete();
                    adapter.notifyDataSetChanged();
                }
            }
        }, 1000);
    }





    private List<Function> getData() {
        List<Function> datas = new ArrayList<>();
        for (int i = (page - 1) * PAGE_SIZE + 1; i <= page * PAGE_SIZE; i++) {
            datas.add(new Function("", "ITEM", String.format(Locale.getDefault(), format, index++)));
        }
        return datas;
    }
}
