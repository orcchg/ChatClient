package com.orcchg.chatclient.data.parser;

import java.text.ParseException;

public class Header {
    String mKey;
    String mValue;

    public Header(String key, String value) {
        mKey = key;
        mValue = value;
    }

    public String getKey() {
        return mKey;
    }

    public String getValue() {
        return mValue;
    }

    public static boolean isHeader(String line) {
        return line.indexOf(':') >= 0;
    }

    @Override
    public String toString() {
        return new StringBuilder("Header{").append(mKey).append(": ").append(mValue).toString();
    }

    /* Parse */
    // --------------------------------------------------------------------------------------------
    public static Header parse(String line) throws ParseException {
        line = line.replaceAll("\\s+", " ").trim();
        int colon = line.indexOf(':');
        if (colon < 0) {
            String error = "Parse error: invalid header: " + line;
            throw new ParseException(error, colon);
        }
        String[] tokens = line.split(":");
        return new Header(tokens[0].trim(), tokens[1].trim());
    }
}
