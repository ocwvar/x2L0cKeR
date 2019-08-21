package com.ocwvar.xlocker.lock.pages.i;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ocwvar.xlocker.data.LockType;

/**
 * 锁定界面的接口
 */
public abstract class BaseLockupPage {

    private final Context applicationContext;
    private final WindowManager windowManager;

    private View floatingView = null;
    private WindowManager.LayoutParams layoutParams = null;

    public BaseLockupPage(Context applicationContext, WindowManager windowManager) {
        this.applicationContext = applicationContext;
        this.windowManager = windowManager;
    }

    /**
     * 应当在这里生成窗口的布局参数对象
     *
     * @return 布局参数对象
     */
    abstract public @NonNull
    WindowManager.LayoutParams onGetLayoutParams();

    /**
     * 应当在这里生成悬浮布局版面
     *
     * @return 悬浮View对象
     */
    abstract public @NonNull
    View onGetFloatingPanel();

    /**
     * 应当在这里进行布局数据的重置
     */
    abstract public void onViewReset();

    /**
     * @return Application Context
     */
    public @NonNull
    Context getApplicationContext() {
        return this.applicationContext;
    }

    /**
     * 显示锁定界面
     */
    public void show() {
        if (isShowing()) {
            return;
        }

        if (this.floatingView == null) {
            this.floatingView = onGetFloatingPanel();
            this.layoutParams = onGetLayoutParams();
            this.floatingView.setTag(false);
        }

        this.floatingView.setTag(true);
        this.windowManager.addView(this.floatingView, this.layoutParams);
    }

    /**
     * 隐藏并重置锁定界面
     */
    public void hideAndReset() {
        if (!isShowing()) {
            return;
        }

        onViewReset();
        this.floatingView.setTag(false);
        this.windowManager.removeView(this.floatingView);
    }

    /**
     * @return 当前是否正在显示
     */
    public boolean isShowing() {
        if (this.floatingView == null){
            return false;
        }

        return (boolean) this.floatingView.getTag();
    }

    /**
     * 设置事件回调接口
     *
     * @param callback 回调接口
     */
    abstract public void setCallback(@Nullable ILockupPageCallback callback);

    /**
     * @return 界面所属的锁定方式
     */
    abstract public LockType getPageLockType();

}
