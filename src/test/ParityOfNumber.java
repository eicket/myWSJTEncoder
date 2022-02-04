package test;

import java.util.Arrays;

public class ParityOfNumber
{

    private static short computeParityBruteForce(long no)
    {
        int result = 0;
        while (no != 0)
        {
            if ((no & 1) == 1)
            {
                result ^= 1;
            }

            no >>>= 1;
        }

        return (short) (result & 0x1);
    }

    private static short computeParityBasedOnClearingSetBit(long no)
    {
        int result = 0;
        while (no != 0)
        {
            no = no & (no - 1);
            result ^= 1;
        }

        return (short) (result & 0x1);
    }

    private static short computeParityWithCaching(long no)
    {
        int[] cache = new int[(int) Math.pow(2, 16)];
        Arrays.fill(cache, -1);

        int WORD_SIZE = 16;
        int mask = 0xFFFF;

        int masked1 = (int) ((no >>> (3 * WORD_SIZE)) & mask);
        checkAndSetInCache(masked1, cache);

        int masked2 = (int) ((no >>> (2 * WORD_SIZE)) & mask);
        checkAndSetInCache(masked2, cache);

        int masked3 = (int) ((no >>> WORD_SIZE) & mask);
        checkAndSetInCache(masked3, cache);

        int masked4 = (int) (no & mask);
        checkAndSetInCache(masked4, cache);

        int result = (cache[masked1] ^ cache[masked2] ^ cache[masked3] ^ cache[masked4]);
        return (short) (result & 0x1);
    }

    private static void checkAndSetInCache(int val, int[] cache)
    {
        if (cache[val] < 0)
        {
            cache[val] = computeParityBasedOnClearingSetBit(val);
        }
    }

    private static short computeParityMostEfficient(long no)
    {
        no ^= (no >>> 32);
        no ^= (no >>> 16);
        no ^= (no >>> 8);
        no ^= (no >>> 4);
        no ^= (no >>> 2);
        no ^= (no >>> 1);

        return (short) (no & 1);
    }

    public static void main(String[] args)
    {
        long no = 1274849;
        System.out.println("Binary representation of the number: " + Long.toBinaryString(no));

        System.out.println("Is Parity [computeParityBruteForce]: " + computeParityBruteForce(no));
        System.out.println("Is Parity [computeParityBasedOnClearingSetBit]: " + computeParityBasedOnClearingSetBit(no));
        System.out.println("Is Parity [computeParityMostEfficient]: " + computeParityMostEfficient(no));
        System.out.println("Is Parity [computeParityWithCaching]: " + computeParityWithCaching(no));
    }
}
