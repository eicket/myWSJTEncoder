package dsp;

import audio.Audio;
import audio.AudioInThread;
import audio.AudioOutThread;
import common.PropertiesWrapper;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
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

public class SkyPanel extends Application
{

    // general
    static final Logger logger = Logger.getLogger(dsp.SkyPanel.class.getName());
    PropertiesWrapper propWrapper;

    // for the LineChart   
    LineChart<Number, Number> audioChart;
    NumberAxis xAxis;
    NumberAxis yAxis;
    XYChart.Series<Number, Number> spectrumPoints = new XYChart.Series<Number, Number>();
    XYChart.Series<Number, Number> waterfallLowPoints = new XYChart.Series<Number, Number>();
    XYChart.Series<Number, Number> waterfallHighPoints = new XYChart.Series<Number, Number>();
    XYChart.Series<Number, Number> filterFrequencyPoints = new XYChart.Series<Number, Number>();
    XYChart.Series<Number, Number> filterEdgePoints = new XYChart.Series<Number, Number>();
    final int SPECTRUM_YAXIS_LO = 0;
    final int NR_OF_SPECTRUM_POINTS = 1024;
    final int INITIAL_YAXIS_HIGH = 1000000; //50000
    // the waterfall   
    int waterfallLow;
    int waterfallHigh;
    final int NR_OF_WATERFALL_LINES = 300;
    LinkedList waterfallList = new LinkedList();
    // the Y Axis upper bound slider
    Slider yAxisUpperBoundSlider;
    int yAxisHigh = INITIAL_YAXIS_HIGH;
    // the auto Level
    LinkedList fftAverageLevels = new LinkedList();
    final int NR_OF_AVERAGES = 100;

    RadioButton offButton;
    RadioButton toneButton;
    RadioButton wsprButton;

    // the zoom
    int zoom = 1;
    int fScroll = 1;

    int fSelected = 1500;
    int sampleRate = 12000;
    int symbolSize = 8192;
    double dBase = 0;
    DSPUtils dspUtils = new DSPUtils();

    // for the audio threads
    AudioInThread audioInThread;
    AudioOutThread audioOutThread;
    // the gain
    double gain = 0;
    //   final float INITIAL_GAIN = 0.5F;
    // the IQ swap state
    final int STRAIGHT = 0;
    final int SWAPPED = 1;
    int[] swapState = new int[1];
    // the auto state
    boolean autoState = true;
    // demodulation
    final int LSB = 0;
    final int USB = 1;
    final int AM = 2;
    final int IPlusQ = 3;
    final int IMinusQ = 4;
    int[] demodulation = new int[1];
    int[] filterFrequency = new int[1];
    int[] filterWidth = new int[1];
    int[] receivedBasebandFFT = new int[symbolSize * 2];
    LinkedBlockingDeque lbdReceivedBasebandFFT = new LinkedBlockingDeque();
    LinkedBlockingDeque lbdReceivedAudioToSoundcard = new LinkedBlockingDeque();
    // the root of javafx
    Group root;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // load the properties
        propWrapper = new PropertiesWrapper();
        Audio audio = new Audio();

        yAxisHigh = propWrapper.getIntProperty("YAxisHigh");
        waterfallLow = propWrapper.getIntProperty("waterfallLow");
        waterfallHigh = propWrapper.getIntProperty("waterfallHigh");

        root = new Group();
        //ScenicView.show(root);
        ////ScenicView.show(scene);    

        Scene scene = new Scene(root, 1250, 850, Color.LINEN);
        scene.getStylesheets().add("dsp/Chart.css");
        primaryStage.setScene(scene);

        xAxis = new NumberAxis(null, -sampleRate / 2, sampleRate / 2, 1000);
        xAxis.setPrefWidth(1024);
        yAxis = new NumberAxis("Audio level", 0, yAxisHigh, yAxisHigh / 10);
        // spent hours in this one : use 60 to prevent jumping the xaxis to the left
        yAxis.setPrefWidth(60);

        audioChart = new LineChart<Number, Number>(xAxis, yAxis);
        // do not change width, otherwise waterfall will not end at end of xAxis
        audioChart.setPrefSize(1000, 400);
        // remove the legend so that the waterfall comes closer to the scatterchart
        audioChart.setLegendVisible(false);
        // do not create symbols for the points !!
        audioChart.setCreateSymbols(false);

        spectrumPoints.getData().add(new XYChart.Data<Number, Number>(0, 0));
        audioChart.getData().add(spectrumPoints);
        waterfallLowPoints.getData().add(new XYChart.Data<Number, Number>(-sampleRate / 2, waterfallLow));
        waterfallLowPoints.getData().add(new XYChart.Data<Number, Number>(sampleRate / 2, waterfallLow));
        audioChart.getData().add(waterfallLowPoints);
        waterfallHighPoints.getData().add(new XYChart.Data<Number, Number>(-sampleRate / 2, waterfallHigh));
        waterfallHighPoints.getData().add(new XYChart.Data<Number, Number>(sampleRate / 2, waterfallHigh));
        audioChart.getData().add(waterfallHighPoints);
        filterFrequency[0] = 1000;
        filterWidth[0] = 2700;
        filterFrequencyPoints.getData().add(new XYChart.Data<Number, Number>(filterFrequency[0], 0));
        filterFrequencyPoints.getData().add(new XYChart.Data<Number, Number>(filterFrequency[0], INITIAL_YAXIS_HIGH));
        audioChart.getData().add(filterFrequencyPoints);
        filterEdgePoints.getData().add(new XYChart.Data<Number, Number>(filterFrequency[0] + filterWidth[0], 0));
        filterEdgePoints.getData().add(new XYChart.Data<Number, Number>(filterFrequency[0] + filterWidth[0], INITIAL_YAXIS_HIGH));
        audioChart.getData().add(filterEdgePoints);
        root.getChildren().add(audioChart);

        ToggleGroup modeGroup = new ToggleGroup();
        offButton = new RadioButton("Off");
        offButton.setToggleGroup(modeGroup);
        offButton.setPrefWidth(90);
        offButton.setSelected(true);
        toneButton = new RadioButton("Tone");
        toneButton.setToggleGroup(modeGroup);
        toneButton.setPrefWidth(90);
        wsprButton = new RadioButton("WSPR");
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

        audio.List(audioInBox, audioOutBox, sampleRate);

        // the sample rate choice box
        ChoiceBox sampleRateBox = new ChoiceBox();
        sampleRateBox.getItems().addAll("12000", "48000");
        sampleRateBox.getSelectionModel().select("12000");

        // the fft size choice box
        ChoiceBox symbolBox = new ChoiceBox();
        symbolBox.getItems().addAll("1024", "2048", "4096", "8192");
        symbolBox.getSelectionModel().selectLast();

        // the start/stop button
        final ToggleButton stopStart = new ToggleButton("OFF");

        dBase = dspUtils.baseFreq(fSelected, symbolSize, sampleRate);
        final Label baseFrequency = new Label("base f : " + Double.toString(dBase));
        //      dspUtils.makeContiguousSinusBufferOut(dBase, symbolSize, sampleRate);

        // the frequency field
        final TextField fBaseField = new TextField();
        fBaseField.setText(Integer.toString(fSelected));
        fBaseField.setStyle("-fx-background-color: slateblue; -fx-text-fill: white;");
        fBaseField.setPrefWidth(80);

        // the gain slider
        final Slider gainSlider = new Slider();
        gainSlider.setMin(0);
        gainSlider.setMax(90);
        gainSlider.setValue(50);
        gainSlider.setShowTickLabels(true);
        gainSlider.setShowTickMarks(true);
        gainSlider.setMajorTickUnit(5);
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

        yAxisUpperBoundSlider = new Slider();
        yAxisUpperBoundSlider.setMin(0);
        yAxisUpperBoundSlider.setMax(yAxisHigh);
        yAxisUpperBoundSlider.setValue(yAxisHigh);
        yAxisUpperBoundSlider.setShowTickLabels(true);
        yAxisUpperBoundSlider.setShowTickMarks(true);
        yAxisUpperBoundSlider.setMajorTickUnit(yAxisHigh / 10);
        yAxisUpperBoundSlider.setMinorTickCount(5);
        yAxisUpperBoundSlider.setBlockIncrement(yAxisHigh / 50);
        yAxisUpperBoundSlider.setLayoutX(1120);
        yAxisUpperBoundSlider.setLayoutY(10);
        yAxisUpperBoundSlider.setPrefHeight(360);
        yAxisUpperBoundSlider.setOrientation(Orientation.VERTICAL);
        yAxisUpperBoundSlider.setVisible(false);
        root.getChildren().add(yAxisUpperBoundSlider);

        swapState[0] = SWAPPED;
        demodulation[0] = LSB;

        AnimationTimer timer = new AnimationTimer()
        {
            @Override
            public void handle(long l)
            {
                logger.fine("Animation event received");

                if ((audioOutThread == null) || !audioOutThread.isAlive())
                {
                    logger.fine("Resetting buttons");
                    offButton.setSelected(true);
                }
            }
        };

        modeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
        {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle selectedToggle)
            {
                RadioButton tb = (RadioButton) selectedToggle;
                String s = tb.getText();

                logger.info("mode toggle : " + s);

                double[] dBufferOut;

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
                                logger.fine("Exception when closing audioOutthread");
                            }
                            logger.info("AudioOut thread stopped");
                        }
                        else
                        {
                            logger.info("AudioOut thread already stopped");
                        }
                        break;

                    case "Tone":
                        //      toneButton.setSelected(true);
                        //      toneButton    .requestFocus();

                        dspUtils.makeSymbols(fSelected, symbolSize, sampleRate, gain);
                        dBufferOut = dspUtils.makeBufferOut(dspUtils.f0(20), symbolSize);

                        audioOutThread = new AudioOutThread(audio.sourceDataLine, dBufferOut, 0);
                        audioOutThread.start();

                        //  audio.play(dBufferOut);
                        //    offButton.setSelected(true);
                        break;

                    case "WSPR":
                        dspUtils.makeSymbols(fSelected, symbolSize, sampleRate, gain);
                        dBufferOut = dspUtils.makeBufferOut(dspUtils.test(), symbolSize);

                        long now = System.currentTimeMillis();
                        // wait one sec into the next 2 mins
                        long wait = 120000 - now % 120000 + 1000;
                        long startAt = now + wait;                        
                        logger.fine("Wait : " + wait);

                        audioOutThread = new AudioOutThread(audio.sourceDataLine, dBufferOut, startAt);
                        audioOutThread.start();

                        // audio.play(dBufferOut);
                        //       offButton.setSelected(true);
                        break;
                }
            }
        });

        audioInBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                propWrapper.setProperty("ReceivedAudioIn", newValue.toString());
            }
        });

        audioOutBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                propWrapper.setProperty("ReceivedAudioOut", newValue.toString());
            }
        });

        sampleRateBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                try
                {
                    sampleRate = Integer.parseInt(newValue.toString());
                }
                catch (NumberFormatException e)
                {
                    logger.severe("Unable to parse sample rate");
                    return;
                }
            }
        });

        symbolBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                try
                {
                    symbolSize = Integer.parseInt(newValue.toString());
                }
                catch (NumberFormatException e)
                {
                    logger.severe("Unable to parse fft size");
                    return;
                }
            }
        });

        fBaseField.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e1)
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

                dBase = dspUtils.baseFreq(fSelected, symbolSize, sampleRate);
                baseFrequency.setText("base f : " + Double.toString(dBase));
                //       dspUtils.makeContiguousSinusBufferOut(dBase, symbolSize, sampleRate,gain);

            }
        });

        fBaseField.setOnScroll(new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event)
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

                dBase = dspUtils.baseFreq(fSelected, symbolSize, sampleRate);
                baseFrequency.setText("base f : " + Double.toString(dBase));
                //        dspUtils.makeContiguousSinusBufferOut(dBase, symbolSize, sampleRate);
            }
        });

        stopStart.setOnMousePressed(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent me)
            {
                stopStart.setText("ON");

            }
        });

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
        if (audioInThread.isAlive())
        {
            audioInThread.stopRequest = true;
            while (audioInThread.isAlive())
            {
                try
                {
                    audioInThread.join();
                }
                catch (InterruptedException ex)
                {
                    logger.fine("Exception when closing audioIn thread");
                }
            }
        }
        else
        {
            logger.info("AudioIn thread already stopped");
        }

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
