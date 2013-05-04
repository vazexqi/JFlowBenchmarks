/* =============================================================================
 *
 * normal.java
 * -- Implementation of normal k-means clustering algorithm
 *
 * =============================================================================
 *
 * Author:
 *
 * Wei-keng Liao
 * ECE Department, Northwestern University
 * email: wkliao@ece.northwestern.edu
 *
 *
 * Edited by:
 *
 * Jay Pisharath
 * Northwestern University.
 *
 * Chi Cao Minh
 * Stanford University
 *
 * Alokika Dash
 * University of California, Irvine
 * Ported to Java
 *
 * =============================================================================
 *
 * For the license of bayes/sort.h and bayes/sort.c, please see the header
 * of the files.
 * 
 * ------------------------------------------------------------------------
 * 
 * For the license of kmeans, please see kmeans/LICENSE.kmeans
 * 
 * ------------------------------------------------------------------------
 * 
 * For the license of ssca2, please see ssca2/COPYRIGHT
 * 
 * ------------------------------------------------------------------------
 * 
 * For the license of lib/mt19937ar.c and lib/mt19937ar.h, please see the
 * header of the files.
 * 
 * ------------------------------------------------------------------------
 * 
 * For the license of lib/rbtree.h and lib/rbtree.c, please see
 * lib/LEGALNOTICE.rbtree and lib/LICENSE.rbtree
 * 
 * ------------------------------------------------------------------------
 * 
 * Unless otherwise noted, the following license applies to STAMP files:
 * 
 * Copyright (c) 2007, Stanford University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 * 
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 * 
 *     * Neither the name of Stanford University nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY STANFORD UNIVERSITY ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL STANFORD UNIVERSITY BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * =============================================================================
 */

public class Normal {
    int CHUNK;

    public Normal() {
        CHUNK= 3;
    }

    /*
     * ==========================================================================
     * === work
     * ==================================================================
     * ===========
     */

    private static float delta;

    public static void work(int myId, GlobalArgs globalArgs) {

        float[][] feature= globalArgs.feature;
        int nfeatures= globalArgs.nfeatures;
        int npoints= globalArgs.npoints;
        int nclusters= globalArgs.nclusters;
        int[] membership= globalArgs.membership;
        float[][] clusters= globalArgs.clusters;
        int[] new_centers_len= globalArgs.new_centers_len;
        float[][] new_centers= globalArgs.new_centers;
        delta= 0.0f;

        final int CHUNK= 500;

        try {

            for (int start= myId * CHUNK; start < npoints; start+= CHUNK) {
                // Begin Stage1
                int stop= (((start + CHUNK) < npoints) ? (start + CHUNK) : npoints);
                int indexArrayLen= stop - start;
                int indexArray[]= new int[indexArrayLen];
                int pidx= 0;
                for (int i1= start; i1 < stop; i1++) {
                    int index= Common.common_findNearestPoint(feature[i1], nfeatures, clusters, nclusters);
                    indexArray[pidx]= index;
                    pidx++;
                }
                // End Stage1

                // Begin Stage2
                int sidx= 0;
                for (int i2= start; i2 < stop; i2++) {

                    int newIndex= indexArray[sidx];
                    if (membership[i2] != newIndex) {
                        delta+= 1.0f;
                    }

                    membership[i2]= newIndex;
                    new_centers_len[newIndex]= new_centers_len[newIndex] + 1;

                    float[] tmpnew_centers= new_centers[newIndex];
                    float[] tmpfeature= feature[i2];

                    for (int j= 0; j < nfeatures; j++) {
                        tmpnew_centers[j]= tmpnew_centers[j] + tmpfeature[j];
                    }

                    sidx++;
                }

                if (start + CHUNK < npoints) {
                    globalArgs.global_i= start + CHUNK;
                }
                // End Stage2
            }
        } catch (Exception e) {

        }

        globalArgs.global_delta= globalArgs.global_delta + delta;
    }

    /*
     * ==========================================================================
     * === normal_exec
     * ==========================================================
     * ===================
     */
    public float[][] normal_exec(int nthreads, float[][] feature, /*
                                                                  * in:
                                                                  * [npoints][
                                                                  * nfeatures]
                                                                  */
            int nfeatures, int npoints, int nclusters, float threshold,
            int[] membership, Random randomPtr, /* out: [npoints] */
            GlobalArgs args) {
        float delta;
        float[][] clusters; /* out: [nclusters][nfeatures] */

        /* Allocate space for returning variable clusters[] */
        clusters= new float[nclusters][nfeatures];

        /* Randomly pick cluster centers */
        for (int i= 0; i < nclusters; i++) {
            int n= (int)(randomPtr.random_generate() % npoints);
            for (int j= 0; j < nfeatures; j++) {
                clusters[i][j]= feature[n][j];
            }
        }

        for (int i= 0; i < npoints; i++) {
            membership[i]= -1;
        }

        /*
         * Need to initialize new_centers_len and new_centers[0] to all 0.
         * Allocate clusters on different cache lines to reduce false sharing.
         */
        int[] new_centers_len= new int[nclusters];

        float[][] new_centers= new float[nclusters][nfeatures];

        int loop= 0;

        long start= System.currentTimeMillis();
        do {
            delta= 0.0f;

            args.feature= feature;
            args.nfeatures= nfeatures;
            args.npoints= npoints;
            args.nclusters= nclusters;
            args.membership= membership;
            args.clusters= clusters;
            args.new_centers_len= new_centers_len;
            args.new_centers= new_centers;

            args.global_i= nthreads * CHUNK;
            args.global_delta= delta;

            // Work in parallel with other threads
            thread_work(args);
            delta= args.global_delta;

            // put stall site here rather than having inside of loop
            nclusters= clusters.length;
            int newCenterLen= new_centers_len.length;
            int new_centersLen= new_centers.length;
            //

            /* Replace old cluster centers with new_centers */
            for (int i= 0; i < nclusters; i++) {
                for (int j= 0; j < nfeatures; j++) {
                    if (new_centers_len[i] > 0) {
                        clusters[i][j]= new_centers[i][j] / new_centers_len[i];
                    }
                    new_centers[i][j]= (float)0.0; /* set back to 0 */
                }
                new_centers_len[i]= 0; /* set back to 0 */
            }

            delta/= npoints;

        } while ((delta > threshold) && (loop++ < 500));
        long stop= System.currentTimeMillis();
        args.global_time+= (stop - start);

        return clusters;
    }

    /**
     * Work done by primary thread in parallel with other threads
     **/
    void thread_work(GlobalArgs args) {
        Normal.work(0, args); // threadId = 0 because primary thread
    }
}

/*
 * =============================================================================
 * 
 * End of normal.java
 * 
 * =============================================================================
 */


