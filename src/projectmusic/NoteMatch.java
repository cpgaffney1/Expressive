/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectmusic;
import java.util.*;

/**
 * A NoteMatch stores one musNote which corresponds to a single note from the sheet music midi file.
 * It stores a list of potential Notes to match with from the recording midi file. 
 * When asked for a single match, it returns the most likely match Note.
 * @author cpgaffney1
 */
public class NoteMatch {
    
    private Note selfNote;
    private Set<Note> potentialMatches;
    private HashMap<Note, Integer> indexOfPotentialMatches;
    // key corresponds to a candidate note, stores percent same notes with the toMatch note match
    private HashMap<Note, Double> percentSameNotes;
    // same as above, except stores longest common substring
    private HashMap<Note, Double> lcs;
    
    public NoteMatch(Note selfNote) {
        assert(selfNote.isMus());
        this.selfNote = selfNote;
        potentialMatches = new HashSet<>();
        percentSameNotes = new HashMap<>();
        indexOfPotentialMatches = new HashMap<>();
        lcs = new HashMap<>();
    }
    
    public void addMatch(Note matchNote, int index) { 
        potentialMatches.add(matchNote); 
        indexOfPotentialMatches.put(matchNote, index);
    }
    //public void addRec(Collection<Note> notes) { potentialMatches.addAll(notes); }
    public void setPercentSameNotes(Note candidateNote, double d) { percentSameNotes.put(candidateNote, d); }
    public void setLcs(Note candidateNote, double len) { lcs.put(candidateNote, len); }
    
    public Note getSelf() { return selfNote; }
    public Set<Note> getPotentialMatches() { return potentialMatches; }
    public double getPercentSameNotes(Note n) { return percentSameNotes.get(n); }
    public double getLcs(Note n) {return lcs.get(n);}
    public int getMatchIndex(Note n) {return indexOfPotentialMatches.get(n);}
    
    // Ex: 0:1/12,3/10,18/4
    // musIndex:candidate1/LCS,...
    public String print() {
        String ret = "" + selfNote.getSongIndex() + ":";
        for(Note candidate: potentialMatches) {
            ret += candidate.getSongIndex() + "/" + lcs.get(candidate) + ",";
        }
        ret = ret.substring(0,ret.length()-1);
        return ret;
    }
    
    
    
}
