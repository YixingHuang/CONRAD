package edu.stanford.rsl.Yixing.Celphalometric;

	/*      This example of the use of both BiCubicInterpolation and BiCubicSpline
    demonstrates interpolation within a calculated data set
        y = x1*x2 + 2.x2^2
    allowing a comparison of the use of gradients calculated analytically
    and by numerical differentiation

    Michael Thomas Flanagan
    Created:    May 2003
    Updated:    1 May 2005, 10-11 January 2011
*/

import flanagan.interpolation.*;

public class BiCubicExampleOne{

    public static void main(String arg[]){

    // Array of x1
    double[] x1 = {0.0,	1.0, 2.0, 3.0, 4.0, 5.0};
    // Array of x2
    double[] x2 = {1.0, 5.0, 9.0, 13.0, 17.0, 21.0, 25.0, 29.0, 33.0, 37.0};

    double[][] y = new double[6][10];       // Array of y
    double[][] dy1 = new double[6][10];     // Array of gradients, dy/dx1
    double[][] dy2 = new double[6][10];     // Array of gradients, dy/dx2
    double[][] dy12 = new double[6][10];    // Array of gradients, d2y/dx1dx2
    double xx1 = 0.0;                       // x1 value at interpolation point
    double xx2 = 0.0;                       // x2 value at interpolation point
    double y1 = 0.0;                        // interpolated y value using BiCubicInterpolation with user supplied
                                            //   analytically calculated gradients
    double y2 = 0.0;                        // interpolated y value using BiCubicInterpolation with gradients obtained
                                            //   as numerical differences using a bicubic spline interpolation
    double y3 = 0.0;                        // interpolated y value using BiCubicInterpolation with gradients obtained
                                            //   as numerical differences using only the supplied data points
    double y4 = 0.0;                        // interpolated y value using BiCubicSpline
    double yt = 0.0;                        // true y value

    // Calculate the data
    for(int i=0; i<6; i++){
        for(int j=0; j<10; j++){
                y[i][j] = x1[i]*x2[j] + 2.0*x2[j]*x2[j] - 3.0*x1[i]*x1[i]*x2[j];
                dy1[i][j] = x2[j] - 6.0*x1[i]*x2[j];
                dy2[i][j] = x1[i] + 4.0*x2[j] - 3.0*x1[i]*x1[i];
                dy12[i][j] = 1.0 - 6.0*x1[i];

        }
    }

    // Create an instance of BiCubicInterpolation with supplied analytically determined derivatives
    BiCubicInterpolation bci1 = new BiCubicInterpolation(x1, x2, y, dy1, dy2, dy12);

    // Create an instance of BiCubicInterpolation with calculated derivatives using interpolation
    BiCubicInterpolation bci2 = new BiCubicInterpolation(x1, x2, y, 1);

    // Create an instance of BiCubicInterpolation with calculated derivatives using only the supplied data points
    BiCubicInterpolation bci3 = new BiCubicInterpolation(x1, x2, y, 0);

    // Create an instance of BiCubicSpline
    BiCubicSpline bcs = new BiCubicSpline(x1, x2, y);

    // First interpolation at a x1 = 2.5, x2 = 13.3
    xx1 = 2.5;
    xx2 = 13.3;
    yt = xx1*xx2 + 2.0*xx2*xx2 - 3.0*xx1*xx1*xx2;

    System.out.println("First interpolation at x1 = " + xx1 + " and x2 = " + xx2);
    System.out.println("The true y value is         " + yt);

    y1 = bci1.interpolate(xx1, xx2);
    System.out.println(" BiCubicInterpolation with supplied analytical gradients:");
    System.out.println(" The interpolated y value is " + y1);

    y2 = bci2.interpolate(xx1, xx2);
    System.out.println(" BiCubicInterpolation with numerical differencing and interpolation:");
    System.out.println(" The interpolated y value is " + y2);

    y3 = bci3.interpolate(xx1, xx2);
    System.out.println(" BiCubicInterpolation with numerical differencing of the supplied data points:");
    System.out.println(" The interpolated y value is " + y3);

    y4 = bcs.interpolate(xx1, xx2);
    System.out.println(" BiCubicSpline interpolation:");
    System.out.println(" The interpolated y value is " + y4);
    System.out.println(" ");

    // Second interpolation at a x1 = 3.3, x2 = 8.7
    xx1 = 3.3;
    xx2 = 8.7;
    yt = xx1*xx2 + 2.0*xx2*xx2 - 3.0*xx1*xx1*xx2;

    System.out.println("Second interpolation at x1 = " + xx1 + " and x2 = " + xx2);
    System.out.println("The true y value is         " + yt);

    y1 = bci1.interpolate(xx1, xx2);
    System.out.println(" BiCubicInterpolation with supplied analytical gradients:");
    System.out.println(" The interpolated y value is " + y1);

    y2 = bci2.interpolate(xx1, xx2);
    System.out.println(" BiCubicInterpolation with numerical differencing and interpolation:");
    System.out.println(" The interpolated y value is " + y2);

    y3 = bci3.interpolate(xx1, xx2);
    System.out.println(" BiCubicInterpolation with numerical differencing of the supplied data points:");
    System.out.println(" The interpolated y value is " + y3);

    y4 = bcs.interpolate(xx1, xx2);
    System.out.println(" BiCubicSpline interpolation:");
    System.out.println(" The interpolated y value is " + y4);
    System.out.println(" ");

    // Third interpolation at a x1 = 4.2, x2 = 36.1
    xx1 = 4.9;
    xx2 = 36.9;
    yt = xx1*xx2 + 2.0*xx2*xx2 - 3.0*xx1*xx1*xx2;

    System.out.println("Third interpolation at x1 = " + xx1 + " and x2 = " + xx2);
    System.out.println("The true y value is         " + yt);

    y1 = bci1.interpolate(xx1, xx2);
    System.out.println(" BiCubicInterpolation with supplied analytical gradients:");
    System.out.println(" The interpolated y value is " + y1);

    y2 = bci2.interpolate(xx1, xx2);
    System.out.println(" BiCubicInterpolation with numerical differencing and interpolation:");
    System.out.println(" The interpolated y value is " + y2);

    y3 = bci3.interpolate(xx1, xx2);
    System.out.println(" BiCubicInterpolation with numerical differencing of the supplied data points:");
    System.out.println(" The interpolated y value is " + y3);

    y4 = bcs.interpolate(xx1, xx2);
    System.out.println(" BiCubicSpline interpolation:");
    System.out.println(" The interpolated y value is " + y4);



 }
}
