package main;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.jfugue.theory.Intervals;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by artem on 12/5/16.
 */
public class ScoreImageBuilder {

    private static final Logger Log = Logger.getLogger(ET_Controller.class.getName());

    private static ArrayList<Float> notes = new ArrayList<Float>(){{
        add((float) 44.0); // midi:52 -> lowE
        add((float) 40.0); // low F
        add((float) 40.1); // .5 == sharp
        add((float) 37.0); // low G
        add((float) 37.1); // G#
        add((float) 32.0); // A
        add((float) 32.1); // A#
        add((float) 29.0); // B
        add((float) 25.0); // middle C
        add((float) 25.1); // C#
        add((float) 21.0); // D
        add((float) 21.1); // D#
        add((float) 17.0); // E
        add((float) 13.0); // F
        add((float) 13.1); // F#
        add((float) 10.0); // G
        add((float) 10.1); // G#
        add((float) 5.0); // A
        add((float) 5.1); // A#
        add((float) 0.0); // B
        add((float) -3.0); // next C
        add((float) -3.1); // C#
        add((float) -7.0); // D
        add((float) -7.1); // D#
        add((float) -11.0); // E
        add((float) -15.0); // F
        add((float) -15.1); // F#
        add((float) -19.0); // G
        add((float) -19.1); // G#
        add((float) -23.0); // high A
    }};

    private static BufferedImage emptyStaff;
    private static BufferedImage extraLines;
    private int noteOrder = 30;

    ScoreImageBuilder(){
        BufferedImage clef = null;
        try {
            clef = ImageIO.read(getClass().getClassLoader().getResource("trebleClef.gif"));
            BufferedImage lines = ImageIO.read(getClass().getClassLoader().getResource("stave.gif"));
            int w = 150;
            int h = clef.getHeight()*2;
            emptyStaff = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics g = emptyStaff .getGraphics();
            g.drawImage(clef, 0, 0, null);

            for(int i=0; i < (150%lines.getWidth());i++){
                g.drawImage(lines, lines.getWidth()*i, 0, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNote(int noteVal){

        int noteDisplacement = noteVal-52;
        float notePos = notes.get(noteDisplacement);
        Log.log(Level.INFO, "Note position if sharp? " + (notePos*10)%10);
        boolean sharp = (notePos*10)%10 == 1.0;

        try {
            BufferedImage note = ImageIO.read(getClass().getClassLoader().getResource("crotchetDown.gif"));

            Graphics g = emptyStaff.getGraphics();
            if(noteDisplacement <= 9){
                extraLines = ImageIO.read(getClass().getClassLoader().getResource("stave.gif")).getSubimage(0,0,10,50);
                g.drawImage(extraLines, noteOrder, 24, null);
            }
            if(sharp){
                BufferedImage accidental = ImageIO.read(getClass().getClassLoader().getResource("sharp.gif"));
                g.drawImage(accidental, noteOrder+13, (int)notePos, null);
            }

            g.drawImage(note, noteOrder, (int)notePos, null);

            Log.log(Level.INFO, "Drawing note " + noteDisplacement);
        } catch (IOException e) {
            e.printStackTrace();
        }

        noteOrder += noteOrder;
    }

    public Image renderScore(){
        Image score = SwingFXUtils.toFXImage(emptyStaff, null);
        noteOrder = 30;
        return score;
    }
}
