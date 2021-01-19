package edu.stanford.rsl.Yixing.truncationPix;

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

public class CopyTrainingData2Folders {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		CopyTrainingData2Folders obj = new CopyTrainingData2Folders();
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\pix2pixTrainingData_d10\\";
		String path2 = "D:\\Pix2pix\\tools\\truncation\\Noisy3D\\";
		ImagePlus imp1, imp2;
		String name1, name2, saveName1;
		Grid3D data, mask;
		Grid2D data2D, mask2D, merge2D;
		String folderName;
		int saveIndex, getIndex;
		File outPutDir;
		for(int idx = 1; idx<=18; idx ++){
			if(idx == 4) continue;
			
			folderName = path2 + "train" + idx + "\\";
			outPutDir = new File(folderName);
			if(!outPutDir.exists()){
			    outPutDir.mkdirs();
			}
			for(int idx2 = 1; idx2 <=18; idx2++)
			{
				if(idx2 == idx ) continue;
				for(int i=10; i < 256; i = i+10) {
					System.out.println( idx + ", " + i);
					
					getIndex = i;
			
					saveIndex = idx2*1000+getIndex;
					name1 = path + saveIndex + ".tif";
					imp1=IJ.openImage(name1);
					data2D = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
	
					saveName1 = folderName + saveIndex + ".tif";
					
				    imp1 = ImageUtil.wrapGrid(data2D, null);
				    IJ.saveAs(imp1, "Tiff", saveName1);
				}
			}
		}
			
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
