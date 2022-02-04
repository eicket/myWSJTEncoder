package test;

import dsp.WSPR;
import java.util.Arrays;
import java.util.logging.Logger;

public class Interleave
{

    static final Logger logger = Logger.getLogger(Interleave.class.getName());

    public static void main(String[] args)
    {

        WSPR dspUtils = new WSPR();

        // interleave
        int[] s = new int[162];
        for (int i = 0; i < s.length; i++)
        {
            s[i] = i;
        }
        logger.info("s array : " + Arrays.toString(s)); // prints LSB first 

        int[] d = new int[162];
        int sIndex = 0;
        for (int i = 0; i < 256; i++)
        {
            // bit reverse i
            int dIndex = Integer.reverse(i << 24) & 0xff;
            if (dIndex < 162)
            {
                d[dIndex] = s[sIndex];
                sIndex++;
            }
        }

        logger.info("d array : " + Arrays.toString(d));
    }
}
