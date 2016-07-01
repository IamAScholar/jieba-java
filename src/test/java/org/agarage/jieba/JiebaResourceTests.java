package org.agarage.jieba;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Nicholas on 2016/7/1.
 */
public class JiebaResourceTests extends TestCase {
    @Test
    public void testWordTags() {
        InputStream is = Jieba.class.getResourceAsStream("/dict.txt.mid");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            Set<String> tags = new HashSet<String>();
            while (reader.ready()) {
                String line = reader.readLine();
                String[] tokens = line.split("[\t ]");

                if (!tags.contains(tokens[2])) {
                    tags.add(tokens[2]);
                }
            }
            for (String tag : tags) {
                System.out.print(tag.toUpperCase() + ", ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCharStateTable() {
        PositionSegmenter posseg = PositionSegmenter.getInstance();
        System.out.println("PositionSegmenter loaded!");
    }
}
