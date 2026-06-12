#!/bin/bash
# Script to regenerate the corrupted gradle-wrapper.jar

set -e

echo "Regenerating gradle-wrapper.jar for Gradle 9.3.1..."
gradle wrapper --gradle-version 9.3.1

echo "gradle-wrapper.jar has been regenerated successfully!"
echo "The file is ready to be committed and pushed."
