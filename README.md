# Large Number Arithmetic Using Doubly Linked List

This project implements arithmetic on **very large integers** using a **doubly linked list** (DLL) where **each node stores one digit**. This avoids limitations of built-in numeric types for extremely long values.

## Assignment requirements (from the PDF)

- Store the large integer as **individual digits in a doubly linked list**
- Support:
  - **addition**
  - **subtraction**
  - **multiplication**
  - **division** (output may include a fractional part, e.g. `27.5`)
- Perform arithmetic **digit-by-digit** with correct **carry/borrow**

This repository fulfills those requirements in **Java** with a `BigNumber` type backed by a DLL.

## Data structure design

### `BigNumber` (digit DLL)

Implemented in `src/BigNumber.java`.

- **Node**
  - `digit`: an integer in `[0..9]`
  - `prev`, `next`: pointers for doubly linked traversal
- **Head / Tail**
  - `head` points to the **most significant digit**
  - `tail` points to the **least significant digit**
- **Sign**
  - `negative` stores the sign separately
  - The number `0` is always stored as a single node `0` and is never negative

### Why doubly linked list?

- Addition/subtraction need to process from **least significant digit** → **most significant digit**, so the `tail` pointer + `prev` links make that traversal \(O(n)\) without reversing.
- Division and parsing process from **most significant digit** → **least significant digit**, so `head` + `next` links also work efficiently.

## Algorithms implemented

### Addition

`BigNumber.add(a, b)`:

- If both numbers have the same sign: add absolute values digit-by-digit from `tail_` with carry.
- If signs differ: reduce to subtraction of absolute values.

### Subtraction

`BigNumber.subtract(a, b)`:

- Implemented as `a + (-b)` with correct sign handling.
- Absolute subtraction (`subAbs`) assumes \(|a| \ge |b|\) and performs digit-by-digit subtraction from `tail_` with borrow.

### Multiplication

`BigNumber.multiply(a, b)` uses grade-school multiplication:

- For each digit of the multiplier (from `tail_`), compute a partial product \(a \times digit\)
- Append zeros to shift (base-10 place shift)
- Sum partial products using DLL addition

### Division (decimal output)

`BigNumber.divideToDecimalString(m, n, precision)`:

- Computes the **integer quotient** using long division:
  - iterate digits from `head_` of the dividend
  - keep a running remainder (also a `BigNumber`)
  - choose each quotient digit `0..9` by binary search and subtraction
- Computes the **fractional part** by repeating:
  - remainder \(\times 10\)
  - next digit `0..9`
  - subtract
- Outputs up to `precision` digits after the decimal point (default used by `main.cpp`: **20**)
- Trailing zeros in the fractional part are trimmed (`27.5000` becomes `27.5`)

## Project files

- `src/BigNumber.java`: big integer implementation (DLL digits)
- `src/Main.java`: simple CLI that reads `m` and `n`, prints all 4 operations

## Build & run (Windows / PowerShell)

From the repository root:

```bash
javac -d out src/*.java
java -cp out Main
```

### If `javac` is not found

You currently have `java` (JRE), but you also need the **JDK** (which includes `javac`) to compile.

- Install a JDK (Java 8+ is fine; Java 17 also works)
- Reopen PowerShell and confirm:

```bash
javac -version
```

### If you have Java 8 installed (common) and JDK 17 installed

If your `java -version` shows **1.8** but your `javac` is from **17**, compile targeting Java 8 so it runs:

```bash
javac --release 8 -d out src/*.java
java -cp out Main
```

## Input format

The program accepts either of these formats:

- With variable label:
  - `m = 123456`
  - `n = 789`
- Or raw number lines:
  - `123456`
  - `789`

Numbers may optionally have `+` or `-` sign and may be arbitrarily long.

## Output format

The program prints:

- `addition = ...`
- `subtraction = ...`
- `multiplication = ...`
- `division = ...` (decimal string)

## Example

Input:

```text
m = 55
n = 2
```

Output:

```text
addition = 57
subtraction = 53
multiplication = 110
division = 27.5
```

## Notes / limitations

- Division prints up to **20** fractional digits by default. You can change this in `src/Main.java` by adjusting the `precision` argument.
- This is base-10 digit storage (one digit per DLL node), matching the assignment statement.

