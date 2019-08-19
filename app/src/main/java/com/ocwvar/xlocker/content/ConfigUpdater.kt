package com.ocwvar.xlocker.content

import android.os.Environment

/**
 * 配置文件刷新器
 */
class ConfigUpdater {

    /**
     * 最新的配置数据
     */
    companion object LastConfigs {

        //最后一次获取更新时配置信息的哈希码
        private var lastConfigHashCode: Int = 114514

        private val apps: ArrayList<App> = ArrayList()
        private val groups: ArrayList<Group> = ArrayList()
        private var config: Config = Config(false, 0L, LockType.blockout)

        /**
         * @return List<App>    配置需要锁定的APP列表
         */
        fun getApps(): List<App> = this.apps

        /**
         * @return List<Group>  锁定配置组别列表
         */
        fun getGroups(): List<Group> = this.groups

        /**
         * @return Config   当前配置信息
         */
        fun getConfig(): Config = this.config

        /**
         * @return Int   最后一次获取更新时配置信息的哈希码
         */
        fun getLastConfigHastCode(): Int = this.lastConfigHashCode

        /**
         * 更新数据 【配置需要锁定的APP列表】
         *
         * @param newSource List<App> 新数据
         */
        internal fun updateAppList(newSource: List<App>) {
            this.apps.apply {
                clear()
                addAll(newSource)
            }
        }

        /**
         * 更新数据 【锁定配置组别列表】
         *
         * @param newSource List<App> 新数据
         */
        internal fun updateGroupList(newSource: List<Group>) {
            this.groups.apply {
                clear()
                addAll(newSource)
            }
        }

        /**
         * 更新配置信息数据
         *
         * @param newSource Config 新数据
         */
        internal fun updateConfig(newSource: Config) {
            this.config = newSource
        }

        /**
         * 设置最后一次配置信息的哈希码
         *
         * @param newHashCode Int   新的哈希码
         */
        internal fun setLastConfigHashCode(newHashCode: Int) {
            this.lastConfigHashCode = newHashCode
        }

    }

    /**
     * 配置文件路径
     */
    private val PATH: String = "${Environment.getExternalStorageDirectory().path}/_c0nFig.json"

    fun update() {

    }

}