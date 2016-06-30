package org.agarage.jieba;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nicholas on 2016/6/30.
 */
public abstract class AbstractCutter {
    protected Dictionary dictionary;

    public AbstractCutter(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public DAG getDag(String sentence) {
        if (!dictionary.isLoaded()) dictionary.load();
        DAG dag = new DAG();
        for (int k = 0; k < sentence.length(); k ++) {
            DAG.Node tmplist = dag.get(k);
            if (tmplist == null) {
                tmplist = new DAG.Node();
                dag.put(k, tmplist);
            }
            StringBuilder frag = new StringBuilder(sentence.substring(k, k + 1));
            int i = k;
            while (i < sentence.length() && dictionary.hasWord(frag.toString())) {
                if (dictionary.getFreq(frag.toString()) > 0) {
                    tmplist.add(i);
                }
                i += 1;
                if (i < sentence.length()) frag.append(sentence.charAt(i));
            }
            if (tmplist.isEmpty()) {
                tmplist.add(k);
            }
        }
        return dag;
    }

    public Map<Integer, Pair<Double, Integer>> calc(String sentence, DAG dag) {
        int N = sentence.length();
        sentence = sentence.toLowerCase();
        Map<Integer, Pair<Double, Integer>> route = new HashMap<>();
        route.put(N, new Pair<Double, Integer>(0.0, 0));
        double logtotal = Math.log(dictionary.getTotal());
        for (int idx = N - 1; idx >= 0; idx --) {
            double r = Double.NEGATIVE_INFINITY;
            Integer maxX = 0;
            for (Integer x : dag.get(idx)) {
                int freq = dictionary.getFreq(sentence.substring(idx, x + 1), 0);
                if (freq == 0) freq = 1;
                double tmp = Math.log(freq) - logtotal + route.get(x + 1).getKey();
                if (tmp > r) {
                    r = tmp;
                    maxX = x;
                }
            }
            route.put(idx, new Pair<>(r, maxX));
        }
        return  route;
    }

    public abstract List<String> cut(String block);
}
