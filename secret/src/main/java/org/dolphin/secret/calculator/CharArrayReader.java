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

    public static class Builder {
        private char[] chars;
        private int offset;

        public Builder() {
            chars = new char[4096];
            offset = 0;
        }

        public Builder append(char data) {
            chars[offset++] = data;
            return this;
        }

        public CharArrayReader build() {
            int count = offset;
            char buff[] = new char[count];
            for (int i = 0; i < count; ++i) {
                buff[i] = chars[i];
            }
            return new CharArrayReader(buff);
        }
    }
}
