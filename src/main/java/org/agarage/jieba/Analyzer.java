package org.agarage.jieba;

import org.agarage.jieba.dictionary.AbstractDictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Nicholas on 2016/7/1.
 */
public class Analyzer {
    private Jieba jieba;
    private Set<String> stopWords = new HashSet<>();
    private Map<String, Double> idf = new HashMap<>();
    private Double midIdf = 0.0;

    public Analyzer(Jieba jieba) {
        this.jieba = jieba;
        loadIDF();
        loadStopWords();
    }

    private void loadIDF() {
        InputStream is = getClass().getResourceAsStream("/idf.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                String[] parts = line.split(" ");
                if (parts.length < 2) continue;
                Double value = Double.parseDouble(parts[1]);
                idf.put(parts[0], value);
            }
            List<Double> idfs = new ArrayList<>(idf.values());
            Collections.sort(idfs);
            midIdf = idfs.get(idfs.size() / 2);
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStopWords() {
        InputStream is = getClass().getResourceAsStream("/stop_words.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while (reader.ready()) {
                String line = reader.readLine().trim();
                if (line.length() == 0) continue;
                stopWords.add(line);
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Word> extractTags(String sentence, int topK) {
        List<Word> result = new LinkedList<>();

        List<org.agarage.jieba.Word> words = jieba.cut(sentence);
        Map<String, Double> freqs = new HashMap<>();
        Map<String, org.agarage.jieba.Word> wordMap = new HashMap<>();

        for (org.agarage.jieba.Word w : words) {
            String wc = w.getWord();
            wordMap.put(wc, w);

            if (wc.length() < 2 || stopWords.contains(wc)) continue;

            freqs.put(wc, freqs.getOrDefault(wc, 0.0) + 1.0);
        }

        double total = 0.0;
        for (Double freq : freqs.values()) {
            total += freq;
        }
        for (Map.Entry<String, Double> entry : freqs.entrySet()) {
            Double freq = entry.getValue();
            freq *= idf.getOrDefault(entry.getKey(), midIdf) / total;
            entry.setValue(freq);
        }

        List<Map.Entry<String, Double>> resultEntries = new ArrayList<>(freqs.entrySet());
        Collections.sort(resultEntries, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });

        for (int i = 0; i < topK && i < resultEntries.size(); i ++) {
            Word word = new Word(wordMap.get(resultEntries.get(i).getKey()));
            word.setWeight(freqs.get(word.getWord()));
            result.add(word);
        }

        return result;
    }

    public static class Word extends org.agarage.jieba.Word {
        private Double weight;

        public Word(int freq) {
            super(freq);
        }

        public Word(org.agarage.jieba.Word word) {
            super(0);
            setWord(word.getWord());
            setFlag(word.getFlag());
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        @Override
        public String toString() {
            return super.toString() + " " + this.weight;
        }
    }
}
