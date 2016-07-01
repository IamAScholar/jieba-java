package org.agarage.jieba.dictionary;

import org.agarage.jieba.Trie;
import org.agarage.jieba.Word;

/**
 * Created by Nicholas on 2016/7/1.
 */
public class TrieDictionary extends AbstractDictionary {
    private Trie<Word> words = new Trie<>();

    @Override
    protected void putWord(String word, Word info) {
        words.put(word, info);
    }

    @Override
    public boolean hasWord(String word) {
        Word info = words.get(word, null);
        return info != null;
    }

    @Override
    public Word getWord(String word) {
        return words.get(word);
    }
}
