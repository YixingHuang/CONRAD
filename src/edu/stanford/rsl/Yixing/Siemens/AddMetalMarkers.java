/**
 * 
 */
package edu.stanford.rsl.Yixing.Siemens;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;

/**
 * @author Yixing Huang
 * this function adds metal ellipsoid markers.
 *
 */
public class AddMetalMarkers {

	private Grid3D image;
	private boolean init = false;
	private int nx = 0;
	private int ny = 0;
	private int nz = 0;
	private double wx, wy, wz;
	private double dx, dy, dz;
	private int numMarkers = 50; //the numer of markers near the body surface
	private int numMarkers2 = 50; // the number of markers inside the body
	private double[][] ellipsoids;


	public AddMetalMarkers( int maxI, int maxJ, int maxK, int numMarkers, int numMarker2){
		
		initBlueGreenAlgae3D(maxI, maxJ, maxK, numMarkers, numMarker2);
		
		if (nx == 0 || ny == 0 || nz == 0 ){
			System.out.println("Errpr: Wrong volume image size!");			
		}	
		this.image = new Grid3D(nx, ny, nz);
	}
	
	
	public AddMetalMarkers( Grid3D image, int numMarkers, int numMarker2){
		this.image = image;
		initBlueGreenAlgae3D( image.getSize()[0], image.getSize()[1], image.getSize()[2], numMarkers, numMarker2);
	}
	
	
	private void initBlueGreenAlgae3D( int maxI, int maxJ, int maxK, int numMarkers, int numMarker2){

		this.nx = maxI;
		this.ny = maxJ;
		this.nz = maxK;
		wx = ( nx - 1 ) / 2.0 ;
		wy = ( ny - 1 ) / 2.0 ;
		wz = ( nz - 1 ) / 2.0 ;
		dx = 2.0 / ( nx - 1 );
		dy = 2.0 / ( ny - 1 );
		dz = 2.0 / ( nz - 1 );
		this.numMarkers = numMarkers;
		this.numMarkers2 = numMarkers2;
		init = true;
	}
	
	private void generateEllipsoids(){
		ellipsoids = new double[numMarkers + numMarkers2][10];
		double r = 0.5; //cell wall inner radius


		double[][] temp = new double[1][10];
		double r3;
		int i = 0;
		while(i < numMarkers)
		{
			for(int j = 0; j < 3; j++)
			{
				temp[0][j] = (Math.random() - 0.5) * 1.8 * r;
				temp[0][j + 6] = (Math.random() - 0.5) * Math.PI; 
			}
			temp[0][0] = (Math.random() - 0.5) * 0.05 - 0.0677; // overwrite x position
			temp[0][2] = (Math.random() - 0.5) * 1.99;// overwrite Z position
			
			r3 = 0.006 + (Math.random() - 0.5) * 0.004;
			temp[0][3] = r3;
			temp[0][4] = r3 + (Math.random() - 0.5) * 0.002;
			temp[0][5] = r3 + (Math.random() - 0.5) * 0.002;
			temp[0][9] = (0.5 + (Math.random() - 0.5) * 0.2) * 57142.0; //57142 = 2000/0.035, as scale factor to HU 
			if(checkBoundary(temp, r, 0.01))
			{
				System.arraycopy(temp[0], 0, ellipsoids[i], 0, 10);
				i++;
			}
		}
		i = 0;
		while(i < numMarkers2)
		{
			for(int j = 0; j < 3; j++)
			{
				temp[0][j] = (Math.random() - 0.5) * 1.8 * r;
				temp[0][j + 6] = (Math.random() - 0.5) * Math.PI; 
			}
			temp[0][0] = (Math.random() - 0.5) * 0.5 + 0.25; // overwrite x position
			temp[0][2] = (Math.random() - 0.5) * 1.99;// overwrite Z position
			
			r3 = 0.006 + (Math.random() - 0.5) * 0.004;
			temp[0][3] = r3;
			temp[0][4] = r3 + (Math.random() - 0.5) * 0.002;
			temp[0][5] = r3 + (Math.random() - 0.5) * 0.002;
			temp[0][9] = (0.5 + (Math.random() - 0.5) * 0.2) * 57142.0; //57142 = 2000/0.035, as scale factor to HU 
			if(checkBoundary(temp, r, 0.01))
			{
				System.arraycopy(temp[0], 0, ellipsoids[i + numMarkers], 0, 10);
				i++;
			}
		}
		
	}

	private void computeNumericalMarkers( ){
		double x, y, z;

		double maxR = 0;
		double centX, centY, centZ;
		int maxXYZ = Math.max(Math.max(nx, ny), nz);
		
		double[][] RT; //transposed rotation matrices
		
		double signal = 0;
		double[] p;
		double sum;
		int iix, iiy, iiz;
		for(int idx = 0; idx < numMarkers + numMarkers2; idx ++)
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
						if(iiz < 0 || iiz >= nz)
							continue;
						p = dot(RT, new double[] {x, y, z});
			            
			            sum = Math.pow(p[0]/ellipsoids[idx][3], 2) + Math.pow(p[1]/ellipsoids[idx][4], 2) + Math.pow(p[2]/ellipsoids[idx][5], 2);
			     
			            signal = (sum<=1.0)?ellipsoids[idx][9]:0;
			            image.setAtIndex(iix, iiy, iiz, image.getAtIndex(iix, iiy, iiz) + (float) signal);
					}
				}
			}
		}
		
	}
	
	public Grid3D addMarkers(Grid3D image){
		if( init) 
		{
			this.image = (Grid3D)image.clone();
			generateEllipsoids();
			computeNumericalMarkers();
		}
		else
		{
			System.out.println("Not Initialized");
		}
		return this.image;
	}
	
	public Grid3D getMarkers(){
		if( init) 
		{
			generateEllipsoids();
			computeNumericalMarkers();
			
		}
		else
		{
			System.out.println("Not Initialized");
		}
		return this.image;
	}
	
	public void resetVolume(){
		image.getGridOperator().fill(image, 0);
	}
	

	
	private boolean checkBoundary(double [][] ellipsoid, double r, double tolerance)
	{

		double dd = 0;
		double d;
		for(int i = 0; i < 2; i++)
			dd += ellipsoid[0][i] * ellipsoid[0][i];
		d = Math.sqrt(dd);
		double maxR = Math.max(ellipsoid[0][3], ellipsoid[0][4]);
		if(maxR + d >= r - tolerance)
			return false;
		else		
			return true;
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
