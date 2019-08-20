package com.ocwvar.xlocker.lock;

import android.content.Context;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import com.ocwvar.xlocker.data.LastConfig;
import com.ocwvar.xlocker.lock.pages.BlockoutPage;
import com.ocwvar.xlocker.lock.pages.i.BaseLockupPage;
import com.ocwvar.xlocker.lock.pages.i.ILockupPageCallback;

/**
 * 锁定界面
 */
public class LockupInterface implements ILockupPageCallback {

    private Context applicationContext;
    private WindowManager windowManager;

    //当前使用的锁定页面接口对象
    private BaseLockupPage lockupPage;

    public LockupInterface(@NonNull Context applicationContext) {
        this.applicationContext = applicationContext;
        this.windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * 显示锁定界面
     */
    public void show() {

        //如果页面为 NULL 或 页面类型不对，则创建新的锁定界面
        if (this.lockupPage == null || this.lockupPage.getPageLockType() != LastConfig.get().getConfig().getLockType()) {
            switch (LastConfig.get().getConfig().getLockType()) {
                case blockout:
                    this.lockupPage = new BlockoutPage(this.applicationContext, this.windowManager);
                    break;
                case fingerprint:
                    break;
            }
            this.lockupPage.setCallback(LockupInterface.this);
        }

        this.lockupPage.show();
    }

    /**
     * 隐藏锁定界面
     */
    public void hide() {
        if (this.lockupPage == null) {
            return;
        }

        this.lockupPage.hide();
    }

    /**
     * 解锁未完成，用户主动退出
     */
    @Override
    public void onQuitWithoutUnlock() {

    }

    /**
     * 解锁成功回调接口
     */
    @Override
    public void onUnlockSuccess() {

    }

    /**
     * 解锁失败回调接口
     *
     * @param retryCount 重试次数
     */
    @Override
    public void onUnlockFailed(int retryCount) {

    }
}
