package cn.chock;

import java.lang.Thread.UncaughtExceptionHandler;

import cn.chock.util.LogUtils;


/**
 * 默认异常处理
 * @author timpkins
 */
class DefaultExceptionHandler implements UncaughtExceptionHandler {
    private static final String TAG = "DefaultExceptionHandler";

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        LogUtils.e(TAG, "Thread Name : " + thread.getName(), exception);
        BaseChockApplication.getApplication().onTerminate();
    }
}
