package org.agarage.jieba;

import javafx.util.Pair;
import org.agarage.jieba.dictionary.AbstractDictionary;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Nicholas on 2016/6/30.
 */
public class DAGTokenizer extends AbstractTokenizer {
    private FinalSeg finalSeg;

    public DAGTokenizer(AbstractDictionary dictionary) {
        super(dictionary);
        finalSeg = new FinalSeg(dictionary);
    }

    private FinalSeg getFinalSeg() {
        return finalSeg;
    }

    @Override
    public List<Word> cut(String block) {
        if (!dictionary.isLoaded()) dictionary.load();
        List<Word> result = new LinkedList<>();
        DAG dag = getDag(block);
        Map<Integer, Pair<Double, Integer>> route = calc(block, dag);
        int x = 0;
        StringBuilder buf = new StringBuilder();
        int N = block.length();
        while (x < N) {
            int y = route.get(x).getValue() + 1;
            String l_word = block.substring(x, y);
            if (y - x == 1) {
                buf.append(l_word);
            } else {
                if (buf.length() > 0) {
                    if (buf.length() == 1) {
                        addResult(result, buf.toString());
                        buf = new StringBuilder();
                    } else {
                        if (dictionary.getFreq(buf.toString(), 0) == 0) {
                            result.addAll(getFinalSeg().cut(buf.toString()));
                        } else {
                            for (int i = 0; i < buf.length(); i ++) {
                                addResult(result, String.valueOf(buf.charAt(i)));
                            }
                        }
                        buf = new StringBuilder();
                    }
                }
                addResult(result, l_word);
            }
            x = y;
        }
        try {
            if (buf.length() > 0) {
                if (buf.length() == 1) {
                    addResult(result, buf.toString());
                } else if (dictionary.getFreq(buf.toString(), 0) == 0) {
                    result.addAll(getFinalSeg().cut(buf.toString()));
                } else {
                    for (int i = 0; i < buf.length(); i ++) {
                        addResult(result, String.valueOf(buf.charAt(i)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
