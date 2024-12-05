#!/bin/bash

# 1) PROMPT
ENABLE_GIT_PROMPT="${ITEM_MARK}Enable 'Git Support'"
GIT_URL_PROMPT="${TOPIC_MARK}Add 'Git Repo URL'"
GIT_BRANCH_PROMPT="${TOPIC_MARK}Define the Principal 'Branch Name'"

# 2) VARIAVEIS DE SWITCHES DO MVN CLI + INPUTS
enable_git_support=$(get_user_input_or_default_value "boleana" "true" "$ENABLE_GIT_PROMPT")
git_url_repo=$(get_user_input_or_default_value "git_url" "https://github.com/your-git-user/your-repo.git" "$GIT_URL_PROMPT" "$enable_git_support")
git_branch_repo=$(get_user_input_or_default_value "texto" "main" "$GIT_BRANCH_PROMPT" "$enable_git_support")

# 3) ADD PROMPT IN PROGRESS_BAR
add_apostrophe_item_in_progress_bar "${ENABLE_GIT_PROMPT}" "${enable_git_support}"