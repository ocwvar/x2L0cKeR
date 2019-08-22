package com.ocwvar.xlocker.lock;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.ocwvar.xlocker.BuildConfig;
import com.ocwvar.xlocker.data.App;
import com.ocwvar.xlocker.data.Group;
import com.ocwvar.xlocker.data.LastConfig;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Locker implements LockupInterface.Callback {

    private Context applicationContext;

    //当前时间
    private Calendar now;

    //组别开始时间
    private Calendar start;

    //组别结束时间
    private Calendar end;

    //锁定界面
    private LockupInterface lockupInterface;

    //当前已解锁的包名
    private String currentUnlockedPackageName;

    //最后一次检查的包名
    private String lastCheckedPackageName;

    public Locker(Context applicationContext) {
        this.applicationContext = applicationContext;
        this.lockupInterface = new LockupInterface(this.applicationContext, Locker.this);
        now = Calendar.getInstance(BuildConfig.DEBUG ? Locale.CHINA : Locale.getDefault());
        start = Calendar.getInstance(BuildConfig.DEBUG ? Locale.CHINA : Locale.getDefault());
        end = Calendar.getInstance(BuildConfig.DEBUG ? Locale.CHINA : Locale.getDefault());
    }

    /**
     * 用户解锁结果
     *
     * @param success     是否成功
     * @param packageName 请求锁定时的包名
     */
    @Override
    public void onUserUnlockResult(boolean success, String packageName) {
        if (success) {
            //解锁成功
            _unlock(packageName);
        } else {
            //解锁失败
            _doQuitAction();

            //这里不直接进行解锁界面的隐藏，应当由界面切换触发解锁界面的隐藏动作
        }
    }

    /**
     * 对包名进行处理
     *
     * @param packageName 包名
     */
    public void handleThisPackageName(@NonNull String packageName) {
        this.lastCheckedPackageName = packageName;

        /*
                判断流程
                0.如果当前有已解锁的包名，但与进行检测的包名不一致，则先清除旧的包名


                1.没有显示锁定界面、包名不符合锁定：不作处理


                2.没有显示锁定界面、包名符合锁定：
                        此包名已经处于解锁状：不作处理
                        此包名尚未解锁：显示锁定界面

                3.正在显示锁定界面、包名不符合锁定：
                        隐藏锁定界面


                4.正在显示锁定界面、包名符合锁定：
                        包名与上一次显示的相同：不作处理
                        包名与上一次不同：切换为新的包名

         */

        //【0】
        if (!TextUtils.isEmpty(this.currentUnlockedPackageName) && !TextUtils.equals(this.currentUnlockedPackageName, packageName)) {
            _clearUnlockState();
        }

        final boolean isThisAppShouldBeLockup = _isThisAppShouldBeLockup(packageName);

        //【1】
        if (!this.lockupInterface.isShowing() && !isThisAppShouldBeLockup) {
            return;
        }

        //【2】
        if (!this.lockupInterface.isShowing() && isThisAppShouldBeLockup) {
            if (!_isUnlocked(packageName)) {
                _lock(packageName);
            }
            return;
        }

        //【3】
        if (this.lockupInterface.isShowing() && !isThisAppShouldBeLockup) {
            this.lockupInterface.hide();
            return;
        }

        //【4】
        if (this.lockupInterface.isShowing() && isThisAppShouldBeLockup) {
            if (!TextUtils.equals(this.lockupInterface.lastPackageName(), packageName)) {
                this.lockupInterface.switchCurrentPackageName(packageName);
            }
            return;
        }

    }

    /**
     * 进行锁定，显示锁定界面
     *
     * @param packageName 需要进行锁定的包名
     */
    private void _lock(@NonNull String packageName) {
        this.lockupInterface.show(packageName);
    }

    /**
     * 设置给定的包名为已解锁状态并隐藏锁定界面
     *
     * @param packageName 包名
     */
    private void _unlock(@NonNull String packageName) {
        this.currentUnlockedPackageName = packageName;
        this.lockupInterface.hide();
    }

    /**
     * 执行退出动作
     */
    private void _doQuitAction() {
        switch (LastConfig.get().getConfig().getQuitType()) {
            case Launcher:
                final Intent backHome = new Intent(Intent.ACTION_MAIN);
                backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                backHome.addCategory(Intent.CATEGORY_HOME);
                this.applicationContext.startActivity(backHome);
                break;
            case Termination:

                break;
        }
    }

    /**
     * 是否已经解锁过该包名的APP了
     *
     * @param packageName 包名
     * @return 是否已解锁
     */
    private boolean _isUnlocked(@NonNull String packageName) {
        return TextUtils.equals(this.currentUnlockedPackageName, packageName);
    }

    /**
     * 清除已解锁的状态
     */
    private void _clearUnlockState() {
        this.currentUnlockedPackageName = null;
    }

    /**
     * 当前这个应用是否符合上锁条件
     *
     * @param packageName 要判断的包名
     * @return 是否符合条件
     */
    private boolean _isThisAppShouldBeLockup(String packageName) {
        final App app = LastConfig.get().indexAppByPackageName(packageName);
        final Group group = LastConfig.get().indexGroupById(app == null ? -1 : app.getGroupId());

        if (app == null || group == null) {
            //没有配置的APP或组别不存在
            return false;
        }

        return _checkTiming(group);
    }

    /**
     * 检查组别时间
     *
     * @param group 组别对象
     * @return 当前是否在组别配置的时间内
     */
    private boolean _checkTiming(Group group) {
        //配置各个时间对象
        final Date date = new Date();
        this.now.clear();
        this.start.clear();
        this.end.clear();
        this.now.setTime(date);
        this.start.setTime(date);
        this.end.setTime(date);
        this.start.set(Calendar.HOUR_OF_DAY, group.getStartTime()[0]);
        this.start.set(Calendar.MINUTE, group.getStartTime()[1]);
        this.end.set(Calendar.HOUR_OF_DAY, group.getEndTime()[0]);
        this.end.set(Calendar.MINUTE, group.getEndTime()[1]);

        //当前时间是要在 StartTime 之后，并且在 EndTime 之前
        return now.after(this.start) && now.before(this.end);
    }

}
