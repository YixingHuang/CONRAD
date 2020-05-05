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

public class SplitIntoSingleSlicesGAN {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		SplitIntoSingleSlicesGAN obj = new SplitIntoSingleSlicesGAN();
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\recon\\";
		String path2 = "D:\\Pix2pix\\tools\\truncation\\valP18\\";
		ImagePlus imp1, imp2;
		String name1, name2, saveName1;
		Grid3D data, mask;
		Grid2D data2D, mask2D, merge2D;
		int saveIndex, getIndex;
	
		for(int idx = 18; idx<=18; idx ++){
			name1 = path + "reconLimited" + idx + ".tif";
			//name2 = path + "reconGT" + idx + ".tif";
			name2 = path + "artifacts" + idx + ".tif";
			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
		
			imp2=IJ.openImage(name2);
			mask = ImageUtil.wrapImagePlus(imp2);
			
			for(int i=0; i < data.getSize()[2]; i++) {
				System.out.println( idx + ", " + i);
				
				getIndex = i;
		
				saveIndex = idx*1000+getIndex;
				
				data2D = data.getSubGrid(getIndex);
				mask2D = mask.getSubGrid(getIndex);
				merge2D = obj.mergeImages(data2D, mask2D);
				saveName1 = path2 + saveIndex + ".tif";
				
			    imp1 = ImageUtil.wrapGrid(merge2D, null);
			    IJ.saveAs(imp1, "Tiff", saveName1);
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
