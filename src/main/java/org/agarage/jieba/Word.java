package org.agarage.jieba;

/**
 * Created by Nicholas on 2016/7/1.
 */
public class Word implements Cloneable {
    private String word;
    private int freq = 0;
    private WordFlag flag = WordFlag.X;

    public Word(int freq) {
        this.freq = freq;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    @Override
    public Word clone() {
        try {
            return (Word) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public WordFlag getFlag() {
        return flag;
    }

    public void setFlag(WordFlag flag) {
        this.flag = flag;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return word + ":" + flag;
    }
}
