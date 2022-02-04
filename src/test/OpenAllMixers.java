/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line.Info;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;


import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author eicket
 */
public class OpenAllMixers
{

    static final Logger logger = Logger.getLogger(OpenAllMixers.class.getName());

    public static void main(String argv[])
    {
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        for (int i = 0; i < aInfos.length; i++)
        {
            logger.info("found audio in : " + aInfos[i].getName());
            
        }


        // default : PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

        float sampleRate = 44100.0F;
        int nBitsPerSample = 16;
        boolean bBigEndian = true;
        int nChannels = 1;
        int nFrameSize = (nBitsPerSample / 8) * nChannels;

        AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);
        logger.info("Mixer : " + aInfos[3] + ", target audio format: " + audioFormat);

         
         
         
        // int nInternalBufferSize = AudioSystem.NOT_SPECIFIED;
        int nInternalBufferSize = 8 * 8192;
        TargetDataLine targetDataLine = null;
        try
        {
            // targetDataLine = (TargetDataLine) AudioCommon.getTargetDataLine(mixerName, audioFormat, nInternalBufferSize);
            
            
            // [0], [2] is nok, falls in output devices ... how to detect ?
            
            targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, aInfos[5]);
            Info info = targetDataLine.getLineInfo();
 
        }
        catch (LineUnavailableException ex)
        {
            logger.severe("Can't get TargetDataLine");
        }
        //
        if (targetDataLine == null)
        {
            logger.severe("Can't get TargetDataLine");
        }
        //

        logger.fine("Target data line created : " + targetDataLine.toString());
        targetDataLine.start();

    }
}
