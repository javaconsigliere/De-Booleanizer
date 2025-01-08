package org.jc;

import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.io.UByteArrayOutputStream;
import org.zoxweb.shared.util.ParamUtil;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AudioTool
{
    private AudioTool() {}

    public static void writeWavHeader(ByteArrayOutputStream out, int totalDataLen, float sampleRate, int channels, int byteRate) throws IOException {
        byte[] header = new byte[44];

        // RIFF header
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // fmt subchunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // Subchunk1Size for PCM
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;   // AudioFormat (1 for PCM)
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) ((int) sampleRate & 0xff);
        header[25] = (byte) (((int) sampleRate >> 8) & 0xff);
        header[26] = (byte) (((int) sampleRate >> 16) & 0xff);
        header[27] = (byte) (((int) sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8); // BlockAlign
        header[33] = 0;
        header[34] = 16;  // BitsPerSample
        header[35] = 0;

        // data subchunk
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalDataLen & 0xff);
        header[41] = (byte) ((totalDataLen >> 8) & 0xff);
        header[42] = (byte) ((totalDataLen >> 16) & 0xff);
        header[43] = (byte) ((totalDataLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    /**
     * Defines the audio format for recording.
     *
     * @return The AudioFormat instance.
     */
    private static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0f; // 16 kHz
        int sampleSizeInBits = 16;    // 16 bits
        int channels = 1;             // Mono
        boolean signed = true;        // Signed data
        boolean bigEndian = false;    // Little-endian

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    /**
     * Records audio for the specified duration and returns the WAVE byte array.
     *
     * @param recordTimeInSeconds Duration of recording in seconds.
     * @return Byte array representing the complete WAVE file, or null if recording fails.
     */
    public static UByteArrayOutputStream recordAudio(int recordTimeInSeconds) throws IOException {
        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        TargetDataLine targetLine = null;

        try
        {
            if (!AudioSystem.isLineSupported(info))
                throw new IllegalArgumentException("The system does not support the specified format.");


            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            targetLine.start();

            System.out.println("Recording started...");

            byte[] buffer = new byte[4096];

            long endTime = System.currentTimeMillis() + recordTimeInSeconds * 1000;

            while (System.currentTimeMillis() < endTime) {
                int bytesRead = targetLine.read(buffer, 0, buffer.length);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Recording stopped.");

        } catch (LineUnavailableException ex) {
            System.err.println("Microphone not available.");
            ex.printStackTrace();
            return null;
        } finally {
            if (targetLine != null) {
                targetLine.stop();
                targetLine.close();
            }
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] audioData = byteArrayOutputStream.toByteArray();
        return createWavFile(audioData, format);
    }

    /**
     * Creates a WAVE byte array by adding a header to the raw audio data.
     *
     * @param audioData The raw audio data.
     * @param format    The audio format.
     * @return A byte array representing the complete WAVE file, or null if an error occurs.
     */
    public static UByteArrayOutputStream createWavFile(byte[] audioData, AudioFormat format) throws IOException {
        UByteArrayOutputStream out = new UByteArrayOutputStream();

        int totalDataLen = audioData.length + 36;
        int byteRate = (int) format.getSampleRate() * format.getChannels() * format.getSampleSizeInBits() / 8;

        // Write WAVE header
        writeWavHeader(out, totalDataLen, format.getSampleRate(), format.getChannels(), byteRate);

        // Write audio data
        out.write(audioData);

        return out;
    }

    /**
     * Saves the WAVE byte array to a file.
     *
     * @param wave   The WAVE byte array.
     * @param os The path to save the WAVE file.
     */
    public static void saveWavToFile(UByteArrayOutputStream wave, OutputStream os, boolean closeOS) throws IOException {
        try
        {
            wave.writeTo(os, 4096);
        }
        finally
        {
            if(closeOS) IOUtil.close(os);
        }
    }



    public static void displayMics() {
        List<Mixer.Info> microphones = getAvailableMics();

        if (microphones.isEmpty()) {
            System.out.println("No microphones found on this system.");
        } else {
            System.out.println("Available Microphones:");
            for (int i = 0; i < microphones.size(); i++) {
                Mixer.Info mixerInfo = microphones.get(i);
                Mixer mixer = AudioSystem.getMixer(mixerInfo);

                System.out.println((i + 1) + ". " + mixerInfo.getName());
                System.out.println("   Description: " + mixerInfo.getDescription());
                System.out.println("   Vendor: " + mixerInfo.getVendor());
                System.out.println("   Version: " + mixerInfo.getVersion());

                // List supported formats
                Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
                for (Line.Info lineInfo : targetLineInfos) {
                    if (lineInfo instanceof DataLine.Info) {
                        DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
                        AudioFormat[] formats = dataLineInfo.getFormats();

                        System.out.println("   Supported Formats:");
                        for (AudioFormat format : formats) {
                            System.out.println("      " + format.toString());
                        }
                    }
                }
                System.out.println();
            }
        }
    }

    /**
     * Retrieves a list of available microphone devices on the system.
     *
     * @return List of Mixer.Info objects representing microphones.
     */
    public static List<Mixer.Info> getAvailableMics() {
        List<Mixer.Info> microphoneList = new ArrayList<>();
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

            if (targetLineInfos.length > 0) { // Mixer supports audio input
                // Optional: Further filter based on name
                String mixerName = mixerInfo.getName().toLowerCase();
                if (mixerName.contains("microphone") || mixerName.contains("input")) {
                    microphoneList.add(mixerInfo);
                }
            }
        }

        return microphoneList;
    }


    public static void main(String[] args) {
        try
        {
            displayMics();
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);
            int duration = params.intValue("d", 5);


            String outputFilePath = params.stringValue("of","recorded_audio.wav");
            if(!outputFilePath.toLowerCase().endsWith(".wav"))
            {
                outputFilePath += ".wav";
            }

            // Record audio
            UByteArrayOutputStream recordingOS = AudioTool.recordAudio(duration);

            // Save to file
            AudioTool.saveWavToFile(recordingOS, new FileOutputStream(outputFilePath), true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
