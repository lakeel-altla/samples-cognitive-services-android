package com.android.sample.cognitive.services.speaker.identification;

import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationClient;
import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationRestClient;

final class SpeakerIdentificationFactory {

    // TODO: You must set your subscription key.
    private static final String SUBSCRIPTION_KEY = "<SUBSCRIPTION_KEY>";

    private static final SpeakerIdentificationClient INSTANCE = new SpeakerIdentificationRestClient(SUBSCRIPTION_KEY);

    static SpeakerIdentificationClient getSpeakerIdentificationClient() {
        return INSTANCE;
    }
}
