package test;

import java.util.ArrayList;
import java.util.Arrays;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;



public class VSJQueryPort
{

    public static void main(String[] args) throws Exception
    {
        probePort();
    }

    public static void probePort() throws Exception
    {
        ArrayList<Mixer.Info> mixerInfos = new ArrayList<Mixer.Info>(Arrays.asList(AudioSystem.getMixerInfo()));
        Line.Info portInfo = new Line.Info(Port.class);
        for (Mixer.Info mixerInfo : mixerInfos)
        {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            if (mixer.isLineSupported(portInfo))
            {
                // found a Port Mixer
                System.out.println();
                System.out.println("Found mixer : " + mixerInfo.getName());
                System.out.println("Mixer desc  : " + mixerInfo.getDescription());
                System.out.println("Source Line Supported:");
                ArrayList<Line.Info> srcInfos = new ArrayList<Line.Info>(Arrays.asList(mixer.getSourceLineInfo()));
                for (Line.Info srcInfo : srcInfos)
                {
                    Port.Info pi = (Port.Info) srcInfo;
                    System.out.println(pi.getName() + ", " + (pi.isSource() ? "source" : "target"));
                    showControls(mixer.getLine(srcInfo));
                } // of for Line.Info
                System.out.println("Target Line Supported:");
                ArrayList<Line.Info> targetInfos = new ArrayList<Line.Info>(Arrays.asList(mixer.getTargetLineInfo()));
                for (Line.Info targetInfo : targetInfos)
                {
                    Port.Info pi = (Port.Info) targetInfo;
                    System.out.println(pi.getName() + " , " + (pi.isSource() ? "source" : "target"));
                    showControls(mixer.getLine(targetInfo));
                }
            } // of if
            // (mixer.isLineSupported)
        } // of for (Mixer.Info)
    }

    private static void showControls(
            Line inLine) throws Exception
    {
        // must open the line to get
        // at controls
        inLine.open();
        System.out.println("Available controls : ");
        ArrayList<Control> ctrls = new ArrayList<Control>(Arrays.asList(inLine.getControls()));
        for (Control ctrl : ctrls)
        {
            System.out.println(ctrl.toString());
            if (ctrl instanceof CompoundControl)
            {
                CompoundControl cc = ((CompoundControl) ctrl);
                ArrayList<Control> ictrls = new ArrayList<Control>(Arrays.asList(cc.getMemberControls()));
                for (Control ictrl : ictrls)
                {
                    System.out.println(ictrl.toString());
                } // of if (ctrl instanceof)
            } // of for(Control ctrl)
            inLine.close();
        }

    }
}