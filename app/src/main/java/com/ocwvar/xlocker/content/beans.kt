package com.ocwvar.xlocker.content

/**
 * 锁定应用配置对象
 *
 * @property packageName String 应用包名
 * @property groupId Int    应用规则所属组别ID
 */
data class App(
    val packageName: String,
    val groupId: Int
)

/**
 * 规则组配置对象
 *
 * @property id Int 组别ID
 * @property startTime String 组别生效时间
 * @property endTime String 组别失效时间
 */
data class Group(
    val id: Int,
    val startTime: String,
    val endTime: String
)

/**
 * 配置参数对象
 *
 * @property updateInterval Long    配置刷新时间
 * @property lockType Int   锁定类型
 */
data class Config(
    val enable: Boolean,
    val updateInterval: Long,
    val lockType: LockType
)

/**
 * 锁定类型
 */
enum class LockType {
    /**
     * 指纹锁
     */
    fingerprint,

    /**
     * 完全拒绝访问
     */
    blockout
}