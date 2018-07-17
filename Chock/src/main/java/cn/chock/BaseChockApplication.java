package cn.chock;

import android.app.Application;

/**
 * <b>Chock</b>application基类，使用该库必须继承该类。
 * @author timpkins
 */
public class BaseChockApplication extends Application {
    private static BaseChockApplication application;

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
    }

    /**
     * 获取当前项目的Application对象
     * @param <T> 当前项目Application对象
     * @return Application对象
     */
    public static <T extends BaseChockApplication> T getApplication() {
        return (T)application;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        System.exit(0);
    }
}
