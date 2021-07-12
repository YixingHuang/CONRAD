package edu.stanford.rsl.tutorial.cone;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.conrad.utils.Configuration;
import ij.io.FileInfo;
import ij.io.FileOpener;

public class GenerateConeBeamReconstructionFromProjections {
	public int factor = 2; //image size factor
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
	public Grid3D sinogram;
	
	public static void main(String[] args) throws Exception {
		new ImageJ();
		boolean isProjectionRaw = true;
		String path = "D:\\wTVprocessedData\\"; //path for wTV data
		String savePathRecon = "D:\\Tasks\\FAU4\\TestRecon\\recon\\";
		String loadProjectionPath = "D:\\Tasks\\FAU4\\TestRecon\\projections\\";
		String projectionName0 = null;
		String projectionName, saveName1;
		GenerateConeBeamReconstructionFromProjections obj = new GenerateConeBeamReconstructionFromProjections(); 
		obj.initialGeometry();
	

		ImagePlus imp1, imp2;


		
		for(int i = 1; i <= 1; i++){

			obj.cbp=new ConeBeamProjector();
			obj.cbbp=new ConeBeamBackprojector();
			
			if(isProjectionRaw)
			{
				obj.getRawData(loadProjectionPath, projectionName0);
			}
			else
			{
				projectionName = loadProjectionPath + "projection" +i + ".tif";		
				imp1 = IJ.openImage(projectionName);
				obj.sinogram = ImageUtil.wrapImagePlus(imp1);
			}
			
			
		    //FDK reconstruction
			obj.FDKReconstruction(obj.sinogram);
			
			//save reconstruction volume
			imp1 = ImageUtil.wrapGrid(obj.reconCL, null);
			saveName1 = savePathRecon + "recon" +i + ".tif";
		    IJ.saveAs(imp1, "Tiff", saveName1);
		    
		    //release GPU OpenCL memory
			obj.reconCL.show("recon");
			obj.reconCL.release();
			
			System.out.println(i);
			
		}
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
	
	private void getRawData(String sinopath, String sinoname){
//		 String sinopath="D:\\tasks\\FAU1\\Research_Limited angle reconstruction\\TVresults\\MagdeburgBrain";
//		 String sinoname="projections.raw"; 
		FileInfo fi=new FileInfo();
		fi.width = 1240;
		fi.height = 960;
		fi.nImages = 496;
		fi.offset = 0;
		fi.intelByteOrder = true;
		fi.fileFormat = FileInfo.RAW;
		fi.fileType = FileInfo.GRAY32_FLOAT;
		fi.intelByteOrder = true; // little endian
		fi.directory = sinopath;
		fi.fileName = sinoname;
		FileOpener fopen = new FileOpener(fi);
		ImagePlus ProjectionImp = fopen.open(false);
		Grid3D sino=ImageUtil.wrapImagePlus(ProjectionImp);
		
		
		sinogram=(Grid3D)sino.clone();
		sinogram.show("loaded projection data");
		//sinoCL=new OpenCLGrid3D(sinogram);
	}
	
	public void FDKReconstruction(Grid3D sinogram){
		double focalLength = geo.getSourceToDetectorDistance();
		double deltaU = geo.getPixelDimensionX();
		double deltaV = geo.getPixelDimensionY();
		int numProjs = geo.getNumProjectionMatrices();	
		Grid3D sinogram2=new Grid3D(sinogram.getSize()[0],sinogram.getSize()[1], numProjs);
		ConeBeamCosineFilter cbFilter = new ConeBeamCosineFilter(focalLength, width, height, deltaU, deltaV);
		RamLakKernel ramK = new RamLakKernel(width, deltaU);
		//ParkerWeightingTool parker = new ParkerWeightingTool(geo);
		for (int i = 0; i < numProjs; ++i) 
			
		{
			//parker.setImageIndex(i);
			//parker.applyToolToImage(sinogram2.getSubGrid(i));
		
			sinogram2.setSubGrid(i, (Grid2D) sinogram.getSubGrid(i).clone());
			cbFilter.applyToGrid(sinogram2.getSubGrid(i));
			//ramp
			for (int j = 0;j <height; ++j)
				ramK.applyToGrid(sinogram2.getSubGrid(i).getSubGrid(j));
			System.out.print(i + " ");
		}
		System.out.println(" ");
		
		Grid3D reconFDK = cbbp.backprojectPixelDrivenCL(sinogram2);
		reconCL= new OpenCLGrid3D(reconFDK);

		//float scalCorrection = (float)( 260/(34.5*720000));
		//reconCL.getGridOperator().multiplyBy(reconCL, scalCorrection);
		//reconFDK = new Grid3D(reconCL);
		//reconFDK.show("FDK reconstruction");	
		
	}

	
}
