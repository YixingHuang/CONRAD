package edu.stanford.rsl.Yixing.ERC;

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

public class CalculateRMSEForEachPatientERC {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "E:\\Lasse\\BVM\\";
		ImagePlus imp;
		Grid3D vol1, vol2, vol3, vol4, vol5, vol6;
		String name1, name2, name3, name4, name5, name6;
		double rmse2, rmse3, rmse4, rmse5, rmse6;
		double sum2 = 0, sum3 = 0, sum4 = 0, sum5 = 0, sum6 = 0;
		Grid2D ref, recon;
		CalculateRMSEForEachPatientERC obj = new CalculateRMSEForEachPatientERC();
		
		String refPath = path + "subvolume.tif";
		ImagePlus imp0 =IJ.openImage(refPath);
		Grid3D vol0 = ImageUtil.wrapImagePlus(imp0);
		 
		for(int idx = 1; idx <= 8; idx ++){
			if(idx == 3 || idx == 4)
				continue;
			name1 = path + "reconNoisy" + idx + ".tif";
			name2 = path + "reconPwls" + idx + ".tif";
			name3 = path + "wTV\\" + idx + "\\10_FinalReconCL.tif";

			
			imp = IJ.openImage(name1);
			vol1 = ImageUtil.wrapImagePlus(imp);
			vol1.getGridOperator().divideBy(vol1, 0.00364f);
			imp = IJ.openImage(name2);
			vol2 = ImageUtil.wrapImagePlus(imp);
			vol2.getGridOperator().divideBy(vol2, 0.00364f);
			imp = IJ.openImage(name3);
			vol3 = ImageUtil.wrapImagePlus(imp);
			

			for(int i = 50; i < vol1.getSize()[2] - 50; i++) {
				ref = vol0.getSubGrid(i);
				recon = vol1.getSubGrid(i);
				rmse2 = obj.RMSE(ref, recon);

				recon = vol2.getSubGrid(i);
				rmse3 = obj.RMSE(ref, recon);
				
				recon = vol3.getSubGrid(i);
				rmse4 = obj.RMSE(ref, recon);
				
				if(i == 119) {
					System.out.println("idx = " + idx + ", i = " + i + ": "+ rmse2 + ", " + rmse3 + ", " + rmse4 );
				}
				sum2 += rmse2; sum3 += rmse3; sum4 += rmse4; 
			}
			int count = (vol1.getSize()[2] - 100);
			sum2 = sum2/count;
			sum3 = sum3/count;
			sum4 = sum4/count;

			System.out.println("idx =" + idx + ", average rmse = " + sum2 + ", " + sum3 + ", " + sum4);
		}
		
		
	}
	
	private void addFOV(Grid2D img, float r)
	{
		float rr = r * r;
		float centX = (img.getSize()[0] - 1)/2.0f;
		float centY = (img.getSize()[1] - 1)/2.0f;
		
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				if( (i - centX) * (i - centX) + (j - centY) * (j - centY) > rr)
					img.setAtIndex(i, j, 0);
	}
	
	private double RMSE(Grid2D recon, Grid2D recon_data) {
		double err = 0;
		Grid2D temp = new Grid2D(recon);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		double sum = temp.getGridOperator().sum(temp);
		err = sum / temp.getNumberOfElements();
		err = Math.sqrt(err);
		return err/16.0;
	}

}
