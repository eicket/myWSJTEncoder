// Erik Icket, ON4PB - 2022

package test.listERF;

import static dsp.ErrorFunction.erf;
import java.util.logging.Logger;

public class ListERF
{

    static final Logger logger = Logger.getLogger(test.listERF.ListERF.class.getName());

    public static void main(String argv[])
    {
        for (int i = -10; i <= 10; i++)
        {
            logger.info("i : " + i + ", erf : " + erf(i));
        }
    }
}
