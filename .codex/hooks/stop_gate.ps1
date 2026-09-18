$null = $input | Out-String
$root = (git rev-parse --show-toplevel).Trim()
$state = Join-Path $root 'PROJECT_STATE.yaml'
$runtime = Join-Path $root '.codex/.runtime'
New-Item -ItemType Directory -Force -Path $runtime | Out-Null
$counter = Join-Path $runtime 'stop_gate_count.txt'
$n = if (Test-Path $counter) { [int](Get-Content $counter -Raw) } else { 0 }
if ($n -ge 8) { Remove-Item $counter -Force; @{decision='allow';reason='STOP-GATE circuit breaker reached while autonomous work remained.'} | ConvertTo-Json -Compress; exit 0 }
$raw = if (Test-Path $state) { Get-Content $state -Raw } else { '' }
$mode = [regex]::Match($raw,'"mode":\s*"([^"]+)"').Groups[1].Value
$micro = [regex]::Match($raw,'"micro_loop_stops_allowed":\s*(true|false)').Groups[1].Value
$work = [regex]::Match($raw,'"autonomous_work_remaining":\s*(true|false)').Groups[1].Value
$human = [regex]::Match($raw,'"human_stop_required":\s*(true|false)').Groups[1].Value
if ($mode -ne 'CONTINUOUS_AUTONOMOUS' -or $micro -eq 'true' -or $human -eq 'true' -or $work -eq 'false') { Remove-Item $counter -Force -ErrorAction SilentlyContinue; @{decision='allow'} | ConvertTo-Json -Compress; exit 0 }
$n++; Set-Content $counter $n -NoNewline
@{decision='block';reason='Do not return an intermediate progress report. Useful autonomous work remains. Continue the current integrated product/gate objective. If one path is blocked, queue it and continue other safe work. Before attempting to stop again, either make further material product progress or persist a genuine human_stop_required kind/reason and complete required hardware batching.'} | ConvertTo-Json -Compress
