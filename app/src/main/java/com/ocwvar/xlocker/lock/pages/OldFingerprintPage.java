package com.ocwvar.xlocker.lock.pages;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ocwvar.xlocker.R;
import com.ocwvar.xlocker.data.LastConfig;
import com.ocwvar.xlocker.data.LockType;
import com.ocwvar.xlocker.lock.pages.i.BaseLockupPage;
import com.ocwvar.xlocker.lock.pages.i.ILockupPageCallback;

/**
 * 指纹访问界面
 */
public class OldFingerprintPage extends BaseLockupPage {

    private ILockupPageCallback callback;

    //指纹识别结果回调
    private final FingerprintResult fingerprintResult;

    //取消操作控制
    private final CancellationSignal cancellationSignal;

    //指纹识别服务
    private final FingerprintManager fingerprintManager;

    //提示文字
    private TextView tips;

    public OldFingerprintPage(Context applicationContext, WindowManager windowManager) {
        super(applicationContext, windowManager);
        this.fingerprintResult = new FingerprintResult();
        this.cancellationSignal = new CancellationSignal();
        this.fingerprintManager = (FingerprintManager) applicationContext.getSystemService(Context.FINGERPRINT_SERVICE);
    }

    /**
     * 显示锁定界面
     */
    @Override
    public void show() {
        super.show();
        if (!fingerprintManager.hasEnrolledFingerprints() || !fingerprintManager.isHardwareDetected()) {
            //设备没有录入指纹或硬件不支持
            Toast.makeText(getApplicationContext(), "尚未录入过指纹或硬件不支持", Toast.LENGTH_SHORT).show();
            if (this.callback != null) {
                this.callback.onUnlockSuccess();
            }
            return;
        }

        this.fingerprintManager.authenticate(
                null,
                this.cancellationSignal,
                0,
                this.fingerprintResult,
                null
        );
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
        final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.page_fingerprint, null);
        this.tips = view.findViewById(R.id.finger_tips);
        view.findViewById(R.id.finger_quit).setOnClickListener((View) -> {
            if (callback != null) {
                callback.onQuitWithoutUnlock();
            }
        });

        //调试模式
        if (LastConfig.get().getConfig().isDebug()){
            view.findViewById(R.id.finger_quit).setOnLongClickListener((self) -> {
                if (LastConfig.get().getConfig().isDebug()){
                    hideAndReset();
                }
                return true;
            });
        }

        return view;
    }

    /**
     * 应当在这里进行布局数据的重置
     */
    @Override
    public void onViewReset() {
        tips.setText(R.string.finger_tip_default);
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
        return LockType.fingerprint;
    }

    /**
     * 指纹识别结果回调
     */
    private class FingerprintResult extends FingerprintManager.AuthenticationCallback {

        /**
         * Called when an unrecoverable error has been encountered and the operation is complete.
         * No further callbacks will be made on this object.
         *
         * @param errorCode An integer identifying the error message
         * @param errString A human-readable error string that can be shown in UI
         */
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);

            switch (errorCode) {
                case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                case FingerprintManager.FINGERPRINT_ERROR_USER_CANCELED:
                    if (callback != null) {
                        callback.onQuitWithoutUnlock();
                    }
                    return;

                case FingerprintManager.FINGERPRINT_ERROR_HW_NOT_PRESENT:
                case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT_PERMANENT:
                case FingerprintManager.FINGERPRINT_ERROR_NO_FINGERPRINTS:
                case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
                case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                case FingerprintManager.FINGERPRINT_ERROR_VENDOR:
                    break;
            }

            Toast.makeText(getApplicationContext(), R.string.finger_tip_error_failed_lockout, Toast.LENGTH_SHORT).show();

            if (callback != null) {
                callback.onUnlockFailed();
            }
        }

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         *
         * @param helpCode   An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
        }

        /**
         * Called when a fingerprint is recognized.
         *
         * @param result An object containing authentication-related data
         */
        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            if (callback != null) {
                callback.onUnlockSuccess();
            }
        }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            tips.setText(R.string.finger_tip_error_failed);
        }
    }

}
