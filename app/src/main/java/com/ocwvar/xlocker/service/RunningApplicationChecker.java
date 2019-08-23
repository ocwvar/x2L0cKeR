package com.ocwvar.xlocker.service;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.autofill.AutofillManager;

import com.ocwvar.xlocker.data.Configuration;
import com.ocwvar.xlocker.data.Group;
import com.ocwvar.xlocker.data.LastConfig;
import com.ocwvar.xlocker.lock.Locker;

/**
 * 运行程序监测服务
 * <p>
 * 会通过判断当前屏幕显示的应用的包名<b>PackageName</b>来进行监测。
 * 监测此应用是否存在已配置的列表中，并且属于有效时间段内：{@link Group#getStartTime()} ~ {@link Group#getEndTime()}
 * 如果符合要求，则判断为符合锁定要求的APP。
 * <p>
 * 如果应用已经解锁过了，则不需要再次进行解锁操作。
 * 解锁后再次锁定的条件为包名发生了变动，则：
 * {@link android.accessibilityservice.AccessibilityServiceInfo#packageNames} 与 {@link Locker#currentUnlockedPackageName}不符
 * 就会再次进入锁定状态。
 */
@SuppressWarnings("JavadocReference")
public final class RunningApplicationChecker extends AccessibilityService {

    //所有必须的权限配置
    private final String[] REQUIRE_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Build.VERSION.SDK_INT >= 28 ? Manifest.permission.USE_BIOMETRIC : Manifest.permission.USE_FINGERPRINT
    };

    //配置更新器
    private Configuration configuration;

    /**
     * 是否曾经拥有悬浮窗权限
     * <p>
     * 在 Android O Api26 的设备上，会有一个叫做 AutoFill 的服务，此服务开启的情况下
     * 会在某些输入框内弹出自动填充窗口，此窗口出现的时候， {@link Settings#canDrawOverlays(Context)} 将会返回 <b>False</b>
     * 即使你的应用已经开启了权限。
     * <p>
     * 而目前 <b>2019-8-23</b> 暂时没有发现有什么办法能获取到 AutoFill 的显示状况。
     * 目前暂时处理方式为  记录权限是否曾经授予过并返回结果。
     * <p>
     * {@link AutofillManager#registerCallback(AutofillManager.AutofillCallback)} 此方法设置的回调接口并没有触发。
     * 原因未知
     */
    private boolean hasOverlayPermissionBefore = false;

    //锁定器
    private Locker locker;

    /**
     * 服务创建的时候创建与初始化相关的处理
     */
    @Override
    @SuppressLint("NewApi")
    public void onCreate() {
        super.onCreate();
        _outputLog("服务已启动");
        this.locker = new Locker(getApplicationContext());
        this.configuration = new Configuration(getApplicationContext());
        this.configuration.startLoadingTask();
    }

    /**
     * 接收无障碍事件回调方法
     *
     * @param event 事件对象
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }

        final String packageName = event.getPackageName().toString();
        _outputLog("当前包名：" + packageName);

        if (!_checkPermission()) {
            //检查普通权限
            startActivity(
                    new Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setData(Uri.fromParts("package", getPackageName(), null))
            );
            return;
        }

        if (!_checkOverlayPermission()) {
            //检查悬浮窗权限
            startActivity(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setData(Uri.parse("package:" + getPackageName()))
            );
            return;
        }

        if (!LastConfig.get().getConfig().getEnable()) {
            //没有启用，则不做任何处理
            return;
        }

        if (LastConfig.get().getConfig().isDebug()) {
            //显示调试信息
            this.locker.__showDebugMsg(packageName);
        }

        if (LastConfig.get().indexIgnoreAppByPackageName(packageName)) {
            //在需要忽略的包名内，不作处理
            return;
        }

        //剩下的交由锁处理
        this.locker.handleThisPackageName(packageName);
    }

    @Override
    public void onInterrupt() {
        stopSelf();
    }

    /**
     * 服务销毁的时候停止其他的处理
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        _outputLog("服务已结束");
        this.configuration.cancelLoading();
    }

    /**
     * 检查权限是否完全
     *
     * @return 是否通过
     */
    private boolean _checkPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        for (final String permission : REQUIRE_PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查悬浮窗权限
     *
     * @return 是否通过
     */
    private boolean _checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 26) {
            // Android 8 之下没有 AutoFill 服务，直接返回普通方式检查结果即可
            return Settings.canDrawOverlays(getApplicationContext());
        }

        if (Build.VERSION.SDK_INT > 26) {
            //这里的说明，请查看变量 hasOverlayPermissionBefore 的注释
            if (!this.hasOverlayPermissionBefore) {
                this.hasOverlayPermissionBefore = Settings.canDrawOverlays(getApplicationContext());
            }

            return this.hasOverlayPermissionBefore;
        }

        //Android 6 以下的设备不需要额外申请权限
        return true;
    }

    /**
     * 输出日志
     */
    private void _outputLog(String msg) {
        Log.d("###Checker###", msg);
    }

}
