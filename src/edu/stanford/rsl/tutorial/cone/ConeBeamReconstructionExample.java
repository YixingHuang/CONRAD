package edu.stanford.rsl.tutorial.cone;


import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.phantom.NumericalSheppLogan3D;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.tutorial.cone.ConeBeamBackprojector;
import edu.stanford.rsl.tutorial.cone.ConeBeamCosineFilter;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;

/**
 * Simple example that computes and displays a cone-beam reconstruction.
 * 
 * @author Recopra Seminar Summer 2012
 * 
 */
public class ConeBeamReconstructionExample {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new ImageJ();
		
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();

		Trajectory geo = conf.getGeometry();
		double focalLength = geo.getSourceToDetectorDistance();
		int maxU_PX = geo.getDetectorWidth();
		int maxV_PX = geo.getDetectorHeight();
		int maxProjs = geo.getProjectionStackSize();
		double deltaU = geo.getPixelDimensionX();
		double deltaV = geo.getPixelDimensionY();
		double maxU = (maxU_PX) * deltaU;
		double maxV = (maxV_PX) * deltaV;
		int imgSizeX = geo.getReconDimensionX();
		int imgSizeY = geo.getReconDimensionY();
		int imgSizeZ = geo.getReconDimensionZ();
		//Phantom3D test3D = new Sphere3D(imgSizeX, imgSizeY, imgSizeZ);
		Grid3D test3D = new NumericalSheppLogan3D(imgSizeX,
				imgSizeY, imgSizeZ).getNumericalSheppLoganPhantom();
		// Alternate Phantom
		/*
		 * NumericalSheppLogan3D shepp3d = new NumericalSheppLogan3D(imgSizeX,
				imgSizeY, imgSizeZ);
		 */
		Grid3D grid = test3D;
		grid.show("object");

		OpenCLGrid3D gridCL = new OpenCLGrid3D(grid);
		ConeBeamProjector cbp =  new ConeBeamProjector();
		OpenCLGrid3D sinoCL = new OpenCLGrid3D(new Grid3D(maxU_PX, maxV_PX, maxProjs));
		sinoCL.getDelegate().prepareForDeviceOperation();		
		cbp.fastProjectRayDrivenCL(sinoCL, gridCL);	
		sinoCL.show("sinoCL");
		Grid3D sino = new Grid3D(sinoCL);
		sinoCL.release();
		gridCL.release();
//		Grid3D sino;
//		ConeBeamProjector cbp =  new ConeBeamProjector();
//		try {
//			sino = cbp.projectRayDrivenCL(grid);
//		} catch (Exception e) {
//			System.out.println(e);
//			return;
//		}
		
		
		ConeBeamCosineFilter cbFilter = new ConeBeamCosineFilter(focalLength, maxU, maxV, deltaU, deltaV);
		RamLakKernel ramK = new RamLakKernel(maxU_PX, deltaU);
		for (int i = 0; i < geo.getProjectionStackSize(); ++i) {
			cbFilter.applyToGrid(sino.getSubGrid(i));
			//ramp
			for (int j = 0;j <maxV_PX; ++j)
				ramK.applyToGrid(sino.getSubGrid(i).getSubGrid(j));
		}
		sino.show("sinoFilt");
			
		ConeBeamBackprojector cbbp = new ConeBeamBackprojector();
		Grid3D recImage = cbbp.backprojectPixelDrivenCL(sino);
		recImage.show("recImage");
		if (true)
			return;
	

	}
}
/*
 * Copyright (C) 2010-2014 Andreas Maier
 * CONRAD is developed as an Open Source project under the GNU General Public License (GPL).
*/