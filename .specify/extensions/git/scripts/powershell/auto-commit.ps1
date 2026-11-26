#!/usr/bin/env pwsh
# SNAPSHOT-AUTOCOMMIT-V2
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
    [string]$TitleFile = "",

    # Optional agent-authored phase items (used when commit_style: snapshot is configured).
    # One item per line; read and deleted before `git add .`, same transport-only pattern.
    [Parameter(Mandatory = $false)]
    [string]$ItemsFile = ""
)
$ErrorActionPreference = 'Stop'

if ($MessageFile) {
    if (-not (Test-Path $MessageFile -PathType Leaf)) {
        Write-Warning "[specify] Error: message file '$MessageFile' not found"
        exit 1
    }
    $GeneratedMessage = (Get-Content -Path $MessageFile -Raw -Encoding UTF8)
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

# Snapshot mode: agent-authored phase items (transport-only artifact).
# One item per line, emitted verbatim under the phase section. Must be read
# and deleted BEFORE any `git add .` so the file itself is never staged.
$itemLines = @()
if ($ItemsFile) {
    if (-not (Test-Path $ItemsFile -PathType Leaf)) {
        Write-Warning "[specify] Warning: items file '$ItemsFile' not found"
    } else {
        $rawItems = (Get-Content -Path $ItemsFile -Raw -Encoding UTF8)
        if ($rawItems) {
            $parsedItems = @($rawItems -split "`r?`n" | ForEach-Object {
                ($_ -replace '^\s*-\s*', '').TrimEnd()
            })
            $firstItem = 0
            while ($firstItem -lt $parsedItems.Count -and -not $parsedItems[$firstItem].Trim()) { $firstItem++ }
            $lastItem = $parsedItems.Count - 1
            while ($lastItem -ge $firstItem -and -not $parsedItems[$lastItem].Trim()) { $lastItem-- }
            if ($lastItem -ge $firstItem) { $itemLines = @($parsedItems[$firstItem..$lastItem]) }
        }
        Remove-Item -Path $ItemsFile -Force -ErrorAction SilentlyContinue
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
# A rollback snapshot taken BEFORE the next command runs: it freezes the state
# left by the previous phase. The title classifies the frozen phase from the
# staged diff (never from hook order, since /analyze and /checklist may run
# in either order). The body carries the synopsis (inherited from the previous
# snapshot, or supplied via -SynopsisFile), a "Concluído" ledger for the frozen
# phase, the next command, and the staged --stat.
# ---------------------------------------------------------------------------
if ($commitStyle -eq 'snapshot') {

    function Test-StagedMatch {
        param([string[]]$Paths, [string]$Pattern)
        foreach ($p in $Paths) { if ($p -match $Pattern) { return $true } }
        return $false
    }

    # Emoji kept out of the literal script text (non-BMP code points).
    $emBullet  = [char]::ConvertFromUtf32(0x1F4CB)   # clipboard
    $emCheck   = [char]0x2705                        # check mark
    $emPin     = [char]::ConvertFromUtf32(0x1F4CC)   # pushpin
    $emFolder  = [char]::ConvertFromUtf32(0x1F4C2)   # open folder
    $emDash    = [char]0x2014                        # em dash
    $emPoint   = [char]::ConvertFromUtf32(0x1F449)   # pointing hand
    $flowArrow = [char]0x27A1                        # flow separator

    # Shorten a task/requirement text to a 2-3 word summary. Deterministic:
    # drops cited file paths and code ticks and leading modal verbs, cuts at
    # the first separator, then caps the result at 48 characters.
    function Compress-ItemText {
        param([string]$Text)
        $hadPath = $Text -match '`[^`]+`'
        $t = $Text -replace '`[^`]+`', ' '
        $t = $t -replace '`', ''
        $t = $t -replace '^(System|The system|O sistema)\s+(MUST|DEVE|deve)\s+', ''
        foreach ($sep in @(' — ', ' – ', ': ', '. ', ' (')) {
            $i = $t.IndexOf($sep)
            if ($i -gt 0) { $t = $t.Substring(0, $i) }
        }
        $t = ($t -replace '\s+', ' ').Trim()
        if ($hadPath) {
            $t = ($t -creplace '\s+(em|no|na|de|do|da|para|com|in|at|to|from|using)$', '').Trim()
        }
        $t = $t.TrimEnd(',', ';', '.', ':')
        if ($t) { $t = $t.Substring(0, 1).ToUpperInvariant() + $t.Substring(1) }
        if ($t.Length -gt 48) {
            $t = $t.Substring(0, 48)
            $sp = $t.LastIndexOf(' ')
            if ($sp -gt 20) { $t = $t.Substring(0, $sp) }
            $t = $t.TrimEnd(',', ';', '.', ':') + [char]0x2026
        }
        if (-not $t) { return [char]0x2B1C }
        return $t
    }

    # Flow line: canonical order up to the next mandatory command.
    function Get-FlowText {
        param([string]$Phase)
        $flow = @('Constitution', 'Specify', 'Clarify', 'Plan', 'Checklist', 'Tasks', 'Analyze', 'Implement', 'Converge')
        $stop = 8
        switch ($Phase) {
            'Constitution' { $stop = 1 }
            'Specify'      { $stop = 3 }
            'Clarify'      { $stop = 3 }
            'Plan'         { $stop = 5 }
            'Checklist'    { $stop = 5 }
            'Tasks'        { $stop = 7 }
            'Analyze'      { $stop = 7 }
        }
        return (($flow[0..$stop]) -join " $flowArrow  ")
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
    # `git rev-parse` fails on an unborn HEAD (fresh repository); keep the
    # warning from becoming a terminating error.
    $branch = $null
    $savedEapBranch = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try { $branch = (git rev-parse --abbrev-ref HEAD 2>$null) } finally { $ErrorActionPreference = $savedEapBranch }
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

    # --- frozen phase: hook event narrows the candidates, staged diff decides -
    # Phases in Spec Kit order. A before_* hook freezes whatever the previous
    # phase left, so only earlier phases are candidates; an after_* hook
    # freezes that phase itself.
    $phaseOrder = @('Constitution', 'Specify', 'Clarify', 'Plan', 'Tasks', 'Implement', 'Checklist', 'Analyze', 'Converge', 'Task2Issues')
    $phaseOfEvent = @{
        'constitution' = 'Constitution'
        'specify'      = 'Specify'
        'clarify'      = 'Clarify'
        'plan'         = 'Plan'
        'tasks'        = 'Tasks'
        'implement'    = 'Implement'
        'checklist'    = 'Checklist'
        'analyze'      = 'Analyze'
        'converge'     = 'Converge'
        'taskstoissues' = 'Task2Issues'
    }

    function Get-FrozenPhaseCandidates {
        param([string]$Name)
        if ($Name -match '^after_(.+)$') {
            $phase = $phaseOfEvent[$matches[1]]
            if ($phase) { return @($phase) }
            return @()
        }
        if ($Name -match '^before_(.+)$') {
            $phase = $phaseOfEvent[$matches[1]]
            if (-not $phase) { return @() }
            $index = [array]::IndexOf($phaseOrder, $phase)
            if ($index -le 0) { return @() }
            return @($phaseOrder[0..($index - 1)])
        }
        return @()
    }

    # spec.md feeds both the Specify/Clarify distinction and the Clarify details.
    $specRel = ($staged | Where-Object { $_ -match '(^|/)spec\.md$' } | Select-Object -First 1)
    $specText = ''
    if ($specRel) {
        $specPath = Join-Path $repoRoot $specRel
        if (Test-Path $specPath) {
            $specText = Get-Content $specPath -Raw -Encoding UTF8
            if ($null -eq $specText) { $specText = '' }
        }
    }

    function Get-DetectedPhases {
        $detected = @()
        if (Test-StagedMatch $staged '^\.specify/memory/constitution\.md$') { $detected += 'Constitution' }
        if ($specRel) {
            # /clarify appends a "## Clarifications" section to spec.md.
            if ($specText -match '(?m)^## Clarifications') { $detected += 'Clarify' }
            else { $detected += 'Specify' }
        }
        if ((Test-StagedMatch $staged '(^|/)(plan|research|data-model|quickstart)\.md$') -or
            (Test-StagedMatch $staged '(^|/)contracts/')) { $detected += 'Plan' }
        if (Test-StagedMatch $staged '(^|/)tasks\.md$') { $detected += 'Tasks' }
        if (Test-StagedMatch $staged '^src/') { $detected += 'Implement' }
        if (Test-StagedMatch $staged '(^|/)checklists/') { $detected += 'Checklist' }
        if (Test-StagedMatch $staged '(^|/)analysis\.md$') { $detected += 'Analyze' }
        # /converge appends a "## Phase N: Convergence" section to tasks.md.
        if (Test-StagedMatch $staged '(^|/)tasks\.md$') {
            $tRel = ($staged | Where-Object { $_ -match '(^|/)tasks\.md$' } | Select-Object -First 1)
            if ($tRel) {
                $tPath = Join-Path $repoRoot $tRel
                if (Test-Path $tPath) {
                    $tText = Get-Content $tPath -Raw -Encoding UTF8
                    if ($null -eq $tText) { $tText = '' }
                    if ($tText -match '(?m)^## Phase \d+: Convergence\b') { $detected += 'Converge' }
                }
            }
        }
        # Most advanced phase first.
        [array]::Reverse($detected)
        return $detected
    }

    # An after_* hook states which phase just finished, so its label is
    # authoritative (Task2Issues, for example, leaves no file signature).
    # A before_* hook narrows the candidates and the staged diff decides;
    # without a known event, the most advanced detected phase wins. This keeps
    # a leftover checklists/ file from mislabelling a Specify snapshot.
    $candidates = @(Get-FrozenPhaseCandidates $EventName)
    $detectedPhases = @(Get-DetectedPhases)
    $frozenPhase = 'Snapshot'
    if ($EventName -match '^after_' -and $candidates.Count -gt 0) {
        $frozenPhase = $candidates[0]
    } elseif ($candidates.Count -gt 0) {
        foreach ($phase in $detectedPhases) {
            if ($candidates -contains $phase) { $frozenPhase = $phase; break }
        }
    } elseif ($detectedPhases.Count -gt 0) {
        $frozenPhase = $detectedPhases[0]
    }

    # --- tasks.md location (used by Tasks summary and Implement bullets) ----
    $tasksRel = ($staged | Where-Object { $_ -match '(^|/)tasks\.md$' } | Select-Object -First 1)
    $tasksPath = $null
    if ($tasksRel) { $tasksPath = Join-Path $repoRoot "$tasksRel" }

    # The ledger counts every task of the feature, not only the staged ones.
    $tasksCountPath = $tasksPath
    if (-not $tasksCountPath -and $featureDir) {
        $candidateTasks = Join-Path $featureDir 'tasks.md'
        if (Test-Path $candidateTasks) { $tasksCountPath = $candidateTasks }
    }

    $taskCount = 0
    $phaseCount = 0
    if ($tasksCountPath -and (Test-Path $tasksCountPath)) {
        foreach ($l in (Get-Content $tasksCountPath -Encoding UTF8)) {
            if ($l -match '^- \[[ Xx]\] T\d+') { $taskCount++ }
            elseif ($l -match '^## Phase \d+') { $phaseCount++ }
        }
    }

    # --- per-phase title summary --------------------------------------------
    $summary = ''
    switch ($frozenPhase) {
        'Specify'   { $summary = 'spec.md' }
        'Clarify'   {
            $sessions = @($specText -split "`r?`n" | Where-Object { $_ -match '^### Session ' }).Count
            $summary = "$sessions sessões"
        }
        'Plan' {
            $planCount = @($staged | Where-Object {
                $_ -match '(^|/)(plan|research|data-model|quickstart)\.md$' -or $_ -match '(^|/)contracts/'
            }).Count
            $summary = "$planCount artefatos planejados"
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
        'Analyze'      { $summary = 'analysis.md' }
        'Converge'     { $summary = 'tasks.md' }
        'Task2Issues'  { $summary = 'issues' }
        'Constitution' { $summary = 'constitution.md' }
        default     { $summary = "$($staged.Count) arquivos" }
    }

    # --- phase items: agent-supplied, or derived from the staged artifacts ---
    $taskIds = @()
    $phaseItems = @()
    if ($itemLines.Count -gt 0) {
        $phaseItems = @($itemLines)
    }
    if ($featureDir -and ($itemLines.Count -eq 0 -or $frozenPhase -eq 'Implement')) {
        switch ($frozenPhase) {
            'Specify' {
                $specPath = Join-Path $featureDir 'spec.md'
                if (Test-Path $specPath) {
                    $usLines = @()
                    $frLines = @()
                    foreach ($l in (Get-Content $specPath -Encoding UTF8)) {
                        if ($l -match '^### User Story\s+(\d+)\s*[-\u2013\u2014]\s*(.+?)\s*(?:\(Priority:.*\))?$') {
                            $usLines += "$emCheck US$($matches[1]): $($matches[2])"
                        } elseif ($l -match '^- \*\*(FR-\d+)\*\*:\s*(.+)$') {
                            $frLines += "$emCheck $($matches[1]): $(Compress-ItemText $matches[2])"
                        }
                    }
                    if ($usLines.Count -gt 0) {
                        $phaseItems += '* US — User Stories'
                        $phaseItems += $usLines
                    }
                    if ($frLines.Count -gt 0) {
                        if ($phaseItems.Count -gt 0) { $phaseItems += '' }
                        $phaseItems += '* FR — Functional Requirements'
                        $phaseItems += $frLines
                    }
                }
            }
            'Clarify' {
                $specPath = Join-Path $featureDir 'spec.md'
                if (Test-Path $specPath) {
                    $specLines = @(Get-Content $specPath -Encoding UTF8)
                    for ($i = 0; $i -lt $specLines.Count; $i++) {
                        if ($specLines[$i] -match '^### Session\s+(\d{4}-\d{2}-\d{2})') {
                            $sessionDate = $matches[1]
                            $sessionText = ''
                            for ($j = $i + 1; $j -lt $specLines.Count; $j++) {
                                $candidate = $specLines[$j].Trim()
                                if ($candidate -match '^#{1,3}\s') { break }
                                if ($candidate) { $sessionText = $candidate; break }
                            }
                            $sessionText = $sessionText -replace '^[-*]\s+', ''
                            $sessionText = $sessionText -replace '^(\*\*)?[Qq](\*\*)?:(\*\*)?\s*', ''
                            if ($sessionText) {
                                $phaseItems += "$emCheck ${sessionDate}: $(Compress-ItemText $sessionText)"
                            } else {
                                $phaseItems += "$emCheck ${sessionDate}"
                            }
                        }
                    }
                }
            }
            'Plan' {
                $planPaths = @($staged | Where-Object {
                    $_ -match '(^|/)(plan|research|data-model|quickstart)\.md$' -or $_ -match '(^|/)contracts/'
                } | Sort-Object)
                foreach ($p in $planPaths) {
                    $rel = $p -replace '\\', '/'
                    $prefix = ($featureDir -replace '\\', '/').TrimEnd('/') + '/'
                    if ($rel.StartsWith($prefix)) { $rel = $rel.Substring($prefix.Length) }
                    $phaseItems += "$emCheck $rel"
                }
            }
            'Tasks' {
                if ($tasksPath -and (Test-Path $tasksPath)) {
                    foreach ($l in (Get-Content $tasksPath -Encoding UTF8)) {
                        if ($l -match '^- \[[ Xx]\] (T\d+)\s+(.+)$') {
                            $tId = $matches[1]
                            $tText = (($matches[2] -replace '^(?:\[(?:P|US\d+)\]\s*)+', '') -replace '\s+', ' ')
                            $phaseItems += "$emCheck ${tId}: $(Compress-ItemText $tText)"
                        }
                    }
                }
            }
            'Converge' {
                if ($tasksPath -and (Test-Path $tasksPath)) {
                    $inConvergence = $false
                    foreach ($l in (Get-Content $tasksPath -Encoding UTF8)) {
                        if ($l -match '^## Phase \d+: Convergence\b') { $inConvergence = $true; continue }
                        if ($l -match '^## ') { $inConvergence = $false; continue }
                        if ($inConvergence -and $l -match '^- \[[ Xx]\] (T\d+)\s+(.+)$') {
                            $tId = $matches[1]
                            $tText = (($matches[2] -replace '^(?:\[(?:P|US\d+)\]\s*)+', '') -replace '\s+', ' ')
                            $phaseItems += "$emCheck ${tId}: $(Compress-ItemText $tText)"
                        }
                    }
                }
            }
            'Checklist' {
                $checkPrefix = ($featureDir -replace '\\', '/').TrimEnd('/') + '/'
                $checkPaths = @($staged | Where-Object { $_ -match '(^|/)checklists/' } | Sort-Object)
                foreach ($p in $checkPaths) {
                    $rel = $p -replace '\\', '/'
                    if ($rel.StartsWith($checkPrefix)) { $rel = $rel.Substring($checkPrefix.Length) }
                    $phaseItems += "$emCheck $rel"
                }
                if ($phaseItems.Count -eq 0) {
                    $checkDir = Join-Path $featureDir 'checklists'
                    if (Test-Path $checkDir) {
                        foreach ($f in @(Get-ChildItem $checkDir -File -ErrorAction SilentlyContinue | Sort-Object Name)) {
                            $phaseItems += "$emCheck checklists/$($f.Name)"
                        }
                    }
                }
            }
            'Analyze' {
                $analysisPath = Join-Path $featureDir 'analysis.md'
                if (Test-Path $analysisPath) {
                    foreach ($l in (Get-Content $analysisPath -Encoding UTF8)) {
                        if ($l -match '^\|\s*([A-Z][0-9]+)\s*\|') {
                            $findingId = $matches[1]
                            $cells = @($l.Trim().Trim('|') -split '\|')
                            $findingSummary = ''
                            if ($cells.Count -ge 5) { $findingSummary = $cells[4].Trim() }
                            if (-not $findingSummary -and $cells.Count -ge 4) { $findingSummary = $cells[3].Trim() }
                            if ($findingSummary) {
                                $phaseItems += "$emCheck ${findingId}: $(Compress-ItemText $findingSummary)"
                            } else {
                                $phaseItems += "$emCheck $findingId"
                            }
                        }
                    }
                    if ($phaseItems.Count -eq 0) { $phaseItems += "$emCheck analysis.md" }
                }
            }
            'Implement' {
                if ($tasksPath -and (Test-Path $tasksPath)) {
                    # full path -> status letter, plus basename -> paths for tasks
                    # that reference a file by name only.
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
                            $tId = $matches[1]
                            # Drop leading [P] / [USn] markers: they are scheduling
                            # hints, not part of the task description.
                            $tText = (($matches[2] -replace '^(?:\[(?:P|US\d+)\]\s*)+', '') -replace '\s+', ' ').Trim()
                            $letter = ''
                            $fullPath = ''
                            foreach ($mp in [regex]::Matches($tText, '\x60([^\x60]+)\x60')) {
                                $cand = $mp.Groups[1].Value
                                $found = $null
                                if ($statusMap.ContainsKey($cand)) {
                                    $found = $cand
                                } elseif ($byName.ContainsKey($cand) -and $byName[$cand].Count -eq 1) {
                                    $found = $byName[$cand][0]
                                }
                                if ($found) {
                                    $letter = $statusMap[$found]
                                    $fullPath = $found
                                    break
                                }
                            }
                            $tSummary = Compress-ItemText $tText
                            if ($itemLines.Count -eq 0) {
                                if ($fullPath) {
                                    $phaseItems += "$emCheck ${tId}: $tSummary $emDash $letter $fullPath"
                                } else {
                                    $phaseItems += "$emCheck ${tId}: $tSummary"
                                }
                            }
                            $taskIds += [int]$tId.Substring(1)
                        }
                    }
                }
            }
        }
    }
    $taskIds = @($taskIds | Sort-Object -Unique)

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
    # >>> SNAPSHOT-TITLE-V2 >>>
    # Compose: <prefix> <phase>: "<subject>"[ (<details>)]
    # $summary holds the automatic per-phase metrics computed above.
    $defaultSubject = 'Implementacao Aplicada'
    $details = $summary
    switch ($frozenPhase) {
        'Specify' {
            $defaultSubject = 'Especificacao Gerada'
            $details = ''
        }
        'Clarify' {
            $defaultSubject = 'Duvidas Esclarecidas'
        }
        'Plan' {
            $defaultSubject = 'Plano Gerado'
        }
        'Tasks' {
            $defaultSubject = 'Tarefas Geradas'
        }
        'Implement' {
            $defaultSubject = 'Implementacao Aplicada'
            if ($rangeText) { $details = "$summary - $rangeText" }
        }
        'Checklist' {
            $defaultSubject = 'Checklist Gerada'
        }
        'Analyze' {
            $defaultSubject = 'Analise Concluida'
        }
        'Converge' {
            $defaultSubject = 'Convergencia Avaliada'
            $details = ''
        }
        'Task2Issues' {
            $defaultSubject = 'Issues Sincronizadas'
            $details = ''
        }
        'Constitution' {
            $defaultSubject = 'Constituicao Definida'
        }
        default {
            $defaultSubject = "$($staged.Count) arquivos Snapeados"
            $details = ''
        }
    }

    $subject = $defaultSubject
    if ($titleText) {
        $subject = $titleText.Substring(0, 1).ToUpperInvariant() + $titleText.Substring(1)
    }
    $subject = $subject.Replace('"', "'")

    $titleHead = "$snapPrefix $frozenPhase`: "
    $suffix = ''
    if ($details) { $suffix = " ($details)" }
    $allowed = 72 - $titleHead.Length - 2 - $suffix.Length
    if ($allowed -lt 1) { $allowed = 1 }
    if ($subject.Length -gt $allowed) {
        $subject = $subject.Substring(0, $allowed - 1) + [char]0x2026
    }
    $title = "$titleHead`"$subject`"$suffix"
    # <<< SNAPSHOT-TITLE-V2 <<<

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
            if ($l -match '^Sinopse:?\s*$') { $inSynopsis = $true; continue }
            if ($inSynopsis) {
                if ($l -match '^(Entregas|Conclu)' -or ($l -notmatch '^\s*-\s' -and $l -match '\S')) { break }
                if ($l -match '^\s*-\s*(.+)$') {
                    $synLine = $matches[1] -replace ('^' + [regex]::Escape($emBullet) + '\s*'), ''
                    if ($synLine.Trim()) { $synopsis += $synLine.Trim() }
                }
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
            $storyCount = 0; $sessionCount = 0; $frCount = 0; $hasClarifications = $false
            foreach ($l in (Get-Content $specPath -Encoding UTF8)) {
                if ($l -match '^### User Story') { $storyCount++ }
                elseif ($l -match '^### Session ') { $sessionCount++ }
                elseif ($l -match '^- \*\*FR-\d+\*\*') { $frCount++ }
                elseif ($l -match '^## Clarifications') { $hasClarifications = $true }
            }
            $doneLines += "Specify: $storyCount histórias / $frCount Requirements"
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
        if ($artCount -gt 0) { $doneLines += "Plan: $artCount artefatos planejados" }
        if ($taskCount -gt 0 -or (Test-Path (Join-Path $featureDir 'tasks.md'))) {
            $doneLines += "Tasks: $taskCount tarefas / $phaseCount fases"
        }
        $analysisPath = Join-Path $featureDir 'analysis.md'
        if (Test-Path $analysisPath) {
            $findingCount = 0
            foreach ($l in (Get-Content $analysisPath -Encoding UTF8)) {
                if ($l -match '^\|\s*[A-Z][0-9]+\s*\|') { $findingCount++ }
            }
            if ($findingCount -gt 0) {
                $doneLines += "Analyze: $findingCount achados"
            } else {
                $doneLines += "Analyze: relatório presente"
            }
        }
        $implSrc = @($staged | Where-Object { $_ -match '^src/' }).Count
        if ($implSrc -gt 0) { $doneLines += "Implement: $($staged.Count) arquivos" }
        $checkDir = Join-Path $featureDir 'checklists'
        if (Test-Path $checkDir) {
            $checkTotal = @(Get-ChildItem $checkDir -File -ErrorAction SilentlyContinue).Count
            if ($checkTotal -gt 0) { $doneLines += "Checklist: $checkTotal listas" }
        }
    }
    $currentPrefix = "$frozenPhase" + ':'
    for ($i = 0; $i -lt $doneLines.Count; $i++) {
        if ($doneLines[$i].StartsWith($currentPrefix)) { $doneLines[$i] = "$($doneLines[$i]) (Current)" }
    }

    # --- next command, from the hook event name -----------------------------
    # Use the event, not the detected phase: after_* records the phase that
    # just finished; before_* announces the command that is about to run.
    $nextMandatory = ''
    $nextOptional = ''
    $nextSpecial = @()
    switch ($EventName) {
        'after_constitution' { $nextMandatory = '/speckit.specify' }
        'after_specify' { $nextMandatory = '/speckit.plan'; $nextOptional = '/speckit.clarify' }
        'after_clarify' { $nextMandatory = '/speckit.plan' }
        'after_plan' { $nextMandatory = '/speckit.tasks'; $nextOptional = '/speckit.checklist' }
        'after_checklist' { $nextMandatory = '/speckit.tasks' }
        'after_tasks' { $nextMandatory = '/speckit.implement'; $nextOptional = '/speckit.analyze' }
        'after_analyze' { $nextMandatory = '/speckit.implement' }
        'after_implement' { $nextMandatory = '/speckit.converge' }
        'after_converge' { $nextSpecial = @('Se convergiu: revisar/abrir PR', 'Se adicionou tarefas: /speckit.implement e /speckit.converge') }
        default {
            if ($EventName -match '^before_(.+)$') { $nextMandatory = "/speckit.$($matches[1])" }
        }
    }

    # --- assemble body -------------------------------------------------------
    $bodyParts = @()
    if ($synFinal.Count -gt 0) {
        $bodyParts += 'Sinopse:'
        foreach ($s in $synFinal) { $bodyParts += "$emBullet $s" }
        $bodyParts += ''
    }
    if ($phaseItems.Count -gt 0) {
        $sectionHeader = ''
        switch ($frozenPhase) {
            'Specify'   { $sectionHeader = 'Especificacoes Criadas:' }
            'Clarify'   { $sectionHeader = 'Clarificacoes Feitas:' }
            'Plan'      { $sectionHeader = 'Artefatos Planejados:' }
            'Tasks'     { $sectionHeader = 'Tasks Criadas:' }
            'Implement' { $sectionHeader = 'Implement:' }
            'Converge'  { $sectionHeader = 'Convergencia Aplicada:' }
            'Checklist' { $sectionHeader = 'Checklists Criadas:' }
            'Analyze'   { $sectionHeader = 'Achados:' }
        }
        if ($sectionHeader) {
            $bodyParts += $sectionHeader
            foreach ($it in $phaseItems) { $bodyParts += $it }
            $bodyParts += ''
        }
    }
    if ($doneLines.Count -gt 0) {
        $bodyParts += 'Entregas Concluidas:'
        foreach ($d in $doneLines) { $bodyParts += "$emPin $d" }
        $bodyParts += ''
    }
    if ($nextMandatory -or $nextOptional -or $nextSpecial.Count -gt 0) {
        $bodyParts += 'Próximo Comando:'
        $bodyParts += "$emPoint Fluxo: $(Get-FlowText $frozenPhase)"
        if ($nextMandatory) { $bodyParts += "$emPoint Obrigatório: $nextMandatory" }
        if ($nextOptional) { $bodyParts += "$emPoint Opcional: $nextOptional" }
        foreach ($s in $nextSpecial) { $bodyParts += "$emPoint $s" }
        $bodyParts += ''
    }
    $stat = (git diff --cached --stat 2>$null | Out-String).Trim()
    if ($stat) {
        $bodyParts += 'Local:'
        foreach ($sl in ($stat -split "`r?`n")) {
            if ($sl.Trim()) { $bodyParts += "$emFolder $($sl.Trim())" }
        }
    }
    $body = (($bodyParts -join "`n")).TrimEnd()

    # --- commit the snapshot --------------------------------------------------
    # Pass the message through a file: the title now contains double quotes,
    # and Windows PowerShell 5.1 splits native arguments with embedded quotes.
    $msgFile = Join-Path ([IO.Path]::GetTempPath()) ("speckit-snapshot-" + [guid]::NewGuid().ToString("N") + ".txt")
    [IO.File]::WriteAllText($msgFile, ($title + "`n`n" + $body), (New-Object System.Text.UTF8Encoding($false)))
    $ErrorActionPreference = 'Continue'
    try {
        $out = git commit -q -F $msgFile 2>&1 | Out-String
        if ($LASTEXITCODE -ne 0) { throw "git commit failed: $out" }
    } catch {
        Write-Warning "[specify] Error: $_"
        exit 1
    } finally {
        Remove-Item -LiteralPath $msgFile -Force -ErrorAction SilentlyContinue
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