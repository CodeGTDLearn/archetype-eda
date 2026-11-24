#!/usr/bin/env bash
# SNAPSHOT-AUTOCOMMIT-V2
# Git extension: auto-commit.sh
# Automatically commit changes after a Spec Kit command completes.
# Checks per-command config keys in git-config.yml before committing.
#
# Usage: auto-commit.sh <event_name> [generated_message]
#        auto-commit.sh <event_name> --message-file <path>
#        auto-commit.sh <event_name> --synopsis-file <path>   (commit_style: snapshot)
#        auto-commit.sh <event_name> --title-file <path>      (commit_style: snapshot)
#   e.g.: auto-commit.sh after_specify
#   e.g.: auto-commit.sh after_specify --message-file /tmp/commit-msg.txt  (commit_style: conventional)
#   e.g.: auto-commit.sh before_implement --synopsis-file /tmp/synopsis.txt (commit_style: snapshot)
#   e.g.: auto-commit.sh before_implement --title-file /tmp/title.txt (commit_style: snapshot)
#
# --message-file is the preferred way to supply an agent-generated commit
# message: it reads the message from a file instead of a shell argument,
# so message content (which may contain quotes, `$(...)`, backticks, etc.)
# is never interpolated into a shell command line.

set -e

EVENT_NAME="${1:-}"
if [ -z "$EVENT_NAME" ]; then
    echo "Usage: $0 <event_name> [generated_message | --message-file <path>]" >&2
    exit 1
fi
shift || true

# Optional second argument: an agent-generated commit message (used when
# commit_style: conventional is configured). Prefer --message-file over
# passing the message directly as a shell argument.
GENERATED_MESSAGE=""
SYNOPSIS_LINES=""
TITLE_TEXT=""
while [ $# -gt 0 ]; do
    case "$1" in
        --synopsis-file)
            _synopsis_file="${2:-}"
            if [ -z "$_synopsis_file" ]; then
                echo "[specify] Error: --synopsis-file requires a path argument" >&2
                exit 1
            fi
            if [ -f "$_synopsis_file" ]; then
                # Transport-only artifact: read, then delete BEFORE `git add .`
                # so it is never staged into the snapshot it describes.
                SYNOPSIS_LINES=$(sed -E 's/^[[:space:]]*-[[:space:]]*//' "$_synopsis_file" | sed 's/[[:space:]]*$//' | grep -v '^$' || true)
                rm -f "$_synopsis_file"
            else
                echo "[specify] Warning: synopsis file '$_synopsis_file' not found" >&2
            fi
            shift 2
            ;;
        --title-file)
            _title_file="${2:-}"
            if [ -z "$_title_file" ]; then
                echo "[specify] Error: --title-file requires a path argument" >&2
                exit 1
            fi
            if [ -f "$_title_file" ]; then
                # Transport-only artifact: read the first non-empty line, then
                # delete BEFORE `git add .` so it is never staged.
                TITLE_TEXT=$(grep -m1 '[^[:space:]]' "$_title_file" | sed -E 's/^[[:space:]]*-[[:space:]]*//' | sed 's/[[:space:]]*$//' || true)
            else
                echo "[specify] Warning: title file '$_title_file' not found" >&2
            fi
            rm -f "$_title_file"
            shift 2
            ;;
        --message-file)
            _message_file="${2:-}"
            if [ -z "$_message_file" ]; then
                echo "[specify] Error: --message-file requires a path argument" >&2
                exit 1
            fi
            if [ ! -f "$_message_file" ]; then
                echo "[specify] Error: message file '$_message_file' not found" >&2
                exit 1
            fi
            GENERATED_MESSAGE="$(cat "$_message_file")"
            # The message file is a transport-only artifact: its content is
            # now captured above, so remove it immediately. Otherwise, if it
            # was written inside the worktree, it would be picked up as an
            # untracked change by both the "any changes?" check below and by
            # `git add .`, polluting the commit or defeating the no-changes
            # short-circuit even when nothing else changed.
            rm -f "$_message_file"
            shift 2
            ;;
        *)
            GENERATED_MESSAGE="$1"
            shift
            ;;
    esac
done

SCRIPT_DIR="$(CDPATH="" cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

_find_project_root() {
    local dir="$1"
    while [ "$dir" != "/" ]; do
        if [ -d "$dir/.specify" ] || [ -d "$dir/.git" ]; then
            echo "$dir"
            return 0
        fi
        dir="$(dirname "$dir")"
    done
    return 1
}

REPO_ROOT=$(_find_project_root "$SCRIPT_DIR") || REPO_ROOT="$(pwd)"
cd "$REPO_ROOT"

# Check if git is available
if ! command -v git >/dev/null 2>&1; then
    echo "[specify] Warning: Git not found; skipped auto-commit" >&2
    exit 0
fi

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    echo "[specify] Warning: Not a Git repository; skipped auto-commit" >&2
    exit 0
fi

# Read per-command config from git-config.yml
_config_file="$REPO_ROOT/.specify/extensions/git/git-config.yml"
_enabled=false
_commit_msg=""
_commit_style="fixed"
# Snapshot title prefix: UTF-8 bytes of the camera emoji, so this script
# stays ASCII-only and no editor encoding can corrupt it. Overridden by
# snapshot_prefix in git-config.yml.
_snap_prefix=$(printf '\360\237\223\270')

if [ -f "$_config_file" ]; then
    # Top-level scalar key: snapshot_prefix (own grep -m1; a top-level key)
    _pfx_val=$(grep -m1 '^snapshot_prefix:' "$_config_file" 2>/dev/null | sed 's/^snapshot_prefix:[[:space:]]*//' | sed 's/[[:space:]]*$//' | sed 's/^["'\'']//' | sed 's/["'\'']*$//')
    if [ -n "$_pfx_val" ]; then
        _snap_prefix="$_pfx_val"
    fi

    # Top-level scalar key: commit_style (fixed | conventional | snapshot)
    _style_val=$(grep -m1 '^commit_style:' "$_config_file" 2>/dev/null | sed 's/^commit_style:[[:space:]]*//' | sed 's/[[:space:]]\{1,\}#.*$//' | sed 's/[[:space:]]*$//' | sed 's/^["'\'']//' | sed 's/["'\'']*$//' | tr '[:upper:]' '[:lower:]')
    if [ -n "$_style_val" ]; then
        case "$_style_val" in
            fixed|conventional|snapshot)
                _commit_style="$_style_val"
                ;;
            *)
                echo "[specify] Warning: unknown commit_style '$_style_val' in git-config.yml (expected 'fixed', 'conventional' or 'snapshot'); defaulting to 'fixed'" >&2
                ;;
        esac
    fi

    # Parse the auto_commit section for this event.
    # Look for auto_commit.<event_name>.enabled and .message
    # Also check auto_commit.default as fallback.
    _in_auto_commit=false
    _in_event=false
    _default_enabled=false

    while IFS= read -r _line; do
        # Detect auto_commit: section
        if echo "$_line" | grep -q '^auto_commit:'; then
            _in_auto_commit=true
            _in_event=false
            continue
        fi

        # Exit auto_commit section on next top-level key
        if $_in_auto_commit && echo "$_line" | grep -Eq '^[a-z]'; then
            break
        fi

        if $_in_auto_commit; then
            # Check default key
            if echo "$_line" | grep -Eq "^[[:space:]]+default:[[:space:]]"; then
                _val=$(echo "$_line" | sed 's/^[^:]*:[[:space:]]*//' | tr -d '[:space:]' | tr '[:upper:]' '[:lower:]')
                [ "$_val" = "true" ] && _default_enabled=true
            fi

            # Detect our event subsection
            if echo "$_line" | grep -Eq "^[[:space:]]+${EVENT_NAME}:"; then
                _in_event=true
                continue
            fi

            # Inside our event subsection
            if $_in_event; then
                # Exit on next sibling key (same indent level as event name)
                if echo "$_line" | grep -Eq '^[[:space:]]{2}[a-z]' && ! echo "$_line" | grep -Eq '^[[:space:]]{4}'; then
                    _in_event=false
                    continue
                fi
                if echo "$_line" | grep -Eq '[[:space:]]+enabled:'; then
                    _val=$(echo "$_line" | sed 's/^[^:]*:[[:space:]]*//' | tr -d '[:space:]' | tr '[:upper:]' '[:lower:]')
                    [ "$_val" = "true" ] && _enabled=true
                    [ "$_val" = "false" ] && _enabled=false
                fi
                if echo "$_line" | grep -Eq '[[:space:]]+message:'; then
                    # Trim trailing whitespace before stripping the closing quote:
                    # a value like `message: "Done"  ` (trailing spaces after the
                    # quote) would otherwise leave the quote dangling (`Done"  `),
                    # since the closing-quote strip is anchored to end-of-string.
                    # The PowerShell twin .Trim()s first; match it for parity.
                    _commit_msg=$(echo "$_line" | sed 's/^[^:]*:[[:space:]]*//' | sed 's/[[:space:]]*$//' | sed 's/^["'\'']//' | sed 's/["'\'']*$//')
                fi
            fi
        fi
    done < "$_config_file"

    # If event-specific key not found, use default
    if [ "$_enabled" = "false" ] && [ "$_default_enabled" = "true" ]; then
        # Only use default if the event wasn't explicitly set to false
        # Check if event section existed at all
        if ! grep -q "^[[:space:]]*${EVENT_NAME}:" "$_config_file" 2>/dev/null; then
            _enabled=true
        fi
    fi
else
    # No config file — auto-commit disabled by default
    exit 0
fi

if [ "$_enabled" != "true" ]; then
    exit 0
fi

# Check if there are changes to commit
if git diff --quiet HEAD 2>/dev/null && git diff --cached --quiet 2>/dev/null && [ -z "$(git ls-files --others --exclude-standard 2>/dev/null)" ]; then
    echo "[specify] No changes to commit after $EVENT_NAME" >&2
    exit 0
fi

# ---------------------------------------------------------------------------
# Snapshot mode (commit_style: snapshot)
#
# Mirrors auto-commit.ps1: a rollback snapshot taken BEFORE the next phase
# runs. Stage first, classify the frozen phase from the staged diff, then
# build title + body (inherited synopsis, "Concluído", next phase, --stat).
# ---------------------------------------------------------------------------
if [ "$_commit_style" = "snapshot" ]; then

    _git_out=$(git add . 2>&1) || { echo "[specify] Error: git add failed: $_git_out" >&2; exit 1; }

    _staged=$(git diff --cached --name-only 2>/dev/null | grep -v '^$' || true)
    if [ -z "$_staged" ]; then
        echo "[specify] No staged changes after $EVENT_NAME" >&2
        exit 0
    fi
    _staged_count=$(printf '%s\n' "$_staged" | grep -c . || true)

    # feature directory (specs/<branch>, fallback: newest specs/*)
    _branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || true)
    _feature_dir=""
    if [ -n "$_branch" ] && [ -d "$REPO_ROOT/specs/$_branch" ]; then
        _feature_dir="$REPO_ROOT/specs/$_branch"
    elif [ -d "$REPO_ROOT/specs" ]; then
        _feature_dir=$(ls -dt "$REPO_ROOT"/specs/*/ 2>/dev/null | head -n 1 || true)
        _feature_dir="${_feature_dir%/}"
    fi

    # frozen phase: the hook event narrows the candidates, the staged diff
    # decides. A before_* hook freezes whatever the previous phase left; an
    # after_* hook states the phase that just finished. Empty candidates =
    # unknown event (generic fallback: the most advanced phase present).
    _candidates=""
    case "$EVENT_NAME" in
        before_specify)       _candidates="Constitution" ;;
        before_clarify)       _candidates="Constitution Specify" ;;
        before_plan)          _candidates="Constitution Specify Clarify" ;;
        before_tasks)         _candidates="Constitution Specify Clarify Plan" ;;
        before_implement)     _candidates="Constitution Specify Clarify Plan Tasks" ;;
        before_checklist)     _candidates="Constitution Specify Clarify Plan Tasks Implement" ;;
        before_analyze)       _candidates="Constitution Specify Clarify Plan Tasks Implement Checklist" ;;
        before_converge)      _candidates="Constitution Specify Clarify Plan Tasks Implement Checklist Analyze" ;;
        before_taskstoissues) _candidates="Constitution Specify Clarify Plan Tasks Implement Checklist Analyze Converge" ;;
        after_constitution)   _candidates="Constitution" ;;
        after_specify)        _candidates="Specify" ;;
        after_clarify)        _candidates="Clarify" ;;
        after_plan)           _candidates="Plan" ;;
        after_tasks)          _candidates="Tasks" ;;
        after_implement)      _candidates="Implement" ;;
        after_checklist)      _candidates="Checklist" ;;
        after_analyze)        _candidates="Analyze" ;;
        after_converge)       _candidates="Converge" ;;
        after_taskstoissues)  _candidates="Task2Issues" ;;
    esac

    _det_constitution=false
    _det_specify=false
    _det_clarify=false
    _det_plan=false
    _det_tasks=false
    _det_implement=false
    _det_checklist=false
    _det_analyze=false
    _det_converge=false

    if printf '%s\n' "$_staged" | grep -qE '^\.specify/memory/constitution\.md$'; then
        _det_constitution=true
    fi
    _spec_rel=$(printf '%s\n' "$_staged" | grep -E '(^|/)spec\.md$' | head -n 1 || true)
    _spec_text=""
    if [ -n "$_spec_rel" ] && [ -f "$REPO_ROOT/$_spec_rel" ]; then
        _spec_text=$(cat "$REPO_ROOT/$_spec_rel" 2>/dev/null || true)
    fi
    if [ -n "$_spec_rel" ]; then
        # /clarify appends a "## Clarifications" section to spec.md.
        if printf '%s\n' "$_spec_text" | grep -qE '^## Clarifications'; then
            _det_clarify=true
        else
            _det_specify=true
        fi
    fi
    if printf '%s\n' "$_staged" | grep -qE '(^|/)(plan|research|data-model|quickstart)\.md$|(^|/)contracts/'; then
        _det_plan=true
    fi
    if printf '%s\n' "$_staged" | grep -qE '(^|/)tasks\.md$'; then
        _det_tasks=true
        # /converge appends a "## Phase N: Convergence" section to tasks.md.
        _t_rel=$(printf '%s\n' "$_staged" | grep -E '(^|/)tasks\.md$' | head -n 1 || true)
        if [ -n "$_t_rel" ] && [ -f "$REPO_ROOT/$_t_rel" ] && grep -qE '^## Phase [0-9]+: Convergence' "$REPO_ROOT/$_t_rel"; then
            _det_converge=true
        fi
    fi
    if printf '%s\n' "$_staged" | grep -q '^src/'; then
        _det_implement=true
    fi
    if printf '%s\n' "$_staged" | grep -qE '(^|/)checklists/'; then
        _det_checklist=true
    fi
    if printf '%s\n' "$_staged" | grep -qE '(^|/)analysis\.md$'; then
        _det_analyze=true
    fi

    # An after_* hook states which phase just finished, so its label is
    # authoritative (Task2Issues, for example, leaves no file signature).
    # A before_* hook narrows the candidates and the staged diff decides;
    # without a known event, the most advanced detected phase wins.
    _frozen="Snapshot"
    _trust_event=false
    case "$EVENT_NAME" in
        after_*)
            if [ -n "$_candidates" ]; then _trust_event=true; fi
            ;;
    esac
    if [ "$_trust_event" = "true" ]; then
        _frozen="$_candidates"
    else
        for _phase in Converge Analyze Checklist Implement Tasks Plan Clarify Specify Constitution; do
            case "$_phase" in
                Converge)     _present="$_det_converge" ;;
                Analyze)      _present="$_det_analyze" ;;
                Checklist)    _present="$_det_checklist" ;;
                Implement)    _present="$_det_implement" ;;
                Tasks)        _present="$_det_tasks" ;;
                Plan)         _present="$_det_plan" ;;
                Clarify)      _present="$_det_clarify" ;;
                Specify)      _present="$_det_specify" ;;
                Constitution) _present="$_det_constitution" ;;
            esac
            [ "$_present" = "true" ] || continue
            if [ -z "$_candidates" ] || printf '%s' " $_candidates " | grep -q " $_phase "; then
                _frozen="$_phase"
                break
            fi
        done
    fi

    # tasks.md location and counts
    _tasks_rel=$(printf '%s\n' "$_staged" | grep -E '(^|/)tasks\.md$' | head -n 1 || true)
    _tasks_path=""
    if [ -n "$_tasks_rel" ]; then
        _tasks_path="$REPO_ROOT/$_tasks_rel"
    fi
    _task_count=0
    _phase_count=0
    if [ -n "$_tasks_path" ] && [ -f "$_tasks_path" ]; then
        _task_count=$(grep -cE '^- \[[ Xx]\] T[0-9]+' "$_tasks_path" || true)
        _phase_count=$(grep -cE '^## Phase [0-9]+' "$_tasks_path" || true)
    fi

    # per-phase title summary
    case "$_frozen" in
        Specify) _summary="spec.md" ;;
        Clarify)
            _sessions=$(printf '%s\n' "$_spec_text" | grep -c '^### Session ' || true)
            _summary="$_sessions sessões" ;;
        Plan)
            _plan_count=$(printf '%s\n' "$_staged" | grep -cE '(^|/)(plan|research|data-model|quickstart)\.md$|(^|/)contracts/' || true)
            _summary="$_plan_count artefatos de design" ;;
        Tasks)
            _summary="$_task_count tarefas em $_phase_count fases" ;;
        Implement)
            _summary="$_staged_count arquivos" ;;
        Checklist)
            _check_count=$(printf '%s\n' "$_staged" | grep -cE '(^|/)checklists/' || true)
            if [ "$_check_count" -eq 0 ] && [ -n "$_feature_dir" ] && [ -d "$_feature_dir/checklists" ]; then
                _check_count=$(find "$_feature_dir/checklists" -maxdepth 1 -type f 2>/dev/null | wc -l | tr -d '[:space:]')
            fi
            _summary="$_check_count listas" ;;
        Analyze)
            _summary="analysis.md" ;;
        Converge)
            _summary="tasks.md" ;;
        Task2Issues)
            _summary="issues" ;;
        Constitution)
            _summary="constitution.md" ;;
        *)
            _summary="$_staged_count arquivos" ;;
    esac

    # Implement bullets: tasks flipped [X] in the staged tasks.md diff.
    # A task that names a staged file (full path, or a unique basename) yields
    # "<status> <basename>"; otherwise the text before the first colon.
    _task_ids=""
    _task_bullets=""
    if [ "$_frozen" = "Implement" ] && [ -n "$_tasks_path" ] && [ -f "$_tasks_path" ]; then
        _status_lines=$(git diff --cached --name-status 2>/dev/null)
        while IFS= read -r _diff_line; do
            [[ "$_diff_line" =~ ^\+-\ \[X\]\ (T[0-9]+)[[:space:]]+(.*)$ ]] || continue
            _task_id="${BASH_REMATCH[1]}"
            _task_text="${BASH_REMATCH[2]}"
            _task_text=$(printf '%s' "$_task_text" | sed -E 's/^(\[(P|US[0-9]+)\][[:space:]]*)+//' | sed -E 's/[[:space:]]+/ /g' | sed -E 's/^ //; s/ $//')

            _letter=""
            _fname=""
            _cands=$(printf '%s\n' "$_task_text" | grep -oE '`[^`]+`' | tr -d '`' || true)
            while IFS= read -r _cand; do
                [ -n "$_cand" ] || continue
                _found=$(printf '%s\n' "$_status_lines" | awk -F '\t' -v p="$_cand" '$2 == p { print $2; exit }')
                if [ -z "$_found" ]; then
                    _base="${_cand##*/}"
                    _matches=$(printf '%s\n' "$_status_lines" | awk -F '\t' -v b="$_base" '{ n = split($2, a, "/"); if (a[n] == b) print $2 }')
                    _mcount=$(printf '%s\n' "$_matches" | grep -c . || true)
                    if [ "$_mcount" -eq 1 ]; then
                        _found="$_matches"
                    fi
                fi
                if [ -n "$_found" ]; then
                    _letter=$(printf '%s\n' "$_status_lines" | awk -F '\t' -v p="$_found" '$2 == p { print $1; exit }')
                    _fname="${_found##*/}"
                    break
                fi
            done <<EOF
$_cands
EOF

            if [ -n "$_fname" ]; then
                _bullet="- $_task_id: $_letter $_fname"
            else
                _head="$_task_text"
                case "$_head" in
                    *:*) _head="${_head%%:*}" ;;
                esac
                _head=$(printf '%s' "$_head" | tr -d '`' | sed -E 's/[[:space:]]+/ /g' | sed -E 's/^ //; s/ $//')
                _bullet="- $_task_id: $_head"
            fi
            _task_bullets="${_task_bullets}${_bullet}
"
            _task_ids="${_task_ids}${_task_id}
"
        done < <(git diff --cached -U0 -- "$_tasks_path" 2>/dev/null || true)

        _task_bullets=$(printf '%s' "$_task_bullets" | grep -v '^$' || true)
        _bcount=$(printf '%s\n' "$_task_bullets" | grep -c . || true)
        if [ "$_bcount" -gt 10 ]; then
            _extra=$((_bcount - 10))
            _task_bullets=$(printf '%s\n' "$_task_bullets" | head -n 10)
            _task_bullets="${_task_bullets}
- (+$_extra tarefas)"
        fi
    fi

    # contiguous range for the title
    _range=""
    _ids_sorted=$(printf '%s\n' "$_task_ids" | grep -v '^$' | sed 's/^T//' | sed 's/^0*//' | sort -n | uniq || true)
    _id_count=$(printf '%s\n' "$_ids_sorted" | grep -c . || true)
    if [ "$_id_count" -gt 0 ]; then
        _endash=$(printf '\342\200\223')
        _range=$(printf '%s\n' "$_ids_sorted" | awk -v dash="$_endash" '
            { v[NR] = $1 }
            END {
                n = NR
                if (n == 0) exit
                if (n == 1) { printf "T%03d", v[1]; exit }
                if (v[n] - v[1] + 1 == n) { printf "T%03d%sT%03d", v[1], dash, v[n]; exit }
                if (n <= 3) {
                    for (i = 1; i <= n; i++) { if (i > 1) printf ","; printf "T%03d", v[i] }
                    exit
                }
                printf "T%03d%sT%03d +%d", v[1], dash, v[n], n - 2
            }')
    fi
    # >>> SNAPSHOT-TITLE-V2 >>>
    # Compose: <prefix> <phase>: "<subject>"[ (<details>)]
    # _summary holds the automatic per-phase metrics computed above.
    _default_subject="Implementacao Aplicada"
    _details="$_summary"
    case "$_frozen" in
        Specify)
            _default_subject="Especificacao Gerada"
            _details="" ;;
        Clarify)
            _default_subject="Duvidas Esclarecidas" ;;
        Plan)
            _default_subject="Plano Gerado" ;;
        Tasks)
            _default_subject="Tarefas Geradas" ;;
        Implement)
            _default_subject="Implementacao Aplicada"
            if [ -n "$_range" ]; then _details="$_summary - $_range"; fi ;;
        Checklist)
            _default_subject="Checklist Gerada" ;;
        Analyze)
            _default_subject="Analise Concluida" ;;
        Converge)
            _default_subject="Convergencia Avaliada"
            _details="" ;;
        Task2Issues)
            _default_subject="Issues Sincronizadas"
            _details="" ;;
        Constitution)
            _default_subject="Constituicao Definida" ;;
        *)
            _default_subject="$_staged_count arquivos Snapeados"
            _details="" ;;
    esac

    _subject="$_default_subject"
    if [ -n "$TITLE_TEXT" ]; then
        _first_char="${TITLE_TEXT:0:1}"
        case "$_first_char" in
            [a-z])
                _upper_char=$(printf '%s' "$_first_char" | tr '[:lower:]' '[:upper:]')
                _subject="${_upper_char}${TITLE_TEXT:1}" ;;
            *)
                _subject="$TITLE_TEXT" ;;
        esac
    fi
    _subject=$(printf '%s' "$_subject" | tr '"' "'")

    _title_head="$_snap_prefix $_frozen: "
    _suffix=""
    if [ -n "$_details" ]; then _suffix=" ($_details)"; fi
    _head_len=$(printf '%s' "$_title_head" | wc -m | tr -d '[:space:]')
    _suffix_len=$(printf '%s' "$_suffix" | wc -m | tr -d '[:space:]')
    _allowed=$((72 - _head_len - 2 - _suffix_len))
    if [ "$_allowed" -lt 1 ]; then _allowed=1; fi
    _subject_len=$(printf '%s' "$_subject" | wc -m | tr -d '[:space:]')
    if [ "$_subject_len" -gt "$_allowed" ]; then
        _ellipsis=$(printf '\342\200\246')
        _cut=$((_allowed - 1))
        _subject="${_subject:0:$_cut}${_ellipsis}"
    fi
    _title="${_title_head}\"${_subject}\"${_suffix}"
    # <<< SNAPSHOT-TITLE-V2 <<<

    # synopsis (inherited from the previous snapshot, like /git-commit)
    _syn_final=""
    if [ -n "$SYNOPSIS_LINES" ]; then
        _syn_final="$SYNOPSIS_LINES"
    else
        _prev_body=$(git log -1 --format=%B 2>/dev/null || true)
        _syn_final=$(printf '%s\n' "$_prev_body" | awk '
            /^Sinopse[[:space:]]*$/ { in_s = 1; next }
            in_s {
                if ($0 ~ /^Conclu/ || ($0 !~ /^[[:space:]]*-[[:space:]]/ && $0 ~ /[^[:space:]]/)) exit
                if ($0 ~ /^[[:space:]]*-[[:space:]]/) {
                    sub(/^[[:space:]]*-[[:space:]]*/, "", $0)
                    print
                }
            }')
    fi
    # dedupe + cap at 6
    _syn_final=$(printf '%s\n' "$_syn_final" | awk '
        { key = tolower($0); if (key != "" && !seen[key] && count < 6) { seen[key] = 1; print; count++ } }')

    # "Concluído" ledger for the frozen phase
    _done_lines=()
    if [ -n "$_feature_dir" ]; then
        if [ -f "$_feature_dir/spec.md" ]; then
            _stories=$(grep -c '^### User Story' "$_feature_dir/spec.md" || true)
            _sessions=$(grep -c '^### Session ' "$_feature_dir/spec.md" || true)
            _done_lines+=("Specify: $_stories histórias")
            if grep -q '^## Clarifications' "$_feature_dir/spec.md"; then
                _done_lines+=("Clarify: $_sessions sessões")
            fi
        fi
        _art=0
        for _n in plan.md research.md data-model.md quickstart.md; do
            if [ -f "$_feature_dir/$_n" ]; then _art=$((_art + 1)); fi
        done
        if [ -d "$_feature_dir/contracts" ]; then
            _c=$(find "$_feature_dir/contracts" -maxdepth 1 -type f 2>/dev/null | wc -l | tr -d '[:space:]')
            _art=$((_art + _c))
        fi
        if [ "$_art" -gt 0 ]; then
            _done_lines+=("Plan: $_art artefatos de design")
        fi
        if [ "$_task_count" -gt 0 ] || [ -f "$_feature_dir/tasks.md" ]; then
            _done_lines+=("Tasks: $_task_count tarefas / $_phase_count fases")
        fi
        if [ -f "$_feature_dir/analysis.md" ]; then
            _done_lines+=("Analyze: relatório presente")
        fi
        _impl=$(printf '%s\n' "$_staged" | grep -c '^src/' || true)
        if [ "$_impl" -gt 0 ]; then
            _done_lines+=("Implement: $_impl arquivos")
        fi
        if [ -d "$_feature_dir/checklists" ]; then
            _check_total=$(find "$_feature_dir/checklists" -maxdepth 1 -type f 2>/dev/null | wc -l | tr -d '[:space:]')
            if [ "$_check_total" -gt 0 ]; then
                _done_lines+=("Checklist: $_check_total listas")
            fi
        fi
    fi

    # next phase, from the hook event name. Only before_* hooks announce the
    # phase that is about to run; after_* hooks record the phase just completed,
    # so the body omits "A seguir".
    _next=""
    case "$EVENT_NAME" in
        before_*) _next="${EVENT_NAME#before_}" ;;
    esac
    if [ -n "$_next" ]; then
        _next="$(printf '%s' "$_next" | cut -c1 | tr '[:lower:]' '[:upper:]')$(printf '%s' "$_next" | cut -c2-)"
    fi

    # assemble body
    _body=""
    if [ -n "$_syn_final" ]; then
        _body="Sinopse
$(printf '%s\n' "$_syn_final" | sed 's/^/- /')
"
    fi
    if [ -n "$_task_bullets" ]; then
        _body="${_body}${_task_bullets}
"
    fi
    if [ "${#_done_lines[@]}" -gt 0 ]; then
        _body="${_body}Concluído
"
        for _d in "${_done_lines[@]}"; do
            _body="${_body}- $_d
"
        done
    fi
    if [ -n "$_next" ]; then
        _body="${_body}A seguir: $_next
"
    fi
    _stat=$(git diff --cached --stat 2>/dev/null || true)
    if [ -n "$_stat" ]; then
        _body="${_body}${_stat}"
    fi

    _git_out=$(git commit -q -m "$_title" -m "$_body" 2>&1) || { echo "[specify] Error: git commit failed: $_git_out" >&2; exit 1; }

    echo "[OK] Snapshot committed (${_frozen}): $_title" >&2
    exit 0
fi

# In conventional mode, the commit message must be supplied by the agent
# (via the generated_message argument); never fall back to the fixed message.
if [ "$_commit_style" = "conventional" ]; then
    if [ -n "$GENERATED_MESSAGE" ]; then
        _commit_msg="$GENERATED_MESSAGE"
    else
        echo "[specify] Error: commit_style is 'conventional' but no generated commit message was supplied; aborting auto-commit (pass --message-file <path>, or a raw message as arg 2, or set commit_style: fixed)" >&2
        exit 1
    fi
fi

# Derive a human-readable command name from the event
# e.g., after_specify -> specify, before_plan -> plan
_command_name=$(echo "$EVENT_NAME" | sed 's/^after_//' | sed 's/^before_//')
_phase=$(echo "$EVENT_NAME" | grep -q '^before_' && echo 'before' || echo 'after')

# Use custom message if configured, otherwise default
if [ -z "$_commit_msg" ]; then
    _commit_msg="[Spec Kit] Auto-commit ${_phase} ${_command_name}"
fi

# Stage and commit
_git_out=$(git add . 2>&1) || { echo "[specify] Error: git add failed: $_git_out" >&2; exit 1; }
_git_out=$(git commit -q -m "$_commit_msg" 2>&1) || { echo "[specify] Error: git commit failed: $_git_out" >&2; exit 1; }

echo "[OK] Changes committed ${_phase} ${_command_name}" >&2
