package audio;

import common.PropertiesWrapper;
import java.util.logging.Logger;
import javafx.scene.control.ChoiceBox;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Audio
{
    static final Logger logger = Logger.getLogger(audio.Audio.class.getName());

    int nBitsPerSample = 16;
    boolean bBigEndian = false;
    int nChannels = 1;
    int nFrameSize = (nBitsPerSample / 8) * nChannels;
    AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    
    public SourceDataLine sourceDataLine;
    public TargetDataLine targetDataLine;

    public void ListAudioIn(ChoiceBox audioInBox)
    {
        PropertiesWrapper propWrapper = new PropertiesWrapper();

        // find all audio mixers       
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            logger.fine("next mixer : " + mixer.getMixerInfo().toString());

            // test for a target == audio in
            if (mixer.isLineSupported(new Line.Info(TargetDataLine.class)))
            {
                logger.fine("audio in - target : " + mixer.getMixerInfo().getName());
                audioInBox.getItems().add(mixer.getMixerInfo().getName());

                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioIn")))
                {
                    logger.info("Found received AudioIn device in property file : " + mixer.getMixerInfo().getName());
                    // select it, there may be other ports after this one ... 
                    audioInBox.getSelectionModel().selectLast();
                }
            }
        }
    }

    public void OpenAudioIn(int sampleRate)
    {
        PropertiesWrapper propWrapper = new PropertiesWrapper();

        // find all audio mixers       
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        boolean foundAudioIn = false;

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            logger.fine("next mixer : " + mixer.getMixerInfo().toString());

            // test for a target == audio in
            if (mixer.isLineSupported(new Line.Info(TargetDataLine.class)))
            {
                logger.fine("audio in - target : " + mixer.getMixerInfo().getName());

                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioIn")))
                {
                    logger.info("Found received AudioIn device in property file : " + mixer.getMixerInfo().getName());

                    foundAudioIn = true;
                    AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);

                    try
                    {
                        // Obtains a target data line that can be used for recording audio data 
                        targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, mixer.getMixerInfo());
                        logger.fine("Target data line created");
                    }
                    catch (Exception ex)
                    {
                        logger.severe("Can't get TargetDataLine : " + ex.getMessage());
                    }

                    break;
                }
            }

        }
        if (!foundAudioIn)
        {
            logger.info("No Audio In found");
        }
    }

    public void ListAudioOut(ChoiceBox audioOutBox)
    {
        // listTargetDataLines();

        PropertiesWrapper propWrapper = new PropertiesWrapper();

        // find all audio mixers      
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            logger.fine("next mixer : " + mixer.getMixerInfo().toString());

            // test for a source == audio out
            if (mixer.isLineSupported(new Line.Info(SourceDataLine.class)))
            {
                logger.fine("audio out -  source : " + mixer.getMixerInfo().getName());

                audioOutBox.getItems().add(mixer.getMixerInfo().getName());

                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioOut")))
                {
                    logger.info("Found received AudioOut device in property file : " + mixer.getMixerInfo().getName());

                    // select it, there may be other ports after this one ... 
                    audioOutBox.getSelectionModel().selectLast();
                }
            }
        }
    }

    public void OpenAudioOut(int sampleRate)
    {
        PropertiesWrapper propWrapper = new PropertiesWrapper();

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
                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioOut")))
                {
                    logger.info("Found received AudioOut device in property file : " + mixer.getMixerInfo().getName());

                    foundAudioOut = true;
                    AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);

                    try
                    {
                        // Obtains a source data line that can be used for playing back audio data
                        sourceDataLine = (SourceDataLine) AudioSystem.getSourceDataLine(audioFormat, mixer.getMixerInfo());

                        sourceDataLine.open(audioFormat);
                        sourceDataLine.start();
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
        }
    }

    private static void listTargetDataLines()
    {
        logger.info("Available Mixers:");

        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            // mixer.open();
            Line.Info[] lines = mixer.getTargetLineInfo();
            logger.info(aInfos[i].getName());
            for (int j = 0; j < lines.length; j++)
            {
                logger.info("  " + lines[j].toString());
                if (lines[j] instanceof DataLine.Info)
                {
                    AudioFormat[] formats = ((DataLine.Info) lines[j]).getFormats();
                    for (int k = 0; k < formats.length; k++)
                    {
                        logger.info("    " + formats[k].toString());
                    }
                }
            }
        }
    }

}
