"""Read-only autonomy gate. Standard library only; no shell, network, secrets, or state writes."""
import argparse
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SAFETY_STOPS = {"CREDENTIALS", "EXTERNAL_APPROVAL", "DESTRUCTIVE_ACTION",
                "SAFETY_SECURITY_DATA_INTEGRITY", "SOURCE_OF_TRUTH", "USER_PAUSE"}
CONTINUE_REASON = (
    "Continue already-authorized, safe software work. Read PROJECT_STATE.yaml execution.work_items; "
    "queue hardware-dependent paths and select another reachable implementation/test/simulation task. "
    "Do not request isolated hardware actions or bypass safety, credentials, approvals, or gate prerequisites. "
    "Update execution state truthfully before the next completion attempt."
)


def load(root):
    text = (root / "PROJECT_STATE.yaml").read_text(encoding="utf-8-sig")
    matches = list(re.finditer(r"^execution:\s*(?=\{)", text, re.M))
    if len(matches) != 1:
        raise ValueError("EXECUTION_SECTION_MISSING_OR_DUPLICATED")
    # execution is an explicit JSON flow mapping, valid YAML; never implement a partial YAML parser.
    state, end = json.JSONDecoder().raw_decode(text[matches[0].end():])
    if state.get("schema_version") != 1:
        raise ValueError("UNSUPPORTED_EXECUTION_SCHEMA")
    location = state["hardware_validation"]["queue"]
    queue_path = (root / location).resolve()
    if not queue_path.is_relative_to(root.resolve()):
        raise ValueError("QUEUE_OUTSIDE_PROJECT")
    queue = json.loads(queue_path.read_text(encoding="utf-8-sig"))
    return state, queue


def work_remains(state):
    return (state["software_work_remaining"] is True or
            state["reachable_non_destructive_work_remaining"] is True or
            any(w.get("state") in {"READY", "IN_PROGRESS"} for w in state["work_items"]))


def batch_ready(state, queue):
    # Green task lists are not completion. A persisted full-product closure audit is required
    # separately, so a local hardware queue can never end software work by itself.
    closure_audit = state.get("closure_audit", {})
    if (work_remains(state) or state["software_preparation_exhausted"] is not True or
            closure_audit.get("full_software_scope_complete") is not True or
            not closure_audit.get("evidence")):
        return False
    if state["hardware_validation"]["consolidated"] is not True or queue.get("consolidated") is not True:
        return False
    pending = [item for item in queue["items"] if item.get("state") == "WAITING_HARDWARE"]
    if not pending:
        return False
    ids = [item["id"] for item in pending]
    if len(set(ids)) != len(ids):
        return False
    grouped = []
    for batch in queue.get("batches", []):
        if batch.get("state") == "BLOCKED_DEFERRED_EXTERNAL_HARDWARE_TOPOLOGY":
            continue
        members = batch.get("items", [])
        if any(key not in ids for key in members):
            return False
        grouped.extend(members)
    if sorted(grouped) != sorted(ids):
        return False
    for item in pending:
        if not all(item.get(key) for key in ("question", "why_hardware", "software_evidence", "prerequisites",
                                             "observation", "engineering_decision", "batch_with")):
            return False
        if item.get("software_preparation_complete") is not True:
            return False
        if not all(item.get("criteria", {}).get(key) for key in ("PASS", "FAIL", "INCONCLUSIVE")):
            return False
        if item.get("recurrence_of") and not (item.get("new_hypothesis") and item.get("competing_explanations")):
            return False
    return True


def evaluate(state, queue, active=False):
    """Return (action, fixed reason). ALLOW never implies any gate has passed."""
    if active:
        return "ALLOW", "CONTINUATION_LOOP_GUARD"
    stop = state["human_stop"]
    if stop["required"] is True and stop.get("kind") in SAFETY_STOPS and stop.get("reason"):
        return "ALLOW", "GENUINE_HUMAN_STOP"
    if stop["required"] is True and stop.get("kind") == "HARDWARE_BATCH":
        if batch_ready(state, queue):
            return "ALLOW", "CONSOLIDATED_HARDWARE_BOUNDARY"
        return "CONTINUE", "HARDWARE_STOP_NOT_GLOBAL"
    if stop["required"] is True:
        return "ALLOW", "UNRECOGNIZED_STOP_REQUIRES_STATE_RECONCILIATION"
    if work_remains(state):
        return "CONTINUE", "USEFUL_SOFTWARE_WORK_REMAINS"
    if state["software_preparation_exhausted"] is True:
        return "ALLOW", "SOFTWARE_EXHAUSTED"
    return "CONTINUE", "PREPARATION_NOT_EXHAUSTED"


def hook(payload, state, queue):
    event = payload.get("hook_event_name")
    if event == "Stop":
        # Only the documented literal boolean enables the guard; strings are not coerced to true.
        action, reason = evaluate(state, queue, payload.get("stop_hook_active") is True)
        if action == "CONTINUE":
            return {"decision": "block", "reason": CONTINUE_REASON}
        return {}
    if event == "SessionStart":
        action, reason = evaluate(state, queue)
        ready = [w["id"] for w in state["work_items"] if w.get("state") in {"READY", "IN_PROGRESS"}]
        ready = [key for key in ready if re.fullmatch(r"[A-Z0-9_-]{1,60}", key)][:8]
        context = (
            "Permanent Osmosis project contract: autonomous software work first across all gates. "
            "A hardware-dependent path is LOCAL_PATH_WAITING_FOR_HARDWARE, not a global stop. "
            f"Execution assessment={action}/{reason}; ready work IDs={','.join(ready) or 'none'}. "
            "Read PROJECT_STATE.yaml execution and docs/hardware/VALIDATION_QUEUE.json. "
            "Exhaust useful reachable safe work before one consolidated hardware session. "
            "Never bypass safety, approvals, credentials, source-of-truth conflicts or destructive boundaries."
        )
        return {"hookSpecificOutput": {"hookEventName": "SessionStart", "additionalContext": context}}
    return {}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    try:
        state, queue = load(ROOT)
        if args.check:
            action, reason = evaluate(state, queue)
            print(json.dumps({"action": action, "reason": reason, "hardware_batch_ready": batch_ready(state, queue)}))
            return
        raw = sys.stdin.read(65537)
        if len(raw) > 65536:
            raise ValueError("INPUT_TOO_LARGE")
        payload = json.loads(raw)
        cwd = Path(payload["cwd"]).resolve()
        if not cwd.is_relative_to(ROOT):
            raise ValueError("CWD_OUTSIDE_PROJECT")
        print(json.dumps(hook(payload, state, queue)))
    except (ValueError, KeyError, TypeError, OSError):
        # Corrupt/missing state must never force an unsafe continuation. No arbitrary input echoed.
        print(json.dumps({"systemMessage": "AUTONOMY_STATE_REVIEW_REQUIRED: reconcile execution state; no forced continuation."}))
        if args.check:
            sys.exit(1)


if __name__ == "__main__":
    main()
