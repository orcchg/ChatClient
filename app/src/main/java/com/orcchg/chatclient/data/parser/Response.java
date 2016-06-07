package com.orcchg.chatclient.data.parser;

import android.support.annotation.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Response {

    public static class Codeline {
        int version;
        int code;
        String message;
    }

    private Codeline mCodeline;
    private List<Header> mHeaders;
    private String mBody;

    /* Parse */
    // --------------------------------------------------------------------------------------------
    @Nullable
    public static Response parse(char[] buffer) {
        try {
            return parse(new String(buffer));
        } catch (ParseException e) {
            Timber.e(e.getMessage());
        }
        return null;
    }

    public static Response parse(String line) throws ParseException {
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
        StringBuilder body = new StringBuilder();
        while (index < tokens.length) {
            String bodyLine = tokens[index].replaceAll("\r+", "");
            body.append(ending).append(bodyLine);
            ending = "\n";
            ++index;
        }

        Response response = new Response();
        response.mCodeline = codeline;
        response.mHeaders = headers;
        response.mBody = body.toString();
        return response;
    }

    public static Codeline parseCodeline(String line) throws ParseException {
        line = line.replaceAll("\\s+", " ").trim();
        int i1 = line.indexOf("HTTP");
        int i2 = line.indexOf(' ', i1 + 8);
        int i3 = line.indexOf(' ', i2 + 1);
        if (i1 < 0 || i2 < 0 || i3 < 0) {
            String error = "Parse error: invalid code line: " + line;
            throw new ParseException(error, i1);
        }
        Codeline codeline = new Codeline();
        codeline.version = Integer.parseInt(line.substring(i1 + 7, i1 + 8));
        codeline.code = Integer.parseInt(line.substring(i2, i3));
        codeline.message = line.substring(i3 + 1);
        return codeline;
    }
}
