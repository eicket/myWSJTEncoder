package filter;

public class BandPassFilter
{

    public static void CalculateFilter(double[] filter, int cutFreq, int filterWidth, float sampleRate)
    {
        int M = filter.length - 1;
        // t1 < t2    
        double ft1 = cutFreq / (double) sampleRate;
        double ft2 = (cutFreq + filterWidth) / (double) sampleRate;

        for (int i = 0; i < M; i++)
        {
            // bandpass from labbookpages.co.uk, t1 < t2            
            double filterWeight;

            if (i == (filter.length - 1) / 2.0)
            {
                filterWeight = 2 * (ft2 - ft1);
            } else
            {
                filterWeight = Math.sin(2 * Math.PI * ft2 * (i - M / 2.0)) / (Math.PI * (i - M / 2.0)) - Math.sin(2 * Math.PI * ft1 * (i - M / 2.0)) / (Math.PI * (i - M / 2.0));
            }

            // apply a Blackman window
            filter[i] = (0.42 - 0.5 * Math.cos((2 * Math.PI * i) / (double) M) + 0.08 * Math.cos((4 * Math.PI * i) / (double) M)) * filterWeight;
        }
    }
}
