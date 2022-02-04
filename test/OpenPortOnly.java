/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class OpenPortOnly
{

    static final Logger logger = Logger.getLogger(test.OpenPortOnly.class.getName());

    public static void main(String argv[])
    {
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();
        Line.Info portInfo = new Line.Info(Port.class);

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);

            // Scan all the source ports :
            logger.info("Found mixer : " + mixer.getMixerInfo().getName());


            // only take the hardware port mixers
            if (mixer.isLineSupported(portInfo))
            {
                // my try
                Line.Info[] srcInfos = mixer.getSourceLineInfo();

                // Scan all the source ports :
                logger.info("    Scan all source ports");

                // take all source ports
                // ArrayList<Line.Info> srcInfos = new ArrayList<Line.Info>(Arrays.asList(mixer.getSourceLineInfo()));
                for (Line.Info srcInfo : srcInfos)
                {
                    Port.Info pi = (Port.Info) srcInfo;

                    logger.info("        Port found : " + mixer.getMixerInfo().getName() + "/" + pi.getName() + ", type : " + pi.isSource());


                    int nBitsPerSample = 16;
                    boolean bBigEndian = true;
                    int nChannels = 2;
                    int nFrameSize = (nBitsPerSample / 8) * nChannels;
                    AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
                    float sampleRate = 44100.0F;

                    // AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);

                    AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
                    int nInternalBufferSize = 8 * 8192;

                      
                      
                    try
                    {
                        SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getSourceDataLine(audioFormat, aInfos[i]);
                        logger.info("        Source data line created");
                    }
                    catch (Exception ex)
                    {
                        logger.severe("        Can't get SourceDataLine : " + ex.getMessage());
                    }

                    try
                    {

                        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, mixer.getMixerInfo());
                        logger.info("        Target data line created");
                        // TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(aInfos[i].getName(), audioFormat, nInternalBufferSize);

                    }
                    catch (Exception ex)
                    {
                        logger.severe("        Can't get TargetDataLine : " + ex.getMessage());
                    }

                    // test code 
                    // TargetDataLine targetDataLine = audio.AudioCommon.getTargetDataLine(aInfos[i].getName(), audioFormat, nInternalBufferSize);
                    // logger.info("Source data line created");
                }

                // Scan all the source ports :
                logger.info("    Scan all target ports");
                /*                
                 // take all target ports
                 ArrayList<Line.Info> targetInfos = new ArrayList<Line.Info>(Arrays.asList(mixer.getTargetLineInfo()));
                 for (Line.Info targetInfo : targetInfos)
                 {
                 Port.Info pi = (Port.Info) targetInfo;

                 logger.info("Port found : " + mixer.getMixerInfo().getName() + "/" + pi.getName()+ ", type : " + pi.isSource());

                 int nBitsPerSample = 16;
                 boolean bBigEndian = true;
                 int nChannels = 2;
                 int nFrameSize = (nBitsPerSample / 8) * nChannels;
                 AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
                 float sampleRate = 48000;

                 AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);
                 int nInternalBufferSize = 8 * 8192;
                 try
                 {
                 // TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(aInfos[i].getName(), audioFormat, nInternalBufferSize);
                 // // SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getSourceDataLine(audioFormat, aInfos[i]);
                 // targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, mixerName);

                 logger.info("Source data line created");
                 }
                 catch (Exception ex)
                 {
                 logger.severe("Can't get SourceDataLine : " + ex.getMessage());
                 }


                 }
                 * 
                 * 
                 */
            }
        }
    }
}
