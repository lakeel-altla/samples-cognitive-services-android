package com.android.sample.ms.speech.api.speech;

public interface MessageListener {

    void onTextReceived(String text);

    void onSpeechEnd();
}
