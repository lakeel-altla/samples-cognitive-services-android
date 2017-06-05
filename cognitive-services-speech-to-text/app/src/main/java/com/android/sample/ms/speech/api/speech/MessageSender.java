package com.android.sample.ms.speech.api.speech;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.message.converter.AudioConverter;
import com.android.sample.ms.speech.api.speech.message.converter.SpeechConfigConverter;
import com.android.sample.ms.speech.api.speech.message.converter.TelemetryConverter;
import com.android.sample.ms.speech.api.speech.telemetry.ConnectionTelemetry;
import com.android.sample.ms.speech.api.speech.telemetry.MicrophoneTelemetry;
import com.android.sample.ms.speech.api.speech.telemetry.TimestampTelemetry;
import com.neovisionaries.ws.client.WebSocket;

import java.nio.ByteBuffer;
import java.util.UUID;

final class MessageSender {

    private final WebSocket ws;

    private final String connectionId;

    private final String requestId = UUID.randomUUID().toString().replace("-", "");

    MessageSender(@NonNull WebSocket ws, @NonNull String connectionId) {
        this.ws = ws;
        this.connectionId = connectionId;
    }

    void sendConfiguration() {
        String message = SpeechConfigConverter.convert(requestId);
        sendText(message);
    }

    void sendAudioFirst(byte[] audioData, @IntRange(from = 0) int read) {
        // Save record start time.
        MicrophoneTelemetry microphoneTelemetry = MicrophoneTelemetry.forRequestId(requestId);
        microphoneTelemetry.saveStartRecordTime();

        ByteBuffer byteBuffer = AudioConverter.convertFirst(audioData, read, requestId);
        sendBinary(byteBuffer.array());
    }

    void sendAudio(byte[] audioData, @IntRange(from = 0) int read) {
        ByteBuffer byteBuffer = AudioConverter.convert(audioData, read, requestId);
        sendBinary(byteBuffer.array());
    }

    void sendTelemetry() {
        TimestampTelemetry timestampTelemetry = TimestampTelemetry.forRequestId(requestId);
        ConnectionTelemetry connectionTelemetry = ConnectionTelemetry.forConnectionId(connectionId);
        MicrophoneTelemetry microphoneTelemetry = MicrophoneTelemetry.forRequestId(requestId);

        String message = TelemetryConverter.convert(timestampTelemetry, connectionTelemetry, microphoneTelemetry, requestId, connectionId);
        sendText(message);
    }

    private void sendText(String message) {
        if (!ws.isOpen()) throw new IllegalStateException("Connection is closed.");
        ws.sendText(message);
    }

    private void sendBinary(byte[] array) {
        if (!ws.isOpen()) throw new IllegalStateException("Connection is closed.");
        ws.sendBinary(array);
    }
}