package edu.stanford.rsl.Yixing.ERC;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.tutorial.cone.ConeBeamBackprojector;
import edu.stanford.rsl.tutorial.cone.ConeBeamCosineFilter;
import edu.stanford.rsl.tutorial.cone.ConeBeamProjector;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.tutorial.pwls.PenalizedWeightedLeastSquare;
import edu.stanford.rsl.conrad.phantom.NumericalSheppLogan3D;

public class WaterCylinder {
	protected int maxProjs;
	public int imgSizeX;
	public int imgSizeY;
	public int imgSizeZ;
	protected Trajectory geo = null;
	protected int width;
	protected int height;
	protected double spacingX;
	protected double spacingY;
	protected double spacingZ;
	protected double originX;
	protected double originY;
	protected double originZ;
	public ConeBeamProjector cbp;
	public ConeBeamBackprojector cbbp;
	
	public OpenCLGrid3D sinoCL, volCL, reconCL, artifactCL;
	public Grid3D sinogram, recon;
	
	public static void main(String[] args) throws Exception{
		new ImageJ();
		boolean isSave = false;
		WaterCylinder obj = new WaterCylinder();
//		Grid3D phan = obj.GetWaterCylinder(256,  256, 256);
		
		String reconPath = "G:\\ERC_Data\\XRM data for reconstruction\\load_Mausknochen_large_pillar\\recon_large_pillar.tif";
		ImagePlus imp2 =IJ.openImage(reconPath);
		Grid3D phan = ImageUtil.wrapImagePlus(imp2);
//		NumericalSheppLogan3D shepp = new NumericalSheppLogan3D(256, 256, 256);
//		Grid3D phan = shepp.getNumericalSheppLoganPhantom();
		phan.show("phan");

	    obj.initialGeometry();
	    obj.cbp=new ConeBeamProjector();
		obj.cbbp=new ConeBeamBackprojector();

		obj.volCL = new OpenCLGrid3D(phan);
		
		if(isSave)
		{
			obj.getMeasuredSinoCL();
			obj.sinogram = new Grid3D(obj.sinoCL);
			obj.sinoCL.release();
			
			
			ImagePlus imp1 = ImageUtil.wrapGrid(obj.sinogram, null);
			String saveName1 = "G:\\ERC_Data\\ParameterAdjust\\projection2.tif";
		    IJ.saveAs(imp1, "Tiff", saveName1);
		}
		else
		{
		    String saveName1 = "G:\\ERC_Data\\ParameterAdjust\\projection.tif";
		    ImagePlus imp0 =IJ.openImage(saveName1);
			obj.sinogram = ImageUtil.wrapImagePlus(imp0);
			
		}

		

		

		obj.FDKReconstruction();
		obj.recon.show("recon");
	}
	
	public Grid3D GetWaterCylinder(int nx, int ny, int nz)
	{
		Grid3D phan = new Grid3D(nx, ny, nz);
		int mz = nz/2;
		int r1;
		r1 = nx/4;
		int rr1 = r1 * r1;
		int rr2;
		int dd;
		for(int z = 1; z < (mz * 0.8f); z++)
		{
			rr2 = z * z;
			for(int i = -nx/2; i < nx/2; i++)
				for(int j = -ny/2; j < ny/2; j++)
				{
					dd = i * i + j * j;
					if(dd < rr1)
						phan.setAtIndex(nx/2 + i, ny/2 + j, mz - z, 1);
					if(dd < rr2)
						phan.setAtIndex(nx/2 + i, ny/2 + j, mz + z, 1);
				}
		}
		
		return phan;
	}
	
	public void initialGeometry() throws Exception {
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		geo = conf.getGeometry();


		width = geo.getDetectorWidth();
		height = geo.getDetectorHeight();
		
		// create context
		maxProjs = geo.getProjectionStackSize();
		imgSizeX = geo.getReconDimensionX();
		imgSizeY = geo.getReconDimensionY();
		imgSizeZ = geo.getReconDimensionZ();
		spacingX = geo.getVoxelSpacingX();
		spacingY = geo.getVoxelSpacingY();
		spacingZ = geo.getVoxelSpacingZ();
		originX = -geo.getOriginX();
		originY = -geo.getOriginY();
		originZ = -geo.getOriginZ();
		
	}
	
	public void FDKReconstruction(){
		double focalLength = geo.getSourceToDetectorDistance();
		double deltaU = geo.getPixelDimensionX();
		double deltaV = geo.getPixelDimensionY();
		int numProjs = geo.getNumProjectionMatrices();	
		Grid3D sinogram2=new Grid3D(sinogram.getSize()[0],sinogram.getSize()[1], numProjs);
		//ConeBeamCosineFilter cbFilter = new ConeBeamCosineFilter(focalLength, width, height, deltaU, deltaV);
		RamLakKernel ramK = new RamLakKernel(width, deltaU);
		//ParkerWeightingTool parker = new ParkerWeightingTool(geo);
		for (int i = 0; i < numProjs; ++i) 
			
		{
			//parker.setImageIndex(i);
			//parker.applyToolToImage(sinogram2.getSubGrid(i));
		
			sinogram2.setSubGrid(i, (Grid2D) sinogram.getSubGrid(i).clone());
		//	cbFilter.applyToGrid(sinogram2.getSubGrid(i));
			//ramp
			for (int j = 0;j <height; ++j)
				ramK.applyToGrid(sinogram2.getSubGrid(i).getSubGrid(j));
			System.out.print(i + " ");
		}
		System.out.println(" ");
		
		recon = cbbp.backprojectPixelDrivenCL(sinogram2);
	
		
	}
	
	public void getMeasuredSinoCL() throws Exception {
		sinoCL = new OpenCLGrid3D(new Grid3D(width, height, maxProjs));
		sinoCL.getDelegate().prepareForDeviceOperation();
		cbp.fastProjectRayDrivenCL(sinoCL, volCL);
		sinoCL.getDelegate().notifyDeviceChange();
		//sinoCL.show("sinoCL");
	}
	
}
