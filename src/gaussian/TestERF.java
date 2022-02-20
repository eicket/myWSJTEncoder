package gaussian;

import static test.gaussian.ErrorFunction.erf;
import java.util.logging.Logger;

public class TestERF
{

    static final Logger logger = Logger.getLogger(gaussian.TestERF.class.getName());

    public static void main(String argv[])
    {
        for (int i = -10; i <= 10; i++)
        {
            logger.info("i : " + i + ", erf : " + erf(i));
        }
    }
}
