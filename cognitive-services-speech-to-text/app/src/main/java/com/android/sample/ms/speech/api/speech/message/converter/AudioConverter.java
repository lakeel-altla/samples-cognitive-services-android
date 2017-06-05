package com.android.sample.ms.speech.api.speech.message.converter;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.helper.CurrentTime;

import java.nio.ByteBuffer;

import static com.android.sample.ms.speech.api.speech.constants.ContentType.AUDIO_WAV;
import static com.android.sample.ms.speech.api.speech.constants.Header.CONTENT_TYPE;
import static com.android.sample.ms.speech.api.speech.constants.Header.CRLF;
import static com.android.sample.ms.speech.api.speech.constants.Header.PATH;
import static com.android.sample.ms.speech.api.speech.constants.Header.X_REQUEST_ID;
import static com.android.sample.ms.speech.api.speech.constants.Header.X_TIMESTAMP;
import static com.android.sample.ms.speech.api.speech.constants.Path.AUDIO;
import static java.lang.Short.MAX_VALUE;
import static java.lang.Short.MIN_VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class AudioConverter {

    private static final int SAMPLING_RATE = 16000;

    private static final byte BITS_PER_SAMPLE = 16;

    private AudioConverter() {
    }

    public static ByteBuffer convert(byte[] audioData,
                                     @IntRange(from = 0) int read,
                                     @NonNull String requestId) {
        String builder = PATH.getValue() + ": " + AUDIO.getValue() + CRLF.getValue() +
                X_REQUEST_ID.getValue() + ": " + requestId + CRLF.getValue() +
                X_TIMESTAMP.getValue() + ": " + CurrentTime.newTime() + CRLF.getValue() +
                CONTENT_TYPE.getValue() + ": " + AUDIO_WAV.getValue() + CRLF.getValue() + CRLF.getValue();

        byte[] header = builder.getBytes(UTF_8);

        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + header.length + read);
        byteBuffer.putShort(toShort(header.length));
        byteBuffer.put(header);
        if (read > 0) byteBuffer.put(audioData, 0, read);

        return byteBuffer;
    }

    public static ByteBuffer convert(@NonNull String requestId) {
        String builder = PATH.getValue() + ": " + AUDIO.getValue() + CRLF.getValue() +
                X_REQUEST_ID.getValue() + ": " + requestId + CRLF.getValue() +
                X_TIMESTAMP.getValue() + ": " + CurrentTime.newTime() + CRLF.getValue() +
                CONTENT_TYPE.getValue() + ": " + AUDIO_WAV.getValue() + CRLF.getValue() + CRLF.getValue();

        byte[] header = builder.getBytes(UTF_8);

        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + header.length);
        byteBuffer.putShort(toShort(header.length));
        byteBuffer.put(header);

        return byteBuffer;
    }

    public static ByteBuffer convertFirst(byte[] audioData,
                                          @IntRange(from = 0) int read,
                                          @NonNull String requestId) {
        byte[] wavHeader = getWavHeader();

        String builder = PATH.getValue() + ": " + AUDIO.getValue() + CRLF.getValue() +
                X_REQUEST_ID.getValue() + ": " + requestId + CRLF.getValue() +
                X_TIMESTAMP.getValue() + ": " + CurrentTime.newTime() + CRLF.getValue() +
                CONTENT_TYPE.getValue() + ": " + AUDIO_WAV.getValue() + CRLF.getValue() + CRLF.getValue();

        byte[] header = builder.getBytes(UTF_8);

        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + wavHeader.length + header.length + read);
        byteBuffer.putShort(toShort(header.length));
        byteBuffer.put(header);
        byteBuffer.put(wavHeader);
        if (read > 0) byteBuffer.put(audioData, 0, read);

        return byteBuffer;
    }

    private static byte[] getWavHeader() {
        // We don't know ahead of time about the length of audio to stream. So set to 0.
        int totalAudioSize = 0;
        // default
        byte formatSize = 16;
        // PCM format code.
        byte formatCode = 1;
        // monaural
        byte channel = 1;
        int byteRate = SAMPLING_RATE * channel * 2;

        byte[] header = new byte[44];

        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        header[4] = (byte) (totalAudioSize);
        header[5] = (byte) (totalAudioSize);
        header[6] = (byte) (totalAudioSize);
        header[7] = (byte) (totalAudioSize);

        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // 'fmt ' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';

        // 4 bytes: size of 'fmt ' chunk
        header[16] = formatSize;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;

        header[20] = formatCode;
        header[21] = 0;

        header[22] = channel;
        header[23] = 0;

        header[24] = (byte) (SAMPLING_RATE & 0xff);
        header[25] = (byte) ((SAMPLING_RATE >> 8) & 0xff);
        header[26] = (byte) (0);
        header[27] = (byte) (0);

        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        // block align
        header[32] = (byte) (16 / 8);
        header[33] = 0;

        // bits per sample
        header[34] = BITS_PER_SAMPLE;
        header[35] = 0;

        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';

        header[40] = (byte) (0);
        header[41] = (byte) (0);
        header[42] = (byte) (0);
        header[43] = (byte) (0);

        return header;
    }

    private static short toShort(@IntRange(from = 0) int num) {
        if (num > MAX_VALUE) {
            throw new IllegalArgumentException(num + " > " + MAX_VALUE);
        } else if (num < MIN_VALUE) {
            throw new IllegalArgumentException(num + " < " + MIN_VALUE);
        }
        return (short) num;
    }
}
