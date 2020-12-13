package com.beney.zip;


import com.beney.utils.BinaryIn;
import com.beney.utils.BinaryOut;

import java.util.*;

/**
 * compress and expand based on Huffman Tree
 *
 * @author Beney
 */
public class HuffmanZip implements MyZip {
    private static final int R = 256; // total number of ascii

    private static final String SUFFIX = ".huf"; // suffix of the compressed file

    private BinaryIn in = null;
    private BinaryOut out = null;

    @Override
    public void compress(String filePath) {
        in = new BinaryIn(filePath);
        out = new BinaryOut(filePath + SUFFIX);
        char[] input = in.readString().toCharArray();
        int[] freq = new int[R];

        for (int i = 0; i < input.length; i++) {
            freq[input[i]]++;
        }
        Node root = buildTrie(freq);
        Map<Character, String> codeMap = buildCode(root);

        writeTrie(root, out);
        out.write(input.length);
        writeCode(codeMap, input, out);
        out.close();
    }

    @Override
    public void expand(String filePath) {
        in = new BinaryIn(filePath);
        out = new BinaryOut(filePath.substring(0, filePath.length() - SUFFIX.length()));

        Node root = readTrie(in), x;
        int length = in.readInt();
        for (int i = 0; i < length; i++) {
            x = root;
            while (!x.isLeaf()) {
                boolean bit = in.readBoolean();
                if (bit) x = x.right;
                else x = x.left;
            }
            out.write(x.ch, 8);
        }
        out.close();
    }

    @Override
    public String fileSuffix() {
        return SUFFIX;
    }

    /**
     * Node in Huffman Tree
     */
    private static class Node implements Comparable<Node> {

        private final char ch;      // only used for leaf
        private final int freq;     // only used for leaf
        private Node left, right;

        public Node(char ch, int freq, Node left, Node right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        public Node(char ch, int freq) {
            this.ch = ch;
            this.freq = freq;
        }

        public boolean isLeaf() {
            return this.left == null && this.right == null;
        }

        @Override
        public int compareTo(Node o) {
            return this.freq - o.freq;  // 通过freq(频率)来比较
        }
    }

    //**************************** private methods ********************************//

    /**
     * @param freq frequency of characters
     * @return root of huffman tree
     */
    private Node buildTrie(int[] freq) {
        Queue<Node> pq = new PriorityQueue<>();
        Node a, b, parent;
        for (char c = 0; c < R; c++) {
            if (freq[c] > 0) {
                pq.add(new Node(c, freq[c]));
            }
        }
        while (pq.size() > 1) {
            a = pq.remove();
            b = pq.remove();
            parent = new Node('\0', a.freq + b.freq, a, b);
            pq.add(parent);
        }
        return pq.remove();
    }

    private Map<Character, String> buildCode(Node r) {
        Map<Character, String> map = new HashMap<>();
        buildCode(map, r, "");
        return map;
    }

    private void buildCode(Map<Character, String> codeMap, Node n, String code) {
        if (n == null) {
        } else if (!n.isLeaf()) {
            buildCode(codeMap, n.left, code + '0');
            buildCode(codeMap, n.right, code + '1');
        } else
            codeMap.put(n.ch, code);
    }

    /**
     * output using pre-order traversal
     * internal node {@code 0/false}, leaf {@code 1/true}
     *
     * @param n current node
     */
    private void writeTrie(Node n, BinaryOut out) {
        if (n.isLeaf()) {
            out.write(true);
            out.write(n.ch, 8);
            return;
        }
        out.write(false);
        writeTrie(n.left, out);
        writeTrie(n.right, out);
    }

    private void writeCode(Map<Character, String> codeMap, char[] input, BinaryOut out) {
        String code;
        for (int i = 0; i < input.length; i++) {
            code = codeMap.get(input[i]);
            for (int j = 0; j < code.length(); j++) {
                if (code.charAt(j) == '0') out.write(false);
                else if (code.charAt(j) == '1') out.write(true);
            }
        }
    }

    private Node readTrie(BinaryIn in) {
        boolean isLeaf = in.readBoolean();
        if (isLeaf)
            return new Node(in.readChar(), -1, null, null);
        else
            return new Node('\0', -1, readTrie(in), readTrie(in));
    }
}
