/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectmusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.ByteBuffer;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author cpgaffney1
 */
public class Parser {

    private static final int NOTE_ON = 0x90;
    private static final int NOTE_OFF = 0x80;
    private static final int TEMPO_MSG = 0x51;
    private static final int DEFAULT_TEMPO = 120;

    /* rec files indicate live performance with irregular "musical" event times and durations for notes.
    *  Treble and bass are not divided into tracks.
     */
    private static final String INPUT_PATH_REC = "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\liszt\\rec\\liebstrm.mid";

    /* rec files indicate live performance with irregular "musical" event times and durations for notes.
    *  Treble and bass ARE divided into tracks.
    *  Currently these files are peculiar because note off events are not denoted - everything is note on. Note off events have velocity 0.
     */
    private static final String INPUT_PATH_MUS = "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\liszt\\mus\\liebstrm.mid";
    private static final String OUTPUT_PATH = "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\AAAtest.mid";
    private static final String[] DIRS = {"brahms", "beethoven", "chopin", "liszt", "mendelssohn", "rach"};
    private static final String[] USE_FILES = {"mendel_op62_3", "chpn_op32_1", "chpn_op27_2", "w9", "pathet1", "pathet2",
        "pathet3", "son23_1", "son23_2", "son23_3"};
    private static HashSet<String> useFiles;
    private static Sequence modifiedParsedSequence;
    private static long lastParsedTick = 0;
    private static long lastSetTick = 0;
    private static float divisionType;
    private static int resolution;
    
    private static List<Integer> tempos;
    private static List<Long> tempoEventTimes;

    private static final boolean selectCertainFiles = false;
    private static final boolean printParse = false;

    private static List<String> recPaths;
    private static List<String> musPaths;

    // Take care to ensure that the file paths are arranged in the proper order for the mus and rec list
    // the path at index i in both lists must correspond to the same work of music.
    public static void setFilePaths() {
        useFiles = new HashSet<>();
        useFiles.addAll(Arrays.asList(USE_FILES));
        recPaths = new ArrayList<String>();
        musPaths = new ArrayList<String>();
        for (String dir : DIRS) {
            File recFolder = new File("C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\" + dir + "\\rec\\");
            String musPath = "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\" + dir + "\\mus\\";
            File[] recFiles = recFolder.listFiles();
            for (int i = 0; i < recFiles.length; i++) {
                assert (recFiles[i].isFile());
                recPaths.add(recFiles[i].getAbsolutePath());
                musPaths.add(musPath + recFiles[i].getName());
            }
        }
        //recPaths.add(INPUT_PATH_REC);
        //musPaths.add(INPUT_PATH_MUS);
    }

    public static List<Song> parseData(boolean asMus) {
        int count = 0;
        List<Song> songList = new ArrayList<>();
        File dataFolder = new File("C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\data");
        File[] subfolders = dataFolder.listFiles();
        for (int i = 0; i < subfolders.length; i++) {
            assert (subfolders[i].isDirectory());
            File[] songFiles = subfolders[i].listFiles();
            for (int j = 0; j < songFiles.length; j++) {
                assert (songFiles[j].isFile());
                String[] patharr = songFiles[j].getAbsolutePath().split("\\\\");
                String name = patharr[patharr.length - 1].split("\\.")[0];
                if (selectCertainFiles && !(useFiles.contains(name))) {
                    continue;
                }
                Song s;
                try {
                    s = parseMidi(songFiles[j].getAbsolutePath(), printParse, true, asMus);
                    s.name = songFiles[j].getAbsolutePath();
                    for (int k = 0; k < s.size(); k++) {
                        s.get(k).setIndex(k);
                    }
                    if(!asMus) {
                        s = incorporateTempos(s);
                    }
                    songList.add(s);
                } catch (Exception ex) {
                    System.out.println(ex);
                    ex.printStackTrace();
                }
                count++;
                
            }
        }
        return songList;
    }

    public static List<Song> multiply(List<Song> lis) {
        List<Song> mult = new ArrayList<>();
        // unison, M3, P4, P5, M6
        int[] transpositions = {0, 4, 5, 7, 9};
        for (Song s : lis) {
            for (int i = 0; i < transpositions.length; i++) {
                Song incremented = new Song(s);
                for (Note n : incremented) {
                    n.setKey(n.getKey() + transpositions[i]);
                }
                mult.add(incremented);
            }
        }
        return mult;
    }

    public static List<Song> parseMus() {
        List<Song> musList = new ArrayList<>();
        try {
            for (String path : musPaths) {
                String[] patharr = path.split("\\\\");
                String name = patharr[patharr.length - 1].split("\\.")[0];
                if (selectCertainFiles && !(useFiles.contains(name))) {
                    continue;
                }
                Song musNotes = parseMidi(path, printParse, true, true);
                musNotes.name = path;
                for (int i = 0; i < musNotes.size(); i++) {
                    musNotes.get(i).setIndex(i);
                }
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
            for (int i = 0; i < song.size(); i++) {
                song.get(i).setIndex(i);
            }
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
                String[] patharr = path.split("\\\\");
                String name = patharr[patharr.length - 1].split("\\.")[0];
                if (selectCertainFiles && !(useFiles.contains(name))) {
                    continue;
                }
                Song recNotes = parseMidi(path, printParse, false, false);
                recNotes.name = path;
                for (int i = 0; i < recNotes.size(); i++) {
                    recNotes.get(i).setIndex(i);
                }
                recList.add(recNotes);
            }
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
        return recList;
    }

    public static Sequence sequenceAndWrite(Song song, List<MidiEvent> otherEvents, String outputPath, boolean write) throws Exception {
        if (write) {
            assert (!outputPath.equals(""));
        }
        File outputFile = new File(outputPath);
        Sequence seq = new Sequence(divisionType, resolution);
        Track t = seq.createTrack();
        for (Note n : song) {
            System.out.println(n.readableToString(true));
            ShortMessage smOn = new ShortMessage(NOTE_ON, 1, n.getKey(), n.getOnVelocity());
            ShortMessage smOff = new ShortMessage(NOTE_OFF, 1, n.getKey(), n.getOffVelocity());
            MidiEvent on = new MidiEvent(smOn, n.getStart());
            MidiEvent off = new MidiEvent(smOff, n.getEnd());
            t.add(on);
            t.add(off);
        }
        if (otherEvents != null) {
            for (MidiEvent e : otherEvents) {
                t.add(e);
            }
        }
        if (write) {
            MidiSystem.write(seq, 0, outputFile);
        }
        return seq;
    }

    // output {musNote}:{candNote1}/lcsPercent/percentNotes/distPercent;...
    public static void writeMatchesAndRec(BufferedWriter bf, List<NoteMatch> potentialMatches, Song rec) {
        try {
            int skippedCount = 0;
            int counter = 0;
            for (NoteMatch nm : potentialMatches) {
                assert (nm.getSelf().getSongIndex() == counter);
                counter++;
                String line = "";
                line += nm.getSelf().toString() + ":";
                for (Note match : nm.getPotentialMatches()) {
                    line += match.toString() + "/" + nm.getLcs(match) + "/" + nm.getPercentSameNotes(match) + "/"
                            + (double) Math.abs(nm.getSelf().getSongIndex() - match.getSongIndex()) / potentialMatches.size()
                            + "/" + nm.getEditDist(match) + ";";
                }
                if (nm.getPotentialMatches().size() > 0) {
                    line = line.substring(0, line.length() - 1);
                } else {
                    skippedCount += 1;
                }
                bf.write(line);
                bf.newLine();
            }
            bf.write("***");
            bf.newLine();
            bf.write(rec.name);
            bf.newLine();
            for (Note n : rec) {
                //System.out.println(n.print());
                bf.write(n.toString());
                bf.newLine();
            }
            bf.write("---");
            bf.newLine();
            System.out.println("skipped = " + skippedCount);
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
    
    public static void writeSongForPrediction(Song mus) {
        try {
            File outputFile = new File("C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\testMus.txt");
            BufferedWriter bf = new BufferedWriter(new FileWriter(outputFile));
            for (Note n : mus) {
                bf.write(n.toString());
                bf.newLine();
            }
            bf.close();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    public static Song readPIDI(String path) {
        Song predictions = new Song();
        try {
            File inFile = new File(path);
            BufferedReader bf = new BufferedReader(new FileReader(inFile));
            while (true) {
                String line = bf.readLine();

                if (line == null) {
                    break;
                }
                line = line.substring(1, line.length() - 1);
                String[] arr = line.split(",");

                int key = (int) (Double.parseDouble(arr[0]));
                int mIndex = (int) (Double.parseDouble(arr[1]));
                int onv = (int) (Double.parseDouble(arr[2]));;
                int offv = (int) (Double.parseDouble(arr[3]));;
                long start = (long) (Double.parseDouble(arr[4]));
                long length = (long) (Double.parseDouble(arr[5]));
                int track = (int) (Double.parseDouble(arr[6]));
                Note n = new Note();
                n.setKey(key);
                n.setIndex(mIndex);
                n.setOnVelocity(onv);
                n.setOffVelocity(offv);
                n.setStart(start);
                n.setEnd(start + length);
                n.setTrack(track);
                predictions.add(n);
            }
            bf.close();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
        return predictions;
    }
    
    private static int nextNoteIndexForEventTime(long event, Song song, int previousIndex) {
        int i;
        for(i = previousIndex; i < song.size(); i++) { 
            if(song.get(i).getStart() >= event) break;
        }
        return i;
    }
    
    private static Song incorporateTempos(Song song) {
        if(printParse) {
            System.out.println("Incorporating tempos");
        }
        final int doubleCutoff = 100;
        Song temposIncorporated = new Song();
        Song startTimeModified = new Song();
        List<Long> startTimesTemp = new ArrayList<>();
        assert(tempos.size() > 1);
        long longOffset = 0;
        double remainderOffset = 0;
        int currentTempo = DEFAULT_TEMPO;
        int nextTempoIndex = 0;
        long tickRange =  song.get(song.size()-1).getEnd();
        for(long i = 0; i < tickRange; i++) {
            // incorporate end time
            for(int j = 0; j < startTimeModified.size(); j++) {
                Note n = startTimeModified.get(j);
                if(n.getEnd() == i) {
                    long newStart = startTimesTemp.get(j);
                    long newEnd = longOffset + (int)remainderOffset;
                    n.setDuration(newStart, newEnd);
                    temposIncorporated.add(n);
                    startTimeModified.remove(j);
                    startTimesTemp.remove(j);
                    j--;
                }
            }
            //incorporate start time
            while(!(song.isEmpty()) && song.get(0).getStart() == i) {
                Note n = song.get(0);
                startTimesTemp.add(longOffset + (int)remainderOffset);
                startTimeModified.add(n);
                song.remove(0);
            }
            // find next tempo if available
            if(nextTempoIndex < tempos.size() && tempoEventTimes.get(nextTempoIndex) == i) {
                // this is the latest tempo that applies
                currentTempo = tempos.get(nextTempoIndex);
                nextTempoIndex++;
            }
            //add to offset
            double tempoRatio = (double)DEFAULT_TEMPO / (double)currentTempo;
            remainderOffset += tempoRatio;
            if(remainderOffset > doubleCutoff) {
                remainderOffset -= doubleCutoff;
                longOffset += doubleCutoff;
            }
            assert(remainderOffset <= doubleCutoff);
            assert(longOffset % doubleCutoff == 0);
        }
        return temposIncorporated;
        /*
        int latestNoteIndex = 0;
        for(int i = 0; i < tempos.size(); i++) {
            latestNoteIndex = nextNoteIndexForEventTime(tempoEventTimes.get(i), song, latestNoteIndex);
            if(printParse) System.out.println(latestNoteIndex);
            long tickOffset = song.get(latestNoteIndex).getStart();
            double stretchRatio = (double)DEFAULT_TEMPO / (double)tempos.get(i);
            if(printParse) System.out.println(stretchRatio);
            for(int j = latestNoteIndex; j < song.size(); j++) {
                Note n = song.get(j);
                n.setStart((long)((n.getStart() - tickOffset) * stretchRatio) + tickOffset);
                n.setEnd((long)((n.getEnd() - tickOffset) * stretchRatio) + tickOffset);
                song.set(j, n);
            }
        }
        
        */      
    }

    // hasNoNoteOff is set to true for mus files downloaded from http://www.piano-midi.de/midi_files.htm for some peculiar encoding reason, otherwise false.
    // We can tell an event is note_off even though it says note_on because velocity is 0.
    private static Song parseMidi(String inputPath, boolean print, boolean hasNoNoteOff, boolean isMus) throws Exception {
        tempos = new ArrayList<>();
        tempoEventTimes = new ArrayList<>();
        if (print) {
            System.out.println(inputPath);
        }
        if (inputPath.indexOf("moonlight") != -1 || inputPath.indexOf("chpn_op9_1") != -1 || inputPath.indexOf("pathet") != -1
                || inputPath.indexOf("son23") != -1 || inputPath.indexOf("liz_rhap02") != -1 || inputPath.indexOf("mendel_op19_1") != -1
                || inputPath.indexOf("mendel_op30_1") != -1 || inputPath.indexOf("mendel_op62_5") != -1 || inputPath.indexOf("mendel_op62_3") != -1) {
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
                            assert (event.getTick() >= 0);
                            n.setEnd(event.getTick());
                            openNotes.remove(n);
                            if (event.getTick() == n.getStart()) {
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
                        byte[] data = mm.getData();
                        int type = mm.getType();
                        if (mm.getType() == TEMPO_MSG) {
                            assert (data.length == 3);
                            int microPerBeat = (data[0] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[2] & 0xFF);
                            int tempo = (int) Math.round(60000000.0 / microPerBeat);
                            tempos.add(tempo);
                            tempoEventTimes.add(event.getTick());
                            if (print) {
                                System.out.println("Tempo: " + tempo);
                            }
                        } else if (print) {
                            System.out.println("Type: " + type + ", Data: " + new String(mm.getData()));
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
        }
        );

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

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
