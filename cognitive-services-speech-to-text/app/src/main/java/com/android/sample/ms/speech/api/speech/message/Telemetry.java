package com.android.sample.ms.speech.api.speech.message;

import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class Telemetry {

    public List<Map<String, Queue<String>>> ReceivedMessages;

    public List<Metrics> Metrics;

    public static class Metrics {

        public String Name;

        // Used by Connection telemetry only.
        public String Id;

        public String Start;

        public String End;

        public String Error;
    }
}
