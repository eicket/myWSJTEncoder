package audio;

import common.PropertiesWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import javafx.scene.control.ChoiceBox;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Audio
{

    static final Logger logger = Logger.getLogger(audio.Audio.class.getName());
    PropertiesWrapper propWrapper = new PropertiesWrapper();

    public Audio()
    {

    }

    // see https://www.codota.com/code/java/methods/javax.sound.sampled.Mixer/getTargetLineInfo
    public void List(ChoiceBox audioInBox, ChoiceBox audioOutBox, int sampleRate)
    {
        // find all audio mixers
        // reset the properties if the loaded audio mixer does not exist
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();
        Line.Info portInfo = new Line.Info(Port.class);
        Mixer.Info rxAudioInMixer = null;
        Mixer.Info rxAudioOutMixer = null;

        // new - filter out only ports
        boolean foundAudioIn = false;
        boolean foundAudioOut = false;

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            logger.info("next mixer : " + mixer.getMixerInfo().getName());

            // only take the hardware port mixers
            if (mixer.isLineSupported(new Line.Info(TargetDataLine.class)))
            {
                logger.info("Is a audio in - target");

                // take all Audio In / target 
                ArrayList<Line.Info> targetInfos = new ArrayList<Line.Info>(Arrays.asList(mixer.getTargetLineInfo()));
                for (Line.Info targetInfo : targetInfos)
                {
                    Port.Info pi = (Port.Info) targetInfo;
                    logger.info("audio in - target : " + mixer.getMixerInfo().getName() + "/" + pi.getName());
                    audioOutBox.getItems().add(mixer.getMixerInfo().getName() + "/" + pi.getName());

                    if ((mixer.getMixerInfo().getName() + "/" + pi.getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioIn")))
                    {
                        logger.info("Found received AudioIn device in property file");
                        // select it, there may be other ports after this one ... 
                        audioOutBox.getSelectionModel().selectLast();
                        foundAudioIn = true;

                        // test
                        int nBitsPerSample = 16;
                        boolean bBigEndian = true;
                        int nChannels = 2;
                        int nFrameSize = (nBitsPerSample / 8) * nChannels;
                        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

                        AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);
                        int nInternalBufferSize = 8 * 8192;
                        try
                        {
                            // Obtains a target data line that can be used for recording audio data 
                            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, mixer.getMixerInfo());
                            //   TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, mixer.getMixerInfo());
                            // targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, mixerName);

                            logger.info("Target data line created");
                        }
                        catch (Exception ex)
                        {
                            logger.severe("Can't get TargetDataLine : " + ex.getMessage());
                        }

                        rxAudioInMixer = aInfos[i];
                    }
                }

            }
            else if (mixer.isLineSupported(new Line.Info(SourceDataLine.class)))
            {
                logger.info("Is a audio out - source");

                // take all Audio Out / source 
                ArrayList<Line.Info> sourceInfos = new ArrayList<Line.Info>(Arrays.asList(mixer.getSourceLineInfo()));
                for (Line.Info sourceInfo : sourceInfos)
                {
                    Port.Info pi = (Port.Info) sourceInfo;
                    logger.info("audio out -  source : " + mixer.getMixerInfo().getName() + "/" + pi.getName());
                    audioInBox.getItems().add(mixer.getMixerInfo().getName() + "/" + pi.getName());

                    if ((mixer.getMixerInfo().getName() + "/" + pi.getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioOut")))
                    {
                        logger.info("Found received AudioOut device in property file");
                        // select it, there may be other ports after this one ... 
                        audioInBox.getSelectionModel().selectLast();
                        foundAudioOut = true;

                        // test
                        int nBitsPerSample = 16;
                        boolean bBigEndian = true;
                        int nChannels = 2;
                        int nFrameSize = (nBitsPerSample / 8) * nChannels;
                        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

                        AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);
                        int nInternalBufferSize = 8 * 8192;
                        try
                        {
                            // Obtains a source data line that can be used for playing back audio data
                            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getSourceDataLine(audioFormat, mixer.getMixerInfo());
                            // targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, mixerName);
                            logger.info("Source data line created");

                        }
                        catch (Exception ex)
                        {
                            logger.severe("Can't get SourceDataLine : " + ex.getMessage());
                        }

                        rxAudioOutMixer = aInfos[i];
                    }
                }

            }
        }
        if (!foundAudioIn)
        {
            audioInBox.getSelectionModel().selectFirst();
            logger.info("No Audio In found");

            // logger.info("Defaulting to first AudioIn device : " + rxAudioInBox.getItems().get(0).toString());
            //propWrapper.setProperty("ReceivedAudioIn", rxAudioInBox.getValue().toString());
        }

        if (!foundAudioOut)
        {
            audioOutBox.getSelectionModel().selectFirst();
            logger.info("No Audio Out found");

            // logger.info("Defaulting to first AudioIn device : " + rxAudioInBox.getItems().get(0).toString());
            //propWrapper.setProperty("ReceivedAudioIn", rxAudioInBox.getValue().toString());
        }
    }
}
