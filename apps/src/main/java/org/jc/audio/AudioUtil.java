package org.jc.audio;

import org.zoxweb.server.io.UByteArrayOutputStream;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.RateCounter;
import org.zoxweb.shared.util.SharedUtil;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AudioUtil {
    public static final LogWrapper log = new LogWrapper(AudioUtil.class).setEnabled(true);
    private AudioUtil(){}

    public static void main(String[] args) {
        RateCounter rc = new RateCounter("mics");
        rc.start();
        List<Mixer.Info> microphones = listMixers();
        rc.stop();
        if (microphones.isEmpty()) {
            if(log.isEnabled()) log.getLogger().info("No microphones found on this system.");
        } else {
            if(log.isEnabled()) log.getLogger().info("Available Microphones:");
            for (int i = 0; i < microphones.size(); i++) {
                Mixer.Info info = microphones.get(i);
                if(log.isEnabled()) log.getLogger().info((i + 1) + ". " + info.getName());
                if(log.isEnabled()) log.getLogger().info("   Description: " + info.getDescription());
                if(log.isEnabled()) log.getLogger().info("   Vendor: " + info.getVendor());
                if(log.isEnabled()) log.getLogger().info("   Version: " + info.getVersion());
                if(log.isEnabled()) log.getLogger().info("Discovery " + Const.TimeInMillis.toString(rc.lastDeltaInMillis()));
            }
        }

        Mixer.Info mic = defaultMicrophone();
        System.out.println("Default microphone " + toString(mic));
        if(mic != null)
        {
            findSupportedFormat(defaultAudioFormat());
            try {
//                UByteArrayOutputStream baos = new UByteArrayOutputStream();
//                recordAudio(mic, defaultAudioFormat(), AudioFileFormat.Type.WAVE, baos, 10);
//                if(log.isEnabled()) log.getLogger().info(baos.toString(false));
                UByteArrayOutputStream baos = recordAudio(5);
                ByteArrayInputStream is = createWAVStream(baos, defaultAudioFormat());
//                IOUtil.relayStreams(is, new FileOutputStream("test.wav"), true);
                is.reset();
                Clip clip = loadClip(is);

                TaskUtil.defaultTaskScheduler();


                clip.addLineListener(e-> System.out.println(e + "    " + Thread.currentThread()));
                clip.start();
                TaskUtil.sleep(250);


//                while(clip.isRunning())
//                {
//                    TaskUtil.sleep(250);
//                    System.out.println("dsdsdsd  " + Thread.currentThread());
//                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    public static ByteArrayInputStream createWAVStream(UByteArrayOutputStream audioData, AudioFormat format) {
            int totalDataLen = audioData.size() + 36;
            int byteRate = (int) format.getSampleRate() * format.getChannels() * format.getSampleSizeInBits() / 8;


            audioData.insertAt(0, createWavHeader(totalDataLen, format.getSampleRate(), format.getChannels(), byteRate));

            return audioData.toByteArrayInputStream();
    }


    public static  AudioFormat defaultAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1; // Mono
        boolean signed = true;
        boolean bigEndian = false;

        //return new AudioFormat(8000.0f, 16, 1, true, true);
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }


    public static AudioFormat findSupportedFormat(AudioFormat format) {
        {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (AudioSystem.isLineSupported(info)) {
                System.out.println("Supported format found: " + format.toString());
                return format;
            }
        }
        System.err.println("No supported AudioFormat found.");
        return null;
    }

    public static void recordAudio(Mixer.Info mixerInfo,
                                   AudioFormat format,
                                   AudioFileFormat.Type type,
                                   OutputStream outputStream,
                                   int recordTimeInSeconds) throws IOException, LineUnavailableException
    {

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try (//Mixer mixer = AudioSystem.getMixer(mixerInfo);
             TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info)) {

            targetLine.open(format);
            targetLine.start();

            if(log.isEnabled()) log.getLogger().info("Recording started...");

            AudioInputStream audioStream = new AudioInputStream(targetLine);

            // Create a thread to stop recording after the specified time
            Thread stopper = new Thread(() -> {
                TaskUtil.sleep(Const.TimeInMillis.SECOND.MILLIS*recordTimeInSeconds);
                targetLine.stop();
                targetLine.close();
                if(log.isEnabled()) log.getLogger().info("Recording stopped.");
            });

            stopper.start();
            TaskUtil.sleep(Const.TimeInMillis.SECOND.MILLIS*(recordTimeInSeconds +5));
            // Write the audio data to the file
            AudioSystem.write(audioStream, type, outputStream);

        }
    }


    public static byte[] createWavHeader(int totalDataLen, float sampleRate, int channels, int byteRate)
    {
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
        return header;
    }




    public static UByteArrayOutputStream recordAudio(int recordTimeInSeconds) {
        AudioFormat format = defaultAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        UByteArrayOutputStream byteArrayOutputStream = new UByteArrayOutputStream();
        TargetDataLine targetLine = null;

        try {
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("The system does not support the specified format.");
                return null;
            }

            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            targetLine.start();

            System.out.println("Recording started...");

            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            long endTime = System.currentTimeMillis() + recordTimeInSeconds * 1000;

            while (System.currentTimeMillis() < endTime) {
                bytesRead = targetLine.read(buffer, 0, buffer.length);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Recording stopped.");

        } catch (LineUnavailableException ex) {
            System.err.println("Microphone not available.");
            ex.printStackTrace();
        } finally {
            if (targetLine != null) {
                targetLine.stop();
                targetLine.close();
            }
        }

        return byteArrayOutputStream;
    }


    public static UByteArrayOutputStream recordAudioThreaded(int recordTimeInSeconds) {
        AudioFormat format = defaultAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        UByteArrayOutputStream byteArrayOutputStream = new UByteArrayOutputStream();
        TargetDataLine targetLine = null;

        try {
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("The system does not support the specified format.");
                return null;
            }

            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            targetLine.start();

            System.out.println("Recording started...");

            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            long endTime = System.currentTimeMillis() + recordTimeInSeconds * 1000;

            while (System.currentTimeMillis() < endTime) {
                bytesRead = targetLine.read(buffer, 0, buffer.length);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Recording stopped.");

        } catch (LineUnavailableException ex) {
            System.err.println("Microphone not available.");
            ex.printStackTrace();
        } finally {
            if (targetLine != null) {
                targetLine.stop();
                targetLine.close();
            }
        }

        return byteArrayOutputStream;
    }

    /**
     * Retrieves a list of available microphone devices on the system.
     *
     * @return List of Mixer.Info objects representing microphones.
     */
    public static List<Mixer.Info> listMixers() {
        List<Mixer.Info> microphoneList = new ArrayList<>();

        // Get all available mixers
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            // Get target lines (for capturing audio)
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

            if (targetLineInfos.length > 0) {
                // Optional: Further filter mixers by checking names or supported formats
                // For example, include only mixers whose names contain "Microphone"
                if (mixerInfo.getName().toLowerCase().contains("microphone")) {
                    microphoneList.add(mixerInfo);
                } else {
                    // Alternatively, include all mixers with target lines
                    microphoneList.add(mixerInfo);
                }
            }
        }

        return microphoneList;
    }

    public static Mixer.Info defaultMicrophone() {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

            if (targetLineInfos.length > 0) {
                // Attempt to identify default microphone based on common naming conventions
                String name = mixerInfo.getName().toLowerCase();
                if (name.contains("default") || name.contains("microphone") || name.contains("input")) {
                    return mixerInfo;
                }
            }
        }

        return null; // Default microphone not found
    }

    public static void listLines(Mixer.Info mixerInfo) {
        Mixer mixer = AudioSystem.getMixer(mixerInfo);
        Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

        if(log.isEnabled()) log.getLogger().info("Lines for Mixer: " + mixerInfo.getName());
        for (Line.Info lineInfo : targetLineInfos) {
            if(log.isEnabled()) log.getLogger().info(" - " + lineInfo.toString());
        }
    }

    public static Clip loadClip(InputStream audioIS) throws LineUnavailableException, IOException, UnsupportedAudioFileException {

        SharedUtil.checkIfNulls("Null input stream", audioIS);
        Clip clip;
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioIS)) {
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            if(log.isEnabled()) log.getLogger().info("Clip Opened: " + toString(clip));
        }

        return clip;
    }

    public static String toString(Clip c)
    {

        return c == null ? "null" : SharedUtil.toCanonicalID(',', c.getFormat(),
                c.getFrameLength(), c.getLineInfo()) ;
    }

    public static String toString(Mixer.Info mi)
    {

        return mi == null ? "null" : SharedUtil.toCanonicalID(',', mi.getName(),
                mi.getDescription(), mi.getVersion(), mi.getVendor()) ;
    }




}
