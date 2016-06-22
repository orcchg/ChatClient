package com.orcchg.chatclient.data.parser;

import android.util.MalformedJsonException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Response {
    public static final int TERMINATE_CODE = 99;

    public static class Codeline {
        private int version;
        private int code;
        private String message;

        public int getVersion() {
            return version;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return new StringBuilder("HTTP/1.")
                    .append(version).append(' ')
                    .append(code).append(' ')
                    .append(message).append("\r\n")
                    .toString();
        }
    }

    private Codeline mCodeline;
    private List<Header> mHeaders;
    private String mBody;

    public Codeline getCodeline() {
        return mCodeline;
    }

    public List<Header> getHeaders() {
        return mHeaders;
    }

    public String getBody() {
        return mBody;
    }

    /* Parse */
    // --------------------------------------------------------------------------------------------
    public static Response parse(char[] buffer) throws ParseException, MalformedJsonException {
        return parse(new String(buffer));
    }

    public static Response parse(String line) throws ParseException, MalformedJsonException {
        Timber.v("Line to parse: %s", line);
        String[] tokens = line.split("\\r?\\n");
        if (tokens.length <= 0) {
            String error = "Parse error: invalid line: " + line;
            throw new ParseException(error, 0);
        }

        Codeline codeline = parseCodeline(tokens[0]);
        List<Header> headers = new ArrayList<>();

        int index = 1;
        while (Header.isHeader(tokens[index])) {
            Header header = Header.parse(tokens[index]);
            headers.add(header);
            ++index;
        }

        String ending = "";
        StringBuilder bodyBuilder = new StringBuilder();
        while (index < tokens.length) {
            String bodyLine = tokens[index].replaceAll("\r+", "");
            bodyBuilder.append(ending).append(bodyLine);
            ending = "\n";
            ++index;
        }

        String body = bodyBuilder.toString();
        int i1 = body.lastIndexOf("}");
        if (i1 > 0) {
            body = body.substring(0, i1 + 2);
        } else {
            String error = "Malformed json body: " + line;
            throw new MalformedJsonException(error);
        }

        Response response = new Response();
        response.mCodeline = codeline;
        response.mHeaders = headers;
        response.mBody = body;
        return response;
    }

    public static Codeline parseCodeline(String line) throws ParseException {
        Timber.v("Line to parse: %s", line);
        line = line.replaceAll("\\s+", " ").trim();
        int i1 = line.indexOf("HTTP");
        int i2 = line.indexOf(' ', i1 + 8);
        int i3 = line.indexOf(' ', i2 + 1);
        if (i1 < 0 || i2 < 0 || i3 < 0) {
            String error = "Parse error: invalid code line: " + line;
            throw new ParseException(error, 0);
        }
        Codeline codeline = new Codeline();
        codeline.version = Integer.parseInt(line.substring(i1 + 7, i1 + 8));
        codeline.code = Integer.parseInt(line.substring(i2 + 1, i3));
        codeline.message = line.substring(i3 + 1);
        return codeline;
    }
}
