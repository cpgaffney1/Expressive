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
import java.util.Random;

/**
 *
 * @author cpgaffney1
 */
public class Matcher {

    private static final int sideOffset = 12;
    private static final int targetCountSurrounding = 2;
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
                    int x = Math.max(0, lh) - Math.max(0, rh - rec.size());
                    int y = Math.min(rec.size(), rh) - Math.min(0, lh);
                    int[] recNoteNeighbors = Arrays.copyOfRange(recArr, Math.max(0, lh) - Math.max(0, rh - rec.size()), Math.min(rec.size(), rh) - Math.min(0, lh));
                    double lcs = lcs(recNoteNeighbors, verticalMusNoteNeighbors);
                    String recStr = "";
                    for (int j = 0; j < recNoteNeighbors.length; j++) {
                        recStr += (char) recNoteNeighbors[j];
                    }
                    String musStr = "";
                    for (HashSet<Integer> chord : verticalMusNoteNeighbors) {
                        for (int note : chord) {
                            musStr += (char) note;
                        }
                    }
                    int editDist = editDistance(musStr, recStr, musStr.length(), recStr.length());
                    /*
                    permute(verticalMusNoteNeighbors, "");
                    int min = Integer.MAX_VALUE;
                    for(String per: allPermutations) {
                        int editDist = editDistance(recStr, per, recStr.length(), per.length());
                        if(editDist < min) {
                            min = editDist;
                        }
                    }
                    min /= (2 * sideOffset);
                    match.setLcs(recNote, min);*/

                    lcs /= (2 * sideOffset);
                    match.setLcs(recNote, lcs);
                    editDist /= (2 * sideOffset);
                    match.setEditDist(recNote, editDist);
                    if (print) {
                        System.out.print("[LCS: " + lcs + "]");
                        System.out.print("[Edit Distance: " + editDist + "]   ");
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
            int rh = Math.min(musIndex, rec.size() - 1);
            int lh = Math.min(musIndex - 1, rec.size() - 2);
            for (int z = 0; z < rec.size(); z++) {
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

    private static int min(int x, int y, int z) {
        if (x <= y && x <= z) {
            return x;
        }
        if (y <= x && y <= z) {
            return y;
        } else {
            return z;
        }
    }

    private static int min(int x, int y) {
        if (x <= y) {
            return x;
        } else {
            return y;
        }
    }

    private static int editDistance(String str1, String str2, int m, int n) {
        // Create a table to store results of subproblems
        int dp[][] = new int[m + 1][n + 1];

        // Fill d[][] in bottom up manner
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                // If first string is empty, only option is to
                // isnert all characters of second string
                if (i == 0) {
                    dp[i][j] = j;  // Min. operations = j
                } // If second string is empty, only option is to
                // remove all characters of second string
                else if (j == 0) {
                    dp[i][j] = i; // Min. operations = i
                } // If last characters are same, ignore last char
                // and recur for remaining string
                else if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } // If last character are different, consider all
                // possibilities and find minimum
                else {
                    dp[i][j] = 1 + min(dp[i][j - 1], // Insert
                            dp[i - 1][j], // Remove
                            dp[i - 1][j - 1]); // Replace
                    if (i > 1 && j > 1 && str1.charAt(i - 1) == str2.charAt(j - 2) && str1.charAt(i - 2) == str2.charAt(j - 1)) {
                        dp[i][j] = min(dp[i][j], dp[i - 2][j - 2] + 1);  // transposition
                    }
                }
            }
        }

        return dp[m][n];
    }

    static int factorial(int n) {
        if (n == 0) {
            return 1;
        } else {
            return (n * factorial(n - 1));
        }
    }

    private static HashSet<String> allPermutations = new HashSet<>();

    private static void permute(List<HashSet<Integer>> verticalMusNoteNeighbors, String curPermutation) {
        if (verticalMusNoteNeighbors.isEmpty()) {
            allPermutations.add(curPermutation);
            return;
        }
        HashSet<Integer> chord = verticalMusNoteNeighbors.remove(0);
        String chordStr = "";
        for (final int note : chord) {
            chordStr += (char) note;
        }
        permuteVertical(chordStr, 0, chordStr.length() - 1);
        HashSet<String> curChordPermutations = new HashSet<>(chordPermutations);
        for (final String str : curChordPermutations) {
            permute(verticalMusNoteNeighbors, curPermutation + str);
        }
        verticalMusNoteNeighbors.add(0, chord);
    }

    /**
     * permutation function
     *
     * @param str string to calculate permutation for
     * @param l starting index
     * @param r end index
     */
    private static HashSet<String> chordPermutations = new HashSet<>();

    private static void permuteVertical(String str, int l, int r) {
        if (l == r) {
            chordPermutations.add(str);
        } else {
            for (int i = l; i <= r; i++) {
                str = swap(str, l, i);
                permuteVertical(str, l + 1, r);
                str = swap(str, l, i);
            }
        }
    }

    /**
     * Swap Characters at position
     *
     * @param a string value
     * @param i position 1
     * @param j position 2
     * @return swapped string
     */
    private static String swap(String a, int i, int j) {
        char temp;
        char[] charArray = a.toCharArray();
        temp = charArray[i];
        charArray[i] = charArray[j];
        charArray[j] = temp;
        return String.valueOf(charArray);
    }

    private static int randElem(HashSet<Integer> set) {

        int size = set.size();
        int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
        int i = 0;
        for (Integer obj : set) {
            if (i == item) {
                return obj;
            }
            i++;
        }
        assert (false);
        return -1;
    }

}
