package mysynth;

import static common.Constants.fMax;
import static common.Constants.minSamplesPerPeriod;
import static common.Constants.waveformDuration;
import java.util.logging.Logger;

public class MySynth
{

    int f = 10000; // in Hz

    static final Logger logger = Logger.getLogger(MySynth.class.getName());

    public static void main(String[] args)
    {
        logger.info("Nr of arguments : " + args.length);

        MySynth mySynth = new MySynth();
        mySynth.construct();       
    }

    public void construct()
    {
        int bufferLength = waveformDuration * fMax * minSamplesPerPeriod;
        double[] buffer = new double[bufferLength];
        logger.info("Buffer created with size : " + buffer.length);
        logger.info("MaxInt is : " + Integer.MAX_VALUE);

        double t = 1 / f;
        int samplesPerPeriod = bufferLength / (waveformDuration * f);
        logger.info("samplesPerPeriod is : " + samplesPerPeriod);
        double radiansPerSample = (2 * Math.PI) / samplesPerPeriod;
        logger.info("radiansPerSample is : " + radiansPerSample);

        int iInsidePeriod = 0;

        for (int i = 0; i < bufferLength; i++)
        {
            //logger.info("iInsidePeriod is : " + iInsidePeriod + ", sin : " + x);
            // iInsidePeriod = i % samplesPerPeriod;
            // double x = Math.sin(iInsidePeriod * radiansPerSample);
            
           
            // phase contiguous -- no jumps
            buffer[i] = Math.sin(i * radiansPerSample);

        }
        
         logger.info("Done" );

    }

}
