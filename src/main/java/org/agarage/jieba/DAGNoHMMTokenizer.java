package org.agarage.jieba;

import javafx.util.Pair;
import org.agarage.jieba.dictionary.AbstractDictionary;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Nicholas on 2016/7/1.
 */
public class DAGNoHMMTokenizer extends AbstractTokenizer {
    private final static Pattern patEng = Pattern.compile("[a-zA-Z0-9]");

    public DAGNoHMMTokenizer(AbstractDictionary dictionary) {
        super(dictionary);
    }

    @Override
    public List<Word> cut(String block) {
        List<Word> result = new LinkedList<>();
        DAG dag = getDag(block);
        Map<Integer, Pair<Double, Integer>> route = calc(block, dag);
        int x = 0;
        StringBuilder buf = new StringBuilder();
        int N = block.length();
        while (x < N) {
            int y = route.get(x).getValue() + 1;
            String l_word = block.substring(x, y);
            if (patEng.matcher(l_word).matches() && l_word.length() == 1) {
                buf.append(l_word);
                x = y;
            } else {
                if (buf.length() > 0) {
                    addResult(result, buf.toString());
                    buf = new StringBuilder();
                }
                addResult(result, l_word);
                x = y;
            }
        }
        if (buf.length() > 0) {
            addResult(result, buf.toString());
        }
        return result;
    }
}
