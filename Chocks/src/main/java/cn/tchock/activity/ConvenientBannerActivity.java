package cn.tchock.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import cn.chock.view.CBViewHolderCreator;
import cn.chock.view.ConvenientBanner;
import cn.chock.view.ConvenientBanner.OnItemClickListener;
import cn.tchock.R;

/**
 * @author timpkins
 */
public class ConvenientBannerActivity extends BaseTChockActivity implements OnItemClickListener {
    private ConvenientBanner<Integer> convenientBanner;//顶部广告栏控件
    private ArrayList<Integer> localImages = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converient_banner);
        convenientBanner = findViewById(R.id.convenientBanner);

        localImages.add(R.mipmap.banner_01);
        localImages.add(R.mipmap.banner_02);
        localImages.add(R.mipmap.banner_03);
        localImages.add(R.mipmap.banner_04);
        localImages.add(R.mipmap.banner_05);
        localImages.add(R.mipmap.banner_06);
        localImages.add(R.mipmap.banner_07);


        //本地图片例子
        convenientBanner.setPages(
                new CBViewHolderCreator() {
                    @Override
                    public LocalImageHolderView createHolder(View itemView) {
                        return new LocalImageHolderView(itemView);
                    }

                    @Override
                    public int getLayoutId() {
                        return R.layout.item_localimage;
                    }
                }, localImages)
                .setPageIndicator(new int[]{R.drawable.ic_indicator_normal, R.drawable.ic_indicator_focus})
                .setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        convenientBanner.startTurning(); //开始自动翻页
    }

    @Override
    protected void onPause() {
        super.onPause();
        convenientBanner.stopTurning(); //停止翻页
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "点击了第" + position + "个", Toast.LENGTH_SHORT).show();
    }
}
