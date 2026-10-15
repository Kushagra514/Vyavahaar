#!/usr/bin/env bash
set -e

# Vyavahaar Backend Test Runner
# Disables external host system plugin auto-loading (e.g. ROS launch_testing) for clean isolated testing.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_ROOT/backend"

cd "$BACKEND_DIR"

if [ -d "venv" ]; then
    # shellcheck disable=SC1091
    source venv/bin/activate
fi

export PYTHONPATH="."
export PYTEST_DISABLE_PLUGIN_AUTOLOAD=1

echo "==> Running Vyavahaar Backend API Automated Test Suite..."
pytest tests/test_api.py -v -p anyio -p asyncio "$@"
echo "==> All backend tests passed successfully!"
