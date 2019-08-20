package com.ocwvar.xlocker.lock.pages.i;

import android.content.Context;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import com.ocwvar.xlocker.data.LockType;

/**
 * 锁定界面的接口
 */
public abstract class BaseLockupPage {

    public final Context applicationContext;
    public final WindowManager windowManager;

    public BaseLockupPage(Context applicationContext, WindowManager windowManager) {
        this.applicationContext = applicationContext;
        this.windowManager = windowManager;
    }

    /**
     * 设置事件回调接口
     *
     * @param callback 回调接口
     */
    abstract public void setCallback(@Nullable ILockupPageCallback callback);

    /**
     * 显示锁定界面
     */
    abstract public void show();

    /**
     * 隐藏锁定界面
     */
    abstract public void hide();

    /**
     * @return 界面所属的锁定方式
     */
    abstract public LockType getPageLockType();

}
