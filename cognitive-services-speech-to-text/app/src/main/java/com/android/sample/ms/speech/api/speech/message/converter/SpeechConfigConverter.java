package com.android.sample.ms.speech.api.speech.message.converter;

import android.os.Build;
import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.helper.CurrentTime;
import com.android.sample.ms.speech.api.speech.helper.JsonMapper;
import com.android.sample.ms.speech.api.speech.message.SpeechConfig;

import static com.android.sample.ms.speech.api.speech.constants.ContentType.APPLICATION_JSON;
import static com.android.sample.ms.speech.api.speech.constants.Header.CONTENT_TYPE;
import static com.android.sample.ms.speech.api.speech.constants.Header.CRLF;
import static com.android.sample.ms.speech.api.speech.constants.Header.PATH;
import static com.android.sample.ms.speech.api.speech.constants.Header.X_REQUEST_ID;
import static com.android.sample.ms.speech.api.speech.constants.Header.X_TIMESTAMP;
import static com.android.sample.ms.speech.api.speech.constants.Path.SPEECH_CONFIG;
import static com.android.sample.ms.speech.api.speech.message.SpeechConfig.Device;
import static com.android.sample.ms.speech.api.speech.message.SpeechConfig.Os;
import static com.android.sample.ms.speech.api.speech.message.SpeechConfig.System;

public final class SpeechConfigConverter {

    private SpeechConfigConverter() {
    }

    public static String convert(@NonNull String requestId) {
        SpeechConfig message = new SpeechConfig();

        System system = new System();
        // This sample does not use Speech SDK, but this value is needed, so set fixed value;
        system.version = "2.0.12341";

        Os os = new Os();
        os.platform = "Android";
        os.name = Build.VERSION.CODENAME;
        os.version = String.valueOf(Build.VERSION.SDK_INT);

        Device device = new Device();
        device.manufacturer = Build.BRAND;
        device.model = Build.DEVICE;
        device.version = Build.VERSION.RELEASE;

        message.context.system = system;
        message.os = os;
        message.device = device;

        String json = JsonMapper.toJson(message);

        return PATH.getValue() + ": " + SPEECH_CONFIG.getValue() + CRLF.getValue() +
                X_REQUEST_ID.getValue() + ": " + requestId + CRLF.getValue() +
                X_TIMESTAMP.getValue() + ": " + CurrentTime.newTime() + CRLF.getValue() +
                CONTENT_TYPE.getValue() + ": " + APPLICATION_JSON.getValue() + CRLF.getValue() + CRLF.getValue() + json;
    }
}
