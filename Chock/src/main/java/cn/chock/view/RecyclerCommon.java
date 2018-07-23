package cn.chock.view;

import android.support.annotation.IntDef;
import android.widget.LinearLayout.LayoutParams;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@link ExRecyclerView}刷新和加载布局常量
 * @author timpkins
 */
interface RecyclerCommon {
    int MATCH_PARENT = LayoutParams.MATCH_PARENT;
    int WRAP_CONTENT = LayoutParams.WRAP_CONTENT;
    int STATE_NORMAL = 0x00; // 正常状态
    int STATE_RELEASE = 0x01; // 松开加载或刷新
    int STATE_LOADING = 0x02; // 正在加载
    int STATE_REFRESH = 0x02; // 正在刷新
    int STATE_SUCCESS = 0x03; // 加载或刷新成功
    int STATE_FAILURE = 0x04; // 加载或刷新失败
    int STATE_NOMORE = 0x05; // 数据加载到底

    @IntDef({STATE_NORMAL, STATE_RELEASE, STATE_LOADING, STATE_SUCCESS, STATE_FAILURE, STATE_NOMORE})
    @Retention(RetentionPolicy.SOURCE)
    @interface FooterState {
    }

    @IntDef({STATE_NORMAL, STATE_RELEASE, STATE_REFRESH, STATE_SUCCESS, STATE_FAILURE})
    @Retention(RetentionPolicy.SOURCE)
    @interface HeaderState {
    }
}
