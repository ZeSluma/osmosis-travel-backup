package dev.konraditurbe.osmosis.connection

import dev.konraditurbe.osmosis.backup.BackupLease
import dev.konraditurbe.osmosis.net.MediaDownloader

/** Pure guard for the service-owned automatic writer; every mutable dependency must still match. */
object AutomaticTransferDispatchPolicy {
    fun mayStart(strictSupported: Boolean, session: String?, hasNetwork: Boolean, lease: SessionLease): Boolean =
        strictSupported && session != null && hasNetwork && CameraSessionCoordinator.mayUseCameraTraffic(lease)

    fun shouldRefuse(
        expectedSession: String,
        actualSession: String?,
        expectedNetwork: Any,
        actualNetwork: Any?,
        expectedEpoch: Long,
        current: SessionLease,
        transferCurrent: Boolean,
        schedulerCurrent: Boolean,
    ): Boolean = actualSession != expectedSession || actualNetwork !== expectedNetwork ||
        current.epoch != expectedEpoch || !transferCurrent || !schedulerCurrent ||
        !CameraSessionCoordinator.mayUseCameraTraffic(current)
}
