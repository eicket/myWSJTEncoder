// Erik Icket, ON4PB - 2022

package dsp;

import java.util.logging.Logger;

public class Utils
{

    static final Logger logger = Logger.getLogger(Utils.class.getName());   

    public static String printArray(byte[] array)
    // MSB first, LSB last
    {
        if (array == null)
        {
            return "";
        }

        String s = "length : " + array.length + " : ";
        for (int i = array.length - 1; i >= 0; i--)
        {
            s = s.concat(Byte.toString(array[i]));
        }
        return (s);
    }

    public static void reverseByteArray(byte[] array)
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
