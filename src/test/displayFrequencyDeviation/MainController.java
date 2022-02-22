// Erik Icket, ON4PB - 2022
package test.displayFrequencyDeviation;

import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8;
import static common.Constants.SAMPLE_RATE;
import static gaussian.makeWaveform.GFSKPulse;
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

    private XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
    private Timeline timeline;

    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    void initialize()
    {

        byte[] symbols =
        {
            0, 2, 3, 2, 1
        };

        // int nrOutputSamples = symbols.length * NR_OF_SAMPLES_PER_SYMBOL_FT8;      
        // returns nrOutputSamples - 1
        // double[] fDeviation = calculateFrequencyDeviationWithGFSK(symbols, FT8_SYMBOL_BT, NR_OF_SAMPLES_PER_SYMBOL_FT8, SAMPLE_RATE);
        double[] fDeviation = calculateFrequencyDeviationWithoutGFSK(symbols, NR_OF_SAMPLES_PER_SYMBOL_FT8, SAMPLE_RATE);

        logger.info("Symbols created");

        lineChart.setCreateSymbols(true);
        //  for (int iSample = 0; iSample < fDeviation.length; iSample++)
        for (int i = 0; i < fDeviation.length; i++)
        {
            series.getData().add(new XYChart.Data<Number, Number>(i, fDeviation[i]));
            logger.fine("added x : " + i + ", fDeviation : " + fDeviation[i]);
        }
        lineChart.getData().add(series);

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

    // Synthesize the waveform and shape the phase with the GFSK smoothed pulse
    // returns an array of frquencies for the symbols entered
    public static double[] calculateFrequencyDeviationWithoutGFSK(byte[] symbols, int nrSamplesPerSymbol, int sampleRate)
    {
        int nrOutputSamples = symbols.length * nrSamplesPerSymbol;

        // modulation index is 1, single tone spacing
        float modulationIndex = 1.0f;

        // number of sinuses per symbol = NR_OF_SAMPLES_PER_SYMBOL * fBase / sampleRate
        //   logger.info("synthesizing : f0 : " + fBase + ", sinuses per symbol : " + nrSamplesPerSymbol * fBase / sampleRate + ", symbolBT : " + symbolBT + ", samples per symbol : " + nrSamplesPerSymbol);
        // delta phase advance in radians, for each symbol going up
        double dphiBetweenSymbols = 2 * Math.PI * modulationIndex / nrSamplesPerSymbol;

        logger.info("synthesizing : samples per symbol : " + nrSamplesPerSymbol + ", output samples  : " + nrOutputSamples + ", dphiBetweenSymbols : " + dphiBetweenSymbols);

        // array of delta phase advance in radians, between each sample
        double[] dphi = new double[nrOutputSamples];

        // set the delta phase advance for the base frequency
        // for 1500 Hz, this is 0,785
        /*
        for (int iSample = 0; iSample < nrOutputSamples + 2 * nrSamplesPerSymbol; iSample++)
        {
            dphi[iSample] = 2 * Math.PI * fBase / sampleRate;
        }
         */
        double runningPhi = 0;
        // loop for every symbol
        for (int iSymbol = 0; iSymbol < symbols.length; iSymbol++)
        {
            double phiIncrementForThisSymbol = dphiBetweenSymbols * symbols[iSymbol];
            // loop for every sample
            for (int iSample = 0; iSample < nrSamplesPerSymbol; iSample++)
            {
                runningPhi = runningPhi + phiIncrementForThisSymbol;
                dphi[iSymbol * nrSamplesPerSymbol + iSample] = runningPhi;
            }
        }

        // calculate the frequency deviation between samples - there will be one less than the number of samples
        double[] fDeviation = new double[nrOutputSamples - 1];
        //  double phi = 0;

        for (int k = 0; k < (nrOutputSamples - 1); k++)
        {
            //    fDeviation[k] = 2 * Math.PI / ((dphi[k + 1] - dphi[k]) * SAMPLE_RATE);
            fDeviation[k] = (SAMPLE_RATE * (dphi[k + 1] - dphi[k])) /( 2 * Math.PI);

            logger.fine("k : " + k + ", dphi[k] : " + dphi[k] + ", dphi[k+1] : " + dphi[k + 1] + ", delta phi : " + (dphi[k + 1] - dphi[k]) + ", fDeviation : " + fDeviation[k]);
        }

        return fDeviation;
    }

    // Synthesize the waveform and shape the phase with the GFSK smoothed pulse
    // returns an array of frquencies for the symbols entered
    public static double[] calculateFrequencyDeviationWithGFSK(byte[] symbols, float symbolBT, int nrSamplesPerSymbol, int sampleRate)
    {
        int nrSymbols = symbols.length;
        int nrOutputSamples = nrSymbols * nrSamplesPerSymbol;

        // modulation index is 1, single tone spacing
        float modulationIndex = 1.0f;

        // number of sinuses per symbol = NR_OF_SAMPLES_PER_SYMBOL * fBase / sampleRate
        //   logger.info("synthesizing : f0 : " + fBase + ", sinuses per symbol : " + nrSamplesPerSymbol * fBase / sampleRate + ", symbolBT : " + symbolBT + ", samples per symbol : " + nrSamplesPerSymbol);
        logger.info("synthesizing : symbolBT : " + symbolBT + ", samples per symbol : " + nrSamplesPerSymbol + ", output samples  : " + nrOutputSamples);

        // delta phase advance in radians, for each symbol going up
        double dphiBetweenSymbols = 2 * Math.PI * modulationIndex / nrSamplesPerSymbol;

        // array of delta phase advance in radians, between each sample
        // add two symbols for leading and trailing GFSK pulse symbol 
        double[] dphi = new double[nrOutputSamples + 2 * nrSamplesPerSymbol];

        // set the delta phase advance for the base frequency
        // for 1500 Hz, this is 0,785
        /*
        for (int iSample = 0; iSample < nrOutputSamples + 2 * nrSamplesPerSymbol; iSample++)
        {
            dphi[iSample] = 2 * Math.PI * fBase / sampleRate;
        }
         */
        double pulse[] = GFSKPulse(nrSamplesPerSymbol, symbolBT);

        // loop for every symbol
        for (int iSymbol = 0; iSymbol < nrSymbols; iSymbol++)
        {
            int iSample = iSymbol * nrSamplesPerSymbol;

            // loop for every sample of the pulse
            for (int iPulse = 0; iPulse < 3 * nrSamplesPerSymbol; iPulse++)
            {
                dphi[iPulse + iSample] += dphiBetweenSymbols * symbols[iSymbol] * pulse[iPulse];
            }
        }

        // calculate the frequency deviation between samples - there will be one less than the number of samples
        double[] fDeviation = new double[nrOutputSamples - 1];
        //  double phi = 0;

        //skip sample one
        for (int k = 1; k < (nrOutputSamples - 1); k++)
        {
            //    fDeviation[k] = 2 * Math.PI / ((dphi[k + 1] - dphi[k]) * SAMPLE_RATE);
            fDeviation[k] = SAMPLE_RATE / (dphi[k + 1] - dphi[k]);
            logger.info("k : " + k + ", delta phi : " + (dphi[k + 1] - dphi[k]) + ", fDeviation : " + fDeviation[k]);
        }

        return fDeviation;
    }

}
