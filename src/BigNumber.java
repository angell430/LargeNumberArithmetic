import java.util.Objects;

/**
 * Big integer implemented as a doubly linked list of base-10 digits.
 * Each node stores exactly one digit [0..9].
 */
public final class BigNumber {
    /**
     * Node in the doubly linked list; one decimal digit per node.
     */
    private static final class Node {
        /** The single-digit integer value [0..9] stored in this node. */
        int digit;
        /** Pointer to the previous digit node (closer to the most significant digit). */
        Node prev;
        /** Pointer to the next digit node (closer to the least significant digit). */
        Node next;

        /**
         * Constructs a new Node containing a single decimal digit.
         * * @param digit The numerical digit value [0..9].
         */
        Node(int digit) {
            this.digit = digit;
        }
    }

    /** Reference to the most significant digit (front of the doubly linked list). */
    private Node head; 
    /** Reference to the least significant digit (end of the doubly linked list). */
    private Node tail; 
    /** Flag indicating whether the number is negative. True for negative, false for non-negative. */
    private boolean negative; 

    /**
     * Default constructor. Initializes the BigNumber value to zero.
     */
    public BigNumber() {
        pushBackDigit(0);
    }

    /**
     * Parse signed integer text into digit nodes.
     * * @param s The string expression of the integer to parse.
     * @throws IllegalArgumentException if the string is null, empty, or contains invalid characters.
     */
    public BigNumber(String s) {
        if (s == null) throw new IllegalArgumentException("null number string");
        String t = s.replaceAll("\\s+", "");
        if (t.isEmpty()) throw new IllegalArgumentException("empty number string");

        int i = 0;
        char first = t.charAt(0);
        if (first == '+' || first == '-') {
            negative = (first == '-');
            i = 1;
        }
        if (i >= t.length()) throw new IllegalArgumentException("invalid number string");

        for (; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c < '0' || c > '9') throw new IllegalArgumentException("invalid character in number string");
            pushBackDigit(c - '0');
        }
        normalize();
    }

    /**
     * Deep-copy constructor. Creates an independent BigNumber instance with identical values.
     * * @param other The BigNumber instance to replicate.
     * @throws NullPointerException if the provided parameter is null.
     */
    public BigNumber(BigNumber other) {
        Objects.requireNonNull(other, "other");
        this.negative = other.negative;
        for (Node p = other.head; p != null; p = p.next) pushBackDigit(p.digit);
        normalize();
    }

    /**
     * Return |x| as a new BigNumber.
     * * @param x The BigNumber target.
     * @return A new BigNumber instance representing the absolute magnitude of x.
     */
    public static BigNumber abs(BigNumber x) {
        BigNumber r = new BigNumber(x);
        r.negative = false;
        r.normalize();
        return r;
    }

    /**
     * Read-only sign query.
     * * @return True if the number is negative; false otherwise.
     */
    public boolean isNegative() {
        return negative;
    }

    /**
     * Convert linked-list digits to canonical string form.
     * * @return The canonical string representation of the signed BigNumber.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (negative && !isZero()) sb.append('-');
        for (Node p = head; p != null; p = p.next) sb.append((char) ('0' + p.digit));
        return sb.toString();
    }

    // ---------------- DLL helpers ----------------

    /**
     * Reset to an empty internal list by clearing all structure references.
     */
    private void clear() {
        head = null;
        tail = null;
        negative = false;
    }

    /**
     * Append one digit at the least-significant side.
     * * @param digit The digit to append at the tail.
     */
    private void pushBackDigit(int digit) {
        Node n = new Node(digit);
        n.prev = tail;
        if (tail != null) tail.next = n;
        tail = n;
        if (head == null) head = n;
    }

    /**
     * Insert one digit at the most-significant side.
     * * @param digit The digit to insert at the head.
     */
    private void pushFrontDigit(int digit) {
        Node n = new Node(digit);
        n.next = head;
        if (head != null) head.prev = n;
        head = n;
        if (tail == null) tail = n;
    }

    /**
     * Remove the current most-significant digit. Handles empty list and single item updates.
     */
    private void popFrontDigit() {
        if (head == null) return;
        Node old = head;
        head = head.next;
        if (head != null) head.prev = null;
        else tail = null;
        // old will be GC'd
    }

    /**
     * Fast check for zero in normalized representation.
     * * @return True if the list contains exactly one single-digit node with the value 0.
     */
    private boolean isZero() {
        return head != null && head == tail && head.digit == 0;
    }

    /**
     * Enforce canonical representation:
     * - no leading zeros (except single zero)
     * - zero is always non-negative
     */
    private void normalize() {
        while (head != null && head.digit == 0 && head != tail) popFrontDigit();
        if (head == null) pushBackDigit(0);
        if (isZero()) negative = false;
    }

    /**
     * Compare magnitudes only (ignore signs): returns -1, 0, 1.
     * * @param a The first BigNumber operand.
     * @param b The second BigNumber operand.
     * @return -1 if |a| < |b|, 0 if |a| == |b|, and 1 if |a| > |b|.
     */
    private static int compareAbs(BigNumber a, BigNumber b) {
        int lenA = 0, lenB = 0;
        for (Node p = a.head; p != null; p = p.next) lenA++;
        for (Node p = b.head; p != null; p = p.next) lenB++;
        if (lenA != lenB) return Integer.compare(lenA, lenB);

        Node pa = a.head;
        Node pb = b.head;
        while (pa != null && pb != null) {
            if (pa.digit != pb.digit) return Integer.compare(pa.digit, pb.digit);
            pa = pa.next;
            pb = pb.next;
        }
        return 0;
    }

    /**
     * Build a BigNumber from a non-negative int helper value.
     * * @param x The non-negative primitive int value to convert.
     * @return A BigNumber instance holding the identical unsigned value.
     * @throws IllegalArgumentException if x is less than 0.
     */
    private static BigNumber fromUnsignedInt(int x) {
        if (x < 0) throw new IllegalArgumentException("x must be >= 0");
        BigNumber r = new BigNumber();
        r.clear();
        if (x == 0) {
            r.pushBackDigit(0);
            return r;
        }
        int v = x;
        while (v > 0) {
            r.pushFrontDigit(v % 10);
            v /= 10;
        }
        r.normalize();
        return r;
    }

    // ---------------- arithmetic on absolute values ----------------

    /**
     * Add magnitudes |a| + |b|.
     * * @param a The first BigNumber magnitude operand.
     * @param b The second BigNumber magnitude operand.
     * @return A new BigNumber instance representing the absolute addition value.
     */
    private static BigNumber addAbs(BigNumber a, BigNumber b) {
        BigNumber r = new BigNumber();
        r.clear();
        int carry = 0;
        Node pa = a.tail;
        Node pb = b.tail;
        while (pa != null || pb != null || carry != 0) {
            int da = (pa != null) ? pa.digit : 0;
            int db = (pb != null) ? pb.digit : 0;
            int s = da + db + carry;
            r.pushFrontDigit(s % 10);
            carry = s / 10;
            pa = (pa != null) ? pa.prev : null;
            pb = (pb != null) ? pb.prev : null;
        }
        r.normalize();
        return r;
    }

    /**
     * Subtract magnitudes |a| - |b|.
     * Precondition: |a| >= |b|.
     * * @param a The larger BigNumber magnitude operand.
     * @param b The smaller BigNumber magnitude operand.
     * @return A new BigNumber instance representing the absolute subtraction value.
     */
    private static BigNumber subAbs(BigNumber a, BigNumber b) {
        BigNumber r = new BigNumber();
        r.clear();
        int borrow = 0;
        Node pa = a.tail;
        Node pb = b.tail;
        while (pa != null) {
            int da = pa.digit - borrow;
            int db = (pb != null) ? pb.digit : 0;
            if (da < db) {
                da += 10;
                borrow = 1;
            } else {
                borrow = 0;
            }
            r.pushFrontDigit(da - db);
            pa = pa.prev;
            pb = (pb != null) ? pb.prev : null;
        }
        r.normalize();
        return r;
    }

    /**
     * Multiply magnitude |a| by a single decimal digit [0..9].
     * * @param a The BigNumber magnitude multiplicand.
     * @param digit The integer digit multiplier.
     * @return A new BigNumber instance containing the scaled value.
     */
    private static BigNumber mulAbsByDigit(BigNumber a, int digit) {
        BigNumber r = new BigNumber();
        r.clear();
        if (digit == 0 || a.isZero()) {
            r.pushBackDigit(0);
            return r;
        }
        int carry = 0;
        for (Node pa = a.tail; pa != null; pa = pa.prev) {
            int prod = pa.digit * digit + carry;
            r.pushFrontDigit(prod % 10);
            carry = prod / 10;
        }
        while (carry != 0) {
            r.pushFrontDigit(carry % 10);
            carry /= 10;
        }
        r.normalize();
        return r;
    }

    /**
     * Grade-school multiplication on magnitudes.
     * * @param a The first BigNumber magnitude factor.
     * @param b The second BigNumber magnitude factor.
     * @return A new BigNumber representing the product of absolute values.
     */
    private static BigNumber mulAbs(BigNumber a, BigNumber b) {
        BigNumber result = fromUnsignedInt(0);
        int shift = 0;
        for (Node pb = b.tail; pb != null; pb = pb.prev) {
            BigNumber part = mulAbsByDigit(a, pb.digit);
            if (!part.isZero()) {
                for (int i = 0; i < shift; i++) part.pushBackDigit(0);
                result = addAbs(result, part);
            }
            shift++;
        }
        result.normalize();
        return result;
    }

    // ---------------- public operators ----------------

    /**
     * Signed addition using sign cases and magnitude arithmetic.
     * * @param a The first addend BigNumber.
     * @param b The second addend BigNumber.
     * @return A new BigNumber corresponding to the final algebraic addition result.
     */
    public static BigNumber add(BigNumber a, BigNumber b) {
        BigNumber ra = new BigNumber(a);
        BigNumber rb = new BigNumber(b);

        if (ra.negative == rb.negative) {
            BigNumber r = addAbs(ra, rb);
            r.negative = ra.negative;
            r.normalize();
            return r;
        }

        int cmp = compareAbs(ra, rb);
        if (cmp == 0) return fromUnsignedInt(0);
        if (cmp > 0) {
            BigNumber r = subAbs(ra, rb);
            r.negative = ra.negative;
            r.normalize();
            return r;
        } else {
            BigNumber r = subAbs(rb, ra);
            r.negative = rb.negative;
            r.normalize();
            return r;
        }
    }

    /**
     * Signed subtraction as a + (-b).
     * * @param a The minuend BigNumber.
     * @param b The subtrahend BigNumber.
     * @return A new BigNumber matching the mathematical algebraic formula value.
     */
    public static BigNumber subtract(BigNumber a, BigNumber b) {
        BigNumber nb = new BigNumber(b);
        if (!nb.isZero()) nb.negative = !nb.negative;
        return add(a, nb);
    }

    /**
     * Signed multiplication: multiply magnitudes, then apply sign rule.
     * * @param a The multiplier factor BigNumber.
     * @param b The multiplicand factor BigNumber.
     * @return A new BigNumber containing the accurate signed product.
     */
    public static BigNumber multiply(BigNumber a, BigNumber b) {
        BigNumber aa = abs(a);
        BigNumber bb = abs(b);
        BigNumber r = mulAbs(aa, bb);
        r.negative = (a.negative != b.negative) && !r.isZero();
        r.normalize();
        return r;
    }

    // ---------------- division (decimal string) ----------------

    /**
     * Multiply current value by 10 by appending one zero digit.
     */
    private void mulBy10() {
        if (isZero()) return;
        pushBackDigit(0);
    }

    /**
     * Add one small digit [0..9] to current non-negative value.
     * * @param digit Single-digit int value [0..9] to accumulate into the current instance.
     */
    private void addSmallDigit(int digit) {
        BigNumber sum = addAbs(this, fromUnsignedInt(digit));
        this.head = sum.head;
        this.tail = sum.tail;
        this.negative = sum.negative;
        normalize();
    }

    /**
     * Integer division on magnitudes using long division.
     * Precondition: divisor != 0 and both inputs are non-negative.
     * * @param dividend The absolute dividend BigNumber.
     * @param divisor The absolute divisor BigNumber.
     * @param remainderOut An external container instance where the operation's remainder is assigned.
     * @return A new BigNumber tracking the scalar non-negative integer quotient.
     * @throws IllegalArgumentException if the divisor evaluated matches a mathematical zero.
     */
    private static BigNumber divAbsInteger(BigNumber dividend, BigNumber divisor, BigNumber remainderOut) {
        if (divisor.isZero()) throw new IllegalArgumentException("division by zero");

        BigNumber q = new BigNumber();
        q.clear();
        remainderOut.clear();
        remainderOut.pushBackDigit(0);

        // Build quotient one digit at a time from most significant side.
        for (Node pd = dividend.head; pd != null; pd = pd.next) {
            remainderOut.mulBy10();
            remainderOut.addSmallDigit(pd.digit);

            int digit = 0;
            if (compareAbs(remainderOut, divisor) >= 0) {
                // Pick the largest digit d in [0..9] such that divisor*d <= remainder.
                int lo = 0, hi = 9;
                while (lo <= hi) {
                    int mid = (lo + hi) / 2;
                    BigNumber prod = mulAbsByDigit(divisor, mid);
                    int c = compareAbs(prod, remainderOut);
                    if (c <= 0) {
                        digit = mid;
                        lo = mid + 1;
                    } else {
                        hi = mid - 1;
                    }
                }
                BigNumber sub = mulAbsByDigit(divisor, digit);
                BigNumber newRem = subAbs(remainderOut, sub);
                remainderOut.head = newRem.head;
                remainderOut.tail = newRem.tail;
                remainderOut.negative = false;
                remainderOut.normalize();
            }
            q.pushBackDigit(digit);
        }

        q.normalize();
        remainderOut.normalize();
        return q;
    }

    /**
     * Divide and return decimal string with up to {@code precision} fractional digits.
     * Trailing zeros in the fractional part are trimmed.
     * * @param dividend The input dividend scale value.
     * @param divisor The input divisor scale value.
     * @param precision Maximum allowable decimal fraction digits to evaluate.
     * @return A fully formatted decimal evaluation String.
     * @throws IllegalArgumentException if the divisor evaluated matches a zero.
     */
    public static String divideToDecimalString(BigNumber dividend, BigNumber divisor, int precision) {
        // Clamp negative precision to zero instead of failing.
        if (precision < 0) precision = 0;
        if (abs(divisor).isZero()) throw new IllegalArgumentException("division by zero");

        boolean neg = (dividend.negative != divisor.negative) && !abs(dividend).isZero();
        BigNumber a = abs(dividend);
        BigNumber b = abs(divisor);

        // First compute integer quotient and remainder.
        BigNumber rem = new BigNumber();
        BigNumber q = divAbsInteger(a, b, rem);

        String intPart = q.toString();
        StringBuilder frac = new StringBuilder();
        // Continue long division to generate fractional digits.
        for (int i = 0; i < precision && !rem.isZero(); i++) {
            rem.mulBy10();

            int digit = 0;
            if (compareAbs(rem, b) >= 0) {
                int lo = 0, hi = 9;
                while (lo <= hi) {
                    int mid = (lo + hi) / 2;
                    BigNumber prod = mulAbsByDigit(b, mid);
                    int c = compareAbs(prod, rem);
                    if (c <= 0) {
                        digit = mid;
                        lo = mid + 1;
                    } else {
                        hi = mid - 1;
                    }
                }
                BigNumber sub = mulAbsByDigit(b, digit);
                BigNumber newRem = subAbs(rem, sub);
                rem.head = newRem.head;
                rem.tail = newRem.tail;
                rem.negative = false;
                rem.normalize();
            }
            frac.append((char) ('0' + digit));
        }

        // Trim redundant trailing zeros in the decimal fraction.
        while (frac.length() > 0 && frac.charAt(frac.length() - 1) == '0') {
            frac.setLength(frac.length() - 1);
        }

        // Assemble final signed output.
        StringBuilder out = new StringBuilder();
        if (neg) out.append('-');
        out.append(intPart);
        if (frac.length() > 0) out.append('.').append(frac);
        return out.toString();
    }
}