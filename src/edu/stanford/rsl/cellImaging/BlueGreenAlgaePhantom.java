/**
 * 
 */
package edu.stanford.rsl.cellImaging;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;

/**
 * @author Yixing Huang
 * this is a blue-green algae phantom. It contains a cell wall, multiple cyanosomes inside the cell wall, and multiple gold nanoparticles outside the cell wall.
 * The sizes and positions of the cyanosomes and gold nanoparticles vary at each time, as a random function is used.
 *
 */
public class BlueGreenAlgaePhantom {

	private Grid3D image;
	private boolean init = false;
	private int nx = 0;
	private int ny = 0;
	private int nz = 0;
	private double wx, wy, wz;
	private double dx, dy, dz;
	private int numCyano = 10;
	private int numNano = 50; //number of gold nano-particles
	private int numPetal = 3;
	private double[][] ellipsoids;


	public BlueGreenAlgaePhantom( int maxI, int maxJ, int maxK, int numCyano, int numNano){
		
		initBlueGreenAlgae3D(maxI, maxJ, maxK, numCyano, numNano);
		
		if (nx == 0 || ny == 0 || nz == 0 ){
			System.out.println("Errpr: Wrong volume image size!");			
		}
		this.image = new Grid3D(nx, ny, nz);
		
	}
	
	
	public BlueGreenAlgaePhantom( Grid3D grid, int numCyano, int numNano){
		this.image = grid;
		initBlueGreenAlgae3D( image.getSize()[0], image.getSize()[1], image.getSize()[2], numCyano, numNano);
	}
	
	
	private void initBlueGreenAlgae3D( int maxI, int maxJ, int maxK, int numCyano, int numNano){

		this.nx = maxI;
		this.ny = maxJ;
		this.nz = maxK;
		wx = ( nx - 1 ) / 2.0 ;
		wy = ( ny - 1 ) / 2.0 ;
		wz = ( nz - 1 ) / 2.0 ;
		dx = 2.0 / ( nx - 1 );
		dy = 2.0 / ( ny - 1 );
		dz = 2.0 / ( nz - 1 );
		this.numCyano = numCyano;
		this.numNano = numNano;
		init = true;
	}

	private void computeNumericalPhantom( ){
		double x, y, z;

		double maxR = 0;
		double centX, centY, centZ;
		int maxXYZ = Math.max(Math.max(nx, ny), nz);
		
		double[][] RT; //transposed rotation matrices
		
		double signal;
		double[] p;
		double sum;
		int iix, iiy, iiz;
		Grid3D tag = new Grid3D(nx, ny, nz);
		for(int idx = 0; idx < numCyano + numNano + numPetal + 2; idx ++)
		{
			RT = transpose(dot(dot( this.Rx(ellipsoids[idx][6]), this.Ry(ellipsoids[idx][7])),this.Rz(ellipsoids[idx][8])));
			
			maxR = Math.max(Math.max(ellipsoids[idx][3], ellipsoids[idx][4]), ellipsoids[idx][5]) * maxXYZ / 2.0;
			centX = wx + nx / 2.0 * ellipsoids[idx][0];
			centY = wy + ny / 2.0 * ellipsoids[idx][1];
			centZ = wz + nz / 2.0 * ellipsoids[idx][2];
			for(int ix = - (int) maxR; ix <= maxR; ix++)
			{
				x = ix * dx;
				iix = ix + (int) centX;
				if(iix < 0 || iix >= nx)
					continue;
				for(int iy = - (int) maxR; iy <= maxR; iy++)
				{
					y = iy * dy;
					iiy = iy + (int) centY;
					if(iiy < 0 || iiy >= ny)
						continue;
					for(int iz = - (int) maxR; iz <= maxR; iz++)
					{						
						z = iz * dz;
						iiz = iz + (int) centZ;
						if(iiz < 0 || iiz >= nz || tag.getAtIndex(iix, iiy, iiz) == 1)
							continue;
						p = dot(RT,new double[] {x, y, z});
			            
			            sum = Math.pow(p[0]/ellipsoids[idx][3], 2) + Math.pow(p[1]/ellipsoids[idx][4], 2) + Math.pow(p[2]/ellipsoids[idx][5], 2);
			        
			            signal = (sum<=1.0)?ellipsoids[idx][9]:0;
			            image.setAtIndex(iix, iiy, iiz, image.getAtIndex(iix, iiy, iiz) + (float) signal);
			            if(idx >= 2 && sum <= 1.0)
			            	tag.setAtIndex(iix, iiy, iiz, 1);
					}
				}
			}
		}
		
	}
	
	public Grid3D getNumericalPhantom(){
		if( init) 
		{
			generateEllipsoids();
			computeNumericalPhantom();
		}
		return image;
	}
	
	public void resetVolume(){
		image.getGridOperator().fill(image, 0);
	}
	
	private void generateEllipsoids(){
		ellipsoids = new double[numCyano + numNano + numPetal + 3][10];
		double r = 0.6; //cell wall inner radius
		double r2 = r + 0.02; //cell wall outer radius
		double[][] cellWall =
				// { delta_x, delta_y, delta_z,        a,       b,       c,            phi,  theta,  psi,     rho }
				{  {    0,       0,       0,           r2,      r2,      r2,             0,      0,    0,     0.5 },
				{       0,       0,       0,           r,       r,       r,              0,      0,    0,   -0.4 }};
		
		System.arraycopy(cellWall[0], 0, ellipsoids[0], 0, 10);
		System.arraycopy(cellWall[1], 0, ellipsoids[1], 0, 10);
		int i = 0;
		double[][] temp = new double[1][10];
		double r3;
		i = 0;
		while(i < numPetal)
		{
			for(int j = 0; j < 3; j++)
			{
				temp[0][j] = (Math.random() - 0.5) * 1.8 * r;
				temp[0][j + 6] = (Math.random() - 0.5) * Math.PI; 
			}
			
			r3 = 0.3 + (Math.random() - 0.5) * 0.05;
			temp[0][3] = r3;
			temp[0][4] = 2*r3 + (Math.random() - 0.5) * 0.005;
			temp[0][5] = r3 + (Math.random() - 0.5) * 0.01;
			temp[0][9] = 0.5 + (Math.random() - 0.5) * 0.1;
			if(checkBoundary(temp, r, 0.005))
			{
				System.arraycopy(temp[0], 0, ellipsoids[i + 2], 0, 10);
				i++;
			}
		}
		
		i = 0;
		while(i < numCyano)
		{
			for(int j = 0; j < 3; j++)
			{
				temp[0][j] = (Math.random() - 0.5) * 1.8 * r;
				temp[0][j + 6] = (Math.random() - 0.5) * Math.PI; 
			}
			
			r3 = 0.05 + (Math.random() - 0.5) * 0.05;
			temp[0][3] = r3;
			temp[0][4] = r3 + (Math.random() - 0.5) * 0.005;
			temp[0][5] = r3 + (Math.random() - 0.5) * 0.01;
			temp[0][9] = 0.5 + (Math.random() - 0.5) * 0.1;
			if(checkBoundary(temp, r, 0.01))
			{
				System.arraycopy(temp[0], 0, ellipsoids[i + numPetal + 2], 0, 10);
				i++;
			}
		}
		
		i = 0;
		
		while(i < numNano)
		{
			for(int j = 0; j < 2; j++)
			{
				temp[0][j] = (Math.random() - 0.5) * 2;
				temp[0][j + 6] = (Math.random() - 0.5) * Math.PI; 
			}
			
			r3 = 0.02 + (Math.random() - 0.5) * 0.01;
			temp[0][3] = r3;
			temp[0][4] = r3 + (Math.random() - 0.5) * 0.02;
			temp[0][5] = r3 + (Math.random() - 0.5) * 0.02;
			temp[0][9] = 1.0 + (Math.random() - 0.5) * 0.3;
			if(checkNanoBoundary(temp, r2, 0))
			{
				System.arraycopy(temp[0], 0, ellipsoids[i + numCyano + numPetal + 2], 0, 10);
				i++;
			}
		}
	}
	
	private boolean checkBoundary(double [][] ellipsoid, double r, double tolerance)
	{

		double dd = 0;
		double d;
		for(int i = 0; i < 3; i++)
			dd += ellipsoid[0][i] * ellipsoid[0][i];
		d = Math.sqrt(dd);
		double maxR = Math.max(Math.max(ellipsoid[0][3], ellipsoid[0][4]), ellipsoid[0][5]);
		if(maxR + d >= r - tolerance)
			return false;
		else		
			return true;
	}
	
	private boolean checkNanoBoundary(double [][] ellipsoid, double r, double tolerance)
	{

		double dd = 0;
		double d;
		for(int i = 0; i < 3; i++)
			dd += ellipsoid[0][i] * ellipsoid[0][i];
		d = Math.sqrt(dd);
		double maxR = Math.max(Math.max(ellipsoid[0][3], ellipsoid[0][4]), ellipsoid[0][5]);
		if( d - maxR >= r + tolerance)
			return true;
		else		
			return false;
	}
	
//	 private static double[][] cellWall =
//				// { delta_x, delta_y, delta_z,        a,       b,       c,            phi,  theta,  psi,     rho }
//				{  {    0,       0,       0,           r2,      r2,      r2,             0,      0,    0,     1 },
//				{       0,       0,       0,           r,       r,       r,              0,      0,    0,   -0.8 }};
	 
	 
	 private double [] dot(double [][] one, double [] two){
	    	double [] vector = new double [one.length];
	    	for (int i= 0; i < one.length; i++){
	    		double sum = 0;
	    		for(int j=0;j < two.length; j++){
	    			sum += one[i][j] * two[j];
	    		}
	    		vector[i]=sum;
	    	}
	    	return vector;
	    }
	 
	  private double [] [] dot(double [][] one, double [][] two){
	    	return new Jama.Matrix(one).times(new Jama.Matrix(two)).getArray();
	    }
	   
     private double[][] Rx(double t){

         return new double[][] {{1, 0, 0}, {0, Math.cos(t), -Math.sin(t)}, {0, Math.sin(t), Math.cos(t)}};
         
     }
     
     private double[][] Ry(double t){

         return new double[][] {{Math.cos(t), 0, Math.sin(t)}, {0, 1, 0}, {-Math.sin(t), 0, Math.cos(t)}};
         
     }
     
     private double[][] Rz(double t){

         return new double[][]{{Math.cos(t), -Math.sin(t), 0}, {Math.sin(t), Math.cos(t), 0}, {0, 0, 1}};
         
     }
     
     private double [] [] transpose(double [] [] in){
     	return new Jama.Matrix(in).transpose().getArray();
     }
	
}
/*
 * Copyright (C) 2010-2014 Andreas Maier
 * CONRAD is developed as an Open Source project under the GNU General Public License (GPL).
*/