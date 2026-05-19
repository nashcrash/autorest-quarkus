#!/bin/bash

CHECK_BRANCH="main"

# --- CHECK CURRENT BRANCH ---
# Get the name of the current active branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# --- CHECK FOR POM.XML ---
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found in the current directory!"
    exit 1
fi

# --- 1. EXTRACT CURRENT VERSION ---
# We use grep and sed to find the first <version> tag content
CURRENT_VERSION=$(grep -m 1 '<version>' pom.xml | sed -E 's/.*<version>(.*)<\/version>.*/\1/')

if [ -z "$CURRENT_VERSION" ]; then
    echo "Error: Could not extract version from pom.xml"
    exit 1
fi

echo "RELEASE MAKER: all files will be pushed to the repository, make sure you have committed all your changes before running this script."
echo
echo "Current version detected: $CURRENT_VERSION"

IFS='-' read -r number snapshot <<< "$CURRENT_VERSION"

# --- 2. CALCULATE NEXT SUGGESTED VERSION (x.y.z+1) ---
# Splitting version by dots
IFS='.' read -r major minor patch build <<< "$number"

if [ "$CURRENT_BRANCH" != "$CHECK_BRANCH" ]; then
  echo "You are on branch '$CURRENT_BRANCH'. Apply a build number"
  if [ -z "$build" ]; then
    build=0
  fi
fi

if [ -z "$build" ]; then
  SUGGESTED_PATCH=$((patch + 1))
  SUGGESTED_VERSION="$major.$minor.$SUGGESTED_PATCH"
else
  SUGGESTED_BUILD=$((build + 1))
  SUGGESTED_VERSION="$major.$minor.$patch.$SUGGESTED_BUILD"
fi

# --- 3. USER INPUT ---
read -p "Enter new version [$SUGGESTED_VERSION]: " NEW_VERSION
NEW_VERSION=${NEW_VERSION:-$SUGGESTED_VERSION}

echo "Updating to version: $NEW_VERSION"

# --- 4. MAVEN BUMP ---
echo "Running mvn versions:set..."
mvn versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false

if [ $? -ne 0 ]; then
    echo "Error: Maven command failed."
    exit 1
fi

# --- 5. GIT OPERATIONS ---
echo "Committing changes to Git..."
git add .

# If there are other modules (sub-poms), you might want to add them too:
# git add "**/pom.xml"

git commit -m "build: bump version to $NEW_VERSION"

echo "Creating git tag: v$NEW_VERSION"
git tag "v$NEW_VERSION"

# --- 6. PUSH ---
echo "Pushing commit and tags to origin..."
git push
git push --tags

# --- 7. DEPLOY ---
read -p "Do you want to release to the Maven Repository? (y/n): " CONFIRM

# Normalize input and check confirmation
case "$CONFIRM" in
  y|Y)
    echo "Starting Maven release..."
    mvn deploy -P release -s .settings.xml
    ;;
  *)
    echo "Release aborted by user."
    exit 0
    ;;
esac

echo "Successfully released version $NEW_VERSION!"