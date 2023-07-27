#!/bin/bash
# List of package folders
projectFolderNames=("web" "gsonrudderadapter" "repository" "models" "rudderjsonadapter" "jacksonrudderadapter" "moshirudderadapter")

for projectFolder in ${projectFolderNames[@]}; do
  # Set of package project name

  # Navigate to folder and perform the string replacement in project.json
  packageName=$projectFolder

  cd $projectFolder
  package_version=$(jq -r .version package.json)
  echo "Generate github release notes file: ${packageName}, $package_version"
  awk -v ver="$package_version" '
   /^(##|###) \[?[0-9]+.[0-9]+.[0-9]+/ {
      if (p) { exit };
      if (index($2, "[")) {
          split($2, a, "[");
          split(a[2], a, "]");
          if (a[1] == ver) {
              p = 1
          }
      } else {
          if ($2 == ver) {
              p = 1
          }
      }
  } p
  ' './CHANGELOG.md' > './CHANGELOG_LATEST.md'
  cd ..
done