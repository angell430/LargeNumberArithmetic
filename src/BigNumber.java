/**
 * BigNumber represents arbitrary-precision integers using a doubly linked list.
 * Each node in the list stores a single decimal digit (0-9).
 * The list is ordered from most significant digit (head) to least significant digit (tail).
 */
public final class BigNumber {
    /**
     * Node represents a single digit in the linked list structure.
     */
    private static final class Node {
        int digit;      // Stores a single decimal digit [0-9]
        Node prev;      // Reference to previous node (more significant digit)
        Node next;      // Reference to next node (less significant digit)

        /**
         * Constructs a node with the given digit value.
         * @param digit A decimal digit value [0-9]
         */
        Node(int digit) {
            this.digit = digit;
        }
    }

    private Node head;          // Points to the most significant digit
    private Node tail;          // Points to the least significant digit
    private boolean negative;   // Flag indicating if the number is negative

    /**
     * Default constructor: initializes BigNumber to 0.
     */
    public BigNumber() {
        pushBackDigit(0);
    }

    /**
     * String constructor: parses a string representation into a BigNumber.
     * Supports optional '+' or '-' sign prefix and handles whitespace.
     * @param s The string to parse (e.g., "123", "-456", "+789")
     * @throws IllegalArgumentException if s is null, empty, or contains invalid characters
     */
    public BigNumber(String s) {
        if (s == null) throw new IllegalArgumentException("null number string");
        String t = s.replaceAll("\\s+", "");
        if (t.isEmpty()) throw new IllegalArgumentException("empty number string");

        int i = 0;
        if (t.charAt(0) == '+' || t.charAt(0) == '-') {
            negative = (t.charAt(0) == '-');
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
     * Copy constructor: creates a deep copy of another BigNumber.
     * @param other The BigNumber to copy
     * @throws NullPointerException if other is null
     */
    public BigNumber(BigNumber other) {
        if (other == null) throw new NullPointerException("other");
        this.negative = other.negative;
        for (Node p = other.head; p != null; p = p.next) {
            pushBackDigit(p.digit);
        }
    }

    /**
     * Returns the absolute value (magnitude) of a BigNumber.
     * @param x The BigNumber to take the absolute value of
     * @return A new BigNumber with the same magnitude but positive sign
     */
    public static BigNumber abs(BigNumber x) {
        BigNumber result = new BigNumber(x);
        result.negative = false;
        return result;
    }

    /**
     * Checks if this BigNumber is negative.
     * @return true if negative, false otherwise
     */
    public boolean isNegative() {
        return negative;
    }

    /**
     * Converts this BigNumber to its string representation.
     * @return String representation (e.g., "-123", "456")
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (negative && !isZero()) sb.append('-');
        for (Node p = head; p != null; p = p.next) {
            sb.append((char) ('0' + p.digit));
        }
        return sb.toString();
    }

    // ========== Linked List Helpers ==========

    /**
     * Clears the list and resets to empty state.
     */
    private void clear() {
        head = null;
        tail = null;
        negative = false;
    }

    /**
     * Appends a digit to the end (least significant side) of the list.
     * @param digit The digit to append [0-9]
     */
    private void pushBackDigit(int digit) {
        Node node = new Node(digit);
        node.prev = tail;
        if (tail != null) tail.next = node;
        tail = node;
        if (head == null) head = node;
    }

    /**
     * Inserts a digit at the beginning (most significant side) of the list.
     * @param digit The digit to insert [0-9]
     */
    private void pushFrontDigit(int digit) {
        Node node = new Node(digit);
        node.next = head;
        if (head != null) head.prev = node;
        head = node;
        if (tail == null) tail = node;
    }

    /**
     * Removes the most significant digit from the list.
     */
    private void popFrontDigit() {
        if (head == null) return;
        head = head.next;
        if (head != null) head.prev = null;
        else tail = null;
    }

    /**
     * Checks if this BigNumber equals zero.
     * @return true if the number is exactly 0
     */
    private boolean isZero() {
        return head != null && head == tail && head.digit == 0;
    }

    /**
     * Normalizes the representation to canonical form:
     * - Removes leading zeros (except for 0 itself)
     * - Ensures zero is always non-negative
     */
    private void normalize() {
        while (head != null && head.digit == 0 && head != tail) {
            popFrontDigit();
        }
        if (head == null) pushBackDigit(0);
        if (isZero()) negative = false;
    }

    // ========== Comparison and Conversion ==========

    /**
     * Compares the absolute values (magnitudes) of two BigNumbers.
     * @param a First BigNumber
     * @param b Second BigNumber
     * @return -1 if |a| < |b|, 0 if |a| == |b|, 1 if |a| > |b|
     */
    private static int compareAbs(BigNumber a, BigNumber b) {
        int lenA = 0;
        int lenB = 0;
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
     * Converts an unsigned integer to a BigNumber.
     * @param x Non-negative integer value
     * @return BigNumber representation of x
     * @throws IllegalArgumentException if x < 0
     */
    private static BigNumber fromUnsignedInt(int x) {
        if (x < 0) throw new IllegalArgumentException("x must be >= 0");
        return new BigNumber(Integer.toString(x));
    }

    // ========== Magnitude Arithmetic ==========

    /**
     * Adds the absolute values of two BigNumbers: |a| + |b|.
     * @param a First BigNumber (magnitude)
     * @param b Second BigNumber (magnitude)
     * @return New BigNumber representing the sum of magnitudes
     */
    private static BigNumber addAbs(BigNumber a, BigNumber b) {
        BigNumber result = new BigNumber();
        result.clear();
        int carry = 0;
        Node pa = a.tail;
        Node pb = b.tail;
        while (pa != null || pb != null || carry != 0) {
            int da = (pa != null) ? pa.digit : 0;
            int db = (pb != null) ? pb.digit : 0;
            int sum = da + db + carry;
            result.pushFrontDigit(sum % 10);
            carry = sum / 10;
            pa = (pa != null) ? pa.prev : null;
            pb = (pb != null) ? pb.prev : null;
        }
        result.normalize();
        return result;
    }

    /**
     * Subtracts the absolute values: |a| - |b| (assumes |a| >= |b|).
     * @param a Larger BigNumber (magnitude)
     * @param b Smaller BigNumber (magnitude)
     * @return New BigNumber representing the difference of magnitudes
     */
    private static BigNumber subAbs(BigNumber a, BigNumber b) {
        BigNumber result = new BigNumber();
        result.clear();
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
            result.pushFrontDigit(da - db);
            pa = pa.prev;
            pb = (pb != null) ? pb.prev : null;
        }
        result.normalize();
        return result;
    }

    /**
     * Multiplies a BigNumber by a single digit: |a| * digit.
     * @param a BigNumber (magnitude)
     * @param digit Single digit multiplier [0-9]
     * @return New BigNumber representing the product
     */
    private static BigNumber mulAbsByDigit(BigNumber a, int digit) {
        BigNumber result = new BigNumber();
        result.clear();
        if (digit == 0 || a.isZero()) {
            result.pushBackDigit(0);
            return result;
        }
        int carry = 0;
        for (Node p = a.tail; p != null; p = p.prev) {
            int prod = p.digit * digit + carry;
            result.pushFrontDigit(prod % 10);
            carry = prod / 10;
        }
        while (carry != 0) {
            result.pushFrontDigit(carry % 10);
            carry /= 10;
        }
        result.normalize();
        return result;
    }

    /**
     * Multiplies two BigNumbers using grade-school (long) multiplication: |a| * |b|.
     * @param a First BigNumber (magnitude)
     * @param b Second BigNumber (magnitude)
     * @return New BigNumber representing the product
     */
    private static BigNumber mulAbs(BigNumber a, BigNumber b) {
        BigNumber result = new BigNumber();
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

    // ========== Public Arithmetic Operations ==========

    /**
     * Adds two signed BigNumbers: a + b.
     * Handles all sign combinations using magnitude arithmetic.
     * @param a First addend
     * @param b Second addend
     * @return New BigNumber representing the sum
     */
    public static BigNumber add(BigNumber a, BigNumber b) {
        if (a.negative == b.negative) {
            BigNumber result = addAbs(a, b);
            result.negative = a.negative;
            result.normalize();
            return result;
        }
        int cmp = compareAbs(a, b);
        if (cmp == 0) return new BigNumber();
        BigNumber result = (cmp > 0) ? subAbs(a, b) : subAbs(b, a);
        result.negative = (cmp > 0) ? a.negative : b.negative;
        result.normalize();
        return result;
    }

    /**
     * Subtracts two signed BigNumbers: a - b (computed as a + (-b)).
     * @param a The minuend
     * @param b The subtrahend
     * @return New BigNumber representing the difference
     */
    public static BigNumber subtract(BigNumber a, BigNumber b) {
        BigNumber nb = new BigNumber(b);
        if (!nb.isZero()) nb.negative = !nb.negative;
        return add(a, nb);
    }

    /**
     * Multiplies two signed BigNumbers: a * b.
     * Applies sign rule after multiplying magnitudes.
     * @param a First factor
     * @param b Second factor
     * @return New BigNumber representing the product
     */
    public static BigNumber multiply(BigNumber a, BigNumber b) {
        BigNumber result = mulAbs(abs(a), abs(b));
        result.negative = (a.negative != b.negative) && !result.isZero();
        return result;
    }

    // ========== Division Helpers ==========

    /**
     * Multiplies this BigNumber by 10 (appends a 0 digit at the least significant end).
     */
    private void mulBy10() {
        if (!isZero()) pushBackDigit(0);
    }

    /**
     * Adds a single small digit to this BigNumber in-place.
     * @param digit Single digit to add [0-9]
     */
    private void addSmallDigit(int digit) {
        BigNumber sum = addAbs(this, new BigNumber(Integer.toString(digit)));
        this.head = sum.head;
        this.tail = sum.tail;
        this.negative = sum.negative;
    }

    /**
     * Performs integer division on absolute values using long division algorithm.
     * Quotient is returned; remainder is stored in remainderOut parameter.
     * @param dividend The dividend (magnitude)
     * @param divisor The divisor (magnitude, must not be zero)
     * @param remainderOut Container where the remainder will be stored
     * @return New BigNumber representing the quotient
     * @throws IllegalArgumentException if divisor is zero
     */
    private static BigNumber divAbsInteger(BigNumber dividend, BigNumber divisor, BigNumber remainderOut) {
        if (divisor.isZero()) throw new IllegalArgumentException("division by zero");
        BigNumber quotient = new BigNumber();
        quotient.clear();
        remainderOut.clear();
        remainderOut.pushBackDigit(0);

        // Process each digit of the dividend from most to least significant
        for (Node p = dividend.head; p != null; p = p.next) {
            remainderOut.mulBy10();
            remainderOut.addSmallDigit(p.digit);
            int digit = 0;
            if (compareAbs(remainderOut, divisor) >= 0) {
                // Binary search to find the largest digit d where divisor*d <= remainder
                int lo = 0;
                int hi = 9;
                while (lo <= hi) {
                    int mid = (lo + hi) / 2;
                    int cmp = compareAbs(mulAbsByDigit(divisor, mid), remainderOut);
                    if (cmp <= 0) {
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
            }
            quotient.pushBackDigit(digit);
        }
        quotient.normalize();
        remainderOut.normalize();
        return quotient;
    }

    /**
     * Divides two BigNumbers and returns the result as a decimal string.
     * Performs both integer and fractional division.
     * @param dividend The dividend
     * @param divisor The divisor (must not be zero)
     * @param precision Maximum number of decimal places to compute
     * @return Decimal string representation of the quotient (e.g., "12.5", "-3.14159")
     * @throws IllegalArgumentException if divisor is zero
     */
    public static String divideToDecimalString(BigNumber dividend, BigNumber divisor, int precision) {
        if (precision < 0) precision = 0;
        if (abs(divisor).isZero()) throw new IllegalArgumentException("division by zero");
        
        // Determine if result should be negative
        boolean negative = (dividend.negative != divisor.negative) && !abs(dividend).isZero();
        
        // Perform integer division on absolute values
        BigNumber rem = new BigNumber();
        BigNumber quotient = divAbsInteger(abs(dividend), abs(divisor), rem);
        
        // Build result string with integer part
        StringBuilder result = new StringBuilder();
        if (negative && !quotient.isZero()) result.append('-');
        result.append(quotient.toString());
        
        // Compute fractional digits using long division
        StringBuilder frac = new StringBuilder();
        for (int i = 0; i < precision && !rem.isZero(); i++) {
            rem.mulBy10();
            int digit = 0;
            if (compareAbs(rem, abs(divisor)) >= 0) {
                // Binary search for the quotient digit
                int lo = 0;
                int hi = 9;
                while (lo <= hi) {
                    int mid = (lo + hi) / 2;
                    if (compareAbs(mulAbsByDigit(abs(divisor), mid), rem) <= 0) {
                        digit = mid;
                        lo = mid + 1;
                    } else {
                        hi = mid - 1;
                    }
                }
                BigNumber newRem = subAbs(rem, mulAbsByDigit(abs(divisor), digit));
                rem.head = newRem.head;
                rem.tail = newRem.tail;
                rem.negative = false;
            }
            frac.append((char) ('0' + digit));
        }
        
        // Trim trailing zeros from fractional part
        while (frac.length() > 0 && frac.charAt(frac.length() - 1) == '0') {
            frac.setLength(frac.length() - 1);
        }
        
        // Append fractional part if non-empty
        if (frac.length() > 0) {
            result.append('.').append(frac);
        }
        return result.toString();
    }
}
