package cn.chock;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * <b>Chock</b>的Activity基类实现通用操作
 * @author timpkins
 */
public class BaseChockActivity extends AppCompatActivity {


    /**
     * 根据ID获取对应的View
     * @param id View对应的id
     * @param <T> View的类型
     * @return View
     */
    public <T extends View> T $(@IdRes int id) {
        return getDelegate().findViewById(id);
    }
}
