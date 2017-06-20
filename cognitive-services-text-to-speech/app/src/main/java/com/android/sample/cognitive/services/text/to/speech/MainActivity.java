package com.android.sample.cognitive.services.text.to.speech;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jamesmurty.utils.XMLBuilder2;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Please paste your subscription key.
    private static final String SUBSCRIPTION_KEY = "<SUBSCRIPTION_KEY>";

    private static final String SEARCH_APP_ID = UUID.randomUUID().toString().replace("-", "");

    private static final String SEARCH_CLIENT_ID = UUID.randomUUID().toString().replace("-", "");

    private class HttpResult<T> {

        T value;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        String accessToken = fetchAccessToken();
        if (accessToken == null || accessToken.isEmpty()) throw new NullPointerException();

        EditText editTextSpeechWord = (EditText) findViewById(R.id.editTextSpeechWord);

        Button buttonSpeech = (Button) findViewById(R.id.buttonSpeech);
        buttonSpeech.setOnClickListener(v -> {
            String speechWord = editTextSpeechWord.getText().toString();
            if (speechWord.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please input english spoken text.", Toast.LENGTH_SHORT).show();
                return;
            }
            speech(speechWord, accessToken);
        });
    }

    private String fetchAccessToken() {
        Request request = new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
                .url("https://api.cognitive.microsoft.com/sts/v1.0/issueToken")
                .header("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY)
                .build();

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        HttpResult<String> httpResult = new HttpResult<>();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (body != null) httpResult.value = body.string();
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "Failed to fetch access token.", e);
                Toast.makeText(getApplicationContext(), "Failed.", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return httpResult.value;
    }

    private void speech(@NonNull String speechWord, @NonNull String accessToken) {
        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/ssml+xml")
                .add("X-Microsoft-OutputFormat", "riff-16khz-16bit-mono-pcm")
                .add("X-Search-AppId", SEARCH_APP_ID)
                .add("X-Search-ClientID", SEARCH_CLIENT_ID)
                .add("User-Agent", "Sample App")
                .add("Authorization", accessToken)
                .build();

        // ex:
        // <speak version="1.0" xml:lang="en-US">
        //    <voice xml:gender="Female" name="Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)">
        //      Hello everyone, I'm John
        //    </voice>
        // </speak>
        XMLBuilder2 xmlBuilder2 = XMLBuilder2
                .create("speak").a("version", "1.0").a("xml:lang", "en-US")
                .e("voice").a("xml:gender", "Male").a("name", "Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)")
                .t(speechWord)
                .up()
                .up();
        String ssml = xmlBuilder2.asString();

        RequestBody body = RequestBody.create(MediaType.parse("application/ssml+xml"), ssml);
        Request request = new Request.Builder()
                .post(body)
                .url("https://speech.platform.bing.com/synthesize")
                .headers(headers)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "Response: " + response.toString());

                ResponseBody body = response.body();
                if (body != null) {
                    new Thread(() -> {
                        byte[] wavData;
                        try {
                            wavData = body.bytes();
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to get response as a byte array.", e);
                            throw new RuntimeException(e);
                        }

                        Log.d(TAG, "wavData.length: " + wavData.length);

                        int bufSize = AudioTrack.getMinBufferSize(
                                16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

                        AudioTrack audioTrack = new AudioTrack(
                                AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO,
                                AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);

                        audioTrack.play();
                        audioTrack.write(wavData, 0, wavData.length);
                        audioTrack.stop();
                    }).start();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "Failed to fetch audio data.", e);
                Toast.makeText(getApplicationContext(), "Failed.", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
        });
    }
}