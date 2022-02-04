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

    public AudioOutThread(SourceDataLine sourceDataLine, double[] dBufferOut, long startAt)
    {
        logger.fine("Audio out thread is started");

        this.sourceDataLine = sourceDataLine;
        this.startAt = startAt;

        sizeDataLineBuffer = sourceDataLine.getBufferSize();
        logger.fine("Size of data line buffer : " + sizeDataLineBuffer);

        audioOutBytes = new byte[2 * dBufferOut.length];
        logger.fine("New audio out buffer, available for write : " + sourceDataLine.available() + ", internal buffer size : " + sourceDataLine.getBufferSize());

        audioOutBytes = fromBufferToAudioBytes(audioOutBytes, dBufferOut);
    }

    public void run()
    {
        while (System.currentTimeMillis() < startAt)
        {
            try
            {
                if (stopRequest)
                {
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

        logger.info("Audio stops");
    }

    public byte[] fromBufferToAudioBytes(byte[] audioBytes, double[] buffer)
    {       
        // Java is always big endian, mixer is little endian
        for (int i = 0; i < buffer.length; i++)
        {
            // float of 1 will give max int
            //       int b = (int) (32767 * buffer[i]);
            int b = (int) (buffer[i]);

            audioBytes[i * 2] = (byte) b;
            audioBytes[i * 2 + 1] = (byte) ((int) b >> 8);

            // logger.info("Float : " + b + ", lo : " + audioBytes[i * 2] + ", high : " + audioBytes[i * 2 + 1]);
        }
        return audioBytes;
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
