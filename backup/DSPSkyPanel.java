package dsp;

import audio.Audio;
import audio.AudioOutThread;
import common.PropertiesWrapper;
import fft.Complex;
import static fft.FFT.fft;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DSPSkyPanel extends Application
{

    static final Logger logger = Logger.getLogger(dsp.DSPSkyPanel.class.getName());

    XYChart.Series<Number, Number> timeDomainSeries = new XYChart.Series<Number, Number>();
    NumberAxis timeDomainXAxis;
    NumberAxis timeDomainYAxis;
    LineChart<Number, Number> timeDomainChart;

    XYChart.Series<Number, Number> frequencyDomainSeries = new XYChart.Series<Number, Number>();
    NumberAxis frequencyDomainXAxis;
    NumberAxis frequencyDomainYAxis;
    LineChart<Number, Number> frequencyDomainChart;

    double gain = 0;
    int fScroll = 1;
    int fSelected = 1500;
    int sampleRate = 48000;
    int nrOfSamplesPerSymbol = 32768;
    double dBase = 0;
    double[] dBufferOut = new double[162 * nrOfSamplesPerSymbol];
    DSPUtils dspUtils = new DSPUtils();

    // AudioInThread audioInThread;
    AudioOutThread audioOutThread;

    Group root;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // load the properties
        PropertiesWrapper propWrapper = new PropertiesWrapper();
        Audio audio = new Audio();

        //      yAxisHigh = propWrapper.getIntProperty("YAxisHigh");
        root = new Group();
        //ScenicView.show(root);
        ////ScenicView.show(scene);    

        Scene scene = new Scene(root, 1250, 850, Color.LINEN);
        scene.getStylesheets().add("dsp/Chart.css");
        primaryStage.setScene(scene);

        //  xAxis = new NumberAxis(0, 100, 10);
        // 100 / sampleRate for axis in secs
        // 100000 / sampleRate for axis in msecs
        // xAxis = new NumberAxis(0, (float)(100000) / sampleRate, (float)(1000) / sampleRate);
        timeDomainXAxis = new NumberAxis("ms", 0, 2, 0.05);
        timeDomainXAxis.setPrefWidth(1024);
        timeDomainYAxis = new NumberAxis(-100, 100, 10); // tickUnit is 10
        // spent hours in this one : use 60 to prevent jumping the xaxis to the left
        timeDomainYAxis.setPrefWidth(60);

        timeDomainChart = new LineChart<Number, Number>(timeDomainXAxis, timeDomainYAxis);
        // do not change width, otherwise waterfall will not end at end of xAxis
        timeDomainChart.setPrefSize(1000, 350);
        // remove the legend so that the waterfall comes closer to the scatterchart
        timeDomainChart.setLegendVisible(false);
        // do not create symbols for the points !!
        timeDomainChart.setCreateSymbols(true);
        timeDomainChart.getData().add(timeDomainSeries);
        root.getChildren().add(timeDomainChart);

        frequencyDomainXAxis = new NumberAxis("frequency", 0, nrOfSamplesPerSymbol / 2, 500);
        frequencyDomainXAxis.setPrefWidth(1024);
        frequencyDomainYAxis = new NumberAxis(0, 100, 10);
        frequencyDomainYAxis.setAutoRanging(true);
        // spent hours in this one : use 60 to prevent jumping the xaxis to the left
        frequencyDomainYAxis.setPrefWidth(60);

        frequencyDomainChart = new LineChart<Number, Number>(frequencyDomainXAxis, frequencyDomainYAxis);
        // do not change width, otherwise waterfall will not end at end of xAxis
        frequencyDomainChart.setPrefSize(1000, 400);
        frequencyDomainChart.setLayoutY(330);
        // remove the legend so that the waterfall comes closer to the scatterchart
        frequencyDomainChart.setLegendVisible(false);
        // do not create symbols for the points !!
        frequencyDomainChart.setCreateSymbols(true);
        frequencyDomainChart.getData().add(frequencyDomainSeries);
        root.getChildren().add(frequencyDomainChart);

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton offButton = new RadioButton("Off");
        offButton.setToggleGroup(modeGroup);
        offButton.setPrefWidth(90);
        offButton.setSelected(true);
        RadioButton toneButton = new RadioButton("Tone");
        toneButton.setToggleGroup(modeGroup);
        toneButton.setPrefWidth(90);
        RadioButton wsprButton = new RadioButton("WSPR");
        wsprButton.setToggleGroup(modeGroup);
        wsprButton.setPrefWidth(90);

        AnchorPane anchor1Pane = new AnchorPane();
        anchor1Pane.setLayoutX(1050);
        anchor1Pane.setLayoutY(30);
        anchor1Pane.setPrefSize(250, 250);
        anchor1Pane.getChildren().addAll(offButton, toneButton, wsprButton);

        AnchorPane.setTopAnchor(offButton, Double.valueOf(0));
        AnchorPane.setLeftAnchor(offButton, Double.valueOf(0));
        AnchorPane.setTopAnchor(toneButton, Double.valueOf(25));
        AnchorPane.setLeftAnchor(toneButton, Double.valueOf(0));
        AnchorPane.setTopAnchor(wsprButton, Double.valueOf(50));
        AnchorPane.setLeftAnchor(wsprButton, Double.valueOf(0));

        // the RX Audio In choice box    
        ChoiceBox audioInBox = new ChoiceBox();
        ChoiceBox audioOutBox = new ChoiceBox();

        audio.ListAudioIn(audioInBox);
        audio.ListAudioOut(audioOutBox);

        // the sample rate choice box
        ChoiceBox sampleRateBox = new ChoiceBox();
        sampleRateBox.getItems().addAll("12000", "48000");
        sampleRateBox.getSelectionModel().select("48000");

        // the fft size choice box
        ChoiceBox symbolBox = new ChoiceBox();
        symbolBox.getItems().addAll("8192", "32768");
        symbolBox.getSelectionModel().select("32768");
        symbolBox.setDisable(true);

        // the start/stop button
        final ToggleButton stopStart = new ToggleButton("OFF");

        dBase = dspUtils.baseFreq(fSelected, nrOfSamplesPerSymbol, sampleRate);
        final Label baseFrequency = new Label("base f : " + Double.toString(dBase));
        //      dspUtils.makeContiguousSinusBufferOut(dBase, symbolSize, sampleRate);

        // the frequency field
        final TextField fBaseField = new TextField();
        fBaseField.setText(Integer.toString(fSelected));
        fBaseField.setStyle("-fx-background-color: slateblue; -fx-text-fill: white;");
        fBaseField.setPrefWidth(80);

        // the gain slider
        final Slider gainSlider = new Slider();
        // amplification of 1 is 0 dB
        // gain in db = 20 * log10 gain (linear)
        // gain (linear) = 10 * (gain in dB / 20)
        gainSlider.setMin(0);
        gainSlider.setMax(90);
        gainSlider.setValue(50);
        gainSlider.setShowTickLabels(true);
        gainSlider.setShowTickMarks(true);
        gainSlider.setMajorTickUnit(2.5);
        gainSlider.setMinorTickCount(1);
        gainSlider.setBlockIncrement(1);
        gainSlider.setSnapToTicks(true);
        gainSlider.setPrefWidth(500);
        gainSlider.setTooltip(new Tooltip(Double.toString(gainSlider.getValue())));
        gain = Math.pow(10, gainSlider.getValue() / 20);

        // an anchorpane to put all controls in
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setLayoutX(0);
        anchorPane.setLayoutY(700);
        anchorPane.setPrefSize(1250, 150);
        anchorPane.setStyle("-fx-background-color: NavajoWhite;");

        anchorPane.getChildren().addAll(audioInBox, audioOutBox, sampleRateBox, symbolBox, stopStart, baseFrequency, fBaseField, gainSlider);

        AnchorPane.setTopAnchor(audioInBox, Double.valueOf(15));
        AnchorPane.setLeftAnchor(audioInBox, Double.valueOf(70));
        AnchorPane.setTopAnchor(audioOutBox, Double.valueOf(55));
        AnchorPane.setLeftAnchor(audioOutBox, Double.valueOf(70));
        AnchorPane.setTopAnchor(sampleRateBox, Double.valueOf(95));
        AnchorPane.setLeftAnchor(sampleRateBox, Double.valueOf(70));
        AnchorPane.setTopAnchor(symbolBox, Double.valueOf(95));
        AnchorPane.setLeftAnchor(symbolBox, Double.valueOf(160));

        AnchorPane.setTopAnchor(stopStart, Double.valueOf(15));
        AnchorPane.setLeftAnchor(stopStart, Double.valueOf(450));
        AnchorPane.setTopAnchor(baseFrequency, Double.valueOf(55));
        AnchorPane.setLeftAnchor(baseFrequency, Double.valueOf(450));

        AnchorPane.setTopAnchor(fBaseField, Double.valueOf(55));
        AnchorPane.setLeftAnchor(fBaseField, Double.valueOf(670));

        AnchorPane.setTopAnchor(gainSlider, Double.valueOf(95));
        AnchorPane.setLeftAnchor(gainSlider, Double.valueOf(670));

        root.getChildren().add(anchorPane);
        root.getChildren().add(anchor1Pane);

        int absBound = (int) Math.pow(10, gainSlider.getValue() / 20);
        timeDomainYAxis.setLowerBound(-absBound);
        timeDomainYAxis.setUpperBound(absBound);

        AnimationTimer timer = new AnimationTimer()
        {
            @Override
            public void handle(long l)
            {
                logger.fine("Animation event received");
                /*
                timeDomainChart.setAnimated(false);
                timeDomainChart.setAnimated(true);

                frequencyDomainChart.setAnimated(false);
                frequencyDomainChart.setAnimated(true);
                 */
                if ((audioOutThread == null) || !audioOutThread.isAlive())
                {
                    logger.fine("Resetting buttons");
                    offButton.setSelected(true);
                }
            }
        };

        timeDomainYAxis.setOnScroll(new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event)
            {
                if (event.getDeltaY() > 0)
                {
                    timeDomainYAxis.setLowerBound(2 * timeDomainYAxis.getLowerBound());
                    timeDomainYAxis.setUpperBound(2 * timeDomainYAxis.getUpperBound());
                }
                else
                {
                    timeDomainYAxis.setLowerBound(timeDomainYAxis.getLowerBound() / 2);
                    timeDomainYAxis.setUpperBound(timeDomainYAxis.getUpperBound() / 2);
                }
                timeDomainYAxis.setTickUnit(timeDomainYAxis.getUpperBound() / 10);
                timeDomainYAxis.setMinorTickCount(2);

                logger.fine("timeDomainYAxis upper bound : " + timeDomainYAxis.getUpperBound());

            }
        });

        frequencyDomainXAxis.setOnScroll(new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event)
            {

                if (event.getDeltaY() > 0)
                {

                    frequencyDomainXAxis.setUpperBound(2 * frequencyDomainXAxis.getUpperBound());
                }
                else
                {

                    frequencyDomainXAxis.setUpperBound(frequencyDomainXAxis.getUpperBound() / 2);
                }
                frequencyDomainChart.requestLayout();

                logger.info("frequencyDomainXAxis upper bound : " + frequencyDomainYAxis.getUpperBound());
            }
        });

        frequencyDomainYAxis.setOnScroll(new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event)
            {
                frequencyDomainYAxis.setAutoRanging(false);
                if (event.getDeltaY() > 0)
                {
                    frequencyDomainYAxis.setLowerBound(2 * frequencyDomainYAxis.getLowerBound());
                    frequencyDomainYAxis.setUpperBound(2 * frequencyDomainYAxis.getUpperBound());
                }
                else
                {
                    frequencyDomainYAxis.setLowerBound(frequencyDomainYAxis.getLowerBound() / 2);
                    frequencyDomainYAxis.setUpperBound(frequencyDomainYAxis.getUpperBound() / 2);
                }
                frequencyDomainChart.requestLayout();

                logger.info("frequencyDomainYAxis upper bound : " + frequencyDomainYAxis.getUpperBound());
            }
        });

        modeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
        {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle selectedToggle)
            {
                RadioButton tb = (RadioButton) selectedToggle;
                String s = tb.getText();

                logger.info("mode toggle : " + s);

                switch (s)
                {
                    case "Off":
                        if ((audioOutThread != null) || audioOutThread.isAlive())
                        {
                            audioOutThread.stopRequest = true;

                            try
                            {
                                audioOutThread.join();
                            }
                            catch (InterruptedException ex)
                            {
                                logger.fine("Exception when closing audioOut thread");
                            }
                            logger.info("AudioOut thread stopped");
                        }
                        else
                        {
                            logger.info("AudioOut thread already stopped");
                        }
                        break;

                    case "Tone":
                        dspUtils.makeSymbols(fSelected, nrOfSamplesPerSymbol, sampleRate, gain);
                        dBufferOut = dspUtils.makeBufferOut(dspUtils.f0(20), nrOfSamplesPerSymbol);

                        // set the yAxis to the gain each time we send the audio
                        int absBound = (int) Math.pow(10, gainSlider.getValue() / 20);
                        timeDomainYAxis.setLowerBound(-absBound);
                        timeDomainYAxis.setUpperBound(absBound);
                        timeDomainYAxis.setTickUnit(timeDomainYAxis.getUpperBound() / 10);
                        timeDomainYAxis.setMinorTickCount(2);

                        timeDomainSeries.getData().clear();
                        for (int i = 0; i < 100; i++)
                        {
                            timeDomainSeries.getData().add(new XYChart.Data<Number, Number>((float) (i * 1000) / sampleRate, dBufferOut[i]));
                        }

                        Complex[] complexIn = new Complex[nrOfSamplesPerSymbol];
                        Complex[] complexOut = new Complex[nrOfSamplesPerSymbol];
                        for (int i = 0; i < nrOfSamplesPerSymbol; i++)
                        {
                            complexIn[i] = new Complex(dBufferOut[i], 0);
                        }
                        complexOut = fft(complexIn);

                        // normalize from 0 .. 1
                        double[] ampl = new double[nrOfSamplesPerSymbol / 2];
                        for (int i = 0; i < nrOfSamplesPerSymbol / 2; i++)
                        {
                            // abs is  Math.sqrt(re*re + im*im)
                            ampl[i] = complexOut[i].abs();
                        }
                        // find the highest value
                        double highest = 0;
                        for (int i = 0; i < ampl.length; i++)
                        {
                            if (ampl[i] > highest)
                            {
                                highest = ampl[i];
                            }
                        }
                        logger.info("Highest dsp bin values is : " + highest);
                        // normalize from 0 .. 1
                        for (int i = 0; i < ampl.length; i++)
                        {
                            ampl[i] = ampl[i] / highest;
                        }

                        frequencyDomainXAxis.setUpperBound(nrOfSamplesPerSymbol / 2);
                        frequencyDomainSeries.getData().clear();
                        for (int i = 0; i < ampl.length; i++)
                        {
                            frequencyDomainSeries.getData().add(new XYChart.Data<Number, Number>(i * sampleRate / nrOfSamplesPerSymbol, 10 * Math.log(ampl[i])));
                        }
                                              
                        audio.OpenAudioOut(sampleRate);
                        audioOutThread = new AudioOutThread(audio.sourceDataLine, dBufferOut, 0);
                        audioOutThread.start();

                        break;

                    case "WSPR":
                        dspUtils.makeSymbols(fSelected, nrOfSamplesPerSymbol, sampleRate, gain);
                        dBufferOut = dspUtils.makeBufferOut(dspUtils.test(), nrOfSamplesPerSymbol);

                        long now = System.currentTimeMillis();
                        // wait one sec into the next 2 mins
                        long wait = 120000 - now % 120000 + 1000;
                        long startAt = now + wait;
                        logger.fine("Wait : " + wait);

                        timeDomainChart.setAnimated(false);
                        timeDomainSeries.getData().clear();
                        timeDomainYAxis.setLowerBound(-Math.pow(10, gainSlider.getValue() / 20));
                        timeDomainYAxis.setUpperBound(Math.pow(10, gainSlider.getValue() / 20));

                        //        for (int i = 0; i < symbolSize; i++)
                        for (int i = 0; i < 100; i++)
                        {
                            timeDomainSeries.getData().add(new XYChart.Data<Number, Number>((float) (i * 1000) / sampleRate, dBufferOut[i]));
                        }
                        timeDomainChart.setAnimated(true);

                        audio.OpenAudioOut(sampleRate);
                        audioOutThread = new AudioOutThread(audio.sourceDataLine, dBufferOut, startAt);
                        audioOutThread.start();

                        break;
                }
            }
        });

        audioInBox.getSelectionModel()
                .selectedItemProperty().addListener(new ChangeListener()
                {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue,
                             Object newValue
                    )
                    {
                        propWrapper.setProperty("ReceivedAudioIn", newValue.toString());
                    }
                }
                );

        audioOutBox.getSelectionModel()
                .selectedItemProperty().addListener(new ChangeListener()
                {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue,
                             Object newValue
                    )
                    {
                        propWrapper.setProperty("ReceivedAudioOut", newValue.toString());
                    }
                }
                );

        sampleRateBox.getSelectionModel()
                .selectedItemProperty().addListener(new ChangeListener()
                {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue,
                             Object newValue
                    )
                    {
                        try
                        {
                            sampleRate = Integer.parseInt(newValue.toString());
                            switch (sampleRate)
                            {
                                case 12000:
                                    nrOfSamplesPerSymbol = 8192;
                                    symbolBox.getSelectionModel().select("8192");
                                    break;

                                case 48000:
                                    nrOfSamplesPerSymbol = 32768;
                                    symbolBox.getSelectionModel().select("32768");
                                    break;
                            }
                        }
                        catch (NumberFormatException e)
                        {
                            logger.severe("Unable to parse sample rate");
                            return;
                        }
                    }
                }
                );

        fBaseField.setOnAction(
                new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e1
            )
            {
                try
                {
                    fSelected = Integer.parseInt(fBaseField.getText());
                }
                catch (NumberFormatException e2)
                {
                    logger.info("Cannot parse fBase field");
                    return;
                }

                logger.fine("fSelected set to : " + fSelected);

                dBase = dspUtils.baseFreq(fSelected, nrOfSamplesPerSymbol, sampleRate);
                baseFrequency.setText("base f : " + Double.toString(dBase));
                //       dspUtils.makeContiguousSinusBufferOut(dBase, symbolSize, sampleRate,gain);

            }
        }
        );

        fBaseField.setOnScroll(
                new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event
            )
            {
                logger.fine("fBase scroll : " + event.getDeltaY());
                if (event.getDeltaY() > 0)
                {
                    fSelected = fSelected + fScroll;
                }
                else
                {
                    fSelected = fSelected - fScroll;
                }
                fBaseField.setText(Integer.toString(fSelected));

                logger.info("fSelected set to : " + fSelected);

                dBase = dspUtils.baseFreq(fSelected, nrOfSamplesPerSymbol, sampleRate);
                baseFrequency.setText("base f : " + Double.toString(dBase));
                //        dspUtils.makeContiguousSinusBufferOut(dBase, symbolSize, sampleRate);
            }
        }
        );

        stopStart.setOnMousePressed(
                new EventHandler<MouseEvent>()
        {

            public void handle(MouseEvent me)
            {
                stopStart.setText("ON");

            }
        }
        );

        gainSlider.setOnMouseReleased(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent me)
            {
                gain = Math.pow(10, gainSlider.getValue() / 20);
                gainSlider.setTooltip(new Tooltip(Double.toString(gainSlider.getValue())));
                logger.info("slider gain in db : " + gainSlider.getValue() + ", gain : " + gain);
            }
        });

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent arg0)
            {
                try
                {
                    logger.info("Closing the primary stage");
                    stopThreads();
                }
                catch (Exception ex)
                {
                    logger.severe("Unable to close the primary stage");
                    return;
                }

            }
        });

        primaryStage.show();
        timer.start();
    }

    public void stopThreads()
    {
        if (audioOutThread.isAlive())
        {
            //  audioOutThread.stopRequest = true;
            while (audioOutThread.isAlive())
            {
                try
                {
                    audioOutThread.join();
                }
                catch (InterruptedException ex)
                {
                    logger.fine("Exception when closing audioOutthread");
                }
            }
        }
        else
        {
            logger.fine("AudioOut thread already stopped");
        }
    }

    public static void main(String[] args)
    {
        Application.launch(args);
    }
}
