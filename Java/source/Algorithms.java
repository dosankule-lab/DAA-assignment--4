import java.util.*;

public final class Algorithms {
    private static final int HASH_BASE = 31;
    private static final int HASH_MOD = 101;
    private static final int ALPHABET = 95;
    private static final int FIRST_CHAR = 32;

    private Algorithms() {
    }

    private static List<?> list(Object value) {
        return (List<?>) value;
    }

    private static long num(Object value) {
        return ((Number) value).longValue();
    }

    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a");
        String b = (String) input.get("b");
        int n = a.length();
        int m = b.length();
        int width = m + 1;
        int[] dp = new int[(n + 1) * width];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i * width + j] = dp[(i - 1) * width + j - 1] + 1;
                } else {
                    dp[i * width + j] = Math.max(dp[(i - 1) * width + j], dp[i * width + j - 1]);
                }
            }
        }
        int length = dp[n * width + m];
        char[] out = new char[length];
        int i = n;
        int j = m;
        int at = length;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                out[--at] = a.charAt(i - 1);
                i--;
                j--;
            } else if (dp[(i - 1) * width + j] >= dp[i * width + j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("length", length);
        result.put("sequence", new String(out));
        return result;
    }

    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> raw = list(input.get("sizes"));
        int n = raw.size();
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + num(raw.get(i));
        }
        List<int[]> plan = new ArrayList<>();
        long cost = 0;
        if (n > 1) {
            long[][] best = new long[n][n];
            int[][] split = new int[n][n];
            for (int length = 2; length <= n; length++) {
                for (int l = 0; l + length - 1 < n; l++) {
                    int r = l + length - 1;
                    long min = Long.MAX_VALUE;
                    int arg = l;
                    for (int k = l; k < r; k++) {
                        long value = best[l][k] + best[k + 1][r];
                        if (value < min) {
                            min = value;
                            arg = k;
                        }
                    }
                    best[l][r] = min + prefix[r + 1] - prefix[l];
                    split[l][r] = arg;
                }
            }
            cost = best[0][n - 1];

            int[] stack = new int[2 * n + 2];
            int[] stackState = new int[2 * n + 2];
            int top = 0;
            stack[top] = 0 * n + (n - 1);
            stackState[top++] = 0;
            while (top > 0) {
                int l = stack[top - 1] / n;
                int r = stack[top - 1] % n;
                if (l == r) {
                    top--;
                    continue;
                }
                int k = split[l][r];
                if (stackState[top - 1] == 0) {
                    stackState[top - 1] = 1;
                    stack[top] = l * n + k;
                    stackState[top++] = 0;
                    stack[top] = (k + 1) * n + r;
                    stackState[top++] = 0;
                } else {
                    plan.add(new int[] {l, k, r});
                    top--;
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cost", cost);
        result.put("plan", plan);
        return result;
    }

    static Map<String, Object> tree(Map<String, Object> input) {
        int n = (int) num(input.get("n"));
        List<?> weightList = list(input.get("weights"));
        List<?> edgeList = list(input.get("edges"));
        Map<String, Object> result = new LinkedHashMap<>();
        if (n == 0) {
            result.put("weight", 0L);
            result.put("selected", new int[0]);
            return result;
        }
        long[] weight = new long[n];
        for (int i = 0; i < n; i++) {
            weight[i] = num(weightList.get(i));
        }
        int m = edgeList.size();
        int[] eu = new int[m];
        int[] ev = new int[m];
        int[] start = new int[n + 1];
        for (int i = 0; i < m; i++) {
            List<?> e = list(edgeList.get(i));
            eu[i] = (int) num(e.get(0));
            ev[i] = (int) num(e.get(1));
            start[eu[i] + 1]++;
            start[ev[i] + 1]++;
        }
        for (int i = 0; i < n; i++) {
            start[i + 1] += start[i];
        }
        int[] adjacent = new int[2 * m];
        int[] fill = Arrays.copyOf(start, n);
        for (int i = 0; i < m; i++) {
            adjacent[fill[eu[i]]++] = ev[i];
            adjacent[fill[ev[i]]++] = eu[i];
        }

        int[] parent = new int[n];
        int[] order = new int[n];
        Arrays.fill(parent, -2);
        int head = 0;
        int tail = 0;
        order[tail++] = 0;
        parent[0] = -1;
        while (head < tail) {
            int u = order[head++];
            for (int k = start[u]; k < start[u + 1]; k++) {
                int v = adjacent[k];
                if (parent[v] == -2) {
                    parent[v] = u;
                    order[tail++] = v;
                }
            }
        }

        long[] take = new long[n];
        long[] skip = new long[n];
        for (int i = 0; i < n; i++) {
            take[i] = weight[i];
        }
        for (int i = tail - 1; i >= 0; i--) {
            int v = order[i];
            int p = parent[v];
            if (p >= 0) {
                take[p] += skip[v];
                skip[p] += Math.max(take[v], skip[v]);
            }
        }

        boolean[] chosen = new boolean[n];
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < tail; i++) {
            int v = order[i];
            int p = parent[v];
            boolean parentTaken = p >= 0 && chosen[p];
            chosen[v] = !parentTaken && take[v] > skip[v];
            if (chosen[v]) {
                selected.add(v);
            }
        }
        result.put("weight", Math.max(take[0], skip[0]));
        result.put("selected", selected);
        return result;
    }

    private static Map<String, Object> positions(List<Integer> found) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("positions", found);
        return result;
    }

    private static List<Integer> allBoundaries(int n) {
        List<Integer> found = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            found.add(i);
        }
        return found;
    }

    static Map<String, Object> naive(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();
        if (m == 0) {
            return positions(allBoundaries(n));
        }
        List<Integer> found = new ArrayList<>();
        for (int i = 0; i + m <= n; i++) {
            int j = 0;
            while (j < m && text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }
            if (j == m) {
                found.add(i);
            }
        }
        return positions(found);
    }

    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();
        if (m == 0) {
            return positions(allBoundaries(n));
        }
        int[] pi = new int[m];
        for (int i = 1, k = 0; i < m; i++) {
            while (k > 0 && pattern.charAt(i) != pattern.charAt(k)) {
                k = pi[k - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(k)) {
                k++;
            }
            pi[i] = k;
        }
        List<Integer> found = new ArrayList<>();
        for (int i = 0, k = 0; i < n; i++) {
            while (k > 0 && (k == m || text.charAt(i) != pattern.charAt(k))) {
                k = pi[k - 1];
            }
            if (text.charAt(i) == pattern.charAt(k)) {
                k++;
            }
            if (k == m) {
                found.add(i - m + 1);
            }
        }
        return positions(found);
    }

    static Map<String, Object> rk(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();
        if (m == 0) {
            return positions(allBoundaries(n));
        }
        List<Integer> found = new ArrayList<>();
        if (m > n) {
            return positions(found);
        }
        long highest = 1;
        for (int i = 1; i < m; i++) {
            highest = highest * HASH_BASE % HASH_MOD;
        }
        long target = 0;
        long window = 0;
        for (int i = 0; i < m; i++) {
            target = (target * HASH_BASE + pattern.charAt(i)) % HASH_MOD;
            window = (window * HASH_BASE + text.charAt(i)) % HASH_MOD;
        }
        for (int i = 0; ; i++) {
            if (window == target) {
                int j = 0;
                while (j < m && text.charAt(i + j) == pattern.charAt(j)) {
                    j++;
                }
                if (j == m) {
                    found.add(i);
                }
            }
            if (i + m >= n) {
                break;
            }
            window = (window - text.charAt(i) % HASH_MOD * highest % HASH_MOD + HASH_MOD) % HASH_MOD;
            window = (window * HASH_BASE + text.charAt(i + m)) % HASH_MOD;
        }
        return positions(found);
    }

    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = (String) input.get("text");
        int n = text.length();
        Map<String, Object> result = new LinkedHashMap<>();
        if (n == 0) {
            result.put("sa", new int[0]);
            result.put("lcp", new int[0]);
            result.put("repeated", "");
            return result;
        }
        int[] sa = buildSuffixArray(text);
        int[] lcp = kasai(text, sa);
        int best = 0;
        int at = 0;
        for (int i = 1; i < n; i++) {
            if (lcp[i] > best) {
                best = lcp[i];
                at = sa[i];
            }
        }
        result.put("sa", sa);
        result.put("lcp", lcp);
        result.put("repeated", best == 0 ? "" : text.substring(at, at + best));
        return result;
    }

    private static int[] buildSuffixArray(String text) {
        int n = text.length();
        int size = Math.max(n, 128) + 1;
        int[] sa = new int[n];
        int[] rank = new int[n];
        int[] next = new int[n];
        int[] byFirst = new int[n];
        int[] count = new int[size];
        for (int i = 0; i < n; i++) {
            rank[i] = text.charAt(i);
            count[rank[i] + 1]++;
        }
        for (int i = 1; i < size; i++) {
            count[i] += count[i - 1];
        }
        for (int i = 0; i < n; i++) {
            sa[count[rank[i]]++] = i;
        }
        for (int k = 1; k < n; k <<= 1) {
            int at = 0;
            for (int i = n - k; i < n; i++) {
                byFirst[at++] = i;
            }
            for (int i = 0; i < n; i++) {
                if (sa[i] >= k) {
                    byFirst[at++] = sa[i] - k;
                }
            }
            Arrays.fill(count, 0);
            for (int i = 0; i < n; i++) {
                count[rank[i] + 1]++;
            }
            for (int i = 1; i < size; i++) {
                count[i] += count[i - 1];
            }
            for (int i = 0; i < n; i++) {
                int p = byFirst[i];
                sa[count[rank[p]]++] = p;
            }
            next[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                int p = sa[i - 1];
                int q = sa[i];
                int pSecond = p + k < n ? rank[p + k] : -1;
                int qSecond = q + k < n ? rank[q + k] : -1;
                boolean same = rank[p] == rank[q] && pSecond == qSecond;
                next[q] = next[p] + (same ? 0 : 1);
            }
            int[] swap = rank;
            rank = next;
            next = swap;
            if (rank[sa[n - 1]] == n - 1) {
                break;
            }
        }
        return sa;
    }

    private static int[] kasai(String text, int[] sa) {
        int n = text.length();
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }
        int[] lcp = new int[n];
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) {
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) {
                    h--;
                }
            } else {
                h = 0;
            }
        }
        return lcp;
    }

    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> patterns = list(input.get("patterns"));
        List<?> texts = list(input.get("texts"));
        int p = patterns.size();
        int total = 1;
        for (Object o : patterns) {
            total += ((String) o).length();
        }
        int[] go = new int[total * ALPHABET];
        Arrays.fill(go, -1);
        int states = 1;
        int[] terminal = new int[p];
        for (int i = 0; i < p; i++) {
            String s = (String) patterns.get(i);
            int v = 0;
            for (int j = 0; j < s.length(); j++) {
                int c = s.charAt(j) - FIRST_CHAR;
                if (go[v * ALPHABET + c] == -1) {
                    go[v * ALPHABET + c] = states++;
                }
                v = go[v * ALPHABET + c];
            }
            terminal[i] = v;
        }

        int[] fail = new int[states];
        int[] order = new int[states];
        int head = 0;
        int tail = 0;
        for (int c = 0; c < ALPHABET; c++) {
            int v = go[c];
            if (v == -1) {
                go[c] = 0;
            } else {
                fail[v] = 0;
                order[tail++] = v;
            }
        }
        while (head < tail) {
            int u = order[head++];
            for (int c = 0; c < ALPHABET; c++) {
                int v = go[u * ALPHABET + c];
                if (v == -1) {
                    go[u * ALPHABET + c] = go[fail[u] * ALPHABET + c];
                } else {
                    fail[v] = go[fail[u] * ALPHABET + c];
                    order[tail++] = v;
                }
            }
        }

        List<long[]> rows = new ArrayList<>();
        long[] visits = new long[states];
        for (Object o : texts) {
            String text = (String) o;
            Arrays.fill(visits, 0);
            int v = 0;
            for (int i = 0; i < text.length(); i++) {
                v = go[v * ALPHABET + text.charAt(i) - FIRST_CHAR];
                visits[v]++;
            }
            for (int i = tail - 1; i >= 0; i--) {
                int u = order[i];
                visits[fail[u]] += visits[u];
            }
            long[] row = new long[p];
            for (int i = 0; i < p; i++) {
                row[i] = visits[terminal[i]];
            }
            rows.add(row);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("counts", rows);
        return result;
    }
}
