// Erik Icket, ON4PB - 2022
package dsp;

import static common.Constants.FT8_SYMBOL_BT;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8;
import static common.Constants.SAMPLE_RATE;
import java.util.logging.Logger;

public class Tone
{

    static final Logger logger = Logger.getLogger(Tone.class.getName());

    public double[] makeTone(int fBase, double gain)
    {

        // 1 symbol = 0,683 sec
        // 1 min => 88 symbols
        byte[] toneSymbols = new byte[88];

        for (int i = 0; i < toneSymbols.length; i++)
        {
            toneSymbols[i] = 0;
        }

        // returns toneSymbols.length * NR_OF_SAMPLES_PER_SYMBOL_FT8
        double[] audio = MakeWaveform.synthesizeWithGFSK(toneSymbols, fBase, FT8_SYMBOL_BT, NR_OF_SAMPLES_PER_SYMBOL_FT8, SAMPLE_RATE, gain);
        return audio;
    }
}
