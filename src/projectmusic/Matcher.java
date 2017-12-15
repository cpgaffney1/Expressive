/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectmusic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author cpgaffney1
 */
public class Matcher {

    private static final int sideOffset = 20;
    private static final int targetCountSurrounding = 5;
    private static final boolean print = false;

    public static List<NoteMatch> matchNotes(final Song mus, final Song rec) {
        List<NoteMatch> matches = new ArrayList<>();
        //initialize matches to contain each note index from mus - these are the ones we are trying to match
        for (Note n : mus) {
            matches.add(new NoteMatch(n));
        }
        
        /*matches = iterativeMatch(matches, rec, mus, true);
        matches = iterativeMatch(matches, rec, mus, false);
        matches = centerMatch(matches, rec, mus, true, true);
        matches = centerMatch(matches, rec, mus, false, true);*/
        matches = centerMatch(matches, rec, mus, true, false);

        int[] recArr = rec.toKeyArray();
        for (int i = 0; i < matches.size(); i++) {
            NoteMatch match = matches.get(i);
            Note s = match.getSelf();
            if (print) {
                System.out.print(Note.getNoteName(match.getSelf().getKey()) + " -- " + i + ": ");
            }
            if (match.getPotentialMatches().isEmpty()) {
                if (print) {
                    System.out.println("NONE");
                    System.out.print(mus.name);
                    assert(false);
                }
            } else {
                for (Note recNote : match.getPotentialMatches()) {
                    double percentSame = percentNotesSame(rec, mus, rec.indexOf(recNote), i);
                    match.setPercentSameNotes(recNote, percentSame);
                    if (print) {
                        System.out.print(rec.indexOf(recNote) + " (" + Math.round(percentSame * 100) + "%)" + " ");
                    }

                    List<HashSet<Integer>> verticalMusNoteNeighbors = mus.toVerticalList(i, sideOffset);
                    int recIndex = match.getMatchIndex(recNote);
                    int lh = recIndex - sideOffset;
                    int rh = recIndex + sideOffset;
                    int x = Math.max(0,lh) - Math.max(0,rh-rec.size());
                    int y = Math.min(rec.size(),rh) - Math.min(0,lh);
                    int[] recNoteNeighbors = Arrays.copyOfRange(recArr, Math.max(0,lh) - Math.max(0,rh-rec.size()), Math.min(rec.size(),rh) - Math.min(0,lh));
                    double lcs = lcs(recNoteNeighbors, verticalMusNoteNeighbors);
                    lcs /= 2 * sideOffset;
                    match.setLcs(recNote, lcs);
                    if (print) {
                        System.out.print("[LCS: " + lcs + "]   ");
                    }
                }
            }

            if (print) {
                System.out.println();
            }
        }
        return matches;
    }
    

    // Finds the perccentage of notes within a certain range surrounding a given note and compares to an equivalent
    // range surrounding the matched note in the otehr file
    private static double percentNotesSame(final Song rec, final Song mus, int recIndex, int musIndex) {
        List<Note> recFirst = rec.subList(Math.max(0, recIndex - sideOffset), recIndex);
        List<Note> recSecond = rec.subList(recIndex + 1, Math.min(rec.size(), recIndex + sideOffset));
        List<Note> musFirst = mus.subList(Math.max(0, musIndex - sideOffset), musIndex);
        List<Note> musSecond = mus.subList(musIndex + 1, Math.min(mus.size(), musIndex + sideOffset));
        final double total = musFirst.size() + musSecond.size();
        HashSet<Integer> markedIndices = new HashSet<>();
        double matchCount = 0;
        for (Note m : musFirst) {
            int r;
            for (r = 0; r < recFirst.size(); r++) {
                if (markedIndices.contains(r)) {
                    continue;
                }
                if (m.getKey() == recFirst.get(r).getKey()) {
                    matchCount++;
                    markedIndices.add(r);
                    break;
                }
            }
        }
        markedIndices.clear();
        for (Note m : musSecond) {
            int r;
            for (r = 0; r < recSecond.size(); r++) {
                if (markedIndices.contains(r)) {
                    continue;
                }
                if (m.getKey() == recSecond.get(r).getKey()) {
                    matchCount++;
                    markedIndices.add(r);
                    break;
                }
            }
        }
        return matchCount / total;
    }

    // Iterates through list of notes trying to match a note with the first unmatched occurrence of a rec note with the same pitch in the other
    // music file. Once a note is matched, it cannot match with another note --- Change that idea??
    private static List<NoteMatch> iterativeMatch(List<NoteMatch> matches, final Song rec, final Song mus, boolean forward) {
        HashSet<Integer> usedIndices = new HashSet<>();
        for (int musIndex = (forward ? 0 : mus.size() - 1); musIndex != (forward ? mus.size() : -1); musIndex += (forward ? 1 : -1)) {
            Note mNote = mus.get(musIndex);
            for (int recIndex = (forward ? 0 : rec.size() - 1); recIndex != (forward ? rec.size() : -1); recIndex += (forward ? 1 : -1)) {
                //System.out.println("\tSearching for " + Note.getNoteName(mNote.getKey()));
                Note rNote = rec.get(recIndex);
                if (rNote.getKey() == mNote.getKey() && !usedIndices.contains(recIndex)) {
                    //System.out.println("\tFound a " + Note.getNoteName(mNote.getKey()) + ", Difference = " + Math.abs(rNote.getStart()-161 - mNote.getStart()/2.6940146));
                    usedIndices.add(recIndex);
                    matches.get(musIndex).addMatch(rec.get(recIndex), recIndex);
                    break;
                }
            }
        }
        return matches;
    }

    // Expand outwards on either side from note we are trying to match, set boolean to indicate forward or backward
    // if oneToOne is false, then even if a recNote has already been matched with, it can be matched with again
    private static List<NoteMatch> centerMatch(List<NoteMatch> matches, final Song rec, final Song mus, boolean forward, boolean oneToOne) {
        HashSet<Integer> usedIndices = new HashSet<>();
        // If forward, outer loop proceeds forward
        for (int musIndex = (forward ? 0 : mus.size() - 1); musIndex != (forward ? mus.size() : -1); musIndex = musIndex + (forward ? 1 : -1)) {
            int foundMatchCount = 0;
            Note mNote = mus.get(musIndex);
            int rh = Math.min(musIndex, rec.size()-1);
            int lh = Math.min(musIndex - 1, rec.size() - 2);
            for(int z = 0; z < rec.size(); z++) {
                Note rNote;
                if (rh < rec.size()) {
                    rNote = rec.get(rh);
                    if (rNote.getKey() == mNote.getKey() && !usedIndices.contains(rh)) {
                        //System.out.println("\tFound a " + Note.getNoteName(mNote.getKey()) + ", Difference = " + Math.abs(rNote.getStart()-161 - mNote.getStart()/2.6940146));
                        if (oneToOne) {
                            usedIndices.add(rh);
                        }
                        matches.get(musIndex).addMatch(rec.get(rh), rh);
                        foundMatchCount++;
                        if (foundMatchCount == targetCountSurrounding) {
                            break;
                        }
                    }
                    rh++;
                }
                if (lh >= 0) {
                    rNote = rec.get(lh);
                    if (rNote.getKey() == mNote.getKey() && !usedIndices.contains(lh)) {
                        //System.out.println("\tFound a " + Note.getNoteName(mNote.getKey()) + ", Difference = " + Math.abs(rNote.getStart()-161 - mNote.getStart()/2.6940146));
                        if (oneToOne) {
                            usedIndices.add(lh);
                        }
                        matches.get(musIndex).addMatch(rec.get(lh), lh);
                        foundMatchCount++;
                        if (foundMatchCount == targetCountSurrounding) {
                            break;
                        }
                    }
                    lh--;
                }
            }
        }
        return matches;
    }

    // code inspired by Saket Kumar
    /* Returns length of LCS for X[0..m-1], Y[0..n-1] */
    private static int lcs(int[] recNoteNeighbors, List<HashSet<Integer>> verticalMusNoteNeighbors) {
        int m = recNoteNeighbors.length;
        int n = Song.verticalListSize(verticalMusNoteNeighbors);
        int L[][] = new int[m + 1][n + 1];

        /* Following steps build L[m+1][n+1] in bottom up fashion. Note
	     that L[i][j] contains length of LCS of X[0..i-1] and Y[0..j-1] */
        for (int i = 0; i <= m; i++) {
            int notesBeforeCurrSet = 0;
            int comparedNotes = 0;
            int verticalListIndex = 1;
            for (int j = 0; j <= n; j++) {
                if (i == 0 || j == 0) {
                    L[i][j] = 0;
                } else if (verticalMusNoteNeighbors.get(verticalListIndex - 1).contains(recNoteNeighbors[i - 1])) {
                    L[i][j] = L[i - 1][j - 1] + 1;
                    comparedNotes++;
                } else {
                    L[i][j] = Math.max(L[i - 1][j], L[i][j - 1]);
                    comparedNotes++;
                }
                if (notesBeforeCurrSet + verticalMusNoteNeighbors.get(verticalListIndex - 1).size() == comparedNotes) {
                    notesBeforeCurrSet += verticalMusNoteNeighbors.get(verticalListIndex - 1).size();
                    verticalListIndex++;
                }
            }
        }
        return L[m][n];
    }

}
