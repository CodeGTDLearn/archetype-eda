---
description: Auto-commit changes after a Spec Kit command completes
---


<!-- Extension: git -->
<!-- Config: .specify/extensions/git/ -->
# Auto-Commit Changes

Automatically stage and commit all changes after a Spec Kit command completes.

## Behavior

This command is invoked as a hook after (or before) core commands. It:

1. Determines the event name from the hook context (e.g., if invoked as an `after_specify` hook, the event is `after_specify`; if `before_plan`, the event is `before_plan`)
2. Checks `.specify/extensions/git/git-config.yml` for the `auto_commit` section
3. Looks up the specific event key to see if auto-commit is enabled
4. Falls back to `auto_commit.default` if no event-specific key exists
5. Determines the commit message based on `commit_style` (see below)
6. If enabled and there are uncommitted changes, runs `git add .` + `git commit`

## Commit Message Styles

Controlled by the `commit_style` key in `.specify/extensions/git/git-config.yml`:

- **`fixed`** (default): use the per-command `message` if configured, otherwise a generic `[Spec Kit] Auto-commit <phase> <command>` message.
- **`conventional`**: inspect the actual changes (`git diff` / `git status`) since the last commit and generate a single-line [Conventional Commit](https://www.conventionalcommits.org/) message (`type(scope): subject`, e.g. `feat: add OAuth specification` or `docs: update implementation plan`) that accurately summarizes the change. Write this message to a temporary file and pass the file's path to the script (see Execution below). The configured `message` values are ignored in this mode.
- **`snapshot`**: rollback snapshot committed by a `before_*` hook, BEFORE the next phase runs. It freezes the state left by the previous phase (`before_tasks` freezes what `plan` produced). The script classifies the frozen phase from the staged diff (`src/` → Implement, `tasks.md` → Tasks, `checklists/` → Checklist, plan artifacts → Plan, `spec.md` → Specify; first match wins — never from hook order, since `/analyze` and `/checklist` may run in either order) and builds the title `📸 <frozen phase>: <summary>` plus a body with a synopsis, a `Concluído` ledger, `A seguir: <next phase>` and the staged `--stat`. The agent may refine the synopsis (see Execution below). The last phase of a branch gets no snapshot on purpose: it closes with a PR/merge.

## Execution

Determine the event name from the hook that triggered this command, then run the script:

- **Bash**: `.specify/extensions/git/scripts/bash/auto-commit.sh <event_name> [--message-file <path>]`
- **PowerShell**: `.specify/extensions/git/scripts/powershell/auto-commit.ps1 <event_name> [-MessageFile <path>]`

Replace `<event_name>` with the actual hook event (e.g., `after_specify`, `before_plan`, `after_implement`). Only pass a generated message when `commit_style: conventional` is configured — first check `.specify/extensions/git/git-config.yml` for the value of `commit_style`:

- If `conventional`: inspect the diff and generate a Conventional Commit message. **Do not interpolate the generated message directly into a shell command string** — its content is derived from repository changes and may contain characters (quotes, `$(...)`, backticks) that a shell would execute or that would break command quoting. Instead, write the message to a temporary file using your file-editing tool (not a shell `echo`/`printf`), then pass that file's path via `--message-file <path>` (Bash) or `-MessageFile <path>` (PowerShell).
- If `snapshot` (PowerShell only in this repo): refine the synopsis, then run the script with just `<event_name>` plus `-SynopsisFile <path>`:
  1. Read the previous snapshot body: `git log -1 --format=%B` — extract its `Sinopse` bullet block.
  2. Read what the previous phase left: `git status --short` and `git diff HEAD`.
  3. Revise the synopsis: keep valid bullets, add/adjust up to 6 bullets total. Never list file-by-file, never repeat an existing bullet.
  4. Write the bullets to `$env:TEMP\opencode\synopsis.txt` (UTF-8 without BOM), one per line.
  5. Run: `.specify/extensions/git/scripts/powershell/auto-commit.ps1 <event_name> -SynopsisFile "$env:TEMP\opencode\synopsis.txt"`.
  The script reads and deletes that file BEFORE `git add .` — never write it inside the repository, and never pass text as a shell argument.
- If `fixed` or absent: run the script with just `<event_name>`; it uses the configured/static message.

## Configuration

In `.specify/extensions/git/git-config.yml`:

```yaml
# "fixed" (default) uses the messages below; "conventional" asks the agent
# to generate a Conventional Commit message from the diff instead;
# "snapshot" builds a rollback snapshot from the staged diff (before_* hooks).
commit_style: fixed

auto_commit:
  default: false          # Global toggle — set true to enable for all commands
  after_specify:
    enabled: true          # Override per-command
    message: "[Spec Kit] Add specification"
  after_plan:
    enabled: false
    message: "[Spec Kit] Add implementation plan"
```

## Graceful Degradation

- If Git is not available or the current directory is not a repository: skips with a warning
- If no config file exists: skips (disabled by default)
- If no changes to commit: skips with a message
- If `commit_style: conventional` is set and no generated message was supplied: fails with a clear error instead of silently falling back to the fixed message format
- If `commit_style: snapshot` is set and no `-SynopsisFile` was supplied: the script inherits the synopsis from the previous snapshot body; when there is none, the `Sinopse` section is omitted - it never blocks the phase