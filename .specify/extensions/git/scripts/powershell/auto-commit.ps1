#!/usr/bin/env pwsh
# Git extension: auto-commit.ps1
# Automatically commit changes after a Spec Kit command completes.
# Checks per-command config keys in git-config.yml before committing.
#
# Usage: auto-commit.ps1 <event_name> [generated_message]
#        auto-commit.ps1 <event_name> -MessageFile <path>
#        auto-commit.ps1 <event_name> -SynopsisFile <path>   (commit_style: snapshot)
#   e.g.: auto-commit.ps1 after_specify
#   e.g.: auto-commit.ps1 after_specify -MessageFile C:\temp\commit-msg.txt  (commit_style: conventional)
#   e.g.: auto-commit.ps1 before_implement -SynopsisFile C:\temp\synopsis.txt (commit_style: snapshot)
#
# -MessageFile is the preferred way to supply an agent-generated commit
# message: it reads the message from a file instead of a shell argument,
# so message content (which may contain quotes, $(...), backticks, etc.)
# is never interpolated into a shell command line.
param(
    [Parameter(Position = 0, Mandatory = $true)]
    [string]$EventName,

    # Optional agent-generated commit message (used when commit_style: conventional is configured).
    # Prefer -MessageFile over passing the message directly as a shell argument.
    [Parameter(Position = 1, Mandatory = $false)]
    [string]$GeneratedMessage = "",

    [Parameter(Mandatory = $false)]
    [string]$MessageFile = "",

    # Optional agent-revised synopsis bullets (used when commit_style: snapshot is configured).
    # Read and deleted before `git add .`, same transport-only pattern as -MessageFile.
    [Parameter(Mandatory = $false)]
    [string]$SynopsisFile = "",

    # Optional agent-authored free-text title (used when commit_style: snapshot is configured).
    # One line; read and deleted before `git add .`, same transport-only pattern.
    [Parameter(Mandatory = $false)]
    [string]$TitleFile = ""
)
$ErrorActionPreference = 'Stop'

if ($MessageFile) {
    if (-not (Test-Path $MessageFile -PathType Leaf)) {
        Write-Warning "[specify] Error: message file '$MessageFile' not found"
        exit 1
    }
    $GeneratedMessage = (Get-Content -Path $MessageFile -Raw)
    if ($null -ne $GeneratedMessage) {
        $GeneratedMessage = $GeneratedMessage.TrimEnd("`r", "`n")
    }
    # The message file is a transport-only artifact: its content is now
    # captured above, so remove it immediately. Otherwise, if it was written
    # inside the worktree, it would be picked up as an untracked change by
    # both the "any changes?" check below and by `git add .`, polluting the
    # commit or defeating the no-changes short-circuit even when nothing
    # else changed.
    Remove-Item -Path $MessageFile -Force -ErrorAction SilentlyContinue
}

# Snapshot mode: agent-revised synopsis bullets (transport-only artifact).
# Must be read and deleted BEFORE any `git add .` so the file itself is
# never staged into the snapshot it describes.
$synopsisLines = @()
if ($SynopsisFile) {
    if (-not (Test-Path $SynopsisFile -PathType Leaf)) {
        Write-Warning "[specify] Warning: synopsis file '$SynopsisFile' not found"
    } else {
        $rawSynopsis = (Get-Content -Path $SynopsisFile -Raw -Encoding UTF8)
        if ($rawSynopsis) {
            $synopsisLines = @($rawSynopsis -split "`r?`n" | ForEach-Object {
                ($_ -replace '^\s*-\s*', '').Trim()
            } | Where-Object { $_ })
        }
        Remove-Item -Path $SynopsisFile -Force -ErrorAction SilentlyContinue
    }
}

# Snapshot mode: agent-authored title text (transport-only artifact).
# Must be read and deleted BEFORE any `git add .` so the file itself is
# never staged into the snapshot it describes. The finally block guarantees
# the temporary file is removed even when reading fails.
$titleText = ""
if ($TitleFile) {
    try {
        if (-not (Test-Path $TitleFile -PathType Leaf)) {
            Write-Warning "[specify] Warning: title file '$TitleFile' not found"
        } else {
            $rawTitle = (Get-Content -Path $TitleFile -Raw -Encoding UTF8)
            if ($rawTitle) {
                $titleText = (($rawTitle -split "`r?`n" | Where-Object { $_.Trim() } | Select-Object -First 1) -replace '^\s*-\s*', '').Trim()
            }
        }
    } finally {
        Remove-Item -Path $TitleFile -Force -ErrorAction SilentlyContinue
    }
}

function Find-ProjectRoot {
    param([string]$StartDir)
    $current = Resolve-Path $StartDir
    while ($true) {
        foreach ($marker in @('.specify', '.git')) {
            if (Test-Path (Join-Path $current $marker)) {
                return $current
            }
        }
        $parent = Split-Path $current -Parent
        if ($parent -eq $current) { return $null }
        $current = $parent
    }
}

$repoRoot = Find-ProjectRoot -StartDir $PSScriptRoot
if (-not $repoRoot) { $repoRoot = Get-Location }
Set-Location $repoRoot

# Check if git is available
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Warning "[specify] Warning: Git not found; skipped auto-commit"
    exit 0
}

# Temporarily relax ErrorActionPreference so git stderr warnings
# (e.g. CRLF notices on Windows) do not become terminating errors.
$savedEAP = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try {
    git rev-parse --is-inside-work-tree 2>$null | Out-Null
    $isRepo = $LASTEXITCODE -eq 0
} finally {
    $ErrorActionPreference = $savedEAP
}
if (-not $isRepo) {
    Write-Warning "[specify] Warning: Not a Git repository; skipped auto-commit"
    exit 0
}

# Read per-command config from git-config.yml
$configFile = Join-Path $repoRoot ".specify/extensions/git/git-config.yml"
$enabled = $false
$commitMsg = ""
$commitStyle = "fixed"
# Snapshot title prefix; overridden by snapshot_prefix in git-config.yml.
# Built from a code point so this script stays pure ASCII (PS 5.1 reads
# BOM-less scripts as ANSI and would corrupt a literal emoji).
$snapPrefix = [string][char]::ConvertFromUtf32(0x1F4F8)

if (Test-Path $configFile) {
    # Snapshot title prefix (top-level scalar key); own pass so the
    # commit_style loop below may break early.
    foreach ($line in Get-Content $configFile -Encoding UTF8) {
        if ($line -match '^snapshot_prefix:\s*(.+)$') {
            $pfxVal = $matches[1].Trim() -replace '^["'']', '' -replace '["'']$', ''
            if ($pfxVal) { $snapPrefix = $pfxVal }
            break
        }
    }

    # Top-level scalar key: commit_style (fixed | conventional | snapshot)
    foreach ($line in Get-Content $configFile -Encoding UTF8) {
        if ($line -match '^commit_style:\s*(.+)$') {
            $styleVal = (($matches[1] -replace '\s+#.*$', '').Trim()) -replace '^["'']' -replace '["'']$'
            if ($styleVal) {
                $styleVal = $styleVal.ToLower()
                if ($styleVal -eq 'fixed' -or $styleVal -eq 'conventional' -or $styleVal -eq 'snapshot') {
                    $commitStyle = $styleVal
                } else {
                    Write-Warning "[specify] Warning: unknown commit_style '$styleVal' in git-config.yml (expected 'fixed', 'conventional' or 'snapshot'); defaulting to 'fixed'"
                }
            }
            break
        }
    }

    # Parse YAML to find auto_commit section
    $inAutoCommit = $false
    $inEvent = $false
    $defaultEnabled = $false

    foreach ($line in Get-Content $configFile -Encoding UTF8) {
        # Detect auto_commit: section
        if ($line -match '^auto_commit:') {
            $inAutoCommit = $true
            $inEvent = $false
            continue
        }

        # Exit auto_commit section on next top-level key
        if ($inAutoCommit -and $line -match '^[a-z]') {
            break
        }

        if ($inAutoCommit) {
            # Check default key
            if ($line -match '^\s+default:\s*(.+)$') {
                $val = $matches[1].Trim().ToLower()
                if ($val -eq 'true') { $defaultEnabled = $true }
            }

            # Detect our event subsection
            if ($line -match "^\s+${EventName}:") {
                $inEvent = $true
                continue
            }

            # Inside our event subsection
            if ($inEvent) {
                # Exit on next sibling key (2-space indent, not 4+)
                if ($line -match '^\s{2}[a-z]' -and $line -notmatch '^\s{4}') {
                    $inEvent = $false
                    continue
                }
                if ($line -match '\s+enabled:\s*(.+)$') {
                    $val = $matches[1].Trim().ToLower()
                    if ($val -eq 'true') { $enabled = $true }
                    if ($val -eq 'false') { $enabled = $false }
                }
                if ($line -match '\s+message:\s*(.+)$') {
                    $commitMsg = $matches[1].Trim() -replace '^["'']' -replace '["'']$'
                }
            }
        }
    }

    # If event-specific key not found, use default
    if (-not $enabled -and $defaultEnabled) {
        $hasEventKey = Select-String -Path $configFile -Pattern "^\s*${EventName}:" -Quiet
        if (-not $hasEventKey) {
            $enabled = $true
        }
    }
} else {
    # No config file -- auto-commit disabled by default
    exit 0
}

if (-not $enabled) {
    exit 0
}

# Check if there are changes to commit
# Relax ErrorActionPreference so CRLF warnings on stderr do not terminate.
$savedEAP = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try {
    git diff --quiet HEAD 2>$null; $d1 = $LASTEXITCODE
    git diff --cached --quiet 2>$null; $d2 = $LASTEXITCODE
    $untracked = git ls-files --others --exclude-standard 2>$null
} finally {
    $ErrorActionPreference = $savedEAP
}

if ($d1 -eq 0 -and $d2 -eq 0 -and -not $untracked) {
    Write-Host "[specify] No changes to commit after $EventName" -ForegroundColor DarkGray
    exit 0
}

# ---------------------------------------------------------------------------
# Snapshot mode (commit_style: snapshot)
#
# A rollback snapshot taken BEFORE the next phase runs: it freezes the state
# left by the previous phase. The title classifies the frozen phase from the
# staged diff (never from hook order, since /analyze and /checklist may run
# in either order). The body carries the synopsis (inherited from the previous
# snapshot, or supplied via -SynopsisFile), a "Concluído" ledger for the frozen
# phase, the next phase, and the staged --stat.
# ---------------------------------------------------------------------------
if ($commitStyle -eq 'snapshot') {

    function Test-StagedMatch {
        param([string[]]$Paths, [string]$Pattern)
        foreach ($p in $Paths) { if ($p -match $Pattern) { return $true } }
        return $false
    }

    # --- stage everything so the diffs below are authoritative -------------
    $savedEAP = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $out = git add . 2>&1 | Out-String
        if ($LASTEXITCODE -ne 0) { throw "git add failed: $out" }
    } catch {
        Write-Warning "[specify] Error: $_"
        exit 1
    } finally {
        $ErrorActionPreference = $savedEAP
    }

    $staged = @(git diff --cached --name-only 2>$null | Where-Object { $_ -and $_.Trim() })
    if (-not $staged -or $staged.Count -eq 0) {
        Write-Host "[specify] No staged changes after $EventName" -ForegroundColor DarkGray
        exit 0
    }

    # --- feature directory (specs/<branch>, fallback: newest specs/*) -------
    $branch = (git rev-parse --abbrev-ref HEAD 2>$null)
    $featureDir = $null
    if ($branch) {
        $candidate = Join-Path $repoRoot ("specs/" + $branch)
        if (Test-Path $candidate) { $featureDir = $candidate }
    }
    if (-not $featureDir) {
        $specsRoot = Join-Path $repoRoot "specs"
        if (Test-Path $specsRoot) {
            $newest = Get-ChildItem $specsRoot -Directory -ErrorAction SilentlyContinue |
                Sort-Object LastWriteTime -Descending | Select-Object -First 1
            if ($newest) { $featureDir = $newest.FullName }
        }
    }

    # --- frozen phase, from the staged diff ---------------------------------
    $frozenPhase = 'Snapshot'
    if (Test-StagedMatch $staged '^src/') {
        $frozenPhase = 'Implement'
    } elseif (Test-StagedMatch $staged '(^|/)tasks\.md$') {
        $frozenPhase = 'Tasks'
    } elseif (Test-StagedMatch $staged '(^|/)checklists/') {
        $frozenPhase = 'Checklist'
    } elseif ((Test-StagedMatch $staged '(^|/)(plan|research|data-model|quickstart)\.md$') -or
              (Test-StagedMatch $staged '(^|/)contracts/')) {
        $frozenPhase = 'Plan'
    } elseif (Test-StagedMatch $staged '(^|/)spec\.md$') {
        $frozenPhase = 'Specify'
    }

    # --- tasks.md location (used by Tasks summary and Implement bullets) ----
    $tasksRel = ($staged | Where-Object { $_ -match '(^|/)tasks\.md$' } | Select-Object -First 1)
    $tasksPath = $null
    if ($tasksRel) { $tasksPath = Join-Path $repoRoot "$tasksRel" }

    $taskCount = 0
    $phaseCount = 0
    if ($tasksPath -and (Test-Path $tasksPath)) {
        foreach ($l in (Get-Content $tasksPath -Encoding UTF8)) {
            if ($l -match '^- \[[ Xx]\] T\d+') { $taskCount++ }
            elseif ($l -match '^## Phase \d+') { $phaseCount++ }
        }
    }

    # --- per-phase title summary --------------------------------------------
    $summary = ''
    switch ($frozenPhase) {
        'Specify'   { $summary = 'spec.md' }
        'Plan' {
            $planCount = @($staged | Where-Object {
                $_ -match '(^|/)(plan|research|data-model|quickstart)\.md$' -or $_ -match '(^|/)contracts/'
            }).Count
            $summary = "$planCount artefatos de design"
        }
        'Tasks'     { $summary = "$taskCount tarefas em $phaseCount fases" }
        'Implement' { $summary = "$($staged.Count) arquivos" }
        'Checklist' {
            $checkCount = @($staged | Where-Object { $_ -match '(^|/)checklists/' }).Count
            if ($checkCount -eq 0 -and $featureDir) {
                $checkDir = Join-Path $featureDir 'checklists'
                if (Test-Path $checkDir) {
                    $checkCount = @(Get-ChildItem $checkDir -File -ErrorAction SilentlyContinue).Count
                }
            }
            $summary = "$checkCount listas"
        }
        default     { $summary = "$($staged.Count) arquivos" }
    }

    # --- Implement bullets: tasks flipped [X] in the staged tasks.md diff ----
    $taskIds = @()
    $taskBullets = @()
    if ($frozenPhase -eq 'Implement' -and $tasksPath -and (Test-Path $tasksPath)) {
        # full path -> status letter, plus basename -> paths for tasks that
        # reference a file by name only (e.g. `__entity_name__ContractTest.java`).
        $statusMap = @{}
        $byName = @{}
        foreach ($l in (git diff --cached --name-status 2>$null)) {
            if ($l -match '^([AMD])\s+(.+)$') {
                $changedPath = $matches[2]
                $statusMap[$changedPath] = $matches[1]
                $changedName = ($changedPath -split '/')[-1]
                if (-not $byName.ContainsKey($changedName)) { $byName[$changedName] = @() }
                $byName[$changedName] += $changedPath
            }
        }
        foreach ($l in (git diff --cached -U0 -- $tasksPath 2>$null)) {
            if ($l -match '^\+- \[X\] (T\d+)\s+(.+)$') {
                $taskId = $matches[1]
                # Drop leading [P] / [USn] markers: they are scheduling hints,
                # not part of the task description.
                $taskText = (($matches[2] -replace '^(?:\[(?:P|US\d+)\]\s*)+', '') -replace '\s+', ' ').Trim()
                # A task that names a staged file yields "<status> <basename>";
                # a unique basename match covers tasks that omit the directory.
                $letter = ''
                $fileName = ''
                foreach ($mp in [regex]::Matches($taskText, '\x60([^\x60]+)\x60')) {
                    $cand = $mp.Groups[1].Value
                    $found = $null
                    if ($statusMap.ContainsKey($cand)) {
                        $found = $cand
                    } elseif ($byName.ContainsKey($cand) -and $byName[$cand].Count -eq 1) {
                        $found = $byName[$cand][0]
                    }
                    if ($found) {
                        $letter = $statusMap[$found]
                        $fileName = ($found -split '/')[-1]
                        break
                    }
                }
                if ($fileName) {
                    $bullet = "- ${taskId}: $letter $fileName"
                } else {
                    # No resolvable file: keep the task's own wording up to the
                    # first colon (the rest is usually acceptance detail).
                    $head = $taskText
                    $colonIdx = $taskText.IndexOf(':')
                    if ($colonIdx -gt 0) { $head = $taskText.Substring(0, $colonIdx) }
                    $head = (($head -replace '\x60', '') -replace '\s+', ' ').Trim()
                    $bullet = "- ${taskId}: $head"
                }
                $taskBullets += $bullet
                $taskIds += [int]$taskId.Substring(1)
            }
        }
        $taskIds = @($taskIds | Sort-Object -Unique)
        if ($taskBullets.Count -gt 10) {
            $extra = $taskBullets.Count - 10
            $taskBullets = @($taskBullets | Select-Object -First 10)
            $taskBullets += "- (+$extra tarefas)"
        }
    }

    # contiguous range for the title (en dash built from a code point)
    $rangeText = ''
    if ($taskIds.Count -eq 1) {
        $rangeText = 'T{0:D3}' -f $taskIds[0]
    } elseif ($taskIds.Count -gt 1) {
        $firstId = 'T{0:D3}' -f $taskIds[0]
        $lastId = 'T{0:D3}' -f $taskIds[$taskIds.Count - 1]
        if ($taskIds.Count -eq ($taskIds[$taskIds.Count - 1] - $taskIds[0] + 1)) {
            $rangeText = "$firstId$([char]0x2013)$lastId"
        } elseif ($taskIds.Count -le 3) {
            $rangeText = (@($taskIds | ForEach-Object { 'T{0:D3}' -f $_ }) -join ',')
        } else {
            $rangeText = "$firstId$([char]0x2013)$lastId +$($taskIds.Count - 2)"
        }
    }
    if ($rangeText) { $summary = "$summary ($rangeText)" }

    # --- title (max 72 chars) ------------------------------------------------
    # An agent-authored title (-TitleFile) replaces the automatic per-phase
    # summary; the first letter is capitalized for consistency.
    if ($titleText) {
        $summary = $titleText.Substring(0, 1).ToUpperInvariant() + $titleText.Substring(1)
    }
    $titleHead = "$snapPrefix $frozenPhase`: "
    $allowed = 72 - $titleHead.Length
    if ($allowed -lt 1) { $allowed = 1 }
    if ($summary.Length -gt $allowed) {
        $title = "$titleHead$($summary.Substring(0, $allowed - 1))$([char]0x2026)"
    } else {
        $title = "$titleHead$summary"
    }

    # --- synopsis (inherited from the previous snapshot, like /git-commit) ---
    # The agent may supply a revised synopsis via -SynopsisFile. Otherwise the
    # bullets of the previous snapshot's Sinopse are carried over. There is no
    # heuristic fallback: without either source, the section is omitted.
    $synopsis = @()
    if ($synopsisLines.Count -gt 0) {
        $synopsis = @($synopsisLines)
    } else {
        $prevBody = @()
        $ErrorActionPreference = 'Continue'
        try { $prevBody = @(git log -1 --format=%B 2>$null) } finally { $ErrorActionPreference = $savedEAP }
        $inSynopsis = $false
        foreach ($l in $prevBody) {
            if ($l -match '^Sinopse\s*$') { $inSynopsis = $true; continue }
            if ($inSynopsis) {
                if ($l -match '^Conclu' -or ($l -notmatch '^\s*-\s' -and $l -match '\S')) { break }
                if ($l -match '^\s*-\s*(.+)$') { $synopsis += $matches[1].Trim() }
            }
        }
    }
    # dedupe + cap at 6
    $synFinal = @()
    $seen = @{}
    foreach ($s in $synopsis) {
        $key = $s.ToLower()
        if (-not $seen.ContainsKey($key)) { $seen[$key] = $true; $synFinal += $s }
        if ($synFinal.Count -ge 6) { break }
    }

    # --- "Concluído" ledger for the frozen phase -----------------------------
    $doneLines = @()
    if ($featureDir) {
        $specPath = Join-Path $featureDir 'spec.md'
        if (Test-Path $specPath) {
            $storyCount = 0; $sessionCount = 0; $hasClarifications = $false
            foreach ($l in (Get-Content $specPath -Encoding UTF8)) {
                if ($l -match '^### User Story') { $storyCount++ }
                elseif ($l -match '^### Session ') { $sessionCount++ }
                elseif ($l -match '^## Clarifications') { $hasClarifications = $true }
            }
            $doneLines += "Specify: $storyCount histórias"
            if ($hasClarifications) { $doneLines += "Clarify: $sessionCount sessões" }
        }
        $artCount = 0
        foreach ($n in @('plan.md', 'research.md', 'data-model.md', 'quickstart.md')) {
            if (Test-Path (Join-Path $featureDir $n)) { $artCount++ }
        }
        $contractsDir = Join-Path $featureDir 'contracts'
        if (Test-Path $contractsDir) {
            $artCount += @(Get-ChildItem $contractsDir -File -ErrorAction SilentlyContinue).Count
        }
        if ($artCount -gt 0) { $doneLines += "Plan: $artCount artefatos de design" }
        if ($taskCount -gt 0 -or (Test-Path (Join-Path $featureDir 'tasks.md'))) {
            $doneLines += "Tasks: $taskCount tarefas / $phaseCount fases"
        }
        if (Test-Path (Join-Path $featureDir 'analysis.md')) {
            $doneLines += "Analyze: relatório presente"
        }
        $implCount = @($staged | Where-Object { $_ -match '^src/' }).Count
        if ($implCount -gt 0) { $doneLines += "Implement: $implCount arquivos" }
        $checkDir = Join-Path $featureDir 'checklists'
        if (Test-Path $checkDir) {
            $checkTotal = @(Get-ChildItem $checkDir -File -ErrorAction SilentlyContinue).Count
            if ($checkTotal -gt 0) { $doneLines += "Checklist: $checkTotal listas" }
        }
    }

    # --- next phase, from the hook event name --------------------------------
    $nextPhase = ($EventName -replace '^before_', '')
    if ($nextPhase) {
        $nextPhase = $nextPhase.Substring(0, 1).ToUpper() + $nextPhase.Substring(1)
    }

    # --- assemble body -------------------------------------------------------
    $bodyParts = @()
    if ($synFinal.Count -gt 0) {
        $bodyParts += 'Sinopse'
        foreach ($s in $synFinal) { $bodyParts += "- $s" }
        $bodyParts += ''
    }
    if ($taskBullets.Count -gt 0) {
        foreach ($b in $taskBullets) { $bodyParts += $b }
        $bodyParts += ''
    }
    if ($doneLines.Count -gt 0) {
        $bodyParts += 'Concluído'
        foreach ($d in $doneLines) { $bodyParts += "- $d" }
        $bodyParts += ''
    }
    if ($nextPhase) {
        $bodyParts += "A seguir: $nextPhase"
        $bodyParts += ''
    }
    $stat = (git diff --cached --stat 2>$null | Out-String).Trim()
    if ($stat) { $bodyParts += $stat }
    $body = (($bodyParts -join "`n")).TrimEnd()

    # --- commit the snapshot --------------------------------------------------
    $ErrorActionPreference = 'Continue'
    try {
        $out = git commit -q -m $title -m $body 2>&1 | Out-String
        if ($LASTEXITCODE -ne 0) { throw "git commit failed: $out" }
    } catch {
        Write-Warning "[specify] Error: $_"
        exit 1
    } finally {
        $ErrorActionPreference = $savedEAP
    }

    Write-Host "[OK] Snapshot committed ($frozenPhase): $title"
    exit 0
}

# In conventional mode, the commit message must be supplied by the agent
# (via the GeneratedMessage argument); never fall back to the fixed message.
if ($commitStyle -eq 'conventional') {
    if ($GeneratedMessage) {
        $commitMsg = $GeneratedMessage
    } else {
        Write-Warning "[specify] Error: commit_style is 'conventional' but no generated commit message was supplied; aborting auto-commit (pass -MessageFile <path>, or a raw message as arg 2, or set commit_style: fixed)"
        exit 1
    }
}

# Derive a human-readable command name from the event
$commandName = $EventName -replace '^after_', '' -replace '^before_', ''
$phase = if ($EventName -match '^before_') { 'before' } else { 'after' }

# Use custom message if configured, otherwise default
if (-not $commitMsg) {
    $commitMsg = "[Spec Kit] Auto-commit $phase $commandName"
}

# Stage and commit
# Relax ErrorActionPreference so CRLF warnings on stderr do not terminate,
# while still allowing redirected error output to be captured for diagnostics.
$savedEAP = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try {
    $out = git add . 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) { throw "git add failed: $out" }
    $out = git commit -q -m $commitMsg 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) { throw "git commit failed: $out" }
} catch {
    Write-Warning "[specify] Error: $_"
    exit 1
} finally {
    $ErrorActionPreference = $savedEAP
}

Write-Host "[OK] Changes committed $phase $commandName"
