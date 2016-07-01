package org.agarage.jieba;

import org.agarage.jieba.dictionary.AbstractDictionary;
import org.agarage.jieba.dictionary.MapDictionary;
import org.agarage.jieba.dictionary.TrieDictionary;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nicholas on 2016/6/30.
 */
public class Jieba {
    private final static Pattern patHanDefault = Pattern.compile("([\u4E00-\u9FD5a-zA-Z0-9+#&\\._]+)");
    private final static Pattern patSkipDefault = Pattern.compile("(\r\n|\\s)");
    private final static Pattern patHanAll = Pattern.compile("([\u4E00-\u9FD5]+)");
    private final static Pattern patSkipAll = Pattern.compile("[^a-zA-Z0-9+#\n]");

    private AbstractDictionary dictionary;
    private DAGCutter dagCutter;
    private DAGNoHMMCutter dagNoHMMCutter;
    private AllCutter allCutter;

    private Analyzer analyzer = null;

    public Analyzer getAnalyzer() {
        if (analyzer == null) {
            analyzer = new Analyzer(this);
        }
        return analyzer;
    }

    private Jieba(AbstractDictionary dictionary) {
        this.dictionary = dictionary;
        this.dagCutter = new DAGCutter(dictionary);
        this.allCutter = new AllCutter(dictionary);
        this.dagNoHMMCutter = new DAGNoHMMCutter(dictionary);
    }

    public static Jieba create() {
//        AbstractDictionary dictionary = new MapDictionary();
        AbstractDictionary dictionary = new TrieDictionary();
        dictionary.loadDir(Paths.get("dict"));
        return new Jieba(dictionary);
    }

    public List<Word> cut(String sentence, boolean hmm) {
        return cut(sentence, false, hmm);
    }

    public List<Word> cut(String sentence) {
        return cut(sentence, false, true);
    }

    public List<Word> cut(String sentence, boolean cutAll, boolean hmm) {
        Pattern patHan, patSkip;
        AbstractCutter cutter;
        if (cutAll) {
            patHan = patHanAll;
            patSkip = patSkipAll;
            cutter = allCutter;
        } else {
            patHan = patHanDefault;
            patSkip = patSkipDefault;
            if (hmm) {
                cutter = dagCutter;
            } else {
                cutter = dagNoHMMCutter;
            }
        }
        List<Word> result = new LinkedList<Word>();
        List<String> blocks = CharacterUtils.split(sentence, patHan);
        for (String block : blocks) {
            if (block.length() == 0) {
                continue;
            }
            if (patHan.matcher(block).matches()) {
                result.addAll(cutter.cut(block));
            } else {
                String[] tmp = patSkip.split(block);
                for (String str : tmp) {
                    if (patSkip.matcher(str).matches()) {
                        addNonChineseResult(result, str);
                    } else if (!cutAll) {
                        for (int i = 0; i < str.length(); i ++) {
                            String w = str.substring(i, i + 1);
                            addNonChineseResult(result, w);
                        }
                    } else {
                        addNonChineseResult(result, str);
                    }
                }
            }
        }
        return result;
    }

    private void addNonChineseResult(List<Word> result, String word) {
        if (CharacterUtils.isNumber(word)) {
            addResult(result, word, WordFlag.M);
        } else if (CharacterUtils.isEnglish(word)) {
            addResult(result, word, WordFlag.ENG);
        } else {
            addResult(result, word);
        }
    }

    private void addResult(List<Word> result, String word) {
        addResult(result, word, WordFlag.X);
    }

    private void addResult(List<Word> result, String word, WordFlag flag) {
        Word info = dictionary.getWord(word);
        if (info == null) {
            info = new Word(0);
            info.setFlag(flag);
        }
        info.setWord(word);
        result.add(info);
    }

    public List<Word> cutForSearch(String sentence) {
        return cutForSearch(sentence, true);
    }

    public List<Word> cutForSearch(String sentence, boolean hmm) {
        List<Word> result = new LinkedList<>();
        List<Word> words = cut(sentence, false, hmm);
        for (Word wordObj : words) {
            String word = wordObj.getWord();
            if (word.length() > 2) {
                for (int i = 0; i < word.length() - 1; i ++) {
                    String gram2 = word.substring(i, i + 2);
                    if (dictionary.getFreq(gram2, 0) > 0) {
                        addResult(result, gram2);
                    }
                }
            }
            if (word.length() > 3) {
                for (int i = 0; i < word.length() - 2; i ++) {
                    String gram3 = word.substring(i, i + 3);
                    if (dictionary.getFreq(gram3, 0) > 0) {
                        addResult(result, gram3);
                    }
                }
            }
            addResult(result, word);
        }
        return result;
    }
}
