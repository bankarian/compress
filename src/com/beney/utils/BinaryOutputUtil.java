package com.beney.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class BinaryOutputUtil {
    private static final OutputStream DEFAULT_TAR = System.out;

    private static BufferedOutputStream out;  // output stream ( output)
    private static int buffer;                // 8-bit buffer of bits to write
    private static int n;                     // number of bits remaining in buffer
    private static boolean isInitialized;     // has BinaryStdOut been called for first time?
    private static OutputStream outputTar = DEFAULT_TAR;

    // 设置输出流，默认为控制台
    public static void setOutputTar(OutputStream outputTar) {
        BinaryOutputUtil.outputTar = outputTar;
        initialize();
    }

    // don't instantiate
    private BinaryOutputUtil() {
    }

    // initialize BinaryStdOut
    private static void initialize() {
        out = new BufferedOutputStream(outputTar);
        buffer = 0;
        n = 0;
        isInitialized = true;
    }

    /**
     * Writes the specified bit to  output.
     */
    private static void writeBit(boolean bit) {
        if (!isInitialized) initialize();

        // add bit to buffer
        buffer <<= 1;
        if (bit) buffer |= 1;

        // if buffer is full (8 bits), write out as a single byte
        n++;
        if (n == 8) clearBuffer();
    }

    /**
     * Writes the 8-bit byte to  output.
     */
    private static void writeByte(int x) {
        if (!isInitialized) initialize();

        assert x >= 0 && x < 256;

        // optimized if byte-aligned
        if (n == 0) {
            try {
                out.write(x);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // otherwise write one bit at a time
        for (int i = 0; i < 8; i++) {
            boolean bit = ((x >>> (8 - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    // write out any remaining bits in buffer to  output, padding with 0s
    private static void clearBuffer() {
        if (!isInitialized) initialize();

        if (n == 0) return;
        if (n > 0) buffer <<= (8 - n);
        try {
            out.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        n = 0;
        buffer = 0;
    }

    /**
     * Flushes  output, padding 0s if number of bits written so far
     * is not a multiple of 8.
     */
    public static void flush() {
        clearBuffer();
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Flushes and closes  output. Once  output is closed, you can no
     * longer write bits to it.
     */
    public static void close() {
        flush();
        try {
            out.close();
            isInitialized = false;
            setOutputTar(DEFAULT_TAR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Writes the specified bit to  output.
     *
     * @param x the {@code boolean} to write.
     */
    public static void write(boolean x) {
        writeBit(x);
    }

    /**
     * Writes the 8-bit byte to  output.
     *
     * @param x the {@code byte} to write.
     */
    public static void write(byte x) {
        writeByte(x & 0xff);
    }

    /**
     * Writes the 32-bit int to  output.
     *
     * @param x the {@code int} to write.
     */
    public static void write(int x) {
        writeByte((x >>> 24) & 0xff);
        writeByte((x >>> 16) & 0xff);
        writeByte((x >>> 8) & 0xff);
        writeByte((x >>> 0) & 0xff);
    }


    /**
     * Writes the 8-bit char to  output.
     *
     * @param x the {@code char} to write.
     * @throws IllegalArgumentException if {@code x} is not betwen 0 and 255.
     */
    public static void write(char x) {
        if (x < 0 || x >= 256) throw new IllegalArgumentException("Illegal 8-bit char = " + x);
        writeByte(x);
    }

    /**
     * Writes the <em>r</em>-bit char to  output.
     *
     * @param x the {@code char} to write.
     * @param r the number of relevant bits in the char.
     * @throws IllegalArgumentException if {@code r} is not between 1 and 16.
     * @throws IllegalArgumentException if {@code x} is not between 0 and 2<sup>r</sup> - 1.
     */
    public static void write(char x, int r) {
        if (r == 8) {
            write(x);
            return;
        }
        if (r < 1 || r > 16) throw new IllegalArgumentException("Illegal value for r = " + r);
        if (x >= (1 << r)) throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
        for (int i = 0; i < r; i++) {
            boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }
}
