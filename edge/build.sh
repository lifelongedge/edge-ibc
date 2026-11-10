#!/usr/bin/env bash
# Build IBC.jar and the Windows and Linux distribution zips, the same layout as upstream's `ant dist`.
# Usage: edge/build.sh <dir with the Gateway's jars (jts4launch-*.jar, twslaunch-*.jar)>   (JDK 17 on PATH)
# The Gateway's jars are needed at compile time only and are never copied into the output.
set -euo pipefail
JARS=${1:?usage: edge/build.sh <gateway jars dir>}
ROOT=$(cd "$(dirname "$0")/.." && pwd)
VER=$(cat "$ROOT/resources/version")
OUT=$ROOT/target
rm -rf "$OUT" && mkdir -p "$OUT/classes" "$ROOT/dist"
cd "$ROOT"
javac --release 17 -g:none -nowarn -cp "$JARS/*" -d "$OUT/classes" $(find src -name '*.java' | sort)
jar --create --file "$ROOT/resources/IBC.jar" -C "$OUT/classes" .

stage() {  # stage <name> <python filter over the resource path>
  local d=$OUT/$1
  mkdir -p "$d"
  cp LICENSE.txt "$d/"
  python3 - "$ROOT/resources" "$d" "$2" <<'PY'
import shutil, sys
from pathlib import Path
src, dst, rule = Path(sys.argv[1]), Path(sys.argv[2]), sys.argv[3]
for p in sorted(src.rglob("*")):
    rel = p.relative_to(src).as_posix()
    if p.is_file() and eval(rule, {"rel": rel, "name": p.name}):
        (dst / rel).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(p, dst / rel)
PY
  rm -f "$ROOT/dist/$1-$VER.zip"
  (cd "$d" && python3 -c "import shutil,sys; shutil.make_archive(sys.argv[1], 'zip', '.')" "$ROOT/dist/$1-$VER")
}
stage IBCWin "not (name.endswith('.sh') or name.endswith('.plist'))"
stage IBCLinux "not (name.endswith(('.bat', '.vbs', '.xml', '.plist', '.ps1')) or name.endswith('macos.sh') or name.startswith('IBC ('))"
ls -l "$ROOT/dist"
