package edu.stanford.rsl.sparseview;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

import java.io.File;

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

public class CreateFolders {
	
	
	public static void main(String[] args) throws Exception {
		new ImageJ();
		
		String mainFolder = "D:\\Tasks\\FAU4\\SparseViewCT\\Noisy3D\\30Degree\\";
		File outPutDir;
		String path;
		path = mainFolder + "DCR\\";
		outPutDir = new File(path);
		if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		}
		
		path = mainFolder + "projections\\";
		outPutDir = new File(path);
		if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		}
		
		path = mainFolder + "recon\\";
		outPutDir = new File(path);
		if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		}
		
		path = mainFolder + "reprojections\\";
		outPutDir = new File(path);
		if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		}
		
		path = mainFolder + "testData_d1\\";
		outPutDir = new File(path);
		if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		}
		
		path = mainFolder + "trainingData_d10\\";
		outPutDir = new File(path);
		if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		}
		
		path = mainFolder + "UNetRecons\\";
		outPutDir = new File(path);
		if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		}
		
		path = mainFolder + "wTV\\";
		outPutDir = new File(path);
		if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		}
		
    }
	
	
}
