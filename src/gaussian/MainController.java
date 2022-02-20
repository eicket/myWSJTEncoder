// Erik Icket, ON4PB - 2022
package gaussian;

import common.Constants;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8;
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
import static test.gaussian.ErrorFunction.erf;

public class MainController
{

    static final Logger logger = Logger.getLogger(MainController.class.getName());

    public static final float FT4_SYMBOL_BT = 1.0f; ///< symbol smoothing filter bandwidth factor (BT)
    public static final float FT8_SYMBOL_BT = 2.0f; ///< symbol smoothing filter bandwidth factor (BT)
    public static final float OTHER_SYMBOL_BT = 99.0f; ///< symbol smoothing filter bandwidth factor (BT) 

    public static final float GFSK_CONST_K = 5.336446f; ///< == pi * sqrt(2 / log(2))

    double[] pulse = new double[3 * NR_OF_SAMPLES_PER_SYMBOL_FT8];

    private XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
    private Timeline timeline;

    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    void initialize()
    {
        logger.info("Go main");

        pulse = my_gfsk_pulse(Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8, FT8_SYMBOL_BT);

        lineChart.setCreateSymbols(true);
        for (int i = 0; i < pulse.length; i++)
        {
            series.getData().add(new XYChart.Data<Number, Number>((i / (double) (NR_OF_SAMPLES_PER_SYMBOL_FT8)) - 1.5f, pulse[i]));
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

    /// Computes a GFSK smoothing pulse.
    /// The pulse is theoretically infinitely long, however, here it's truncated at 3 times the symbol length.
    /// This means the pulse array has to have space for 3*nrSamplesPerSymbol elements.
    /// @param[in] nrSamplesPerSymbol Number of samples per symbol
    /// @param[in] b Shape parameter (values defined for FT8/FT4)
    /// @param[out] pulse Output array of pulse samples
    ///
    private double[] gfsk_pulse(int nrSamplesPerSymbol, float symbol_bt)
    {
        double[] pulse = new double[3 * nrSamplesPerSymbol];

        for (int i = 0; i < 3 * nrSamplesPerSymbol; i++)
        {
            // t values are from -1.5 to 1.5
            float t = i / (float) nrSamplesPerSymbol - 1.5f;
            float arg1 = GFSK_CONST_K * symbol_bt * (t + 0.5f);
            float arg2 = GFSK_CONST_K * symbol_bt * (t - 0.5f);
            pulse[i] = (erf(arg1) - erf(arg2)) / 2;
        }

        return pulse;
    }

    /// Computes a GFSK smoothing pulse.
    /// The pulse is theoretically infinitely long, however, here it's truncated at 3 times the symbol length.
    /// This means the pulse array has to have space for 3*nrSamplesPerSymbol elements.
    /// @param[in] nrSamplesPerSymbol Number of samples per symbol
    /// @param[in] b Shape parameter (values defined for FT8/FT4)
    /// @param[out] pulse Output array of pulse samples
    ///
    private double[] my_gfsk_pulse(int nrSamplesPerSymbol, float symbol_bt)
    {

        double[] pulse = new double[3 * nrSamplesPerSymbol];

        for (int i = 0; i < 3 * nrSamplesPerSymbol; i++)
        {
            // t values are from -1.5 to 1.5, one unit of t is a single sample
            float t = i / (float) nrSamplesPerSymbol - 1.5f;

            float arg1 = GFSK_CONST_K * symbol_bt * (t + 0.5f);
            float arg2 = GFSK_CONST_K * symbol_bt * (t - 0.5f);
            pulse[i] = (erf(arg1) - erf(arg2)) / 2;
        }

        return pulse;
    }
}
