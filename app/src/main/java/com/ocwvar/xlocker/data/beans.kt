package com.ocwvar.xlocker.data

/**
 * 锁定应用配置对象
 *
 * @property packageName String 应用包名
 * @property groupId Int    应用规则所属组别ID
 */
data class App(
    val packageName: String,
    val groupId: Int
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }

        if (other !is App) {
            return false
        }

        return other.hashCode() == hashCode()
    }

    override fun toString(): String = "$packageName with group $groupId"

    override fun hashCode(): Int = packageName.hashCode()
}

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
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }

        if (other !is Group) {
            return false
        }

        return other.hashCode() == hashCode()
    }

    override fun toString(): String = "GroupId $id from $startTime to $endTime"

    override fun hashCode(): Int = id
}

/**
 * 配置参数对象
 *
 * @property enable Boolean    是否启用
 * @property updateInterval Long    配置刷新时间
 * @property lockType LockType   锁定类型
 * @property quitType QuitType   退出类型
 */
data class Config(
    val enable: Boolean,
    val updateInterval: Long,
    val lockType: LockType,
    val quitType: QuitType
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

/**
 * 退出类型
 */
enum class QuitType {

    /**
     * 返回主Launcher
     */
    Launcher,

    /**
     * 终止应用，需要ROOT权限
     */
    Termination

}