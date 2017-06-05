package com.android.sample.ms.speech.api.speech;

import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.telemetry.ConnectionTelemetry;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.List;
import java.util.Map;

final class ConnectionReceiver extends WebSocketAdapter {

    private final ConnectionContext context;

    private final String connectionId;

    ConnectionReceiver(@NonNull ConnectionContext context, @NonNull String connectionId) {
        this.context = context;
        this.connectionId = connectionId;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ConnectionTelemetry connectionTelemetry = ConnectionTelemetry.forConnectionId(connectionId);
        connectionTelemetry.saveEstablishedConnectionTime();

        context.getMessageSender().sendConfiguration();

        callOnConnected();
    }

    @Override
    public void onDisconnected(WebSocket websocket,
                               WebSocketFrame serverCloseFrame,
                               WebSocketFrame clientCloseFrame,
                               boolean closedByServer) throws Exception {
        callOnDisconnected();
    }


    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        ConnectionTelemetry connectionTelemetry = ConnectionTelemetry.forConnectionId(connectionId);
        connectionTelemetry.saveErrorMessage(exception.getMessage());
    }

    private void callOnConnected() {
        for (ConnectionListener listener : context.getConnectionListeners()) {
            context.runOnMainThread(listener::onConnected);
        }
    }

    private void callOnDisconnected() {
        for (ConnectionListener listener : context.getConnectionListeners()) {
            context.runOnMainThread(listener::onDisconnected);
        }
    }
}
