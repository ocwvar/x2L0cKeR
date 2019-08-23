package com.ocwvar.xlocker.lock;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ocwvar.xlocker.R;
import com.ocwvar.xlocker.data.LastConfig;
import com.ocwvar.xlocker.lock.pages.BlockoutPage;
import com.ocwvar.xlocker.lock.pages.OldFingerprintPage;
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

    //当前需要显示锁定页面的包名
    private String currentPackageName;


    //DEBUG：调试的东西
    private TextView debugView;
    private WindowManager.LayoutParams debugLP;
    private ClipboardManager clipboardManager;

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
                    this.lockupPage = new OldFingerprintPage(this.applicationContext, this.windowManager);
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
     * DEBUG：切换显示调试悬浮 view
     *
     * @param show 是否显示
     */
    void __switchDebugView(boolean show) {
        if (show) {
            if (this.debugView == null) {
                this.debugView = (TextView) LayoutInflater.from(this.applicationContext).inflate(R.layout.debug_page_text, null);
                this.debugView.setOnClickListener((self) -> {
                    final String text = this.debugView.getText().toString();
                    this.clipboardManager.setPrimaryClip(ClipData.newPlainText(text, text));
                    Toast.makeText(applicationContext, "已复制 " + text, Toast.LENGTH_SHORT).show();
                });
                this.debugView.setTag(false);

                this.clipboardManager = (ClipboardManager) applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);

                this.debugLP = new WindowManager.LayoutParams();
                this.debugLP.gravity = Gravity.BOTTOM | Gravity.LEFT;
                this.debugLP.format = PixelFormat.RGBA_8888;
                this.debugLP.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                this.debugLP.dimAmount = 0.0f;
                this.debugLP.height = WindowManager.LayoutParams.WRAP_CONTENT;
                this.debugLP.width = WindowManager.LayoutParams.WRAP_CONTENT;
                this.debugLP.type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
            }

            if (!(boolean) this.debugView.getTag()) {
                this.windowManager.addView(this.debugView, this.debugLP);
                this.debugView.setTag(true);
            }
        } else {
            if (this.debugView != null && (boolean) this.debugView.getTag()) {
                this.windowManager.removeView(this.debugView);
                this.debugView.setTag(false);
            }
        }
    }

    /**
     * DEBUG：更新调试悬浮 view
     *
     * @param msg 是否显示
     */
    void __updateDebugView(String msg) {
        if (this.debugView == null || !(boolean) this.debugView.getTag()) {
            return;
        }

        this.debugView.setText(msg);
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
