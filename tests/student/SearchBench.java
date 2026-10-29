import java.util.*;

public final class SearchBench {
    private static final long SEED = 4242L;
    private static final int WARMUP_ROUNDS = 10;
    private static final int RUNS = 5;
    private static final int CALLS_PER_RUN = 20;

    public static void main(String[] args) {
        int[][] sizes = {{300, 10}, {1000, 30}, {3000, 100}};
        System.out.println("seed=" + SEED + " warmup_rounds=" + WARMUP_ROUNDS + " timed_runs=" + RUNS
                + " calls_per_run=" + CALLS_PER_RUN + " java=" + System.getProperty("java.version")
                + " cpus=" + Runtime.getRuntime().availableProcessors());
        System.out.println("| case | n | m | pattern | algorithm | run 1 | run 2 | run 3 | run 4 | run 5 | median | starts |");
        System.out.println("|---|---:|---:|---|---|---:|---:|---:|---:|---:|---:|---:|");
        String[] cases = {"random", "all-a", "periodic"};
        for (String name : cases) {
            for (int[] size : sizes) {
                int n = size[0];
                int m = size[1];
                Random rnd = new Random(SEED + n + name.hashCode());
                String text;
                String pattern;
                String description;
                if (name.equals("random")) {
                    text = randomText(rnd, n, 4);
                    int at = rnd.nextInt(n - m + 1);
                    pattern = text.substring(at, at + m);
                    description = "substring of random text over {a,b,c,d}";
                } else if (name.equals("all-a")) {
                    text = "a".repeat(n);
                    pattern = "a".repeat(m - 1) + "b";
                    description = "a^(m-1)b on text a^n (no match)";
                } else {
                    text = "ab".repeat(n / 2);
                    pattern = "ab".repeat(m / 2);
                    description = "(ab)^(m/2) on text (ab)^(n/2)";
                }
                Map<String, Object> request = new LinkedHashMap<>();
                request.put("text", text);
                request.put("pattern", pattern);

                Object expected = Algorithms.naive(request).get("positions");
                if (!expected.equals(Algorithms.kmp(request).get("positions"))
                        || !expected.equals(Algorithms.rk(request).get("positions"))) {
                    throw new IllegalStateException("outputs differ for " + name + " n=" + n);
                }
                int starts = ((List<?>) expected).size();
                report(name, n, m, description, "naive", request, 0, starts);
                report(name, n, m, description, "kmp", request, 1, starts);
                report(name, n, m, description, "rk", request, 2, starts);
            }
        }
    }

    private static String randomText(Random rnd, int n, int alphabet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append((char) ('a' + rnd.nextInt(alphabet)));
        }
        return sb.toString();
    }

    private static Object call(int which, Map<String, Object> request) {
        switch (which) {
            case 0: return Algorithms.naive(request);
            case 1: return Algorithms.kmp(request);
            default: return Algorithms.rk(request);
        }
    }

    private static void report(String name, int n, int m, String description, String algorithm,
                               Map<String, Object> request, int which, int starts) {
        Object sink = null;
        for (int i = 0; i < WARMUP_ROUNDS * CALLS_PER_RUN; i++) {
            sink = call(which, request);
        }
        double[] micros = new double[RUNS];
        for (int run = 0; run < RUNS; run++) {
            long t = System.nanoTime();
            for (int i = 0; i < CALLS_PER_RUN; i++) {
                sink = call(which, request);
            }
            micros[run] = (System.nanoTime() - t) / 1e3 / CALLS_PER_RUN;
        }
        if (sink == null) {
            throw new IllegalStateException();
        }
        double[] sorted = micros.clone();
        Arrays.sort(sorted);
        StringBuilder row = new StringBuilder();
        for (double v : micros) {
            row.append(String.format(" %.1f |", v));
        }
        System.out.printf("| %s | %d | %d | %s | %s |%s %.1f | %d |%n", name, n, m, description, algorithm,
                row, sorted[RUNS / 2], starts);
    }
}
