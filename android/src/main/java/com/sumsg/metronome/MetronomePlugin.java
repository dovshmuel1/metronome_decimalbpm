package com.sumsg.metronome;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** MetronomePlugin */
public class MetronomePlugin implements FlutterPlugin, MethodCallHandler {
    private MethodChannel channel;
    private EventChannel eventTick;
    private EventChannel.EventSink eventTickSink;
    private Metronome metronome = null;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "metronome");
        channel.setMethodCallHandler(this);

        eventTick = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "metronome_tick");
        eventTick.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object args, EventChannel.EventSink events) {
                eventTickSink = events;
            }

            @Override
            public void onCancel(Object args) {
                eventTickSink = null;
            }
        });
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "init":
                metronomeInit(call);
                break;
            case "play":
                metronome.play();
                break;
            case "pause":
                metronome.pause();
                break;
            case "stop":
                metronome.stop();
                break;
            case "getVolume":
                result.success(metronome.audioVolume);
                break;
            case "setVolume":
                setVolume(call);
                break;
            case "isPlaying":
                result.success(metronome.isPlaying());
                break;
            case "setBPM":
                setBPM(call);
                break;
            case "getBPM":
                result.success(metronome.audioBpm);
                break;
            case "setTimeSignature":
                setTimeSignature(call);
                break;
            case "getTimeSignature":
                result.success(metronome.audioTimeSignature);
                break;
            case "setAudioFile":
                setAudioFile(call);
                break;
            case "destroy":
                metronome.destroy();
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        eventTick.setStreamHandler(null);
    }

    private void metronomeInit(@NonNull MethodCall call) {
        byte[] mainFileBytes = call.argument("mainFileBytes");
        if (mainFileBytes == null) mainFileBytes = new byte[0];

        byte[] accentedFileBytes = call.argument("accentedFileBytes");
        if (accentedFileBytes == null) accentedFileBytes = new byte[0];

        boolean enableTickCallback = Boolean.TRUE.equals(call.argument("enableTickCallback"));

        Integer timeSignature = call.argument("timeSignature");
        int timeSignatureValue = (timeSignature != null) ? timeSignature : 0;

        // DECIMAL BPM MOD: parse Number instead of Integer
        Number bpmValue = call.argument("bpm");
        double bpm = (bpmValue != null) ? bpmValue.doubleValue() : 120.0;

        Double volumeValue = call.argument("volume");
        float volume = (volumeValue != null) ? volumeValue.floatValue() : 0.5F;

        Integer sampleRateValue = call.argument("sampleRate");
        int sampleRate = (sampleRateValue != null) ? sampleRateValue : 44100;

        metronome = new Metronome(mainFileBytes, accentedFileBytes, bpm, timeSignatureValue, volume, sampleRate);

        if (enableTickCallback && eventTickSink != null) {
            metronome.enableTickCallback(eventTickSink);
        }
    }

    private void setVolume(@NonNull MethodCall call) {
        if (metronome != null) {
            Double _volume = call.argument("volume");
            if (_volume != null) {
                float _volume1 = _volume.floatValue();
                metronome.setVolume(_volume1);
            }
        }
    }

    private void setBPM(@NonNull MethodCall call) {
        if (metronome != null) {
            Number _bpm = call.argument("bpm"); // DECIMAL BPM MOD
            if (_bpm != null) {
                metronome.setBPM(_bpm.doubleValue()); // DECIMAL BPM MOD
            }
        }
    }

    private void setTimeSignature(@NonNull MethodCall call) {
        if (metronome != null) {
            Integer _timeSignature = call.argument("timeSignature");
            if (_timeSignature != null) {
                metronome.setTimeSignature(_timeSignature);
            }
        }
    }

    private void setAudioFile(@NonNull MethodCall call) {
        if (metronome != null) {
            byte[] mainFileBytes = call.argument("mainFileBytes");
            byte[] accentedFileBytes = call.argument("accentedFileBytes");

            if (mainFileBytes == null) mainFileBytes = new byte[0];
            if (accentedFileBytes == null) accentedFileBytes = new byte[0];

            metronome.setAudioFile(mainFileBytes, accentedFileBytes);
        }
    }
}
