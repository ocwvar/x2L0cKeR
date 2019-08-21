package com.ocwvar.xlocker.lock;

import android.content.Context;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    //事件回传接口
    private Callback callback;

    //当前使用的锁定页面接口对象
    private BaseLockupPage lockupPage;

    private String currentPackageName;

    LockupInterface(@NonNull Context applicationContext, @NonNull Callback callback) {
        this.callback = callback;
        this.applicationContext = applicationContext;
        this.windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * 显示锁定界面
     */
    void show(String packageName) {
        this.currentPackageName = packageName;

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
    void hide() {
        if (this.lockupPage == null) {
            return;
        }

        this.lockupPage.hideAndReset();
    }

    /**
     * @return 当前是否正在显示
     */
    boolean isShowing() {
        return this.lockupPage != null && this.lockupPage.isShowing();
    }

    /**
     * @return 当前正在显示的包名
     */
    @Nullable
    String lastPackageName() {
        return this.currentPackageName;
    }

    /**
     * 切换当前的包名
     *
     * @param newPackageName 新的包名
     */
    void switchCurrentPackageName(@NonNull String newPackageName) {
        this.currentPackageName = newPackageName;
    }

    /**
     * 解锁未完成，用户主动退出
     */
    @Override
    public void onQuitWithoutUnlock() {
        this.callback.onUserUnlockResult(false, this.currentPackageName);
    }

    /**
     * 解锁成功回调接口
     */
    @Override
    public void onUnlockSuccess() {
        this.callback.onUserUnlockResult(true, this.currentPackageName);
    }

    /**
     * 解锁失败回调接口
     */
    @Override
    public void onUnlockFailed() {
        this.callback.onUserUnlockResult(false, this.currentPackageName);
    }

    interface Callback {

        /**
         * 用户解锁结果
         *
         * @param success     是否成功
         * @param packageName 请求锁定时的包名
         */
        void onUserUnlockResult(boolean success, String packageName);

    }
}
