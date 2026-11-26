#!/usr/bin/env python3
# SNAPSHOT-AUTOCOMMIT-V2
"""Git extension: auto_commit.py

Automatically commit changes after a Spec Kit command completes.
Python port of ``auto-commit.sh`` / ``auto-commit.ps1``.
Checks per-command config keys in git-config.yml before committing.

Usage: auto_commit.py <event_name> [--synopsis-file <path>] [--title-file <path>]
       [--items-file <path>]
  e.g.: auto_commit.py after_specify
  e.g.: auto_commit.py before_implement --synopsis-file /tmp/synopsis.txt
        (commit_style: snapshot)
  e.g.: auto_commit.py before_implement --title-file /tmp/title.txt
        (commit_style: snapshot)
  e.g.: auto_commit.py after_implement --items-file /tmp/items.txt
        (commit_style: snapshot)
"""

from __future__ import annotations

import re
import shutil
import subprocess
import sys
from pathlib import Path


def _find_project_root(start: Path) -> Path | None:
    current = start
    while True:
        if (current / ".specify").is_dir() or (current / ".git").exists():
            return current
        if current.parent == current:
            return None
        current = current.parent


def _value_after_colon(line: str) -> str:
    return re.sub(r"^[^:]*:\s*", "", line)


def _strip_quotes(value: str) -> str:
    """Strip surrounding whitespace, then one leading quote and all trailing quotes.

    Trimming first matters when the YAML value has trailing whitespace after a
    closing quote (``message: "Done"  ``): stripping quotes anchored to the end
    of string would leave the closing quote dangling (``Done"  ``) because the
    quote is no longer at the end. The PowerShell twin ``.Trim()``s before
    stripping, so trim here too to keep all three script variants in parity.
    """
    value = value.strip()
    value = re.sub(r"^[\"']", "", value)
    return re.sub(r"[\"']*$", "", value)


def _parse_scalar(content: str, key: str) -> str:
    """Return the value of a top-level ``key: value`` scalar, or ""."""
    for line in content.splitlines():
        match = re.match(rf"^{re.escape(key)}:\s*(.+)$", line)
        if match:
            return _strip_quotes(re.sub(r"\s+#.*$", "", match.group(1)))
    return ""


def _git(repo_root: Path, *args: str) -> subprocess.CompletedProcess[str]:
    """Run git and decode its output as UTF-8.

    ``text=True`` alone uses the Windows locale encoding (cp1252), which would
    mangle accented commit messages. Git always writes UTF-8.
    """
    return subprocess.run(
        ["git", *args],
        cwd=repo_root,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )


def _parse_auto_commit_config(
    config_file: Path, event_name: str
) -> tuple[bool, str]:
    """Parse the auto_commit section for this event, mirroring the bash line parser.

    Returns (enabled, commit_msg). Looks for auto_commit.<event_name>.enabled
    and .message, with auto_commit.default as fallback.
    """
    enabled = False
    commit_msg = ""
    default_enabled = False
    in_auto_commit = False
    in_event = False

    try:
        content = config_file.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError):
        # Unreadable or non-UTF-8 config is treated like a missing one:
        # auto-commit stays disabled instead of crashing with a traceback.
        return False, ""
    for record in content.splitlines(keepends=True):
        if not record.endswith("\n"):
            break
        line = record[:-1].rstrip("\r")
        if line.startswith("auto_commit:"):
            in_auto_commit = True
            in_event = False
            continue

        # Exit auto_commit section on next top-level key
        if in_auto_commit and re.match(r"^[a-z]", line):
            break

        if not in_auto_commit:
            continue

        if re.match(r"^\s+default:\s", line):
            value = re.sub(r"\s", "", _value_after_colon(line)).lower()
            if value == "true":
                default_enabled = True

        if re.match(rf"^\s+{re.escape(event_name)}:", line):
            in_event = True
            continue

        if in_event:
            # Exit on next sibling key (same indent level as event name)
            if re.match(r"^\s{2}[a-z]", line) and not re.match(r"^\s{4}", line):
                in_event = False
                continue
            if re.search(r"\s+enabled:", line):
                value = re.sub(r"\s", "", _value_after_colon(line)).lower()
                if value == "true":
                    enabled = True
                elif value == "false":
                    enabled = False
            if re.search(r"\s+message:", line):
                commit_msg = _strip_quotes(_value_after_colon(line))

    # If event-specific key not found, use default — but only if the event
    # section didn't exist at all (an explicit false must win).
    if not enabled and default_enabled:
        if not re.search(rf"^\s*{re.escape(event_name)}:", content, re.MULTILINE):
            enabled = True

    return enabled, commit_msg


# Phases in Spec Kit order. A ``before_<phase>`` hook freezes whatever the
# previous phase left, so only earlier phases are candidates; an
# ``after_<phase>`` hook freezes that phase itself.
_PHASE_ORDER = [
    "Constitution",
    "Specify",
    "Clarify",
    "Plan",
    "Tasks",
    "Implement",
    "Checklist",
    "Analyze",
    "Converge",
    "Task2Issues",
]

_PHASE_OF_EVENT = {
    "constitution": "Constitution",
    "specify": "Specify",
    "clarify": "Clarify",
    "plan": "Plan",
    "tasks": "Tasks",
    "implement": "Implement",
    "checklist": "Checklist",
    "analyze": "Analyze",
    "converge": "Converge",
    "taskstoissues": "Task2Issues",
}


def _frozen_phase_candidates(event_name: str) -> list[str]:
    """Phases the hook may be freezing, or [] when the event is unknown."""
    if event_name.startswith("after_"):
        phase = _PHASE_OF_EVENT.get(event_name[len("after_"):])
        return [phase] if phase else []
    if event_name.startswith("before_"):
        phase = _PHASE_OF_EVENT.get(event_name[len("before_"):])
        if not phase or phase not in _PHASE_ORDER:
            return []
        return _PHASE_ORDER[: _PHASE_ORDER.index(phase)]
    return []


_EM_BULLET = "\U0001F4CB"
_EM_CHECK = "\u2705"
_EM_PIN = "\U0001F4CC"
_EM_FOLDER = "\U0001F4C2"
_EM_DASH = "\u2014"
_EM_POINT = "\U0001F449"

_FLOW = [
    "Constitution",
    "Specify",
    "Clarify",
    "Plan",
    "Checklist",
    "Tasks",
    "Analyze",
    "Implement",
    "Converge",
]
_FLOW_STOP = {
    "Constitution": 1,
    "Specify": 3,
    "Clarify": 3,
    "Plan": 5,
    "Checklist": 5,
    "Tasks": 7,
    "Analyze": 7,
}


def _compress_item(text: str) -> str:
    """Shorten a task/requirement text to a 2-3 word summary."""
    had_path = re.search(r"`[^`]+`", text) is not None
    value = re.sub(r"`[^`]+`", " ", text)
    value = value.replace("`", "")
    value = re.sub(r"^(System|The system|O sistema)\s+(MUST|DEVE|deve)\s+", "", value)
    for separator in (" \u2014 ", " \u2013 ", ": ", ". ", " ("):
        index = value.find(separator)
        if index > 0:
            value = value[:index]
    value = re.sub(r"\s+", " ", value).strip()
    value = value.rstrip(",;.:").rstrip()
    if had_path:
        value = re.sub(
            r"\s+(em|no|na|de|do|da|para|com|in|at|to|from|using)$", "", value
        )
    if value:
        value = value[:1].upper() + value[1:]
    if len(value) > 48:
        value = value[:48]
        space = value.rfind(" ")
        if space > 20:
            value = value[:space]
        value = value.rstrip(",;.:") + "\u2026"
    if not value:
        return "\u2B1C"
    return value


def _flow_text(phase: str) -> str:
    """Canonical flow up to the next mandatory command."""
    stop = _FLOW_STOP.get(phase, 8)
    return " \u27A1  ".join(_FLOW[: stop + 1])


def _snapshot(
    repo_root: Path,
    event_name: str,
    synopsis_lines: list[str],
    title_text: str,
    snap_prefix: str,
    item_lines: list[str],
) -> int:
    """Commit a rollback snapshot of the state left by the previous phase.

    Mirrors auto-commit.ps1: stage first, classify the frozen phase from the
    staged diff, then build title + body (inherited synopsis, "Concluído",
    next command, --stat).
    """
    added = _git(repo_root, "add", ".")
    if added.returncode != 0:
        output = (added.stdout + added.stderr).strip()
        print(f"[specify] Error: git add failed: {output}", file=sys.stderr)
        return 1

    staged = [
        path
        for path in _git(repo_root, "diff", "--cached", "--name-only").stdout.splitlines()
        if path.strip()
    ]
    if not staged:
        print(f"[specify] No staged changes after {event_name}", file=sys.stderr)
        return 0

    branch = _git(repo_root, "rev-parse", "--abbrev-ref", "HEAD").stdout.strip()
    feature_dir: Path | None = None
    candidate = repo_root / "specs" / branch if branch else None
    if candidate is not None and candidate.is_dir():
        feature_dir = candidate
    else:
        specs_root = repo_root / "specs"
        if specs_root.is_dir():
            dirs = sorted(
                (entry for entry in specs_root.iterdir() if entry.is_dir()),
                key=lambda entry: entry.stat().st_mtime,
                reverse=True,
            )
            if dirs:
                feature_dir = dirs[0]

    def matches(pattern: str) -> bool:
        return any(re.search(pattern, path) for path in staged)

    # spec.md feeds both the Specify/Clarify distinction and the Clarify details.
    spec_rel = next(
        (path for path in staged if re.search(r"(^|/)spec\.md$", path)), ""
    )
    spec_text = ""
    spec_file = repo_root / spec_rel if spec_rel else None
    if spec_file is not None and spec_file.is_file():
        try:
            spec_text = spec_file.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError):
            spec_text = ""

    def detect_phases() -> list[str]:
        """Phases with signature artifacts staged, most advanced first."""
        detected: list[str] = []
        if matches(r"^\.specify/memory/constitution\.md$"):
            detected.append("Constitution")
        if spec_rel:
            # /clarify appends a "## Clarifications" section to spec.md.
            if re.search(r"^## Clarifications", spec_text, re.MULTILINE):
                detected.append("Clarify")
            else:
                detected.append("Specify")
        if matches(r"(^|/)(plan|research|data-model|quickstart)\.md$") or matches(
            r"(^|/)contracts/"
        ):
            detected.append("Plan")
        if matches(r"(^|/)tasks\.md$"):
            detected.append("Tasks")
        if matches(r"^src/"):
            detected.append("Implement")
        if matches(r"(^|/)checklists/"):
            detected.append("Checklist")
        if matches(r"(^|/)analysis\.md$"):
            detected.append("Analyze")
        # /converge appends a "## Phase N: Convergence" section to tasks.md, so
        # a Convergence tasks.md outranks the plain Tasks phase.
        if matches(r"(^|/)tasks\.md$"):
            tasks_rel = next(
                (path for path in staged if re.search(r"(^|/)tasks\.md$", path)), ""
            )
            tasks_file = repo_root / tasks_rel if tasks_rel else None
            if tasks_file is not None and tasks_file.is_file():
                try:
                    tasks_text = tasks_file.read_text(encoding="utf-8")
                except (OSError, UnicodeDecodeError):
                    tasks_text = ""
                if re.search(
                    r"^## Phase \d+: Convergence\b", tasks_text, re.MULTILINE
                ):
                    detected.append("Converge")
        return detected[::-1]

    # An after_* hook states which phase just finished, so its label is
    # authoritative (Task2Issues, for example, leaves no file signature).
    # A before_* hook narrows the candidates and the staged diff decides;
    # without a known event, the most advanced detected phase wins. This keeps
    # a leftover checklists/ file from mislabelling a Specify snapshot.
    candidates = _frozen_phase_candidates(event_name)
    detected_phases = detect_phases()
    if event_name.startswith("after_") and candidates:
        frozen = candidates[0]
    elif candidates:
        frozen = next(
            (phase for phase in detected_phases if phase in candidates), "Snapshot"
        )
    else:
        frozen = detected_phases[0] if detected_phases else "Snapshot"

    tasks_rel = next(
        (path for path in staged if re.search(r"(^|/)tasks\.md$", path)), ""
    )
    tasks_path = repo_root / tasks_rel if tasks_rel else None
    # The ledger counts every task of the feature, not only the staged ones.
    tasks_count_path = tasks_path
    if tasks_count_path is None and feature_dir is not None:
        candidate_tasks = feature_dir / "tasks.md"
        if candidate_tasks.is_file():
            tasks_count_path = candidate_tasks
    task_count = 0
    phase_count = 0
    if tasks_count_path is not None and tasks_count_path.is_file():
        for line in tasks_count_path.read_text(encoding="utf-8").splitlines():
            if re.match(r"^- \[[ Xx]\] T\d+", line):
                task_count += 1
            elif re.match(r"^## Phase \d+", line):
                phase_count += 1

    if frozen == "Specify":
        summary = "spec.md"
    elif frozen == "Clarify":
        sessions = sum(
            1 for line in spec_text.splitlines() if line.startswith("### Session ")
        )
        summary = f"{sessions} sessões"
    elif frozen == "Plan":
        plan_count = sum(
            1
            for path in staged
            if re.search(r"(^|/)(plan|research|data-model|quickstart)\.md$", path)
            or re.search(r"(^|/)contracts/", path)
        )
        summary = f"{plan_count} artefatos planejados"
    elif frozen == "Tasks":
        summary = f"{task_count} tarefas em {phase_count} fases"
    elif frozen == "Checklist":
        check_count = sum(1 for path in staged if re.search(r"(^|/)checklists/", path))
        if check_count == 0 and feature_dir is not None:
            check_dir = feature_dir / "checklists"
            if check_dir.is_dir():
                check_count = sum(1 for entry in check_dir.iterdir() if entry.is_file())
        summary = f"{check_count} listas"
    elif frozen == "Analyze":
        summary = "analysis.md"
    elif frozen == "Converge":
        summary = "tasks.md"
    elif frozen == "Task2Issues":
        summary = "issues"
    elif frozen == "Constitution":
        summary = "constitution.md"
    else:
        summary = f"{len(staged)} arquivos"

    # Phase items: agent-supplied, or derived from the staged artifacts.
    task_ids: list[int] = []
    phase_items: list[str] = list(item_lines)
    if frozen == "Implement" and tasks_path is not None and tasks_path.is_file():
        status_map: dict[str, str] = {}
        by_name: dict[str, list[str]] = {}
        for line in _git(repo_root, "diff", "--cached", "--name-status").stdout.splitlines():
            parts = line.split("\t")
            if len(parts) >= 2 and parts[0] in ("A", "M", "D"):
                changed_path = parts[1]
                status_map[changed_path] = parts[0]
                by_name.setdefault(Path(changed_path).name, []).append(changed_path)

        diff = _git(repo_root, "diff", "--cached", "-U0", "--", tasks_rel).stdout
        for line in diff.splitlines():
            match = re.match(r"^\+- \[X\] (T\d+)\s+(.+)$", line)
            if not match:
                continue
            task_id = match.group(1)
            text = re.sub(r"^(?:\[(?:P|US\d+)\]\s*)+", "", match.group(2))
            text = re.sub(r"\s+", " ", text).strip()

            letter = ""
            full_path = ""
            for candidate_path in re.findall(r"`([^`]+)`", text):
                found = ""
                if candidate_path in status_map:
                    found = candidate_path
                elif len(by_name.get(candidate_path, [])) == 1:
                    found = by_name[candidate_path][0]
                if found:
                    letter = status_map[found]
                    full_path = found
                    break

            if not item_lines:
                item_summary = _compress_item(text)
                if full_path:
                    phase_items.append(
                        f"{_EM_CHECK} {task_id}: {item_summary} {_EM_DASH} {letter} {full_path}"
                    )
                else:
                    phase_items.append(f"{_EM_CHECK} {task_id}: {item_summary}")
            task_ids.append(int(task_id[1:]))

        task_ids = sorted(set(task_ids))
    elif not phase_items and feature_dir is not None:
        if frozen == "Specify":
            spec_path = feature_dir / "spec.md"
            if spec_path.is_file():
                us_lines: list[str] = []
                fr_lines: list[str] = []
                for line in spec_path.read_text(encoding="utf-8").splitlines():
                    match = re.match(
                        r"^### User Story\s+(\d+)\s*[-\u2013\u2014]\s*"
                        r"(.+?)\s*(?:\(Priority:.*\))?$",
                        line,
                    )
                    if match:
                        us_lines.append(f"{_EM_CHECK} US{match.group(1)}: {match.group(2)}")
                        continue
                    match = re.match(r"^- \*\*(FR-\d+)\*\*:\s*(.+)$", line)
                    if match:
                        fr_lines.append(
                            f"{_EM_CHECK} {match.group(1)}: "
                            f"{_compress_item(match.group(2))}"
                        )
                if us_lines:
                    phase_items.append("* US \u2014 User Stories")
                    phase_items.extend(us_lines)
                if fr_lines:
                    if phase_items:
                        phase_items.append("")
                    phase_items.append("* FR \u2014 Functional Requirements")
                    phase_items.extend(fr_lines)
        elif frozen == "Clarify":
            spec_path = feature_dir / "spec.md"
            if spec_path.is_file():
                lines = spec_path.read_text(encoding="utf-8").splitlines()
                for index, line in enumerate(lines):
                    match = re.match(r"^### Session\s+(\d{4}-\d{2}-\d{2})", line)
                    if not match:
                        continue
                    session_summary = ""
                    for following in lines[index + 1:]:
                        candidate = following.strip()
                        if re.match(r"^#{1,3}\s", candidate):
                            break
                        if candidate:
                            session_summary = candidate
                            break
                    session_summary = re.sub(r"^[-*]\s+", "", session_summary)
                    session_summary = re.sub(r"^(\*\*)?[Qq](\*\*)?:(\*\*)?\s*", "", session_summary)
                    if session_summary:
                        phase_items.append(
                            f"{_EM_CHECK} {match.group(1)}: {_compress_item(session_summary)}"
                        )
                    else:
                        phase_items.append(f"{_EM_CHECK} {match.group(1)}")
        elif frozen == "Plan":
            feature_prefix = str(feature_dir).replace("\\", "/").rstrip("/") + "/"
            plan_paths = sorted(
                path
                for path in staged
                if re.search(r"(^|/)(plan|research|data-model|quickstart)\.md$", path)
                or re.search(r"(^|/)contracts/", path)
            )
            for path in plan_paths:
                relative = path.replace("\\", "/")
                if relative.startswith(feature_prefix):
                    relative = relative[len(feature_prefix):]
                phase_items.append(f"{_EM_CHECK} {relative}")
        elif frozen == "Tasks" and tasks_path is not None and tasks_path.is_file():
            for line in tasks_path.read_text(encoding="utf-8").splitlines():
                match = re.match(r"^- \[[ Xx]\] (T\d+)\s+(.+)$", line)
                if not match:
                    continue
                text = re.sub(r"^(?:\[(?:P|US\d+)\]\s*)+", "", match.group(2))
                text = re.sub(r"\s+", " ", text)
                phase_items.append(
                    f"{_EM_CHECK} {match.group(1)}: {_compress_item(text)}"
                )
        elif frozen == "Converge":
            if tasks_path is not None and tasks_path.is_file():
                inside_convergence = False
                for line in tasks_path.read_text(encoding="utf-8").splitlines():
                    if re.match(r"^## Phase \d+: Convergence\b", line):
                        inside_convergence = True
                        continue
                    if line.startswith("## "):
                        inside_convergence = False
                        continue
                    if not inside_convergence:
                        continue
                    match = re.match(r"^- \[[ Xx]\] (T\d+)\s+(.+)$", line)
                    if not match:
                        continue
                    text = re.sub(r"^(?:\[(?:P|US\d+)\]\s*)+", "", match.group(2))
                    text = re.sub(r"\s+", " ", text)
                    phase_items.append(
                        f"{_EM_CHECK} {match.group(1)}: {_compress_item(text)}"
                    )
        elif frozen == "Checklist":
            feature_prefix = str(feature_dir).replace("\\", "/").rstrip("/") + "/"
            for path in sorted(
                path for path in staged if re.search(r"(^|/)checklists/", path)
            ):
                relative = path.replace("\\", "/")
                if relative.startswith(feature_prefix):
                    relative = relative[len(feature_prefix):]
                phase_items.append(f"{_EM_CHECK} {relative}")
            if not phase_items:
                check_dir = feature_dir / "checklists"
                if check_dir.is_dir():
                    for entry in sorted(check_dir.iterdir()):
                        if entry.is_file():
                            phase_items.append(f"{_EM_CHECK} checklists/{entry.name}")
        elif frozen == "Analyze":
            analysis_path = feature_dir / "analysis.md"
            if analysis_path.is_file():
                for line in analysis_path.read_text(encoding="utf-8").splitlines():
                    if not re.match(r"^\|\s*[A-Z][0-9]+\s*\|", line):
                        continue
                    cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
                    finding_id = cells[0]
                    finding_summary = cells[4] if len(cells) >= 5 else ""
                    if not finding_summary and len(cells) >= 4:
                        finding_summary = cells[3]
                    if finding_summary:
                        phase_items.append(
                            f"{_EM_CHECK} {finding_id}: {_compress_item(finding_summary)}"
                        )
                    else:
                        phase_items.append(f"{_EM_CHECK} {finding_id}")
                if not phase_items:
                    phase_items.append(f"{_EM_CHECK} analysis.md")

    # Contiguous range for the title.
    range_text = ""
    if len(task_ids) == 1:
        range_text = f"T{task_ids[0]:03d}"
    elif task_ids:
        first, last = task_ids[0], task_ids[-1]
        if len(task_ids) == last - first + 1:
            range_text = f"T{first:03d}\u2013T{last:03d}"
        elif len(task_ids) <= 3:
            range_text = ",".join(f"T{task_id:03d}" for task_id in task_ids)
        else:
            range_text = f"T{first:03d}\u2013T{last:03d} +{len(task_ids) - 2}"
    # >>> SNAPSHOT-TITLE-V2 >>>
    # Compose: <prefix> <phase>: "<subject>"[ (<details>)]
    # ``summary`` holds the automatic per-phase metrics computed above.
    default_subject = "Implementacao Aplicada"
    details = summary
    if frozen == "Specify":
        default_subject = "Especificacao Gerada"
        details = ""
    elif frozen == "Clarify":
        default_subject = "Duvidas Esclarecidas"
    elif frozen == "Plan":
        default_subject = "Plano Gerado"
    elif frozen == "Tasks":
        default_subject = "Tarefas Geradas"
    elif frozen == "Implement":
        default_subject = "Implementacao Aplicada"
        if range_text:
            details = f"{summary} - {range_text}"
    elif frozen == "Checklist":
        default_subject = "Checklist Gerada"
    elif frozen == "Analyze":
        default_subject = "Analise Concluida"
    elif frozen == "Converge":
        default_subject = "Convergencia Avaliada"
        details = ""
    elif frozen == "Task2Issues":
        default_subject = "Issues Sincronizadas"
        details = ""
    elif frozen == "Constitution":
        default_subject = "Constituicao Definida"
    else:
        default_subject = f"{len(staged)} arquivos Snapeados"
        details = ""

    subject = default_subject
    if title_text:
        subject = title_text[:1].upper() + title_text[1:]
    subject = subject.replace('"', "'")

    title_head = f"{snap_prefix} {frozen}: "
    suffix = f" ({details})" if details else ""
    allowed = max(1, 72 - len(title_head) - 2 - len(suffix))
    if len(subject) > allowed:
        subject = subject[: allowed - 1] + "\u2026"
    title = f'{title_head}"{subject}"{suffix}'
    # <<< SNAPSHOT-TITLE-V2 <<<

    # Synopsis (inherited from the previous snapshot, like /git-commit).
    if synopsis_lines:
        synopsis = list(synopsis_lines)
    else:
        previous = _git(repo_root, "log", "-1", "--format=%B").stdout.splitlines()
        synopsis = []
        in_synopsis = False
        for line in previous:
            if re.match(r"^Sinopse:?\s*$", line):
                in_synopsis = True
                continue
            if not in_synopsis:
                continue
            if re.match(r"^(Entregas|Conclu)", line) or (
                not re.match(r"^\s*-\s", line) and line.strip()
            ):
                break
            bullet = re.match(r"^\s*-\s*(.+)$", line)
            if bullet:
                synopsis.append(bullet.group(1).replace(_EM_BULLET, "").strip())

    deduped: list[str] = []
    seen: set[str] = set()
    for item in synopsis:
        key = item.lower()
        if key not in seen:
            seen.add(key)
            deduped.append(item)
        if len(deduped) >= 6:
            break
    synopsis = deduped

    # "Concluído" ledger for the frozen phase.
    done: list[str] = []
    if feature_dir is not None:
        spec_path = feature_dir / "spec.md"
        if spec_path.is_file():
            spec_text = spec_path.read_text(encoding="utf-8")
            stories = sum(
                1 for line in spec_text.splitlines() if line.startswith("### User Story")
            )
            sessions = sum(
                1 for line in spec_text.splitlines() if line.startswith("### Session ")
            )
            requirements = sum(
                1
                for line in spec_text.splitlines()
                if re.match(r"^- \*\*FR-\d+\*\*", line)
            )
            done.append(f"Specify: {stories} histórias / {requirements} Requirements")
            if re.search(r"^## Clarifications", spec_text, re.MULTILINE):
                done.append(f"Clarify: {sessions} sessões")
        artifacts = sum(
            1
            for name in ("plan.md", "research.md", "data-model.md", "quickstart.md")
            if (feature_dir / name).is_file()
        )
        contracts = feature_dir / "contracts"
        if contracts.is_dir():
            artifacts += sum(1 for entry in contracts.iterdir() if entry.is_file())
        if artifacts:
            done.append(f"Plan: {artifacts} artefatos planejados")
        if task_count or (feature_dir / "tasks.md").is_file():
            done.append(f"Tasks: {task_count} tarefas / {phase_count} fases")
        analysis_path = feature_dir / "analysis.md"
        if analysis_path.is_file():
            findings = sum(
                1
                for line in analysis_path.read_text(encoding="utf-8").splitlines()
                if re.match(r"^\|\s*[A-Z][0-9]+\s*\|", line)
            )
            if findings:
                done.append(f"Analyze: {findings} achados")
            else:
                done.append("Analyze: relatório presente")
        implemented = sum(1 for path in staged if path.startswith("src/"))
        if implemented:
            done.append(f"Implement: {len(staged)} arquivos")
        check_dir = feature_dir / "checklists"
        if check_dir.is_dir():
            total = sum(1 for entry in check_dir.iterdir() if entry.is_file())
            if total:
                done.append(f"Checklist: {total} listas")

    current_prefix = f"{frozen}:"
    done = [
        f"{item} (Current)" if item.startswith(current_prefix) else item
        for item in done
    ]

    # Next command, from the hook event name. Use the event, not the detected
    # phase: after_* records the phase that just finished; before_* announces
    # the command that is about to run.
    mapping = {
        "after_constitution": ("/speckit.specify", ""),
        "after_specify": ("/speckit.plan", "/speckit.clarify"),
        "after_clarify": ("/speckit.plan", ""),
        "after_plan": ("/speckit.tasks", "/speckit.checklist"),
        "after_checklist": ("/speckit.tasks", ""),
        "after_tasks": ("/speckit.implement", "/speckit.analyze"),
        "after_analyze": ("/speckit.implement", ""),
        "after_implement": ("/speckit.converge", ""),
    }
    next_mandatory = ""
    next_optional = ""
    next_special: list[str] = []
    if event_name in mapping:
        next_mandatory, next_optional = mapping[event_name]
    elif event_name == "after_converge":
        next_special = [
            "Se convergiu: revisar/abrir PR",
            "Se adicionou tarefas: /speckit.implement e /speckit.converge",
        ]
    elif event_name.startswith("before_"):
        next_mandatory = f"/speckit.{event_name[len('before_'):]}"

    # Assemble body.
    parts: list[str] = []
    if synopsis:
        parts.append("Sinopse:")
        parts.extend(f"{_EM_BULLET} {item}" for item in synopsis)
        parts.append("")
    if phase_items:
        section_header = {
            "Specify": "Especificacoes Criadas:",
            "Clarify": "Clarificacoes Feitas:",
            "Plan": "Artefatos Planejados:",
            "Tasks": "Tasks Criadas:",
            "Implement": "Implement:",
            "Converge": "Convergencia Aplicada:",
            "Checklist": "Checklists Criadas:",
            "Analyze": "Achados:",
        }.get(frozen, "")
        if section_header:
            parts.append(section_header)
            parts.extend(phase_items)
            parts.append("")
    if done:
        parts.append("Entregas Concluidas:")
        parts.extend(f"{_EM_PIN} {item}" for item in done)
        parts.append("")
    if next_mandatory or next_optional or next_special:
        parts.append("Próximo Comando:")
        parts.append(f"{_EM_POINT} Fluxo: {_flow_text(frozen)}")
        if next_mandatory:
            parts.append(f"{_EM_POINT} Obrigatório: {next_mandatory}")
        if next_optional:
            parts.append(f"{_EM_POINT} Opcional: {next_optional}")
        parts.extend(f"{_EM_POINT} {item}" for item in next_special)
        parts.append("")
    stat = _git(repo_root, "diff", "--cached", "--stat").stdout.strip()
    if stat:
        parts.append("Local:")
        parts.extend(
            f"{_EM_FOLDER} {line.strip()}"
            for line in stat.splitlines()
            if line.strip()
        )
    body = "\n".join(parts).rstrip()

    committed = _git(repo_root, "commit", "-q", "-m", title, "-m", body)
    if committed.returncode != 0:
        output = (committed.stdout + committed.stderr).strip()
        print(f"[specify] Error: git commit failed: {output}", file=sys.stderr)
        return 1

    print(f"[OK] Snapshot committed ({frozen}): {title}", file=sys.stderr)
    return 0


def main(argv: list[str]) -> int:
    event_name = argv[0] if argv else ""
    if not event_name:
        print(
            f"Usage: {Path(sys.argv[0]).name} <event_name> "
            "[--synopsis-file <path>] [--title-file <path>] [--items-file <path>]",
            file=sys.stderr,
        )
        return 1

    synopsis_file = ""
    title_file = ""
    items_file = ""
    index = 1
    while index < len(argv):
        if argv[index] == "--synopsis-file":
            if index + 1 >= len(argv):
                print(
                    "[specify] Error: --synopsis-file requires a path argument",
                    file=sys.stderr,
                )
                return 1
            synopsis_file = argv[index + 1]
            index += 2
        elif argv[index] == "--title-file":
            if index + 1 >= len(argv):
                print(
                    "[specify] Error: --title-file requires a path argument",
                    file=sys.stderr,
                )
                return 1
            title_file = argv[index + 1]
            index += 2
        elif argv[index] == "--items-file":
            if index + 1 >= len(argv):
                print(
                    "[specify] Error: --items-file requires a path argument",
                    file=sys.stderr,
                )
                return 1
            items_file = argv[index + 1]
            index += 2
        else:
            index += 1

    # Transport-only artifact: read, then delete BEFORE `git add .` so it is
    # never staged into the snapshot it describes.
    synopsis_lines: list[str] = []
    if synopsis_file:
        synopsis_path = Path(synopsis_file)
        if not synopsis_path.is_file():
            print(
                f"[specify] Warning: synopsis file '{synopsis_file}' not found",
                file=sys.stderr,
            )
        else:
            try:
                raw_synopsis = synopsis_path.read_text(encoding="utf-8")
            except (OSError, UnicodeDecodeError) as exc:
                print(
                    f"[specify] Warning: could not read synopsis file "
                    f"'{synopsis_file}': {exc}",
                    file=sys.stderr,
                )
            else:
                for line in raw_synopsis.splitlines():
                    cleaned = re.sub(r"^\s*-\s*", "", line).strip()
                    if cleaned:
                        synopsis_lines.append(cleaned)
            finally:
                try:
                    synopsis_path.unlink()
                except OSError:
                    pass

    # Snapshot mode: agent-authored title text (transport-only artifact).
    # Read and delete BEFORE `git add .`; the finally block guarantees the
    # temporary file is removed even when reading fails.
    title_text = ""
    if title_file:
        title_path = Path(title_file)
        try:
            if not title_path.is_file():
                print(
                    f"[specify] Warning: title file '{title_file}' not found",
                    file=sys.stderr,
                )
            else:
                try:
                    raw_title = title_path.read_text(encoding="utf-8")
                except (OSError, UnicodeDecodeError) as exc:
                    print(
                        f"[specify] Warning: could not read title file "
                        f"'{title_file}': {exc}",
                        file=sys.stderr,
                    )
                else:
                    for line in raw_title.splitlines():
                        cleaned = re.sub(r"^\s*-\s*", "", line).strip()
                        if cleaned:
                            title_text = cleaned
                            break
        finally:
            try:
                title_path.unlink()
            except OSError:
                pass

    # Snapshot mode: agent-authored phase items (transport-only artifact).
    # One item per line, emitted verbatim under the phase section.
    item_lines: list[str] = []
    if items_file:
        items_path = Path(items_file)
        if not items_path.is_file():
            print(
                f"[specify] Warning: items file '{items_file}' not found",
                file=sys.stderr,
            )
        else:
            try:
                raw_items = items_path.read_text(encoding="utf-8")
            except (OSError, UnicodeDecodeError) as exc:
                print(
                    f"[specify] Warning: could not read items file "
                    f"'{items_file}': {exc}",
                    file=sys.stderr,
                )
            else:
                parsed_items = [
                    re.sub(r"^\s*-\s*", "", line).rstrip()
                    for line in raw_items.splitlines()
                ]
                while parsed_items and not parsed_items[0].strip():
                    parsed_items.pop(0)
                while parsed_items and not parsed_items[-1].strip():
                    parsed_items.pop()
                item_lines = parsed_items
            finally:
                try:
                    items_path.unlink()
                except OSError:
                    pass

    script_dir = Path(__file__).resolve().parent
    repo_root = _find_project_root(script_dir) or Path.cwd()

    if shutil.which("git") is None:
        print("[specify] Warning: Git not found; skipped auto-commit", file=sys.stderr)
        return 0

    probe = subprocess.run(
        ["git", "rev-parse", "--is-inside-work-tree"],
        cwd=repo_root,
        capture_output=True,
        text=True,
    )
    if probe.returncode != 0:
        print(
            "[specify] Warning: Not a Git repository; skipped auto-commit",
            file=sys.stderr,
        )
        return 0

    config_file = repo_root / ".specify" / "extensions" / "git" / "git-config.yml"
    if not config_file.is_file():
        # No config file — auto-commit disabled by default
        return 0

    enabled, commit_msg = _parse_auto_commit_config(config_file, event_name)
    if not enabled:
        return 0

    try:
        config_text = config_file.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError):
        return 0
    commit_style = (_parse_scalar(config_text, "commit_style") or "fixed").lower()
    if commit_style not in ("fixed", "conventional", "snapshot"):
        print(
            f"[specify] Warning: unknown commit_style '{commit_style}' in "
            "git-config.yml (expected 'fixed', 'conventional' or 'snapshot'); "
            "defaulting to 'fixed'",
            file=sys.stderr,
        )
        commit_style = "fixed"
    snap_prefix = _parse_scalar(config_text, "snapshot_prefix") or "\U0001F4F8"

    # Check if there are changes to commit
    def _quiet(*args: str) -> bool:
        return (
            subprocess.run(
                ["git", *args], cwd=repo_root, capture_output=True, text=True
            ).returncode
            == 0
        )

    untracked = subprocess.run(
        ["git", "ls-files", "--others", "--exclude-standard"],
        cwd=repo_root,
        capture_output=True,
        text=True,
    ).stdout.strip()
    if _quiet("diff", "--quiet", "HEAD") and _quiet("diff", "--cached", "--quiet") and not untracked:
        print(f"[specify] No changes to commit after {event_name}", file=sys.stderr)
        return 0

    if commit_style == "snapshot":
        return _snapshot(
            repo_root, event_name, synopsis_lines, title_text, snap_prefix, item_lines
        )

    # Derive a human-readable command name from the event
    # e.g., after_specify -> specify, before_plan -> plan
    command_name = re.sub(r"^(after_|before_)", "", event_name)
    phase = "before" if event_name.startswith("before_") else "after"

    if not commit_msg:
        commit_msg = f"[Spec Kit] Auto-commit {phase} {command_name}"

    steps = [
        (["git", "add", "."], "git add"),
        (["git", "commit", "-q", "-m", commit_msg], "git commit"),
    ]
    for cmd, label in steps:
        result = subprocess.run(cmd, cwd=repo_root, capture_output=True, text=True)
        if result.returncode != 0:
            output = (result.stdout + result.stderr).strip()
            print(f"[specify] Error: {label} failed: {output}", file=sys.stderr)
            return 1

    print(f"[OK] Changes committed {phase} {command_name}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))