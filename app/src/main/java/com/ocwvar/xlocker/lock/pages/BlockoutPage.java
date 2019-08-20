package com.ocwvar.xlocker.lock.pages;

import android.content.Context;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import com.ocwvar.xlocker.data.LockType;
import com.ocwvar.xlocker.lock.pages.i.BaseLockupPage;
import com.ocwvar.xlocker.lock.pages.i.ILockupPageCallback;

public class BlockoutPage extends BaseLockupPage {

    public BlockoutPage(Context applicationContext, WindowManager windowManager) {
        super(applicationContext, windowManager);
    }

    /**
     * 设置事件回调接口
     *
     * @param callback 回调接口
     */
    @Override
    public void setCallback(@Nullable ILockupPageCallback callback) {

    }

    /**
     * 显示锁定界面
     */
    @Override
    public void show() {

    }

    /**
     * 隐藏锁定界面
     */
    @Override
    public void hide() {

    }

    /**
     * @return 界面所属的锁定方式
     */
    @Override
    public LockType getPageLockType() {
        return LockType.blockout;
    }
}
