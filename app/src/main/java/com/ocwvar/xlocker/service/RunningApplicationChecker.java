package com.ocwvar.xlocker.service;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.ocwvar.xlocker.BuildConfig;
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
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //忽略的包名
    private final String[] IGNORE_PACKAGE_NAMES = new String[]{
            BuildConfig.APPLICATION_ID,
            "com.android.systemui",
            "android"
    };

    //配置更新器
    private Configuration configuration;

    //锁定器
    private Locker locker;

    /**
     * 服务创建的时候创建与初始化相关的处理
     */
    @Override
    public void onCreate() {
        super.onCreate();
        _outputLog("服务已启动");

        if (BuildConfig.DEBUG) {
            //由于应用没有界面，所以如果是调试下，这里休眠 5s
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ignore) {
            }
        }

        this.locker = new Locker(getApplicationContext());
        this.configuration = new Configuration();
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
            //所有必须的权限没有给予
            startActivity(
                    new Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setData(Uri.fromParts("package", getPackageName(), null))
            );
            return;
        } else if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(getApplicationContext())) {
            //没有悬浮窗权限
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

        if (_checkInIgnorePage(packageName)) {
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
     * 检查是否在忽略处理页面
     *
     * @param packageName 包名
     * @return 是否在忽略页面
     */
    private boolean _checkInIgnorePage(String packageName) {
        for (final String name : IGNORE_PACKAGE_NAMES) {
            if (TextUtils.equals(name, packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 输出日志
     */
    private void _outputLog(String msg) {
        Log.d("###Checker###", msg);
    }

}
