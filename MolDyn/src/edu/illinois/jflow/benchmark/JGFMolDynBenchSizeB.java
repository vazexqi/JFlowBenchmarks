package edu.illinois.jflow.benchmark;
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
public class JGFMolDynBenchSizeB { 

    public static void main(String argv[]){
    int nthreads;
    int workload;
    if(argv.length != 0 ) {
      nthreads = Integer.parseInt(argv[0]);
      workload=Integer.parseInt(argv[1]);
    } else {
      System.out.println("The no of threads has not been specified, defaulting to 1\n");
      System.out.println("  " + "\n");
      nthreads = 1;
      workload=300;
    }

    JGFMolDynBench mold;
    mold = new JGFMolDynBench(nthreads,workload); 
    int size = 1;

    mold.JGFsetsize(size); 


    JGFMolDynBench tmp;
    mold.JGFinitialise(); 
    JGFMolDynBench.JGFapplication(mold); 

    /* Validate data */
    double[] refval = new double[2];
    refval[0] = 1731.4306625334357;
    refval[1] = 7397.392307839352;
    double dval;
    dval = mold.ek[0];
    double dev = Math.abs(dval - refval[size]);
    long l = (long) refval[size] *1000000;
    long r = (long) dval * 1000000;
    if (l != r ){
      System.out.println("Validation failed\n");
      System.out.println("Kinetic Energy = " + (long)dval + "  " + (long)dev + "  " + size + "\n");
    }else{
      System.out.println("VALID\n");
    }

        System.out.println("Finished\n");
  }
}

