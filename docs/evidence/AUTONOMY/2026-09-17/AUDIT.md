# Instruction/runtime and autonomy-control audit

Scope: C:/Users/Selim/CodexWork/osmosis-travel-backup, current working directory at repository root. Permanent project policy, not GATE-3-specific.

## Observed instruction/config sources

| Source | Evidence |
|---|---|
| Host system/developer instructions | Active conversation includes sandbox/approval rules, desktop/tool behavior, skills catalog and collaboration constraints. Repository files cannot override these. |
| Global AGENTS/override | Neither present under C:/Users/Selim/.codex. Checked ancestor paths; no additional files observed. |
| Project AGENTS | Root AGENTS.md is the only repository AGENTS/override file. Before update6818 bytes; below default32768-byte discovery limit. No configured fallback filenames or project_doc_max_bytes override. No file-size truncation evidence. |
| Nested instructions | Repository recursive inventory found none. Root-to-cwd chain contains only root AGENTS. |
| User config | C:/Users/Selim/.codex/config.toml; project is explicitly trusted. No developer_instructions, instruction-file override, approval/sandbox settings, feature overrides or inline hooks in this file. Model/UI/MCP/plugin settings retained. Credentials never read. |
| Existing project .codex | No project config/hooks existed before this work. |
| Rules | One global default.rules entry permits git switch -c. It supplies no autonomy/stop behavior. |
| Global/plugin hooks | No global hooks.json; no cached plugin hooks.json found. Global notify exists, separate from Stop/SessionStart policy. Inline custom plugin-manifest hook coverage has not been exhaustively proven. |
| Skills | Catalog is host-provided, not automatically every skill body. OpenAI Docs skill read for this audit; no skill requires routine hardware escalation. |
| Runtime | Local binary reports codex-cli0.155.0-alpha.2.6. Help exposes project configuration, strict-config and hook-trust options. Current session explicitly uses workspace-write and automatic approval review. Fresh-runtime effective config/prompt verification remains pending. |

## Cause and reconciliation

The previous root policy allowed a physical-hardware stop too broadly. PROJECT_STATE and the active plan explicitly described missing ADB as the next global stop, although useful software tasks remained. The last human checkpoint followed that stale operational instruction. There is no evidence that token truncation or a nested override caused it. The preloaded conversation also contains an older user-supplied AGENTS snapshot; editing the file does not retroactively prove a fresh runtime loaded it.

Root contract now precedes other project instructions. Structured execution state distinguishes local waiting from global stop; software and follow-on work remain true. The gate stays NOT_TESTED rather than globally BLOCKED by ADB. Current queue includes only new discriminating validations; known Playback/rescan observations are explicitly excluded.

## Runtime support and activation limits

Official docs were fetched, not inferred from memory: [AGENTS discovery](https://learn.chatgpt.com/docs/agent-configuration/agents-md), [hooks](https://learn.chatgpt.com/docs/hooks), [configuration reference](https://learn.chatgpt.com/docs/config-file/config-reference).

Supported documented choices: short developer_instructions; on-request with auto_review while retaining workspace-write; SessionStart compact context; Stop JSON continuation with stop_hook_active. Non-managed hooks require review/trust of the exact definition. No bypass flag or trust-store edit is used.

Ready configuration and hook definitions are staged at tools/autonomy/project-config.toml and project-hooks.json for the project .codex layer. They are NOT activated or claimed loaded. The expanded read-only runtime audit requiring escalation was rejected because automatic approval review hit an account usage limit; action did not execute. This is review infrastructure failure, not an unsafe-action determination. Protected .codex activation/fresh-runtime verification must await working approval review; unrelated workspace implementation/testing continues.

The Windows command resolves the git root and existing bundled Python interpreter. SessionStart includes compact; PostCompact is unnecessary for developer context. Hook definitions perform no file writes, network calls, media access or credential access. Runtime reload and hook trust remain external activation facts, distinct from script-level verification.

## Verification

Initial15 synthetic control tests PASS, including all eight requested cases. Wire-protocol and malformed-input tests added afterward; latest results recorded with the final checkpoint. Current `control.py --check` reports CONTINUE/USEFUL_SOFTWARE_WORK_REMAINS, hardware_batch_ready=false. A successful script test is SOFTWARE_PROVEN; actual runtime hook invocation is UNPROVEN until activated and observed. No completed gate was restarted.
