package com.beney.huffman;



import com.beney.interfaces.MyZip;
import com.beney.utils.BinaryInputUtil;
import com.beney.utils.BinaryOutputUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * 基于哈夫曼算法实现压缩和解压缩
 *
 * @author Beney
 */
public class HuffmanZip implements MyZip {
    /**
     * 字符ascii码的总数
     */
    private final int R = 256;

    /**
     * 压缩后文件的后缀
     */
    public final String SUFFIX = ".huf";

    @Override
    public void compress(String filePath) {
        int[] freq = getFrequencyFromFile(filePath);
        Node root = buildTrie(freq);
        Map<Character, String> codeMap = buildCode(root);

        writeCompressResult(filePath, root, codeMap);
    }

    @Override
    public void expand(String filePath) {
        try {
            BinaryInputUtil.setInputSrc(new FileInputStream(new File(filePath)));
            BinaryOutputUtil.setOutputTar(new FileOutputStream(new File(filePath.substring(0, filePath.length() - SUFFIX.length()))));

            Node root = readTrie(), n;
            while (!BinaryInputUtil.isEmpty()) {
                n = root;
                while (!n.isLeaf()) {
                    boolean bit = BinaryInputUtil.readBoolean();
                    if (bit) n = n.right;
                    else n = n.left;
                }
                BinaryOutputUtil.write(n.ch, 8);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            BinaryInputUtil.close();
            BinaryOutputUtil.close();
        }
    }

    @Override
    public String fileSuffix() {
        return SUFFIX;
    }

    /**
     * 哈夫曼树结点
     */
    private static class Node implements Comparable<Node> {

        private final char ch;      // 仅用于叶子结点
        private final int freq;     // 仅用于叶子结点
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

    private void writeCompressResult(String filePath, Node root, Map<Character, String> codeMap) {
        try {
            BinaryOutputUtil.setOutputTar(new FileOutputStream(new File(filePath + SUFFIX)));
            BinaryInputUtil.setInputSrc(new FileInputStream(new File(filePath)));
            writeTrie(root);
            writeCode(codeMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            BinaryInputUtil.close();
            BinaryOutputUtil.close();
        }
    }

    /**
     * @param freq 统计字符出现频率的数组
     * @return huffman树的根
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

    private int[] getFrequencyFromFile(String filePath) {
        int[] freq = new int[R];
        try {
            BinaryInputUtil.setInputSrc(new FileInputStream(new File(filePath)));
            while (!BinaryInputUtil.isEmpty()) {
                freq[BinaryInputUtil.readChar()] += 1;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            BinaryInputUtil.close();
        }
        return freq;
    }

    /**
     * 前序遍历将Trie树输出<br/>
     * 分支结点0/false，叶子结点1/true
     *
     * @param n 当前遍历到的结点
     */
    private void writeTrie(Node n) {
        if (n.isLeaf()) {
            BinaryOutputUtil.write(true);
            BinaryOutputUtil.write(n.ch, 8);
            return;
        }
        BinaryOutputUtil.write(false);
        writeTrie(n.left);
        writeTrie(n.right);
    }

    private void writeCode(Map<Character, String> codeMap) {
        String code;
        while (!BinaryInputUtil.isEmpty()) {
            code = codeMap.get(BinaryInputUtil.readChar());
            for (int i = 0; i < code.length(); ++i) {
                if (code.charAt(i) == '0')
                    BinaryOutputUtil.write(false);
                else
                    BinaryOutputUtil.write(true);
            }
        }
    }

    private Node readTrie() {
        boolean isLeaf = BinaryInputUtil.readBoolean();
        if (isLeaf)
            return new Node(BinaryInputUtil.readChar(), -1, null, null);
        else
            return new Node('\0', -1, readTrie(), readTrie());
    }
}
