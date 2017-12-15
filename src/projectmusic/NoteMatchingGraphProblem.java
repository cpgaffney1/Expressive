///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package projectmusic;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.PriorityQueue;
//import java.util.Vector;
//import java.lang.Integer;
//
///**
// *
// * @author cpgaffney1
// */
//public class NoteMatchingGraphProblem {
//
//    // for states, mus notes are assigned even numbers starting at zero, rec notes odd numbers starting at 1
//    private Song mus;
//    private Song rec;
//    private List<NoteMatch> matches;
//
//    public NoteMatchingGraphProblem(Song mus, Song rec, List<NoteMatch> matches) {
//        this.mus = mus;
//        this.rec = rec;
//        this.matches = matches;
//    }
//
//    private int getStart() {
//        return 0;
//    }
//
//    private boolean isEnd(int st, int matched) {
//        return st % 2 == 1 && matched == mus.size();
//    }
//
//    private HashSet<Integer> getSucc(int st) {
//        assert (st % 2 == 0);
//        HashSet<Integer> successors = new HashSet<>();
//        for (Note n : matches.get(st / 2).getPotentialMatches()) {
//            assert (!n.isMus());
//            successors.add(n.getSongIndex() * 2 + 1);
//        }
//        return successors;
//    }
//
//    // only valid when succ is a rec (candidate) note. succ corresponds to a state
//    // musIndex corresponds to an index of a mus note, not a state
//    private int getCost(int musIndex, int succ) {
//        assert (succ % 2 == 1);
//        int lcs = matches.get(musIndex).getLcs(rec.get(succ / 2));
//        return lcs;
//    }
//
//    public void search() {
//        List<List<Integer>> actions = null;
//        int totalCost = 0;
//        int numStatesExplored = 0;
//        Frontier frontier = new Frontier();
//        HashMap<Integer, List<Integer>> backpointers = new HashMap<>();
//        int start = getStart();
//        //frontier.add(start);
//
//        while (true) {
//            
//            
//        }
//
//    }
//
//    private class Frontier {
//
//        Heap heap = new Heap();
//        //from state to prioritiy
//        HashMap<Integer, Integer> priorities = new HashMap<>();
//        
//
//        public boolean update(int state, int newPriority) {
//            Integer oldPriority = priorities.get(state);
//            if (oldPriority == null || newPriority < oldPriority) {
//                priorities.put(state, newPriority);
//                List<Integer> pair = new ArrayList<>();
//                pair.add(state);
//                pair.add(newPriority);
//                //heap.insert(pair);
//            }
//        }
//    }
//
//    private class Heap {
//
//        private static final int CAPACITY = 2;
//
//        private int size;            // Number of elements in heap
//        private List<Integer>[] heap;     // The heap array
//
//        public Heap() {
//            size = 0;
//                heap = new List<Integer>[CAPACITY];
//        }
//
//        /**
//         * Construct the binary heap given an array of items.
//         */
//        public Heap(int[] array) {
//            size = array.length;
//            heap = array;
//
//            System.arraycopy(array, 0, heap, 1, array.length);//we do not use 0 index
//
//            buildHeap();
//        }
//
//        /**
//         * runs at O(size)
//         */
//        private void buildHeap() {
//            for (int k = size / 2; k > 0; k--) {
//                percolatingDown(k);
//            }
//        }
//
//        private void percolatingDown(int k) {
//            int tmp = heap[k];
//            int child;
//
//            for (; 2 * k <= size; k = child) {
//                child = 2 * k;
//
//                if (child != size
//                        && heap[child] - (heap[child + 1]) > 0) {
//                    child++;
//                }
//
//                if (tmp - (heap[child]) > 0) {
//                    heap[k] = heap[child];
//                } else {
//                    break;
//                }
//            }
//            heap[k] = tmp;
//        }
//
//        /**
//         * Sorts a given array of items.
//         */
//        public void heapSort(int[] array) {
//            size = array.length;
//            heap = new int[size+1];
//            System.arraycopy(array, 0, heap, 1, size);
//            buildHeap();
//
//            for (int i = size; i > 0; i--) {
//                int tmp = heap[i]; //move top item to the end of the heap array
//                heap[i] = heap[1];
//                heap[1] = tmp;
//                size--;
//                percolatingDown(1);
//            }
//            for (int k = 0; k < heap.length - 1; k++) {
//                array[k] = heap[heap.length - 1 - k];
//            }
//        }
//
//        /**
//         * Deletes the top item
//         */
//        public int deleteMin() throws RuntimeException {
//            if (size == 0) {
//                throw new RuntimeException();
//            }
//            int min = heap[1];
//            heap[1] = heap[size--];
//            percolatingDown(1);
//            return min;
//        }
//
//        /**
//         * Inserts a new item
//         */
//        public void insert(int x) {
//            if (size == heap.length - 1) {
//                doubleSize();
//            }
//
//            //Insert a new item to the end of the array
//            int pos = ++size;
//
//            //Percolate up
//            for (; pos > 1 && x - heap[pos/2] < 0; pos = pos / 2) {
//                heap[pos] = heap[pos / 2];
//            }
//
//            heap[pos] = x;
//        }
//
//        private void doubleSize() {
//            int[] old = heap;
//            heap = new int[heap.length * 2];
//            System.arraycopy(old, 1, heap, 1, size);
//        }
//
//        public String toString() {
//            String out = "";
//            for (int k = 1; k <= size; k++) {
//                out += heap[k] + " ";
//            }
//            return out;
//        }
//    }
//
//    }
