package com.ocwvar.xlocker.lock.pages;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ocwvar.xlocker.R;
import com.ocwvar.xlocker.data.LockType;
import com.ocwvar.xlocker.lock.pages.i.BaseLockupPage;
import com.ocwvar.xlocker.lock.pages.i.ILockupPageCallback;

/**
 * 完全禁止访问页面
 */
public class BlockoutPage extends BaseLockupPage implements View.OnClickListener {

    private ILockupPageCallback callback;
    private View floatingView;

    public BlockoutPage(Context applicationContext, WindowManager windowManager) {
        super(applicationContext, windowManager);
    }

    /**
     * 应当在这里生成窗口的布局参数对象
     *
     * @return 布局参数对象
     */
    @NonNull
    @Override
    public WindowManager.LayoutParams onGetLayoutParams() {
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.dimAmount = 1.0f;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        return layoutParams;
    }

    /**
     * 应当在这里生成悬浮布局版面
     *
     * @return 悬浮View对象
     */
    @NonNull
    @Override
    public View onGetFloatingPanel() {
        this.floatingView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.page_blockout, null);
        this.floatingView.findViewById(R.id.blockout_quit).setOnClickListener(BlockoutPage.this);
        return this.floatingView;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (this.callback == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.blockout_quit:
                this.callback.onQuitWithoutUnlock();
                break;
        }
    }

    /**
     * 应当在这里进行布局数据的重置
     */
    @Override
    public void onViewReset() {

    }

    /**
     * 设置事件回调接口
     *
     * @param callback 回调接口
     */
    @Override
    public void setCallback(@Nullable ILockupPageCallback callback) {
        this.callback = callback;
    }

    /**
     * @return 界面所属的锁定方式
     */
    @Override
    public LockType getPageLockType() {
        return LockType.blockout;
    }

}
