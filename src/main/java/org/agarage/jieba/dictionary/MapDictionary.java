package org.agarage.jieba.dictionary;

import org.agarage.jieba.Word;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nicholas on 2016/7/1.
 */
public class MapDictionary extends AbstractDictionary {
    private Map<String, Word> words = new HashMap<>();

    @Override
    protected void putWord(String word, Word info) {
        words.put(word, info);
    }

    @Override
    public boolean hasWord(String word) {
        return words.containsKey(word);
    }

    @Override
    public Word getWord(String word) {
        return words.get(word);
    }
}
