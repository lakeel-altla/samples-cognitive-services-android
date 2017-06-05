package com.android.sample.ms.speech.api.speech;

import java.util.List;

public interface ConnectionContext {

    MessageSender getMessageSender();

    List<ConnectionListener> getConnectionListeners();

    void runOnMainThread(Runnable runnable);
}
