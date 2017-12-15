/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectmusic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple container for a list of notes
 * @author cpgaffney1
 */
public class Song extends ArrayList<Note> {
    
    public String name;
    
    int[] toKeyArray() {
        int notes[] = new int[this.size()];
        for(int i = 0; i < this.size(); i++) {
            notes[i] = this.get(i).getKey();
        }
        return notes;
    }
    
    // if surrounding index is set to -1, returns vertical list of entire song
    public List<HashSet<Integer>> toVerticalList(int surroundingIndex, int sideOffset) {
        List<Note> copy;
        if(surroundingIndex == -1) copy = new ArrayList(this);
        else copy = new ArrayList(this.subList(Math.max(0, surroundingIndex - sideOffset), Math.min(this.size(), surroundingIndex + sideOffset)));
        List<HashSet<Integer>> result = new ArrayList<>();
        for(int i = 0; i < copy.size(); i++) {
            Note a = copy.get(0);
            HashSet<Integer> concurrentNotes = new HashSet<>();
            concurrentNotes.add(a.getKey());
            copy.remove(0);
            for(int j = 0; j < copy.size(); j++) {
                Note b = copy.get(j);
                if(a.getStart() == b.getStart()) {
                    concurrentNotes.add(b.getKey());
                    copy.remove(b);
                    j--;
                }
            }
            result.add(concurrentNotes);
        }
        return result;
    }
    
    public static int verticalListSize(List<HashSet<Integer>> list) {
        int count = 0;
        for(HashSet<Integer> set: list) {
            count += set.size();
        }
        return count;
    }
    
    public static void printVerticalList(List<HashSet<Integer>> list) {
        for (HashSet<Integer> set : list) {
            for(Integer key: set) {
                System.out.print(Note.getNoteName(key) + " ");
            }
            System.out.println();
        }
    }
    
    public void print() {
        for (Note n : this) {
            System.out.println(n.toString());
        }
    }
    
}
