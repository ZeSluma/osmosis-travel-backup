package dev.konraditurbe.osmosis.ledger

/** Candidate identity is not verified byte identity. Missing length never creates an actionable asset. */
object IdentityEvidence {
    fun materializable(size: Long?) = size != null && size > 0
    fun provesSameVersion(observation: IdentityObservationRow, asset: AssetRow): Boolean =
        observation.sourceId == asset.sourceId && observation.storage == asset.storage &&
            observation.remotePath == asset.remotePath && observation.strongVersion != null &&
            observation.strongVersion == asset.strongVersion && materializable(asset.size) &&
            (observation.size == null || observation.size == asset.size) &&
            observation.mediaType == asset.mediaType &&
            (observation.remoteTime == null || observation.remoteTime == asset.remoteTime) &&
            (observation.handleEvidence == null || observation.handleEvidence == asset.handleEvidence)
}
