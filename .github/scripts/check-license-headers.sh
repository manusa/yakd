#!/usr/bin/env bash
#
# Check that all source files have the required license header.
# This script uses git ls-files to respect .gitignore exclusions.
#
# Usage: .github/scripts/check-license-headers.sh
#
# Exit codes:
#   0 - All files have correct license headers
#   1 - Some files are missing or have incorrect license headers
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
HEADER_FILE="$SCRIPT_DIR/../license-header.txt"

if [[ ! -f "$HEADER_FILE" ]]; then
  echo "Error: License header file not found at $HEADER_FILE"
  exit 1
fi

cd "$REPO_ROOT"

HEADER_LINES=$(wc -l < "$HEADER_FILE")
MISSING_HEADER=0
COUNT=0

while IFS= read -r file; do
  COUNT=$((COUNT + 1))
  if ! diff -q <(head -n "$HEADER_LINES" "$file") "$HEADER_FILE" > /dev/null 2>&1; then
    echo "Missing or incorrect license header: $file"
    MISSING_HEADER=1
  fi
done < <(git ls-files 'src/main/java/**/*.java' 'src/test/java/**/*.java' 'src/main/frontend/*.js' 'src/main/frontend/src/**/*.js' 'src/main/frontend/src/**/*.jsx' 'src/main/frontend/src/**/*.ts' 'src/main/frontend/src/**/*.css')

echo "Checked $COUNT files"

if [[ $MISSING_HEADER -eq 1 ]]; then
  echo "Error: Some files are missing the required license header"
  exit 1
fi

echo "All source files have the required license header"