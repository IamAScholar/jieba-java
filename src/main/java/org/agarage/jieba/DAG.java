package org.agarage.jieba;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Nicholas on 2016/7/1.
 */
public class DAG extends HashMap<Integer, DAG.Node> {

    public static class Node extends LinkedList<Integer> {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (Integer i : this) {
                sb.append(i);
                sb.append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("]");
            return sb.toString();
        }
    }
}
