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

public class AudioOut
{

    static final Logger logger = Logger.getLogger(audio.AudioOut.class.getName());

    public static void ListAudioOut(ChoiceBox audioOutBox)
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
