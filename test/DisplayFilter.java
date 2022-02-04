package test;

import fft.Complex;
import filter.BandPassFilter;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class DisplayFilter extends Application
{
    private final float sampleRate = 48000;
    private final int fftSize = 8192;
    private final int filterFrequency = -5000;
    private final int filterWidth = 2700;
    private float binSize = sampleRate / fftSize;
    private double[] filter = new double[fftSize + 1];
    Complex[] fftIn = new Complex[fftSize];
    Complex[] fftOut = new Complex[fftSize];    

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Group root = new Group();

        Scene scene = new Scene(root, 1100, 850);
        scene.getStylesheets().add("test/Chart.css");
        primaryStage.setScene(scene);

        // the sinc function chart
        NumberAxis x1Axis = new NumberAxis("Frequency", 0, fftSize, 100);
        x1Axis.autosize();
        x1Axis.setLowerBound(3496); // 4096 - 600
        x1Axis.setUpperBound(4696); // 4096 + 600
        NumberAxis y1Axis = new NumberAxis();
        y1Axis.autosize();
        y1Axis.setPrefWidth(60);
        LineChart<Number, Number> sincChart = new LineChart<Number, Number>(x1Axis, y1Axis);
        sincChart.setPrefSize(1000, 400);
        // do not create symbols for the points !!
        sincChart.setCreateSymbols(false);
        System.out.println("0 is : " + x1Axis.getDisplayPosition(0) + ", " + y1Axis.getDisplayPosition(0));

        XYChart.Series<Number, Number> sincPoints = new XYChart.Series<Number, Number>();
        BandPassFilter.CalculateFilter(filter, filterFrequency, filterWidth, sampleRate);
                 
        for (int i = 0; i < filter.length; i++)
        {
            sincPoints.getData().add(new XYChart.Data<Number, Number>(i, filter[i]));
        }
        sincPoints.setName("sinc function");
        sincChart.getData().add(sincPoints);

        // the fir filter chart
        NumberAxis x2Axis = new NumberAxis("Frequency", 0, sampleRate, 1000);
        x2Axis.autosize();
        //x2Axis.setUpperBound(3000);
        x2Axis.setUpperBound(50000);
        NumberAxis y2Axis = new NumberAxis();
        y2Axis.autosize();
        y2Axis.setPrefWidth(60);
        LineChart<Number, Number> filterChart = new LineChart<Number, Number>(x2Axis, y2Axis);
        filterChart.setPrefSize(1000, 400);
        // do not create symbols for the points !!
        filterChart.setCreateSymbols(false);
        System.out.println("0 is : " + x2Axis.getDisplayPosition(0) + ", " + y2Axis.getDisplayPosition(0));
        for (int i = 0; i < fftSize; i++)
        {
            fftIn[i] = new Complex(filter[i], filter[i]);
        }
        fftOut = fft.FFT.fft(fftIn);

        XYChart.Series<Number, Number> firPoints = new XYChart.Series<Number, Number>();
        for (int i = 0; i < fftSize; i++)
        {
            firPoints.getData().add(new XYChart.Data<Number, Number>(i * binSize, fftOut[i].abs()));
        }
        firPoints.setName("fir filter");
        filterChart.getData().add(firPoints);
        filterChart.relocate(200, 250);

        // add buttons and label to grid and set their positions
        GridPane.setConstraints(sincChart, 0, 0);
        GridPane.setConstraints(filterChart, 0, 1);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        // grid.setLayoutX(1000);
        // grid.setLayoutY(700);
        root.getChildren().add(grid);
        grid.getChildren().addAll(sincChart, filterChart);

        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
