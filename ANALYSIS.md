# Assignment 4 — Analysis

Student: Dossankul Yerassyl, group SE-2515. Language: Java (`submission.json`: `{"language":"Java"}`). All algorithms are in `Java/source/Algorithms.java`; the supplied `Main.java` and `Json.java` are unchanged.

## Environment and commands

- Runtime: OpenJDK 21.0.12 (sources compiled with `--release 17`, as the launcher does). Hardware used for all reported measurements: Intel Xeon @ 2.30 GHz, 2 vCPUs, 8 GB RAM, Linux 6.18.
- The assessed commit is recorded in Moodle.

```sh
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input tests/student/cases.jsonl

mkdir -p build_tests
javac --release 17 -encoding UTF-8 -d build_tests Java/source/Algorithms.java Java/source/Json.java tests/student/StudentTests.java tests/student/SearchBench.java
java -Xmx384m -cp build_tests StudentTests
java -Xmx384m -cp build_tests SearchBench
```

Results obtained: `--setup` passes; `--check` passes all 44 public cases and the repeated batch; `StudentTests` reports `passed=12110 failed=0` (`tests/student/results.txt`).

## Algorithm explanations

Input strings are read as Java `String`s, numbers as `long`. Every method allocates its own arrays and containers inside the call and uses no static mutable state, so consecutive requests of different tasks cannot influence each other. `StudentTests.stateResetCases` runs ten mixed requests three times in one process and requires identical outputs; the public check does the same for its batch.

### lcs

- **State:** `dp[i][j]` is the length of an LCS of the first `i` characters of `a` and the first `j` characters of `b`. Compare `a[i-1]` with `b[j-1]`.
- **Base cases:** `dp[0][j] = dp[i][0] = 0`; this also covers empty inputs.
- **Transition:** if `a[i-1] == b[j-1]` then `dp[i][j] = dp[i-1][j-1] + 1`, otherwise `max(dp[i-1][j], dp[i][j-1])`.
- **Order:** increasing `i`, then increasing `j`; the three needed neighbours are already computed.
- **Correctness:** in the equal case some optimal solution matches the last characters (an exchange argument), so the last character is taken. In the mismatch case the last characters cannot both be in a common subsequence as its final pair, so one of them is dropped, and the better drop is the maximum.
- **Reconstruction:** walk back from `(n, m)`. On equal characters emit the character and go diagonally; otherwise move towards the larger neighbour (up on ties). The emitted characters, reversed, form an LCS; ties give one of several equally long answers.
- **Complexity:** `O((n+1)(m+1))` time and memory (one flat `int[]`); output `O(L)`.
- **Boundary test:** `tea_time`/`team` (the final `m` is separated, so a substring-only recurrence would fail) and `"" / "abc"` (empty base row).

### merge

- **State:** `best[l][r]` is the minimum total cost of merging blocks `l..r` into one block while keeping original order; a merge pays the combined size of the merged interval.
- **Base cases:** `best[i][i] = 0`; for `n <= 1` the result is cost 0 and an empty plan.
- **Transition:** `best[l][r] = min over k in [l, r-1] of best[l][k] + best[k+1][r] + (prefix[r+1] - prefix[l])`; `split[l][r]` stores the best `k` (the last merge joins `[l,k]` and `[k+1,r]`).
- **Order:** by increasing interval length, so both sub-intervals are ready.
- **Correctness:** the last merge of any plan joins two adjacent intervals covering `[l, r]`, and the two sides are independent subproblems, so the optimum is the minimum over the split point. Taking two arbitrary smallest blocks would violate adjacency (`[1,100,100,1]` is the test: the optimum is 404).
- **Reconstruction:** an explicit stack does a post-order walk over the split tree: children `[l,k]` and `[k+1,r]` are processed, then the parent `[l,k,r]` is appended. Hence every merge appears after the merges that create its two inputs, and the plan has exactly `n-1` entries.
- **Complexity:** `O(n^3)` time, `O(n^2)` memory; costs are `long` (up to about `200·200·10^9`).
- **Boundary test:** 200 blocks of 10^9 give totals beyond 32 bits; zeros give cost 0 with a valid plan.

### tree

- **State:** `take[v]` is the best weight in the subtree of `v` if `v` is selected, `skip[v]` if it is not.
- **Transition:** `take[v] = w[v] + sum skip[c]`, `skip[v] = sum max(take[c], skip[c])` over children `c`. Leaves: `take = w`, `skip = 0`.
- **Order:** a BFS from vertex 0 records an order and a parent array; processing that order backwards visits every vertex after all its children (post-order effect) with no recursion, so a chain of 100,000 vertices works.
- **Correctness:** the choice for `v` only constrains its children, so subtrees are independent given the status of `v`. The empty set is always allowed (`skip` is never negative), which is why all-negative inputs give weight 0. The optimum does not depend on the root (checked by reversing edge directions).
- **Reconstruction:** go forward in BFS order; the root is selected if `take > skip`; a child is selected only if its parent is not selected and `take[c] > skip[c]`. Strict `>` leaves zero-weight vertices out; any optimum is accepted.
- **Complexity:** `O(n+1)` time and memory (CSR adjacency); weights are `long`.
- **Boundary test:** `n=0`, a single negative vertex, a star whose centre is worse than its leaves, and the 100,000-vertex chain with weight `50000·10^9`.

### naive

- **Idea:** try every start `i = 0..n-m` and compare `pattern[j]` for `j = 0..m-1`. The last valid start is `n-m`, so no window runs past the text.
- **Boundaries:** `m = 0` returns `0..n` (n+1 boundaries) before any indexing; `m > n` returns an empty list. Overlaps are kept because the next start is simply `i+1`.
- **Complexity:** `O(n·m)` worst case, `O(1)` auxiliary memory, `O(z)` output.
- **Boundary test:** `aaaaa`/`aaa` gives `[0,1,2]`; an empty pattern on an empty text gives `[0]`.

### kmp

- **Prefix function:** `pi[i]` is the length of the longest proper prefix of `pattern[0..i]` that is also its suffix. It is built by the standard fallback loop `k = pi[k-1]` on mismatch.
- **Search:** `k` is the number of pattern characters currently matched. On a mismatch `k` falls back to `pi[k-1]`; after a full match (`k == m`) the next iteration falls back to `pi[m-1]` before comparing, which preserves overlapping matches. A start is reported as `i-m+1`.
- **Correctness:** the invariant is that `pattern[0..k)` is the longest pattern prefix that is a suffix of the text read so far; the fallback chain enumerates all shorter borders, so no occurrence is skipped.
- **Complexity:** each text step increases `k` by at most 1 and falls back at most as often as it increased: `O(n + m + z)` time, `O(m)` auxiliary memory. `m = 0` returns all boundaries before the prefix function is built.
- **Boundary test:** `abababa`/`aba` gives `[0,2,4]` (a version that restarts at 0 after a match would miss start 2); the all-`a` text of 200,000 characters with a 100,000-character pattern gives 100,001 starts.

### rk

- **Hash:** polynomial with base 31, modulus 101, ASCII codes, the leftmost character having the highest power. `highest = 31^(m-1) mod 101`.
- **Rolling update:** `h' = ((h - text[i]·highest) mod 101 + 101) mod 101`, then `h' = (h'·31 + text[i+m]) mod 101`. The `+ 101` normalises a negative remainder.
- **Verification:** equal hashes are only candidates; the actual characters are compared and a start is reported only if all `m` match. `ab` and `dj` both hash to 75, so `abdj`/`ab` gives `[0]` (the collision test), and `djdjab`/`ab` has two collision windows before the real match.
- **Complexity:** `O(n + m + c·m)` where `c` is the number of equal-hash windows; with modulus 101 many windows collide, and a periodic text makes every window a true match, so the worst case is `O(n·m)`. No linear worst case is claimed. Memory `O(1)` plus output.
- **Boundaries:** `m = 0` and `m > n` are handled before a window is formed.

### suffix

- **Prefix doubling:** after round `k`, `rank[i]` is the class of the first `2k` characters of suffix `i` (the first round starts from the character codes). A round sorts by the pair `(rank[i], rank[i+k])` where a missing second part counts as smaller than everything (`-1`).
- **Radix step:** suffixes are first ordered by the second key (those with `i+k >= n` first, then `sa[j]-k` in current order), then stably counting-sorted by the first key. New ranks are assigned by comparing neighbouring pairs. The loop stops when all ranks are distinct. This is `O(n log n)` (`log n` rounds, each `O(n + alphabet)`).
- **No sentinel:** shorter suffixes sort first through the `-1` second key, so the output contains exactly the `n` nonempty suffixes.
- **Kasai:** with `rank` the inverse of `sa`, process suffixes in text order and keep `h`; when moving from suffix `i` to `i+1` the common prefix with the predecessor is at least `h-1`, so `h` is decreased by one and then only extended. Total work `O(n)`. `lcp[0] = 0`.
- **Repeated substring:** the longest substring that occurs at two distinct starts is the longest common prefix of some pair of suffixes, and the maximum over all pairs is attained by an adjacent pair in sorted order, so it equals `max(lcp)`; the answer is `text[sa[i]..sa[i]+max)`, or `""` if the maximum is 0.
- **Complexity:** `O(n log n)` time, `O(n)` memory; no suffix strings are materialised.
- **Boundary test:** empty text, one character, `aaaaa` (all `lcp` values 1..4, repeat `aaaa` overlapping itself), `abcdef` (no repeat), and 50,000 characters of all-equal, periodic and random text.

### aho

- **Automaton:** a trie over the 95-character alphabet (codes 32..126); each pattern ends in a terminal state, and duplicate patterns share the state but keep their own dictionary position. A BFS computes failure links and fills the missing transitions, producing a complete goto table (`95·S` ints).
- **Counting:** for each text, a fresh `visits[]` array is filled by walking the automaton once and incrementing the state after every character. Then, in reverse BFS order, `visits[fail[u]] += visits[u]`. A state's final value is the number of text positions at which the string of that state ends as a suffix of the text prefix — this includes patterns that are suffixes of longer matched patterns (`he` inside `she`) and overlapping matches.
- **Output:** `row[i] = visits[terminal[i]]`, so duplicates get equal counts and columns follow the dictionary order. An empty dictionary gives an empty row per text; an empty text list gives `[]`. The array is reset for every text.
- **Complexity:** construction `O(L + 95·S + p)`, each text `O(|text| + S + p)`; the propagation is independent of the number of matches, so dense inputs (10,000 patterns of the form `a^k`, 100 texts of 2000 `a`) take about 140 ms.
- **Boundary test:** `ushers` with `he, she, his, hers` (the lecture example with `she`/`he` ending at the same character), `aa, a, aaa, aa` on `aaaa` (overlaps, suffix patterns and a duplicate), empty dictionary and empty texts.

## Own tests

`tests/student/StudentTests.java` contains independent oracles: exhaustive subsequence enumeration for LCS, brute-force merge-order search, subset enumeration for the tree, `String.startsWith` for searches, sorting of suffix strings with direct LCP computation for `suffix`, and direct counting for Aho–Corasick. Non-unique answers (LCS sequence, merge plan, tree set, repeat) are checked semantically, and the checkers are themselves tested to accept alternatives and reject invalid witnesses. 1,500 random iterations cover all commands; large cases are timed. Full list and commands: `tests/student/README.md`. Selected rows (full logs in `tests/student/results.txt` and `tests/student/cases_output.txt`):

|Command|Input or fixture path|Expected result|Actual result / log|What it checks|
|---|---|---|---|---|
|lcs|`tea_time` / `team`|length 4, `team`|`{"length":4,"sequence":"team"}`|subsequence, not substring|
|lcs|`""` / `abc`|length 0, `""`|`{"length":0,"sequence":""}`|empty base case|
|lcs|`AbC xY` / `abc xy`|length 3 (`b`, space, `x`)|passes (`StudentTests`)|case sensitivity, spaces as characters|
|merge|`[1,100,100,1]`|cost 404 (independent Python oracle)|`{"cost":404,"plan":[[2,2,3],[0,0,1],[0,1,3]]}`|adjacency; children before parent|
|merge|`[1,1,1]`|cost 5, two optimal plans|`{"cost":5,"plan":[[1,1,2],[0,0,2]]}`|alternative optimal plan accepted|
|merge|200 × 10^9|`bruteMergeBalanced` oracle|passes|totals beyond 32 bits|
|tree|weights `[-3,-1,-2]`, path|weight 0, `[]`|`{"weight":0,"selected":[]}`|empty set allowed|
|tree|star `[1,10,10,10]`|weight 30|`{"weight":30,"selected":[1,2,3]}`|centre vs leaves|
|tree|chain of 100,000, weight 10^9|weight 50000·10^9|passes|no recursion failure, large sum|
|naive/kmp/rk|`abc` / `""`|`[0,1,2,3]`|`{"positions":[0,1,2,3]}`|empty pattern gives n+1 boundaries|
|kmp|`abababa` / `aba`|`[0,2,4]`|passes|fallback after full match|
|rk|`abdj` / `ab`|`[0]`|`{"positions":[0]}`|hash 75 collision is verified away|
|rk|`djdjab` / `ab`|`[4]`|`{"positions":[4]}`|several collision windows|
|suffix|`banana`|sa `[5,3,1,0,4,2]`, lcp `[0,1,3,0,0,2]`, `ana`|identical|order, Kasai, overlapping repeat|
|suffix|`aaaaa`|sa `[4,3,2,1,0]`, `aaaa`|identical|all-equal text|
|suffix|`abcdef`|repeat `""`|identical|no repeat|
|aho|`he,she,he` on `sheshe`, `sheshe`, `""`|`[[2,2,2],[2,2,2],[0,0,0]]`|identical|suffix patterns, duplicates, independent texts|
|aho|`aa,a,aaa,aa` on `aaaa`,`a`,`""`|`[[3,4,2,3],[0,1,0,0],[0,0,0,0]]`|identical|overlaps and duplicates|
|aho|no patterns on `abc`, `""`|`[[],[]]`|`{"counts":[[],[]]}`|empty dictionary keeps one row per text|
|state reset|ten mixed requests ×3 in one process|identical outputs|12,110 checks in total pass|no stale state|

## Search comparison

- **Inputs:** the same `text` and `pattern` objects were given to naive, KMP and RK. Three increasing sizes within the common domain: `(n, m) = (300, 10), (1000, 30), (3000, 100)`. Three cases: **random** (text over `{a,b,c,d}`, pattern is a random substring of it), **all-a** (text `a^n`, pattern `a^(m-1)b`, no match — worst case for naive, every window a prefix match), **periodic** (text `(ab)^(n/2)`, pattern `(ab)^(m/2)` — many true matches). Generator: `java.util.Random(4242 + n + name.hashCode())` in `tests/student/SearchBench.java`.
- **Correctness first:** before timing, the benchmark aborts unless the three methods return identical position lists for every case (the number of starts is shown in the tables below).
- **Timing scope (identical for all methods):** one call of `Algorithms.naive/kmp/rk(request)` on an already parsed request; it includes building the prefix function or hash values and the result map, and excludes compilation, request generation, JSON parsing and output. Each run is 20 consecutive calls; the reported value is the mean time per call in microseconds. Warm-up: 200 untimed calls per method and case before the 5 timed runs. Timer: `System.nanoTime()`. Command: `java -Xmx384m -cp build_tests SearchBench`. Hardware as above, Java 21.
- **Unit:** microseconds per call.

|Case|n|m|Seed/generator|Algorithm|Run 1|Run 2|Run 3|Run 4|Run 5|Median|Correct outputs checked|
|---|---:|---:|---|---|---:|---:|---:|---:|---:|---:|---|
|random|300|10|seed 4242+n+hash("random"); substring of random text over {a,b,c,d}|naive|12.7|8.6|5.3|4.8|4.9|5.3|yes, 1 start(s), identical for all three|
|random|300|10|seed 4242+n+hash("random"); substring of random text over {a,b,c,d}|kmp|18.4|80.0|14.3|13.9|13.6|14.3|yes, 1 start(s), identical for all three|
|random|300|10|seed 4242+n+hash("random"); substring of random text over {a,b,c,d}|rk|18.2|22.3|12.3|7.5|6.8|12.3|yes, 1 start(s), identical for all three|
|random|1000|30|seed 4242+n+hash("random"); substring of random text over {a,b,c,d}|naive|22.2|20.9|24.0|29.1|23.7|23.7|yes, 1 start(s), identical for all three|
|random|1000|30|seed 4242+n+hash("random"); substring of random text over {a,b,c,d}|kmp|15.1|14.4|14.1|14.3|14.8|14.4|yes, 1 start(s), identical for all three|
|random|1000|30|seed 4242+n+hash("random"); substring of random text over {a,b,c,d}|rk|18.9|19.6|18.4|20.8|19.7|19.6|yes, 1 start(s), identical for all three|
|random|3000|100|seed 4242+n+hash("random"); substring of random text over {a,b,c,d}|naive|46.1|46.7|46.7|46.4|46.1|46.4|yes, 1 start(s), identical for all three|
|random|3000|100|seed 4242+n+hash("random"); substring of random text over {a,b,c,d}|kmp|43.4|44.2|44.4|44.9|44.0|44.2|yes, 1 start(s), identical for all three|
|random|3000|100|seed 4242+n+hash("random"); substring of random text over {a,b,c,d}|rk|50.2|48.6|48.9|48.8|49.6|48.9|yes, 1 start(s), identical for all three|
|all-a|300|10|seed 4242+n+hash("all-a"); a^(m-1)b on text a^n (no match)|naive|32.0|31.2|30.8|31.7|31.1|31.2|yes, 0 start(s), identical for all three|
|all-a|300|10|seed 4242+n+hash("all-a"); a^(m-1)b on text a^n (no match)|kmp|8.8|8.9|9.8|10.0|9.2|9.2|yes, 0 start(s), identical for all three|
|all-a|300|10|seed 4242+n+hash("all-a"); a^(m-1)b on text a^n (no match)|rk|4.7|4.9|5.0|5.6|5.0|5.0|yes, 0 start(s), identical for all three|
|all-a|1000|30|seed 4242+n+hash("all-a"); a^(m-1)b on text a^n (no match)|naive|92.9|47.5|59.6|54.9|60.5|59.6|yes, 0 start(s), identical for all three|
|all-a|1000|30|seed 4242+n+hash("all-a"); a^(m-1)b on text a^n (no match)|kmp|36.0|42.3|38.0|34.3|33.9|36.0|yes, 0 start(s), identical for all three|
|all-a|1000|30|seed 4242+n+hash("all-a"); a^(m-1)b on text a^n (no match)|rk|27.9|24.3|26.2|24.2|25.4|25.4|yes, 0 start(s), identical for all three|
|all-a|3000|100|seed 4242+n+hash("all-a"); a^(m-1)b on text a^n (no match)|naive|410.6|383.6|384.2|386.7|380.5|384.2|yes, 0 start(s), identical for all three|
|all-a|3000|100|seed 4242+n+hash("all-a"); a^(m-1)b on text a^n (no match)|kmp|12.1|11.8|12.4|11.8|11.6|11.8|yes, 0 start(s), identical for all three|
|all-a|3000|100|seed 4242+n+hash("all-a"); a^(m-1)b on text a^n (no match)|rk|56.4|57.7|56.8|56.3|57.4|56.8|yes, 0 start(s), identical for all three|
|periodic|300|10|seed 4242+n+hash("periodic"); (ab)^(m/2) on text (ab)^(n/2)|naive|3.7|3.6|3.6|4.3|3.5|3.6|yes, 146 start(s), identical for all three|
|periodic|300|10|seed 4242+n+hash("periodic"); (ab)^(m/2) on text (ab)^(n/2)|kmp|2.6|2.6|4.6|2.5|2.5|2.6|yes, 146 start(s), identical for all three|
|periodic|300|10|seed 4242+n+hash("periodic"); (ab)^(m/2) on text (ab)^(n/2)|rk|25.9|28.8|258.8|22.5|24.8|25.9|yes, 146 start(s), identical for all three|
|periodic|1000|30|seed 4242+n+hash("periodic"); (ab)^(m/2) on text (ab)^(n/2)|naive|25.0|21.2|22.1|21.8|21.5|21.8|yes, 486 start(s), identical for all three|
|periodic|1000|30|seed 4242+n+hash("periodic"); (ab)^(m/2) on text (ab)^(n/2)|kmp|16.5|9.5|9.0|9.0|8.6|9.0|yes, 486 start(s), identical for all three|
|periodic|1000|30|seed 4242+n+hash("periodic"); (ab)^(m/2) on text (ab)^(n/2)|rk|140.4|69.8|47.0|45.4|106.3|69.8|yes, 486 start(s), identical for all three|
|periodic|3000|100|seed 4242+n+hash("periodic"); (ab)^(m/2) on text (ab)^(n/2)|naive|201.3|202.7|200.0|200.9|207.7|201.3|yes, 1451 start(s), identical for all three|
|periodic|3000|100|seed 4242+n+hash("periodic"); (ab)^(m/2) on text (ab)^(n/2)|kmp|29.4|27.9|26.5|28.2|31.7|28.2|yes, 1451 start(s), identical for all three|
|periodic|3000|100|seed 4242+n+hash("periodic"); (ab)^(m/2) on text (ab)^(n/2)|rk|327.7|329.8|330.8|327.6|343.4|329.8|yes, 1451 start(s), identical for all three|

**Observations.** Times of a few microseconds are noisy and include residual JIT effects (for example the 258.8 µs outlier in the first periodic row for RK and the 80.0 µs outlier for KMP on the smallest random case), so medians are used. On the all-`a` case the naive search was clearly slowest and grew fastest with `n·m` (31 µs, 60 µs, 384 µs), while KMP stayed small (9, 36 and 12 µs — the middle value is noisy, and all three are far from the naive cost at the largest size). On the random case the three methods are within a small factor of each other, because most naive comparisons fail at the first or second character (naive was even fastest at `n=300`). On the periodic case RK was the slowest (330 µs at `n=3000`, m=100) because every even-offset window is a true match, so each of the `z` matches costs a full `m`-character verification on top of the per-character hash arithmetic with `%` operations; naive pays the same comparisons without the hashing (201 µs), while KMP never rescans (28 µs). Rabin–Karp was fastest on all-`a` at the smallest sizes since the hash rarely matches the pattern ending in `b` and the verification is skipped.

**Limitations and interpretation.** There is one generated input per size and case, only five runs, and a 2-vCPU shared machine; the benchmark measures this Java implementation including `ArrayList` boxing of positions, not an abstract algorithm. The medians do not prove asymptotic bounds: they only illustrate that naive and RK degrade with `n·m`-type work on repetitive inputs while KMP does not. The modulus 101 is deliberately small so that collisions are testable (`ab`/`dj` are a correctness test); it makes false candidates frequent and must not be generalised to production hashes, which use large moduli or randomised bases so that collisions are rare, although exact RK still has a candidate-sensitive worst case.

## Sources and AI assistance

- Algorithms follow the course lectures for weeks 8–10 and `docs/LECTURE_CLARIFICATIONS_EN_RU.md` (sentinel, `she`/`he` positions, naive loop bounds); the textbook reference is Cormen, Leiserson, Rivest & Stein, *Introduction to Algorithms* (dynamic programming, string matching, suffix-array practice).
- AI assistance: Claude (Anthropic) generated the first version of `Algorithms.java`, the tests, the benchmark and this document. The code was compiled and run in the Claude environment (setup, public checks, `StudentTests`, `SearchBench`), and all numbers above come from those runs. The student is responsible for reviewing, understanding and re-running everything, and must be able to explain every part at the defence.
