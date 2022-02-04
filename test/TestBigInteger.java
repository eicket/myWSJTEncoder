package test;

import dsp.FT;
import java.math.BigInteger;
import java.util.logging.Logger;

public class TestBigInteger
{

    static final Logger logger = Logger.getLogger(TestBigInteger.class.getName());

    public static void main(String[] args)
    {

        FT ft8 = new FT();
        BigInteger big = BigInteger.valueOf(1024);
        ft8.dumpBigInteger(big,4);

        for (int i = 0; i < 10000; i++)
        {
            logger.info("byte : " + big.testBit(i));
        }
    }
}
