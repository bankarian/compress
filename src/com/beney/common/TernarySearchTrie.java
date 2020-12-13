package com.beney.common;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The {@code TernarySearchTrie} is an symbol table of key-val pairs,
 * with string keys and generic values;
 *
 * @author Beney
 */
public class TernarySearchTrie<V> {
    private int size;
    private Node<V> root;   // root of TST


    private static class Node<V> {
        private char c;
        private Node<V> left, mid, right;
        private V val;  // value associated with string
    }

    public int size() {
        return size;
    }

    /**
     *
     * @param key key
     * @return {@code true} if this symbol table contains {@code key}
     *      and {@code false} otherwise
     */
    public boolean contains(String key) {
        if (key == null) {
            return false;
        }
        return get(key) != null;
    }

    /**
     *
     * @param key key
     * @return the value associated with the given key if the key exist in the
     *      symbol table, {@code null} otherwise
     */
    public V get(String key) {
        if (key == null || key.length() == 0) {
            return null;
        }
        Node<V> x = get(root, key, 0);
        if (x == null) return null;
        return x.val;
    }

    // return subtrie corresponding to given key
    private Node<V> get(Node<V> x, String key, int idx) {
        if (x == null) return null;
        if (key == null || key.length() == 0) {
            return null;
        }
        char c = key.charAt(idx);
        if      (c < x.c)                return get(x.left, key, idx);
        else if (c > x.c)                return get(x.right, key, idx);
        else if (idx < key.length() - 1) return get(x.mid, key, idx + 1);
        else                             return x;
    }

    /**
     * Insert key-value pair into the symbol table, overwriting the old
     * value with the new if the key is already in the table.
     * if the value is {@code null}, effectively deletes the key from the table
     * @param key key
     * @param val value
     */
    public void put(String key, V val) {
        if (key == null || key.length() == 0) return;
        if (!contains(key))     size++;
        else if (val == null)   size --;
        root = put(root, key, val, 0);
    }

    // return subtrie after inserting key on subtrie (x)
    private Node<V> put(Node<V> x, String key, V val, int idx) {
        char c = key.charAt(idx);
        if (x == null) {
            x = new Node<>();
            x.c = c;
        }
        if      (c < x.c)                x.left  = put(x.left, key, val, idx);
        else if (c > x.c)                x.right = put(x.right, key, val, idx);
        else if (idx < key.length() - 1) x.mid   = put(x.mid, key, val, idx + 1);
        else                             x.val   = val;
        return x;
    }

    /**
     *
     * @param query query string
     * @return the string in the symbol table that is the longest prefix
     *      of {@code query}, or {@code null} if no such string.
     */
    public String longestPrefixOf(String query) {
        if (query == null || query.length() == 0) {
            return null;
        }
        int len = 0, queryLength = query.length();
        Node<V> x = root;
        int idx = 0;
        while (x != null && idx < queryLength) {
            char c = query.charAt(idx);
            if (c < x.c) x = x.left;
            else if (c > x.c) {
                x = x.right;
            } else {
                idx++;
                if (x.val != null) len = idx;
                x = x.mid;
            }
        }
        return query.substring(0, len);
    }

    /**
     *
     * @return all keys in the table as an {@code Iterable}
     */
    public Iterable<String> keys() {
        Queue<String> queue = new LinkedList<>();
        collect(root, new StringBuilder(), queue);
        return queue;
    }

    private void collect(Node<V> x, StringBuilder prefix, Queue<String> q) {
        if (x == null) return;
        collect(x.left, prefix, q);
        if (x.val != null) {
            q.offer(prefix.toString() + x.c);
        }
        collect(x.mid, prefix.append(x.c), q);
        prefix.deleteCharAt(prefix.length() - 1);
        collect(x.right, prefix, q);
    }


//    public static void main(String[] args) {
//        TernarySearchTrie<Integer> st = new TernarySearchTrie<>();
//        Scanner cin = new Scanner(System.in);
//        for (int i = 0; i < 5; i++) {
//            String key = cin.next();
//            st.put(key, i);
//        }
//        for (String key : st.keys()) {
//            System.out.println(key + ": " + st.get(key));
//        }
//    }
}
