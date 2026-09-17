# ADR 0006 — Accepted GATE-9 and plan-bound confirmation

2026-09-17. Gate placement and normal confirmation UX accepted by explicit user instruction. Continuation policy below is the documented architecture decision requested before implementation; it remains subject to GATE-1/GATE-9 verification. Supersedes ADR0005's pending GATE-9/optional-confirmation decisions and earlier blanket reconfirm-on-every-interruption design, without changing historical evidence.

## Gate and normal UX

**GATE-9 — SAFE CAMERA CLEANUP** is accepted, NOT_TESTED. Purpose: prove explicitly authorized removal of verified source assets is correct, bounded, recoverable and independently checked, with stricter evidence than downloads. Require GATE-4 PASS, G2/G3/G7 and their predecessors PASS, and a passed secondary-destination gate sufficient for configured redundancy. Default is phone + SSD (G5), or phone + cloud (G6); require both only if policy makes both mandatory. GATE-8 is independent, not a cleanup prerequisite.

One explicit destructive confirmation after the primary cleanup action is sufficient in normal operation. Show recording/asset counts, approximate size, exact scope, verified destinations, retained newer/unverified/excluded counts and latest source/redundancy verification. No generic OK, typed phrase, per-file selection or repetitive dialogs. Optional biometric/hold/swipe confirmation defaults OFF; evidence may motivate a separately reviewed stronger preference. No automatic initiation or format fallback.

## Live safety and authorization binding

SAFE TO CLEAR CAMERA is never sticky authority. Immediately invalidate the displayed predicate and stop new command dispatch on new camera assets, stale/invalid replica proof, relevant SSD disappearance/cloud verification loss, camera/storage identity change, required unknown discovery, incomplete enumeration or ledger inconsistency. Revalidation may later establish a new eligible plan; an old green label is never authorization. A destination or source evidence set cannot be silently substituted under an old confirmation even if a different set would satisfy the default policy.

Bind authorization to operation UUID, cleanup snapshot ID, exact asset-version/effect set and plan digest, camera identity, storage identity/generation evidence, replica identities/proof versions and policy, plus safety-validation generation/version. Persist user initiation/confirmation, active/cancelled state and expiry policy. Fresh pre-delete validation is mandatory after confirmation and immediately before dispatch. Material changes invalidate approval and require a freshly validated plan and explicit confirmation. A timestamp-only observation refresh may corroborate the same proof generation; it cannot silently rebind authorization to changed proof or scope.

## Partial-operation authorization decision

Automatic initiation of cleanup remains prohibited. Bounded continuation of an already explicitly authorized active operation is distinct from initiating a new operation. On transient connection loss stop commands, reconnect read-only and exhaustively reconcile against the original plan. The same authorization may cover only the still-present intended remainder when ALL are true:

- same live fenced CleanupCoordinator/operation; no process death, user pause/cancel/stop, service termination or camera restart;
- original authorization is unexpired (initial design maximum 10 minutes without successful progress; timeouts are conservative design limits to validate, not measured camera behavior);
- full relevant-store inventory succeeds; exact camera/storage identity, remaining asset versions/addresses and collateral scope match;
- current required replica proofs/availability, policy and safety-validation generation match; no new source, unknown member, inconsistent ledger or unexplained disappearance;
- differences are solely expected deletions within the confirmed set; per-item confirmed/unknown outcomes have been reconciled, never guessed from a response;
- dispatch is confined to the still-active user operation and its bounded recovery budget, not a generic sync timer, background job or future app-open callback.

No second dialog is required in that narrow case, and completed items are never reconfirmed or resent. The permission covers the original set; expected removals reduce the work set without expanding/redefining authorization. A new observation sequence number does not itself change the approved safety generation, but any changed material input does.

If any condition fails, preserve outcomes, revoke authorization, show the remaining intended scope and obtain one new explicit confirmation after revalidation. Process death/reboot, camera restart, unknown storage continuity, expiry or user stop always takes this path. UI-only recreation may attach to the live owner without dispatching a duplicate. No automatic recovery can mint authorization or override cancellation. Treat unconfirmed commands as UNKNOWN until complete same-source inventory resolves them; never blindly resend a handle.

## Acceptance

CL01-CL12 and S25 Ultra + Pocket 4P hardware evidence must cover exact-snapshot deletion; newer/excluded/unverified retention; primary/sidecar effects; partial interruption/reconnect; duplicate/already-absent requests; process and camera restart; wrong camera/card/storage; exhaustive post-enumeration and intended-absent/unintended-present checks; correct audit; and absence of unconfirmed automatic initiation. Use deliberately created non-critical test media only. Never use irreplaceable footage for initial destructive validation.

This ADR authorizes no code or hardware operation. GATE-1 begins separately as read-only architecture/security verification; all implementation/release flags remain false and hardware remains paused until explicit return.
