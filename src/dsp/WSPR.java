// Erik Icket, ON4PB - 2022

package dsp;

import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_WSPR;
import static common.Constants.SAMPLE_RATE;
import java.util.Arrays;
import java.util.logging.Logger;
import static dsp.Utils.printArray;

public class WSPR
{
    // continuous phase 4-FSK, tone separation 1.4648 Hz, is done by taking one more / less sine per symbol
    // symbol duration : NR_OF_SAMPLES_PER_SYMBOL / 12000 = 0,68 sec with NR_OF_SAMPLES_PER_SYMBOL = 8192
    // number of sines per symbol = NR_OF_SAMPLES_PER_SYMBOL * fBase / sampleRate
    // 8192 samples * 1500 Hz / 12000 samples per sec = 1024 sinuses 
    // 162 symbols * 8192 / 12000 secs = 110,6 secs

    static final Logger logger = Logger.getLogger(WSPR.class.getName());

    public double[] makeWSPR(int fBase, String message, double gain)
    {
        // 162 bytes are encoded, 1 symbol per byte
        // each symbols contains a byte with the symbol, symbol values are 0,1,2,3 
        byte[] symbols = encode(message);

        // returns symbols.length * NR_OF_SAMPLES_PER_SYMBOL_WSPR
        double[] audio = MakeWaveform.synthesizeWithoutGFSK(symbols, fBase, NR_OF_SAMPLES_PER_SYMBOL_WSPR, SAMPLE_RATE, gain);
        return audio;
    }

    // returns 162 bytes, 1 symbol per byte
    private byte[] encode(String message)
    {
        if (message.length() > 14)
        {
            logger.severe("WSPR message is too long");
            return null;
        }

        String[] messageParts = message.split(" ");
        if (messageParts.length != 3)
        {
            logger.severe("WSPR message must have 3 parts");
            return null;
        }

        // test regex at https://www.freeformatter.com/java-regex-tester.html#ad-output
        if ((messageParts[0].length() >= 4) && (messageParts[0].length() <= 6) && (messageParts[1].matches(".*[0-9]+.*")))
        {
            // encode the callsign
            String callSign = messageParts[0];
            logger.fine("Callsign : " + callSign);

            callSign = callSign.toUpperCase();

            // Is the third char a number ?
            char c = callSign.charAt(2);
            if ((c >= '0') && (c <= '9'))
            {
                logger.fine("3rd char is a digit");
            }
            else
            {
                callSign = " " + callSign;
                logger.fine("New callsign : " + callSign);
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
                    return null;
            }

            logger.fine("Callsign ready for encoding : *" + callSign + "*");

            int n1 = encodeCallSignChar(callSign.charAt(0));
            int n2 = n1 * 36 + encodeCallSignChar(callSign.charAt(1));
            int n3 = n2 * 10 + encodeCallSignChar(callSign.charAt(2));
            int n4 = 27 * n3 + (encodeCallSignChar(callSign.charAt(3)) - 10);
            int n5 = 27 * n4 + (encodeCallSignChar(callSign.charAt(4)) - 10);
            int N = 27 * n5 + (encodeCallSignChar(callSign.charAt(5)) - 10);

            logger.fine("encoded callsign (N) : " + N);

            // encode the locator
            String locator = messageParts[1];
            logger.fine("Locator : " + locator);

            locator = locator.toUpperCase();

            int M1 = (179 - 10 * encodeLocatorChar(locator.charAt(0)) - encodeLocatorChar(locator.charAt(2)))
                    * 180 + 10 * encodeLocatorChar(locator.charAt(1)) + encodeLocatorChar(locator.charAt(3));

            logger.fine("encoded locator : " + M1);

            // parse the power
            String sPower = messageParts[2];
            logger.fine("Power : " + sPower);

            int iPower;
            try
            {
                iPower = Integer.parseInt(sPower);
            }
            catch (NumberFormatException e2)
            {
                logger.severe("Cannot parse power field");
                return null;
            }

            if ((iPower < 0) || (iPower > 60))
            {
                logger.severe("Invalid power");
                return null;
            }

            int M = M1 * 128 + iPower + 64;
            logger.fine("encoded locator plus power (M) : " + M);

            // Bit packing : N (28 bits callsign) + M (22 bits locator + power)
            byte[] nArray = convertIntToByteArray(28, N);             // [0] entry is LSB !
            // logger.info("N array : " + Arrays.toString(nArray)); // prints LSB first and is not very readable
            logger.fine("N array : " + printArray(nArray)); // print is MSB first

            byte[] mArray = convertIntToByteArray(22, M);             // [0] entry is LSB !
            logger.fine("M array : " + printArray(mArray));
            printArray(mArray);

            // M and N array have bin values of 0 and 1
            // C array has binary values of 0 and 1
            byte[] C = new byte[81];
            for (int i = 0; i < C.length; i++)
            {
                C[i] = 0;
            }

            //  -- 28 N callsign, MSB first --    -- 22 M locator + power, MSB first --    0 .. 0
            //  |                            |    |                                   |    |    |
            //  80                           53   52                                  31   30   0 --> index in C array
            for (int i = 0; i < nArray.length; i++)
            {
                C[53 + i] = (byte) (nArray[i]);
            }

            for (int i = 0; i < mArray.length; i++)
            {
                C[31 + i] = (byte) (mArray[i]);
            }
            // [0] entry is printed first and is LSB
            logger.fine("C array : " + printArray(C));

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

            return symbol;
        }
        else
        {
            logger.severe("Invalid callsign");
            return null;
        }

    }

    private byte encodeCallSignChar(char c)
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

        logger.severe("Invalid char in callsign");

        return 0;
    }

    private byte encodeLocatorChar(char c)
    {
        if ((c >= '0') && (c <= '9'))
        {
            return ((byte) (c - 48));
        }
        else if ((c >= 'A') && (c <= 'R'))
        {
            return ((byte) (c - 65));
        }

        logger.severe("Invalid char in locator");

        return 0;
    }

    public byte[] convertIntToByteArray(int arraySize, int b)
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
        reverseByteArray(binArray);

        return binArray;
    }

    public void reverseByteArray(byte[] array)
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

    private short computeParityMostEfficient(long no)
    {
        no ^= (no >>> 32);
        no ^= (no >>> 16);
        no ^= (no >>> 8);
        no ^= (no >>> 4);
        no ^= (no >>> 2);
        no ^= (no >>> 1);

        return (short) (no & 1);
    }

    private byte[] encodeTest()
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
}
