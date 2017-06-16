package com.android.sample.cognitive.services.speaker.verification;

import com.microsoft.cognitive.speakerrecognition.SpeakerVerificationClient;
import com.microsoft.cognitive.speakerrecognition.SpeakerVerificationRestClient;

final class SpeakerVerificationFactory {

    // TODO: You must set your subscription key.
    private static final String SUBSCRIPTION_KEY = "<SUBSCRIPTION_KEY>";

    private static final SpeakerVerificationClient INSTANCE = new SpeakerVerificationRestClient(SUBSCRIPTION_KEY);

    static SpeakerVerificationClient getSpeakerVerificationClient() {
        return INSTANCE;
    }
}
