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
 *      This version copyright (c) The University of Edinburgh, 1999.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

public class JGFTimer {

  public String name; 
  public String opname; 
  public double time; 
  public double opcount; 
  public long calls; 
  public int size;

  private long start_time;
  private boolean on; 

  public JGFTimer(String name, String opname){
    this.size = -1;
    this.name = name;
    this.opname = opname;
    reset(); 
  }

  public JGFTimer(String name, String opname, int size){
    this.name = name;
    this.opname = opname;
    this.size = size;
    reset();
  }

  public JGFTimer(String name){
    this.name = name;
    this.opname = "";
    reset();
  }



  public void start(){
    if (on) System.out.println("Warning timer " + " was already turned on\n");
    on = true; 
    start_time = System.currentTimeMillis();
  }


  public void stop(){
    time += (double) (System.currentTimeMillis()-start_time) / 1000.;
    if (!on) System.out.println("Warning timer " + " wasn't turned on\n");
    calls++;
    on = false;  
  }

  public void addops(double count){
    opcount += count;
  } 

  public void addtime(double added_time){
    time += added_time;
  }

  public void reset(){
    time = 0.0; 
    calls = 0; 
    opcount = 0; 
    on = false;
  }

  public double perf(){
    return opcount / time; 
  }

  public void longprint(){
    System.out.println("Timer            Calls         Time(s)       Performance("+opname+"/s)\n");   
    System.out.println(name + "           " + calls +    "           "  +  (long)time + "        " + (long)this.perf() + "\n");
  }

  public void print(){
    if (opname.equals("")) {
      System.out.println(name + "   " + (long)time + " (s)\n");
    }
    else {
      if(size == 0) {
        System.out.println(name + ":SizeA" + "\t" + (long)time + " (s) \t " + (long)this.perf() + "\t" + " ("+opname+"/s)\n");
      } else if (size == 1) {
        System.out.println(name + ":SizeB" + "\t" + (long)time + " (s) \t " + (long)this.perf() + "\t" + " ("+opname+"/s)\n");
      } else if (size == 2) {
        System.out.println(name + ":SizeC" + "\t" + (long)time + " (s) \t " + (long)this.perf() + "\t" + " ("+opname+"/s)\n");
      } else{
        System.out.println(name + "\t" + (long)time + " (s) \t " + (long)this.perf() + "\t" + " ("+opname+"/s)\n");
      }
    }
  }


  public void printperf(){

    String name;
    name = this.name; 

    // pad name to 40 characters
    while ( name.length() < 40 ) name = name + " "; 

    System.out.println(name + "\t" + (long)this.perf() + "\t"
        + " ("+opname+"/s)\n");  
  }

}
