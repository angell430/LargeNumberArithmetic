import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main application class serving as the entry point for the BigNumber calculator.
 * It handles terminal I/O, parses user inputs, and executes basic arithmetic operations.
 */
public final class Main {
    
    /**
     * Reads one line and accepts either a plain number ("123")
     * or a variable-style assignment ("m = 123").
     * * @param br The BufferedReader tied to standard input.
     * @return The cleaned right-hand side numeric string, or null if EOF is reached.
     * @throws IOException If an I/O transport error occurs while reading.
     */
    private static String readValueLine(BufferedReader br) throws IOException {
        // Accept either "m = 123" or just "123"
        String line = br.readLine();
        if (line == null) return null;
        int eq = line.indexOf('=');
        String rhs = (eq >= 0) ? line.substring(eq + 1) : line;
        return rhs.trim();
    }

    /**
     * Executes the application life cycle: gathering inputs, invoking the BigNumber 
     * library API methods, and rendering calculated answers to the console.
     * * @param args Command-line arguments (not utilized).
     */
    public static void main(String[] args) {
        // Instantiate a BufferedReader inside a try-with-resources block to handle auto-closing.
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            // Read the two operands from stdin.
            System.out.println("Enter m (e.g. m = 123):");
            String ms = readValueLine(br);
            System.out.println("Enter n (e.g. n = 456):");
            String ns = readValueLine(br);

            // Basic validation before parsing into BigNumber.
            if (ms == null || ns == null || ms.isEmpty() || ns.isEmpty()) {
                System.err.println("Missing input.");
                return;
            }

            // Parse user input into big integers backed by linked lists.
            BigNumber m = new BigNumber(ms);
            BigNumber n = new BigNumber(ns);

            // Run all required arithmetic operations.
            BigNumber add = BigNumber.add(m, n);
            BigNumber sub = BigNumber.subtract(m, n);
            BigNumber mul = BigNumber.multiply(m, n);
            String div = BigNumber.divideToDecimalString(m, n, 20);

            // Print final results.
            System.out.println("addition = " + add.toString());
            System.out.println("subtraction = " + sub.toString());
            System.out.println("multiplication = " + mul.toString());
            System.out.println("division = " + div);
        } catch (IllegalArgumentException e) {
            // Handles invalid numbers and division-by-zero from BigNumber.
            System.err.println("Error: " + e.getMessage());
        } catch (IOException e) {
            // Handles console input failures.
            System.err.println("I/O Error: " + e.getMessage());
        }
    }
}