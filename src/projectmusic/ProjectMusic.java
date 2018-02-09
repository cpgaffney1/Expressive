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
        writeForPrediction,
        parseSongs,
        readPIDI
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Case c = Case.parseSongs;
        String path = "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\liszt\\";
        String name = "liebstrm.mid";
        BufferedWriter bf = null;
        List<Song> musList;
        List<Song> recList;
        switch (c) {
            case writeForPrediction:
                Song toPredict = Parser.parseSong(path + "mus\\" + name);
                Song humanSong = Parser.parseSong(path + "rec\\" + name);
                //writeSongForPrediction(toPredict);
                try {
                        File outputFile = new File("C:\\Users\\cpgaf\\PycharmProjects\\ExpressiveAI\\src\\testData0.txt");
                        bf = new BufferedWriter(new FileWriter(outputFile));
                    } catch (Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }
                normalizeTimes(toPredict, humanSong);
                List<NoteMatch> potentialMatches = Matcher.matchNotes(toPredict, humanSong);
                    
                if(bf != null) {
                    Parser.writeMatchesAndRec(bf, potentialMatches, humanSong);
                    try { bf.close(); }
                        catch (Exception ex) {
                            System.out.println(ex);
                            ex.printStackTrace();
                        }
                }
                break;
            case readPIDI:
                Song inferred = Parser.readPIDI("C:\\Users\\cpgaf\\PycharmProjects\\ExpressiveAI\\inferredMus\\inferred0.txt");
                String out = "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\inferred0.mid";
                try {
                    Parser.sequenceAndWrite(inferred, null, out, true);
                } catch(Exception ex) {
                    System.out.println(ex);
                    ex.printStackTrace();
                }
                break;
            case predict:
                Song predictions = Parser.readPIDI("C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\predictions0.txt");
                out = "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\predictions0.mid";
                predictions.print();
                try {
                    Parser.sequenceAndWrite(predictions, null, out, true);
                } catch(Exception ex) {
                    System.out.println(ex);
                    ex.printStackTrace();
                }
                break;
            case parseAndMatch:
                Parser.setFilePaths();
                musList = Parser.parseMus();
                recList = Parser.parseRec();
                //musList = Parser.multiply(musList);
                //recList = Parser.multiply(recList);
                assert musList.size() == recList.size();
                bf = null;
                for (int i = 0; i < musList.size(); i++) {
                    System.out.println(i);
                    Song mus = musList.get(i);
                    Song rec = recList.get(i);
                    normalizeTimes(mus, rec);
                    potentialMatches = Matcher.matchNotes(mus, rec);
                    try {
                        File outputFile = new File("C:\\Users\\cpgaf\\PycharmProjects\\ExpressiveAI\\javaOutput\\javaOutput" + i + ".txt");
                        bf = new BufferedWriter(new FileWriter(outputFile));
                        Parser.writeMatchesAndRec(bf, potentialMatches, rec);
                        bf.close();
                    } catch (Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }                   
                }
                break;
            case parseSongs:
                musList = Parser.parseData(true);
                recList = Parser.parseData(false);
                musList = Parser.multiply(musList);
                recList = Parser.multiply(recList);
                
                int index = 0;
                for(Song s: musList) {
                    final String dir = "C:\\Users\\cpgaf\\PycharmProjects\\ExpressiveAI\\mus";
                    s.writePIDI(dir + "\\javaOutput" + index + ".txt", false);
                    index++;
                }
                
                index = 0;
                for(Song s: recList) {
                    final String dir = "C:\\Users\\cpgaf\\PycharmProjects\\ExpressiveAI\\rec";
                    s.writePIDI(dir + "\\javaOutput" + index + ".txt", false);
                    index++;
                }
                    /*try {
                        File outputFile = new File("C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\test.mid");
                        bf = new BufferedWriter(new FileWriter(outputFile));
                        for(Song s: recList) {
                            Parser.sequenceAndWrite(s, null, outputFile.getAbsolutePath(), true);
                        }
                        bf.close();
                    } catch (Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }*/
                /*recList = Parser.parseRec();
                //recList = Parser.multiply(recList);
                try {
                        File outputFile = new File("C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\out.txt");
                        bf = new BufferedWriter(new FileWriter(outputFile));
                        for(Song r: recList) {
                            rec.write(bf, true);
                        }
                        bf.close();
                    } catch (Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }  */
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
        final String input = "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\matchedNotes.txt";
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
            Parser.sequenceAndWrite(rec, recEvents, "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\recTest.mid", true);
            Parser.sequenceAndWrite(mus, musEvents, "C:\\Users\\cpgaf\\OneDrive\\Documents\\NetBeansProjects\\Expressive\\files\\musTest.mid", true);
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

    

}
