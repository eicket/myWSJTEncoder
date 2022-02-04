package player;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;

public class Oscillator extends AudioInputStream
{
    public static final int WAVEFORM_SINE = 0;
    public static final int WAVEFORM_SQUARE = 1;
    public static final int WAVEFORM_TRIANGLE = 2;
    public static final int WAVEFORM_SAWTOOTH = 3;
    private byte[] buffer;
    private int ibuffer;
    private long remainingFrames;

    public Oscillator(int nWaveformType, float fSignalFrequency, float fAmplitude, AudioFormat audioFormat, long lLength)
    {
        // calls AudioInputStream constructor    
        super(new ByteArrayInputStream(new byte[0]), new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
                2, 4, audioFormat.getFrameRate(), audioFormat.isBigEndian()), lLength);

        remainingFrames = lLength;
        fAmplitude = (float) (fAmplitude * Math.pow(2, getFormat().getSampleSizeInBits() - 1));
        // length of one period in frames
        int nPeriodLengthInFrames = Math.round(getFormat().getFrameRate() / fSignalFrequency);
        int nBuffer = nPeriodLengthInFrames * getFormat().getFrameSize();
        buffer = new byte[nBuffer];
        for (int nFrame = 0; nFrame < nPeriodLengthInFrames; nFrame++)
        {
            // The relative position inside the period of the waveform. 0.0 = beginning, 1.0 = end
            float fPeriodPosition = (float) nFrame / (float) nPeriodLengthInFrames;
            float fValue = 0;
            switch (nWaveformType)
            {
                case WAVEFORM_SINE:
                    fValue = (float) Math.sin(fPeriodPosition * 2.0 * Math.PI);
                    break;

                case WAVEFORM_SQUARE:
                    fValue = (fPeriodPosition < 0.5F) ? 1.0F : -1.0F;
                    break;

                case WAVEFORM_TRIANGLE:
                    if (fPeriodPosition < 0.25F)
                    {
                        fValue = 4.0F * fPeriodPosition;
                    }
                    else if (fPeriodPosition < 0.75F)
                    {
                        fValue = -4.0F * (fPeriodPosition - 0.5F);
                    }
                    else
                    {
                        fValue = 4.0F * (fPeriodPosition - 1.0F);
                    }
                    break;

                case WAVEFORM_SAWTOOTH:
                    if (fPeriodPosition < 0.5F)
                    {
                        fValue = 2.0F * fPeriodPosition;
                    }
                    else
                    {
                        fValue = 2.0F * (fPeriodPosition - 1.0F);
                    }
                    break;
            }
            int nValue = Math.round(fValue * fAmplitude);
            int nBaseAddr = (nFrame) * getFormat().getFrameSize();
            // this is for 16 bit stereo, little endian
            buffer[nBaseAddr + 0] = (byte) (nValue & 0xFF);
            buffer[nBaseAddr + 1] = (byte) ((nValue >>> 8) & 0xFF);
            buffer[nBaseAddr + 2] = (byte) (nValue & 0xFF);
            buffer[nBaseAddr + 3] = (byte) ((nValue >>> 8) & 0xFF);
        }
        ibuffer = 0;
    }

    /**	Returns the number of bytes that can be read without blocking.
    Since there is no blocking possible here, we simply try to
    return the number of bytes available at all. In case the
    length of the stream is indefinite, we return the highest
    number that can be represented in an integer. If the length
    if finite, this length is returned, clipped by the maximum
    that can be represented.
     */
    public int available()
    {
        int nAvailable = 0;
        if (remainingFrames == AudioSystem.NOT_SPECIFIED)
        {
            nAvailable = Integer.MAX_VALUE;
        }
        else
        {
            long lBytesAvailable = remainingFrames * getFormat().getFrameSize();
            nAvailable = (int) Math.min(lBytesAvailable, (long) Integer.MAX_VALUE);
        }
        return nAvailable;
    }

    public int read(byte[] abData, int nOffset, int nLength) throws IOException
    {
        if (nLength % getFormat().getFrameSize() != 0)
        {
            throw new IOException("length must be an integer multiple of frame size");
        }
        int nConstrainedLength = Math.min(available(), nLength);
        int nRemainingLength = nConstrainedLength;
        while (nRemainingLength > 0)
        {
            int nNumBytesToCopyNow = buffer.length - ibuffer;
            nNumBytesToCopyNow = Math.min(nNumBytesToCopyNow, nRemainingLength);
            System.arraycopy(buffer, ibuffer, abData, nOffset, nNumBytesToCopyNow);
            nRemainingLength -= nNumBytesToCopyNow;
            nOffset += nNumBytesToCopyNow;
            ibuffer = (ibuffer + nNumBytesToCopyNow) % buffer.length;
        }
        int nFramesRead = nConstrainedLength / getFormat().getFrameSize();
        if (remainingFrames != AudioSystem.NOT_SPECIFIED)
        {
            remainingFrames -= nFramesRead;
        }
        int nReturn = nConstrainedLength;
        if (remainingFrames == 0)
        {
            nReturn = -1;
        }

        return nReturn;
    }
}


