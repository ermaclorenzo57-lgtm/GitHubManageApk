"""Validate apksigner output after apksigner verify has succeeded."""
import re
import sys
from pathlib import Path

def verify(report, expected):
    expected = expected.lower()
    if not re.fullmatch(r"[0-9a-f]{64}", expected):
        raise ValueError("Invalid configured certificate digest")
    counts = re.findall(r"(?m)^\s*Number of signers:\s*(\d+)\s*$", report)
    if counts != ["1"]:
        raise ValueError("Expected exactly one APK signer")
    fingerprints = re.findall(
        r"(?im)^\s*(?:Signer #\d+ certificate|V\d+(?:\.\d+)? Signer(?: #\d+)?: certificate) SHA-256 digest:\s*([0-9a-f]{64})\s*$",
        report,
    )
    if not fingerprints:
        raise ValueError("No signing certificate digest found in apksigner output")
    if any(d.lower() != expected for d in fingerprints):
        raise ValueError("APK signer does not match configured certificate")
    return True

if __name__ == "__main__":
    try:
        verify(Path(sys.argv[1]).read_text(encoding="utf-8"), sys.argv[2])
    except (ValueError, OSError) as exc:
        print(f"::error::{exc}", file=sys.stderr)
        sys.exit(1)
