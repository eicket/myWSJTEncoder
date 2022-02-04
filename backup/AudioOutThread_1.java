package audio;

import java.util.logging.Logger;
import javax.sound.sampled.SourceDataLine;

public class AudioOutThread extends Thread
{

    static final Logger logger = Logger.getLogger(audio.AudioOutThread.class.getName());
    public boolean stopRequest = false;

    SourceDataLine sourceDataLine;
    long startAt;
    int sizeDataLineBuffer;
    byte[] audioOutBytes;

    // -- 16 bit
    public AudioOutThread(SourceDataLine sourceDataLine, double[] dBufferOut, long startAt)
    {
        logger.fine("Audio out thread is started");

        this.sourceDataLine = sourceDataLine;
        this.startAt = startAt;

        sizeDataLineBuffer = sourceDataLine.getBufferSize();
        logger.fine("Size of data line buffer : " + sizeDataLineBuffer);

        audioOutBytes = new byte[2 * dBufferOut.length];
        logger.fine("New audio out buffer, available for write : " + sourceDataLine.available() + ", internal buffer size : " + sourceDataLine.getBufferSize());

        // Java is always big endian, mixer is little endian
        for (int i = 0; i < dBufferOut.length; i++)
        {
            // float of 1 will give max int
            //       int b = (int) (32767 * buffer[i]);
            //   int iAudioAmplitude = (int) (dBufferOut[i]);
            int iAudioAmplitude = (int) Math.round(dBufferOut[i]);

            audioOutBytes[i * 2] = (byte) iAudioAmplitude;
            //     audioOutBytes[i * 2 + 1] = (byte) ((int) iAudioAmplitude >> 8);
            audioOutBytes[i * 2 + 1] = (byte) (iAudioAmplitude >> 8);

            logger.fine("iAudioAmplitude : " + iAudioAmplitude + ", dBufferOut : " + dBufferOut[i] + ", lo : " + audioOutBytes[i * 2] + ", high : " + audioOutBytes[i * 2 + 1]);
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
