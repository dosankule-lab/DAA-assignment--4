import java.util.*;

public final class StudentTests {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        fixedCases();
        checkerCases();
        stateResetCases();
        randomCases();
        largeCases();
        System.out.println("TOTAL passed=" + passed + " failed=" + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void check(String name, boolean ok) {
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> run(String json) {
        Map<String, Object> request = (Map<String, Object>) Json.parse(json);
        Map<String, Object> raw;
        switch ((String) request.get("task")) {
            case "lcs": raw = Algorithms.lcs(request); break;
            case "merge": raw = Algorithms.merge(request); break;
            case "tree": raw = Algorithms.tree(request); break;
            case "naive": raw = Algorithms.naive(request); break;
            case "kmp": raw = Algorithms.kmp(request); break;
            case "rk": raw = Algorithms.rk(request); break;
            case "suffix": raw = Algorithms.suffix(request); break;
            default: raw = Algorithms.aho(request); break;
        }
        return (Map<String, Object>) Json.parse(Json.write(raw));
    }

    static String q(String s) {
        return Json.write(s);
    }

    static String arr(long[] a) {
        return Arrays.toString(a).replace(" ", "");
    }

    static String arr(int[] a) {
        return Arrays.toString(a).replace(" ", "");
    }

    static List<?> list(Object o) {
        return (List<?>) o;
    }

    static long num(Object o) {
        return ((Number) o).longValue();
    }

    static int[] ints(Object o) {
        List<?> l = list(o);
        int[] r = new int[l.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = (int) num(l.get(i));
        }
        return r;
    }

    static boolean isSubsequence(String s, String t) {
        int i = 0;
        for (int j = 0; j < t.length() && i < s.length(); j++) {
            if (s.charAt(i) == t.charAt(j)) {
                i++;
            }
        }
        return i == s.length();
    }

    static int bruteLcs(String a, String b) {
        int best = 0;
        for (int mask = 0; mask < (1 << a.length()); mask++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < a.length(); i++) {
                if ((mask >> i & 1) == 1) {
                    sb.append(a.charAt(i));
                }
            }
            if (sb.length() > best && isSubsequence(sb.toString(), b)) {
                best = sb.length();
            }
        }
        return best;
    }

    static boolean validLcs(String a, String b, Map<String, Object> out) {
        String s = (String) out.get("sequence");
        long length = num(out.get("length"));
        return length == bruteLcs(a, b) && s.length() == length && isSubsequence(s, a) && isSubsequence(s, b);
    }

    static long bruteMerge(List<Long> blocks) {
        if (blocks.size() <= 1) {
            return 0;
        }
        long best = Long.MAX_VALUE;
        for (int i = 0; i + 1 < blocks.size(); i++) {
            List<Long> next = new ArrayList<>(blocks);
            long sum = next.get(i) + next.get(i + 1);
            next.set(i, sum);
            next.remove(i + 1);
            best = Math.min(best, sum + bruteMerge(next));
        }
        return best;
    }

    static boolean validMerge(long[] sizes, Map<String, Object> out, long optimum) {
        long cost = num(out.get("cost"));
        List<?> plan = list(out.get("plan"));
        if (cost != optimum) {
            return false;
        }
        if (sizes.length <= 1) {
            return plan.isEmpty();
        }
        if (plan.size() != sizes.length - 1) {
            return false;
        }
        List<int[]> blocks = new ArrayList<>();
        for (int i = 0; i < sizes.length; i++) {
            blocks.add(new int[] {i, i});
        }
        long[] prefix = new long[sizes.length + 1];
        for (int i = 0; i < sizes.length; i++) {
            prefix[i + 1] = prefix[i] + sizes[i];
        }
        long total = 0;
        for (Object step : plan) {
            int[] s = ints(step);
            int at = -1;
            for (int i = 0; i + 1 < blocks.size(); i++) {
                if (blocks.get(i)[0] == s[0] && blocks.get(i)[1] == s[1]
                        && blocks.get(i + 1)[0] == s[1] + 1 && blocks.get(i + 1)[1] == s[2]) {
                    at = i;
                }
            }
            if (at < 0) {
                return false;
            }
            blocks.set(at, new int[] {s[0], s[2]});
            blocks.remove(at + 1);
            total += prefix[s[2] + 1] - prefix[s[0]];
        }
        return blocks.size() == 1 && total == cost;
    }

    static long bruteTree(int n, long[] w, int[][] edges) {
        long best = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            boolean ok = true;
            for (int[] e : edges) {
                if ((mask >> e[0] & 1) == 1 && (mask >> e[1] & 1) == 1) {
                    ok = false;
                }
            }
            if (ok) {
                long sum = 0;
                for (int i = 0; i < n; i++) {
                    if ((mask >> i & 1) == 1) {
                        sum += w[i];
                    }
                }
                best = Math.max(best, sum);
            }
        }
        return best;
    }

    static boolean validTree(int n, long[] w, int[][] edges, Map<String, Object> out, long optimum) {
        if (num(out.get("weight")) != optimum) {
            return false;
        }
        int[] sel = ints(out.get("selected"));
        boolean[] in = new boolean[n];
        long sum = 0;
        for (int v : sel) {
            if (v < 0 || v >= n || in[v]) {
                return false;
            }
            in[v] = true;
            sum += w[v];
        }
        for (int[] e : edges) {
            if (in[e[0]] && in[e[1]]) {
                return false;
            }
        }
        return sum == optimum;
    }

    static String treeRequest(int n, long[] w, int[][] edges) {
        StringBuilder sb = new StringBuilder("{\"task\":\"tree\",\"n\":" + n + ",\"weights\":" + arr(w) + ",\"edges\":[");
        for (int i = 0; i < edges.length; i++) {
            sb.append(i > 0 ? "," : "").append(arr(edges[i]));
        }
        return sb.append("]}").toString();
    }

    static List<Integer> oraclePositions(String text, String pattern) {
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i <= text.length() - pattern.length(); i++) {
            if (text.startsWith(pattern, i)) {
                r.add(i);
            }
        }
        return r;
    }

    static boolean positionsEqual(Map<String, Object> out, List<Integer> expected) {
        int[] got = ints(out.get("positions"));
        if (got.length != expected.size()) {
            return false;
        }
        for (int i = 0; i < got.length; i++) {
            if (got[i] != expected.get(i)) {
                return false;
            }
        }
        return true;
    }

    static boolean validSuffix(String text, Map<String, Object> out) {
        int n = text.length();
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (x, y) -> text.substring(x).compareTo(text.substring(y)));
        int[] sa = ints(out.get("sa"));
        int[] lcp = ints(out.get("lcp"));
        if (sa.length != n || lcp.length != n) {
            return false;
        }
        int maxLcp = 0;
        for (int i = 0; i < n; i++) {
            if (sa[i] != order[i]) {
                return false;
            }
            int h = 0;
            if (i > 0) {
                String a = text.substring(sa[i - 1]);
                String b = text.substring(sa[i]);
                while (h < a.length() && h < b.length() && a.charAt(h) == b.charAt(h)) {
                    h++;
                }
            }
            if (lcp[i] != h) {
                return false;
            }
            maxLcp = Math.max(maxLcp, h);
        }
        String rep = (String) out.get("repeated");
        if (rep.length() != maxLcp) {
            return false;
        }
        if (maxLcp == 0) {
            return true;
        }
        int first = text.indexOf(rep);
        return first >= 0 && text.indexOf(rep, first + 1) >= 0;
    }

    static long[][] oracleAho(String[] patterns, String[] texts) {
        long[][] r = new long[texts.length][patterns.length];
        for (int t = 0; t < texts.length; t++) {
            for (int p = 0; p < patterns.length; p++) {
                r[t][p] = oraclePositions(texts[t], patterns[p]).size();
            }
        }
        return r;
    }

    static String ahoRequest(String[] patterns, String[] texts) {
        StringBuilder sb = new StringBuilder("{\"task\":\"aho\",\"patterns\":[");
        for (int i = 0; i < patterns.length; i++) {
            sb.append(i > 0 ? "," : "").append(q(patterns[i]));
        }
        sb.append("],\"texts\":[");
        for (int i = 0; i < texts.length; i++) {
            sb.append(i > 0 ? "," : "").append(q(texts[i]));
        }
        return sb.append("]}").toString();
    }

    static boolean ahoEquals(Map<String, Object> out, long[][] expected) {
        List<?> rows = list(out.get("counts"));
        if (rows.size() != expected.length) {
            return false;
        }
        for (int t = 0; t < expected.length; t++) {
            List<?> row = list(rows.get(t));
            if (row.size() != expected[t].length) {
                return false;
            }
            for (int p = 0; p < expected[t].length; p++) {
                if (num(row.get(p)) != expected[t][p]) {
                    return false;
                }
            }
        }
        return true;
    }

    static void fixedCases() {
        Map<String, Object> r = run("{\"task\":\"lcs\",\"a\":\"tea_time\",\"b\":\"team\"}");
        check("lcs example", validLcs("tea_time", "team", r) && num(r.get("length")) == 4);
        r = run("{\"task\":\"lcs\",\"a\":\"\",\"b\":\"abc\"}");
        check("lcs one side empty", num(r.get("length")) == 0 && r.get("sequence").equals(""));
        r = run("{\"task\":\"lcs\",\"a\":\"\",\"b\":\"\"}");
        check("lcs both empty", num(r.get("length")) == 0 && r.get("sequence").equals(""));
        r = run("{\"task\":\"lcs\",\"a\":\"abcbdab\",\"b\":\"bdcaba\"}");
        check("lcs tied optima, length 4", validLcs("abcbdab", "bdcaba", r) && num(r.get("length")) == 4);
        r = run("{\"task\":\"lcs\",\"a\":\"AbC xY\",\"b\":\"abc xy\"}");
        check("lcs case and spaces are ordinary characters", validLcs("AbC xY", "abc xy", r) && num(r.get("length")) == 3);
        r = run("{\"task\":\"lcs\",\"a\":\"aaaa\",\"b\":\"aa\"}");
        check("lcs repeated characters", num(r.get("length")) == 2 && r.get("sequence").equals("aa"));

        r = run("{\"task\":\"merge\",\"sizes\":[2,3,4]}");
        check("merge example", validMerge(new long[] {2, 3, 4}, r, 14));
        r = run("{\"task\":\"merge\",\"sizes\":[]}");
        check("merge no blocks", num(r.get("cost")) == 0 && list(r.get("plan")).isEmpty());
        r = run("{\"task\":\"merge\",\"sizes\":[7]}");
        check("merge one block", num(r.get("cost")) == 0 && list(r.get("plan")).isEmpty());
        r = run("{\"task\":\"merge\",\"sizes\":[0,0,0,0]}");
        check("merge zeros", validMerge(new long[] {0, 0, 0, 0}, r, 0));
        r = run("{\"task\":\"merge\",\"sizes\":[1,100,100,1]}");
        check("merge adjacency: arbitrary smallest pair would be illegal", validMerge(new long[] {1, 100, 100, 1}, r, 404));
        long[] large = new long[200];
        Arrays.fill(large, 1_000_000_000L);
        r = run("{\"task\":\"merge\",\"sizes\":" + arr(large) + "}");
        check("merge n=200 of 1e9 beyond 32 bits", validMerge(large, r, bruteMergeBalanced(large)));

        r = run(treeRequest(3, new long[] {4, 9, 6}, new int[][] {{0, 1}, {1, 2}}));
        check("tree example", validTree(3, new long[] {4, 9, 6}, new int[][] {{0, 1}, {1, 2}}, r, 10));
        r = run("{\"task\":\"tree\",\"n\":0,\"weights\":[],\"edges\":[]}");
        check("tree n=0", num(r.get("weight")) == 0 && list(r.get("selected")).isEmpty());
        r = run("{\"task\":\"tree\",\"n\":1,\"weights\":[-5],\"edges\":[]}");
        check("tree single negative vertex", num(r.get("weight")) == 0 && list(r.get("selected")).isEmpty());
        r = run("{\"task\":\"tree\",\"n\":1,\"weights\":[5],\"edges\":[]}");
        check("tree single positive vertex", num(r.get("weight")) == 5);
        long[] negs = {-3, -1, -2};
        r = run(treeRequest(3, negs, new int[][] {{0, 1}, {1, 2}}));
        check("tree all negative gives empty set", validTree(3, negs, new int[][] {{0, 1}, {1, 2}}, r, 0));
        long[] star = {1, 10, 10, 10};
        int[][] starEdges = {{0, 1}, {0, 2}, {0, 3}};
        r = run(treeRequest(4, star, starEdges));
        check("tree star centre vs leaves", validTree(4, star, starEdges, r, 30));
        long[] ties = {5, 5, 5, 5};
        int[][] pathEdges = {{0, 1}, {1, 2}, {2, 3}};
        r = run(treeRequest(4, ties, pathEdges));
        check("tree ties", validTree(4, ties, pathEdges, r, 10));

        r = run("{\"task\":\"naive\",\"text\":\"aaaaa\",\"pattern\":\"aaa\"}");
        check("naive overlaps", positionsEqual(r, List.of(0, 1, 2)));
        for (String task : new String[] {"naive", "kmp", "rk"}) {
            r = run("{\"task\":\"" + task + "\",\"text\":\"abc\",\"pattern\":\"\"}");
            check(task + " empty pattern gives n+1 starts", positionsEqual(r, List.of(0, 1, 2, 3)));
            r = run("{\"task\":\"" + task + "\",\"text\":\"\",\"pattern\":\"\"}");
            check(task + " both empty gives [0]", positionsEqual(r, List.of(0)));
            r = run("{\"task\":\"" + task + "\",\"text\":\"\",\"pattern\":\"a\"}");
            check(task + " empty text", positionsEqual(r, List.of()));
            r = run("{\"task\":\"" + task + "\",\"text\":\"ab\",\"pattern\":\"abc\"}");
            check(task + " pattern longer than text", positionsEqual(r, List.of()));
            r = run("{\"task\":\"" + task + "\",\"text\":\"abababa\",\"pattern\":\"aba\"}");
            check(task + " overlapping starts 0,2,4", positionsEqual(r, List.of(0, 2, 4)));
            r = run("{\"task\":\"" + task + "\",\"text\":\"Abc, abc!\",\"pattern\":\"abc\"}");
            check(task + " case-sensitive with punctuation", positionsEqual(r, List.of(5)));
            r = run("{\"task\":\"" + task + "\",\"text\":\"aaaaa\",\"pattern\":\"aaaaa\"}");
            check(task + " pattern equals text", positionsEqual(r, List.of(0)));
            r = run("{\"task\":\"" + task + "\",\"text\":\"a b  a b\",\"pattern\":\" \"}");
            check(task + " space is ordinary character", positionsEqual(r, List.of(1, 3, 4, 6)));
        }
        r = run("{\"task\":\"rk\",\"text\":\"abdj\",\"pattern\":\"ab\"}");
        check("rk collision ab/dj: equal hash 75 but only ab reported", positionsEqual(r, List.of(0)));
        r = run("{\"task\":\"rk\",\"text\":\"djdjab\",\"pattern\":\"ab\"}");
        check("rk many collisions before the real match", positionsEqual(r, List.of(4)));
        r = run("{\"task\":\"rk\",\"text\":\"~~~~~~~~\",\"pattern\":\"~~~\"}");
        check("rk high codes and negative remainder normalisation", positionsEqual(r, List.of(0, 1, 2, 3, 4, 5)));

        r = run("{\"task\":\"suffix\",\"text\":\"banana\"}");
        check("suffix banana", validSuffix("banana", r) && r.get("repeated").equals("ana"));
        r = run("{\"task\":\"suffix\",\"text\":\"\"}");
        check("suffix empty", list(r.get("sa")).isEmpty() && list(r.get("lcp")).isEmpty() && r.get("repeated").equals(""));
        r = run("{\"task\":\"suffix\",\"text\":\"x\"}");
        check("suffix one char", Arrays.equals(ints(r.get("sa")), new int[] {0}) && Arrays.equals(ints(r.get("lcp")), new int[] {0}));
        r = run("{\"task\":\"suffix\",\"text\":\"aaaaa\"}");
        check("suffix all equal", validSuffix("aaaaa", r) && r.get("repeated").equals("aaaa"));
        r = run("{\"task\":\"suffix\",\"text\":\"abcdef\"}");
        check("suffix all distinct has no repeat", validSuffix("abcdef", r) && r.get("repeated").equals(""));
        r = run("{\"task\":\"suffix\",\"text\":\"abab ab\"}");
        check("suffix with space", validSuffix("abab ab", r));

        r = run("{\"task\":\"aho\",\"patterns\":[\"he\",\"she\",\"he\"],\"texts\":[\"sheshe\",\"sheshe\",\"\"]}");
        check("aho example", ahoEquals(r, new long[][] {{2, 2, 2}, {2, 2, 2}, {0, 0, 0}}));
        r = run("{\"task\":\"aho\",\"patterns\":[],\"texts\":[\"abc\",\"\"]}");
        check("aho empty dictionary gives empty rows", list(r.get("counts")).size() == 2 && list(list(r.get("counts")).get(0)).isEmpty());
        r = run("{\"task\":\"aho\",\"patterns\":[\"a\"],\"texts\":[]}");
        check("aho no texts gives []", list(r.get("counts")).isEmpty());
        String[] ushers = {"he", "she", "his", "hers"};
        r = run(ahoRequest(ushers, new String[] {"ushers"}));
        check("aho ushers: he, she, hers once, his zero", ahoEquals(r, new long[][] {{1, 1, 0, 1}}));
        String[] overlapPatterns = {"aa", "a", "aaa", "aa"};
        r = run(ahoRequest(overlapPatterns, new String[] {"aaaa", "a", ""}));
        check("aho overlaps, suffix patterns, duplicates", ahoEquals(r, oracleAho(overlapPatterns, new String[] {"aaaa", "a", ""})));
    }

    static long bruteMergeBalanced(long[] sizes) {
        int n = sizes.length;
        long[][] dp = new long[n][n];
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + sizes[i];
        }
        for (int len = 2; len <= n; len++) {
            for (int l = 0; l + len <= n; l++) {
                int r = l + len - 1;
                long best = Long.MAX_VALUE;
                for (int k = l; k < r; k++) {
                    best = Math.min(best, dp[l][k] + dp[k + 1][r]);
                }
                dp[l][r] = best + prefix[r + 1] - prefix[l];
            }
        }
        return dp[0][n - 1];
    }

    static void checkerCases() {
        Map<String, Object> good = new LinkedHashMap<>();
        good.put("length", 2L);
        good.put("sequence", "ab");
        Map<String, Object> notSub = new LinkedHashMap<>();
        notSub.put("length", 2L);
        notSub.put("sequence", "ba");
        Map<String, Object> shortOne = new LinkedHashMap<>();
        shortOne.put("length", 1L);
        shortOne.put("sequence", "a");
        check("lcs checker accepts valid witness", validLcs("axb", "ab", good));
        check("lcs checker rejects non-subsequence", !validLcs("axb", "ab", notSub));
        check("lcs checker rejects non-optimal length", !validLcs("axb", "ab", shortOne));

        Map<String, Object> altA = (Map<String, Object>) Json.parse("{\"cost\":5,\"plan\":[[0,0,1],[0,1,2]]}");
        Map<String, Object> altB = (Map<String, Object>) Json.parse("{\"cost\":5,\"plan\":[[1,1,2],[0,0,2]]}");
        Map<String, Object> badOrder = (Map<String, Object>) Json.parse("{\"cost\":5,\"plan\":[[0,1,2],[0,0,1]]}");
        Map<String, Object> wrongCost = (Map<String, Object>) Json.parse("{\"cost\":4,\"plan\":[[0,0,1],[0,1,2]]}");
        long[] s = {1, 1, 1};
        check("merge checker accepts plan A", validMerge(s, altA, 5));
        check("merge checker accepts plan B (other optimal split)", validMerge(s, altB, 5));
        check("merge checker rejects parent before child", !validMerge(s, badOrder, 5));
        check("merge checker rejects wrong cost", !validMerge(s, wrongCost, 5));

        long[] w = {4, 9, 6};
        int[][] e = {{0, 1}, {1, 2}};
        Map<String, Object> adj = (Map<String, Object>) Json.parse("{\"weight\":13,\"selected\":[0,1]}");
        Map<String, Object> dup = (Map<String, Object>) Json.parse("{\"weight\":8,\"selected\":[0,0]}");
        Map<String, Object> ok2 = (Map<String, Object>) Json.parse("{\"weight\":10,\"selected\":[2,0]}");
        check("tree checker accepts unordered valid set", validTree(3, w, e, ok2, 10));
        check("tree checker rejects adjacent vertices", !validTree(3, w, e, adj, 13));
        check("tree checker rejects duplicate vertex", !validTree(3, w, e, dup, 8));

        Map<String, Object> badSa = (Map<String, Object>) Json.parse("{\"sa\":[0,1,2],\"lcp\":[0,0,0],\"repeated\":\"\"}");
        Map<String, Object> badRep = (Map<String, Object>) Json.parse("{\"sa\":[5,3,1,0,4,2],\"lcp\":[0,1,3,0,0,2],\"repeated\":\"nan\"}");
        Map<String, Object> shortRep = (Map<String, Object>) Json.parse("{\"sa\":[5,3,1,0,4,2],\"lcp\":[0,1,3,0,0,2],\"repeated\":\"an\"}");
        Map<String, Object> goodSuffix = (Map<String, Object>) Json.parse("{\"sa\":[5,3,1,0,4,2],\"lcp\":[0,1,3,0,0,2],\"repeated\":\"ana\"}");
        check("suffix checker accepts correct answer", validSuffix("banana", goodSuffix));
        check("suffix checker rejects wrong order", !validSuffix("banana", badSa));
        check("suffix checker rejects non-repeated witness", !validSuffix("banana", badRep));
        check("suffix checker rejects non-longest repeat", !validSuffix("banana", shortRep));

        Map<String, Object> aho = (Map<String, Object>) Json.parse("{\"counts\":[[2,2,2],[0,0,0]]}");
        check("aho checker accepts exact rows", ahoEquals(aho, new long[][] {{2, 2, 2}, {0, 0, 0}}));
        check("aho checker rejects wrong count", !ahoEquals(aho, new long[][] {{2, 1, 2}, {0, 0, 0}}));
        check("aho checker rejects wrong row count", !ahoEquals(aho, new long[][] {{2, 2, 2}}));
    }

    static void stateResetCases() {
        String[] lines = {
            "{\"task\":\"lcs\",\"a\":\"tea_time\",\"b\":\"team\"}",
            "{\"task\":\"merge\",\"sizes\":[2,3,4]}",
            treeRequest(3, new long[] {4, 9, 6}, new int[][] {{0, 1}, {1, 2}}),
            "{\"task\":\"naive\",\"text\":\"aaaaa\",\"pattern\":\"aaa\"}",
            "{\"task\":\"kmp\",\"text\":\"abababa\",\"pattern\":\"aba\"}",
            "{\"task\":\"rk\",\"text\":\"abdj\",\"pattern\":\"ab\"}",
            "{\"task\":\"suffix\",\"text\":\"banana\"}",
            "{\"task\":\"aho\",\"patterns\":[\"he\",\"she\",\"he\"],\"texts\":[\"sheshe\",\"sheshe\",\"\"]}",
            "{\"task\":\"suffix\",\"text\":\"ab\"}",
            "{\"task\":\"aho\",\"patterns\":[\"a\"],\"texts\":[\"aaa\",\"b\"]}"
        };
        String[] first = new String[lines.length];
        for (int round = 0; round < 3; round++) {
            for (int i = 0; i < lines.length; i++) {
                String out = Json.write(run(lines[i]));
                if (round == 0) {
                    first[i] = out;
                } else {
                    check("state reset round " + round + " request " + i, out.equals(first[i]));
                }
            }
        }
    }

    static String randomString(Random rnd, int length, int alphabet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + rnd.nextInt(alphabet)));
        }
        return sb.toString();
    }

    static void randomCases() {
        Random rnd = new Random(20261029);
        for (int iter = 0; iter < 1500; iter++) {
            int alphabet = 1 + rnd.nextInt(3);
            String a = randomString(rnd, rnd.nextInt(9), alphabet);
            String b = randomString(rnd, rnd.nextInt(9), alphabet);
            check("random lcs #" + iter, validLcs(a, b, run("{\"task\":\"lcs\",\"a\":" + q(a) + ",\"b\":" + q(b) + "}")));

            int blocks = rnd.nextInt(7);
            long[] sizes = new long[blocks];
            List<Long> asList = new ArrayList<>();
            for (int i = 0; i < blocks; i++) {
                sizes[i] = rnd.nextInt(6);
                asList.add(sizes[i]);
            }
            check("random merge #" + iter, validMerge(sizes, run("{\"task\":\"merge\",\"sizes\":" + arr(sizes) + "}"), bruteMerge(asList)));

            int n = 1 + rnd.nextInt(10);
            long[] w = new long[n];
            for (int i = 0; i < n; i++) {
                w[i] = rnd.nextInt(21) - 8;
            }
            int[][] edges = new int[n - 1][];
            for (int v = 1; v < n; v++) {
                edges[v - 1] = rnd.nextBoolean() ? new int[] {rnd.nextInt(v), v} : new int[] {v, rnd.nextInt(v)};
            }
            check("random tree #" + iter, validTree(n, w, edges, run(treeRequest(n, w, edges)), bruteTree(n, w, edges)));

            String text = randomString(rnd, rnd.nextInt(30), alphabet);
            String pattern = randomString(rnd, rnd.nextInt(5), alphabet);
            List<Integer> expected = pattern.isEmpty() ? new ArrayList<>() : oraclePositions(text, pattern);
            if (pattern.isEmpty()) {
                for (int i = 0; i <= text.length(); i++) {
                    expected.add(i);
                }
            }
            for (String task : new String[] {"naive", "kmp", "rk"}) {
                check("random " + task + " #" + iter,
                        positionsEqual(run("{\"task\":\"" + task + "\",\"text\":" + q(text) + ",\"pattern\":" + q(pattern) + "}"), expected));
            }

            String st = randomString(rnd, rnd.nextInt(25), alphabet + 1);
            check("random suffix #" + iter, validSuffix(st, run("{\"task\":\"suffix\",\"text\":" + q(st) + "}")));

            int pc = rnd.nextInt(5);
            String[] patterns = new String[pc];
            for (int i = 0; i < pc; i++) {
                patterns[i] = randomString(rnd, 1 + rnd.nextInt(4), alphabet);
            }
            int tc = rnd.nextInt(4);
            String[] texts = new String[tc];
            for (int i = 0; i < tc; i++) {
                texts[i] = randomString(rnd, rnd.nextInt(15), alphabet);
            }
            check("random aho #" + iter, ahoEquals(run(ahoRequest(patterns, texts)), oracleAho(patterns, texts)));
        }
    }

    static void report(String name, long start) {
        System.out.printf("TIME %-45s %8.1f ms (parse, run, write, re-parse)%n", name, (System.nanoTime() - start) / 1e6);
    }

    static void largeCases() {
        Random rnd = new Random(11);
        long t = System.nanoTime();
        String a = randomString(rnd, 1000, 4);
        String b = randomString(rnd, 1000, 4);
        Map<String, Object> r = run("{\"task\":\"lcs\",\"a\":" + q(a) + ",\"b\":" + q(b) + "}");
        String seq = (String) r.get("sequence");
        check("large lcs 1000x1000 witness valid", seq.length() == num(r.get("length")) && isSubsequence(seq, a) && isSubsequence(seq, b));
        report("lcs 1000x1000", t);

        t = System.nanoTime();
        long[] sizes = new long[200];
        for (int i = 0; i < 200; i++) {
            sizes[i] = rnd.nextInt(1_000_000_001);
        }
        r = run("{\"task\":\"merge\",\"sizes\":" + arr(sizes) + "}");
        check("large merge n=200 cost matches independent dp and plan valid", validMerge(sizes, r, bruteMergeBalanced(sizes)));
        report("merge n=200", t);

        int n = 100000;
        long[] w = new long[n];
        int[][] chain = new int[n - 1][];
        for (int i = 0; i < n; i++) {
            w[i] = 1_000_000_000L;
        }
        for (int i = 0; i + 1 < n; i++) {
            chain[i] = new int[] {i, i + 1};
        }
        t = System.nanoTime();
        r = run(treeRequest(n, w, chain));
        check("deep chain tree n=100000: weight 50000e9", num(r.get("weight")) == 50_000L * 1_000_000_000L
                && validTree(n, w, chain, r, 50_000L * 1_000_000_000L));
        report("tree chain n=100000", t);

        int[][] random = new int[n - 1][];
        for (int v = 1; v < n; v++) {
            random[v - 1] = new int[] {rnd.nextInt(v), v};
            w[v] = rnd.nextInt(2_000_000_001) - 1_000_000_000;
        }
        t = System.nanoTime();
        r = run(treeRequest(n, w, random));
        long expected = num(r.get("weight"));
        check("large random tree: selected is independent and sums to weight", validTree(n, w, random, r, expected));
        int[][] flipped = new int[n - 1][];
        for (int i = 0; i < n - 1; i++) {
            flipped[i] = new int[] {random[i][1], random[i][0]};
        }
        Map<String, Object> r2 = run(treeRequest(n, w, flipped));
        check("tree optimum independent of edge direction (root choice)", num(r2.get("weight")) == expected);
        report("tree random n=100000", t);

        String text = randomString(rnd, 200000, 2);
        String pattern = randomString(rnd, 100000, 2);
        t = System.nanoTime();
        r = run("{\"task\":\"kmp\",\"text\":" + q(text) + ",\"pattern\":" + q(pattern) + "}");
        check("large kmp matches oracle", positionsEqual(r, oraclePositions(text, pattern)));
        String allA = "a".repeat(200000);
        String pa = "a".repeat(100000);
        r = run("{\"task\":\"kmp\",\"text\":" + q(allA) + ",\"pattern\":" + q(pa) + "}");
        check("kmp worst case all 'a' gives 100001 starts", list(r.get("positions")).size() == 100001);
        report("kmp 200000/100000", t);

        String rt = randomString(rnd, 10000, 2);
        String rp = randomString(rnd, 2000, 2);
        t = System.nanoTime();
        r = run("{\"task\":\"rk\",\"text\":" + q(rt) + ",\"pattern\":" + q(rp) + "}");
        check("large rk matches oracle", positionsEqual(r, oraclePositions(rt, rp)));
        r = run("{\"task\":\"rk\",\"text\":" + q("a".repeat(10000)) + ",\"pattern\":" + q("a".repeat(2000)) + "}");
        check("rk all equal text: 8001 starts", list(r.get("positions")).size() == 8001);
        report("rk 10000/2000", t);

        String nt = randomString(rnd, 3000, 2);
        String np = randomString(rnd, 3, 2);
        r = run("{\"task\":\"naive\",\"text\":" + q(nt) + ",\"pattern\":" + q(np) + "}");
        check("naive 3000 text matches oracle", positionsEqual(r, oraclePositions(nt, np)));

        for (String kind : new String[] {"random", "all-a", "periodic"}) {
            String st;
            if (kind.equals("random")) {
                st = randomString(rnd, 50000, 3);
            } else if (kind.equals("all-a")) {
                st = "a".repeat(50000);
            } else {
                st = "abc".repeat(16667).substring(0, 50000);
            }
            t = System.nanoTime();
            r = run("{\"task\":\"suffix\",\"text\":" + q(st) + "}");
            report("suffix 50000 " + kind, t);
            check("suffix 50000 " + kind + " permutation, order, lcp, repeated length", checkLargeSuffix(st, r));
        }

        List<String> patterns = new ArrayList<>();
        for (int i = 1; i <= 10000; i++) {
            patterns.add("a".repeat(Math.min(i, 13)));
        }
        String[] ps = patterns.toArray(new String[0]);
        String[] texts = new String[100];
        for (int i = 0; i < 100; i++) {
            texts[i] = "a".repeat(2000);
        }
        t = System.nanoTime();
        r = run(ahoRequest(ps, texts));
        List<?> rows = list(r.get("counts"));
        boolean ok = rows.size() == 100;
        List<?> row0 = list(rows.get(0));
        for (int i = 0; i < ps.length && ok; i++) {
            ok = num(row0.get(i)) == 2000 - ps[i].length() + 1;
        }
        check("aho dense: 10000 duplicate-heavy patterns, 100 texts of 'a'*2000", ok);
        report("aho dense 10000 patterns x 100 texts", t);

        String[] bigPatterns = new String[10000];
        for (int i = 0; i < bigPatterns.length; i++) {
            bigPatterns[i] = randomString(rnd, 10, 3);
        }
        String[] bigTexts = {randomString(rnd, 100000, 3), randomString(rnd, 100000, 3)};
        t = System.nanoTime();
        r = run(ahoRequest(bigPatterns, bigTexts));
        check("aho 10000 patterns of length 10, 2 texts of 100000: matches oracle on sample",
                ahoSampleMatches(r, bigPatterns, bigTexts));
        report("aho 10000x10 patterns, 200000 text chars", t);
    }

    static boolean ahoSampleMatches(Map<String, Object> out, String[] patterns, String[] texts) {
        List<?> rows = list(out.get("counts"));
        for (int t = 0; t < texts.length; t++) {
            List<?> row = list(rows.get(t));
            for (int p = 0; p < patterns.length; p += 97) {
                if (num(row.get(p)) != oraclePositions(texts[t], patterns[p]).size()) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean checkLargeSuffix(String text, Map<String, Object> out) {
        int n = text.length();
        int[] sa = ints(out.get("sa"));
        int[] lcp = ints(out.get("lcp"));
        if (sa.length != n || lcp.length != n || lcp[0] != 0) {
            return false;
        }
        boolean[] seen = new boolean[n];
        for (int v : sa) {
            if (v < 0 || v >= n || seen[v]) {
                return false;
            }
            seen[v] = true;
        }
        int best = 0;
        for (int i = 1; i < n; i++) {
            int x = sa[i - 1];
            int y = sa[i];
            int h = 0;
            while (x + h < n && y + h < n && text.charAt(x + h) == text.charAt(y + h)) {
                h++;
            }
            if (h != lcp[i]) {
                return false;
            }
            if (x + h < n && y + h < n) {
                if (text.charAt(x + h) > text.charAt(y + h)) {
                    return false;
                }
            } else if (x + h < n) {
                return false;
            }
            best = Math.max(best, h);
        }
        return ((String) out.get("repeated")).length() == best;
    }
}
