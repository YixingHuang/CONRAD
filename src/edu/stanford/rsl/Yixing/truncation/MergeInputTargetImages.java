package edu.stanford.rsl.Yixing.truncation;

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

public class MergeInputTargetImages {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		MergeInputTargetImages obj = new MergeInputTargetImages();
		String inputPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\trainingData_d10\\";
		String outputPath = "D:\\Pix2pix\\tools\\truncation\\trainPNG\\";
		String path3;
		ImagePlus imp1, imp2;
		String loadName1, loadName2, saveName;
		Grid3D data, mask;
		Grid2D data2D, mask2D;
		int saveIndex, getIndex;
		int outIndex = 1;
		Grid2D merge, merge2;
		for(int idx = 1; idx<= 18; idx ++){

			path3 = inputPath;
			for(int i = 1; i < 26; i++){
				System.out.println( idx + ", " + i);
				
				//getIndex = i*20+10;
				getIndex = i*10;
				saveIndex = idx*1000+getIndex;
				
				loadName1 = path3 + "data" + saveIndex + ".tif";
				loadName2 = path3 + "data" + saveIndex + "_mask.tif";
			
				imp1=IJ.openImage(loadName1);
				data2D = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
				imp1=IJ.openImage(loadName2);
				mask2D = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);

				merge = obj.mergeImages(data2D, mask2D);
				merge2 = obj.rescaleForPng(merge, -0.7f, 1.2f);
				saveName = outputPath + outIndex + ".png";
			    imp2 = ImageUtil.wrapGrid(merge, null);
			    IJ.saveAs(imp2, "png", saveName);
			    outIndex ++;
		}
		
		}
		
		
	}
	
	Grid2D rescaleForPng(Grid2D img, float min, float max)
	{
		Grid2D img2 = new Grid2D(img);
		img2.getGridOperator().subtractBy(img2, min);
		img2.getGridOperator().divideBy(img2, max - min);
		return img2;
	}
	
	Grid2D mergeImages(Grid2D data2D, Grid2D mask2D) {
		Grid2D merge = new Grid2D(512, 256);
		for(int i = 0; i < data2D.getSize()[0]; i++)
			for(int j = 0; j < data2D.getSize()[1]; j++)
			{
				merge.setAtIndex(i, j, data2D.getAtIndex(i, j));
				merge.setAtIndex(i + data2D.getSize()[0], j, mask2D.getAtIndex(i, j));
			}
		
		return merge;
	}
}
