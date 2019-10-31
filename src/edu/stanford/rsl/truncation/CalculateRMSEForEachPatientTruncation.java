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

public class CalculateRMSEForEachPatientTruncation {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\finalTifs\\";
		ImagePlus imp;
		Grid3D vol1, vol2, vol3, vol4, vol5, vol6;
		String name1, name2, name3, name4, name5, name6;
		double rmse2, rmse3, rmse4, rmse5, rmse6;
		double sum2 = 0, sum3 = 0, sum4 = 0, sum5 = 0, sum6 = 0;
		Grid2D ref, recon;
		CalculateRMSEForEachPatientTruncation obj = new CalculateRMSEForEachPatientTruncation();
		
		for(int idx = 18; idx <= 18; idx ++){
			name1 = path + "GT.tif";
			name2 = path + "FBP.tif";
			name3 = path + "WCE.tif";
			name4 = path + "wTV.tif";
			name5 = path + "UNet.tif";
			name6 = path + "DCR.tif";
			
			imp = IJ.openImage(name1);
			vol1 = ImageUtil.wrapImagePlus(imp);
			imp = IJ.openImage(name2);
			vol2 = ImageUtil.wrapImagePlus(imp);
			imp = IJ.openImage(name3);
			vol3 = ImageUtil.wrapImagePlus(imp);
			imp = IJ.openImage(name4);
			vol4 = ImageUtil.wrapImagePlus(imp);
			imp = IJ.openImage(name5);
			vol5 = ImageUtil.wrapImagePlus(imp);
			imp = IJ.openImage(name6);
			vol6 = ImageUtil.wrapImagePlus(imp);

			for(int i = 0; i < vol1.getSize()[2]; i++) {
				ref = vol1.getSubGrid(i);
				recon = vol2.getSubGrid(i);
				rmse2 = obj.RMSE(ref, recon);

				recon = vol3.getSubGrid(i);
				rmse3 = obj.RMSE(ref, recon);
				
				recon = vol4.getSubGrid(i);
				rmse4 = obj.RMSE(ref, recon);
				recon = vol5.getSubGrid(i);
				rmse5 = obj.RMSE(ref, recon);
				recon = vol6.getSubGrid(i);
				rmse6 = obj.RMSE(ref, recon);
				if(i == 29 || i == 139) {
					System.out.println("i = " + i + ": "+ rmse2 + ", " + rmse3 + ", " + rmse4 + ", " + rmse5 + ", " + rmse6);
				}
				sum2 += rmse2; sum3 += rmse3; sum4 += rmse4; sum5 += rmse5; sum6 += rmse6;
			}
			sum2 = sum2/vol1.getSize()[2];
			sum3 = sum3/vol1.getSize()[2];
			sum4 = sum4/vol1.getSize()[2];
			sum5 = sum5/vol1.getSize()[2];
			sum6 = sum6/vol1.getSize()[2];
			System.out.println("average rmse = " + sum2 + ", " + sum3 + ", " + sum4 + ", " + sum5 + ", " + sum6);
		}
		
		
	}
	
//	private double RMSE(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		err = temp.getGridOperator().sum(temp);
//		err = err / (temp.getSize()[0] * temp.getSize()[1]);
//		err = Math.sqrt(err);
//		return err * 2040.0;
//	}
	
//	private double RMSE(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		double sum = 0;
//		int count = 0;
//		float x, y;
//		float thres = 97;
//		float thres2 = thres * thres;
//		for(int i = 0; i < recon.getSize()[0]; i ++)
//		{
//			x = i - (recon.getSize()[0] - 1)/2.0f;
//			for(int j = 0; j < recon.getSize()[1]; j++)
//			{
//				y = j - (recon.getSize()[1] - 1)/2.0f;
//				if(x * x + y * y < thres2)
//				{
//					count ++;
//					sum = sum + temp.getAtIndex(i, j);
//				}
//			}
//		}
//		err = sum /count;
//		err = Math.sqrt(err);
//		return err * 2040.0;
//	}
	
	private double RMSE(Grid2D recon, Grid2D recon_data) {
		double err = 0;
		Grid2D temp = new Grid2D(recon);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		double sum = 0;
		int count = 0;

		for(int i = 0; i < recon.getSize()[0]; i ++)
		{

			for(int j = 0; j < recon.getSize()[1]; j++)
				if(j < 190)
				{
					count ++;
					sum = sum + temp.getAtIndex(i, j);
				}
		}
		err = sum /count;
		err = Math.sqrt(err);
		return err * 2040.0;
	}
	
//	private double RMSE(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		double sum = 0;
//		int count = 0;
//		float x, y;
//		float thres = 80;
//		float thres2 = thres * thres;
//		for(int i = 0; i < recon.getSize()[0]; i ++)
//		{
//			x = i - (recon.getSize()[0] - 1)/2.0f;
//			for(int j = 0; j < recon.getSize()[1]; j++)
//			{
//				y = j - (recon.getSize()[1] - 1)/2.0f;
//				if(x * x + y * y > thres2)
//				{
//					count ++;
//					sum = sum + temp.getAtIndex(i, j);
//				}
//			}
//		}
//		err = sum /count;
//		err = Math.sqrt(err);
//		return err * 2040.0;
//	}
}
