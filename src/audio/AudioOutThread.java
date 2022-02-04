// Erik Icket, ON4PB - 2022

package audio;

import common.Constants;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class AudioOutThread extends Thread
{

    static final Logger logger = Logger.getLogger(audio.AudioOutThread.class.getName());
    public boolean stopRequest = false;

    private int nBitsPerSample = 16;
    private boolean bBigEndian = true;
    private int nChannels = 1;
    private int nFrameSize = (nBitsPerSample / 8) * nChannels;
    private AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    private AudioFormat audioFormat;

    private SourceDataLine sourceDataLine;
    private long startAt;
    private int sizeDataLineBuffer;
    private byte[] audioOutBytes;

    public AudioOutThread(String device, double[] dBufferOut, long startAt)
    {
        logger.fine("Audio out thread is started");

        // find all audio mixers      
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        boolean foundAudioOut = false;

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            logger.fine("next mixer : " + mixer.getMixerInfo().toString());

            // test for a source == audio out
            if (mixer.isLineSupported(new Line.Info(SourceDataLine.class)))
            {
                logger.fine("audio out -  source : " + mixer.getMixerInfo().getName());

                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(device))
                {
                    logger.info("Found received AudioOut device : " + device);

                    foundAudioOut = true;
                    audioFormat = new AudioFormat(encoding, Constants.SAMPLE_RATE, nBitsPerSample, nChannels, nFrameSize, Constants.SAMPLE_RATE, bBigEndian);

                    try
                    {
                        // Obtains a source data line that can be used for playing audio data 
                        sourceDataLine = (SourceDataLine) AudioSystem.getSourceDataLine(audioFormat, mixer.getMixerInfo());
                        logger.info("Source data line created");
                    }
                    catch (Exception ex)
                    {
                        logger.severe("Can't get SourceDataLine : " + ex.getMessage());
                    }

                    break;
                }
            }
        }

        if (!foundAudioOut)
        {
            logger.info("No Audio Out found");
            return;
        }

        try
        {
            sourceDataLine.open(audioFormat);
        }
        catch (LineUnavailableException ex)
        {
            logger.severe("LineUnavailableException : " + ex.getMessage());
        }
        sourceDataLine.start();

        logger.info("Audio starts : " + sourceDataLine.isOpen());

        this.startAt = startAt;

        sizeDataLineBuffer = sourceDataLine.getBufferSize();
        logger.fine("Size of data line buffer : " + sizeDataLineBuffer);

        audioOutBytes = new byte[2 * dBufferOut.length];
        logger.fine("New audio out buffer, available for write : " + sourceDataLine.available() + ", internal buffer size : " + sourceDataLine.getBufferSize());

        for (int i = 0; i < dBufferOut.length; i++)
        {
            // dBufferOut is float, audio is int
            int iAudioAmplitude = (int) Math.round(dBufferOut[i]);

            // this byte comes out first
            audioOutBytes[i * 2] = (byte) (iAudioAmplitude >> 8);
            // and then this one           
            audioOutBytes[i * 2 + 1] = (byte) iAudioAmplitude;

            logger.fine("iAudioAmplitude : " + iAudioAmplitude + ", dBufferOut : " + dBufferOut[i] + ", lo : " + audioOutBytes[i * 2 + 1] + ", high : " + audioOutBytes[i * 2]);
        }
    }

    public void run()
    {
        while (System.currentTimeMillis() < startAt)
        {
            try
            {
                if (stopRequest)
                {
                    sourceDataLine.stop();
                    sourceDataLine.close();

                    return;
                }

                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
            }
        }

        logger.info("Audio starts");

        for (int i = 0; i < audioOutBytes.length;)
        {
            if (stopRequest)
            {
                sourceDataLine.stop();
                sourceDataLine.close();

                return;
            }

            int nrOfBytesWritten = sourceDataLine.write(audioOutBytes, i, sizeDataLineBuffer);
            i = i + nrOfBytesWritten;

            logger.fine("Number of bytes written : " + nrOfBytesWritten + ", index now at : " + i);

            if ((i + sizeDataLineBuffer + 1) > audioOutBytes.length)
            {
                sizeDataLineBuffer = audioOutBytes.length - i;
                logger.fine("rest : " + sizeDataLineBuffer);
            }
        }

        sourceDataLine.stop();
        sourceDataLine.close();

        logger.info("Audio stops");
    }

    public static void delay(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
        }
    }
}
