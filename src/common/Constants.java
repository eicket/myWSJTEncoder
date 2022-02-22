// Erik Icket, ON4PB - 2022
package common;

public class Constants
{

    public static final int SAMPLE_RATE = 12000;
    public static final int NR_OF_SAMPLES_PER_SYMBOL_WSPR = 8192;
    public static final int NR_OF_SAMPLES_PER_SYMBOL_FT4 = 576;
    public static final int NR_OF_SAMPLES_PER_SYMBOL_FT8 = 1920;

    // smoothing filter bandwidth (BT)
    public static final float FT8_SYMBOL_BT = 2.0f; 
    public static final float FT4_SYMBOL_BT = 1.0f; 
}
