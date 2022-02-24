// Erik Icket, ON4PB - 2022

package dsp;

import static dsp.ErrorFunction.erf;
import java.util.logging.Logger;


public class MakeWaveform
{

    static final Logger logger = Logger.getLogger(MakeWaveform.class.getName());

    // Calculate GFSK Gaussian smoothed pulse
    // The pulse is infinite but limited to 3 times the symbol length    
    public static double[] GFSKPulse(int nrSamplesPerSymbol, float BT)
    {
        // 5.336446256636997
        double K = Math.PI * Math.sqrt(2 / Math.log(2));
        double[] pulse = new double[3 * nrSamplesPerSymbol];

        for (int i = 0; i < 3 * nrSamplesPerSymbol; ++i)
        {
            // t values are from -1.5 to 1.5
            float t = i / (float) nrSamplesPerSymbol - 1.5f;
            pulse[i] = (erf(K * BT * (t + 0.5f)) - erf(K * BT * (t - 0.5f))) / 2;
        }
        return pulse;
    }

    // Synthesize the waveform and shape the phase with the GFSK smoothed pulse
    public static double[] synthesizeWithGFSK(byte[] symbols, float fBase, float symbolBT, int nrSamplesPerSymbol, int sampleRate, double gain)
    {
        int nrSymbols = symbols.length;
        int nrOutputSamples = nrSymbols * nrSamplesPerSymbol;

        // modulation index is 1, single tone spacing
        float modulationIndex = 1.0f;

        // number of sines per symbol = NR_OF_SAMPLES_PER_SYMBOL * fBase / sampleRate
        logger.info("synthesizing : f0 : " + fBase + ", sinuses per symbol : " + nrSamplesPerSymbol * fBase / sampleRate + ", symbolBT : " + symbolBT + ", samples per symbol : " + nrSamplesPerSymbol);

        // delta phase advance in radians, for each tone going up
        double dphiBetweenTones = 2 * Math.PI * modulationIndex / nrSamplesPerSymbol;

        // array of delta phase advance in radians, between each sample
        // add two symbols for leading and trailing GFSK pulse symbol 
        double[] dphi = new double[nrOutputSamples + 2 * nrSamplesPerSymbol];

        // set the delta phase advance for the base frequency
        // for 1500 Hz, this is 0,785
        for (int i = 0; i < nrOutputSamples + 2 * nrSamplesPerSymbol; i++)
        {
            dphi[i] = 2 * Math.PI * fBase / sampleRate;
        }

        double pulse[] = GFSKPulse(nrSamplesPerSymbol, symbolBT);

        // loop for every symbol
        for (int iSymbol = 0; iSymbol < nrSymbols; iSymbol++)
        {
            int iSample = iSymbol * nrSamplesPerSymbol;

            // loop for every sample of the pulse
            for (int iPulse = 0; iPulse < 3 * nrSamplesPerSymbol; iPulse++)
            {
                dphi[iPulse + iSample] += dphiBetweenTones * symbols[iSymbol] * pulse[iPulse];
            }
        }

        // make the fDeviation, the amplitude of the sample is the sine of the phase
        double[] audio = new double[nrOutputSamples];
        double phi = 0;
        for (int k = 0; k < nrOutputSamples; k++)
        {
            audio[k] = Math.sin(phi) * gain;

            // skip the first symbol by adding + nrSamplesPerSymbol to index in dphi
            // no modulo count 2 PI for increasing number of radians - phi runs up to 120818.5407310197 radians         
            phi = phi + dphi[k + nrSamplesPerSymbol];
        }

        // Apply envelope shaping to the first and last symbols
        // 8 PI t / T
        // 8 PI iSample / nrSamplesPerSymbol
        // PI iSample / ramp
        int ramp = nrSamplesPerSymbol / 8;
        for (int i = 0; i < ramp; i++)
        {
            double envelope = (1 - Math.cos(Math.PI * i / ramp)) / 2;
            audio[i] *= envelope;
            audio[nrOutputSamples - i - 1] *= envelope;
        }

        return audio;
    }

    // Synthesize the waveform 
    public static double[] synthesizeWithoutGFSK(byte[] symbols, float fBase, int nrSamplesPerSymbol, int sampleRate, double gain)
    {
        int nrSymbols = symbols.length;
        int nrOutputSamples = nrSymbols * nrSamplesPerSymbol;

        // modulation index is 1, single tone spacing
        float modulationIndex = 1.0f;

        // number of sines per symbol = NR_OF_SAMPLES_PER_SYMBOL * fBase / sampleRate
        logger.info("synthesizing : f0 : " + fBase + ", sinuses per symbol : " + nrSamplesPerSymbol * fBase / sampleRate + ", samples per symbol : " + nrSamplesPerSymbol);

        // delta phase advance in radians, for each tone going up
        double dphiBetweenTones = 2 * Math.PI * modulationIndex / nrSamplesPerSymbol;

        // array of delta phase advance in radians, between each sample    
        double[] dphi = new double[nrOutputSamples];

        // set the delta phase advance for the base frequency
        // for 1500 Hz, this is 0,785
        for (int i = 0; i < nrOutputSamples; i++)
        {
            dphi[i] = 2 * Math.PI * fBase / sampleRate;
        }

        // loop for every symbol
        for (int iSymbol = 0; iSymbol < nrSymbols; iSymbol++)
        {
            int iSample = iSymbol * nrSamplesPerSymbol;

            for (int i = 0; i < nrSamplesPerSymbol; i++)
            {
                dphi[i + iSample] += dphiBetweenTones * symbols[iSymbol];
            }
        }

        // make the fDeviation, the amplitude of the sample is the sine of the phase
        double[] audio = new double[nrOutputSamples];
        double phi = 0;
        for (int k = 0; k < nrOutputSamples; k++)
        {
            audio[k] = Math.sin(phi) * gain;

            // no modulo count 2 PI for increasing number of radians - phi runs up to 120818.5407310197 radians         
            phi = phi + dphi[k];
        }

        return audio;
    }   
}
