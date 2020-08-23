package com.beney.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

public final class BinaryInputUtil {
    private static final int EOF = -1;      // end of file
    private static final InputStream DEFAULT_SRC = System.in;

    private static BufferedInputStream in;  // input stream
    private static int buffer;              // one character buffer
    private static int n;                   // number of bits left in buffer
    private static boolean isInitialized;   // has BinaryStdIn been called for first time?
    private static InputStream inputSrc = DEFAULT_SRC;

    // 设置输入流，默认为控制台
    public static void setInputSrc(InputStream inputSrc) {
        BinaryInputUtil.inputSrc = inputSrc;
    }

    // don't instantiate
    private BinaryInputUtil() {
    }

    // fill buffer
    private static void initialize() {
        in = new BufferedInputStream(inputSrc);
        buffer = 0;
        n = 0;
        fillBuffer();
        isInitialized = true;
    }

    private static void fillBuffer() {
        try {
            buffer = in.read();
            n = 8;
        } catch (IOException e) {
            System.out.println("EOF");
            buffer = EOF;
            n = -1;
        }
    }

    /**
     * Close this input stream and release any associated system resources.
     */
    public static void close() {
        if (!isInitialized) initialize();
        try {
            in.close();
            isInitialized = false;
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not close BinaryStdIn", ioe);
        }
    }

    /**
     * Returns true if standard input is empty.
     *
     * @return true if and only if standard input is empty
     */
    public static boolean isEmpty() {
        if (!isInitialized) initialize();
        return buffer == EOF;
    }

    /**
     * Reads the next bit of data from standard input and return as a boolean.
     *
     * @return the next bit of data from standard input as a {@code boolean}
     * @throws NoSuchElementException if standard input is empty
     */
    public static boolean readBoolean() {
        if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
        n--;
        boolean bit = ((buffer >> n) & 1) == 1;
        if (n == 0) fillBuffer();
        return bit;
    }

    /**
     * Reads the next 8 bits from standard input and return as an 8-bit char.
     * Note that {@code char} is a 16-bit type;
     * to read the next 16 bits as a char, use {@code readChar(16)}.
     *
     * @return the next 8 bits of data from standard input as a {@code char}
     * @throws NoSuchElementException if there are fewer than 8 bits available on standard input
     */
    public static char readChar() {
        if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");

        // special case when aligned byte
        if (n == 8) {
            int x = buffer;
            fillBuffer();
            return (char) (x & 0xff);
        }

        // combine last n bits of current buffer with first 8-n bits of new buffer
        int x = buffer, oldN = n;
        x <<= (8 - n);
        fillBuffer();
        if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
        n = oldN;
        x |= (buffer >>> n);
        return (char) (x & 0xff);
        // the above code doesn't quite work for the last character if n = 8
        // because buffer will be -1, so there is a special case for aligned byte
    }

    /**
     * Reads the next <em>r</em> bits from standard input and return as an <em>r</em>-bit character.
     *
     * @param r number of bits to read.
     * @return the next r bits of data from standard input as a {@code char}
     * @throws NoSuchElementException   if there are fewer than {@code r} bits available on standard input
     * @throws IllegalArgumentException unless {@code 1 <= r <= 16}
     */
    public static char readChar(int r) {
        if (r < 1 || r > 16) throw new IllegalArgumentException("Illegal value of r = " + r);

        // optimize r = 8 case
        if (r == 8) return readChar();

        char x = 0;
        for (int i = 0; i < r; i++) {
            x <<= 1;
            boolean bit = readBoolean();
            if (bit) x |= 1;
        }
        return x;
    }

    /**
     * Reads the remaining bytes of data from standard input and return as a string.
     *
     * @return the remaining bytes of data from standard input as a {@code String}
     * @throws NoSuchElementException if standard input is empty or if the number of bits
     *                                available on standard input is not a multiple of 8 (byte-aligned)
     */
    public static String readString() {
        if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");

        StringBuilder sb = new StringBuilder();
        while (!isEmpty()) {
            char c = readChar();
            sb.append(c);
        }
        return sb.toString();
    }


    /**
     * Reads the next 32 bits from standard input and return as a 32-bit int.
     *
     * @return the next 32 bits of data from standard input as a {@code int}
     * @throws NoSuchElementException if there are fewer than 32 bits available on standard input
     */
    public static int readInt() {
        int x = 0;
        for (int i = 0; i < 4; i++) {
            char c = readChar();
            x <<= 8;
            x |= c;
        }
        return x;
    }

    /**
     * Reads the next <em>r</em> bits from standard input and return as an <em>r</em>-bit int.
     *
     * @param r number of bits to read.
     * @return the next r bits of data from standard input as a {@code int}
     * @throws NoSuchElementException   if there are fewer than {@code r} bits available on standard input
     * @throws IllegalArgumentException unless {@code 1 <= r <= 32}
     */
    public static int readInt(int r) {
        if (r < 1 || r > 32) throw new IllegalArgumentException("Illegal value of r = " + r);

        // optimize r = 32 case
        if (r == 32) return readInt();

        int x = 0;
        for (int i = 0; i < r; i++) {
            x <<= 1;
            boolean bit = readBoolean();
            if (bit) x |= 1;
        }
        return x;
    }

    /**
     * Reads the next 8 bits from standard input and return as an 8-bit byte.
     *
     * @return the next 8 bits of data from standard input as a {@code byte}
     * @throws NoSuchElementException if there are fewer than 8 bits available on standard input
     */
    public static byte readByte() {
        char c = readChar();
        return (byte) (c & 0xff);
    }
}