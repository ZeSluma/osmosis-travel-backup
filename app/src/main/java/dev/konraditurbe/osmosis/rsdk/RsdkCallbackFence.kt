package dev.konraditurbe.osmosis.rsdk

import java.util.concurrent.atomic.AtomicLong

/** Fences platform callbacks from a released R-SDK GATT client. */
class RsdkCallbackFence {
    private val generation = AtomicLong(0)

    fun begin(): Long = generation.incrementAndGet()
    fun invalidate() { generation.incrementAndGet() }
    fun accepts(token: Long): Boolean = generation.get() == token
}
