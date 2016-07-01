package org.agarage.jieba;

import java.util.*;

/**
 * Created by Nicholas on 2016/7/1.
 */
public class UndirectWeightGraph {
    private static final double D = 0.85;

    private Map<Integer, NodeList> graph = new HashMap<>();

    public void addEdge(int start, int end, double weight) {
        addNode(start, new Node(start, end, weight));
        addNode(end, new Node(end, start, weight));
    }

    private void addNode(int start, Node node) {
        NodeList nodes = graph.get(start);
        if (nodes == null) {
            nodes = new NodeList();
            graph.put(start, nodes);
        }
        nodes.add(node);
    }

    public Map<Integer, Double> rank() {
        Map<Integer, Double> ws = new HashMap<>();
        Map<Integer, Double> outSum = new HashMap<>();

        double wsdef = 1.0 / (graph.isEmpty() ? 1.0 : graph.size());
        for (Map.Entry<Integer, NodeList> entry : graph.entrySet()) {
            ws.put(entry.getKey(), wsdef);
            outSum.put(entry.getKey(), entry.getValue().sumWeight());
        }

        List<Integer> sortedKeys = new ArrayList<>(graph.size());
        sortedKeys.addAll(graph.keySet());
        for (int i = 0; i < 10; i ++) {
            for (Integer n : sortedKeys) {
                double s = 0.0;
                for (Node e : graph.get(n)) {
                    s += e.getWeight() / outSum.get(e.getEnd()) * ws.get(e.getEnd());
                }
                ws.put(n, (1 - D) + D * s);
            }
        }

        double min_rank = Double.NEGATIVE_INFINITY, max_rank = Double.POSITIVE_INFINITY;
        for (Double w : ws.values()) {
            if (w < min_rank) min_rank = w;
            if (w > max_rank) max_rank = w;
        }

        for (Map.Entry<Integer, Double> entry : ws.entrySet()) {
            ws.put(entry.getKey(), (entry.getValue() - min_rank / 10.0) / (max_rank - min_rank / 10.0));
        }

        return ws;
    }

    public static class NodeList extends LinkedList<Node> {
        public double sumWeight() {
            double sum = 0.0;
            for (Node node : this) {
                sum += node.getWeight();
            }
            return sum;
        }
    }

    public static class Node {
        private int start;
        private int end;
        private double weight;

        public Node(int start, int end, double weight) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }
}
