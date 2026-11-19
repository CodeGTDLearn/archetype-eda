#!/usr/bin/env python3
"""Git extension: auto_commit.py

Automatically commit changes after a Spec Kit command completes.
Python port of ``auto-commit.sh`` / ``auto-commit.ps1``.
Checks per-command config keys in git-config.yml before committing.

Usage: auto_commit.py <event_name> [--synopsis-file <path>] [--title-file <path>]
  e.g.: auto_commit.py after_specify
  e.g.: auto_commit.py before_implement --synopsis-file /tmp/synopsis.txt
        (commit_style: snapshot)
  e.g.: auto_commit.py before_implement --title-file /tmp/title.txt
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
        line = record[:-1]
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


def _snapshot(
    repo_root: Path,
    event_name: str,
    synopsis_lines: list[str],
    title_text: str,
    snap_prefix: str,
) -> int:
    """Commit a rollback snapshot of the state left by the previous phase.

    Mirrors auto-commit.ps1: stage first, classify the frozen phase from the
    staged diff, then build title + body (inherited synopsis, "Concluído",
    next phase, --stat).
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

    # Frozen phase, from the staged diff (never from hook order: /analyze and
    # /checklist may run in either order).
    frozen = "Snapshot"
    if matches(r"^src/"):
        frozen = "Implement"
    elif matches(r"(^|/)tasks\.md$"):
        frozen = "Tasks"
    elif matches(r"(^|/)checklists/"):
        frozen = "Checklist"
    elif matches(r"(^|/)(plan|research|data-model|quickstart)\.md$") or matches(
        r"(^|/)contracts/"
    ):
        frozen = "Plan"
    elif matches(r"(^|/)spec\.md$"):
        frozen = "Specify"

    tasks_rel = next(
        (path for path in staged if re.search(r"(^|/)tasks\.md$", path)), ""
    )
    tasks_path = repo_root / tasks_rel if tasks_rel else None
    task_count = 0
    phase_count = 0
    if tasks_path is not None and tasks_path.is_file():
        for line in tasks_path.read_text(encoding="utf-8").splitlines():
            if re.match(r"^- \[[ Xx]\] T\d+", line):
                task_count += 1
            elif re.match(r"^## Phase \d+", line):
                phase_count += 1

    if frozen == "Specify":
        summary = "spec.md"
    elif frozen == "Plan":
        plan_count = sum(
            1
            for path in staged
            if re.search(r"(^|/)(plan|research|data-model|quickstart)\.md$", path)
            or re.search(r"(^|/)contracts/", path)
        )
        summary = f"{plan_count} artefatos de design"
    elif frozen == "Tasks":
        summary = f"{task_count} tarefas em {phase_count} fases"
    elif frozen == "Checklist":
        check_count = sum(1 for path in staged if re.search(r"(^|/)checklists/", path))
        if check_count == 0 and feature_dir is not None:
            check_dir = feature_dir / "checklists"
            if check_dir.is_dir():
                check_count = sum(1 for entry in check_dir.iterdir() if entry.is_file())
        summary = f"{check_count} listas"
    else:
        summary = f"{len(staged)} arquivos"

    # Implement bullets: tasks flipped [X] in the staged tasks.md diff.
    task_ids: list[int] = []
    task_bullets: list[str] = []
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
            file_name = ""
            for candidate_path in re.findall(r"`([^`]+)`", text):
                found = ""
                if candidate_path in status_map:
                    found = candidate_path
                elif len(by_name.get(candidate_path, [])) == 1:
                    found = by_name[candidate_path][0]
                if found:
                    letter = status_map[found]
                    file_name = Path(found).name
                    break

            if file_name:
                task_bullets.append(f"- {task_id}: {letter} {file_name}")
            else:
                head = text.split(":", 1)[0]
                head = re.sub(r"\s+", " ", head.replace("`", "")).strip()
                task_bullets.append(f"- {task_id}: {head}")
            task_ids.append(int(task_id[1:]))

        task_ids = sorted(set(task_ids))
        if len(task_bullets) > 10:
            extra = len(task_bullets) - 10
            task_bullets = task_bullets[:10] + [f"- (+{extra} tarefas)"]

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
    if range_text:
        summary = f"{summary} ({range_text})"

    # Title (max 72 chars). An agent-authored title replaces the automatic
    # per-phase summary; the first character is capitalized for consistency.
    if title_text:
        summary = title_text[:1].upper() + title_text[1:]
    title_head = f"{snap_prefix} {frozen}: "
    allowed = max(1, 72 - len(title_head))
    if len(summary) > allowed:
        title = f"{title_head}{summary[: allowed - 1]}\u2026"
    else:
        title = f"{title_head}{summary}"

    # Synopsis (inherited from the previous snapshot, like /git-commit).
    if synopsis_lines:
        synopsis = list(synopsis_lines)
    else:
        previous = _git(repo_root, "log", "-1", "--format=%B").stdout.splitlines()
        synopsis = []
        in_synopsis = False
        for line in previous:
            if re.match(r"^Sinopse\s*$", line):
                in_synopsis = True
                continue
            if not in_synopsis:
                continue
            if line.startswith("Conclu") or (
                not re.match(r"^\s*-\s", line) and line.strip()
            ):
                break
            bullet = re.match(r"^\s*-\s*(.+)$", line)
            if bullet:
                synopsis.append(bullet.group(1).strip())

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
            done.append(f"Specify: {stories} histórias")
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
            done.append(f"Plan: {artifacts} artefatos de design")
        if task_count or (feature_dir / "tasks.md").is_file():
            done.append(f"Tasks: {task_count} tarefas / {phase_count} fases")
        if (feature_dir / "analysis.md").is_file():
            done.append("Analyze: relatório presente")
        implemented = sum(1 for path in staged if path.startswith("src/"))
        if implemented:
            done.append(f"Implement: {implemented} arquivos")
        check_dir = feature_dir / "checklists"
        if check_dir.is_dir():
            total = sum(1 for entry in check_dir.iterdir() if entry.is_file())
            if total:
                done.append(f"Checklist: {total} listas")

    # Next phase, from the hook event name.
    next_phase = re.sub(r"^before_", "", event_name)
    if next_phase:
        next_phase = next_phase[:1].upper() + next_phase[1:]

    # Assemble body.
    parts: list[str] = []
    if synopsis:
        parts.append("Sinopse")
        parts.extend(f"- {item}" for item in synopsis)
        parts.append("")
    if task_bullets:
        parts.extend(task_bullets)
        parts.append("")
    if done:
        parts.append("Concluído")
        parts.extend(f"- {item}" for item in done)
        parts.append("")
    if next_phase:
        parts.append(f"A seguir: {next_phase}")
        parts.append("")
    stat = _git(repo_root, "diff", "--cached", "--stat").stdout.strip()
    if stat:
        parts.append(stat)
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
            "[--synopsis-file <path>] [--title-file <path>]",
            file=sys.stderr,
        )
        return 1

    synopsis_file = ""
    title_file = ""
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
        return _snapshot(repo_root, event_name, synopsis_lines, title_text, snap_prefix)

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
