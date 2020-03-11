package edu.stanford.rsl.mar;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class SplitIntoSingleSlices {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Fau\\ws1920\\forschung\\projection\\";
		String path2 = "D:\\Fau\\ws1920\\forschung\\projectionSlices\\";
		String path3;
		ImagePlus imp1, imp2, imp3;
		String name1, name2, name3, saveName1, saveName2, saveName3;
		Grid3D data, mask, metal;
		Grid2D data2D, mask2D, metal2D;
		int saveIndex, getIndex;
		String path4, path5;
		File outPutDir;
		for(int idx = 1; idx<=1; idx ++){
			name1 = path + "projection" + idx + ".tif";
			name2 = path + "projection" + idx + "Metal.tif";
			name3 = path + "projection" + idx + "Mask.tif";
			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
			
			imp2 = IJ.openImage(name2);
			metal = ImageUtil.wrapImagePlus(imp2);
		
			imp3=IJ.openImage(name3);
			mask = ImageUtil.wrapImagePlus(imp3);
			

			path3 = path2 + idx + "\\";
			for(int i=0; i < data.getSize()[2]; i++) {
				System.out.println( idx + ", " + i);
				
				
				getIndex = i;
//				path4 = path3+ i +'\\';
				path4 = path3;
				saveIndex = idx*1000+getIndex;
				
				data2D = data.getSubGrid(getIndex);
				mask2D = mask.getSubGrid(getIndex);
				metal2D = metal.getSubGrid(getIndex);
				saveName1 = path4 + "data" + saveIndex + ".tif";
				saveName2 = path4 + "data" + saveIndex + "_mask.tif";
				saveName3 = path4 + "data" + saveIndex + "_metal.tif";
				outPutDir = new File(path4);
				if(!outPutDir.exists()){
				    outPutDir.mkdirs();
				}
				
			    imp1 = ImageUtil.wrapGrid(data2D, null);
			    IJ.saveAs(imp1, "Tiff", saveName1);
			    imp2 = ImageUtil.wrapGrid(mask2D, null);
			    IJ.saveAs(imp2, "Tiff", saveName2);
			    imp3 = ImageUtil.wrapGrid(metal2D, null);
			    IJ.saveAs(imp3, "Tiff", saveName3);
			}
			path5 = path2 + idx + "\\evaluation\\";
			outPutDir = new File(path5);
			if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		}
		}
		
		
	}
}
