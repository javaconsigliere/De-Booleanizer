package org.jc;

import org.jc.audio.AudioUtil;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.io.UByteArrayOutputStream;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class RobustAudioRecorder {
    private List<AudioFormat> formatsToTry = Arrays.asList(
            new AudioFormat(16000.0f, 16, 1, true, false),
            new AudioFormat(44100.0f, 16, 2, true, false), // CD Quality
            new AudioFormat(8000.0f, 16, 1, true, false) // Telephony Qualit// Low Quality
    );

    public AudioFormat findSupportedFormat(Mixer mixer) {
        for (AudioFormat format : formatsToTry) {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (mixer.isLineSupported(info)) {
                System.out.println("Supported format found: " + format.toString());
                return format;
            }
        }
        return null;
    }

    public Mixer findSuitableMixer() {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

            if (targetLineInfos.length > 0) {
                AudioFormat supportedFormat = findSupportedFormat(mixer);
                if (supportedFormat != null) {
                    System.out.println("Selected Mixer: " + mixerInfo.getName());
                    return mixer;
                }
            }
        }
        System.err.println("No suitable mixer found.");
        return null;
    }

    public InputStream record(String outputFilePath, int recordTimeInSeconds) {
        Mixer mixer = findSuitableMixer();
        if (mixer == null) {
            System.err.println("Recording aborted due to no suitable mixer.");
            return null;
        }

        AudioFormat format = null;
        DataLine.Info info = null;

        // Find the supported format for the selected mixer
        for (AudioFormat fmt : formatsToTry) {
            info = new DataLine.Info(TargetDataLine.class, fmt);
            if (mixer.isLineSupported(info)) {
                format = fmt;
                break;
            }
        }

        if (format == null) {
            System.err.println("No supported AudioFormat found for the selected mixer.");
            return null;
        }


        UByteArrayOutputStream baos = new UByteArrayOutputStream();
        try (TargetDataLine targetLine = (TargetDataLine) mixer.getLine(info)) {
            targetLine.open(format);
            targetLine.start();

            System.out.println("Recording started with format: " + format.toString());

            AudioInputStream audioStream = new AudioInputStream(targetLine);
            Thread stopper = new Thread(() -> {
                try {
                    Thread.sleep(recordTimeInSeconds * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                targetLine.stop();
                targetLine.close();
                System.out.println("Recording stopped.");
            });

            stopper.start();

            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, new FileOutputStream(new File(outputFilePath)));

        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable for the selected format.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O error during recording.");
            e.printStackTrace();
        }

        return baos.toByteArrayInputStream();
    }



    public static void main(String[] args) {
        RobustAudioRecorder recorder = new RobustAudioRecorder();
        String outputFile = "robust_recording.wav";
        int duration = 10; // seconds
        //recorder.record(outputFile, duration);
        UByteArrayOutputStream baos = AudioUtil.recordAudio(duration);
        try {
            IOUtil.relayStreams(baos.toByteArrayInputStream(), new FileOutputStream(outputFile), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

