package edu.illinois.jflow.benchmark;
/**************************************************************************
 *                                                                         *
 *             Java Grande Forum Benchmark Suite - Version 2.0             *
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
 *      Original version of this code by Hon Yau (hwyau@epcc.ed.ac.uk)     *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 1999.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/
import java.util.Vector;

/**
 * Code, a test-harness for invoking and driving the Applications Demonstrator classes.
 * 
 * <p>
 * To do:
 * <ol>
 * <li>Very long delay prior to connecting to the server.</li>
 * <li>Some text output seem to struggle to get out, without the user tapping ENTER on the keyboard!
 * </li>
 * </ol>
 * 
 * @author H W Yau
 * @version $Revision: 1.3 $ $Date: 2011/06/01 23:48:35 $
 */
public class AppDemo extends Universal {
    // ------------------------------------------------------------------------
    // Class variables.
    // ------------------------------------------------------------------------

    public static double JGFavgExpectedReturnRateMC;

    public static boolean DEBUG;

    protected static String prompt;

    public static int Serial;

    // ------------------------------------------------------------------------
    // Instance variables.
    // ------------------------------------------------------------------------
    /**
     * Name of the historical rate to model.
     */
    private String dataFilename;

    /**
     * The number of time-steps which the Monte Carlo simulation should run for.
     */
    private int nTimeStepsMC;

    /**
     * The number of Monte Carlo simulations to run.
     */
    private int nRunsMC;

    /**
     * The default duration between time-steps, in units of a year.
     */
    private double dTime;

    /**
     * Flag to determine whether initialisation has already taken place.
     */
    private boolean initialised;

    /**
     * Variable to determine which deployment scenario to run.
     */
    private int runMode;

    private Vector tasks;

    // private Vector results;

    private int workload;

    public AppDemo(String dataFilename, int nTimeStepsMC, int nRunsMC, int workload) {

        JGFavgExpectedReturnRateMC= 0.0;
        DEBUG= true;
        prompt= "AppDemo> ";
        Serial= 1;

        dTime= 1.0 / 365.0;
        initialised= false;

        pathStartValue= 100.0;
        avgExpectedReturnRateMC= 0.0;
        avgVolatilityMC= 0.0;

        initAllTasks= null;

        this.dataFilename= dataFilename;
        this.nTimeStepsMC= nTimeStepsMC;
        this.nRunsMC= nRunsMC;
        this.initialised= false;
        this.workload= workload;
    }

    /**
     * Single point of contact for running this increasingly bloated class. Other run modes can
     * later be defined for whether a new rate should be loaded in, etc. Note that if the
     * <code>hostname</code> is set to the string "none", then the demonstrator runs in purely
     * serial mode.
     */

    /**
     * Initialisation and Run methods.
     */

    PriceStock psMC;

    double pathStartValue;

    double avgExpectedReturnRateMC;

    double avgVolatilityMC;

    ToInitAllTasks initAllTasks;

    public void initSerial() {
        //
        // Measure the requested path rate.
        RatePath rateP= new RatePath(dataFilename);
        rateP.dbgDumpFields();
        ReturnPath returnP= rateP.getReturnCompounded();
        returnP.estimatePath();
        returnP.dbgDumpFields();
        double expectedReturnRate= returnP.get_expectedReturnRate();
        double volatility= returnP.get_volatility();
        //
        // Now prepare for MC runs.
        initAllTasks= new ToInitAllTasks(returnP, nTimeStepsMC, pathStartValue);
        String slaveClassName= "MonteCarlo.PriceStock";
        //
        // Now create the tasks.
        initTasks(nRunsMC);
        //

    }

    // Made avgExpectedReturnRateMC2 into a field so that we can overcome the limitation of Java
    // anonymous inner classes not being able to reference non-final local variables
    private double avgExpectedReturnRateMC2;

    public void runSerial() throws Exception {
        avgExpectedReturnRateMC2= 0.0;

        // Create an instance of a RatePath, for accumulating the results of the
        // Monte Carlo simulations.
        RatePath avgMCrate= new RatePath(nTimeStepsMC, "MC", 19990109, 19991231, dTime);

        for (int iRun= 0; iRun < nRunsMC; iRun= iRun + workload) {
            // Begin Stage1
            int ilow= iRun;
            int iupper= iRun + workload;
            if (iupper > nRunsMC) {
                iupper= nRunsMC;
            }

            int l_size= iupper - ilow;
            PriceStock psArray[]= new PriceStock[l_size];

            for (int idx= ilow; idx < iupper; idx++) {
                PriceStock ps= new PriceStock();
                ps.setInitAllTasks(initAllTasks);
                ps.setTask(tasks.elementAt(idx));
                ps.run();
                psArray[idx - ilow]= ps;
            }
            // End Stage1

            // Begin Stage2
            for (int idx= ilow; idx < iupper; idx++) {
                ToResult returnMC= (ToResult)psArray[idx - ilow].getResult();
                avgMCrate.inc_pathValue(returnMC.get_pathValue());
                avgExpectedReturnRateMC2+= returnMC.get_expectedReturnRate();
                avgVolatilityMC+= returnMC.get_volatility();
            }
            // End Stage2

        }
        avgMCrate.inc_pathValue((double)1.0 / ((double)nRunsMC));
        avgExpectedReturnRateMC2/= nRunsMC;
        avgVolatilityMC/= nRunsMC;
        JGFavgExpectedReturnRateMC= avgExpectedReturnRateMC2;
    }

    // ------------------------------------------------------------------------idx
    /**
     * Generates the parameters for the given Monte Carlo simulation.
     * 
     * @param nRunsMC the number of tasks, and hence Monte Carlo paths to produce.
     */
    private void initTasks(int nRunsMC) {
        tasks= new Vector(nRunsMC);
        for (int i= 0; i < nRunsMC; i++) {
            String header= "MC run " + String.valueOf(i);
            ToTask toTask= new ToTask(header, (long)i * 11);
            tasks.addElement((Object)toTask);
        }
    }

    /**
     * Method for doing something with the Monte Carlo simulations. It's probably not mathematically
     * correct, but shall take an average over all the simulated rate paths.
     * 
     * @exception DemoException thrown if there is a problem with reading in any values.
     */
    private void processResults() {
        /*
         * double avgExpectedReturnRateMC = 0.0; double avgVolatilityMC = 0.0;
         * double runAvgExpectedReturnRateMC = 0.0; double runAvgVolatilityMC = 0.0;
         * ToResult returnMC; if (nRunsMC != results.size()) {errPrintln(
         * "Fatal: TaskRunner managed to finish with no all the results gathered in!"
         * ); System.exit(-1); } // // Create an instance of a RatePath, for
         * accumulating the results of the // Monte Carlo simulations. RatePath
         * avgMCrate = new RatePath(nTimeStepsMC, "MC", 19990109, 19991231, dTime);
         * for (int i = 0; i < nRunsMC; i++) { // First, create an instance which is
         * supposed to generate a // particularly simple MC path. returnMC =
         * (ToResult) results.elementAt(i);
         * avgMCrate.inc_pathValue(returnMC.get_pathValue());
         * avgExpectedReturnRateMC += returnMC.get_expectedReturnRate();
         * avgVolatilityMC += returnMC.get_volatility(); runAvgExpectedReturnRateMC
         * = avgExpectedReturnRateMC / ((double) (i + 1)); runAvgVolatilityMC =
         * avgVolatilityMC / ((double) (i + 1)); } // for i;
         * avgMCrate.inc_pathValue((double) 1.0 / ((double) nRunsMC));
         * avgExpectedReturnRateMC /= nRunsMC; avgVolatilityMC /= nRunsMC;
         * JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;
         */
    }

    //
    // ------------------------------------------------------------------------
    // Accessor methods for class AppDemo.
    // Generated by 'makeJavaAccessor.pl' script. HWY. 20th January 1999.
    // ------------------------------------------------------------------------
    /**
     * Accessor method for private instance variable <code>dataFilename</code>.
     * 
     * @return Value of instance variable <code>dataFilename</code>.
     */
    public String get_dataFilename() {
        return (this.dataFilename);
    }

    /**
     * Set method for private instance variable <code>dataFilename</code>.
     * 
     * @param dataFilename the value to set for the instance variable <code>dataFilename</code>.
     */
    public void set_dataFilename(String dataFilename) {
        this.dataFilename= dataFilename;
    }

    /**
     * Accessor method for private instance variable <code>nTimeStepsMC</code>.
     * 
     * @return Value of instance variable <code>nTimeStepsMC</code>.
     */
    public int get_nTimeStepsMC() {
        return (this.nTimeStepsMC);
    }

    /**
     * Set method for private instance variable <code>nTimeStepsMC</code>.
     * 
     * @param nTimeStepsMC the value to set for the instance variable <code>nTimeStepsMC</code>.
     */
    public void set_nTimeStepsMC(int nTimeStepsMC) {
        this.nTimeStepsMC= nTimeStepsMC;
    }

    /**
     * Accessor method for private instance variable <code>nRunsMC</code>.
     * 
     * @return Value of instance variable <code>nRunsMC</code>.
     */
    public int get_nRunsMC() {
        return (this.nRunsMC);
    }

    /**
     * Set method for private instance variable <code>nRunsMC</code>.
     * 
     * @param nRunsMC the value to set for the instance variable <code>nRunsMC</code>.
     */
    public void set_nRunsMC(int nRunsMC) {
        this.nRunsMC= nRunsMC;
    }

    /**
     * Accessor method for private instance variable <code>tasks</code>.
     * 
     * @return Value of instance variable <code>tasks</code>.
     */
    public Vector get_tasks() {
        return (this.tasks);
    }

    /**
     * Set method for private instance variable <code>tasks</code>.
     * 
     * @param tasks the value to set for the instance variable <code>tasks</code>.
     */
    public void set_tasks(Vector tasks) {
        this.tasks= tasks;
    }

    // ------------------------------------------------------------------------
}
