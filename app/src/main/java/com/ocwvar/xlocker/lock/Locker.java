package com.ocwvar.xlocker.lock;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;
import com.ocwvar.xlocker.BuildConfig;
import com.ocwvar.xlocker.data.App;
import com.ocwvar.xlocker.data.Group;
import com.ocwvar.xlocker.data.LastConfig;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Locker {

    private Context applicationContext;

    //当前时间
    private Calendar now;

    //组别开始时间
    private Calendar start;

    //组别结束时间
    private Calendar end;

    //当前已解锁的包名
    private String currentUnlockedPackageName;

    public Locker(Context applicationContext) {
        this.applicationContext = applicationContext;
        now = Calendar.getInstance(BuildConfig.DEBUG ? Locale.CHINA : Locale.getDefault());
        start = Calendar.getInstance(BuildConfig.DEBUG ? Locale.CHINA : Locale.getDefault());
        end = Calendar.getInstance(BuildConfig.DEBUG ? Locale.CHINA : Locale.getDefault());
    }

    /**
     * 判断这个包名是否需要进行锁定
     *
     * @param packageName 包名
     * @return 是否需要进行锁定
     */
    public boolean isThisPackageNameNeed2Lock(String packageName) {
        if (!TextUtils.equals(packageName, this.currentUnlockedPackageName)) {
            _clearUnlockState();
        }

        final App app = LastConfig.get().indexAppByPackageName(packageName);
        final Group group = LastConfig.get().indexGroupById(app == null ? -1 : app.getGroupId());

        if (app == null || group == null) {
            //没有配置的APP或组别不存在
            return false;
        }

        return _checkTiming(group);
    }

    /**
     * 是否已经解锁过该包名的APP了
     *
     * @param packageName 包名
     * @return 是否已解锁
     */
    public boolean isUnlocked(String packageName) {
        return TextUtils.equals(packageName, this.currentUnlockedPackageName);
    }

    /**
     * 进行锁定，显示锁定界面
     *
     * @param packageName 需要进行锁定的包名
     */
    public void lock(String packageName) {
        // TODO: 2019/8/20 这里进行锁定的操作
        _unlock(packageName);
        Toast.makeText(applicationContext, "已解锁 " + packageName, Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置给定的包名为已解锁状态
     *
     * @param packageName 包名
     */
    private void _unlock(String packageName) {
        this.currentUnlockedPackageName = packageName;
    }

    /**
     * 清除已解锁的状态
     */
    private void _clearUnlockState() {
        this.currentUnlockedPackageName = null;
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
