package dsp;

import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL;
import static common.Constants.SAMPLE_RATE;
import static java.lang.Math.round;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.logging.Logger;

public class FT8
{
    // continuous phase 4-FSK, tone separation 1.4648 Hz, is done by taking one more / less sinus per symbol

    // symbolSize * fBase / sampleRate = number of sinuses per symbol
    // 8192 samples * 1500 Hz / 12000 samples per sec = 1024 sinuses 
    // 8192 samples * 1500 Hz / 12000 samples per sec = 1024 sinuses 
    // 32768 samples * 1500 Hz / 48000 samples per sec = 1024 sinuses
    static final Logger logger = Logger.getLogger(FT8.class.getName());

    public double[] symbol0;
    public double[] symbol1;
    public double[] symbol2;
    public double[] symbol3;

    public FT8()
    {

    }

    public double baseFreq(int fSelected)
    {
        long nrOfSinusesPerSymbol = round((double) NR_OF_SAMPLES_PER_SYMBOL * fSelected / SAMPLE_RATE);
        double dBase = (double) nrOfSinusesPerSymbol * SAMPLE_RATE / NR_OF_SAMPLES_PER_SYMBOL;
        logger.info("sinuses per symbol : " + nrOfSinusesPerSymbol + ", fBase : " + dBase);

        return dBase;
    }

    public void makeSymbols(int fSelected, double gain)
    {
        long nrOfSinusesPerSymbol = round((double) NR_OF_SAMPLES_PER_SYMBOL * fSelected / SAMPLE_RATE);
        logger.info("starting number of sinuses per symbol : " + nrOfSinusesPerSymbol);

        // frequency 0        
        double dBase0 = (double) nrOfSinusesPerSymbol * SAMPLE_RATE / NR_OF_SAMPLES_PER_SYMBOL;
        double dSinusesPerSymbol0 = NR_OF_SAMPLES_PER_SYMBOL * dBase0 / SAMPLE_RATE;
        double dSamplesPerSinus0 = NR_OF_SAMPLES_PER_SYMBOL / dSinusesPerSymbol0;
        double dRadianIncrement0 = 2 * Math.PI / dSamplesPerSinus0;
        symbol0 = new double[NR_OF_SAMPLES_PER_SYMBOL];
        double dRadian = 0;
        for (int i = 0; i < NR_OF_SAMPLES_PER_SYMBOL; i++)
        {
            symbol0[i] = Math.sin(dRadian) * gain;
            dRadian = dRadian + dRadianIncrement0;
        }
        logger.info("Frequency 0 : fBase : " + dBase0 + ", sinuses per symbol : " + nrOfSinusesPerSymbol + ", samples per sinus : " + dSamplesPerSinus0 + ", radian Increment : " + dRadianIncrement0);

        // frequency 1        
        nrOfSinusesPerSymbol++;
        double dBase1 = (double) (nrOfSinusesPerSymbol) * SAMPLE_RATE / NR_OF_SAMPLES_PER_SYMBOL;
        double dSinusesPerSymbol1 = NR_OF_SAMPLES_PER_SYMBOL * dBase1 / SAMPLE_RATE;
        double dSamplesPerSinus1 = NR_OF_SAMPLES_PER_SYMBOL / dSinusesPerSymbol1;
        double dRadianIncrement1 = 2 * Math.PI / dSamplesPerSinus1;
        symbol1 = new double[NR_OF_SAMPLES_PER_SYMBOL];
        dRadian = 0;
        for (int i = 0; i < NR_OF_SAMPLES_PER_SYMBOL; i++)
        {
            symbol1[i] = Math.sin(dRadian) * gain;
            dRadian = dRadian + dRadianIncrement1;
        }
        logger.info("Frequency 1 : fBase : " + dBase1 + ", sinuses per symbol : " + nrOfSinusesPerSymbol + ", samples per sinus : " + dSamplesPerSinus1 + ", radian Increment : " + dRadianIncrement1);

        // frequency 2
        nrOfSinusesPerSymbol++;
        double dBase2 = (double) (nrOfSinusesPerSymbol) * SAMPLE_RATE / NR_OF_SAMPLES_PER_SYMBOL;
        double dSinusesPerSymbol2 = NR_OF_SAMPLES_PER_SYMBOL * dBase2 / SAMPLE_RATE;
        double dSamplesPerSinus2 = NR_OF_SAMPLES_PER_SYMBOL / dSinusesPerSymbol2;
        double dRadianIncrement2 = 2 * Math.PI / dSamplesPerSinus2;
        symbol2 = new double[NR_OF_SAMPLES_PER_SYMBOL];
        dRadian = 0;
        for (int i = 0; i < NR_OF_SAMPLES_PER_SYMBOL; i++)
        {
            symbol2[i] = Math.sin(dRadian) * gain;
            dRadian = dRadian + dRadianIncrement2;
        }
        logger.info("Frequency 2 : fBase : " + dBase2 + ", sinuses per symbol : " + nrOfSinusesPerSymbol + ", samples per sinus : " + dSamplesPerSinus2 + ", radian Increment : " + dRadianIncrement2);

        // frequency 3
        nrOfSinusesPerSymbol++;
        double dBase3 = (double) (nrOfSinusesPerSymbol) * SAMPLE_RATE / NR_OF_SAMPLES_PER_SYMBOL;
        double dSinusesPerSymbol3 = NR_OF_SAMPLES_PER_SYMBOL * dBase3 / SAMPLE_RATE;
        double dSamplesPerSinus3 = NR_OF_SAMPLES_PER_SYMBOL / dSinusesPerSymbol3;
        double dRadianIncrement3 = 2 * Math.PI / dSamplesPerSinus3;
        symbol3 = new double[NR_OF_SAMPLES_PER_SYMBOL];
        dRadian = 0;
        for (int i = 0; i < NR_OF_SAMPLES_PER_SYMBOL; i++)
        {
            symbol3[i] = Math.sin(dRadian) * gain;
            dRadian = dRadian + dRadianIncrement3;
        }
        logger.info("Frequency 3 : fBase : " + dBase3 + ", sinuses per symbol : " + nrOfSinusesPerSymbol + ", samples per sinus : " + dSamplesPerSinus3 + ", radian Increment : " + dRadianIncrement3);
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

    public byte[] f0(int nrOfSymbols)
    {
        byte[] b = new byte[nrOfSymbols];
        for (int i = 0; i < nrOfSymbols; i++)
        {
            b[i] = 0;
        }

        return b;
    }

    /*
    C:\WSJT\wsjtx\bin>ft8code "FREE FRE FREE"
        Message                               Decoded                             Err i3.n3
    ----------------------------------------------------------------------------------------------------
     1. FREE FRE FREE                         FREE FRE FREE                            0.0 Free text

    Source-encoded message, 77 bits:
    00110110011110001111010110111111101000000001001010100001110110001010101 000000

    14-bit CRC:
    10110011001000

    83 Parity bits:
    00100011101100010110001110100011000001010110110100101111110110000011010000100111110

    Channel symbols (79 tones):
      Sync               Data               Sync               Data               Sync
    3140652 16575246677300336026536301215 3140652 50526534145201344567440230574 3140652
     */
    public byte[] encode(String message)
    {
        String FT8MessageFixedAt13;

        if (message.length() < 13)
        {
            // pad until we have 13 chars
            FT8MessageFixedAt13 = padSpaces(message, 13);
        }
        else if (message.length() > 13)
        {
            logger.severe("FT8 message is too long");
            FT8MessageFixedAt13 = message.substring(0, 13);
        }
        else
        {
            FT8MessageFixedAt13 = message;
        }
        logger.info("FT8 at 13 chars : |" + FT8MessageFixedAt13 + "|");

        BigInteger big = BigInteger.ZERO;

        for (int i = 0; i < FT8MessageFixedAt13.length(); i++)
        {
            logger.fine("next char : " + FT8MessageFixedAt13.charAt(i) + ", encoded val : " + encodeChar(FT8MessageFixedAt13.charAt(i)));

            logger.fine("big before add : " + big.toString());
            big = big.add(BigInteger.valueOf(encodeChar(FT8MessageFixedAt13.charAt(i))));

            // do not multiply after last char is added
            if (i != (FT8MessageFixedAt13.length() - 1))
            {
                big = big.multiply(BigInteger.valueOf(42));
            }
            logger.fine("big after add  : " + big.toString());
        }

        logger.fine("big after add   : " + big.toString(2) + ", length : " + big.bitLength());

        // to label as a free message
        big = big.shiftLeft(6);
        logger.fine("big plus 6      : " + big.toString(2) + ", length : " + big.bitLength());

        logger.fine("there are " + (77 - big.bitLength()) + " leading zeroes");

        BigInteger bigWithLeadingBit = BigInteger.ONE;
        bigWithLeadingBit = bigWithLeadingBit.shiftLeft(77);
        logger.fine("big leading bit : " + bigWithLeadingBit.toString(2) + ", length : " + bigWithLeadingBit.bitLength());

        big = big.add(bigWithLeadingBit);
        logger.info("big - fixed 78  : " + big.toString(2) + ", length : " + big.bitLength());

        /* must be :
 00110110011110001111010110111111101000000001001010100001110110001010101000000
is :
100110110011110001111010110111111101000000001001010100001110110001010101000000         
         */
        // shift left - 14 bits plus 5 bits, to make a multiple of 16
        big = big.shiftLeft(19);
        logger.info("big - fixed 97  : " + big.toString(2) + ", length : " + big.bitLength());

        BitSet shiftReg = new BitSet(14);
        BitSet polynomial = new BitSet(14);
        // 0x4757
        polynomial.set(0);
        polynomial.set(1);
        polynomial.set(2);
        polynomial.set(4);
        polynomial.set(6);
        polynomial.set(8);
        polynomial.set(9);
        polynomial.set(10);
        polynomial.set(13);

        // shift 96 bit out to the left -- better : iterate from left to right
        // most significant bit comes out first
        // do not shift out most significant bit
        
        
        
        
        
        
        // take fixed length !!
        
        
        
        
        
        
        
        
        for (int i = big.bitLength() - 2; i >= 0; i--)
        {
            boolean leftOutFromBig = big.testBit(i);
            logger.fine("left out : " + leftOutFromBig);

            // shift the reg to the left, reserve the first leftout
            boolean leftOutFromReg = shiftReg.get(13);
            for (int j = 13; j >= 1; j--)
            {
                logger.fine("j : " + j);
                shiftReg.set(j, shiftReg.get(j - 1));
            }

            // push in leftOut from the big
            shiftReg.set(0, leftOutFromBig);

            // XOR with polynomial if leftOutFromBig is true
            if (leftOutFromReg)
            {
                shiftReg.xor(polynomial);
            }
        }

        // must be 10110011001000
        logger.info("CRC : " + shiftReg);
        // least is first out
        for (int j = 0; j <= 13; j++)
        {
            logger.info("CRC bit - LSB first : " + shiftReg.get(j));
        }

        // parity bits
        BitSet bsParityBits = new BitSet(83);
        initBitSetWithTrue(bsParityBits);
        for (int i = 0; i < ldpc_174_91_generator.ldpc_174_91.length; i++)
        {
            logger.info("ldpc row - 23 hex digits - 92 bits in hex - last bit is not used : " + ldpc_174_91_generator.ldpc_174_91[i]);

            BitSet bsLDPCRow = new BitSet(91);
            //    initBitSetWithTrue(bsLDPCRow);
            for (int n = 0; n < 91; n++)
            {
                bsLDPCRow.set(n, true);
            }
            dumpBitSet(bsLDPCRow);

            // highest index in the BitSet is the MSB
            int iBsLDPCRow = 90;
            // logger.info("size bsLDPCRow : " + bsLDPCRow.length());

            //   byte[] bRow = ldpc_174_91_generator.ldpc_174_91[i].getBytes();
            // take one hex byte at the time from sRow and make 4 bits in the bsRow bitset
            for (int j = 0; j < 23; j++)
            {
                char c = ldpc_174_91_generator.ldpc_174_91[i].charAt(j);
                logger.info("hex byte : " + c);
                byte l = (byte) hexToBin(c);
                for (int k = 3; k >= 0; k--)
                {
                    boolean isSet = isBitSet(l, k);
                    // skip the last bit of the last hex char -- not used
                    if (iBsLDPCRow >= 0)
                    {
                        bsLDPCRow.set(iBsLDPCRow, isSet);
                        logger.info("iBsLDPCRow : " + iBsLDPCRow + ", bit set - MSB first : " + isSet);
                    }
                    iBsLDPCRow--;
                }
            }

            dumpBitSet(bsLDPCRow);
            
            // BitSet bsLDPCRow now contains 91 bits -- MSB is at position 91
            // xor bsLDPCRow with the 91 data bits, xor from right to left, so MSB first 
            BitSet bsXORResult = new BitSet(91);
            //      initBitSetWithTrue(bsXORResult);

            int bitCount = 0;
            for (int j = 96; j >= 6; j--)
            {
                // count the number of bits
                boolean xorResult = big.testBit(j) ^ bsXORResult.get(j);
                if (xorResult)
                {
                    bitCount++;
                }
            }

            logger.info("bitCount : " + bitCount);

            // even parity - https://en.wikipedia.org/wiki/Parity_bit
            boolean evenParityBoolean = false;
            if ((bitCount & 1) == 0)
            {
                // even
                evenParityBoolean = true;
                bsParityBits.set(i);
            }
            else
            {
                // odd
                // nok !!
                bsParityBits.set(i);
            }
            logger.info("parity : " + evenParityBoolean + ", i : " + i);

            //    bsParityBits.set(i, evenParityBoolean);
        }

        //  dumpBitSet(bsParityBits);

        /*
        You could also use the constructor BigInteger(String val, int radix) - 
        which might be readily more apparently what's going on in your code if you don't mind a performance hit for parsing a String. 
        Then you could generate a string like val = "111111111111111111111111111111111111111" and then call BigInteger myInt = new BigInteger(val, 2); 
        - resulting in the same 39 bit integer.
         */
        return null;
    }

    byte encodeChar(char c)
    {
        // return 0
        if (c == ' ')
        {
            return 0;
        }
        // return 1 to 10
        else if ((c >= '0') && (c <= '9'))
        {
            return ((byte) (c - 29));
        }
        // return 11 to 36
        else if ((c >= 'A') && (c <= 'Z'))
        {
            return ((byte) (c - 54));
        }
        else if (c == '+')
        {
            return 37;
        }
        else if (c == '-')
        {
            return 38;
        }
        else if (c == '.')
        {
            return 39;
        }
        else if (c == '/')
        {
            return 40;
        }
        else if (c == '?')
        {
            return 41;
        }
        else
        {
            logger.severe("Invalid char");
        }

        return 0;
    }

    public String padSpaces(String inputString, int length)
    {
        if (inputString.length() >= length)
        {
            return inputString;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(inputString);
        while (sb.length() < length)
        {
            sb.append(' ');
        }

        return sb.toString();
    }

    private int hexToBin(char ch)
    {
        if ('0' <= ch && ch <= '9')
        {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F')
        {
            return ch - 'A' + 10;
        }
        if ('a' <= ch && ch <= 'f')
        {
            return ch - 'a' + 10;
        }
        return -1;
    }

    // where bit ranges from 0 to 7
    private Boolean isBitSet(byte b, int bit)
    {
        return (b & (1 << bit)) != 0;
    }

    // obsolete -- use toString with Radix
    public void dumpBigInteger(BigInteger n)
    {
        if (n.compareTo(BigInteger.ZERO) < 0)
        {
            throw new IllegalArgumentException("n must not be negative");
        }
        logger.info("len in bits : " + n.bitLength() + ", dump : " + n.toString());

        // least significant bit comes out first
        for (int i = 0; i < n.bitLength(); i++)
        {
            logger.fine("bit : " + n.testBit(i));
        }

        String dump = "";
        // highest significant bit comes first
        for (int i = n.bitLength() - 1; i >= 0; i--)
        {
            char c;
            if (n.testBit(i))
            {
                c = '1';
            }
            else
            {
                c = '0';
            }
            dump = dump + c;
        }
        logger.info("dump big : " + dump);
    }

    public void initBitSetWithTrue(BitSet bs)
    {
        // Do not use the length to conclude the no of bits modified !!
        
        
        for (int i = 0; i < bs.length(); i++)
        {
            bs.set(i);
        }
    }

    // highest index comes first
    public void dumpBitSet(BitSet bs)
    {
        StringBuilder s = new StringBuilder();
        //   for (int i = 0; i < bs.length(); i++)
        for (int i = bs.length() - 1; i >= 0; i--)
        {
            s.append(bs.get(i) == true ? 1 : 0);
        }

        logger.info("bitset len : " + bs.length() + ", dump : " + s);
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

    public String printArray(byte[] array)
    // MSB first, LSB last
    {
        if (array == null)
        {
            return "";
        }

        String s = "";
        for (int i = array.length - 1; i >= 0; i--)
        {
            if (array[i] == 0)
            {
                s = s.concat("0, ");
            }
            else
            {
                s = s.concat("1, ");
            }
        }
        return (s);
    }
}
