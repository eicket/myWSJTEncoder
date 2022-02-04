// Erik Icket, ON4PB - 2022

package dsp;

import static common.Constants.SAMPLE_RATE;
import static java.lang.Math.round;
import java.util.logging.Logger;

public class Utils
{

    static final Logger logger = Logger.getLogger(Utils.class.getName());

    public static double baseFreq(int fSelected, int nrOfSamplesPerSymbol)
    {
        long nrOfSinusesPerSymbol = round((double) nrOfSamplesPerSymbol * fSelected / SAMPLE_RATE);
        double dBaseFreq = (double) nrOfSinusesPerSymbol * SAMPLE_RATE / nrOfSamplesPerSymbol;
        logger.info("sinuses per symbol : " + nrOfSinusesPerSymbol + ", fBase : " + dBaseFreq);

        return dBaseFreq;
    }

    public static double[] makeAudioSymbols(int nrOfSinusesPerSymbol, double gain, int nrOfSamplesPerSymbol)
    {
        double[] symbol = new double[nrOfSamplesPerSymbol];
        double dBase = (double) nrOfSinusesPerSymbol * SAMPLE_RATE / nrOfSamplesPerSymbol;
        double dSinusesPerSymbol = nrOfSamplesPerSymbol * dBase / SAMPLE_RATE;
        double dSamplesPerSinus = nrOfSamplesPerSymbol / dSinusesPerSymbol;
        double dRadianIncrement = 2 * Math.PI / dSamplesPerSinus;
        double dRadian = 0;
        for (int i = 0; i < nrOfSamplesPerSymbol; i++)
        {
            symbol[i] = Math.sin(dRadian) * gain;
            dRadian = dRadian + dRadianIncrement;

            logger.fine("Symbol amplitude : " + symbol[i] + ", i : " + i);
        }
        logger.info("Frequency base : " + dBase + ", sinuses per symbol : " + nrOfSinusesPerSymbol + ", samples per sinus : " + dSamplesPerSinus + ", radian Increment : " + dRadianIncrement + ", gain : " + gain);

        return symbol;
    }

    public static String printArray(byte[] array)
    // MSB first, LSB last
    {
        if (array == null)
        {
            return "";
        }

        String s = "length : " + array.length + " : ";
        for (int i = array.length - 1; i >= 0; i--)
        {
            s = s.concat(Byte.toString(array[i]));
        }
        return (s);
    }

    public static void reverseByteArray(byte[] array)
    {
        if (array == null)
        {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i)
        {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }
}
