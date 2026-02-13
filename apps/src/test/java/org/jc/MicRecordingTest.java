package org.jc;

import io.xlogistx.audio.AudioRecorder;
import io.xlogistx.audio.AudioUtil;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.io.SharedIOUtil;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.ParamUtil;
import org.zoxweb.shared.util.RateController;

import java.io.FileOutputStream;
import java.io.InputStream;

public class MicRecordingTest
{

    //private static final LogWrapper log = new LogWrapper(MicRecordingTest.class).setEnabled(true);
    private MicRecordingTest() {}



    private static InputStream threaded(int durationInSec) throws Exception {
        AudioRecorder ar = new AudioRecorder(AudioUtil.defaultAudioFormat(), true );
        long durationToMillis = (Const.TimeInMillis.SECOND.MILLIS*durationInSec);
        System.out.println(durationInSec + " " +durationToMillis);
        System.out.println("started recording " + Const.TimeInMillis.toString(System.currentTimeMillis()) + " " + Const.TimeInMillis.toString(durationToMillis));
        TaskUtil.defaultTaskProcessor().execute(ar);
        ar.setStatus(AudioRecorder.Status.RECORDING);
        RateController rc = new RateController("AudioChunk", "12/m");
        rc.nextWait();
//        TaskUtil.defaultTaskScheduler().queue(rc.nextWait(), ()->{
//            ar.setStatus(AudioRecorder.Status.STOP_RECORDING);
//            System.out.println("Stopped recording " + Const.TimeInMillis.toString(System.currentTimeMillis()) + " " + Const.TimeInMillis.toString(durationToMillis));
//        });
//        TaskUtil.defaultTaskScheduler().queue(rc.nextWait(), ()->{
//            ar.setStatus(AudioRecorder.Status.RECORDING);
//            System.out.println("Restarted recording " + Const.TimeInMillis.toString(System.currentTimeMillis()));
//
//        });

        TaskUtil.defaultTaskScheduler().queue(durationToMillis, ()->{
            SharedIOUtil.close(ar);System.out.println("Closed " + Const.TimeInMillis.toString(System.currentTimeMillis()));});

        TaskUtil.waitIfBusy(300);
        TaskUtil.close();

        return ar.getRecordedStream();
    }


    public static void main(String[] args) {
        try
        {
            AudioUtil.displayMics();
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);
            int duration = params.intValue("d", 5);


            String outputFilePath = params.stringValue("of","recorded_audio.wav");
            if(!outputFilePath.toLowerCase().endsWith(".wav"))
            {
                outputFilePath += ".wav";
            }

            // Record audio
//            UByteArrayOutputStream recordingOS = AudioUtil.recordAudio(null, AudioUtil.defaultAudioFormat(), duration);
//            IOUtil.relayStreams(AudioUtil.createWavStream(recordingOS, AudioUtil.defaultAudioFormat()),new FileOutputStream(outputFilePath), true );

            System.out.println("Duration " + duration);
            IOUtil.relayStreams(threaded(duration),new FileOutputStream(outputFilePath), true );
            System.out.println(outputFilePath + " saved." );

            // Save to file
            //AudioUtil.saveWavToFile(recordingOS, new FileOutputStream(outputFilePath), true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
