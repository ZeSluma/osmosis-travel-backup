package dev.konraditurbe.osmosis.integrity

/**
 * Compares two observations of one remote object without granting source-equivalence by accident.
 * A name/path and length are useful contradiction checks, never enough to confirm continuity.
 */
enum class Continuity { CONFIRMED, UNCONFIRMED, CONTRADICTED }

data class SourceObservation(
    val source: String,
    val asset: String,
    val remotePath: String,
    val bytes: Long,
    val validator: String? = null,
    val validatorTrust: VersionTrust = VersionTrust.UNKNOWN
)

object SourceContinuity {
    fun evaluate(previous: SourceObservation, current: SourceObservation): Continuity {
        if (previous.source != current.source || previous.asset != current.asset ||
            previous.remotePath != current.remotePath || previous.bytes != current.bytes) return Continuity.CONTRADICTED
        val oldValidator = previous.validator?.takeIf { it.isNotBlank() }
        val newValidator = current.validator?.takeIf { it.isNotBlank() }
        if (oldValidator != null && newValidator != null && oldValidator != newValidator) return Continuity.CONTRADICTED
        return if (oldValidator != null && oldValidator == newValidator &&
            previous.validatorTrust == VersionTrust.IMMUTABLE_VERSION &&
            current.validatorTrust == VersionTrust.IMMUTABLE_VERSION) Continuity.CONFIRMED
        else Continuity.UNCONFIRMED
    }
}
