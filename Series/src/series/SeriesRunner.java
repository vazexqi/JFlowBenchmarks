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
 *                  Original version of this code by                       *
 *                 Gabriel Zachmann (zach@igd.fhg.de)                      *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

/**
 * Class SeriesTest
 *
 * Performs the transcendental/trigonometric portion of the
 * benchmark. This test calculates the first n fourier
 * coefficients of the function (x+1)^x defined on the interval
 * 0,2 (where n is an arbitrary number that is set to make the
 * test last long enough to be accurately measured by the system
 * clock). Results are reported in number of coefficients calculated
 * per sec.
 *
 * The first four pairs of coefficients calculated shoud be:
 * (2.83777, 0), (1.04578, -1.8791), (0.2741, -1.15884), and
 * (0.0824148, -0.805759).
 */

package series;

/**
 * A rewrite of the SeriesTest class following the code from the paper Bamboo: A Data-Centric,
 * Object-Oriented. Approach to Many-core Software
 * 
 * Main difference is that we retain the precision of using doubles instead of float (vs. the Bamboo
 * version); We also do not try to limit ourselves to only one exit point as done in Bamboo.
 * 
 * @author nchen
 * 
 */
public class SeriesRunner {

    int id;

    int range;

    public SeriesRunner(int id, int range) {
        this.id= id;
        this.range= range;
    }

    public void run() {
        double pair[][]= new double[2][range];
        int ilow, iupper;

        // Calculate the fourier series. Begin by calculating A[0].
        if (id == 0) {
            pair[0][0]= TrapezoidIntegrate(0.0, //Lower bound.
                    2.0, // Upper bound.
                    1000, // # of steps.
                    0.0, // No omega*n needed.
                    0) / 2.0; // 0 = term A[0].
        }

        // Calculate the fundamental frequency.
        // ( 2 * pi ) / period...and since the period
        // is 2, omega is simply pi.

        ilow= id * range;
        if (id == 0)
            ilow= 1; // Start with 1 not 0
        iupper= (id + 1) * range;

        for (int i= ilow; i < iupper; i++) {
            int j= i - id * range;
            // Calculate A[i] terms. Note, once again, that we
            // can ignore the 2/period term outside the integral
            // since the period is 2 and the term cancels itself
            // out.

            pair[0][j]= TrapezoidIntegrate(0.0,
                    2.0,
                    1000,
                    Math.PI * i,
                    1); // 1 = cosine term.

            // Calculate the B[i] terms.

            pair[1][j]= TrapezoidIntegrate(0.0,
                    2.0,
                    1000,
                    Math.PI * i,
                    2); // 2 = sine term.
        }

        // validate
        if (id == 0) {
            double ref[][]= { { 2.8729524964837996, 0.0 },
                    { 1.1161046676147888, -1.8819691893398025 },
                    { 0.34429060398168704, -1.1645642623320958 },
                    { 0.15238898702519288, -0.8143461113044298 } };
            for (int i= 0; i < 4; i++) {
                for (int j= 0; j < 2; j++) {
                    double error= Math.abs(pair[j][i] - ref[i][j]);
                    if (error > 1.0e-12) {
                        System.out.println("Validation failed for coefficient " + j + "," + id + "\n");
                        System.out.println("Computed value = " + (int)(pair[j][j] * 100000000) + "\n");
                        System.out.println("Reference value = " + (int)(ref[i][j] * 100000000) + "\n");
                    }
                }
            }
        }
    }

/*
* TrapezoidIntegrate
*
* Perform a simple trapezoid integration on the function (x+1)**x.
* x0,x1 set the lower and upper bounds of the integration.
* nsteps indicates # of trapezoidal sections.
* omegan is the fundamental frequency times the series member #.
* select = 0 for the A[0] term, 1 for cosine terms, and 2 for
* sine terms. Returns the value.
*/

    private double TrapezoidIntegrate(double x0, // Lower bound.
            double x1, // Upper bound.
            int nsteps, // # of steps.
            double omegan, // omega * n.
            int select) // Term type.
    {
        double x; // Independent variable.
        double dx; // Step size.
        double rvalue; // Return value.

        // Initialize independent variable.

        x= x0;

        // Calculate stepsize.

        dx= (x1 - x0) / (double)nsteps;

        // Initialize the return value.

        rvalue= thefunction(x0, omegan, select) / (double)2.0;

        // Compute the other terms of the integral.

        if (nsteps != 1)
        {
            --nsteps; // Already done 1 step.
            while (--nsteps > 0)
            {
                x+= dx;
                rvalue+= thefunction(x, omegan, select);
            }
        }

        // Finish computation.

        rvalue= (rvalue + thefunction(x1, omegan, select) / (double)2.0) * dx;
        return (rvalue);
    }

/*
* thefunction
*
* This routine selects the function to be used in the Trapezoid
* integration. x is the independent variable, omegan is omega * n,
* and select chooses which of the sine/cosine functions
* are used. Note the special case for select=0.
*/

    private double thefunction(double x, // Independent variable.
            double omegan, // Omega * term.
            int select) // Choose type.
    {

        // Use select to pick which function we call.

        switch (select) {
            case 0:
                return (Math.pow(x + (double)1.0, x));

            case 1:
                return (Math.pow(x + (double)1.0, x) * Math.cos(omegan * x));

            case 2:
                return (Math.pow(x + (double)1.0, x) * Math.sin(omegan * x));
        }

        // We should never reach this point, but the following
        // keeps compilers from issuing a warning message.

        return (0.0);
    }
}
