# Assignment 4 — practical rubric

Edition 2.1.0. Practical 50 + defence 50; A4 retains its 20% contribution to the second attestation.

## Practical groups

|Command|Group|Maximum|
|---|---|---:|
|lcs|core|4|
|lcs|witness|2|
|lcs|scale|2|
|merge|core|3|
|merge|witness|2|
|merge|scale|2|
|tree|core|3|
|tree|witness|2|
|tree|scale|2|
|naive|core|2|
|kmp|core|3|
|rk|core|3|
|kmp|scale|2|
|suffix|core|4|
|suffix|witness|3|
|suffix|scale|2|
|aho|core|4|
|aho|edge|3|
|aho|scale|2|
|**Total (19 groups)**||**50**|

Each group earns maximum × passed checks / total checks, rounded half-up to 0.01. Sum the 19 rounded groups, capped at 50. core checks correct results/optimum values; witness checks valid optimal subsequences, merge plans, selected tree vertices, or the longest repeated substring; edge checks Aho boundary behaviour; scale checks correct results on large valid inputs. For suffix, core and scale require correct sa and lcp; witness additionally requires a valid optimal repeated substring. Thus correct repeated alone does not earn suffix witness credit when sa or lcp is wrong. Alternative optimal witnesses are accepted.

The original group weights are retained. More tests do not increase a group's maximum. Every response must first satisfy the complete output schema: required fields and their types, including witness fields. For lcs, merge and tree, a correctly typed but logically incorrect witness does not prevent credit for a correct optimum value in core/scale; it fails witness checks. Missing or wrong-typed witness fields fail schema validation even in core. Suffix follows the explicit sa+lcp prerequisite described above. Extra object fields are ignored consistently. A failed build cannot earn execution-check points; timeouts affect their failed checks. An internal checker/infrastructure error leaves grading pending for investigation and rerun, not an automatic student zero.

The required algorithms, English analysis, own tests and comparison are learning deliverables without separate manual bonus or discretionary deductions. Black-box checks do not prove the internal implementation or authorship. The defence assesses individual understanding. Test-data correctness, not a tiny timing advantage, determines scale results.

## Assessment and defence

Practical work is worth 50 points from independent automated requirement-group checks. The defence is a separate 50-point individual quiz. The required methods, analysis, own tests and benchmark are learning deliverables with no separate hidden manual points or discretionary score deductions. Result tests cannot prove the internal algorithm or authorship; the defence checks individual understanding. The practical group weights are listed above.

The English defence contains 15 questions in 25 minutes, one attempt: 10 single-answer questions with four options each and 5 multiple-response questions with five options each. Each question has a raw maximum of 1; the quiz grade is raw/15×50. A correct single answer earns 1; a wrong or blank answer earns 0. The questions apply the methods to short new cases: explaining a transition, checking a witness, finding a counterexample, diagnosing an incorrect implementation, or interpreting complexity.

For a multiple-response question with k correct options (k=2 or 3), every selected correct option earns 1/k and every selected wrong option deducts 1/(2k). Sum the selected weights and floor at 0. With two correct options: +0.50/−0.25. With three: +1/3/−1/6. Selecting all five options therefore earns 0.25 or approximately 0.67 respectively; full credit requires exactly all correct options. This partial-credit policy follows the latest A3 format and is a course assignment rule. It is not specified by the syllabus. No individual oral score recovery is included. A defective question/key or test is corrected consistently for all affected work under course rules.

Work independently during the quiz, without AI or external help. Follow the quiz instructions and accommodations approved by the course. There are three pages of five questions with free navigation. Dates, access and answer-release settings are announced in Moodle. Question keys are not part of the public assignment package.

## Public checks and the official grade

Public checks provide feedback and do not issue or transfer the official practical grade. They check complete results; the official grader separates the published groups above. Their pass/fail output is therefore not a prediction of partial credit. Compile/batch limits are listed in TESTING and the specification.
