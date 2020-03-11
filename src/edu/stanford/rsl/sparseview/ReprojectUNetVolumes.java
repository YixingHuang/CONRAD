package edu.stanford.rsl.sparseview;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.phantom.NumericalSheppLogan3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.fan.CosineFilter;
import edu.stanford.rsl.tutorial.fan.FanBeamBackprojector2D;
import edu.stanford.rsl.tutorial.fan.FanBeamProjector2D;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.tutorial.cone.ConeBeamBackprojector;
import edu.stanford.rsl.tutorial.cone.ConeBeamCosineFilter;
import edu.stanford.rsl.tutorial.cone.ConeBeamProjector;
import edu.stanford.rsl.conrad.filtering.PoissonNoiseFilteringTool;
import edu.stanford.rsl.conrad.filtering.redundancy.ParkerWeightingTool;
import edu.stanford.rsl.tutorial.weightedtv.TVOpenCLGridOperators;

public class ReprojectUNetVolumes {
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
		

		String pathRecon = "C:\\Tasks\\FAU4\\SparseView\\90degree\\UNetRecons\\";
		String savePath = "C:\\Tasks\\FAU4\\SparseView\\90degree\\reprojections\\";
		String saveName1;
		ReprojectUNetVolumes obj = new ReprojectUNetVolumes(); 
		obj.initialGeometry();
		OpenCLGrid3D recon_no_leison;
		
		//Grid3D img = obj.getOriginalGroundTruthData(path, 7);
		//img.clone().show("img");
		
		ImagePlus imp1, imp2;
		boolean isTumor = false;
		boolean isNoisy = true;
		TVOpenCLGridOperators op = TVOpenCLGridOperators.getInstance();
		
	   String pathTemp;
	   Grid3D recon;
		for(int i = 18; i <= 18; i++){
			if(i == 4)
				continue;
		//int i = 1;
			obj.cbp=new ConeBeamProjector();
			obj.cbbp=new ConeBeamBackprojector();
			pathTemp = pathRecon + "UNetP" + i + ".tif";
			imp1 = IJ.openImage(pathTemp);
			recon = ImageUtil.wrapImagePlus(imp1);
			obj.volCL = new OpenCLGrid3D(recon);
			obj.volCL.getGridOperator().multiplyBy(obj.volCL, 0.07f);

			//obj.addTumors(obj.volCL);
			//obj.volCL.clone().show("volCL");
			obj.volCL.setSpacing(1.25, 1.25, 1);
			obj.volCL.setOrigin(obj.geo.getOriginX(), obj.geo.getOriginY(), obj.geo.getOriginZ());
			
			obj.getMeasuredSinoCL();

			obj.sinogram = new Grid3D(obj.sinoCL);
			obj.sinoCL.release();
			

			//obj.sinogram.clone().show("sinogram");
		    
			
			imp1 = ImageUtil.wrapGrid(obj.sinogram, null);
			saveName1 = savePath + "projection" +i + ".tif";
		    IJ.saveAs(imp1, "Tiff", saveName1);
			
			obj.volCL.release();

			
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
	
	
	
	public void getMeasuredSinoCL() throws Exception {
		sinoCL = new OpenCLGrid3D(new Grid3D(width, height, maxProjs));
		sinoCL.getDelegate().prepareForDeviceOperation();
		cbp.fastProjectRayDrivenCL(sinoCL, volCL);
		//sinoCL.show("sinoCL");
	}
	
	
	
	
	public Grid3D getOriginalGroundTruthData(String path, int ii){
		String pathTemp;
		ImagePlus imp;
	
		pathTemp = path + ii + ".tif";
		imp=IJ.openImage(pathTemp);
		Grid3D imgTemp = ImageUtil.wrapImagePlus(imp);
		
		
		return imgTemp;
	}
	
	
	
	
	public void saveTrainingData(String path, Grid3D reconGT, Grid3D reconLimited, Grid3D artifacts, int index){
		ImagePlus imp1,imp2, imp3;
		
			imp1 = ImageUtil.wrapGrid(reconGT, null);
			IJ.saveAs(imp1, "Tiff", (path + "reconGT" + index + ".tif"));
			imp2 = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(imp2, "Tiff", (path + "reconTruncated" + index + ".tif"));
			imp3 = ImageUtil.wrapGrid(artifacts, null);
			IJ.saveAs(imp3, "Tiff", (path + "artifacts" + index + ".tif"));
		
		
	}
	
	public void saveFullReconData(String path, Grid3D reconFull, int index){
		ImagePlus imp1;
		
			imp1 = ImageUtil.wrapGrid(reconFull, null);
			IJ.saveAs(imp1, "Tiff", (path + "reconFull" + index + ".tif"));
	}
}
