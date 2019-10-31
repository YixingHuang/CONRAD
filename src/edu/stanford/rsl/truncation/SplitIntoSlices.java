package edu.stanford.rsl.truncation;

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

public class SplitIntoSlices {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\recon\\";
		String path2 = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\trainingData_d10\\";
		String path3;
		ImagePlus imp1, imp2;
		String name1, name2, saveName1, saveName2;
		Grid3D data, mask;
		Grid2D data2D, mask2D;
		int saveIndex, getIndex;
		for(int idx = 1; idx<= 17; idx ++){
			name1 = path + "reconLimited" + idx + ".tif";
			//name2 = path + "reconGT" + idx + ".tif";
			name2 = path + "artifacts" + idx + ".tif";
			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
		    
			imp2=IJ.openImage(name2);
			mask = ImageUtil.wrapImagePlus(imp2);
			
			int num1 = data.getGridOperator().countInvalidElements(data);
			System.out.println(idx + ": " + num1);
			//path3 = path2 + idx + "\\";
			
			path3 = path2;
			for(int i = 1; i < 26; i++){
				System.out.println( idx + ", " + i);
				
				//getIndex = i*20+10;
				getIndex = i*10;
				saveIndex = idx*1000+getIndex;
				
				data2D = data.getSubGrid(getIndex);
				mask2D = mask.getSubGrid(getIndex);
				saveName1 = path3 + "data" + saveIndex + ".tif";
				saveName2 = path3 + "data" + saveIndex + "_mask.tif";
			
			    imp1 = ImageUtil.wrapGrid(data2D, null);
			    IJ.saveAs(imp1, "Tiff", saveName1);
			    imp2 = ImageUtil.wrapGrid(mask2D, null);
			    IJ.saveAs(imp2, "Tiff", saveName2);
		}
		
		}
		
		
	}
}
