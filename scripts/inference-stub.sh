#!/usr/bin/env bash
# External inference script STUB for the llm-flag-inference mechanism.
# See cli-contract.md Section 2; tasks.md T008.
#
# This stub returns a deterministic Map<String, String> of flag -> value
# for testing. The real operator-supplied script MUST set LLM temperature=0
# per FR-019 and produce equivalent output for the same (description, contract)
# inputs across invocations.
#
# The stub DERIVES the entity from the contract it receives (the contract is
# the source of truth): the entity name and the attribute list are read from
# the contract, so the end-to-end path (contract -> derived flags -> entity)
# is exercised without a real LLM.
#
# Contract (per cli-contract.md Section 2):
#   Args: --use-case-description <text> --use-case-contract <json>
#   OR:   --use-case-description-file <path> --use-case-contract-file <path>
#   Stdout (success): single-line JSON object: {"flag1":"value1",...}
#   Stdout (failure): empty
#   Exit 0 on success, non-zero on failure
#   Diagnostics go to stderr
set -euo pipefail

DESCRIPTION=""
CONTRACT=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --use-case-description)       DESCRIPTION="$2"; shift 2;;
    --use-case-description-file)  DESCRIPTION="$(cat "$2")"; shift 2;;
    --use-case-contract)          CONTRACT="$2"; shift 2;;
    --use-case-contract-file)     CONTRACT="$(cat "$2")"; shift 2;;
    --catalog-file)               shift 2;;  # ignored by stub
    *) echo "Unknown arg: $1" >&2; exit 3;;
  esac
done

if [[ -z "$DESCRIPTION" || -z "$CONTRACT" ]]; then
    echo "ERROR: --use-case-description and --use-case-contract (or their -file variants) are required" >&2
    exit 3
fi

# ---------------------------------------------------------------------------
# Derive the entity name from the contract
# ---------------------------------------------------------------------------
ENTITY_NAME="$(printf '%s' "$CONTRACT" \
    | sed -n 's/.*"entityName"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n 1)"
if [[ -z "$ENTITY_NAME" ]]; then
    ENTITY_NAME="$(printf '%s' "$CONTRACT" \
        | sed -n 's/.*"entity_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n 1)"
fi

# ---------------------------------------------------------------------------
# Derive the attribute list (Type:name,Type:name) from the contract
# ---------------------------------------------------------------------------
ATTRIBUTES=""
while IFS= read -r chunk; do
    [[ -z "$chunk" ]] && continue
    attr_name="$(printf '%s' "$chunk" \
        | sed -n 's/.*"name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
    [[ -z "$attr_name" ]] && continue
    attr_type="$(printf '%s' "$chunk" \
        | sed -n 's/.*"type"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
    [[ -z "$attr_type" ]] && attr_type="String"
    # only the simple name survives the catalog regex (no FQN, no dots)
    attr_type="${attr_type##*.}"
    if [[ -n "$ATTRIBUTES" ]]; then ATTRIBUTES="$ATTRIBUTES,"; fi
    ATTRIBUTES="$ATTRIBUTES$attr_type:$attr_name"
done <<< "$(printf '%s' "$CONTRACT" | tr '{' '\n' | grep '"name"' || true)"

# ---------------------------------------------------------------------------
# Emit the proposal
# ---------------------------------------------------------------------------
EXTRA=""
if [[ -n "$ENTITY_NAME" ]]; then
    EXTRA="$EXTRA,\"entity_name\":\"$ENTITY_NAME\""
fi
if [[ -n "$ATTRIBUTES" ]]; then
    EXTRA="$EXTRA,\"enable_custom_attributes\":\"true\""
    EXTRA="$EXTRA,\"custom_attributes_list\":\"$ATTRIBUTES\""
fi

printf '%s' "{\"enable_jwt_authentication\":\"false\",\"api_port\":\"8080\",\"enable_binding_properties\":\"false\",\"properties_filetype\":\"1\",\"language\":\"pt\"$EXTRA}"
echo ""
exit 0
