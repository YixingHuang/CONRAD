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

public class CreateTestFolders {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		CreateTestFolders obj = new CreateTestFolders();

		String path2 = "C:\\Tasks\\FAU4\\TruncationCorrection\\Pix2pix\\Noisy3D\\";

		String folderName;
	
		File outPutDir;
		for(int idx = 1; idx<=18; idx ++){
//			folderName = path2 + "\\testResult" + idx + "\\";
			folderName = path2 + "\\model" + idx + "\\";
			outPutDir = new File(folderName);
			if(!outPutDir.exists()){
			    outPutDir.mkdirs();
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
