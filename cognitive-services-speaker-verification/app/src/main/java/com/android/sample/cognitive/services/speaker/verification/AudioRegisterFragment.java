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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;
import com.microsoft.cognitive.speakerrecognition.SpeakerVerificationClient;
import com.microsoft.cognitive.speakerrecognition.contract.verification.Enrollment;
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
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@FragmentWithArgs
public final class AudioRegisterFragment extends Fragment {

    private static final String TAG = AudioRegisterFragment.class.getSimpleName();

    private static final String MENU_TITLE_REMOVE = "Remove";

    private static final int SAMPLING_RATE = 16000;

    private final SpeakerVerificationClient client = SpeakerVerificationFactory.getSpeakerVerificationClient();

    private final int bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    private final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private boolean isRecording;

    private Button recordButton;

    private TextView phraseTextView;

    private TextView remainingEnrollmentsTextView;

    @Arg
    String verificationProfileId;

    @Arg
    int remainingEnrollmentsCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentArgs.inject(this);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_audio_register, container, false);
        phraseTextView = (TextView) view.findViewById(R.id.textViewPhrase);
        phraseTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        remainingEnrollmentsTextView = (TextView) view.findViewById(R.id.textViewRemainingEnrollments);
        recordButton = (Button) view.findViewById(R.id.buttonRecord);
        recordButton.setOnClickListener(v -> startRecording());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getString(R.string.title_register_audio));

        remainingEnrollmentsTextView.setText("Remaining enrollments count: " + String.valueOf(remainingEnrollmentsCount));

        AndroidDeferredManager manager = new AndroidDeferredManager();
        manager.when(new DeferredAsyncTask<Void, Object, List<VerificationPhrase>>() {

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

            Log.d(TAG, builder.toString());
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem removeItem = menu.add(MENU_TITLE_REMOVE);
        removeItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MENU_TITLE_REMOVE.equals(item.getTitle())) {
            removeProfile();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioRecord.release();
    }

    private void startRecording() {
        recordButton.setEnabled(false);

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

                FileHelper.copyWaveFile(bufferSize, verificationProfileId);
                FileHelper.deleteTempFile();

                // Register user voice.
                registerAudio();
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

                recordButton.setEnabled(true);
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void registerAudio() {
        String filePath = FileHelper.getFilename(verificationProfileId);

        AndroidDeferredManager manager = new AndroidDeferredManager();
        manager
                .when(new DeferredAsyncTask<Void, Object, Enrollment>() {

                    @Override
                    protected Enrollment doInBackgroundSafe(Void... voids) throws Exception {
                        InputStream inputStream = new FileInputStream(filePath);
                        return client.enroll(inputStream, UUID.fromString(verificationProfileId));
                    }
                })
                .done(enrollment -> {
                    Log.d(TAG, "remainingEnrollments: " + enrollment.remainingEnrollments);
                    Log.d(TAG, "phrase: " + enrollment.phrase);

                    if (enrollment.remainingEnrollments <= 0) {
                        Toast.makeText(getContext(), "Success.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Remaining enrollments are " + enrollment.remainingEnrollments, Toast.LENGTH_SHORT).show();
                    }
                })
                .fail(new AndroidFailCallback<Throwable>() {

                    @Override
                    public void onFail(Throwable throwable) {
                        Log.e(TAG, "Failed to register audio.", throwable);
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public AndroidExecutionScope getExecutionScope() {
                        return null;
                    }
                });
    }

    private void removeProfile() {
        AndroidDeferredManager deferredManager = new AndroidDeferredManager();
        deferredManager.when(new DeferredAsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackgroundSafe(Void... voids) throws Exception {
                client.deleteProfile(UUID.fromString(verificationProfileId));
                return null;
            }
        }).done(result -> {
            Log.d(TAG, "Success");
            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }).fail(new AndroidFailCallback<Throwable>() {

            @Override
            public void onFail(Throwable e) {
                Log.e(TAG, "Failed to remove user profile.", e);
                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public AndroidExecutionScope getExecutionScope() {
                return null;
            }
        });
    }
}
