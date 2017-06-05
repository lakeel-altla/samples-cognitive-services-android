package com.android.sample.ms.speech.api.speech;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.configuration.SpeechConfiguration;
import com.android.sample.ms.speech.api.speech.constants.Header;
import com.android.sample.ms.speech.api.speech.exception.SpeechClientException;
import com.android.sample.ms.speech.api.speech.telemetry.ConnectionTelemetry;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public final class SpeechToText implements SpeechClient, ConnectionContext, ReceiverContext {

    private final SpeechConfiguration configuration;

    private WebSocket ws;

    private MessageSender sender;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new LinkedList<ConnectionListener>());

    private final List<MessageListener> messageListeners = Collections.synchronizedList(new LinkedList<MessageListener>());

    public static SpeechClient getSpeechClient(@NonNull SpeechConfiguration configuration) {
        return new SpeechToText(configuration);
    }

    private SpeechToText(@NonNull SpeechConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void connect() {
        if (ws != null && ws.isOpen()) return;

        WebSocketFactory factory = new WebSocketFactory();
        try {
            ws = factory.createSocket(configuration.getUri());
        } catch (IOException e) {
            throw new SpeechClientException(e);
        }

        String connectionId = UUID.randomUUID().toString().replace("-", "").toUpperCase();

        // Save connection started time.
        ConnectionTelemetry connectionTelemetry = ConnectionTelemetry.forConnectionId(connectionId);
        connectionTelemetry.saveStartConnectionTime();

        ws.addHeader(Header.X_CONNECTION_ID.getValue(), connectionId);
        ws.addHeader(Header.OCP_APIM_SUBSCRIPTION_KEY.getValue(), configuration.getSubscriptionKey());
        ws.addListener(new MessageReceiver(this));
        ws.addListener(new ConnectionReceiver(this, connectionId));
        ws.connectAsynchronously();

        sender = new MessageSender(ws, connectionId);
    }

    @Override
    public void sendAudioFirst(byte[] audioData, @IntRange(from = 0) int read) {
        sender.sendAudioFirst(audioData, read);
    }

    @Override
    public void sendAudio(byte[] audioData, @IntRange(from = 0) int read) {
        sender.sendAudio(audioData, read);
    }

    @Override
    public void addMessageListener(@NonNull MessageListener listener) {
        messageListeners.add(listener);
    }

    @Override
    public void removeConnectionListener(@NonNull ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    @Override
    public void removeMessageListener(@NonNull MessageListener listener) {
        messageListeners.remove(listener);
    }

    @Override
    public List<ConnectionListener> getConnectionListeners() {
        return Collections.unmodifiableList(connectionListeners);
    }

    @Override
    public void runOnMainThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public List<MessageListener> getMessageListeners() {
        return Collections.unmodifiableList(messageListeners);
    }

    @Override
    public MessageSender getMessageSender() {
        return sender;
    }

    @Override
    public void addConnectionListener(@NonNull ConnectionListener listener) {
        connectionListeners.add(listener);
    }
}