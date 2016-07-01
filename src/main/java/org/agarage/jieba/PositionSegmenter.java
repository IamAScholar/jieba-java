package org.agarage.jieba;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nicholas on 2016/7/2.
 */
public class PositionSegmenter {
    private Map<Character, List<String>> charStateTable = new HashMap<>();
    private Map<String, Map<Character, Double>> emitProb = new HashMap<>();
    private Map<String, Double> startProb = new HashMap<>();
    private Map<String, Map<String, Double>> transProb = new HashMap<>();

    private static PositionSegmenter instance = null;

    public static PositionSegmenter getInstance() {
        if (instance == null) {
            instance = new PositionSegmenter();
        }
        return instance;
    }

    private PositionSegmenter() {
        loadCharStateTable();
        loadEmitProb();
        loadStartProb();
        loadTransProb();
    }

    private void loadCharStateTable() {
        InputStream is = getClass().getResourceAsStream("/posseg/char_state_tab.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.trim().length() == 0) continue;

                String[] parts = line.split(" ");
                if (parts.length < 2) continue;
                if (parts[0].length() < 1) continue;

                String[] statePairs = parts[1].split(",");
                List<String> pairs = new ArrayList<>(statePairs.length);
                charStateTable.put(parts[0].charAt(0), pairs);
                for (String pairStr : statePairs) {
                    if (pairStr.length() < 3) continue;
                    pairs.add(pairStr);
                }
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadEmitProb() {
        InputStream is = getClass().getResourceAsStream("/posseg/prob_emit.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                String[] parts = line.split(" ");
                if (parts.length < 2) continue;

                String[] weightStrs = parts[1].split(",");
                Map<Character, Double> weightMap = new HashMap<>();
                emitProb.put(parts[0], weightMap);
                for (String weightStr : weightStrs) {
                    if (weightStr.length() < 3) continue;
                    weightMap.put(weightStr.charAt(0), Double.parseDouble(weightStr.substring(2)));
                }
            }

            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStartProb() {
        InputStream is = getClass().getResourceAsStream("/posseg/prob_start.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                String[] parts = line.split(" ");
                if (parts.length < 2) continue;
                if (parts[0].length() < 3) continue;

                startProb.put(parts[0], Double.parseDouble(parts[1]));
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTransProb() {
        InputStream is = getClass().getResourceAsStream("/posseg/prob_trans.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                String[] parts = line.split(" ");
                if (parts.length < 2) continue;
                if (parts[0].length() < 3) continue;

                String[] weightStrs = parts[1].split(",");
                Map<String, Double> weightMap = new HashMap<>();
                transProb.put(parts[0], weightMap);
                for (String weightStr : weightStrs) {
                    if (weightStr.length() == 0) continue;
                    String[] tokens = weightStr.split("=");
                    if (tokens.length < 2) continue;
                    if (tokens[0].length() < 3) continue;
                    if (tokens[1].length() == 0) continue;

                    weightMap.put(tokens[0], Double.parseDouble(tokens[1]));
                }
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
