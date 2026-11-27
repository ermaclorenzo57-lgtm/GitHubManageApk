"""Check declared Gradle distribution and pin its published checksum."""
from pathlib import Path
import sys
import hashlib

root = Path(sys.argv[1])
props = root / "gradle/wrapper/gradle-wrapper.properties"
data = props.read_text()
expected = "d725d707bfabd4dfdc958c624003b3c80accc03f7037b5122c4b1d0ef15cecab"
if "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.9-bin.zip" not in data:
    raise SystemExit("Unexpected Gradle distribution; review required")
jar = root / "gradle/wrapper/gradle-wrapper.jar"
if jar.exists():
    if hashlib.sha256(jar.read_bytes()).hexdigest() != "498495120a03b9a6ab5d155f5de3c8f0d986a449153702fb80fc80e134484f17":
        raise SystemExit("Gradle wrapper JAR differs from official 8.9 checksum; investigate before release")
else:
    raise SystemExit("No standard wrapper JAR found; review actual launcher and Gradle distribution before release")
if "distributionSha256Sum=" in data:
    if f"distributionSha256Sum={expected}" not in data:
        raise SystemExit("Unexpected distribution checksum")
else:
    props.write_text(data.rstrip()+"\ndistributionSha256Sum="+expected+"\n")
print("Gradle 8.9 wrapper and distribution checksum configured")
