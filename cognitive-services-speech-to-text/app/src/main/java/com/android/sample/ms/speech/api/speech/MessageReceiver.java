package com.android.sample.ms.speech.api.speech;

import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.constants.Path;
import com.android.sample.ms.speech.api.speech.helper.JsonMapper;
import com.android.sample.ms.speech.api.speech.helper.MessageParser;
import com.android.sample.ms.speech.api.speech.message.SpeechPhrase;
import com.android.sample.ms.speech.api.speech.telemetry.MicrophoneTelemetry;
import com.android.sample.ms.speech.api.speech.telemetry.TimestampTelemetry;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;

import java.util.Map;

import static com.android.sample.ms.speech.api.speech.constants.Header.PATH;
import static com.android.sample.ms.speech.api.speech.constants.Header.X_REQUEST_ID;

final class MessageReceiver extends WebSocketAdapter {

    private final ReceiverContext context;

    MessageReceiver(@NonNull ReceiverContext context) {
        this.context = context;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        // Parse json.
        Map<String, String> headers = MessageParser.parseHeader(text);
        String json = MessageParser.parseBody(text);

        String path = headers.get(PATH.getValue());
        String requestId = headers.get(X_REQUEST_ID.getValue());

        // Save timestamp that received message.
        TimestampTelemetry telemetry = TimestampTelemetry.forRequestId(requestId);
        telemetry.saveTimestamp(path);

        // Message handling.
        switch (Path.toPath(path)) {
            case TURN_START: {
                break;
            }
            case SPEECH_START_DETECTED: {
                break;
            }
            case SPEECH_HYPOTHESIS: {
                break;
            }
            case SPEECH_PHRASE: {
                SpeechPhrase speechPhrase = JsonMapper.toObject(json, SpeechPhrase.class);
                callOnTextReceived(speechPhrase.DisplayText);
                break;
            }
            case SPEECH_END_DETECTED: {
                // Save record end time.
                MicrophoneTelemetry microphoneTelemetry = MicrophoneTelemetry.forRequestId(requestId);
                microphoneTelemetry.saveEndRecordTime();
                callOnSpeechEnd();
                break;
            }
            case TURN_END: {
                context.getMessageSender().sendTelemetry();
                break;
            }
            case UNKNOWN:
                break;
        }
    }

    private void callOnTextReceived(@NonNull String text) {
        for (MessageListener listener : context.getMessageListeners()) {
            context.runOnMainThread(() -> listener.onTextReceived(text));
        }
    }

    private void callOnSpeechEnd() {
        for (MessageListener listener : context.getMessageListeners()) {
            context.runOnMainThread(listener::onSpeechEnd);
        }
    }
}