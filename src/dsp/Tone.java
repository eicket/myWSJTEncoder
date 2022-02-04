package dsp;

import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_WSPR;
import static common.Constants.SAMPLE_RATE;
import static java.lang.Math.round;
import java.util.logging.Logger;
import static dsp.Utils.makeAudioSymbols;

public class Tone
{

    static final Logger logger = Logger.getLogger(Tone.class.getName());

    public double[] makeAudio(int fSelected, double gain)
    {

        int nrOfSinusesPerSymbol = (int) round((double) NR_OF_SAMPLES_PER_SYMBOL_WSPR * fSelected / SAMPLE_RATE);
        logger.info("starting number of sinuses per symbol : " + nrOfSinusesPerSymbol);

        double[] symbol0 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_WSPR);

        // 1 symbol = 0,683 sec
        // 1 min => 88 symbols
        byte[] toneSymbols = new byte[500];

        for (int i = 0; i < 88; i++)
        {
            toneSymbols[i] = 0;
        }

        double[] dBufferOut = new double[toneSymbols.length * NR_OF_SAMPLES_PER_SYMBOL_WSPR];
        int iBufferOut = 0;
        for (int i = 0; i < toneSymbols.length; i++)
        {
            for (int j = 0; j < NR_OF_SAMPLES_PER_SYMBOL_WSPR; j++)
            {
                dBufferOut[iBufferOut + j] = symbol0[j];
            }
            iBufferOut = iBufferOut + NR_OF_SAMPLES_PER_SYMBOL_WSPR;
        }
        return dBufferOut;
    }
}
