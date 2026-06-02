import java.util.Objects;

/**
 * Big integer implemented as a doubly linked list of base-10 digits.
 * Each node stores exactly one digit [0..9].
 */
public final class BigNumber {
    private static final class Node {
        int digit;
        Node prev;
        Node next;

        Node(int digit) {
            this.digit = digit;
        }
    }

    private Node head; // most significant digit
    private Node tail; // least significant digit
    private boolean negative;

    public BigNumber() {
        pushBackDigit(0);
    }

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

    public BigNumber(BigNumber other) {
        Objects.requireNonNull(other, "other");
        this.negative = other.negative;
        for (Node p = other.head; p != null; p = p.next) pushBackDigit(p.digit);
        normalize();
    }

    public static BigNumber abs(BigNumber x) {
        BigNumber r = new BigNumber(x);
        r.negative = false;
        r.normalize();
        return r;
    }

    public boolean isNegative() {
        return negative;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (negative && !isZero()) sb.append('-');
        for (Node p = head; p != null; p = p.next) sb.append((char) ('0' + p.digit));
        return sb.toString();
    }

    // ---------------- DLL helpers ----------------

    private void clear() {
        head = null;
        tail = null;
        negative = false;
    }

    private void pushBackDigit(int digit) {
        Node n = new Node(digit);
        n.prev = tail;
        if (tail != null) tail.next = n;
        tail = n;
        if (head == null) head = n;
    }

    private void pushFrontDigit(int digit) {
        Node n = new Node(digit);
        n.next = head;
        if (head != null) head.prev = n;
        head = n;
        if (tail == null) tail = n;
    }

    private void popFrontDigit() {
        if (head == null) return;
        Node old = head;
        head = head.next;
        if (head != null) head.prev = null;
        else tail = null;
        // old will be GC'd
    }

    private boolean isZero() {
        return head != null && head == tail && head.digit == 0;
    }

    private void normalize() {
        while (head != null && head.digit == 0 && head != tail) popFrontDigit();
        if (head == null) pushBackDigit(0);
        if (isZero()) negative = false;
    }

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

    // precondition: |a| >= |b|
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

    public static BigNumber subtract(BigNumber a, BigNumber b) {
        BigNumber nb = new BigNumber(b);
        if (!nb.isZero()) nb.negative = !nb.negative;
        return add(a, nb);
    }

    public static BigNumber multiply(BigNumber a, BigNumber b) {
        BigNumber aa = abs(a);
        BigNumber bb = abs(b);
        BigNumber r = mulAbs(aa, bb);
        r.negative = (a.negative != b.negative) && !r.isZero();
        r.normalize();
        return r;
    }

    // ---------------- division (decimal string) ----------------

    private void mulBy10() {
        if (isZero()) return;
        pushBackDigit(0);
    }

    private void addSmallDigit(int digit) {
        BigNumber sum = addAbs(this, fromUnsignedInt(digit));
        this.head = sum.head;
        this.tail = sum.tail;
        this.negative = sum.negative;
        normalize();
    }

    // precondition: divisor != 0, both non-negative
    private static BigNumber divAbsInteger(BigNumber dividend, BigNumber divisor, BigNumber remainderOut) {
        if (divisor.isZero()) throw new IllegalArgumentException("division by zero");

        BigNumber q = new BigNumber();
        q.clear();
        remainderOut.clear();
        remainderOut.pushBackDigit(0);

        for (Node pd = dividend.head; pd != null; pd = pd.next) {
            remainderOut.mulBy10();
            remainderOut.addSmallDigit(pd.digit);

            int digit = 0;
            if (compareAbs(remainderOut, divisor) >= 0) {
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
     */
    public static String divideToDecimalString(BigNumber dividend, BigNumber divisor, int precision) {
        if (precision < 0) precision = 0;
        if (abs(divisor).isZero()) throw new IllegalArgumentException("division by zero");

        boolean neg = (dividend.negative != divisor.negative) && !abs(dividend).isZero();
        BigNumber a = abs(dividend);
        BigNumber b = abs(divisor);

        BigNumber rem = new BigNumber();
        BigNumber q = divAbsInteger(a, b, rem);

        String intPart = q.toString();
        StringBuilder frac = new StringBuilder();
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

        while (frac.length() > 0 && frac.charAt(frac.length() - 1) == '0') {
            frac.setLength(frac.length() - 1);
        }

        StringBuilder out = new StringBuilder();
        if (neg) out.append('-');
        out.append(intPart);
        if (frac.length() > 0) out.append('.').append(frac);
        return out.toString();
    }
}

