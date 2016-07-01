package org.agarage.jieba;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nicholas on 2016/7/1.
 */
public class Trie<V> {
    private Map<Character, Node<V>> roots = new HashMap<>();

    public void put(String word, V value) {
        if (word.length() == 0) return;
        Node<V> parent = roots.get(word.charAt(0));
        if (parent == null) {
            parent = new Node<V>();
            parent.setChr(word.charAt(0));
            roots.put(parent.getChr(), parent);
        }
        Node<V> node = parent;
        for (int i = 1; i < word.length(); i ++) {
            node = parent.getNext(word.charAt(i));
            if (node == null) {
                node = new Node<V>();
                node.setChr(word.charAt(i));
                parent.putNext(node.getChr(), node);
            }
            parent = node;
        }
        node.setValue(value);
    }

    public V get(String word, V notFound) {
        if (word.length() == 0) return null;
        Node<V> node = roots.get(word.charAt(0));
        for (int i = 1; i < word.length() && node != null; i ++) {
            node = node.getNext(word.charAt(i));
        }
        if (node == null) {
            return notFound;
        }
        return node.getValue();
    }

    public V get(String word) {
        return get(word, null);
    }

    public static class Node<V> {
        private Character chr;
        private V value;
        private Map<Character, Node<V>> next = new HashMap<>();

        public Character getChr() {
            return chr;
        }

        public void setChr(Character chr) {
            this.chr = chr;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Node<V> getNext(Character chr) {
            return next.get(chr);
        }

        public void putNext(Character chr, Node<V> node) {
            this.next.put(chr, node);
        }
    }
}
