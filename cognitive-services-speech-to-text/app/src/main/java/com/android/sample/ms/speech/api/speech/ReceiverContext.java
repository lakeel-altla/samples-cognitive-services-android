package com.android.sample.ms.speech.api.speech;

import java.util.List;

interface ReceiverContext {

    MessageSender getMessageSender();

    List<MessageListener> getMessageListeners();

    void runOnMainThread(Runnable runnable);
}
