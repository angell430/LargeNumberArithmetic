import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class Main {
    private static String readValueLine(BufferedReader br) throws IOException {
        // Accept either "m = 123" or just "123"
        String line = br.readLine();
        if (line == null) return null;
        int eq = line.indexOf('=');
        String rhs = (eq >= 0) ? line.substring(eq + 1) : line;
        return rhs.trim();
    }

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Enter m (e.g. m = 123):");
            String ms = readValueLine(br);
            System.out.println("Enter n (e.g. n = 456):");
            String ns = readValueLine(br);

            if (ms == null || ns == null || ms.isEmpty() || ns.isEmpty()) {
                System.err.println("Missing input.");
                return;
            }

            BigNumber m = new BigNumber(ms);
            BigNumber n = new BigNumber(ns);

            BigNumber add = BigNumber.add(m, n);
            BigNumber sub = BigNumber.subtract(m, n);
            BigNumber mul = BigNumber.multiply(m, n);
            String div = BigNumber.divideToDecimalString(m, n, 20);

            System.out.println("addition = " + add.toString());
            System.out.println("subtraction = " + sub.toString());
            System.out.println("multiplication = " + mul.toString());
            System.out.println("division = " + div);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        }
    }
}

