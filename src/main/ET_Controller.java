package main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.text.Text;

import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;

import rx.Observable;
import rx.functions.Action1;
import rx.observables.JavaFxObservable;
import rx.schedulers.JavaFxScheduler;
import rx.schedulers.Schedulers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

public class ET_Controller implements Initializable{

    private final Logger Log = Logger.getLogger(ET_Controller.class.getName());

    @FXML
    public Slider slider_start;

    @FXML
    public ChoiceBox play_options;
    private int interval_mode = 0;
    private String[] mode_tooltip = {"Play one note after another", "Play two notes together"};

    @FXML
    public CheckBox check_display_notes;
    @FXML
    public ImageView image_score;

    @FXML
    public Button btn_play;
    private boolean played;

    private int firstNoteVal;
    private int secondNoteVal;


    @FXML
    public Button btn_higher;
    @FXML
    public Button btn_same;
    @FXML
    public Button btn_lower;

    @FXML
    public Text text_result;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources){

        // Draw Score test


        // First Note Slider configuration
        slider_start.setMin(52); // First note set to E4 (E below middle C)
        firstNoteVal = (int) slider_start.getValue();
        slider_start.setMax(76);
        slider_start.setMajorTickUnit(1);
        slider_start.setSnapToTicks(true);
        slider_start.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                Log.log(INFO, "Slider moved to " + t1.intValue());
                firstNoteVal = (int) slider_start.getValue();
                played = false;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        btn_play.setText("Play");
                    }
                });
            }
        });



        // Set up options
        // Will change the function run by Play Button
        // Melodic Intervals, Harmonic Intervals
        play_options.setItems(FXCollections.observableArrayList("Melodic", "Harmonic"));
        play_options.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number old_value, Number new_value) {
                interval_mode = new_value.intValue();

                play_options.setTooltip(new Tooltip(mode_tooltip[new_value.intValue()]));
            }
        });

        // Handler for clicks on the guess buttons
        // Higher, Lower, Same
        // Checks if the user played anything
        // Responds if the guess was correct - changes result text
        //
        EventHandler<ActionEvent> checkHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(played == false){
                    Log.log(Level.WARNING,"YA HAVEN'T PLAYED AN INTERVAL YET!");
                    text_result.setText("Ya haven't played an interval yet!");
                    return;
                }
                if(secondNoteVal > firstNoteVal && actionEvent.getSource() == btn_higher){
                    Log.log(Level.INFO, "YA GOT IT! IT'S HIGHER!");
                    text_result.setText("YOU GOT IT! IT'S HIGHER!");
                    btn_play.setText("Play");
                    played = false;
                } else
                if(secondNoteVal == firstNoteVal && actionEvent.getSource() == btn_same){
                    Log.log(Level.INFO, "YA GOT IT! IT'S THE SAME!");
                    text_result.setText("YA GOT IT! IT'S THE SAME!");
                    btn_play.setText("Play");
                    played = false;
                } else
                if(secondNoteVal < firstNoteVal && actionEvent.getSource() == btn_lower){
                    Log.log(Level.INFO, "YA GOT IT! IT'S LOWER!");
                    text_result.setText("YA GOT IT! IT'S LOWER!");
                    btn_play.setText("Play");
                    played = false;
                } else {
                    text_result.setText("Nope :(");
                    Log.log(Level.INFO, "FAIL");
                }
            }
        };


        check_display_notes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(check_display_notes.isSelected()){
                    showStaff();
                } else {
                    hideStaff();
                }
            }
        });
        // Play Button handling
        // Use observable for this because the JFugue music player needs to be on a separate thread
        // I could've done this differently, but it's good practice for RxJava
        Observable<ActionEvent> btnPlayEvent = JavaFxObservable.fromActionEvents(btn_play);
        btnPlayEvent.observeOn(Schedulers.newThread())
                .subscribe(new Action1<ActionEvent>() {
            @Override
            public void call(ActionEvent actionEvent) {
                if(interval_mode == 0){
                    playMelodic();

                } else if (interval_mode == 1){
                    playHarmonic();
                }
                if(check_display_notes.isSelected()){
                    showStaff();
                } else {
                    hideStaff();
                }

            }
        });
        btn_higher.setOnAction(checkHandler);
        btn_same.setOnAction(checkHandler);
        btn_lower.setOnAction(checkHandler);
    }

    private void playMelodic() {
        firstNoteVal = (int) slider_start.getValue();
        if(played == false){
            secondNoteVal =  ThreadLocalRandom.current().nextInt(52, 81);
        }

        String noteOne = Integer.toString(firstNoteVal)+"w";
        String noteTwo = Integer.toString(secondNoteVal)+"w";

        Pattern melodic_interval = new Pattern( noteOne + " " + noteTwo);

        Player intervalGenerator = new Player();
        Log.log(INFO, "Player playing " + melodic_interval.toString());
        text_result.setText("Let's See what you Hear!");
        played = true;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                btn_play.setText("RePlay?");
            }
        });
        intervalGenerator.play(melodic_interval);
    }

    private void playHarmonic() {
        firstNoteVal = (int) slider_start.getValue();
        if(played == false){
            secondNoteVal =  ThreadLocalRandom.current().nextInt(52, 81);
        }

        String noteOne = Integer.toString(firstNoteVal)+"w";
        String noteTwo = Integer.toString(secondNoteVal)+"w";

        Pattern first_note = new Pattern(noteOne);
        Pattern harmonic_interval = new Pattern( "V0 " + noteOne + " V1 " + noteTwo);
        Player intervalGenerator = new Player();

        Log.log(INFO, "Player playing " + harmonic_interval.toString());

        text_result.setText("Let's See what you Hear!");
        played = true;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                btn_play.setText("RePlay?");
            }
        });
        intervalGenerator.play(first_note);
        intervalGenerator.play(harmonic_interval);
    }

    private void showStaff(){
        ScoreImageBuilder testBuilder = new ScoreImageBuilder();
        if (played){
            testBuilder.addNote(firstNoteVal);
            testBuilder.addNote(secondNoteVal);
        }
        Image filledStaff = testBuilder.renderScore();

        image_score.setX(175.0);
        image_score.setY(14.0);
        image_score.setFitHeight(120.0);
        image_score.setFitWidth(150.0);
        image_score.setImage(filledStaff);
    }

    private void hideStaff() {
        ScoreImageBuilder testBuilder = new ScoreImageBuilder();
        Image clearedStaff = testBuilder.renderScore();
        image_score.setX(175.0);
        image_score.setY(14.0);
        image_score.setFitHeight(120.0);
        image_score.setFitWidth(150.0);
        image_score.setImage(clearedStaff);
    }


}
