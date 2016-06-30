package org.agarage.jieba;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Nicholas on 2016/6/30.
 */
public class Dictionary {
    private final static String MAIN_DICT = "/dict.txt";
    private final static String USER_DICT_SUFFIX = ".dict";

    private Set<String> loadedPath = new HashSet<>();

    private File dict = null;

    private Map<String, Integer> freq = new HashMap<String, Integer>();
    private int total = 0;

    private boolean loaded = false;

    public void loadDir(Path path) {
        if (!isLoaded()) {
            load();
        }
        String abspath = path.toAbsolutePath().toString();
        System.out.println("initialize user dictionary:" + abspath);
        synchronized (Dictionary.class) {
            if (loadedPath.contains(abspath))
                return;

            DirectoryStream<Path> stream;
            try {
                stream = Files.newDirectoryStream(path, String.format(Locale.getDefault(), "*%s", USER_DICT_SUFFIX));
                for (Path file: stream){
                    System.err.println(String.format(Locale.getDefault(), "loading dict %s", file.toString()));
                    loadUserDict(file);
                }
                loadedPath.add(abspath);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                System.err.println(String.format(Locale.getDefault(), "%s: load user dict failure!", path.toString()));
            }
        }
    }

    public void loadUserDict(Path userDict) {
        loadUserDict(userDict, StandardCharsets.UTF_8);
    }

    public void loadUserDict(Path userDict, Charset charset) {
        try {
            BufferedReader br = Files.newBufferedReader(userDict, charset);
            long s = System.currentTimeMillis();
            int count = 0;
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 1) {
                    // Ignore empty line
                    continue;
                }

                String word = tokens[0];
                Integer freq = Integer.parseInt(tokens[1]);
                addWord(word, freq);
                count ++;

            }
            System.out.println(String.format(Locale.getDefault(), "user dict %s load finished, tot words:%d, time elapsed:%dms", userDict.toString(), count, System.currentTimeMillis() - s));
            br.close();
        }
        catch (IOException e) {
            System.err.println(String.format(Locale.getDefault(), "%s: load user dict failure!", userDict.toString()));
        }
    }

    private void addWord(String word, Integer freq) {
        Integer originFreq = this.freq.get(word);
        if (originFreq != null) {
            total -= originFreq;
        }
        this.freq.put(word, freq);
        total += freq;

        for (int i = 0; i < word.length(); i ++) {
            String wfrag = word.substring(0, i + 1);
            if (!hasWord(wfrag)) {
                this.freq.put(wfrag, 0);
            }
        }
    }

    public void load() {
        InputStream is = getClass().getResourceAsStream(MAIN_DICT);
        load(is);
    }

    public void load(InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            long time = System.currentTimeMillis();
            while (reader.ready()) {
                String line = reader.readLine();
                String[] parts = line.split("[\t ]+");

                if (parts.length < 2) continue;

                String word = parts[0];
                Integer f = Integer.parseInt(parts[1]);
                freq.put(word, f);
                total += f;
                for (int i = 0; i < word.length(); i ++) {
                    String frag = word.substring(0, i + 1);
                    if (!freq.containsKey(frag)) {
                        freq.put(frag, 0);
                    }
                }
            }
            loaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean hasWord(String word) {
        return freq.containsKey(word);
    }

    public Integer getFreq(String word) {
        return freq.get(word);
    }

    public Integer getFreq(String word, Integer def) {
        return freq.getOrDefault(word, def);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getTotal() {
        return total;
    }
}
