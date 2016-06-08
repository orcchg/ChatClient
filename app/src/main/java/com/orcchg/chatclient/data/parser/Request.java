package com.orcchg.chatclient.data.parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Request {

    public static class Startline {
        String method;
        String path;
        int version;

        @Override
        public String toString() {
            return new StringBuilder(method)
                    .append(' ').append(path)
                    .append(' ').append("HTTP/1.")
                    .append(version).append("\r\n")
                    .toString();
        }
    }

    private Startline mStartline;
    private List<Header> mHeaders;
    private String mBody;

    public Startline getStartline() {
        return mStartline;
    }

    public List<Header> getHeaders() {
        return mHeaders;
    }

    public String getBody() {
        return mBody;
    }

    /* Parse */
    // --------------------------------------------------------------------------------------------
    public static Request parse(String line) throws ParseException {
        String[] tokens = line.split("\\r?\\n");
        if (tokens.length <= 0) {
            String error = "Parse error: invalid line: " + line;
            throw new ParseException(error, 0);
        }

        Startline startline = parseStartLine(tokens[0]);
        List<Header> headers = new ArrayList<>();

        int index = 1;
        while (Header.isHeader(tokens[index])) {
            Header header = Header.parse(tokens[index]);
            headers.add(header);
            ++index;
        }

        String ending = "";
        StringBuilder body = new StringBuilder();
        while (index < tokens.length) {
            String bodyLine = tokens[index].replaceAll("\r+", "");
            body.append(ending).append(bodyLine);
            ending = "\n";
            ++index;
        }

        Request request = new Request();
        request.mStartline = startline;
        request.mHeaders = headers;
        request.mBody = body.toString();
        return request;
    }

    public static Startline parseStartLine(String line) throws ParseException {
        line = line.replaceAll("\\s+", " ").trim();
        int i1 = line.indexOf(' ');
        int i2 = line.indexOf("HTTP", i1 + 1);
        if (i1 < 0 || i2 < 0) {
            String error = "Parse error: invalid start line: " + line;
            throw new ParseException(error, 0);
        }
        Startline startline = new Startline();
        startline.method = line.substring(0, i1);
        startline.path = line.substring(i1 + 1, i2 - 1);
        startline.version = Integer.parseInt(line.substring(i2 + 7));
        return startline;
    }
}
