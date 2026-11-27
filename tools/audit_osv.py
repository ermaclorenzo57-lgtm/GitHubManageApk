"""Inventory resolved Maven dependencies and fail closed on OSV advisories or errors."""
import json
import sys
import urllib.request
from pathlib import Path

root = Path(sys.argv[1])
rows = [line.split("\t") for line in (root / "dependencies.tsv").read_text().splitlines()]
packages = sorted({(group, name, version) for _, group, name, version in rows})
if not packages:
    raise SystemExit("No dependencies to audit")
components = [{"type": "library", "group": g, "name": n, "version": v,
               "purl": f"pkg:maven/{g}/{n}@{v}"} for g,n,v in packages]
(root / "sbom.cdx.json").write_text(json.dumps({"bomFormat":"CycloneDX",
    "specVersion":"1.5","version":1,"components":components}, indent=2))
findings = []
for start in range(0, len(packages), 100):
    batch = packages[start:start+100]
    queries = [{"package":{"ecosystem":"Maven","name":f"{g}:{n}"},"version":v}
               for g,n,v in batch]
    request = urllib.request.Request("https://api.osv.dev/v1/querybatch",
        data=json.dumps({"queries": queries}).encode(),
        headers={"Content-Type":"application/json","User-Agent":"MaxRepo-dependency-audit"})
    with urllib.request.urlopen(request, timeout=60) as response:
        results = json.load(response)["results"]
    if len(results) != len(batch):
        raise SystemExit("Incomplete OSV response")
    for (g,n,v), result in zip(batch,results):
        if result.get("next_page_token"):
            raise SystemExit("Paginated OSV response requires additional review")
        for vuln in result.get("vulns", []):
            findings.append({"package":f"{g}:{n}","version":v,"advisory":vuln["id"]})
(root / "osv-findings.json").write_text(json.dumps(findings, indent=2))
print(f"Checked {len(packages)} resolved Maven coordinates; {len(findings)} advisory matches")
for finding in findings:
    print(finding["package"], finding["version"], finding["advisory"])
if findings:
    raise SystemExit("Known vulnerabilities require review before release")
