#!/bin/bash
# List of package folders
projectFolderNames=("web" "gsonrudderadapter" "repository" "models" "rudderjsonadapter" "jacksonrudderadapter" "moshirudderadapter")

for projectFolder in ${projectFolderNames[@]}; do
  # Set of package project name

  # Navigate to folder and perform the string replacement in project.json
  packageName=$projectFolder
  cd $projectFolder
  package_version=$(jq -r .version package.json)
  echo "Sync version in project.json: ${packageName}, $package_version"
  # This will not work on MAC system
  sed -i "s/$packageName@.*/$packageName@$package_version\",/" project.json
  cd ..
done