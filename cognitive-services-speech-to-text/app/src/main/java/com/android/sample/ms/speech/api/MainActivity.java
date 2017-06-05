package com.android.sample.ms.speech.api;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android.sample.ms.speech.api.speech.ConnectionListener;
import com.android.sample.ms.speech.api.speech.MessageListener;
import com.android.sample.ms.speech.api.speech.SpeechClient;
import com.android.sample.ms.speech.api.speech.SpeechToText;
import com.android.sample.ms.speech.api.speech.configuration.SpeechConfiguration;
import com.android.sample.ms.speech.api.speech.constants.EndPoint;
import com.android.sample.ms.speech.api.speech.constants.OutputFormat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // TODO: Set your subscription key.
    private static final String SUBSCRIPTION_KEY = "<SUBSCRIPTION_KEY>";

    private static final int SAMPLING_RATE = 16000;

    private SpeechClient speechClient;

    private Button recordButton;

    private TextView speechResultTextView;

    private boolean isRecording;

    private int bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    private AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

    private final StringBuilder builder = new StringBuilder();

    public ConnectionListener connectionListener = new ConnectionListener() {

        @Override
        public void onConnected() {
            Log.d(TAG, "Connected.");
            speechResultTextView.setText(R.string.textView_connected);
            recordButton.setEnabled(true);
        }

        @Override
        public void onDisconnected() {
            Log.e(TAG, "Disconnected");
            speechResultTextView.setText(R.string.textView_disconnected);
            recordButton.setEnabled(false);
        }
    };

    public MessageListener messageListener = new MessageListener() {

        @Override
        public void onTextReceived(String text) {
            builder.append("\r\n").append(text);
            speechResultTextView.setText(builder.toString());
        }

        @Override
        public void onSpeechEnd() {
            isRecording = false;
            audioRecord.stop();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpeechConfiguration configuration = new SpeechConfiguration(SUBSCRIPTION_KEY, EndPoint.Conversation, OutputFormat.SIMPLE);
        speechClient = SpeechToText.getSpeechClient(configuration);

        Button connectButton = (Button) findViewById(R.id.buttonConnect);
        connectButton.setOnClickListener(view -> speechClient.connect());

        recordButton = (Button) findViewById(R.id.buttonRecord);
        recordButton.setOnClickListener(view -> recordAudio());

        speechResultTextView = (TextView) findViewById(R.id.textViewSpeechResult);
        speechResultTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    protected void onStart() {
        super.onStart();
        speechClient.addConnectionListener(connectionListener);
        speechClient.addMessageListener(messageListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        speechClient.removeConnectionListener(connectionListener);
        speechClient.removeMessageListener(messageListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioRecord.release();
    }

    private void recordAudio() {
        if (isRecording) return;

        isRecording = true;
        audioRecord.startRecording();

        final byte[] audioData = new byte[bufferSize];
        new Thread(() -> {
            boolean isFirstRecord = true;
            while (true) {
                if (!isRecording) return;

                int read = audioRecord.read(audioData, 0, audioData.length);
                if (isFirstRecord) {
                    speechClient.sendAudioFirst(audioData, read);
                } else {
                    speechClient.sendAudio(audioData, read);
                }
                isFirstRecord = false;
            }
        }).start();
    }
}