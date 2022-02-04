package test;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

public class ListAudioInputLines
{

    public static void main(String argv[])
    {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        List<Line.Info> availableLines = new ArrayList<Line.Info>();
        for (Mixer.Info mixerInfo : mixers)
        {
            System.out.println("Found Mixer: " + mixerInfo);
            Mixer m = AudioSystem.getMixer(mixerInfo);
            Line.Info[] lines = m.getTargetLineInfo();
            for (Line.Info li : lines)
            {
                System.out.println("Found target line: " + li);
                try
                {
                    m.open();
                    availableLines.add(li);
                }
                catch (LineUnavailableException e)
                {
                    System.out.println("Line unavailable.");
                }
            }
        }
        System.out.println("Available lines: " + availableLines);
    }
}
