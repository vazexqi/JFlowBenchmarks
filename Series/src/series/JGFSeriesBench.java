/**************************************************************************
 *                                                                         *
 *         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
 *                                                                         *
 *                            produced by                                  *
 *                                                                         *
 *                  Java Grande Benchmarking Project                       *
 *                                                                         *
 *                                at                                       *
 *                                                                         *
 *                Edinburgh Parallel Computing Centre                      *
 *                                                                         * 
 *                email: epcc-javagrande@epcc.ed.ac.uk                     *
 *                                                                         *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/


package series;

/**
 * A rewrite of the SeriesTest class following the code from the paper Bamboo: A Data-Centric,
 * Object-Oriented. Approach to Many-core Software
 * 
 * @author nchen
 * 
 */
public class JGFSeriesBench {
    static int datasizes[]= { 10000, 100000, 1000000 };

    public static void main(String[] args) {
        int datasize= datasizes[1];
        int threadnum= 4;
        int range= datasize / threadnum;
        for (int i= 0; i < threadnum; ++i) {
            SeriesRunner sr= new SeriesRunner(i, range);
            sr.run();
        }
    }
}
