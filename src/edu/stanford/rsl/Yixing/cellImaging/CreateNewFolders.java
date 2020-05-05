package edu.stanford.rsl.Yixing.cellImaging;

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

public class CreateNewFolders {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\CellImaging\\parallel120PhantomTest\\";
		int num = 10;
		String folderName;
		File outPutDir;
		for(int idx = 0; idx < num; idx ++){
			folderName = path + idx + "\\";
			
			outPutDir = new File(folderName);
			if(!outPutDir.exists()){
		    outPutDir.mkdirs();
		    System.out.print(idx + " ");
			}
			
		}
		System.out.println("\nFinished!");
		
	}
}
