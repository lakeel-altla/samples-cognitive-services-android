package com.android.sample.ms.speech.api.speech.helper;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JsonMapper {

    public static class JsonMapperException extends RuntimeException {

        public JsonMapperException(@NonNull String msg, @NonNull Throwable cause) {
            super(msg, cause);
        }
    }

    // Only non-null.
    private static final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private JsonMapper() {
    }

    public static String toJson(@NonNull Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonMapperException("Failed to convert to json:objectName=" + object.getClass().getSimpleName(), e);
        }
    }

    public static <T> T toObject(@NonNull String json, @NonNull Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonMapperException("Failed to convert to object:json=" + json, e);
        }
    }
}