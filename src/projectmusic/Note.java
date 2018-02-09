/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectmusic;


/**
 *
 * @author cpgaffney1
 */
public class Note {
    
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    //Corresponding to piano keys 1 - 88
    private int key = 1;
    
    private String note = "A0";
    private int onVelocity = 60;
    private int offVelocity = 64;
    private int channel = 0;
    private boolean isMus;
    private int index;
    private int track;
    
    //Start and end in ticks from beginning
    private long start = -1;
    private long end = -1;
    private static final int MAX_VAL = 88;
    
    public Note() {}
       
    public Note(char pitch, boolean sharp, int octave, boolean mus) throws Exception {
        pitch = Character.toUpperCase(pitch);
        if(pitch < 'A' || pitch > 'G') throw new Exception("Invalid pitch");
        if(octave < 0 || octave > 8) throw new Exception("Invalid ocatve");
        int sharpVal = (sharp) ? 1 : 0;
        key = octave + pitch - 'A' + sharpVal;
        note = "" + pitch + ((sharp) ? "#" : "") + octave;
        this.isMus = mus;
    }
    
    public Note(int key, String note, boolean mus) throws Exception {
        if(key < 0) throw new Exception("Invalid value");
        this.key = key;
        this.note = note;
        this.isMus = mus;
    }
    
    public void setVelocity(int on, int off) {
        this.onVelocity = on;
        this.offVelocity = off;
    }
    
    public void setDuration(long start, long end){
        assert(start >= 0);
        assert(end >= 0);
        assert(end - start >= 0);
        this.start = start;
        this.end = end;
    }
    
    public void setTrack(int t) {track = t;}
    public void setIndex(int i){index = i;}
    public void setNote(String note) { this.note = note; }
    public void setKey(int key) { this.key = key; }
    public void setOnVelocity(int vel) { onVelocity = vel; }
    public void setOffVelocity(int vel) { offVelocity = vel; }
    public void setStart(long start) { 
        assert(start >=0 );
        this.start = start; 
    }
    public void setEnd(long end) { 
        assert(end > 0);
        assert(end - start >= 0);
        this.end = end; 
    }
    public void setChannel(int c) { this.channel = c; }

    
    public int getKey() { return key; }
    public boolean isMus() {return isMus;}
    public int getSongIndex() {return index;}
    public int getOnVelocity() { return onVelocity; }
    public int getOffVelocity() { return offVelocity; }
    public long getStart() { return start; }
    public long getEnd() { return end; }
    public int getChannel() { return channel; }

    
    public static String getNoteName(int key) {
        int octave = (key / 12)-1;
        int note = key % 12;
        String noteName = NOTE_NAMES[note];
        return noteName + octave;
    }
    
    public String toString() {
        assert(start >= 0);
        assert(end > 0);
        assert(end - start >= 0);
        if(end == start) System.out.println("End and start are the same");
        return "{" + key + "," + index + "," + onVelocity + "," + offVelocity + "," + start + "," + end + "," + track + "}";
    }
    
    public String readableToString(boolean on) {
        if(on) return "Note on @" + start + ", " + Note.getNoteName(key) + " key=" + key + " velocity: " + onVelocity + ", length= " + (end - start);
        else return "Note off @" + end + ", " + Note.getNoteName(key) + " key=" + key + " velocity: " + offVelocity;
    }
    
   

    /*
    public static Triple<char, boolean, int> split(String name) throws Exception {
        Triple<char, boolean, int> ret
        if(name.length() == 2) {
            this(name.charAt(0), 0, Integer.parseInt(name.substring(1)));
        } else if(name.length() == 3) {
            boolean sharp = false;
            if(name.charAt(1) == '#') sharp = true;
            Note(name.charAt(0), sharp, Integer.parseInt(name.substring(2)));
        } else throw new Exception("Invalid Note");
    }*/
}
