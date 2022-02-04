package test;



import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioCommon
{
    static Logger logger = Logger.getLogger(AudioCommon.class.getName());

    public static void listSupportedTargetTypes()
    {
        String strMessage = "Supported target types:";
        AudioFileFormat.Type[] aTypes = AudioSystem.getAudioFileTypes();
        for (int i = 0; i < aTypes.length; i++)
        {
            strMessage += " " + aTypes[i].getExtension();
        }
        logger.info(strMessage);
    }

    /* Trying to get an audio file type for the passed extension.
    This works by examining all available file types. For each
    type, if the extension this type promisses to handle matches
    the extension we are trying to find a type for, this type is
    returned. If no appropriate type is found, null is returned.
     */
    public static AudioFileFormat.Type findTargetType(String strExtension)
    {
        AudioFileFormat.Type[] aTypes = AudioSystem.getAudioFileTypes();
        for (int i = 0; i < aTypes.length; i++)
        {
            if (aTypes[i].getExtension().equals(strExtension))
            {
                return aTypes[i];
            }
        }
        return null;
    }

    public static void listMixersAndExit()
    {
        logger.info("Available Mixers :");
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();
        for (int i = 0; i < aInfos.length; i++)
        {
            logger.info(aInfos[i].getName());
        }
        if (aInfos.length == 0)
        {
            logger.info("No mixers available");
        }
        System.exit(0);
    }

    // List Mixers. Only Mixers that support either TargetDataLines or SourceDataLines are listed, depending on the value of bPlayback.
    public static void listMixersAndExit(boolean bPlayback)
    {
        logger.info("Available Mixers :");
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();
        for (int i = 0; i < aInfos.length; i++)
        {
            Mixer mixer = AudioSystem.getMixer(aInfos[i]);
            Line.Info lineInfo = new Line.Info(bPlayback ? SourceDataLine.class : TargetDataLine.class);
            if (mixer.isLineSupported(lineInfo))
            {
                logger.info(aInfos[i].getName());
            }
        }
        if (aInfos.length == 0)
        {
            logger.info("No mixers available");
        }
        System.exit(0);
    }

    // This method tries to return a Mixer.Info whose name matches the passed name. If no matching Mixer.Info is found, null is returned.
    public static Mixer.Info getMixerInfo(String strMixerName)
    {
        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();
        for (int i = 0; i < aInfos.length; i++)
        {
            if (aInfos[i].getName().equals(strMixerName))
            {
                return aInfos[i];
            }
        }
        return null;
    }

    public static TargetDataLine getTargetDataLine(String strMixerName, AudioFormat audioFormat, int nBufferSize)
    {
        /*
        Asking for a line is a rather tricky thing. We have to construct an Info object that specifies the desired properties for the line.
        First, we have to say which kind of line we want. The possibilities are: SourceDataLine (for playback), 
        Clip (for repeated playback) and TargetDataLine (for recording).
        Here, we want to do normal capture, so we ask for a TargetDataLine.
        Then, we have to pass an AudioFormat object, so that the Line knows which format the data passed to it will have.
        Furthermore, we can give Java Sound a hint about how big the internal buffer for the line should be. This
        isn't used here, signaling that we don't care about the exact size. Java Sound will use some default value for the buffer size.
         */
        TargetDataLine targetDataLine = null;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat, nBufferSize);
        try
        {
            if (strMixerName != null)
            {
                Mixer.Info mixerInfo = getMixerInfo(strMixerName);
                if (mixerInfo == null)
                {
                    logger.info("AudioCommon.getTargetDataLine(): mixer not found: " + strMixerName);
                    return null;
                }
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                targetDataLine = (TargetDataLine) mixer.getLine(info);
            }
            else
            {
                logger.info("AudioCommon.getTargetDataLine(): using default mixer");
                targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            }

            // The line is there, but it is not yet ready to receive audio data. We have to open the line.
            logger.info("AudioCommon.getTargetDataLine(): opening line...");
            targetDataLine.open(audioFormat, nBufferSize);
            logger.info("AudioCommon.getTargetDataLine(): opened line");
        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        logger.info("AudioCommon.getTargetDataLine(): returning line: " + targetDataLine);
        return targetDataLine;
    }

    public static SourceDataLine getSourceDataLine(String strMixerName, AudioFormat audioFormat, int nBufferSize)
    {
        /*
        Asking for a line is a rather tricky thing. We have to construct an Info object that specifies the desired properties for the line.
        First, we have to say which kind of line we want. The possibilities are: SourceDataLine (for playback), 
        Clip (for repeated playback) and TargetDataLine (for recording).
        Here, we want to do normal capture, so we ask for a TargetDataLine.
        Then, we have to pass an AudioFormat object, so that the Line knows which format the data passed to it will have.
        Furthermore, we can give Java Sound a hint about how big the internal buffer for the line should be. This
        isn't used here, signaling that we don't care about the exact size. Java Sound will use some default value for the buffer size.
         */
        SourceDataLine sourceDataLine = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, nBufferSize);
        try
        {
            if (strMixerName != null)
            {
                Mixer.Info mixerInfo = getMixerInfo(strMixerName);
                if (mixerInfo == null)
                {
                    logger.info("AudioCommon.getSourceDataLine(): mixer not found: " + strMixerName);
                    return null;
                }
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                sourceDataLine = (SourceDataLine) mixer.getLine(info);
            }
            else
            {
                logger.info("AudioCommon.getSourceDataLine(): using default mixer");
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            }

            // The line is there, but it is not yet ready to receive audio data. We have to open the line.
            logger.info("AudioCommon.getSourceDataLine(): opening line...");
            sourceDataLine.open(audioFormat, nBufferSize);
            logger.info("AudioCommon.getSourceDataLine(): opened line");
        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        logger.info("AudioCommon.getSourceDataLine(): returning line: " + sourceDataLine);
        return sourceDataLine;
    }

    
    // Checks if the encoding is PCM.
    public static boolean isPcm(AudioFormat.Encoding encoding)
    {
        return encoding.equals(AudioFormat.Encoding.PCM_SIGNED) || encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED);
    }
}