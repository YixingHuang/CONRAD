/**
 * 
 */
package edu.stanford.rsl.cellImaging;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.phantom.SheppLogan3D;

/**
 * @author febmeng
 *
 */
public class BlueGreenAlgaePhantom {

	boolean init = false;
	int nx = 0;
	int ny = 0;
	int nz = 0;
	double wx, wy, wz;
	double dx, dy, dz;
	Grid3D image;
	SheppLogan3D phan;
	int numCyano = 10;
	double[][] ellipsoids;

	public BlueGreenAlgaePhantom( int maxI, int maxJ, int maxK, int numCyano ){
		
		initBlueGreenAlgae3D(maxI, maxJ, maxK, numCyano);
		
		if (nx == 0 || ny == 0 || nz == 0 ){
			System.out.println("Errpr: Wrong volume image size!");			
		}
		this.image = new Grid3D(nx, ny, nz);
		
	}
	
	
	public BlueGreenAlgaePhantom( Grid3D grid, int numCyano ){
		this.image = grid;
		initBlueGreenAlgae3D( image.getSize()[0], image.getSize()[1], image.getSize()[2], numCyano);
	}
	
	
	void initBlueGreenAlgae3D( int maxI, int maxJ, int maxK, int numCyano){

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
		generateEllipsoids();
		phan = new SheppLogan3D(this.ellipsoids);
		init = true;
	}

	void computeNumericalPhantom( ){
		
		//System.out.print("nx = " + nx + ", ny = " + ny + ", nz = " + nz + " .\n");
		//System.out.print("dx = " + dx + ", dy = " + dy + ", dz = " + dz + " .\n");
		//System.out.print("wx = " + wx + ", wy = " + wy + ", wz = " + wz + " .\n");
		
		for( int ix = 0; ix < nx; ix++ ){
			
			double x = ( ix - wx ) * dx ;
			
			for( int iy = 0; iy < ny; iy++ ){
				
				double y = ( iy - wy ) * dy ;
				
				for( int iz = 0; iz < nz; iz++ ){
					
					double z = ( iz - wz ) * dz ;
					
					image.setAtIndex(ix, iy, iz, (float)phan.ImageDomainSignal(x, y, z));
					
				}
			}
		}
		
	}
	
	public Grid3D getNumericalPhantom(){
		if( init) 
			computeNumericalPhantom();
		return image;
	}
	
	
	private void generateEllipsoids(){
		ellipsoids = new double[numCyano + 2][10];
		double r = 0.78;
		double r2 = r + 0.02;
		double[][] background = 	 
			// { delta_x, delta_y, delta_z,        a,       b,       c,            phi,  theta,  psi,     rho }
			{  {    0,       0,       0,           r2,      r2,      r2,             0,      0,    0,     1 },
			{       0,       0,       0,           r,       r,       r,              0,      0,    0,   -0.8 }};
		
		System.arraycopy(background[0], 0, ellipsoids[0], 0, 10);
		System.arraycopy(background[1], 0, ellipsoids[1], 0, 10);
		int i = 0;
		double[][] temp = new double[1][10];
		double r3;
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
			temp[0][9] = 0.1 + (Math.random() - 0.5) * 0.1;
			if(checkBoundary(temp, r))
			{
				System.arraycopy(temp[0], 0, ellipsoids[i+2], 0, 10);
				i++;
			}
		}	
	}
	
	private boolean checkBoundary(double [][] ellipsoid, double r)
	{
//		for(int i = 0; i < 3; i++)
//			if(Math.abs(ellipsoid[0][i]) + ellipsoid[0][i] >= r - 0.01)
//			{
//				return false;
//			}
		double dd = 0;
		double d;
		for(int i = 0; i < 3; i++)
			dd += ellipsoid[0][i] * ellipsoid[0][i];
		d = Math.sqrt(dd);
		double maxR = Math.max(Math.max(ellipsoid[0][3], ellipsoid[0][4]), ellipsoid[0][5]);
		if(maxR + d >= r - 0.01)
			return false;
		else		
			return true;
	}
	
}
/*
 * Copyright (C) 2010-2014 Andreas Maier
 * CONRAD is developed as an Open Source project under the GNU General Public License (GPL).
*/