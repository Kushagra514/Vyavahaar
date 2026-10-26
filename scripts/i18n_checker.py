#!/usr/bin/env python3
"""STEP 1 checker for B1 (language switch completeness). Reports each
hard-coded user-visible literal once, key mismatches, and per-screen sim."""
import re, sys
from pathlib import Path

ROOT = Path("/home/kushagra/Downloads/Vyavaahar")
UI_DIR = ROOT / "android/app/src/main/java/org/vyavahaar/app/ui"
EN_FILE = ROOT / "android/app/src/main/res/values/strings.xml"
HI_FILE = ROOT / "android/app/src/main/res/values-hi/strings.xml"
ALLOWLIST_FILE = ROOT / "scripts/i18n_allowlist.txt"

DEVANAGARI = re.compile(r"[\u0900-\u097F]")
LATIN = re.compile(r"[A-Za-z]")
TEMPLATE = re.compile(r"\$\{[^}]*\}")
CURRENCY_DIGITS = re.compile(r"[\d\u0966-\u096F%₹$.,:;()\-+/]+")

failures = []
seen = set()  # dedupe (file,line,literal)

def parse_strings(path):
    out = {}
    text = path.read_text(encoding="utf-8")
    for m in re.finditer(r'<string\s+name="([^"]+)"[^>]*>(.*?)</string>', text, re.S):
        val = m.group(2)
        val = val.replace("&amp;", "&").replace("&quot;", '"').replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">")
        out[m.group(1)] = val
    return out

def load_allowlist():
    if not ALLOWLIST_FILE.exists():
        return set()
    return {l.strip() for l in ALLOWLIST_FILE.read_text(encoding="utf-8").splitlines()
            if l.strip() and not l.startswith("#")}

allowlist = load_allowlist()

def report(check, detail, file, line):
    key = (str(file), line, detail)
    if key in seen:
        return
    seen.add(key)
    failures.append((check, detail, str(file), line))

def line_of(src, pos):
    return src[:pos].count("\n") + 1

text_literal = re.compile(r'Text\s*\(\s*(?:style\s*=\s*[^,]*,?\s*)?"([^"\\]{3,})"')

# --- (a) hard-coded literals (direct Text and direct text=) ---
hardcoded = []
for kt in sorted(UI_DIR.rglob("*.kt")):
    src = kt.read_text(encoding="utf-8")
    cleaned = re.sub(r'stringResource\s*\(', 'STRINGRES', src)
    cleaned = re.sub(r'//[^\n]*', '', cleaned)
    for m in text_literal.finditer(cleaned):
        lit = m.group(1)
        if lit.startswith("STRINGRES") or lit.strip() == "":
            continue
        report("a hard-coded string (bypasses translation)", lit, kt, line_of(src, m.start()))
        if DEVANAGARI.search(lit):
            report("b Devanagari literal in UI code", lit, kt, line_of(src, m.start()))

# theme files too
for kt in sorted((ROOT / "android/app/src/main/java/org/vyavahaar/app/theme").rglob("*.kt")):
    src = kt.read_text(encoding="utf-8")
    for m in text_literal.finditer(src):
        lit = m.group(1)
        if lit.strip() == "":
            continue
        if DEVANAGARI.search(lit):
            report("b Devanagari literal in theme code", lit, kt, line_of(src, m.start()))

# --- (c) key mismatch / empty ---
en = parse_strings(EN_FILE)
hi = parse_strings(HI_FILE)
for k in sorted(hi):
    if k not in en:
        report("c missing in English", k, HI_FILE, 0)

# --- (d) per-screen simulation ---
by_screen = {}
for file, line, lit in hardcoded:
    by_screen.setdefault(file, []).append((line, lit))

for file in sorted(by_screen):
    entries = by_screen[file]
    # English sim: any Devanagari literal => FAIL
    dev_lits = [lit for _, lit in entries if DEVANAGARI.search(lit)]
    if dev_lits:
        report("d shows Devanagari under English", "; ".join(dev_lits), file, entries[0][0])
    # Hindi sim: any non-allowlisted Latin word (after stripping templates) => FAIL
    bad_lits = []
    for line, lit in entries:
        stripped = TEMPLATE.sub("", lit)
        stripped = CURRENCY_DIGITS.sub("", stripped)
        words = LATIN.findall(stripped)
        bad = [w for w in set(words) if w not in allowlist]
        if bad:
            # report each
            report("d shows Latin under Hindi", lit + " (unallowlisted: " + ",".join(bad) + ")", file, line)

if not failures:
    print("PASS: no i18n issues found")
    sys.exit(0)

print("FAIL: i18n issues found:")
for check, detail, file, line in failures:
    loc = f"{file}:{line}"
    print(f"  [{check}] {loc} :: {detail}")
sys.exit(1)
