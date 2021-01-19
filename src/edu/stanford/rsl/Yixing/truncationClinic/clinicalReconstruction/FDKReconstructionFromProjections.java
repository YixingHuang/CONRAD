package edu.stanford.rsl.Yixing.truncationClinic.clinicalReconstruction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
import ij.io.FileOpener;
import edu.stanford.rsl.conrad.numerics.SimpleMatrix;
import edu.stanford.rsl.conrad.geometry.Projection;
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
import edu.stanford.rsl.conrad.filtering.redundancy.TrajectoryParkerWeightingTool;
import edu.stanford.rsl.tutorial.weightedtv.TVOpenCLGridOperators;

import java.io.BufferedWriter;

import edu.stanford.rsl.Yixing.truncation.WaterCylinderExtrapolation2DFan;

public class FDKReconstructionFromProjections {
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
	protected float radius = 288.0f;
	public ConeBeamProjector cbp;
	public ConeBeamBackprojector cbbp;
	private TVOpenCLGridOperators op;
	
	public OpenCLGrid3D sinoCL, volCL, reconCL, artifactCL;
	public Grid3D sinogram, reconFDK;
	
	public static void main(String[] args) throws Exception {
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicProjectionComb\\"; //path for wTV data
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicRecons\\";

		FDKReconstructionFromProjections obj = new FDKReconstructionFromProjections(); 
		obj.initialGeometry();

		
		ImagePlus imp1;
		float numTrunc = 320;
		obj.op = TVOpenCLGridOperators.getInstance();
		float xsinc = 0.04f;
		WaterCylinderExtrapolation2DFan wceObj = new WaterCylinderExtrapolation2DFan(obj.height, (int)numTrunc, xsinc);
		String nameProj;
		for(int i = 6; i <= 6; i++){
			obj.cbbp=new ConeBeamBackprojector();
			nameProj = path + "projection" + i + ".tif";
//			nameProj = path + "projectionCrop1.tif";
			imp1 =IJ.openImage(nameProj);
			obj.sinogram = ImageUtil.wrapImagePlus(imp1);
			obj.FDKReconstruction();
			imp1 = ImageUtil.wrapGrid(obj.reconFDK, null);
			IJ.saveAs(imp1, "Tiff", (savePath + "FDKscale" + i+ ".tif"));
			System.out.println(i);
			
		}
    }
	

	
	public void initialGeometry() throws Exception {
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		geo = conf.getGeometry();
//		getProjectionMatrix();
		
		
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
		
		System.out.println(geo.getNumProjectionMatrices());
		SimpleVector spacingUV = new SimpleVector(geo.getPixelDimensionX(), geo.getPixelDimensionY());
		double[] sdd = geo.getProjectionMatrices()[0].computeSourceToDetectorDistance(spacingUV);
		geo.setSourceToDetectorDistance(sdd[0]);
		SimpleVector sp = geo.getProjectionMatrices()[0].computeCameraCenter();
		double sid = sp.normL2();
		geo.setSourceToAxisDistance(sid);
		
		radius=(float)(radius*0.4/spacingX);
		
	}
	
	private void getProjectionMatrix() throws IOException{
		 String Rawpath="D:\\Tasks\\FAU1\\Research_LimitedAngleReconstruction\\TVresults\\MagdeburgBrain";
		 String Rawname="ProjMatrix.bin";
		
		 boolean isSave = true;
		String outName = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\projMatrx.txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(outName));
			
			
		FileInfo fi = new FileInfo();
		fi.width = 3;
		fi.height = 4;
		fi.nImages = 496;
		fi.offset = 6;
		fi.intelByteOrder = true;
		fi.fileFormat = FileInfo.RAW;
		fi.fileType = FileInfo.GRAY64_FLOAT;
		fi.directory = Rawpath;
		fi.fileName = Rawname;
		FileOpener fopen = new FileOpener(fi);
		ImagePlus ProjMatrixImp = fopen.open(true);
		Grid3D ProjGrid3D = ImageUtil.wrapImagePlus(ProjMatrixImp);
		Projection[] projMats = new Projection[ProjGrid3D.getSize()[2]];
       
		SimpleMatrix tempMat = new SimpleMatrix(ProjGrid3D.getSize()[0], ProjGrid3D.getSize()[1]);
		System.out.println(ProjGrid3D.getSize()[0] + " " + ProjGrid3D.getSize()[1]);
		
		for(int i = 0; i  <ProjGrid3D.getSize()[2]; i++){
			if(isSave)
			{
				int ii = i + 1;
				float a = 0;
				bw.write("@" + ii+"\r\n");
				bw.write(a + " " + a +"\r\n");
			}
			for(int k = 0; k < ProjGrid3D.getSize()[0]; k++)
			{
				for(int m = 0; m < ProjGrid3D.getSize()[1]; m++) {
					tempMat.setElementValue(k, m, ProjGrid3D.getSubGrid(i).getAtIndex(k, m));			
//			        projMats[i] = new Projection();
//			        projMats[i].initFromP(tempMat);
			        if(isSave)
			        {
			        	bw.write(String.format("%6.10f", ProjGrid3D.getSubGrid(i).getAtIndex(k, m)) + " ");
			        }
				}
				if(isSave)
					bw.write("\r\n");
			}
			if(isSave)
				bw.write("\r\n");
		}
		if(isSave)
		{
			bw.flush();
			bw.close();
		}
		geo.setNumProjectionMatrices(ProjGrid3D.getSize()[2]);
		
		geo.setProjectionMatrices(projMats);
		System.out.println("SAD = " + geo.getSourceToAxisDistance());
		System.out.println("SDD = " + geo.getSourceToDetectorDistance());
		
		
	}
	
	public void FDKReconstruction() throws Exception{
		double focalLength = geo.getSourceToDetectorDistance();
		double deltaU = geo.getPixelDimensionX();
		double deltaV = geo.getPixelDimensionY();
		int numProjs = geo.getNumProjectionMatrices();	
		Grid3D sinogram2=new Grid3D(sinogram.getSize()[0],sinogram.getSize()[1], numProjs);
		ConeBeamCosineFilter cbFilter = new ConeBeamCosineFilter(focalLength, width, height, deltaU, deltaV);
		RamLakKernel ramK = new RamLakKernel(width, deltaU);
		TrajectoryParkerWeightingTool tparker = new TrajectoryParkerWeightingTool();
		tparker.configure();
	//	ParkerWeightingTool parker = new ParkerWeightingTool(geo);
		Grid2D sino2D, sino2Dfiltered;
		double[] angles = tparker.getPrimaryAngles();
		for(int i = 0; i < angles.length; i++)
			System.out.print(angles[i] + " ");
		System.out.println(" ");
		for (int i = 0; i < numProjs; i++) 
			
		{
			sino2D = (Grid2D) sinogram.getSubGrid(i).clone();
			tparker.setImageIndex(i);
			sino2Dfiltered = tparker.applyToolToImage(sino2D);
			cbFilter.applyToGrid(sino2Dfiltered);
			//ramp
			for (int j = 0;j <height; ++j)
				ramK.applyToGrid(sino2Dfiltered.getSubGrid(j));
			sinogram2.setSubGrid(i, (Grid2D)sino2Dfiltered.clone());
			System.out.print(i + " ");
		}
		System.out.println(" ");

		reconFDK = cbbp.backprojectPixelDrivenCL(sinogram2);
		//reconFDK.getGridOperator().divideBy(reconFDK, 0.07f);
		reconCL = new OpenCLGrid3D(reconFDK);
		reconCL.getGridOperator().divideBy(reconCL, 0.07f);
		op.maskFOV(reconCL, radius);
		reconFDK = new Grid3D(reconCL);
		reconCL.release();
	
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
