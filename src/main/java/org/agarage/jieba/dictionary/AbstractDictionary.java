package org.agarage.jieba.dictionary;

import org.agarage.jieba.Word;
import org.agarage.jieba.WordFlag;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Nicholas on 2016/7/1.
 */
public abstract class AbstractDictionary {
    protected final static String MAIN_DICT = "/dict.txt.mid";
    protected final static String USER_DICT_SUFFIX = ".dict";
    protected final static Word DEFAULT_WORD = new Word(0);

    private Set<String> loadedPath = new HashSet<>();

    private int total = 0;

    private boolean loaded = false;

    public void loadDir(Path path) {
        if (!isLoaded()) {
            load();
        }
        String abspath = path.toAbsolutePath().toString();
        System.out.println("initialize user dictionary:" + abspath);
        synchronized (this) {
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
                addLine(line);
                count ++;

            }
            System.out.println(String.format(Locale.getDefault(), "user dict %s load finished, tot words:%d, time elapsed:%dms", userDict.toString(), count, System.currentTimeMillis() - s));
            br.close();
        }
        catch (IOException e) {
            System.err.println(String.format(Locale.getDefault(), "%s: load user dict failure!", userDict.toString()));
        }
    }

    public void addWord(String word, Word info) {
        Word originInfo = getWord(word);
        if (originInfo != null) {
            total -= originInfo.getFreq();
        }
        putWord(word, info);
        total += info.getFreq();

        for (int i = 0; i < word.length(); i ++) {
            String wfrag = word.substring(0, i + 1);
            if (!hasWord(wfrag)) {
                putWord(wfrag, DEFAULT_WORD.clone());
            }
        }
    }

    protected abstract void putWord(String word, Word info);

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
                addLine(line);
            }
            loaded = true;

            System.out.println("main dict loaded from disk within " + String.valueOf(System.currentTimeMillis() - time) + " ms.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addLine(String line) {
        String[] parts = line.split("[\t ]+");

        if (parts.length < 2) return;

        String word = parts[0];
        Integer f = Integer.parseInt(parts[1]);
        Word info = new Word(f);
        if (parts.length > 2) {
            info.setFlag(WordFlag.valueOf(parts[2].toUpperCase()));
        }
        addWord(word, info);
    }

    public Integer getFreq(String word) {
        return getFreq(word, 0);
    }

    public Integer getFreq(String word, Integer def) {
        Word info = getWord(word);
        if (info == null) {
            return def;
        }
        return info.getFreq();
    }

    public abstract boolean hasWord(String word);

    public abstract Word getWord(String word);

    public boolean isLoaded() {
        return loaded;
    }

    public int getTotal() {
        return total;
    }
}
