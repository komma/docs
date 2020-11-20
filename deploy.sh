#!/bin/bash
dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

target=$dir/target/generated-docs
deploy_branch=gh-pages
http_user=""
if [ "$1" ]; then http_user="$1"; fi
remote_url="https://${http_user}@github.com/$GITHUB_REPOSITORY"

# author, date and message for deployment commit
name=$(git log -n 1 --format='%aN')
email=$(git log -n 1 --format='%aE')
author="$name <$email>"
date=$(git log -n 1 --format='%aD')
message="Built from $(git rev-parse --short HEAD)"

# create temp dir and clone deploy repository
tempdir=$(mktemp -d -p .)
if ! git clone "$remote_url" "$tempdir"; then exit; fi

# change to deploy repository
cd "$tempdir"
currentbranch=$(git symbolic-ref --short -q HEAD)
if ! git checkout "$deploy_branch" &>/dev/null
then
	git checkout --orphan "$deploy_branch" &>/dev/null
        git rm -rf . &>/dev/null
fi
# copy generated files
cp -R "$target/." .

status=$(git status --porcelain)
# if there are any changes
if [ "$status" != "" ]
then
	git config user.email "$email"
	git config user.name "$name"
	git add --all && git commit --message="$message" --author="$author" --date="$date"
	git push -q origin $deploy_branch:$deploy_branch
fi
