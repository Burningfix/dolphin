package org.dolphin.secret.calculator;

/**
 * Created by hanyanan on 2016/7/6.
 */
public class CharArrayReader {
    private int index = 0;
    private final char[] chars;

    public CharArrayReader(char[] chars) {
        this.chars = chars;
    }

    public boolean ready() {
        return index < chars.length;
    }

    public char shift() {
        return chars[index++];
    }

    public void unshift() {
        index--;
        index = index < 0 ? 0 : index;
    }
}
