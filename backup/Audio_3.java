package audio;

import common.PropertiesWrapper;
import java.util.logging.Logger;
import javafx.scene.control.ChoiceBox;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Audio
{

    static final Logger logger = Logger.getLogger(audio.Audio.class.getName());
    PropertiesWrapper propWrapper = new PropertiesWrapper();

    public SourceDataLine sourceDataLine;
    public TargetDataLine targetDataLine;

    public void ListAudioIn(ChoiceBox audioInBox, int sampleRate)
    {
        audioInBox.getItems().clear();

        // find all audio mixers       
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        boolean foundAudioIn = false;

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            logger.info("next mixer : " + mixer.getMixerInfo().toString());

            // test for a target == audio in
            if (mixer.isLineSupported(new Line.Info(TargetDataLine.class)))
            {
                logger.info("audio in - target : " + mixer.getMixerInfo().getName());
                audioInBox.getItems().add(mixer.getMixerInfo().getName());

                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioIn")))
                {
                    logger.info("Found received AudioIn device in property file : " + mixer.getMixerInfo().getName());
                    // select it, there may be other ports after this one ... 
                    audioInBox.getSelectionModel().selectLast();
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
                        targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, mixer.getMixerInfo());
                        logger.fine("Target data line created");
                    }
                    catch (Exception ex)
                    {
                        logger.severe("Can't get TargetDataLine : " + ex.getMessage());
                    }
                }

            }

        }
        if (!foundAudioIn)
        {
            audioInBox.getSelectionModel().selectFirst();
            logger.info("No Audio In found");
        }
    }

    public void ListAudioOut(ChoiceBox audioOutBox, int sampleRate)
    {

        //   audioOutBox.getItems().clear();
        // find all audio mixers      
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        boolean foundAudioOut = false;

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            logger.info("next mixer : " + mixer.getMixerInfo().toString());

            // test for a source == audio out
            if (mixer.isLineSupported(new Line.Info(SourceDataLine.class)))
            {
                logger.info("audio out -  source : " + mixer.getMixerInfo().getName());
                audioOutBox.getItems().add(mixer.getMixerInfo().getName());

                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioOut")))
                {
                    logger.info("Found received AudioOut device in property file : " + mixer.getMixerInfo().getName());
                    // select it, there may be other ports after this one ... 
                    audioOutBox.getSelectionModel().selectLast();
                    foundAudioOut = true;

                    // test
                    int nBitsPerSample = 16;
                    boolean bBigEndian = true;
                    int nChannels = 1;
                    int nFrameSize = (nBitsPerSample / 8) * nChannels;
                    AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

                    // mine :          AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);
                    AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
                    int nInternalBufferSize = 8 * 8192;
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
                }
            }
        }

        if (!foundAudioOut)
        {
            audioOutBox.getSelectionModel().selectFirst();
            logger.info("No Audio Out found");
        }
    }
    
     public void OpenAudioOut(ChoiceBox audioOutBox, int sampleRate)
    {

        //   audioOutBox.getItems().clear();
        // find all audio mixers      
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        boolean foundAudioOut = false;

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            logger.info("next mixer : " + mixer.getMixerInfo().toString());

            // test for a source == audio out
            if (mixer.isLineSupported(new Line.Info(SourceDataLine.class)))
            {
                logger.info("audio out -  source : " + mixer.getMixerInfo().getName());
                audioOutBox.getItems().add(mixer.getMixerInfo().getName());

                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioOut")))
                {
                    logger.info("Found received AudioOut device in property file : " + mixer.getMixerInfo().getName());
                    // select it, there may be other ports after this one ... 
                    audioOutBox.getSelectionModel().selectLast();
                    foundAudioOut = true;

                    // test
                    int nBitsPerSample = 16;
                    boolean bBigEndian = true;
                    int nChannels = 1;
                    int nFrameSize = (nBitsPerSample / 8) * nChannels;
                    AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

                    // mine :          AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);
                    AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
                    int nInternalBufferSize = 8 * 8192;
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
                }
            }
        }

        if (!foundAudioOut)
        {
            audioOutBox.getSelectionModel().selectFirst();
            logger.info("No Audio Out found");
        }
    }

    /*
    public void List(ChoiceBox audioInBox, ChoiceBox audioOutBox, int sampleRate)
    {
        audioInBox.getItems().clear();
        audioOutBox.getItems().clear();

        // find all audio mixers
        // reset the properties if the loaded audio mixer does not exist
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

        // new - filter out only ports
        boolean foundAudioIn = false;
        boolean foundAudioOut = false;

        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            logger.info("next mixer : " + mixer.getMixerInfo().toString());

            // test for a target == audio in
            if (mixer.isLineSupported(new Line.Info(TargetDataLine.class)))
            {
                logger.info("audio in - target : " + mixer.getMixerInfo().getName());
                audioInBox.getItems().add(mixer.getMixerInfo().getName());

                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioIn")))
                {
                    logger.info("Found received AudioIn device in property file : " + mixer.getMixerInfo().getName());
                    // select it, there may be other ports after this one ... 
                    audioInBox.getSelectionModel().selectLast();
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
                        targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat, mixer.getMixerInfo());
                        logger.fine("Target data line created");
                    }
                    catch (Exception ex)
                    {
                        logger.severe("Can't get TargetDataLine : " + ex.getMessage());
                    }
                }

            }
            // test for a source == audio out
            else if (mixer.isLineSupported(new Line.Info(SourceDataLine.class)))
            {
                logger.info("audio out -  source : " + mixer.getMixerInfo().getName());
                audioOutBox.getItems().add(mixer.getMixerInfo().getName());

                if ((mixer.getMixerInfo().getName()).equalsIgnoreCase(propWrapper.getStringProperty("ReceivedAudioOut")))
                {
                    logger.info("Found received AudioOut device in property file : " + mixer.getMixerInfo().getName());
                    // select it, there may be other ports after this one ... 
                    audioOutBox.getSelectionModel().selectLast();
                    foundAudioOut = true;

                    // test
                    int nBitsPerSample = 16;
                    boolean bBigEndian = true;
                    int nChannels = 1;
                    int nFrameSize = (nBitsPerSample / 8) * nChannels;
                    AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

                    // mine :          AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, nBitsPerSample, nChannels, nFrameSize, sampleRate, bBigEndian);
                    AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
                    int nInternalBufferSize = 8 * 8192;
                    try
                    {
                        // Obtains a source data line that can be used for playing back audio data
                        sourceDataLine = (SourceDataLine) AudioSystem.getSourceDataLine(audioFormat, mixer.getMixerInfo());
                        sourceDataLine.open(audioFormat);
                        sourceDataLine.start();
                        logger.fine("Source data line created");
                    }
                    catch (Exception ex)
                    {
                        logger.severe("Can't get SourceDataLine : " + ex.getMessage());
                    }
                }
            }
        }
        if (!foundAudioIn)
        {
            audioInBox.getSelectionModel().selectFirst();
            logger.info("No Audio In found");
        }

        if (!foundAudioOut)
        {
            audioOutBox.getSelectionModel().selectFirst();
            logger.info("No Audio Out found");
        }
    }
     */
    public void play(double[] dBufferOut)
    {
        logger.info("Play is started");

        int sizeDataLineBuffer = sourceDataLine.getBufferSize();
        logger.fine("Size of data line buffer : " + sizeDataLineBuffer);

        byte audioOutByte[] = new byte[2 * dBufferOut.length];
        logger.fine("New audio out buffer, available for write : " + sourceDataLine.available() + ", internal buffer size : " + sourceDataLine.getBufferSize());

        audioOutByte = fromBufferToAudioBytes(audioOutByte, dBufferOut);

        for (int i = 0; i < audioOutByte.length;)
        {
            int nrOfBytesWritten = sourceDataLine.write(audioOutByte, i, sizeDataLineBuffer);
            i = i + nrOfBytesWritten;

            logger.fine("Number of bytes written : " + nrOfBytesWritten + ", index now at : " + i);

            if ((i + sizeDataLineBuffer + 1) > audioOutByte.length)
            {
                sizeDataLineBuffer = audioOutByte.length - i;
                logger.fine("rest : " + sizeDataLineBuffer);
            }
        }

        logger.info("Play is finished");
    }

    public byte[] fromBufferToAudioBytes(byte[] audioBytes, double[] buffer)
    {
        int n = buffer.length;
        // Java is always big endian, mixer is little endian
        for (int i = 0; i < n; i++)
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

}
