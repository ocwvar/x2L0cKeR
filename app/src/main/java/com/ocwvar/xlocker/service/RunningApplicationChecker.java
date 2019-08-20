package com.ocwvar.xlocker.service;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import com.ocwvar.xlocker.data.Configuration;

/**
 * 运行程序监管服务
 */
public final class RunningApplicationChecker extends AccessibilityService {

    //配置更新器
    private Configuration configuration;

    /**
     * 服务创建的时候创建与初始化相关的处理
     */
    @Override
    public void onCreate() {
        super.onCreate();
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
        outputLog("当前包名：" + event.getPackageName());
    }

    @Override
    public void onInterrupt() {
    }

    /**
     * 服务销毁的时候停止其他的处理
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.configuration.cancelLoading();
    }

    private void outputLog(String msg) {
        System.out.println("#Checker# " + msg);
    }
}
