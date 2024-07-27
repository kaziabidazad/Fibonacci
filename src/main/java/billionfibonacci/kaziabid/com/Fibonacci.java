package billionfibonacci.kaziabid.com;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Kazi Abid
 */
public class Fibonacci {
    private final int        N;
    private BigInteger       result;
    private String           stringReasult;
    private AtomicBoolean    calculated = new AtomicBoolean(false);
    /**
     * Requirement: CUTOFF >= 64, or else there will be infinite recursion.
     */
    private static final int CUTOFF     = 1536;

    /**
     * 
     * @param n
     * 
     * @throws IllegalArgumentException
     */
    public Fibonacci(int n)
            throws IllegalArgumentException {
        if (n < 0)
            throw new IllegalArgumentException("Value should be positive integer only!");

        N = n;
    }

    private void checkAndCalculate() {
        if (!calculated.get()) {
            fastFibonacciDoubling();
            calculated.set(true);
        }
    }

    /**
     * https://www.nayuki.io/page/fast-fibonacci-algorithms <br>
     */
    private void fastFibonacciDoubling() {
        BigInteger a = BigInteger.ZERO;
        BigInteger b = BigInteger.ONE;
        for (int bit = Integer.highestOneBit(N); bit != 0; bit >>>= 1) {
            // Double it
            BigInteger d = multiply(a, b.shiftLeft(1).subtract(a));
            BigInteger e = multiply(a, a).add(multiply(b, b));
            a = d;
            b = e;
            // Advance by one conditionally
            if ((N & bit) != 0) {
                BigInteger c = a.add(b);
                a = b;
                b = c;
            }
        }
        this.result = a;
        this.stringReasult = a.toString();
    }

    private BigInteger multiply(BigInteger x, BigInteger y) {
        return multiplyKaratsuba(x, y);
    }

    /**
     * 
     * Returns {@code x * y}, the product of the specified integers. This gives the
     * same result as {@code x.multiply(y)} but should be faster.
     * 
     * @param x a multiplicand
     * @param y a multiplicand
     * 
     * @return {@code x} times {@code} y
     * 
     * @throws NullPointerException if {@code x} or {@code y} is {@code null}
     */
    private BigInteger multiplyKaratsuba(BigInteger x, BigInteger y) {
        if (x.bitLength() <= CUTOFF || y.bitLength() <= CUTOFF) {  // Base case
            return x.multiply(y);

        } else {
            int n = Math.max(x.bitLength(), y.bitLength());
            int half = (n + 32) / 64 * 32;  // Number of bits to use for the low part
            BigInteger mask = BigInteger.ONE.shiftLeft(half).subtract(BigInteger.ONE);
            BigInteger xlow = x.and(mask);
            BigInteger ylow = y.and(mask);
            BigInteger xhigh = x.shiftRight(half);
            BigInteger yhigh = y.shiftRight(half);

            BigInteger a = multiply(xhigh, yhigh);
            BigInteger b = multiply(xlow.add(xhigh), ylow.add(yhigh));
            BigInteger c = multiply(xlow, ylow);
            BigInteger d = b.subtract(a).subtract(c);
            return a.shiftLeft(half).add(d).shiftLeft(half).add(c);
        }
    }

    public BigInteger val() {
        checkAndCalculate();
        return this.result;
    }

    public String valString() {
        checkAndCalculate();
        return this.stringReasult;
    }

    @Override
    public String toString() {
        checkAndCalculate();
        return valString();
    }

    public void write(OutputStream os) throws IOException {
        checkAndCalculate();
        os.write(this.stringReasult.getBytes());
    }

    /**
     * Writes - overwrites if present
     * 
     * @param file
     * 
     * @throws IOException
     */
    public void write(File file) throws IOException {
        checkAndCalculate();
        if (!file.exists())
            file.createNewFile();
        try (FileChannel channel =
                FileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.allocate(stringReasult.length());
            buffer.put(stringReasult.getBytes());
            buffer.flip();
            channel.write(buffer);
        }

    }

    public static void main(String[] args) throws IOException {
        int N = 1000000000; // 1 billion
        Fibonacci fibonacci = new Fibonacci(N);
        long start = System.currentTimeMillis();
        fibonacci.val();
        long end = System.currentTimeMillis();
        long total = end - start;
        System.out
                .println(String
                        .format("Time taken to compute %sth fibonacci number: %s ms. or %s s.",
                                N, total, (total / 1000)));
        fibonacci.write(new File(String.format("Fib-%s.txt", N)));
        for (int i = 0; i <= 15; i++) {
            fibonacci = new Fibonacci(i);
            System.out.print(fibonacci.valString() + "   ");
        }
    }
}
