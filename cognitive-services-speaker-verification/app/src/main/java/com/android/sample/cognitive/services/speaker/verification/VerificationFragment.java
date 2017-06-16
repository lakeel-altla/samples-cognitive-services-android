package com.android.sample.cognitive.services.speaker.verification;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.microsoft.cognitive.speakerrecognition.SpeakerVerificationClient;
import com.microsoft.cognitive.speakerrecognition.contract.verification.Profile;
import com.microsoft.cognitive.speakerrecognition.contract.verification.Result;
import com.microsoft.cognitive.speakerrecognition.contract.verification.Verification;
import com.microsoft.cognitive.speakerrecognition.contract.verification.VerificationException;
import com.microsoft.cognitive.speakerrecognition.contract.verification.VerificationPhrase;

import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.jdeferred.android.DeferredAsyncTask;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class VerificationFragment extends Fragment {

    private static final String TAG = VerificationFragment.class.getSimpleName();

    private static final int SAMPLING_RATE = 16000;

    private final SpeakerVerificationClient client = SpeakerVerificationFactory.getSpeakerVerificationClient();

    private final int bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    private final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private final String filePath = UUID.randomUUID().toString().replace("-", "");

    private boolean isRecording;

    private Button verificationButton;

    private TextView phraseTextView;

    private String verificationProfileId;

    public static VerificationFragment newInstance() {
        return new VerificationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verification, container, false);

        phraseTextView = (TextView) view.findViewById(R.id.textViewPhrase);
        phraseTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        verificationButton = (Button) view.findViewById(R.id.buttonVerification);
        verificationButton.setOnClickListener(v -> startVerification());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getString(R.string.title_verification));

        new AndroidDeferredManager().when(new DeferredAsyncTask<Void, Object, List<Profile>>() {

            @Override
            protected List<Profile> doInBackgroundSafe(Void... voids) throws Exception {
                List<Profile> profiles = client.getProfiles();
                // Sort by createdDateTime.
                Collections.sort(profiles, (p1, p2) -> p2.createdDateTime.compareTo(p1.createdDateTime));
                return profiles;
            }
        }).done(profiles -> {
            if (profiles == null || profiles.size() == 0) {
                Toast.makeText(getContext(), "Please register audio first.", Toast.LENGTH_SHORT).show();
                return;
            }
            List<UUID> ids = new ArrayList<>(profiles.size());
            for (Profile profile : profiles) {
                ids.add(profile.verificationProfileId);
            }

            new MaterialDialog.Builder(getContext())
                    .title("Select speaker")
                    .content("Select speaker id for verification.")
                    .items(ids)
                    .cancelable(false)
                    .itemsCallbackSingleChoice(-1, (dialog1, itemView, which, text) -> {
                        verificationProfileId = (String) text;
                        return true;
                    })
                    .show();

            fetchPhases();
        }).fail(new AndroidFailCallback<Throwable>() {

            @Override
            public void onFail(Throwable throwable) {
                Log.e(TAG, "Failed to fetch user profiles.", throwable);
            }

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return null;
            }
        });
    }

    private void fetchPhases() {
        new AndroidDeferredManager().when(new DeferredAsyncTask<Void, Object, List<VerificationPhrase>>() {

            @Override
            protected List<VerificationPhrase> doInBackgroundSafe(Void... voids) throws Exception {
                return client.getPhrases("en-US");
            }
        }).done(results -> {
            StringBuilder builder = new StringBuilder();
            builder.append("Phrases is below. You must speech one of the phrases.");
            builder.append("\n\n");
            for (VerificationPhrase result : results) {
                builder.append(result.phrase);
                builder.append("\n\n");
            }
            phraseTextView.setText(builder.toString());
        }).fail(new AndroidFailCallback<Throwable>() {

            @Override
            public void onFail(Throwable e) {
                Log.e(TAG, "Failed to fetch phrase.", e);
                Toast.makeText(getContext(), "Failed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return null;
            }
        });
    }


    private void startVerification() {
        verificationButton.setEnabled(false);

        audioRecord.startRecording();
        isRecording = true;

        new Thread(() -> {
            // First, write temp file.
            String filePath = FileHelper.getTempFilename();
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(filePath);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Failed to find audio file.", e);
                Toast.makeText(getContext(), "Failed.", Toast.LENGTH_SHORT).show();
                return;
            }

            byte buf[] = new byte[bufferSize];
            while (isRecording) {
                audioRecord.read(buf, 0, buf.length);

                Log.v(TAG, "read " + buf.length + " bytes");

                try {
                    outputStream.write(buf);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to write audio data.", e);
                    Toast.makeText(getContext(), "Failed.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // After recording, copy from temp file to WAV file.
            if (!isRecording) {
                // Close stream.
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close stream.", e);
                    Toast.makeText(getContext(), "Failed.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FileHelper.copyWaveFile(bufferSize, filePath);
                FileHelper.deleteTempFile();

                verification();
            }
        }).start();

        // Stop recording after 5 seconds.
        executor.schedule(new TimerTask() {

            @Override
            public void run() {
                // Stop recording.
                isRecording = false;

                Log.v(TAG, "stop");
                audioRecord.stop();

                verificationButton.setEnabled(true);
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void verification() {
        String path = FileHelper.getFilename(filePath);
        AndroidDeferredManager manager = new AndroidDeferredManager();
        manager.when(new DeferredAsyncTask<Void, Object, Verification>() {

            @Override
            protected Verification doInBackgroundSafe(Void... voids) throws Exception {
                InputStream inputStream = new FileInputStream(path);
                return client.verify(inputStream, UUID.fromString(verificationProfileId));
            }
        }).done(verification -> {
            Result result = verification.result;
            if (Result.ACCEPT == result) {
                Log.d(TAG, "Verification result: Accept");
                Log.d(TAG, "confidence: " + verification.confidence);
                Log.d(TAG, "phrase: " + verification.phrase);
                Toast.makeText(getContext(), "Accept.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Verification result: Reject");
                Toast.makeText(getContext(), "Reject.", Toast.LENGTH_SHORT).show();
            }
        }).fail(new AndroidFailCallback<Throwable>() {

            @Override
            public void onFail(Throwable e) {
                Log.e(TAG, "Failed to identify.", e);

                if (e instanceof VerificationException) {
                    VerificationException exception = (VerificationException) e;
                    String message = exception.getMessage();
                    if ("IncompleteEnrollment".equals(message)) {
                        Toast.makeText(getContext(), "Not enrolled yet. Please register audio.", Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).showProfileListFragment();
                        return;
                    }
                }

                Toast.makeText(getContext(), "Failed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return null;
            }
        });
    }
}
