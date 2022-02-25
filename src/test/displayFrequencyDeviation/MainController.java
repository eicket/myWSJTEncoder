// Erik Icket, ON4PB - 2022
package test.displayFrequencyDeviation;

import static common.Constants.FT8_SYMBOL_BT;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8;
import static common.Constants.SAMPLE_RATE;
import static dsp.MakeWaveform.GFSKPulse;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import javafx.util.Duration;

public class MainController
{

    static final Logger logger = Logger.getLogger(MainController.class.getName());

    private XYChart.Series<Number, Number> seriesWithoutGFSK = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> seriesWithGFSK = new XYChart.Series<Number, Number>();
    private Timeline timeline;

    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    void initialize()
    {

        byte[] symbols =
        {
            // for FT4, xaxis upper bound 0.5, yaxis upper bound 75
            // 0, 1, 3, 2, 1, 0, 3, 1, 3, 0
            
             // for FT8, xaxis upper bound 2, yaxis upper bound 50
            0, 1, 2, 2, 3, 4, 5, 7, 0, 6  
                
        };

        // double[] fDeviationWithoutGFSK = calculateFrequencyDeviationWithoutGFSK(symbols, NR_OF_SAMPLES_PER_SYMBOL_FT4, SAMPLE_RATE);
        // double[] fDeviationWithGFSK = calculateFrequencyDeviationWithGFSK(symbols, FT4_SYMBOL_BT, NR_OF_SAMPLES_PER_SYMBOL_FT4, SAMPLE_RATE);
        double[] fDeviationWithoutGFSK = calculateFrequencyDeviationWithoutGFSK(symbols, NR_OF_SAMPLES_PER_SYMBOL_FT8, SAMPLE_RATE);
        double[] fDeviationWithGFSK = calculateFrequencyDeviationWithGFSK(symbols, FT8_SYMBOL_BT, NR_OF_SAMPLES_PER_SYMBOL_FT8, SAMPLE_RATE);
        
        logger.info("Symbols created");

        for (int i = 0; i < fDeviationWithoutGFSK.length; i++)
        {
            seriesWithoutGFSK.getData().add(new XYChart.Data<Number, Number>(i / (float) SAMPLE_RATE, fDeviationWithoutGFSK[i]));
            logger.fine("added x : " + i + ", fDeviation : " + fDeviationWithoutGFSK[i]);
        }
        lineChart.getData().add(seriesWithoutGFSK);

        for (int i = 0; i < fDeviationWithGFSK.length; i++)
        {
            seriesWithGFSK.getData().add(new XYChart.Data<Number, Number>(i / (float) SAMPLE_RATE, fDeviationWithGFSK[i]));
            logger.fine("added x : " + i + ", fDeviation : " + fDeviationWithGFSK[i]);
        }
        lineChart.getData().add(seriesWithGFSK);

        timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                logger.fine("Animation event received");
            }
        }
        ));
        timeline.setCycleCount(Animation.INDEFINITE);

        timeline.play();
    }

    // Synthesize the waveform 
    // returns an array of frequency deviations for the symbols entered
    public static double[] calculateFrequencyDeviationWithoutGFSK(byte[] symbols, int nrSamplesPerSymbol, int sampleRate)
    {
        int nrOutputSamples = symbols.length * nrSamplesPerSymbol;

        // modulation index is 1, single tone spacing
        float modulationIndex = 1.0f;

        // delta phase advance in radians, for each gray code going up
        double dphiBetweenTones = 2 * Math.PI * modulationIndex / nrSamplesPerSymbol;

        logger.info("synthesizing : samples per symbol : " + nrSamplesPerSymbol + ", output samples  : " + nrOutputSamples + ", dphiBetweenTones : " + dphiBetweenTones);

        // array of instantanous phase -- in radians, one phase angle per sample        
        double[] dPhi = new double[nrOutputSamples];

        // calculate the delta phi from sample to sample
        // loop for every symbol
        for (int iSymbol = 0; iSymbol < symbols.length; iSymbol++)
        {
            // loop for every sample
            for (int iSample = 0; iSample < nrSamplesPerSymbol; iSample++)
            {
                dPhi[iSymbol * nrSamplesPerSymbol + iSample] = dphiBetweenTones * symbols[iSymbol];
            }
        }

        // calculate the running phi
        double runningPhi = 0;
        double[] phi = new double[nrOutputSamples];
        for (int k = 0; k < nrOutputSamples; k++)
        {
            runningPhi = runningPhi + dPhi[k];
            phi[k] = runningPhi;
        }

        // calculate the frequency deviation between samples - there is one deviation value less than the number of samples
        double[] fDeviation = new double[nrOutputSamples - 1];
        for (int k = 0; k < (nrOutputSamples - 1); k++)
        {
            fDeviation[k] = (SAMPLE_RATE * (phi[k + 1] - phi[k])) / (2 * Math.PI);
            logger.fine("k : " + k + ", phi[k] : " + phi[k] + ", phi[k+1] : " + phi[k + 1] + ", delta phi : " + (phi[k + 1] - phi[k]) + ", fDeviation : " + fDeviation[k]);
        }

        return fDeviation;
    }

    // Synthesize the waveform and shape the phase with the GFSK smoothed pulse
    // returns an array of frquency deviations for the symbols entered
    public static double[] calculateFrequencyDeviationWithGFSK(byte[] symbols, float symbolBT, int nrSamplesPerSymbol, int sampleRate)
    {
        int nrOutputSamples = symbols.length * nrSamplesPerSymbol;

        // modulation index is 1, single tone spacing
        float modulationIndex = 1.0f;

        // delta phase advance in radians, for each symbol going up
        double dphiBetweenTones = 2 * Math.PI * modulationIndex / nrSamplesPerSymbol;

        logger.info("synthesizing : samples per symbol : " + nrSamplesPerSymbol + ", output samples  : " + nrOutputSamples + ", dphiBetweenTones : " + dphiBetweenTones + ", symbolBT : " + symbolBT);

        // array of instantanous phase -- in radians, one phase angle per sample
        // add two symbols for leading and trailing GFSK pulse symbol 
        double[] dPhi = new double[nrOutputSamples + 2 * nrSamplesPerSymbol];
        double pulse[] = GFSKPulse(nrSamplesPerSymbol, symbolBT);

        // calculate the delta phi from sample to sample
        // loop for every symbol
        for (int iSymbol = 0; iSymbol < symbols.length; iSymbol++)
        {
            int iSample = iSymbol * nrSamplesPerSymbol;

            // loop for every sample of the pulse
            for (int iPulse = 0; iPulse < 3 * nrSamplesPerSymbol; iPulse++)
            {
                dPhi[iPulse + iSample] += dphiBetweenTones * symbols[iSymbol] * pulse[iPulse];
            }
        }

        // dPhi contains the delta Phi per sample
        // make a new array with a running Phi
        double runningPhi = 0;
        double[] phi = new double[nrOutputSamples + 2 * nrSamplesPerSymbol];
        for (int k = 0; k < (nrOutputSamples + 2 * nrSamplesPerSymbol); k++)
        {
            runningPhi = runningPhi + dPhi[k];
            phi[k] = runningPhi;
        }

        // calculate the frequency deviation between samples - there will be one less than the number of samples
        double[] fDeviation = new double[nrOutputSamples - 1];

        // skip sample one
        for (int k = 0; k < (nrOutputSamples - 1); k++)
        {
            fDeviation[k] = (SAMPLE_RATE * (phi[k + nrSamplesPerSymbol + 1] - phi[k + nrSamplesPerSymbol])) / (2 * Math.PI);
            logger.fine("k : " + k + ", phi[k + nrSamplesPerSymbol] : " + phi[k + nrSamplesPerSymbol] + ", phi[k + nrSamplesPerSymbol + 1] : " + phi[k + nrSamplesPerSymbol + 1] + ", fDeviation : " + fDeviation[k]);
        }

        return fDeviation;
    }
}
