#!/bin/bash

txt_own_repos="repos.txt"
oca_repos="repos_oca.csv"
non_ent_repos="repos_non_ent.csv"
extra_repos="repos_extra.csv"
branch_name=$1


function check_status_directory() {
  DIRECTORY="$1"
  if [[ -d "$DIRECTORY" ]]; then
    echo "$DIRECTORY exists on your filesystem."
  else
    mkdir "$DIRECTORY"
  fi
}

function check_status_own_repositories() {
  DIRECTORY="$1"
  FILE_REPOS="$2"
  VERSION="$3"


  while read line
  do
    PRE_MODULE=${line:32}
    MODULE=${PRE_MODULE/.git/""}
    FULL_PATH=$DIRECTORY/$MODULE
    if [[ -d "$FULL_PATH" ]]; then
      echo "$FULL_PATH exists on your filesystem."
      git -C "$FULL_PATH" fetch origin
      git -C "$FULL_PATH" reset --hard origin/"$VERSION"
    else
      echo "Will clone repo: $MODULE"
      git -C "$DIRECTORY" clone -b "$VERSION" --depth 1 "$line"
    fi
  done < "$FILE_REPOS"
}


function check_status_non_ent_repositories() {
  NON_ENT_DIRECTORY="$1"
  BASE_OWN_DIRECTORY="$2"
  NON_ENT_CSV="$3"
  ENTERPRISE_DIR="$BASE_OWN_DIRECTORY/enterprise"

  if [[ -d $ENTERPRISE_DIR ]]; then
      while IFS=, read -r MOD_NAME_LINE
      do
          MODULE="$ENTERPRISE_DIR/$MOD_NAME_LINE"
          cp -r "$MODULE" "$NON_ENT_DIRECTORY"
          echo "Module $MOD_NAME_LINE was moved."
          if [ "$MOD_NAME_LINE" == "hr_payroll_account" ]; then
              old_text=", 'account_accountant'"
              new_text="    'depends': ['hr_payroll'],"
              sed -i "/$old_text/c\\$new_text" "$NON_ENT_DIRECTORY/$MOD_NAME_LINE/__manifest__.py"
              echo "enterprise dependency over $MOD_NAME_LINE was removed"
          fi
      done < "$NON_ENT_CSV"
  fi
}

function check_status_extra_repositories() {
  EXTRA_MAIN_DIRECTORY="$1"
  EXTRA_REPOS_CSV="$2"
  VERSION="$3"
  python3 scripts/check_repos_extra.py "${EXTRA_MAIN_DIRECTORY}" "${EXTRA_REPOS_CSV}" "$VERSION"
}

odoo_base_directory="repositories"

odoo_base_own_directory="$odoo_base_directory/repos"
odoo_base_oca_directory="$odoo_base_directory/repos_oca"
odoo_non_ent_directory="$odoo_base_directory/repos_non_ent"

check_status_directory "$odoo_base_directory"
check_status_directory "$odoo_base_own_directory"
check_status_directory "$odoo_base_oca_directory"
check_status_directory "$odoo_non_ent_directory"


if [[ $branch_name == *"dev"* ]]; 
then

    check_status_own_repositories "$odoo_base_own_directory" "$txt_own_repos" "$branch_name"

    branch_odoo_version="${branch_name:0:2}.0"
    check_status_extra_repositories "$odoo_base_oca_directory" "$oca_repos" "$branch_odoo_version"
    check_status_non_ent_repositories "$odoo_non_ent_directory" "$odoo_base_oca_directory" "$non_ent_repos"
    check_status_extra_repositories "$odoo_base_oca_directory" "$extra_repos" "$branch_odoo_version"

else

    check_status_own_repositories "$odoo_base_own_directory" "$txt_own_repos" "$branch_name"
    check_status_extra_repositories "$odoo_base_oca_directory" "$oca_repos" "$branch_name"
    check_status_non_ent_repositories "$odoo_non_ent_directory" "$odoo_base_oca_directory" "$non_ent_repos"
    check_status_extra_repositories "$odoo_base_oca_directory" "$extra_repos" "$branch_name"
fi
