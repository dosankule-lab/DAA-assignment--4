# Student tests

Run all commands from the repository root.

| File | Purpose |
|---|---|
| `StudentTests.java` | Self-checking tests with independent oracles, checker accept/reject cases, state reset, random and full-limit inputs |
| `results.txt` | Actual output of `StudentTests` |
| `cases.jsonl` | Hand-written requests for the JSON Lines launcher |
| `cases_output.txt` | Actual launcher output for `cases.jsonl` |
| `SearchBench.java` | naive / KMP / exact Rabin–Karp benchmark |
| `benchmark_results.txt` | Actual benchmark output |

## Commands

```sh
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input tests/student/cases.jsonl

mkdir -p build_tests
javac --release 17 -encoding UTF-8 -d build_tests Java/source/Algorithms.java Java/source/Json.java tests/student/StudentTests.java tests/student/SearchBench.java
java -Xmx384m -cp build_tests StudentTests
java -Xmx384m -cp build_tests SearchBench
```

## Independent oracles

| Command | Oracle for small inputs | Witness check |
|---|---|---|
| lcs | enumeration of all subsequences of `a` tested against `b` | returned sequence is a subsequence of both and has the optimal length |
| merge | exhaustive search over all adjacent merge orders; an independent `O(n^3)` DP for 200 blocks (also compared with a separate Python script for the fixtures) | the plan is replayed: every step merges two existing adjacent blocks, costs are summed, one block remains |
| tree | enumeration of all vertex subsets | selected vertices are distinct, pairwise non-adjacent and sum to the weight |
| naive / kmp / rk | `String.startsWith` at every start; all three compared | exact list of starts |
| suffix | sorting of suffix strings; direct LCP by character comparison | `sa`, `lcp` equal the oracle; `repeated` has length `max(lcp)` and occurs at two distinct starts |
| aho | per-pattern direct counting | exact count matrix, including duplicates and empty rows |

The checkers are tested too: they accept alternative valid answers (other optimal merge plan, unordered tree set) and reject invalid ones (non-subsequence, parent before child, adjacent tree vertices, duplicate vertex, wrong order, non-repeated or too short repeat, wrong Aho count). 1,500 random iterations per command family and these large inputs are included: LCS 1000×1000, merge n=200, tree chain and random tree with n=100,000, KMP 200,000/100,000 and all-`a`, RK 10,000/2,000, suffix with 50,000 characters (random, all-equal, periodic), Aho with 10,000 patterns and 100 texts, and Aho with 200,000 text characters.
