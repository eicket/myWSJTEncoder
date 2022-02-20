 package test.gaussian;

import static test.gaussian.ErrorFunction.erf;

public class synth_gfsk
{

 public static final float FT8_SYMBOL_BT = 2.0f; ///< symbol smoothing filter bandwidth factor (BT)
 public static final float FT4_SYMBOL_BT = 1.0f; ///< symbol smoothing filter bandwidth factor (BT)
 public static final float GFSK_CONST_K = 5.336446f; ///< == pi * sqrt(2 / log(2))

/// Computes a GFSK smoothing pulse.
/// The pulse is theoretically infinitely long, however, here it's truncated at 3 times the symbol length.
/// This means the pulse array has to have space for 3*nrSamplesPerSymbol elements.
/// @param[in] nrSamplesPerSymbol Number of samples per symbol
/// @param[in] b Shape parameter (values defined for FT8/FT4)
/// @param[out] pulse Output array of pulse samples
///
private double[] gfsk_pulse(int nrSamplesPerSymbol, float symbol_bt)
{
    double [] pulse = new double[3 * nrSamplesPerSymbol];
            
    for (int i = 0; i < 3 * nrSamplesPerSymbol; ++i)
    {
        // t values are from -1.5 to 1.5
        float t = i / (float)nrSamplesPerSymbol - 1.5f;
        float arg1 = GFSK_CONST_K * symbol_bt * (t + 0.5f);
        float arg2 = GFSK_CONST_K * symbol_bt * (t - 0.5f);
        pulse[i] = (erf(arg1) - erf(arg2)) / 2;
    }
    
    return pulse;
}
}


/*



}


    float pulse[3 * n_spsym];
    gfsk_pulse(n_spsym, symbol_bt, pulse);

    for (int i = 0; i < n_sym; ++i)
    {
        int ib = i * n_spsym;
        for (int j = 0; j < 3 * n_spsym; ++j)
        {
            dphi[j + ib] += dphi_peak * symbols[i] * pulse[j];
        }
    }
}

*/