package com.android.sample.ms.speech.api.speech.helper;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import static com.android.sample.ms.speech.api.speech.constants.Header.CRLF;

public final class MessageParser {

    private MessageParser() {
    }

    public static Map<String, String> parseHeader(@NonNull String message) {
        String[] parts = message.split(CRLF.getValue() + CRLF.getValue());
        if (parts.length != 2) {
            throw new IllegalArgumentException("Message '" + message + "' does not have header and body.");
        }

        String[] headerLines = parts[0].split(CRLF.getValue());
        Map<String, String> headers = new HashMap<>(headerLines.length);

        for (String headerLine : headerLines) {
            String[] headerParts = headerLine.split(":");
            if (headerParts.length < 2) {
                throw new IllegalArgumentException("Header '" + headerLine + "' does not have a name and value.");
            }

            StringBuilder headerValueBuilder = new StringBuilder();
            for (int i = 1; i < headerParts.length; i++) {
                headerValueBuilder.append(headerParts[i]).append(':');
            }
            headerValueBuilder.setLength(headerValueBuilder.length() - 1);

            String headerValue = headerValueBuilder.toString().trim();
            String headerName = headerParts[0].trim();
            headers.put(headerName, headerValue);
        }

        return headers;
    }

    public static String parseBody(@NonNull String message) {
        String[] parts = message.split(CRLF.getValue() + CRLF.getValue());
        if (parts.length != 2) {
            throw new IllegalArgumentException("Message '" + message + "' does not have header and body.");
        }
        return parts[1];
    }
}
