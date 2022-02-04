package test;

import dsp.WSPR;
import java.util.Arrays;
import java.util.logging.Logger;

public class IntToBinary
{

    static final Logger logger = Logger.getLogger(IntToBinary.class.getName());

    public static void main(String[] args)
    {
        WSPR dspUtils = new WSPR();

        byte[] result = dspUtils.convertIntToByteArray(28, 259047992);
        
        // 259047992 is [1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0] 
        // [0] entry is printed first and is MSB !
        
        
        //              [0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1]
        // result after byte reversal, [0] entry is now LSB
        
        logger.info("M      array : " + Arrays.toString(result));

    }
}
