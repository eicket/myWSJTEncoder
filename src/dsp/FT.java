// Erik Icket, ON4PB - 2022

package dsp;

import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT4;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8;
import static common.Constants.SAMPLE_RATE;
import static dsp.Utils.makeAudioSymbols;
import static dsp.Utils.printArray;
import static dsp.Utils.reverseByteArray;
import static java.lang.Math.round;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.logging.Logger;

public class FT
{

    static final Logger logger = Logger.getLogger(FT.class.getName());

    // set when button is clicked, with input from the text field
    public String message;

    public double[] makeFT4Audio(int fSelected, double gain)
    {
        // continuous phase 4-FSK, tone separation 20,833 Hz, is done by taking one more / less sinus per symbol
        // symbol duration : NR_OF_SAMPLES_PER_SYMBOL / 12000 = 0,048 sec with NR_OF_SAMPLES_PER_SYMBOL = 576
        // number of sinuses per symbol = NR_OF_SAMPLES_PER_SYMBOL * fBase / sampleRate
        // 576 samples * 1500 Hz / 12000 samples per sec = 72 sinuses 
        // 73 sinuses => 1520,833 Hz
        // 79 symbols * 576 / 12000 secs = 12,64 secs

        BigInteger bigCodeWord174 = encode(message, true);
        byte[] symbols = makeFT4Symbols(bigCodeWord174);
        reverseByteArray(symbols);

        int nrOfSinusesPerSymbol = (int) round((double) NR_OF_SAMPLES_PER_SYMBOL_FT4 * fSelected / SAMPLE_RATE);
        logger.fine("starting number of sinuses per symbol : " + nrOfSinusesPerSymbol);

        double[] symbol0 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT4);
        nrOfSinusesPerSymbol++;
        double[] symbol1 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT4);
        nrOfSinusesPerSymbol++;
        double[] symbol2 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT4);
        nrOfSinusesPerSymbol++;
        double[] symbol3 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT4);

        double[] dBufferOut = new double[symbols.length * NR_OF_SAMPLES_PER_SYMBOL_FT4];
        int iBufferOut = 0;
        for (int i = 0; i < symbols.length; i++)
        {
            for (int j = 0; j < NR_OF_SAMPLES_PER_SYMBOL_FT4; j++)
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
            iBufferOut = iBufferOut + NR_OF_SAMPLES_PER_SYMBOL_FT4;
        }
        return dBufferOut;
    }

    public double[] makeFT8Audio(int fSelected, double gain)
    {
        // continuous phase 8-FSK, tone separation 1,4648 Hz, is done by taking one more / less sinus per symbol
        // symbol duration : NR_OF_SAMPLES_PER_SYMBOL / 12000 = 0,16 sec with NR_OF_SAMPLES_PER_SYMBOL = 1920
        // number of sinuses per symbol = NR_OF_SAMPLES_PER_SYMBOL * fBase / sampleRate
        // 1920 samples * 1500 Hz / 12000 samples per sec = 240 sinuses 
        // 241 sinuses => 1506,25 Hz
        // 79 symbols * 1920 / 12000 secs = 12,64 secs

        BigInteger bigCodeWord174 = encode(message, false);
        byte[] symbols = makeFT8Symbols(bigCodeWord174);
        reverseByteArray(symbols);

        int nrOfSinusesPerSymbol = (int) round((double) NR_OF_SAMPLES_PER_SYMBOL_FT8 * fSelected / SAMPLE_RATE);
        logger.fine("starting number of sinuses per symbol : " + nrOfSinusesPerSymbol);

        double[] symbol0 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT8);
        nrOfSinusesPerSymbol++;
        double[] symbol1 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT8);
        nrOfSinusesPerSymbol++;
        double[] symbol2 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT8);
        nrOfSinusesPerSymbol++;
        double[] symbol3 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT8);
        nrOfSinusesPerSymbol++;
        double[] symbol4 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT8);
        nrOfSinusesPerSymbol++;
        double[] symbol5 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT8);
        nrOfSinusesPerSymbol++;
        double[] symbol6 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT8);
        nrOfSinusesPerSymbol++;
        double[] symbol7 = makeAudioSymbols(nrOfSinusesPerSymbol, gain, NR_OF_SAMPLES_PER_SYMBOL_FT8);

        double[] dBufferOut = new double[symbols.length * NR_OF_SAMPLES_PER_SYMBOL_FT8];
        int iBufferOut = 0;
        for (int i = 0; i < symbols.length; i++)
        {
            for (int j = 0; j < NR_OF_SAMPLES_PER_SYMBOL_FT8; j++)
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
                    case 4:
                        dBufferOut[iBufferOut + j] = symbol4[j];
                        break;
                    case 5:
                        dBufferOut[iBufferOut + j] = symbol5[j];
                        break;
                    case 6:
                        dBufferOut[iBufferOut + j] = symbol6[j];
                        break;
                    case 7:
                        dBufferOut[iBufferOut + j] = symbol7[j];
                        break;
                }
            }
            iBufferOut = iBufferOut + NR_OF_SAMPLES_PER_SYMBOL_FT8;
        }
        return dBufferOut;
    }

    // returns 79 bytes, 1 symbol per byte
    private BigInteger encode(String message, boolean scramble)
    {
        String FTMessageFixedAt13;

        if (message.length() < 13)
        {
            // pad until we have 13 chars
            FTMessageFixedAt13 = padSpaces(message, 13);
        }
        else if (message.length() > 13)
        {
            logger.severe("FT message is too long");
            FTMessageFixedAt13 = message.substring(0, 13);
        }
        else
        {
            FTMessageFixedAt13 = message;
        }
        logger.fine("FT at 13 chars : |" + FTMessageFixedAt13 + "|");

        BigInteger big = BigInteger.ZERO;
        for (int i = 0; i < FTMessageFixedAt13.length(); i++)
        {
            logger.fine("next char : " + FTMessageFixedAt13.charAt(i) + ", encoded val : " + encodeChar(FTMessageFixedAt13.charAt(i)));

            logger.fine("big before add : " + big.toString());
            big = big.add(BigInteger.valueOf(encodeChar(FTMessageFixedAt13.charAt(i))));

            // do not multiply after last char is added
            if (i != (FTMessageFixedAt13.length() - 1))
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

        if (scramble)
        {
            // scramble, only for FT4
            logger.fine("Before scramble, 77 bits   : " + dumpBigInteger(bigSource77, 77));
            
            BigInteger bigScramblingVector = StringToBigInteger("01001010010111101000100110110100101100001000101001111001010101011011111000101");
            logger.fine("Scrambling vect, 77 bits   : " + dumpBigInteger(bigScramblingVector, 77));

            bigSource77 = bigSource77.xor(bigScramblingVector);
            logger.fine("After scramble, 77 bits    : " + dumpBigInteger(bigSource77, 77));
        }

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
        logger.fine("14-bit CRC : " + dumpBitSet(bsCRCShiftReg, 14));

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

        logger.fine("bigSourcePlusCRC91 : " + dumpBitSet(bsSourcePlusCRC91, 91));

        // parity bits
        BitSet bsParityBits = new BitSet(83);
        for (int i = 0; i < ldpc_174_91_generator.ldpc_174_91.length; i++)
        {
            logger.fine("ldpc row - 23 hex digits - 92 bits in hex - last bit is not used : " + ldpc_174_91_generator.ldpc_174_91[i]);

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
            logger.fine("bsLDPCRow         : " + dumpBitSet(bsLDPCRow, 91));
            logger.fine("bsSourcePlusCRC91 : " + dumpBitSet(bsSourcePlusCRC91, 91));

            // AND bsLDPCRow with the 91 data bits, xor from right to left, so MSB first 
            bsLDPCRow.and(bsSourcePlusCRC91);
            logger.fine("after AND         : " + dumpBitSet(bsLDPCRow, 91));

            int bitCount = 0;

            for (int j = 90; j >= 0; j--)
            {
                // count the number of bits               
                if (bsLDPCRow.get(j))
                {
                    bitCount++;
                }
            }

            logger.fine("bitCount : " + bitCount);

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
        logger.fine("83 Parity bits : " + dumpBitSet(bsParityBits, 83));

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
        logger.fine("Code word      : " + dumpBigInteger(bigCodeWord174, 174));

        return bigCodeWord174;
    }

    private byte[] makeFT4Symbols(BigInteger bigCodeWord174)
    {
        /*
        code word is 174 bits
        nr of data symbols = 174 / 2 = 87
        87/3 = 29 data symbols per block

        start and stop with a special ramped up 0 symbol

        104    103-100    99-71       70-67     66-38       37-34     33-5       4-1        0
        0      sync1      block1      sync2     block2      sync3     block3     sync4      0
        0      0132                   1023                  2310                 3201       0
        
        index i           99-71                 66-38                 33-5 -> index in symbols
        index j           173,172-116           115,114-58            57,55-0 -> index in code word
         */
        byte[] symbol = new byte[105];
        for (int i = 0; i < 105; i++)
        {
            switch (i)
            {
                // 0, 104 : ramp up with 0
                case 0:
                case 104:

                case 2:
                case 34:
                case 69:
                case 103:
                    symbol[i] = 0;
                    break;

                case 1:
                case 35:
                case 70:
                case 102:
                    symbol[i] = 1;
                    break;

                case 3:
                case 37:
                case 68:
                case 100:
                    symbol[i] = 2;

                    break;
                case 4:
                case 36:
                case 67:
                case 101:
                    symbol[i] = 3;
                    break;

                default:
                {
                    // take 2 bits from bigCodeWord174
                    int j = 0;
                    if ((i >= 5) && (i <= 33))
                    {
                        j = 2 * (i - 5);
                    }
                    else if ((i >= 38) && (i <= 66))
                    {
                        j = 2 * (i - 38) + 58;
                    }
                    else if ((i >= 71) && (i <= 99))
                    {
                        j = 2 * (i - 71) + 116;
                    }
                    else
                    {
                        logger.severe("in switch : " + i);
                    }

                    logger.fine("i : " + i + ",j : " + j);

                    symbol[i] = grayCodePer2(bigCodeWord174.testBit(j + 1), bigCodeWord174.testBit(j));
                    break;
                }
            }
        }

        // should be : 3140652 16575246677300336026536301215 3140652 50526534145201344567440230574 3140652
        //             3151763 16575246677300336026536301215 3151763 50526534145201344567440230574 3151763
        //             3151763 16575246677300336026536301215 3151763 50526534145201344567440230574 3151763
        logger.info("symbols - MSB first : " + printArray(symbol));

        return symbol;
    }

    private byte[] makeFT8Symbols(BigInteger bigCodeWord174)
    {
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
                    //          symbol[i] = 2;
                    symbol[i] = 2;
                    break;

                case 1:
                case 37:
                case 73:
                    //           symbol[i] = 5;
                    symbol[i] = 5;
                    break;
                case 2:
                case 38:
                case 74:
                    // symbol[i] = 6;
                    symbol[i] = 6;

                    break;
                case 3:
                case 39:
                case 75:
                    //  symbol[i] = 0;
                    symbol[i] = 0;
                    break;
                case 4:
                case 40:
                case 76:
                    //   symbol[i] = 4;
                    symbol[i] = 4;
                    break;
                case 5:
                case 41:
                case 77:
                    //  symbol[i] = 1;
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

                    symbol[i] = grayCodePer3(bigCodeWord174.testBit(j + 2), bigCodeWord174.testBit(j + 1), bigCodeWord174.testBit(j));
                    break;
                }
            }
        }

        // should be : 3140652 16575246677300336026536301215 3140652 50526534145201344567440230574 3140652
        //             3151763 16575246677300336026536301215 3151763 50526534145201344567440230574 3151763
        //             3151763 16575246677300336026536301215 3151763 50526534145201344567440230574 3151763
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
            return ((byte) (c - 47));
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

    /*
    private BitSet StringToBitset(String binary)
    {
        BitSet bitset = new BitSet(binary.length());
        for (int i = 0; i < binary.length(); i++)
        {
            if (binary.charAt(i) == '1')
            {
                bitset.set(i);
            }
        }
        return bitset;
    }
*/

    // fist char in string is the MSB in the BigInteger
    // !! bigInteger is immutable -- so, re-assign !
    private BigInteger StringToBigInteger(String binary)
    {
        logger.fine("len : " + binary.length());

        BigInteger bigInteger = BigInteger.ZERO;

        // left most string char comes out first at index 0
        for (int i = 0; i <= binary.length() - 1; i++)
        {
            logger.fine("i : " + i + ", val : " + binary.charAt(i));
            if (binary.charAt(i) == '1')
            {
                bigInteger = bigInteger.setBit(binary.length() - 1 - i);
                logger.fine(binary.length() - 1 - i + " set");
            }          
        }

        logger.fine("After conversion77 bits    : " + dumpBigInteger(bigInteger, 77));

        return bigInteger;
    }

    private byte grayCodePer2(boolean b1, boolean b0)
    {
        if (!b1 && !b0)
        {
            return 0;
        }
        else if (!b1 && b0)
        {
            return 1;
        }
        else if (b1 && b0)
        {
            return 2;
        }
        else if (b1 && !b0)
        {
            return 3;
        }

        logger.severe("in graycode");
        return 0;
    }

    private byte grayCodePer3(boolean b2, boolean b1, boolean b0)
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
}
