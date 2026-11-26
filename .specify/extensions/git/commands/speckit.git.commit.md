---
description: "Auto-commit changes after a Spec Kit command completes"
---

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
- **`snapshot`**: commit taken by a `speckit.git.commit` hook. An `after_*` hook states the phase that just finished, so its label is authoritative (even when the phase leaves no file signature, as `Task2Issues` does). A `before_*` hook freezes the state left by the previous phase (`before_tasks` freezes what `plan` produced); the event narrows the candidate phases and the staged diff picks the most advanced one present (`src/` → Implement, `analysis.md` → Analyze, `checklists/` → Checklist, `tasks.md` → Tasks, plan artifacts → Plan, `spec.md` → Specify, or Clarify when `spec.md` carries a `## Clarifications` section, `tasks.md` with a `## Phase N: Convergence` section → Converge, or Constitution for `.specify/memory/constitution.md`), so a leftover `checklists/` file cannot mislabel a Specify snapshot. It builds the title `📸 <frozen phase>: "<subject>"[ (<details>)]`, where `<subject>` is the agent-authored free-text subject (falling back to a per-phase default: `Especificacao Gerada`, `Duvidas Esclarecidas`, `Plano Gerado`, `Tarefas Geradas`, `Implementacao Aplicada`, `Checklist Gerada`, `Analise Concluida`, `Convergencia Avaliada`, `Issues Sincronizadas`, `Constituicao Definida`, or `N arquivos Snapeados`) and `<details>` are automatic (Implement: `N arquivos - T012–T015`; Tasks: `N tarefas em M fases`; Plan: `N artefatos planejados`; Checklist: `N listas`; Clarify: `N sessões`; Analyze: `analysis.md`; Constitution: `constitution.md`; Converge/Task2Issues/Specify/Snapshot: omitted), plus a body with `Sinopse:` (`📋` bullets), a per-phase item section (`Especificacoes Criadas:` for Specify, `Clarificacoes Feitas:` for Clarify, `Artefatos Planejados:` for Plan, `Tasks Criadas:` for Tasks, `Implement:` for Implement, `Convergencia Aplicada:` for Converge, `Checklists Criadas:` for Checklist, `Achados:` for Analyze; `✅` lines, an empty summary rendered as `⬜`), an `Entregas Concluidas:` ledger (`📌` lines, `(Current)` on the frozen phase; the `Tasks` line counts every task in the feature's `tasks.md`, even when it is not staged), a `Próximo Comando:` section (`➡ Fluxo:`, `➡️ Obrigatório:`, `➡️ Opcional:`, and the converge branch) and a `Local:` section (`📂` on every `--stat` line). The agent may supply the title, refine the synopsis and write the phase items with `--items-file` (see Execution below). The last phase of a branch gets no snapshot on purpose: it closes with a PR/merge.

## Execution

Determine the event name from the hook that triggered this command, then run the script:

- **Bash**: `.specify/extensions/git/scripts/bash/auto-commit.sh <event_name> [--message-file <path>] [--title-file <path>]`
- **PowerShell**: `.specify/extensions/git/scripts/powershell/auto-commit.ps1 <event_name> [-MessageFile <path>] [-TitleFile <path>]`
- **Python**: `.specify/extensions/git/scripts/python/auto_commit.py <event_name> [--synopsis-file <path>] [--title-file <path>]`

Replace `<event_name>` with the actual hook event (e.g., `after_specify`, `before_plan`, `after_implement`). Only pass a generated message when `commit_style: conventional` is configured — first check `.specify/extensions/git/git-config.yml` for the value of `commit_style`:

- If `conventional`: inspect the diff and generate a Conventional Commit message. **Do not interpolate the generated message directly into a shell command string** - its content is derived from repository changes and may contain characters (quotes, `$(...)`, backticks) that a shell would execute or that would break command quoting. Instead, write the message to a temporary file using your file-editing tool (not a shell `echo`/`printf`), then pass that file's path via `--message-file <path>` (Bash) or `-MessageFile <path>` (PowerShell).
- If `snapshot` (Bash, PowerShell or Python): write the title, refine the synopsis, then run the script with `<event_name>` plus both flags:
  1. Read the previous snapshot body: `git log -1 --format=%B` - extract its `Sinopse` bullet block.
  2. Read what the previous phase left: `git status --short` and `git diff HEAD`.
  3. Write the title to a temporary file (UTF-8 without BOM): a single line with the free-text subject of the frozen phase, e.g. `Pasta de Destino do Modulo`. The script composes the final title as `<snapshot_prefix> <frozen phase>: "<subject>"[ (<details>)]`; provide only the subject. Also write the phase items, one per line, to a temporary file and pass it with `--items-file`: `* US — User Stories` + `✅ US1: ...` / `* FR — Functional Requirements` + `✅ FR-001: ...` (Specify), `✅ <date>: <decision>` (Clarify), `✅ <artifact>` (Plan), `✅ T001: <short summary>` (Tasks), `✅ T010: <short summary> — M src/...` (Implement), `✅ T019: <short summary>` (Converge), `✅ <checklist file>` (Checklist), `✅ A1: <finding>` (Analyze). Each line is emitted verbatim under the phase section; without the file the script derives the items from the staged artifacts.
  4. Revise the synopsis: keep valid bullets, add/adjust up to 6 bullets total. Never list file-by-file, never repeat an existing bullet.
  5. Write the bullets to another temporary file (UTF-8 without BOM), one per line - e.g. `$env:TEMP\opencode\synopsis.txt` on Windows.
  6. Run:
     - **Bash**: `.specify/extensions/git/scripts/bash/auto-commit.sh <event_name> --title-file <path> --synopsis-file <path>`
     - **PowerShell**: `.specify/extensions/git/scripts/powershell/auto-commit.ps1 <event_name> -TitleFile <path> -SynopsisFile <path>`
     - **Python**: `.specify/extensions/git/scripts/python/auto_commit.py <event_name> --title-file <path> --synopsis-file <path>`
  The script reads and deletes both files BEFORE `git add .` - never write them inside the repository, and never pass text as a shell argument. Deletion happens even when reading fails, so no temporary file lingers.
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
- If `commit_style: snapshot` is set and no title file was supplied (or it is empty): the script falls back to the per-phase default subject; the automatic details are still appended - it never blocks the phase. The same holds for `--items-file`: when it is absent the items are derived from the staged artifacts, and the section is omitted only when there is nothing to list
