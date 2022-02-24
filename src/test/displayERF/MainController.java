// Erik Icket, ON4PB - 2022
package test.displayERF;

import common.Constants;
import static common.Constants.FT4_SYMBOL_BT;
import static common.Constants.FT8_SYMBOL_BT;
import static common.Constants.NO_SMOOTH_SYMBOL_BT;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT4;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8;
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

    double[] ft4Pulse = new double[3 * NR_OF_SAMPLES_PER_SYMBOL_FT4];
    double[] ft8Pulse = new double[3 * NR_OF_SAMPLES_PER_SYMBOL_FT8];
    double[] squarePulse = new double[3 * NR_OF_SAMPLES_PER_SYMBOL_FT8];

    private XYChart.Series<Number, Number> ft4Series = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> ft8Series = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> squareSeries = new XYChart.Series<Number, Number>();
    private Timeline timeline;

    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    void initialize()
    {
        ft4Pulse = GFSKPulse(Constants.NR_OF_SAMPLES_PER_SYMBOL_FT4, FT4_SYMBOL_BT);
        ft8Pulse = GFSKPulse(Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8, FT8_SYMBOL_BT);
        squarePulse = GFSKPulse(Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8, NO_SMOOTH_SYMBOL_BT);

        for (int i = 0; i < ft4Pulse.length; i++)
        {
            ft4Series.getData().add(new XYChart.Data<Number, Number>((i / (double) (NR_OF_SAMPLES_PER_SYMBOL_FT4)) - 1.5f, ft4Pulse[i]));
        }
        ft4Series.setName("BT=1 (FT4)");
        lineChart.getData().add(ft4Series);

        for (int i = 0; i < ft8Pulse.length; i++)
        {
            ft8Series.getData().add(new XYChart.Data<Number, Number>((i / (double) (NR_OF_SAMPLES_PER_SYMBOL_FT8)) - 1.5f, ft8Pulse[i]));
            squareSeries.getData().add(new XYChart.Data<Number, Number>((i / (double) (NR_OF_SAMPLES_PER_SYMBOL_FT8)) - 1.5f, squarePulse[i]));
        }
        ft8Series.setName("BT=2 (FT8)");
        lineChart.getData().add(ft8Series);
        squareSeries.setName("BT=99");
        lineChart.getData().add(squareSeries);

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
}
