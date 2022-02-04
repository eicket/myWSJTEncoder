package dsp;

import static java.lang.Math.round;
import java.util.Arrays;
import java.util.logging.Logger;

public class DSPUtils
{
    // continuous phase 4-FSK, tone separation 1.4648 Hz, is done by taking one more / less sinus per symbol

    // symbolSize * fBase / sampleRate = number of sinuses per symbol
    // 8192 samples * 1500 Hz / 12000 samples per sec = 1024 sinuses 
    // 8192 samples * 1500 Hz / 12000 samples per sec = 1024 sinuses 
    // 32768 samples * 1500 Hz / 48000 samples per sec = 1024 sinuses
    static final Logger logger = Logger.getLogger(DSPUtils.class.getName());

    double[] symbol0;
    double[] symbol1;
    double[] symbol2;
    double[] symbol3;

    public DSPUtils()
    {

    }

    public double baseFreq(int fSelected, int symbolSize, int sampleRate)
    {
        double dNrPeriods = (double) symbolSize * fSelected / sampleRate;
        long roundedPeriods = round(dNrPeriods);
        double dBase = (double) roundedPeriods * sampleRate / symbolSize;
        logger.info("nrPeriods : " + dNrPeriods + ", roundedPeriods : " + roundedPeriods + ", fBase : " + dBase);

        return dBase;
    }

    public double[] makeBufferOut(byte[] b, int nrOfSamplesPerSymbol)
    {
        double[] dBufferOut = new double[b.length * nrOfSamplesPerSymbol];
        int iBufferOut = 0;
        for (int i = 0; i < b.length; i++)
        {
            for (int j = 0; j < nrOfSamplesPerSymbol; j++)
            {
                switch (b[i])
                {
                    case 0:
                        dBufferOut[iBufferOut + j] = symbol0[j];
                        break;
                    case 1:
                        dBufferOut[iBufferOut + j] = symbol1[j];
                        break;
                    case 2:
                        dBufferOut[iBufferOut + j] = symbol2[j];
                        break;
                    case 3:
                        dBufferOut[iBufferOut + j] = symbol3[j];
                        break;
                }
            }
            iBufferOut = iBufferOut + nrOfSamplesPerSymbol;
        }
        return dBufferOut;
    }

    public void makeSymbols(int fSelected, int symbolSize, int sampleRate, double gain)
    {
        double dNrPeriods = (double) symbolSize * fSelected / sampleRate;
        logger.fine("nrPeriods : " + dNrPeriods);
        long roundedPeriods = round(dNrPeriods);

        // frequency 0        
        double dBase0 = (double) roundedPeriods * sampleRate / symbolSize;
        double dSinusesPerSymbol0 = symbolSize * dBase0 / sampleRate;
        double dSamplesPerSinus0 = symbolSize / dSinusesPerSymbol0;
        double dRadianIncrement0 = 2 * Math.PI / dSamplesPerSinus0;
        symbol0 = new double[symbolSize];
        double dRadian = 0;
        for (int i = 0; i < symbolSize; i++)
        {
            symbol0[i] = Math.sin(dRadian) * gain;
            dRadian = dRadian + dRadianIncrement0;
        }
        logger.info("Frequency 0 : " + "roundedPeriods : " + roundedPeriods + ", fBase : " + dBase0 + ", nr of sinuses per symbol : " + dSinusesPerSymbol0
                + ", nr of samples per sinus : " + dSamplesPerSinus0 + ", radianIncrement : " + dRadianIncrement0);

        // frequency 1        
        roundedPeriods++;
        double dBase1 = (double) (roundedPeriods) * sampleRate / symbolSize;
        double dSinusesPerSymbol1 = symbolSize * dBase1 / sampleRate;
        double dSamplesPerSinus1 = symbolSize / dSinusesPerSymbol1;
        double dRadianIncrement1 = 2 * Math.PI / dSamplesPerSinus1;
        symbol1 = new double[symbolSize];
        dRadian = 0;
        for (int i = 0; i < symbolSize; i++)
        {
            symbol1[i] = Math.sin(dRadian) * gain;
            dRadian = dRadian + dRadianIncrement1;
        }
        logger.info("Frequency 1 : " + "roundedPeriods : " + roundedPeriods + ", fBase : " + dBase1 + ", nr of sinuses per symbol : " + dSinusesPerSymbol1
                + ", nr of samples per sinus : " + dSamplesPerSinus1 + ", radianIncrement : " + dRadianIncrement1);

        // frequency 2
        roundedPeriods++;
        double dBase2 = (double) (roundedPeriods) * sampleRate / symbolSize;
        double dSinusesPerSymbol2 = symbolSize * dBase2 / sampleRate;
        double dSamplesPerSinus2 = symbolSize / dSinusesPerSymbol2;
        double dRadianIncrement2 = 2 * Math.PI / dSamplesPerSinus2;
        symbol2 = new double[symbolSize];
        dRadian = 0;
        for (int i = 0; i < symbolSize; i++)
        {
            symbol2[i] = Math.sin(dRadian) * gain;
            dRadian = dRadian + dRadianIncrement2;
        }
        logger.info("Frequency 2 : " + "roundedPeriods : " + roundedPeriods + ", fBase : " + dBase2 + ", nr of sinuses per symbol : " + dSinusesPerSymbol2
                + ", nr of samples per sinus : " + dSamplesPerSinus2 + ", radianIncrement : " + dRadianIncrement2);

        // frequency 3
        roundedPeriods++;
        double dBase3 = (double) (roundedPeriods) * sampleRate / symbolSize;
        double dSinusesPerSymbol3 = symbolSize * dBase3 / sampleRate;
        double dSamplesPerSinus3 = symbolSize / dSinusesPerSymbol3;
        double dRadianIncrement3 = 2 * Math.PI / dSamplesPerSinus3;
        symbol3 = new double[symbolSize];
        dRadian = 0;
        for (int i = 0; i < symbolSize; i++)
        {
            symbol3[i] = Math.sin(dRadian) * gain;
            dRadian = dRadian + dRadianIncrement3;
        }
        logger.info("Frequency 3 : " + "roundedPeriods : " + roundedPeriods + ", fBase : " + dBase3 + ", nr of sinuses per symbol : " + dSinusesPerSymbol3
                + ", nr of samples per sinus : " + dSamplesPerSinus3 + ", radianIncrement : " + dRadianIncrement3);
    }

    public byte[] test()
    {
        // K1ABC FN42 37 
        byte[] b =
        {
            3, 3, 0, 0, 2, 0, 0, 0, 1, 0, 2, 0, 1, 3, 1, 2, 2, 2, 1, 0, 0, 3, 2, 3, 1, 3, 3, 2, 2, 0,
            2, 0, 0, 0, 3, 2, 0, 1, 2, 3, 2, 2, 0, 0, 2, 2, 3, 2, 1, 1, 0, 2, 3, 3, 2, 1, 0, 2, 2, 1,
            3, 2, 1, 2, 2, 2, 0, 3, 3, 0, 3, 0, 3, 0, 1, 2, 1, 0, 2, 1, 2, 0, 3, 2, 1, 3, 2, 0, 0, 3,
            3, 2, 3, 0, 3, 2, 2, 0, 3, 0, 2, 0, 2, 0, 1, 0, 2, 3, 0, 2, 1, 1, 1, 2, 3, 3, 0, 2, 3, 1,
            2, 1, 2, 2, 2, 1, 3, 3, 2, 0, 0, 0, 0, 1, 0, 3, 2, 0, 1, 3, 2, 2, 2, 2, 2, 0, 2, 3, 3, 2,
            3, 2, 3, 3, 2, 0, 0, 3, 1, 2, 2, 2
        };
        return b;
    }

    public byte[] f0(int nrOfSymbols)
    {
        byte[] b = new byte[nrOfSymbols];
        for (int i = 0; i < nrOfSymbols; i++)
        {
            b[i] = 0;
        }

        return b;
    }

    public boolean encodeWspr(String message)
    {
        String[] messageParts = message.split(" ");
        if (messageParts.length != 3)
        {
            logger.info("Message must have 3 parts");
            return false;
        }

        // test regex at https://www.freeformatter.com/java-regex-tester.html#ad-output
        if ((messageParts[0].length() >= 4) && (messageParts[0].length() <= 6) && (messageParts[1].matches(".*[0-9]+.*")))
        {
            // encode the callsign
            String callSign = messageParts[0];
            logger.info("Callsign : " + callSign);

            callSign = callSign.toUpperCase();

            // Is the third char a number ?
            char c = callSign.charAt(2);
            if ((c >= '0') && (c <= '9'))
            {
                logger.info("3rd char is a digit");
            }
            else
            {
                callSign = " " + callSign;
                logger.info("New callsign : " + callSign);
            }

            switch (callSign.length())
            {
                case 4:
                    callSign = callSign + "  ";
                    break;
                case 5:
                    callSign = callSign + " ";
                    break;
                case 6:
                    break;
                default:
                    logger.severe("Invalid callsign length");
                    return false;
            }

            logger.info("Callsign ready for encoding : *" + callSign + "*");

            int n1 = encodeSingleCallSignChar(callSign.charAt(0));
            int n2 = n1 * 36 + encodeSingleCallSignChar(callSign.charAt(1));
            int n3 = n2 * 10 + encodeSingleCallSignChar(callSign.charAt(2));
            int n4 = 27 * n3 + (encodeSingleCallSignChar(callSign.charAt(3)) - 10);
            int n5 = 27 * n4 + (encodeSingleCallSignChar(callSign.charAt(4)) - 10);
            int N = 27 * n5 + (encodeSingleCallSignChar(callSign.charAt(5)) - 10);

            logger.info("encoded callsign (N) : " + N);

            // encode the locator
            String locator = messageParts[1];
            logger.info("Locator : " + locator);

            locator = locator.toUpperCase();

            int M1 = (179 - 10 * encodeSingleLocatorChar(locator.charAt(0)) - encodeSingleLocatorChar(locator.charAt(2)))
                    * 180 + 10 * encodeSingleLocatorChar(locator.charAt(1)) + encodeSingleLocatorChar(locator.charAt(3));

            logger.info("encoded locator : " + M1);

            // parse the power
            String sPower = messageParts[2];
            logger.info("Power : " + sPower);

            int iPower;
            try
            {
                iPower = Integer.parseInt(sPower);
            }
            catch (NumberFormatException e2)
            {
                logger.severe("Cannot parse power field");
                return false;
            }

            if ((iPower < 0) || (iPower > 60))
            {
                logger.severe("Invalid power");
                return false;
            }

            int M = M1 * 128 + iPower + 64;
            logger.info("encoded locator plus power (M) : " + M);

            // Bit packing : N (28 bits callsign) + M (22 bits locator + power)
            char[] nArray = Integer.toBinaryString(N).toCharArray();
            // [0] entry is printed first and is MSB !
            logger.info("N array : " + Arrays.toString(nArray));
            if (nArray.length != 28)
            {
                logger.severe("Incorrect N array length");
                return false;
            }

            byte[] test = convertToBinary(28, M);
            logger.info("T array : " + Arrays.toString(test));

            char[] mTempArray = Integer.toBinaryString(M).toCharArray();
            logger.info("M temp array : " + Arrays.toString(mTempArray));

            // make the variable length (15 - 22) mTempArray a fixed 22 bit mArray
            char[] mArray = new char[22];
            for (int i = 0; i < mArray.length; i++)
            {
                mArray[i] = '0';
            }
            for (int i = 0; i < mTempArray.length; i++)
            {
                mArray[i] = mTempArray[i];
            }
            logger.info("M      array : " + Arrays.toString(mArray));

            // M and N array have char values of '0' and '1'
            // C array has binary values of 0 and 1
            byte[] C = new byte[81];
            for (int i = 0; i < C.length; i++)
            {
                C[i] = 0;
            }

            //  -- 28 N callsign, MSB first --    -- 22 locator + power, MSB first --    0 .. 0
            //  |                            |    |                                 |    |    |
            //  80                           53   52                                31   30   0 --> index in C array
            for (int i = 0; i < nArray.length; i++)
            {
                //  C[53 + i] = (byte) (nArray[i] - 0x30);
                C[80 - i] = (byte) (nArray[i] - 0x30);
            }

            for (int i = 0; i < mArray.length; i++)
            {
                C[31 + i] = (byte) (mArray[i] - 0x30);
            }
            // [0] entry is printed first
            logger.info("C array : " + Arrays.toString(C));

            // Parity of a number is 1 if the total number of set bits in the binary representation of the number is odd else parity is 0.
            // Convolutional encoding
            int[] s = new int[162];
            int sIndex = 0;
            for (sIndex = 0; sIndex < s.length; sIndex++)
            {
                s[sIndex] = 0;
            }

            int Reg0 = 0;
            int Reg1 = 0;
            sIndex = 0;
            for (int i = C.length - 1; i >= 0; i--)
            {
                Reg0 = Reg0 << 1;
                Reg0 = Reg0 | (int) C[i];
                int j0 = Reg0 & 0xF2D05351;
                short p0 = computeParityMostEfficient(j0);
                s[sIndex] = p0;
                sIndex++;

                Reg1 = Reg1 << 1;
                Reg1 = Reg1 | (int) C[i];
                j0 = Reg1 & 0xE4613C47;
                p0 = computeParityMostEfficient(j0);
                s[sIndex] = p0;
                sIndex++;
            }

            // interleave
            int[] d = new int[162];
            sIndex = 0;
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

            // merge with sync vector
            byte[] sync =
            {
                1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0,
                0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0,
                1, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 1,
                1, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0
            };

            byte[] symbol = new byte[162];
            for (int i = 0; i < 162; i++)
            {
                symbol[i] = (byte) (sync[i] + 2 * d[i]);
            }

            logger.info("symbols : " + Arrays.toString(symbol));

            return true;

        }
        else
        {
            logger.severe("Invalid callsign");
            return false;
        }

    }

    byte encodeSingleCallSignChar(char c)
    {
        if ((c >= '0') && (c <= '9'))
        {
            return ((byte) (c - 48));
        }
        else if ((c >= 'A') && (c <= 'Z'))
        {
            return ((byte) (c - 55));
        }
        else if (c == ' ')
        {
            return 36;
        }
        logger.info("Invalid char in callsign");
        return 0;
    }

    byte encodeSingleLocatorChar(char c)
    {
        if ((c >= '0') && (c <= '9'))
        {
            return ((byte) (c - 48));
        }
        else if ((c >= 'A') && (c <= 'R'))
        {
            return ((byte) (c - 65));
        }

        logger.info("Invalid char in locator");
        return 0;
    }

    short computeParityMostEfficient(long no)
    {
        no ^= (no >>> 32);
        no ^= (no >>> 16);
        no ^= (no >>> 8);
        no ^= (no >>> 4);
        no ^= (no >>> 2);
        no ^= (no >>> 1);

        return (short) (no & 1);
    }

    public byte[] convertToBinary(int arraySize, int b)
    {
        byte[] binArray = new byte[arraySize];
        for (int i = arraySize - 1; i >= 0; i--)
        {
            if (b % 2 == 1)
            {
                binArray[i] = 1;
            }
            else
            {
                binArray[i] = 0;
            }
            b /= 2;
        }

        // make it LSB in [0] entry
        reverse(binArray);
        
        return binArray;
    }

    public void reverse(byte[] array)
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
