package dsp;

import static common.Constants.SAMPLE_RATE;
import static java.lang.Math.round;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.logging.Logger;

public class FT8
{
    // continuous phase 8-FSK, tone separation 1,4648 Hz, is done by taking one more / less sinus per symbol
    // symbol duration : NR_OF_SAMPLES_PER_SYMBOL / 12000 = 0,16 sec with NR_OF_SAMPLES_PER_SYMBOL = 1920
    // number of sinuses per symbol = NR_OF_SAMPLES_PER_SYMBOL * fBase / sampleRate
    // 1920 samples * 1500 Hz / 12000 samples per sec = 240 sinuses 
    // 241 sinuses => 1506,25 Hz
    // 79 symbols * 1920 / 12000 secs = 12,64 secs

    static final Logger logger = Logger.getLogger(FT8.class.getName());

    public static final int NR_OF_SAMPLES_PER_SYMBOL = 1920;

    public String message;

    private double[] symbol0;
    private double[] symbol1;
    private double[] symbol2;
    private double[] symbol3;

    public double[] makeAudio(int fSelected, double gain)
    {
        make8AudioSymbols(fSelected, gain);

        byte[] symbols = encode(message);

        double[] dBufferOut = new double[symbols.length * NR_OF_SAMPLES_PER_SYMBOL];
        int iBufferOut = 0;
        for (int i = 0; i < symbols.length; i++)
        {
            for (int j = 0; j < NR_OF_SAMPLES_PER_SYMBOL; j++)
            {
                switch (symbols[i])
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
            iBufferOut = iBufferOut + NR_OF_SAMPLES_PER_SYMBOL;
        }
        return dBufferOut;
    }

    // returns 79 bytes, 1 symbol per byte
    private byte[] encode(String message)
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

        // radix 2 -- leading zeroes are not shown, because .length() stops at last bit (1234 is shown iso 001234)
        logger.fine("big after add   : " + big.toString(2) + ", length : " + big.bitLength());

        // to label as a free message
        BigInteger bigSource77 = big.shiftLeft(6);

        logger.info("Source-encoded message, 77 bits : " + dumpBigInteger(bigSource77, 77));
        /* must be :
        00110110011110001111010110111111101000000001001010100001110110001010101000000
         */

        // since BigIntger is a byte array, you can use testBit() for an unlimited number of bits, even in a short BigInteger
        // shift left - 14 bits plus 5 bits, to make a multiple of 16
        BigInteger bigSource96 = bigSource77.shiftLeft(19);

        // big is now 77 + 19 = 96 bit fixed length
        BitSet bsCRCShiftReg = new BitSet(14);
        BitSet bsPolynomial = new BitSet(14);
        // 0x4757
        bsPolynomial.set(0);
        bsPolynomial.set(1);
        bsPolynomial.set(2);
        bsPolynomial.set(4);
        bsPolynomial.set(6);
        bsPolynomial.set(8);
        bsPolynomial.set(9);
        bsPolynomial.set(10);
        bsPolynomial.set(13);

        // shift 96 bit out to the left -- better : iterate from left to right
        // most significant bit comes out first 
        // do not use .length() -> will not include leading zeroes
        for (int i = 95; i >= 0; i--)
        {
            boolean leftOutFromBig = bigSource96.testBit(i);
            logger.fine("left out : " + leftOutFromBig);

            // shift the reg to the left, reserve the first leftout
            boolean leftOutFromReg = bsCRCShiftReg.get(13);
            for (int j = 13; j >= 1; j--)
            {
                logger.fine("j : " + j);
                bsCRCShiftReg.set(j, bsCRCShiftReg.get(j - 1));
            }

            // push in leftOut from the big
            bsCRCShiftReg.set(0, leftOutFromBig);

            // XOR with polynomial if leftOutFromBig is true
            if (leftOutFromReg)
            {
                bsCRCShiftReg.xor(bsPolynomial);
            }
        }

        // must be 10110011001000
        logger.fine("CRC : " + bsCRCShiftReg);
        logger.info("14-bit CRC : " + dumpBitSet(bsCRCShiftReg, 14));

        // concat 77 bit source bigInteger with 14 bit CRC => 91 bit
        BitSet bsSourcePlusCRC91 = new BitSet(91);
        for (int j = 90; j >= 14; j--)
        {
            bsSourcePlusCRC91.set(j, bigSource77.testBit(j - 14));
        }
        for (int j = 13; j >= 0; j--)
        {
            bsSourcePlusCRC91.set(j, bsCRCShiftReg.get(j));
        }

        logger.info("bigSourcePlusCRC91 : " + dumpBitSet(bsSourcePlusCRC91, 91));

        // parity bits
        BitSet bsParityBits = new BitSet(83);
        for (int i = 0; i < ldpc_174_91_generator.ldpc_174_91.length; i++)
        {
            logger.info("ldpc row - 23 hex digits - 92 bits in hex - last bit is not used : " + ldpc_174_91_generator.ldpc_174_91[i]);

            BitSet bsLDPCRow = new BitSet(91);

            // highest index in the BitSet is the MSB
            int iBsLDPCRow = 90;

            // take one hex byte at the time from sRow and make 4 bits in the bsRow bitset
            for (int j = 0; j < 23; j++)
            {
                char c = ldpc_174_91_generator.ldpc_174_91[i].charAt(j);
                logger.fine("hex byte : " + c);
                byte l = (byte) hexToBin(c);
                for (int k = 3; k >= 0; k--)
                {
                    // skip the last bit of the last hex char -- not used
                    if (iBsLDPCRow >= 0)
                    {
                        bsLDPCRow.set(iBsLDPCRow, isBitSet(l, k));
                        logger.fine("iBsLDPCRow : " + iBsLDPCRow + ", bit set - MSB first : " + isBitSet(l, k));
                    }
                    iBsLDPCRow--;
                }
            }

            // BitSet bsLDPCRow now contains 91 bits -- MSB is at position 91
            logger.info("bsLDPCRow         : " + dumpBitSet(bsLDPCRow, 91));
            logger.info("bsSourcePlusCRC91 : " + dumpBitSet(bsSourcePlusCRC91, 91));

            // AND bsLDPCRow with the 91 data bits, xor from right to left, so MSB first 
            bsLDPCRow.and(bsSourcePlusCRC91);
            logger.info("after AND         : " + dumpBitSet(bsLDPCRow, 91));

            int bitCount = 0;

            for (int j = 90; j >= 0; j--)
            {
                // count the number of bits               
                if (bsLDPCRow.get(j))
                {
                    bitCount++;
                }
            }

            logger.info("bitCount : " + bitCount);

            // even parity - https://en.wikipedia.org/wiki/Parity_bit          
            if ((bitCount & 1) == 0)
            {
                bsParityBits.set(82 - i, false);
            }
            else
            {
                bsParityBits.set(82 - i, true);
            }
        }

        /* should be :
        00100011101100010110001110100011000001010110110100101111110110000011010000100111110            
         */
        logger.info("83 Parity bits : " + dumpBitSet(bsParityBits, 83));

        // make the codeword
        // 77 bits (source) + 14 bits (CRC) + 83 bits (Parity) = 174 bits
        // bsSourcePlusCRC91        bsParityBits
        // BigInteger is big-endian, while BitSet is little-endian,
        BigInteger bigCodeWord174 = BigInteger.ZERO;
        for (int j = 0; j < 83; j++)
        {
            if (bsParityBits.get(j))
            {
                bigCodeWord174 = bigCodeWord174.setBit(j);
            }
        }
        for (int j = 0; j < 91; j++)
        {
            if (bsSourcePlusCRC91.get(j))
            {
                bigCodeWord174 = bigCodeWord174.setBit(j + 83);
            }
        }

        // should be
        // 001101100111100011110101101111111010000000010010101000011101100010101010000001011001100100000100011101100010110001110100011000001010110110100101111110110000011010000100111110
        logger.info("Code word      : " + dumpBigInteger(bigCodeWord174, 174));

        /*
        code word is 174 bits
        nr of symbols = 174 / 3 = 58
        58/2 = 29 symbols per block


        78-72     71-43       42-36     35-7       6-0
        sync      block1      sync      block2     sync

        index i   71-43                 35-7 -> index in symbols
        index j  173-87                 86-0 -> index in code word
         */
        byte[] symbol = new byte[79];
        for (int i = 0; i < 79; i++)
        {
            switch (i)
            {
                case 0:
                case 36:
                case 72:
                    symbol[i] = 2;
                    break;

                case 1:
                case 37:
                case 73:
                    symbol[i] = 5;
                    break;
                case 2:
                case 38:
                case 74:
                    symbol[i] = 6;
                    break;
                case 3:
                case 39:
                case 75:
                    symbol[i] = 0;
                    break;
                case 4:
                case 40:
                case 76:
                    symbol[i] = 4;
                    break;
                case 5:
                case 41:
                case 77:
                    symbol[i] = 1;
                    break;
                case 6:
                case 42:
                case 78:
                    symbol[i] = 3;
                    break;

                default:
                {
                    // take 3 bits from bigCodeWord174
                    int j = 0;
                    if ((i >= 7) && (i <= 37))
                    {
                        j = 3 * (i - 7);
                    }
                    else if ((i >= 43) && (i <= 71))
                    {
                        j = 3 * (i - 43) + 87;
                    }
                    else
                    {
                        logger.severe("in switch : " + i);
                    }

                    logger.fine("i : " + i + ",j : " + j);

                    symbol[i] = grayCode(bigCodeWord174.testBit(j + 2), bigCodeWord174.testBit(j + 1), bigCodeWord174.testBit(j));
                    break;
                }
            }
        }

        // should be : 3140652 16575246677300336026536301215 3140652 50526534145201344567440230574 3140652
        logger.info("symbols - MSB first : " + printArray(symbol));

        return symbol;
    }

    private byte encodeChar(char c)
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

    private String padSpaces(String inputString, int length)
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

    // highest index comes first
    public String dumpBigInteger(BigInteger n, int nrOfBits)
    {
        if (n.compareTo(BigInteger.ZERO) < 0)
        {
            throw new IllegalArgumentException("n must not be negative");
        }

        // most significant bit comes out first
        for (int i = nrOfBits - 1; i >= 0; i--)
        {
            logger.fine("bit : " + n.testBit(i));
        }

        String dump = "";
        // highest significant bit comes first
        for (int i = nrOfBits - 1; i >= 0; i--)
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
        return dump;
    }

    private void initBitSetWithTrue(BitSet bs, int nrOfBits)
    {
        // Do not use the length to calculate the no of bits modified !!
        for (int i = 0; i < nrOfBits; i++)
        {
            bs.set(i);
        }
    }

    // highest index comes first
    private String dumpBitSet(BitSet bs, int nrOfBits)
    {
        String dump = "";

        for (int i = nrOfBits - 1; i >= 0; i--)
        {
            char c;
            if (bs.get(i))
            {
                c = '1';
            }
            else
            {
                c = '0';
            }
            dump = dump + c;
        }
        return dump;
    }

    private byte grayCode(boolean b2, boolean b1, boolean b0)
    {
        if (!b2 && !b1 && !b0)
        {
            return 0;
        }
        else if (!b2 && !b1 && b0)
        {
            return 1;
        }
        else if (!b2 && b1 && b0)
        {
            return 2;
        }
        else if (!b2 && b1 && !b0)
        {
            return 3;
        }
        else if (b2 && b1 && !b0)
        {
            return 4;
        }
        else if (b2 && !b1 && !b0)
        {
            return 5;
        }
        else if (b2 && !b1 && b0)
        {
            return 6;
        }
        else if (b2 && b1 && b0)
        {
            return 7;
        }

        logger.severe("in graycode");
        return 0;
    }

    private String printArray(byte[] array)
    // MSB first, LSB last
    {
        if (array == null)
        {
            return "";
        }

        String s = "";
        for (int i = array.length - 1; i >= 0; i--)
        {
            s = s.concat(Byte.toString(array[i]));
        }
        return (s);
    }

    public double baseFreq(int fSelected)
    {
        long nrOfSinusesPerSymbol = round((double) NR_OF_SAMPLES_PER_SYMBOL * fSelected / SAMPLE_RATE);
        double dBase = (double) nrOfSinusesPerSymbol * SAMPLE_RATE / NR_OF_SAMPLES_PER_SYMBOL;
        logger.info("sinuses per symbol : " + nrOfSinusesPerSymbol + ", fBase : " + dBase);

        return dBase;
    }

    private void make8AudioSymbols(int fSelected, double gain)
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
}
