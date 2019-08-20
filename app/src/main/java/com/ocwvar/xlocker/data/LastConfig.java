package com.ocwvar.xlocker.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ocwvar.xlocker.BuildConfig;

import java.util.LinkedHashMap;

public class LastConfig {

    private static LastConfig self;
    private final Object LOCK = new Object();

    private volatile int lastConfigHashCode = 114514;

    private LinkedHashMap<String, App> appList;
    private LinkedHashMap<Integer, Group> groupList;
    private Group zeroGroup;
    private Config config;

    private LastConfig() {
        this.zeroGroup = new Group(0, new int[]{0, 0}, new int[]{24, 0});
        this.appList = new LinkedHashMap<>();
        this.groupList = new LinkedHashMap<>();
        this.config = new Config(false, BuildConfig.DEBUG ? (5L * 1000L) : (60L * 1000L), LockType.fingerprint, QuitType.Launcher);
    }

    public static LastConfig get() {
        if (self == null) {
            self = new LastConfig();
        }
        return self;
    }

    /**
     * @return 最后更新的配置文件哈希码
     */
    public int getLastConfigHashCode() {
        synchronized (this.LOCK) {
            return lastConfigHashCode;
        }
    }

    /**
     * 通过包名来检索配置的应用
     *
     * @param packageName 用于检索的包名
     * @return 检索得到的结果，可能为 NULL
     */
    public @Nullable
    App indexAppByPackageName(@NonNull String packageName) {
        return this.appList.get(packageName);
    }

    /**
     * 通过包名来检索配置组别
     *
     * @param groupId 组别Id
     * @return 检索得到的结果，可能为 NULL
     */
    public @Nullable
    Group indexGroupById(int groupId) {
        return groupId == 0 ? this.zeroGroup : this.groupList.get(groupId);
    }

    /**
     * @return 主体配置信息
     */
    public @NonNull
    Config getConfig() {
        return config;
    }

    /**
     * 设置最后一次更新配置的哈希码
     *
     * @param lastConfigHashCode 哈希码
     */
    void setLastConfigHashCode(int lastConfigHashCode) {
        synchronized (this.LOCK) {
            this.lastConfigHashCode = lastConfigHashCode;
        }
    }

    /**
     * 设置需要锁定的App列表数据
     *
     * @param newSource 数据源
     */
    void setAppList(LinkedHashMap<String, App> newSource) {
        this.appList.clear();
        this.appList.putAll(newSource);
    }

    /**
     * 设置规则组别列表
     *
     * @param newSource 数据源
     */
    void setGroupList(LinkedHashMap<Integer, Group> newSource) {
        this.groupList.clear();
        this.groupList.putAll(newSource);
    }

    /**
     * 设置主体信息配置
     *
     * @param newSource 数据源
     */
    void setConfig(Config newSource) {
        this.config = newSource;
    }

}
