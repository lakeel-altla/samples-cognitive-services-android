package com.android.sample.ms.speech.api.speech.message;

public final class SpeechConfig {

    public Context context = new Context();

    public Os os;

    public Device device;

    public static class Context {

        public System system;
    }

    public static class System {

        public String version;
    }

    public static class Os {

        public String platform;

        public String name;

        public String version;
    }

    public static class Device {

        public String manufacturer;

        public String model;

        public String version;
    }
}
