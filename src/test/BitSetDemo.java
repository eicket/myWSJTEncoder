
package test;

import java.util.BitSet;

public class BitSetDemo
{

    public static void main(String[] args)
    {

        // create 2 bitsets
        BitSet bitset1 = new BitSet(8);
        BitSet bitset2 = new BitSet(8);

        // assign values to bitset1
        bitset1.set(0);
        bitset1.set(1);
        bitset1.set(2);
        bitset1.set(3);
        bitset1.set(4);
        bitset1.set(5);

        // assign values to bitset2
        bitset2.set(2);
        bitset2.set(4);
        bitset2.set(6);
        bitset2.set(8);
        bitset2.set(10);

        // print the sets
        System.out.println("Bitset1:" + bitset1);
        System.out.println("Bitset2:" + bitset2);

        // perform a xor operation between two bitsets
        bitset1.xor(bitset2);
        System.out.println("" + bitset1);
    }
}
