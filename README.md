# Assignment 4 — Dynamic Programming and String Processing

[Interactive lesson and practice](https://rzhaniya.github.io/daa-assignment-4-archive/) · [English specification](docs/Assignment_4_EN.md) · [Русская версия](README_RU.md)

**Course:** Design and Analysis of Algorithms. **Edition:** 2.1.0. **Practical:** 50 points. **Defence:** 50 points. **Deadline:** 8 November 2026, 23:59; Moodle records approved extensions.

Implement six parts with eight commands: LCS; ordered adjacent-block merging; maximum-weight independent set on a tree; naive, KMP and exact Rabin–Karp search; suffix array with Kasai LCP; Aho–Corasick. This is a starter template: the algorithm methods are TODOs. Read the specification before coding.

## Create your repository

1. Select **Use this template → Create a new repository**. Create your own repository, then clone it to your computer or open it in your development environment. Work in your copy.
2. Set `language` in `submission.json` to `Java`, `Python`, `Cpp` or `CSharp`. Install Python 3.12+ for the launcher and the selected runtime: JDK 17+, Python 3.12+, C++17 compiler, or .NET 8 SDK.
3. From the repository root, run `python tools/run.py --setup`. This checks compilation and startup, not the algorithms.
4. Implement the TODO methods in your chosen language's `source` directory. Preserve the UTF-8 JSON Lines interface. Add your own tests under `tests/student` and complete [ANALYSIS.md](ANALYSIS.md) in English.
5. Run `python tools/run.py --check`. It checks 44 public cases and a repeated mixed batch. Also test the full published domains, boundary cases and alternative optimal witnesses.
6. Commit and push your implementation. In **Actions → Public checks**, check the run for that commit. The official template checks startup in all four languages; your copy also runs the algorithm checks. The template's green setup run is not evidence that your TODOs are complete.

## Submit in Moodle

Submit the **exact commit URL with its full 40-character SHA**, plus the chosen language, in [Assignment 4 → Online text](https://lms.astanait.edu.kz/mod/assign/view.php?id=95272). An Actions run URL for the same commit is optional. Give the teacher access if your repository is private. Click **Submit assignment** and verify the final submission status. If you revise before the deadline, update the commit link. Do not upload a ZIP as your submission.

The submitted commit must include selected source files, `submission.json`, your tests, completed English `ANALYSIS.md` and actual build/run/test commands. Include correctness, reconstruction, complexity and a reproducible comparison of naive/KMP/exact RK. Acknowledge sources and AI assistance, their contribution and your own verification.

## Learn and prepare

Use the [bilingual lesson](https://rzhaniya.github.io/daa-assignment-4-archive/), eight small interactive models, [Week 8 lecture](https://rzhaniya.github.io/daa-assignment-4-archive/week8/), and the [ungraded 15-question / 25-minute practice](https://rzhaniya.github.io/daa-assignment-4-archive/practice.html). The public learning questions are separate from the assessed Moodle questions. Site source is in `docs/` and is served through GitHub Pages.

The [individual English defence](https://lms.astanait.edu.kz/mod/quiz/view.php?id=95271) has **15 questions in 25 minutes**, one attempt: 10 single-answer and 5 multiple-response, three pages of five, free navigation. Read the partial-credit rules in Moodle. All six assignment parts are covered.

## Assessment and contracts

Practical work: 19 marking groups, 50 points. Defence: 50 points. A4 is out of 100 and contributes 20% of the second attestation; Quiz II is separate. Public checks provide feedback; they do not calculate or transfer the official grade. The official grader evaluates the submitted commit.

[Contracts](CONTRACTS.md) · [Rubric](RUBRIC.md) · [Testing](TESTING.md) · [Submission checklist](SUBMISSION.md) · [Lecture clarifications](docs/LECTURE_CLARIFICATIONS_EN_RU.md)
