package com.android.sample.ms.speech.api.speech.configuration;

import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.constants.EndPoint;
import com.android.sample.ms.speech.api.speech.constants.OutputFormat;

import java.util.Locale;

public final class SpeechConfiguration {

    private static final String URI = "wss://speech.platform.bing.com";

    private final String subscriptionKey;

    private final EndPoint endPoint;

    private final OutputFormat format;

    public SpeechConfiguration(@NonNull String subscriptionKey,
                               @NonNull EndPoint endPoint,
                               @NonNull OutputFormat format) {
        this.subscriptionKey = subscriptionKey;
        this.endPoint = endPoint;
        this.format = format;
    }

    public String getUri() {
        Locale locale = Locale.getDefault();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        return URI + endPoint.getValue() +
                "?" + "LANGUAGE" + "=" + language + "-" + country +
                "&" + "FORMAT" + "=" + format.getValue();
    }

    public String getSubscriptionKey() {
        return subscriptionKey;
    }
}
