package org.agarage.jieba;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nicholas on 2016/6/30.
 */
public class CharacterUtils {
    private final static Pattern patHanAll = Pattern.compile("[\u4E00-\u9FD5]+");

    public static boolean isChinese(String str) {
        return patHanAll.matcher(str).matches();
    }

    public static List<String> split(String str, Pattern pat) {
        List<String> result = new LinkedList<String>();
        Matcher matcher = pat.matcher(str);
        String[] parts = str.split(pat.pattern());
        for (String part : parts) {
            result.add(part);
            if (matcher.find()) {
                result.add(matcher.group());
            }
        }
        if (parts.length == 0 && matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    public static List<String> split(String str) {
        return split(str, patHanAll);
    }
}
