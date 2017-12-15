/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectmusic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *
 * @author cpgaffney1
 */
public class Parser {

    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;

    /* rec files indicate live performance with irregular "musical" event times and durations for notes.
    *  Treble and bass are not divided into tracks.
     */
    private static final String INPUT_PATH_REC = "C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\liszt\\rec\\liebstrm.mid";

    /* rec files indicate live performance with irregular "musical" event times and durations for notes.
    *  Treble and bass ARE divided into tracks.
    *  Currently these files are peculiar because note off events are not denoted - everything is note on. Note off events have velocity 0.
     */
    private static final String INPUT_PATH_MUS = "C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\liszt\\mus\\liebstrm.mid";
    private static final String OUTPUT_PATH = "C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\AAAtest.mid";
    private static final String[] DIRS = {"beethoven","chopin","liszt","mendelssohn"};
    private static Sequence modifiedParsedSequence;
    private static long lastParsedTick = 0;
    private static long lastSetTick = 0;
    private static float divisionType;
    private static int resolution;

    private static List<String> recPaths;
    private static List<String> musPaths;

    // Take care to ensure that the file paths are arranged in the proper order for the mus and rec list
    // the path at index i in both lists must correspond to the same work of music.
    public static void setFilePaths() {
        recPaths = new ArrayList<String>();
        musPaths = new ArrayList<String>();
        for(String dir: DIRS) {
            File recFolder = new File("C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\" + dir + "\\rec\\");
            String musPath = "C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\" + dir + "\\mus\\";
            File[] recFiles = recFolder.listFiles();
            for (int i = 0; i < recFiles.length; i++) {
                assert(recFiles[i].isFile());
                recPaths.add(recFiles[i].getAbsolutePath());
                musPaths.add(musPath + recFiles[i].getName());
            }
        }
        //recPaths.add(INPUT_PATH_REC);
        //musPaths.add(INPUT_PATH_MUS);
    }

    private static final boolean printParse = false;

    public static List<Song> parseMus() {
        List<Song> musList = new ArrayList<>();
        try {
            for (String path : musPaths) {
                Song musNotes = parseMidi(path, printParse, true, true);
                String[] patharr = path.split("\\\\");
                String name = patharr[patharr.length - 1];
                musNotes.name = path;
                for(int i = 0; i < musNotes.size(); i++) musNotes.get(i).setIndex(i);
                musList.add(musNotes);
            }
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
        return musList;
    }
    
    public static Song parseSong(String path) {
        Song song = null;
        try {
            song = parseMidi(path, printParse, true, true);
            String[] patharr = path.split("\\\\");
            String name = patharr[patharr.length - 1];
            song.name = path;
            for(int i = 0; i < song.size(); i++) song.get(i).setIndex(i);
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
        return song;
    }
    
    public static List<Song> parseRec() {
        List<Song> recList = new ArrayList<>();
        try {
            for (String path : recPaths) {
                Song recNotes = parseMidi(path, printParse, false, false);
                String[] patharr = path.split("\\\\");
                String name = patharr[patharr.length - 1];
                recNotes.name = path;
                for(int i = 0; i < recNotes.size(); i++) recNotes.get(i).setIndex(i);
                recList.add(recNotes);
            }
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
        return recList;
    }

    public static Sequence sequenceAndWrite(Song song, List<MidiEvent> otherEvents, String outputPath, boolean write) throws Exception {
        if(write) assert(!outputPath.equals(""));
        File outputFile = new File(outputPath);
        Sequence seq = new Sequence(divisionType, resolution);
        Track t = seq.createTrack();
        for (Note n : song) {
            ShortMessage smOn = new ShortMessage(NOTE_ON, 1, n.getKey(), n.getOnVelocity());
            ShortMessage smOff = new ShortMessage(NOTE_OFF, 1, n.getKey(), n.getOffVelocity());
            MidiEvent on = new MidiEvent(smOn, n.getStart());
            MidiEvent off = new MidiEvent(smOff, n.getEnd());
            t.add(on);
            t.add(off);
        }
        if(otherEvents != null) {
            for(MidiEvent e: otherEvents) {
                t.add(e);
            }
        }
        if (write) {
            MidiSystem.write(seq, 0, outputFile);
        }
        return seq;
    }

    

    // hasNoNoteOff is set to true for mus files downloaded from http://www.piano-midi.de/midi_files.htm for some peculiar encoding reason, otherwise false.
    // We can tell an event is note_off even though it says note_on because velocity is 0.
    private static Song parseMidi(String inputPath, boolean print, boolean hasNoNoteOff, boolean isMus) throws Exception {
        if(print) System.out.println(inputPath);
        if(inputPath.indexOf("moonlight") != -1 || inputPath.indexOf("chpn_op9_1") != -1 || inputPath.indexOf("pathet") != -1
                || inputPath.indexOf("son23") != -1 || inputPath.indexOf("liz_rhap02") != -1 || inputPath.indexOf("mendel_op19_1") != -1
                || inputPath.indexOf("mendel_op30_1") != -1 || inputPath.indexOf("mendel_op62_5") != -1 || inputPath.indexOf("mendel_op62_3") != -1){
            hasNoNoteOff = true;
        }
        Song parsedNotes = new Song();
        HashSet<Note> openNotes = new HashSet<Note>();
        Sequence sequence = MidiSystem.getSequence(new File(inputPath));
        divisionType = sequence.getDivisionType();
        resolution = sequence.getResolution();
        modifiedParsedSequence = new Sequence(sequence.getDivisionType(), sequence.getResolution());
        int trackNumber = 0;
        for (int t = 0; t < sequence.getTracks().length; t++) {
            Track track = sequence.getTracks()[t];
            modifiedParsedSequence.createTrack();
            trackNumber++;
            if (print) {
                System.out.println("Track " + trackNumber + ": size = " + track.size());
            }
            if (print) {
                System.out.println();
            }
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                if (print) {
                    System.out.print("@" + event.getTick() + " ");
                }
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    /* Short message structure:
                    * data1 = piano key
                    * data2 = velocity
                    * command = NOTE_ON or NOTE_OFF
                    * channel = 1 for piano
                     */
                    ShortMessage sm = (ShortMessage) message;
                    if (print) {
                        System.out.print("Channel: " + sm.getChannel() + " ");
                    }
                    int velocity = sm.getData2();
                    if (sm.getCommand() == NOTE_OFF || (hasNoNoteOff && sm.getCommand() == NOTE_ON && velocity == 0)) {
                        int key = sm.getData1();
                        if (print) {
                            System.out.println("Note off, " + Note.getNoteName(key) + " key=" + key + " velocity: " + velocity);
                        }
                        Note n = findClosingNote(openNotes, key);
                        if (n == null) {
                            System.out.println("Alert: Note " + key + " ended without a beginning.");
                        } else {
                            n.setOffVelocity(velocity);
                            assert(event.getTick() >= 0);
                            n.setEnd(event.getTick());
                            openNotes.remove(n);
                            if(event.getTick() == n.getStart()) {
                                System.out.println(n.readableToString(true));
                                System.out.println(n.readableToString(false));
                            }
                            event = noteEndParsed(event);
                        }
                    } else if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        if (print) {
                            System.out.println("Note on, " + Note.getNoteName(key) + " key=" + key + " velocity: " + velocity);
                        }
                        Note n = new Note(key, Note.getNoteName(key), isMus);
                        n.setOnVelocity(velocity);
                        n.setChannel(sm.getChannel());
                        n.setStart(event.getTick());
                        n.setEnd(event.getTick() + 50); // temporary end value, unless reset by note end event
                        n.setTrack(trackNumber);
                        openNotes.add(n);
                        parsedNotes.add(n);
                        event = noteStartParsed(event);
                    } else if (print) {
                        System.out.println("Command:" + sm.getCommand());
                    }
                } else {
                    if (print) {
                        System.out.println("Other message: " + message.getClass());
                    }
                    if (message instanceof MetaMessage) {
                        MetaMessage mm = (MetaMessage) message;
                        if (print) {
                            System.out.println("Type: " + mm.getType() + ", Data: " + new String(mm.getData()));
                        }
                    }
                }
                modifiedParsedSequence.getTracks()[t].add(event);
            }
            if (print) {
                System.out.println();
            }
        }

        //Sort notes in order of note_on event
        Collections.sort(parsedNotes, new Comparator<Note>() {
            public int compare(Note lhs, Note rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getStart() < rhs.getStart() ? -1 : (lhs.getStart() > rhs.getStart()) ? 1 : 0;
            }
        });

        return parsedNotes;
    }

    private static Note findClosingNote(final HashSet<Note> open, final int key) {
        for (Note n : open) {
            if (n.getKey() == key) {
                return n;
            }
        }
        return null;
    }

    private static MidiEvent noteStartParsed(MidiEvent oldevent) throws Exception {
        Random rand = new Random();
        ShortMessage oldmsg = (ShortMessage) (oldevent.getMessage());
        ShortMessage newmsg = new ShortMessage();
        // data1 = key, data2 = velocity
        newmsg.setMessage(ShortMessage.NOTE_ON, oldmsg.getChannel(), oldmsg.getData1() + 1, oldmsg.getData2());
        long tick;
        if (oldevent.getTick() == lastParsedTick) {
            tick = lastSetTick;
        } else {
            final int std = 10;
            final int mu = 8;
            boolean chance = rand.nextBoolean();
            lastSetTick = chance ? (int) ((rand.nextGaussian() + 10) * mu) : (int) ((rand.nextGaussian() - mu) * std);
            if (lastSetTick < lastParsedTick) {
                lastSetTick = lastParsedTick + 1;
            }
            tick = oldevent.getTick() + lastSetTick;
        }
        MidiEvent newevent = new MidiEvent(newmsg, tick);
        return newevent;
    }

    private static MidiEvent noteEndParsed(MidiEvent event) {
        return event;
    }
}
