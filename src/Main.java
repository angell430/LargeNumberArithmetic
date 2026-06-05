import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main application class for the BigNumber calculator.
 * This program reads two large integers from the user and performs:
 * - Addition
 * - Subtraction
 * - Multiplication
 * - Division (with up to 20 decimal places)
 */
public final class Main {
    /**
     * Reads a line from input and extracts the numeric value.
     * Supports two input formats:
     * - "m = 123" (extracts "123")
     * - "123" (uses directly)
     * Handles whitespace trimming.
     * @param br BufferedReader connected to standard input
     * @return The numeric string, or null if EOF is reached
     * @throws IOException if an I/O error occurs
     */
    private static String readValueLine(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null) return null;
        int eq = line.indexOf('=');
        return ((eq >= 0) ? line.substring(eq + 1) : line).trim();
    }

    /**
     * Main entry point for the BigNumber calculator application.
     * Workflow:
     * 1. Prompts user to enter two numbers (m and n)
     * 2. Parses input strings into BigNumber objects
     * 3. Performs all four arithmetic operations
     * 4. Displays results to console
     * 5. Handles exceptions gracefully (invalid input, division by zero)
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Input:");
            // Prompt user for first operand
            System.out.print("Enter m:");
            String ms = readValueLine(br);
            
            // Prompt user for second operand
            System.out.print("Enter n:");
            String ns = readValueLine(br);

            // Validate input: neither operand should be null or empty
            if (ms == null || ns == null || ms.isEmpty() || ns.isEmpty()) {
                System.err.println("Missing input.");
                return;
            }

            // Parse string inputs into BigNumber objects
            BigNumber m = new BigNumber(ms);
            BigNumber n = new BigNumber(ns);

            // Perform all four arithmetic operations
            System.out.println("Output:");
            System.out.println("addition = " + BigNumber.add(m, n));
            System.out.println("subtraction = " + BigNumber.subtract(m, n));
            System.out.println("multiplication = " + BigNumber.multiply(m, n));
            System.out.println("division = " + BigNumber.divideToDecimalString(m, n, 20));
        } catch (IllegalArgumentException e) {
            // Handle invalid number format or division by zero
            System.err.println("Error: " + e.getMessage());
        } catch (IOException e) {
            // Handle console input errors
            System.err.println("I/O Error: " + e.getMessage());
        }
    }
}
