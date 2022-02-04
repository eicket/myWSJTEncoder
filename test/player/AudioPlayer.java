package player;

import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.logging.Logger;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer
{
    static Logger logger = Logger.getLogger(player.AudioPlayer.class.getName());

    public static void main(String[] args)
    {
        int nWaveformType = Oscillator.WAVEFORM_SINE;
        float fSampleRate = 44100.0F;
        float fSignalFrequency = 1000.0F;
        float fAmplitude = 0.7F;
        //  Default is 10 seconds.
        int nDuration = 10;
/*
        Getopt g = new Getopt("AudioPlayer", args, "t:r:f:a:d:");
        int c;
        while ((c = g.getopt()) != -1)
        {
            switch (c)
            {
                case 't':
                    nWaveformType = getWaveformType(g.getOptarg());
                    break;

                case 'r':
                    fSampleRate = Float.parseFloat(g.getOptarg());
                    break;

                case 'f':
                    fSignalFrequency = Float.parseFloat(g.getOptarg());
                    break;

                case 'a':
                    fAmplitude = Float.parseFloat(g.getOptarg());
                    break;

                case 'd':
                    String str = g.getOptarg();
                    nDuration = Integer.parseInt(str);
                    nDuration = Integer.parseInt(g.getOptarg());
                    break;

                default:
                    break;
            }
        }
*/
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fSampleRate, 16, 2, 4, fSampleRate, false);
        int nLengthInFrames = Math.round(nDuration * fSampleRate);

        SourceDataLine sourceDataLine = null;
        try
        {
            sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
        }
        catch (Exception e)
        {
            logger.severe("Error opening source data line : " + e.getMessage());
        }

        int sizeDataLineBuffer = sourceDataLine.getBufferSize();
        logger.fine("Size of data line buffer : " + sizeDataLineBuffer);

        Oscillator oscillator = new Oscillator(nWaveformType, fSignalFrequency, fAmplitude, audioFormat, nLengthInFrames);

        int sizeAudioSample = 0;
        try
        {
            sizeAudioSample = oscillator.available();
            byte[] audioSample = new byte[sizeAudioSample];
            logger.fine("Nr of bytes in sample : " + sizeAudioSample);
            oscillator.read(audioSample, 0, sizeAudioSample);

            for (int i = 0; i < sizeAudioSample;)
            {
                int nrOfBytesWritten = sourceDataLine.write(audioSample, i, sizeDataLineBuffer);
                i = i + nrOfBytesWritten;
                logger.fine("Nr of bytes written : " + nrOfBytesWritten + ", index now at : " + i);
            }
        }
        catch (IOException e)
        {
            logger.severe("Error in oscillator : " + e.getMessage());
        }
    }

    private static int getWaveformType(String strWaveformType)
    {
        int nWaveformType = Oscillator.WAVEFORM_SINE;
        strWaveformType = strWaveformType.trim().toLowerCase();
        if (strWaveformType.equals("sine"))
        {
            nWaveformType = Oscillator.WAVEFORM_SINE;
        }
        else if (strWaveformType.equals("square"))
        {
            nWaveformType = Oscillator.WAVEFORM_SQUARE;
        }
        else if (strWaveformType.equals("triangle"))
        {
            nWaveformType = Oscillator.WAVEFORM_TRIANGLE;
        }
        else if (strWaveformType.equals("sawtooth"))
        {
            nWaveformType = Oscillator.WAVEFORM_SAWTOOTH;
        }
        return nWaveformType;
    }
}


