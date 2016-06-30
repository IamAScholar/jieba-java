package org.agarage.jieba;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Nicholas on 2016/6/30.
 */
public class AllCutter extends AbstractCutter {
    public AllCutter(Dictionary dict) {
        super(dict);
    }

    public List<String> cut(String block) {
        DAG dag = getDag(block);
        List<String> result = new LinkedList<String>();
        int old_j = -1;
        for (Map.Entry<Integer, DAG.Node> entry : dag.entrySet()) {
            if (entry.getValue().size() == 1 && entry.getKey() > old_j) {
                result.add(block.substring(entry.getKey(), entry.getValue().get(0) + 1));
                old_j = entry.getValue().get(0);
            } else {
                for (Integer j : entry.getValue()) {
                    if (j > entry.getKey()) {
                        result.add(block.substring(entry.getKey(), j + 1));
                        old_j = j;
                    }
                }
            }
        }
        return result;
    }
}
