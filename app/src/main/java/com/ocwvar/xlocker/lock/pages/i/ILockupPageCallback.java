package com.ocwvar.xlocker.lock.pages.i;

public interface ILockupPageCallback {

    /**
     * 解锁未完成，用户主动退出
     */
    void onQuitWithoutUnlock();

    /**
     * 解锁成功回调接口
     */
    void onUnlockSuccess();

    /**
     * 解锁失败回调接口
     */
    void onUnlockFailed();

}
