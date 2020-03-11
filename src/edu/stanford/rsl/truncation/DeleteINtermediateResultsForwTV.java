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

public class DeleteINtermediateResultsForwTV {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
	
		String path2 = "D:\\Tasks\\FAU4\\LimitedAngle\\Noisy3D\\150Degree\\wTV\\";
		String path3, path4, path5;
		ImagePlus imp1, imp2, imp3, imp4;
		String name1, name2, name3;
		Grid3D data, mask;
		double rmse0, rmse1, rmse2;
		Grid3D recon3D = new Grid3D(256,256,256);
		File outPutDir0, outPutDir1, outPutDir2;
		BufferedWriter bw0, bw1, bw2; 
		for(int idx = 1; idx <= 18; idx ++){
		
			

			path3 = path2 + idx + "\\";
			for(int j = 0; j < 100; j = j+5)
			{
				if(j == 50)
					continue;
				name3 = path3 + j + "thResult.tif";

				outPutDir0 = new File(name3);
				if(outPutDir0.exists()){
					outPutDir0.delete();
				}
			}
			
			System.out.print(idx + " ");
		}
	}
	
	private static double RMSE(Grid2D recon, Grid2D recon_data) {
		double err = 0;
		Grid2D temp = new Grid2D(recon);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		err = temp.getGridOperator().sum(temp);
		err = err / (temp.getSize()[0] * temp.getSize()[1]);
		err = Math.sqrt(err);
		return err;
	}
}
