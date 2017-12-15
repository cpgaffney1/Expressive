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
public class Prediction {
    int mIndex;
    long start;
    long length;
        public Prediction(int m, long s, long e) {
            mIndex = m;
            start = s;
            length = e;
        }
        public String toString() {
            return "mus index = " + mIndex + "; start = " + start + "; length = " + length;
        }
}
