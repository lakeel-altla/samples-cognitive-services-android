package com.android.sample.ms.speech.api.speech.exception;

import android.support.annotation.NonNull;

public final class SpeechClientException extends RuntimeException {

    public SpeechClientException(@NonNull Exception e) {
        super(e);
    }
}
