package com.android.sample.cognitive.services.speaker.identification;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;
import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationClient;
import com.microsoft.cognitive.speakerrecognition.contract.identification.OperationLocation;

import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.jdeferred.android.DeferredAsyncTask;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@FragmentWithArgs
public final class AudioRegisterFragment extends Fragment {

    private static final String TAG = AudioRegisterFragment.class.getSimpleName();

    private static final String MENU_TITLE_REMOVE = "Remove";

    private static final int SAMPLING_RATE = 16000;

    private final SpeakerIdentificationClient client = SpeakerIdentificationFactory.getSpeakerIdentificationClient();

    private final int bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    private final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private boolean isRecording;

    private Button recordButton;

    @Arg
    String identificationProfileId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentArgs.inject(this);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_audio_register, container, false);
        recordButton = (Button) view.findViewById(R.id.buttonRecord);
        recordButton.setOnClickListener(v -> startRecording());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getString(R.string.title_register_audio));
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

                FileHelper.copyWaveFile(bufferSize, identificationProfileId);
                FileHelper.deleteTempFile();

                // Register user voice.
                registerAudio(FileHelper.getFilename(identificationProfileId));
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

    private void registerAudio(String filePath) {
        AndroidDeferredManager manager = new AndroidDeferredManager();
        manager
                .when(new DeferredAsyncTask<Void, Object, OperationLocation>() {

                    @Override
                    protected OperationLocation doInBackgroundSafe(Void... voids) throws Exception {
                        InputStream inputStream = new FileInputStream(filePath);
                        return client.enroll(inputStream, UUID.fromString(identificationProfileId), true);
                    }
                })
                .done(location -> {
                    Log.d(TAG, "EnrollmentStatus: " + location.Url);
                    Toast.makeText(getContext(), "Success.", Toast.LENGTH_SHORT).show();
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
                client.deleteProfile(UUID.fromString(identificationProfileId));
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
