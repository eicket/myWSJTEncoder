package gaussian;

import static common.Constants.FT8_SYMBOL_BT;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8;
import static common.Constants.SAMPLE_RATE;
import java.util.logging.Logger;
import static test.displayFrequencyDeviation.MainController.calculateFrequencyDeviationWithGFSK;

public class TestFDeviation
{

    static final Logger logger = Logger.getLogger(gaussian.TestFDeviation.class.getName());

    public static void main(String argv[])
    {
        byte[] symbols =
        {
            0, 1, 2, 3, 4, 5, 4, 3, 2, 1, 0
        };
        //    int nrOutputSamples = symbols.length * NR_OF_SAMPLES_PER_SYMBOL_FT8;      

        // returns nrOutputSamples - 1
        double[] fDeviation = calculateFrequencyDeviationWithGFSK(symbols, FT8_SYMBOL_BT, NR_OF_SAMPLES_PER_SYMBOL_FT8, SAMPLE_RATE);

    }

   
}
