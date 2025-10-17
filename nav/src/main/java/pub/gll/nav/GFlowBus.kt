package pub.gll.nav

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ==============================================
 * 🔄 GFlowBus
 * ==============================================
 *
 * 一个基于 [Kotlin Flow] 实现的轻量级事件总线（Flow-based EventBus）。
 *
 * 支持：
 * - 生命周期自动解绑（基于 [LifecycleOwner]）
 * - 粘性事件（Stick 模式）
 * - 协程安全的事件发送与接收
 *
 * ---
 * ### ⚙️ 使用方式
 *
 * #### 🔹 普通事件（非粘性）
 * ```kotlin
 * // 发送事件
 * GFlowBus.with<MessageEvent>("test").post(this, messageEvent)
 *
 * // 接收事件（自动随生命周期解绑）
 * GFlowBus.with<MessageEvent>("test").register(this) { event ->
 *     Log.d("TAG", "收到事件: $event")
 * }
 * ```
 *
 * #### 🔹 粘性事件（Sticky）
 * 粘性事件在订阅后仍能接收到最后一次发送的值。
 * ```kotlin
 * GFlowBus.withStick<UserInfo>("user_update").register(this) { user ->
 *     Log.d("TAG", "上次或最新的用户信息: $user")
 * }
 * ```
 *
 * ---
 * ### 💡 设计理念
 *
 * - `GFlowBus` 基于 `MutableSharedFlow`，代替传统 LiveDataBus / EventBus。
 * - 支持多事件通道（通过 `key` 区分）。
 * - 自动清理无订阅者的通道，避免内存泄漏。
 *
 * ---
 * ### ⚠️ 注意事项
 * - 每个事件通道通过 `key` 区分，请保持唯一。
 * - 若在 `Service` / `ViewModel` 中使用，请手动调用 `destroy()` 释放。
 * - 粘性事件通道内部缓冲 1 个事件。
 *
 * @author  GLL
 * @since   1.0.0
 */
object GFlowBus {

    private const val TAG = "GFlowBus"

    /** 非粘性事件通道 Map */
    private val busMap = mutableMapOf<String, FlowEventBus<*>>()

    /** 粘性事件通道 Map */
    private val busStickMap = mutableMapOf<String, FlowStickEventBus<*>>()

    // ======================================================
    // 🚀 创建或获取非粘性事件通道
    // ======================================================

    /**
     * 获取一个普通（非粘性）的事件通道。
     *
     * @param key 通道唯一标识
     * @return [FlowEventBus] 实例，用于发送或接收事件
     */
    @Synchronized
    fun <T> with(key: String): FlowEventBus<T> {
        var flowEventBus = busMap[key]
        if (flowEventBus == null) {
            flowEventBus = FlowEventBus<T>(key)
            busMap[key] = flowEventBus
        }
        return flowEventBus as FlowEventBus<T>
    }

    // ======================================================
    // 🧷 创建或获取粘性事件通道
    // ======================================================

    /**
     * 获取一个粘性事件通道。
     * 粘性事件会缓存最近一次事件值，
     * 新注册的观察者会立即收到该值。
     *
     * @param key 通道唯一标识
     * @return [FlowStickEventBus] 实例
     */
    @Synchronized
    fun <T> withStick(key: String): FlowStickEventBus<T> {
        var stickEventBus = busStickMap[key]
        if (stickEventBus == null) {
            stickEventBus = FlowStickEventBus<T>(key)
            busStickMap[key] = stickEventBus
        }
        return stickEventBus as FlowStickEventBus<T>
    }

    // ======================================================
    // 🔸 非粘性事件通道实现
    // ======================================================

    /**
     * Flow 事件通道（非粘性）。
     *
     * @param key 当前通道唯一标识
     */
    open class FlowEventBus<T>(private val key: String) : DefaultLifecycleObserver {

        /** 内部 MutableSharedFlow（事件发送通道） */
        private val _events: MutableSharedFlow<T> by lazy { obtainEvent() }

        /** 外部暴露的只读 SharedFlow（事件接收通道） */
        private val events = _events.asSharedFlow()

        /**
         * 创建 SharedFlow 对象。
         * 默认缓存区大小为 1，溢出策略为丢弃最旧值。
         */
        open fun obtainEvent(): MutableSharedFlow<T> =
            MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)

        // ======================================================
        // 📨 事件接收
        // ======================================================

        /**
         * 注册事件监听（绑定生命周期，自动解绑）。
         *
         * @param lifecycleOwner 生命周期宿主（Activity / Fragment）
         * @param action 接收到事件时的回调
         */
        fun register(lifecycleOwner: LifecycleOwner, action: (t: T) -> Unit) {
            lifecycleOwner.lifecycleScope.launch {
                events.collect {
                    try {
                        action(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(TAG, "FlowBus - Error:$e")
                    }
                }
            }
        }

        /**
         * 在指定协程作用域中注册事件监听。
         *
         * @param scope 协程作用域（可用于 ViewModelScope 等）
         * @param action 接收到事件时的回调
         */
        fun register(scope: CoroutineScope, action: (t: T) -> Unit) {
            scope.launch {
                events.collect {
                    try {
                        action(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(TAG, "FlowBus - Error:$e")
                    }
                }
            }
        }

        // ======================================================
        // 📤 事件发送
        // ======================================================

        /**
         * 在协程中发送事件（挂起函数）。
         */
        suspend fun post(event: T) {
            _events.emit(event)
        }

        /**
         * 在指定协程作用域中发送事件（非挂起）。
         */
        fun post(scope: CoroutineScope, event: T) {
            scope.launch { _events.emit(event) }
        }

        // ======================================================
        // ♻️ 销毁逻辑
        // ======================================================

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            Log.w(TAG, "FlowBus ==== 自动onDestroy")
            val subscriptCount = _events.subscriptionCount.value
            if (subscriptCount <= 0) busMap.remove(key)
        }

        /**
         * 手动销毁（用于 Service、广播、无生命周期环境）。
         */
        fun destroy() {
            Log.w(TAG, "FlowBus ==== 手动销毁")
            val subscriptionCount = _events.subscriptionCount.value
            if (subscriptionCount <= 0) busMap.remove(key)
        }
    }

    // ======================================================
    // 📌 粘性事件通道实现
    // ======================================================

    /**
     * 粘性事件版本的 [FlowEventBus]。
     * 内部缓存最近一次事件值，供新观察者立即获取。
     */
    class FlowStickEventBus<T>(key: String) : FlowEventBus<T>(key) {
        override fun obtainEvent(): MutableSharedFlow<T> =
            MutableSharedFlow(1, 1, BufferOverflow.DROP_OLDEST)
    }
}
