package org.agarage.jieba;

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

    private Dictionary dictionary;
    private DAGCutter dagCutter;
    private DAGNoHMMCutter dagNoHMMCutter;
    private AllCutter allCutter;

    private Jieba(Dictionary dictionary) {
        this.dictionary = dictionary;
        this.dagCutter = new DAGCutter(dictionary);
        this.allCutter = new AllCutter(dictionary);
        this.dagNoHMMCutter = new DAGNoHMMCutter(dictionary);
    }

    public static Jieba create() {
        Dictionary dictionary = new Dictionary();
        dictionary.loadDir(Paths.get("dict"));
        return new Jieba(new Dictionary());
    }

    public List<String> cut(String sentence, boolean hmm) {
        return cut(sentence, false, hmm);
    }

    public List<String> cut(String sentence) {
        return cut(sentence, false, true);
    }

    public List<String> cut(String sentence, boolean cutAll, boolean hmm) {
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
        List<String> result = new LinkedList<String>();
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
                        result.add(str);
                    } else if (!cutAll) {
                        for (int i = 0; i < str.length(); i ++) {
                            result.add(str.substring(i, i + 1));
                        }
                    } else {
                        result.add(str);
                    }
                }
            }
        }
        return result;
    }

    public List<String> cutForSearch(String sentence) {
        return cutForSearch(sentence, true);
    }

    public List<String> cutForSearch(String sentence, boolean hmm) {
        List<String> result = new LinkedList<>();
        List<String> words = cut(sentence, false, hmm);
        for (String word : words) {
            if (word.length() > 2) {
                for (int i = 0; i < word.length() - 1; i ++) {
                    String gram2 = word.substring(i, i + 2);
                    if (dictionary.getFreq(gram2, 0) > 0) {
                        result.add(gram2);
                    }
                }
            }
            if (word.length() > 3) {
                for (int i = 0; i < word.length() - 2; i ++) {
                    String gram3 = word.substring(i, i + 3);
                    if (dictionary.getFreq(gram3, 0) > 0) {
                        result.add(gram3);
                    }
                }
            }
            result.add(word);
        }
        return result;
    }
}
