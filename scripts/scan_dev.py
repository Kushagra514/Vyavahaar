import re, pathlib
D = re.compile(r'[\u0900-\u097F]')
lit = re.compile(r'"([^"\\]{3,})"')
seen = set()
root = pathlib.Path('/home/kushagra/Downloads/Vyavaahar/android/app/src/main/java/org/vyavahaar/app/ui')
for p in sorted(root.rglob('*.kt')):
    s = p.read_text(encoding='utf-8')
    for m in lit.finditer(s):
        v = m.group(1)
        if D.search(v) and (str(p), v) not in seen:
            seen.add((str(p), v))
            line = s[:m.start()].count('\n') + 1
            print(f'{p}:{line} :: {v}')
print('TOTAL distinct Devanagari literals:', len(seen))
