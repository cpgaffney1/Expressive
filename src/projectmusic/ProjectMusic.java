/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectmusic;

import org.jfugue.pattern.*;
import org.jfugue.player.*;
import org.jfugue.integration.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import org.jfugue.parser.*;
import nu.xom.*;
import org.jfugue.midi.*;
import javax.sound.midi.*;
import org.staccato.*;
import javax.xml.parsers.*;
import org.jfugue.devtools.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.sound.midi.*;
import java.util.function.*;
/**
 *
 * @author cpgaffney1
 */
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ProjectMusic {

    enum Case {
        predict,
        parseAndMatch,
        writeForPrediction
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Case c = Case.predict;
        String path = "C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\chopin\\";
        String name = "chpn-p4.mid";
        BufferedWriter bf = null;
        switch (c) {
            case writeForPrediction:
                Song toPredict = Parser.parseSong(path + "mus\\" + name);
                Song humanSong = Parser.parseSong(path + "rec\\" + name);
                //writeSongForPrediction(toPredict);
                try {
                        File outputFile = new File("C:\\Users\\cpgaffney1\\PycharmProjects\\Music\\testData0.txt");
                        bf = new BufferedWriter(new FileWriter(outputFile));
                    } catch (Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }
                normalizeTimes(toPredict, humanSong);
                List<NoteMatch> potentialMatches = Matcher.matchNotes(toPredict, humanSong);
                    
                if(bf != null) {
                    writeMatchesAndRec(bf, potentialMatches, humanSong);
                    try { bf.close(); }
                        catch (Exception ex) {
                            System.out.println(ex);
                            ex.printStackTrace();
                        }
                }
                break;
            case predict:
                toPredict = Parser.parseSong(path + "mus\\" + name);
                Song actual = Parser.parseSong(path + "rec\\" + name);
                List<Prediction> predictions = readPredictions();
                Song predictedSong = new Song();
                for(Prediction p: predictions) {
                    long length = toPredict.get(p.mIndex).getEnd() - toPredict.get(p.mIndex).getStart();
                    toPredict.get(p.mIndex).setStart(p.start);
                    toPredict.get(p.mIndex).setEnd(p.start + length);
                    System.out.println(toPredict.get(p.mIndex).readableToString(true));
                }
                String out = "C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\prediction.mid";
                toPredict.print();
                try {
                    Parser.sequenceAndWrite(toPredict, null, out, true);
                } catch(Exception ex) {
                    System.out.println(ex);
                    ex.printStackTrace();
                }
                break;
            case parseAndMatch:
                Parser.setFilePaths();
                List<Song> musList = Parser.parseMus();
                List<Song> recList = Parser.parseRec();
                assert musList.size() == recList.size();
                bf = null;
                for (int i = 0; i < musList.size(); i++) {
                    System.out.println(i);
                    Song mus = musList.get(i);
                    Song rec = recList.get(i);
                    normalizeTimes(mus, rec);
                    potentialMatches = Matcher.matchNotes(mus, rec);
                    try {
                        File outputFile = new File("C:\\Users\\cpgaffney1\\PycharmProjects\\Music\\javaOutput\\javaOutput" + i + ".txt");
                        bf = new BufferedWriter(new FileWriter(outputFile));
                        writeMatchesAndRec(bf, potentialMatches, rec);
                        bf.close();
                    } catch (Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }                   
                    
                    //visualize(mus, rec);
//                    Sequence seq = null;
//                    try {
//                        seq = Parser.sequenceAndWrite(rec, null, "", true);
//                    } catch (Exception ex) {
//                        System.out.println(ex);
//                        ex.printStackTrace();
//                    }
//                    if (seq != null) {
//                        play(seq);
//                    }
                }
                break;
            default:
                break;
        }
    }

    private static void normalizeTimes(Song mus, Song rec) {
        long firstRecTick = rec.get(0).getStart();
        for (Note n : rec) {
            //System.out.println(n.toString());
            n.setStart(n.getStart() - firstRecTick);
            n.setEnd(n.getEnd() - firstRecTick);
        }
        long firstMusTick = mus.get(0).getStart();
        for (Note n : mus) {
            //System.out.println(n.toString());
            n.setStart(n.getStart() - firstMusTick);
            n.setEnd(n.getEnd() - firstMusTick);
        }
    }

    private static void normalizeTimes(Song s, double ratio) {
        for (Note n : s) {
            n.setStart((long) (n.getStart() / ratio));
            n.setEnd((long) (n.getEnd() / ratio));
        }
    }

    private static void visualize(Song mus, Song rec) {
        final String input = "C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\matchedNotes.txt";
        try {
            BufferedReader rd = new BufferedReader(new FileReader(input));
            String line;
            List<MidiEvent> musEvents = new ArrayList<>();
            List<MidiEvent> recEvents = new ArrayList<>();
            while (true) {
                line = rd.readLine();
                if (line == null) {
                    break;
                }
                String[] arr = line.split(",");
                int musIndex = Integer.parseInt(arr[0]);
                int recIndex = Integer.parseInt(arr[1]);
                String musMsg = "" + musIndex + ": matches " + recIndex;
                String recMsg = "" + recIndex + ": matches " + musIndex;
                System.out.println(musMsg);
                System.out.println(recMsg);
                System.out.println();
                //long randOffset =  mus.get(musIndex).getStart() + (int)(Math.random() * 10 - 5);
                ///long randStart = Math.max(mus.get(musIndex).getStart() + randOffset, 0);
                //long randEnd = Math.max(mus.get(musIndex).getEnd() + randOffset, 0);
                //mus.get(musIndex).setStart(randStart);
                //mus.get(musIndex).setEnd(randEnd);
                musEvents.add(new MidiEvent(new MetaMessage(1, musMsg.getBytes(), musMsg.getBytes().length), mus.get(musIndex).getStart()));
                recEvents.add(new MidiEvent(new MetaMessage(1, recMsg.getBytes(), recMsg.getBytes().length), rec.get(recIndex).getStart()));
            }
            Parser.sequenceAndWrite(rec, recEvents, "C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\recTest.mid", true);
            Parser.sequenceAndWrite(mus, musEvents, "C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\musTest.mid", true);
            rd.close();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }

    }

    private static void play(final Sequence seq) {
        try {
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(seq);
            sequencer.start();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }

    }

    private static void printCompareNoteLists(Song A, Song B) {
        int max = (A.size() > B.size()) ? A.size() : B.size();
        for (int i = 0; i < max; i++) {
            if (i < A.size()) {
                System.out.print(Note.getNoteName(A.get(i).getKey()));
            }
            System.out.print("\t");
            if (i < B.size()) {
                System.out.print(Note.getNoteName(B.get(i).getKey()));
            }
            System.out.println();
        }
    }

    // output {musNote}:{candNote1}/lcsPercent/percentNotes/distPercent;...
    private static void writeMatchesAndRec(BufferedWriter bf, List<NoteMatch> potentialMatches, Song rec) {
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
                            + (double) Math.abs(nm.getSelf().getSongIndex() - match.getSongIndex()) / potentialMatches.size() + ";";
                }
                if(nm.getPotentialMatches().size() > 0) line = line.substring(0, line.length() - 1);
                else skippedCount += 1;
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

    private static void writeSongForPrediction(Song mus) {
        try {
            File outputFile = new File("C:\\Users\\cpgaffney1\\PycharmProjects\\Music\\testMus.txt");
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
    
    private static List<Prediction> readPredictions() {
        List<Prediction> predictions = new ArrayList<>();
        try {
            File inFile = new File("C:\\Users\\cpgaffney1\\Documents\\NetBeansProjects\\ProjectMusic\\files\\predictions.txt");
            BufferedReader bf = new BufferedReader(new FileReader(inFile));
            while(true) {
                String line = bf.readLine();
                if(line == null) break;
                String[] arr = line.split(",");
                int mIndex = (Integer.parseInt(arr[0]));
                long start = (long)(Double.parseDouble(arr[1]) * 4);
                long length = (long)(Double.parseDouble(arr[2]) * 4);
                Prediction p = new Prediction(mIndex, start, length);
                predictions.add(p);
            }
            bf.close();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
        return predictions;
    }

}
