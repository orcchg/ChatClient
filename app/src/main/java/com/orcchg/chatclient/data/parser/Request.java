package com.orcchg.chatclient.data.parser;

import android.util.MalformedJsonException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Request {

    public static class Startline {
        private String method;
        private String path;
        private int version;

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public int getVersion() {
            return version;
        }

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(mStartline.toString());
        for (Header header : mHeaders) {
            builder.append(header.toString());
        }
        builder.append("\r\n").append(mBody);
        return builder.toString();
    }

    /* Parse */
    // --------------------------------------------------------------------------------------------
    public static Request parse(String line) throws ParseException, MalformedJsonException {
        Timber.v("Line to parse: %s", line);
        String[] tokens = line.split("\\r?\\n");
        if (tokens.length <= 0) {
            String error = "Parse error: invalid line: " + line;
            throw new ParseException(error, 0);
        }

        Startline startline = parseStartLine(tokens[0]);
        List<Header> headers = new ArrayList<>();

        int index = 1;
        boolean checkForBody = true, hasBody = false;
        while (Header.isHeader(tokens[index])) {
            Header header = Header.parse(tokens[index]);
            headers.add(header);
            ++index;

            if (checkForBody && header.getKey().equals("Content-Length")) {
                checkForBody = false;
                hasBody = Integer.parseInt(header.getValue().trim()) > 0;
            }
        }

        String ending = "";
        StringBuilder bodyBuilder = new StringBuilder();
        while (hasBody && index < tokens.length) {
            String bodyLine = tokens[index].replaceAll("\r+", "");
            bodyBuilder.append(ending).append(bodyLine);
            ending = "\n";
            ++index;
        }

        String body = bodyBuilder.toString();
        if (hasBody) {
            int i1 = body.lastIndexOf("}");
            if (i1 > 0) {
                body = body.substring(0, i1 + 1);
            } else {
                String error = "Malformed json body: " + line;
                throw new MalformedJsonException(error);
            }
        }

        Request request = new Request();
        request.mStartline = startline;
        request.mHeaders = headers;
        request.mBody = body.toString();
        return request;
    }

    public static Startline parseStartLine(String line) throws ParseException {
        Timber.v("Line to parse: %s", line);
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
