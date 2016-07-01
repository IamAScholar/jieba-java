package org.agarage.jieba;

import javafx.util.Pair;
import org.agarage.jieba.dictionary.AbstractDictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FinalSeg {
    private static FinalSeg singleInstance;
    private static final String PROB_EMIT = "/prob_emit.txt";
    private static char[] states = new char[] { 'B', 'M', 'E', 'S' };
    private static Map<Character, Map<Character, Double>> emit;
    private static Map<Character, Double> start;
    private static Map<Character, Map<Character, Double>> trans;
    private static Map<Character, char[]> prevStatus;
    private static Double MIN_FLOAT = -3.14e100;;
    private final static Pattern patSkip = Pattern.compile("(\\d+\\.\\d+|[a-zA-Z0-9]+)");

    private AbstractDictionary dictionary;


    public FinalSeg(AbstractDictionary dictionary) {
        this.dictionary = dictionary;
        loadModel();
    }

    private synchronized void loadModel() {
        long s = System.currentTimeMillis();
        prevStatus = new HashMap<Character, char[]>();
        prevStatus.put('B', new char[] { 'E', 'S' });
        prevStatus.put('M', new char[] { 'M', 'B' });
        prevStatus.put('S', new char[] { 'S', 'E' });
        prevStatus.put('E', new char[] { 'B', 'M' });

        start = new HashMap<Character, Double>();
        start.put('B', -0.26268660809250016);
        start.put('E', -3.14e+100);
        start.put('M', -3.14e+100);
        start.put('S', -1.4652633398537678);

        trans = new HashMap<Character, Map<Character, Double>>();
        Map<Character, Double> transB = new HashMap<Character, Double>();
        transB.put('E', -0.510825623765990);
        transB.put('M', -0.916290731874155);
        trans.put('B', transB);
        Map<Character, Double> transE = new HashMap<Character, Double>();
        transE.put('B', -0.5897149736854513);
        transE.put('S', -0.8085250474669937);
        trans.put('E', transE);
        Map<Character, Double> transM = new HashMap<Character, Double>();
        transM.put('E', -0.33344856811948514);
        transM.put('M', -1.2603623820268226);
        trans.put('M', transM);
        Map<Character, Double> transS = new HashMap<Character, Double>();
        transS.put('B', -0.7211965654669841);
        transS.put('S', -0.6658631448798212);
        trans.put('S', transS);

        InputStream is = this.getClass().getResourceAsStream(PROB_EMIT);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            emit = new HashMap<Character, Map<Character, Double>>();
            Map<Character, Double> values = null;
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("\t");
                if (tokens.length == 1) {
                    values = new HashMap<Character, Double>();
                    emit.put(tokens[0].charAt(0), values);
                }
                else {
                    values.put(tokens[0].charAt(0), Double.valueOf(tokens[1]));
                }
            }
        }
        catch (IOException e) {
            System.err.println(String.format(Locale.getDefault(), "%s: load model failure!", PROB_EMIT));
        }
        finally {
            try {
                if (null != is)
                    is.close();
            }
            catch (IOException e) {
                System.err.println(String.format(Locale.getDefault(), "%s: close failure!", PROB_EMIT));
            }
        }
        System.out.println(String.format(Locale.getDefault(), "model load finished, time elapsed %d ms.",
            System.currentTimeMillis() - s));
    }


    public List<Word> cut(String sentence) {
        List<Word> result = new LinkedList<Word>();
        List<String> blocks = CharacterUtils.split(sentence);
        for (String block : blocks) {
            if (CharacterUtils.isChinese(block)) {
                //cut
                Pair<Double, List<Character>> viterbiResult = viterbi(block, "BMES");
                double prob = viterbiResult.getKey();
                List<Character> pos_list = viterbiResult.getValue();
                int begin = 0, nexti = 0;
                for (int i = 0; i < block.length(); i ++) {
                    char pos = pos_list.get(i);
                    switch (pos) {
                        case 'B':
                            begin = i;
                            break;
                        case 'E':
                            addResult(result, block.substring(begin, i + 1));
                            nexti = i + 1;
                            break;
                        case 'S':
                            addResult(result, block.substring(i, i + 1));
                            nexti = i + 1;
                            break;
                    }
                }
                if (nexti < block.length()) {
                    addResult(result, block.substring(nexti));
                }
            } else {
                List<String> tmp = CharacterUtils.split(block, patSkip);
                for (String str : tmp) {
                    if (str.length() > 0) {
                        if (CharacterUtils.isNumber(str)) {
                            addResult(result, str, WordFlag.M);
                        } else if (CharacterUtils.isEnglish(str)) {
                            addResult(result, str, WordFlag.ENG);
                        } else {
                            addResult(result, str);
                        }
                    }
                }
            }
        }
        return result;
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

    public Pair<Double, List<Character>> viterbi(String sentence, String states) {
        List<Map<Character, Double>> vectors = new LinkedList<Map<Character, Double>>();
        Map<Character, List<Character>> path = new HashMap<Character, List<Character>>();
        Map<Character, Double> v = new HashMap<Character, Double>();
        vectors.add(v);
        for (int i = 0; i < states.length(); i ++) {
            Character y = states.charAt(i);
            v.put(y, start.get(y) + emit.get(y).getOrDefault(sentence.charAt(0), MIN_FLOAT));
            List<Character> p = new LinkedList<>();
            p.add(y);
            path.put(y, p);
        }
        for (int t = 1; t < sentence.length(); t ++) {
            vectors.add(new HashMap<>());
            Map<Character, List<Character>> newpath = new HashMap<>();
            for (int i = 0; i < states.length(); i ++) {
                Character y = states.charAt(i);
                double em_p = emit.get(y).getOrDefault(sentence.charAt(t), MIN_FLOAT);
                double prob = Double.NEGATIVE_INFINITY;
                Character state = null;
                char[] ps = prevStatus.get(y);
                for (int j = 0; j < ps.length; j ++) {
                    char y0 = ps[j];
                    double tmp = vectors.get(t - 1).get(y0) + trans.get(y0).getOrDefault(y, MIN_FLOAT) + em_p;
                    if (tmp > prob) {
                        prob = tmp;
                        state = y0;
                    }
                }
                vectors.get(t).put(y, prob);
                List<Character> tmp = new LinkedList<>(path.get(state));
                tmp.add(y);
                newpath.put(y, tmp);
            }
            path = newpath;
        }

        double probE = vectors.get(sentence.length() - 1).get('E');
        double probS = vectors.get(sentence.length() - 1).get('S');
        if (probE > probS) {
            return new Pair<>(probE, path.get('E'));
        } else {
            return new Pair<>(probS, path.get('S'));
        }
    }
}
