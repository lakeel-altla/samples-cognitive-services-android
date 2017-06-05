package com.android.sample.ms.speech.api.speech;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public interface SpeechClient {

    void connect();

    void sendAudioFirst(byte[] audioData, @IntRange(from = 0) int read);

    void sendAudio(byte[] audioData, @IntRange(from = 0) int read);

    void addConnectionListener(@NonNull ConnectionListener listener);

    void addMessageListener(@NonNull MessageListener listener);

    void removeConnectionListener(@NonNull ConnectionListener listener);

    void removeMessageListener(@NonNull MessageListener listener);
}
