package com.beney.zip;

import com.beney.common.TernarySearchTrie;
import com.beney.utils.BinaryIn;
import com.beney.utils.BinaryOut;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

/**
 * @author Beney
 */
public class LZWZip implements MyZip {

    private static final int R = 256;
    private static final int W = 12;    // codeword width
    private static final int L = 4096;  // number of codeword = 2^W
    private static final String SUFFIX = ".lzw";

    private BinaryIn in = null;
    private BinaryOut out = null;

    @Override
    public void compress(String filePath) {
        try {
            in = new BinaryIn(new FileInputStream(filePath));
            out = new BinaryOut(new FileOutputStream(filePath + SUFFIX));
            TernarySearchTrie<Integer> symbolTable = new TernarySearchTrie<>();
            for (int i = 0; i < R; i++) {
                symbolTable.put("" + (char) i, i);
            }

            String input = in.readString();
            int code = R + 1;
            while (input.length() > 0) {
                String key = symbolTable.longestPrefixOf(input);
                out.write(symbolTable.get(key), W);     // output key's encoding
                int t = key.length();
                if (t < input.length() && code < L) {   // add key to symbol table
                    symbolTable.put(input.substring(0, t + 1), code++);
                }
                input = input.substring(t);     // scan past key in input
            }
            out.write(R, W);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void expand(String filePath) {
        try {
            in = new BinaryIn(new FileInputStream(filePath));
            out = new BinaryOut(new FileOutputStream(filePath.substring(0, filePath.length() - SUFFIX.length())));
            String[] st = new String[L];
            int i;
            for (i = 0; i < R; i++) { // initialize symbol table with all 1-character strings
                st[i] = "" + (char) i;
            }
            st[i++] = ""; // unused lookahead for EOF
            int codeword = in.readInt(W);
            if (codeword == R) {
                return; // empty string
            }
            String val = st[codeword];
            while (true) {
                out.write(val);
                codeword = in.readInt(W);
                if (codeword == R) {
                    break;
                }
                String s = st[codeword];
                if (i == codeword) s = val + val.charAt(0);
                if (i < L) st[i++] = val + s.charAt(0);
                val = s;
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String fileSuffix() {
        return SUFFIX;
    }
}
