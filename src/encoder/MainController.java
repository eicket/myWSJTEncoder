package encoder;

import dsp.WSPR;
import audio.AudioOut;
import audio.AudioOutThread;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT4;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_FT8;
import static common.Constants.NR_OF_SAMPLES_PER_SYMBOL_WSPR;
import common.PropertiesWrapper;
import dsp.FT;
import dsp.Tone;
import dsp.Utils;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

public class MainController
{

    static final Logger logger = Logger.getLogger(MainController.class.getName());

    private PropertiesWrapper propWrapper = new PropertiesWrapper();

    private int fSelected = 1500;
    private double fBase = 0;
    private double gain = 0;

    private Tone tone = new Tone();
    private WSPR wspr = new WSPR();
    private FT ft = new FT();
    private AudioOutThread audioOutThread;
    private Timeline timeline;

    @FXML
    private RadioButton offButton;

    @FXML
    void offClicked(MouseEvent event)
    {
        
        logger.info("Off button");

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
    }

    @FXML
    private RadioButton toneButton;

    @FXML
    void toneClicked(MouseEvent event)

    {
        logger.info("Tone button");

        propWrapper.setProperty("ReceivedAudioOut", audioOutBox.getValue());
        logger.info("Audio out : " + audioOutBox.getValue());

        long startAt = 0;

        audioOutThread = new AudioOutThread(audioOutBox.getValue(), tone.makeAudio(fSelected, gain), startAt);
        audioOutThread.start();
    }

    @FXML
    private RadioButton wsprButton;

    @FXML
    void wsprClicked(MouseEvent event)

    {
        logger.info("WSPR button");

        propWrapper.setProperty("ReceivedAudioOut", audioOutBox.getValue());
        logger.info("Audio out : " + audioOutBox.getValue());

        fBase = Utils.baseFreq(fSelected, NR_OF_SAMPLES_PER_SYMBOL_WSPR);
        baseFrequencyLabel.setText("base f : " + Double.toString(fBase));

        long startAt = 0;

        long now = System.currentTimeMillis();
        // wait one sec into the next 2 mins
        long wait = 120000 - now % 120000 + 1000;
        startAt = now + wait;
        logger.fine("Wait : " + wait);

        audioOutThread = new AudioOutThread(audioOutBox.getValue(), wspr.makeAudio(fSelected, gain), startAt);
        audioOutThread.start();
    }

    @FXML
    private RadioButton ft4Button;

    @FXML
    void ft4Clicked(MouseEvent event)
    {
        logger.info("FT4 button");

        propWrapper.setProperty("ReceivedAudioOut", audioOutBox.getValue());
        logger.info("Audio out : " + audioOutBox.getValue());

        fBase = Utils.baseFreq(fSelected, NR_OF_SAMPLES_PER_SYMBOL_FT4);
        baseFrequencyLabel.setText("base f : " + Double.toString(fBase));

        long startAt = 0;

        long now = System.currentTimeMillis();
        // wait 500 msec 
        long wait = 15000 - now % 15000 + 500;
        startAt = now + wait;
        logger.fine("Wait : " + wait);

        audioOutThread = new AudioOutThread(audioOutBox.getValue(), ft.makeFT4Audio(fSelected, gain), startAt);
        audioOutThread.start();
    }

    @FXML
    private RadioButton ft8Button;

    @FXML
    void ft8Clicked(MouseEvent event)
    {
        logger.info("FT8 button");

        propWrapper.setProperty("ReceivedAudioOut", audioOutBox.getValue());
        logger.info("Audio out : " + audioOutBox.getValue());

        fBase = Utils.baseFreq(fSelected, NR_OF_SAMPLES_PER_SYMBOL_FT8);
        baseFrequencyLabel.setText("base f : " + Double.toString(fBase));

        long startAt = 0;

        long now = System.currentTimeMillis();
        // wait 500 msec into the next 2 mins
        long wait = 15000 - now % 15000 + 500;
        startAt = now + wait;
        logger.fine("Wait : " + wait);

        audioOutThread = new AudioOutThread(audioOutBox.getValue(), ft.makeFT8Audio(fSelected, gain), startAt);
        audioOutThread.start();
    }

    @FXML
    private ChoiceBox<String> audioOutBox;

    @FXML
    private TextField fSelectedField;

    @FXML
    void fSelectedFieldKeyPressed(KeyEvent event)
    {
        if (event.getCode().equals(KeyCode.ENTER) || event.getCode().equals(KeyCode.TAB))
        {
            try
            {
                fSelected = Integer.parseInt(fSelectedField.getText());
            }
            catch (NumberFormatException e2)
            {
                logger.info("Cannot parse fBase field");
                return;
            }

            logger.info("fSelected set to : " + fSelected);
        }
    }

    @FXML
    void fSelectedFieldScroll(ScrollEvent event)
    {
        int fScroll = 1;

        logger.fine("fBase scroll : " + event.getDeltaY());
        if (event.getDeltaY() > 0)
        {
            fSelected = fSelected + fScroll;
        }
        else
        {
            fSelected = fSelected - fScroll;
        }
        fSelectedField.setText(Integer.toString(fSelected));

        logger.info("fSelected set to : " + fSelected);
    }
    @FXML
    private Label baseFrequencyLabel;

    @FXML
    private TextField wsprMessageField;

    @FXML
    void wsprMessageFieldKeyPressed(KeyEvent event)
    {
        if (event.getCode().equals(KeyCode.ENTER) || event.getCode().equals(KeyCode.TAB))
        {
            wspr.message = wsprMessageField.getText();
            logger.info("wsprMessage set to : " + wspr.message);
        }
    }

    @FXML
    private TextField ftMessageField;

    @FXML
    void ftMessageFieldKeyPressed(KeyEvent event)
    {
        if (event.getCode().equals(KeyCode.ENTER) || event.getCode().equals(KeyCode.TAB))
        {
            ft.message = ftMessageField.getText();
            logger.info("ftMessage set to : " + ft.message);
        }
    }

    @FXML
    private Slider gainSlider;

    @FXML
    private Label gainLabel;

    @FXML
    void initialize()
    {
        AudioOut.ListAudioOut(audioOutBox);

        fSelectedField.setText(Integer.toString(fSelected));
        gainSlider.setTooltip(new Tooltip("dB"));

        fBase = Utils.baseFreq(fSelected, NR_OF_SAMPLES_PER_SYMBOL_WSPR);
        baseFrequencyLabel.setText("base f : " + Double.toString(fBase));
        wsprMessageField.setText("ON4PB JO20 30");
        wspr.message = wsprMessageField.getText();

        ftMessageField.setText("FREE FRE FREE");
        ft.message = ftMessageField.getText();

        timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                logger.fine("Animation event received");

                gain = Math.pow(10, gainSlider.getValue() / 20);
                logger.fine("slider gain in db : " + gainSlider.getValue() + ", gain : " + gain);

                gainLabel.setText(gainSlider.getValue() + " dB");

                if ((audioOutThread == null) || !audioOutThread.isAlive())
                {
                    logger.fine("Resetting buttons");
                    offButton.setSelected(true);
                }
            }
        }
        ));
        timeline.setCycleCount(Animation.INDEFINITE);

        timeline.play();
    }
}
