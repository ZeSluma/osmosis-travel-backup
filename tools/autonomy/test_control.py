import copy
import unittest
import json
import subprocess
import sys
from control import evaluate, hook, load, ROOT


def state():
    return {"schema_version": 1, "software_work_remaining": True, "reachable_non_destructive_work_remaining": False,
            "software_preparation_exhausted": False, "human_stop": {"required": False, "kind": "NONE", "reason": ""},
            "hardware_validation": {"consolidated": False}, "work_items": [{"id": "TESTS", "state": "READY"}]}


def queued():
    return {"consolidated": False, "items": [], "batches": []}


def hardware_ready():
    s = state()
    s.update(software_work_remaining=False, software_preparation_exhausted=True, work_items=[])
    s["closure_audit"] = {"full_software_scope_complete": True, "evidence": "independent closure audit"}
    s["human_stop"] = {"required": True, "kind": "HARDWARE_BATCH", "reason": "Only physical evidence remains"}
    s["hardware_validation"]["consolidated"] = True
    q = queued()
    q["consolidated"] = True
    for n in range(4):
        q["items"].append(dict(id=f"HW{n}", state="WAITING_HARDWARE", question="question", why_hardware="physical fact",
                              software_evidence=["test"], prerequisites=["safe"], observation="observe", engineering_decision="branch",
                              batch_with=["other compatible checks"], software_preparation_complete=True,
                              criteria={"PASS": "yes", "FAIL": "contradiction", "INCONCLUSIVE": "unavailable"}))
    q["batches"] = [{"items": [i["id"] for i in q["items"]]}]
    return s, q


class AutonomyTests(unittest.TestCase):
    def test_adb_unavailable_is_local(self):
        s=state();s["human_stop"]={"required":True,"kind":"HARDWARE_BATCH","reason":"ADB unavailable"}
        self.assertEqual("CONTINUE", evaluate(s,queued())[0])
    def test_hardware_pending_with_simulator_work(self):
        s,q=hardware_ready();s["work_items"]=[{"id":"SIMULATOR","state":"READY"}]
        self.assertEqual("CONTINUE",evaluate(s,q)[0])
    def test_gate_pass_with_next_gate_ready(self):
        s=state();s["software_work_remaining"]=False;s["reachable_non_destructive_work_remaining"]=True
        self.assertEqual("CONTINUE",evaluate(s,queued())[0])
    def test_exhausted_four_checks_allow_single_batch(self):
        s,q=hardware_ready();self.assertEqual(("ALLOW","CONSOLIDATED_HARDWARE_BOUNDARY"),evaluate(s,q))
    def test_hardware_batch_needs_explicit_full_product_closure_audit(self):
        s,q=hardware_ready();s["closure_audit"]={"full_software_scope_complete":False,"evidence":"gap found"}
        self.assertEqual("CONTINUE",evaluate(s,q)[0])
    def test_credentials(self):
        s=state();s["human_stop"]={"required":True,"kind":"CREDENTIALS","reason":"Required authentication"}
        self.assertEqual("ALLOW",evaluate(s,queued())[0])
    def test_destructive(self):
        s=state();s["human_stop"]={"required":True,"kind":"DESTRUCTIVE_ACTION","reason":"Explicit approval missing"}
        self.assertEqual("ALLOW",evaluate(s,queued())[0])
    def test_safety_truth_approval_and_user_pause_override_work(self):
        for kind in ["SAFETY_SECURITY_DATA_INTEGRITY","SOURCE_OF_TRUTH","EXTERNAL_APPROVAL","USER_PAUSE"]:
            s=state();s["human_stop"]={"required":True,"kind":kind,"reason":"Real boundary"}
            self.assertEqual("ALLOW",evaluate(s,queued())[0])
    def test_one_continuation_then_loop_guard(self):
        first=hook({"hook_event_name":"Stop","stop_hook_active":False},state(),queued())
        self.assertEqual("block",first["decision"])
        self.assertEqual({},hook({"hook_event_name":"Stop","stop_hook_active":True},state(),queued()))
    def test_unconsolidated_or_incomplete_criteria_cannot_stop(self):
        s,q=hardware_ready();q["consolidated"]=False
        self.assertEqual("CONTINUE",evaluate(s,q)[0])
        s,q=hardware_ready();del q["items"][0]["criteria"]["INCONCLUSIVE"]
        self.assertEqual("CONTINUE",evaluate(s,q)[0])
    def test_missing_duplicate_and_stale_batch_members_rejected(self):
        for mode in ["missing","duplicate","extra"]:
            s,q=hardware_ready()
            if mode=="missing":q["batches"][0]["items"].pop()
            else:q["batches"][0]["items"].append("HW0" if mode=="duplicate" else "STALE")
            self.assertEqual("CONTINUE",evaluate(s,q)[0])
    def test_recurrence_requires_new_discriminating_hypothesis(self):
        s,q=hardware_ready();q["items"][0]["recurrence_of"]="known playback"
        self.assertEqual("CONTINUE",evaluate(s,q)[0])
        q["items"][0].update(new_hypothesis="changed decoder",competing_explanations=["split loss","transport omission"])
        self.assertEqual("ALLOW",evaluate(s,q)[0])
    def test_exhausted_work_not_forced_to_continue(self):
        s=state();s.update(software_work_remaining=False,software_preparation_exhausted=True,work_items=[])
        self.assertEqual("ALLOW",evaluate(s,queued())[0])
    def test_startup_and_compaction_get_short_same_contract(self):
        for source in ["startup","resume","clear","compact"]:
            output=hook({"hook_event_name":"SessionStart","source":source},state(),queued())
            context=output["hookSpecificOutput"]["additionalContext"]
            self.assertLess(len(context),1000);self.assertIn("LOCAL_PATH_WAITING_FOR_HARDWARE",context)
    def test_unknown_stop_is_not_permission_to_force_work(self):
        s=state();s["human_stop"]={"required":True,"kind":"UNKNOWN","reason":"unclear"}
        self.assertEqual("ALLOW",evaluate(s,queued())[0])
    def test_project_state_loads(self):
        s,q=load(ROOT);self.assertEqual(("CONTINUE","USEFUL_SOFTWARE_WORK_REMAINS"),evaluate(s,q))
    def test_hook_wire_protocol_and_loop_guard(self):
        for event,active in [("SessionStart",False),("Stop",False),("Stop",True)]:
            result=subprocess.run([sys.executable,str(ROOT/"tools/autonomy/control.py")],
                input=json.dumps({"hook_event_name":event,"cwd":str(ROOT),"stop_hook_active":active,"source":"compact"}),
                capture_output=True,text=True,timeout=5)
            self.assertEqual(0,result.returncode);output=json.loads(result.stdout)
            if event=="Stop":
                if active:self.assertEqual({},output)
                else:self.assertEqual("block",output["decision"])
            else:self.assertIn("additionalContext",output["hookSpecificOutput"])
    def test_malformed_input_and_wrong_workspace_never_force(self):
        for payload in ["not json",json.dumps({"hook_event_name":"Stop","cwd":str(ROOT.parent)})]:
            result=subprocess.run([sys.executable,str(ROOT/"tools/autonomy/control.py")],input=payload,capture_output=True,text=True,timeout=5)
            output=json.loads(result.stdout);self.assertNotIn("decision",output)
            self.assertIn("AUTONOMY_STATE_REVIEW_REQUIRED",output["systemMessage"])


if __name__ == "__main__":unittest.main()
