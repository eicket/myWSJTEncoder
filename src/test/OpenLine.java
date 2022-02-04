/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

public class OpenLine
{

    static final Logger logger = Logger.getLogger(test.OpenLine.class.getName());

    public static void main(String argv[])
    {
        // set the format

        int nBitsPerSample = 16;
        boolean bBigEndian = true;
        int nChannels = 2;
        int nFrameSize = (nBitsPerSample / 8) * nChannels;
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float sampleRate = 48000;

        AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);

        TargetDataLine line;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info))
        {
            // Handle the error.
        }



        // Obtain and open the line.
        try
        {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);

            logger.info("Line opened : " + line.getLineInfo().toString());
        }
        catch (LineUnavailableException ex)
        {
            // Handle the error.
            //... 
        }

        // add
        if (AudioSystem.isLineSupported(Port.Info.MICROPHONE))
        {
            try
            {
                Port p = (Port) AudioSystem.getLine(Port.Info.MICROPHONE);
                                
                logger.info("Port opened : " + p.getLineInfo().toString());
            }
            catch (Exception ex)
            {
                logger.info("Exception for port : " + ex.getMessage());
                            
            }
        }

    }
}
