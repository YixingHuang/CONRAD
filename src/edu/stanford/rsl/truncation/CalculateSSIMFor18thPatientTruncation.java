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
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class CalculateSSIMFor18thPatientTruncation {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\finalTifs\\";
		ImagePlus imp;
		Grid3D vol1, vol2, vol3, vol4, vol5, vol6;
		String name1, name2, name3, name4, name5, name6;
		double rmse2, rmse3, rmse4, rmse5, rmse6;
		double sum2 = 0, sum3 = 0, sum4 = 0, sum5 = 0, sum6 = 0;
		Grid2D ref, recon;
		CalculateSSIMFor18thPatientTruncation obj = new CalculateSSIMFor18thPatientTruncation();
		
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
				obj.keepFOV(ref);
				
				recon = vol2.getSubGrid(i);
				obj.keepFOV(recon);
				rmse2 = obj.getSSIMDoubleValue(ref, recon);
				
				recon = vol3.getSubGrid(i);
				obj.keepFOV(recon);
				rmse3 = obj.getSSIMDoubleValue(ref, recon);
				
				recon = vol4.getSubGrid(i);
				obj.keepFOV(recon);
				rmse4 = obj.getSSIMDoubleValue(ref, recon);
				
				recon = vol5.getSubGrid(i);
				obj.keepFOV(recon);
				rmse5 = obj.getSSIMDoubleValue(ref, recon);
				
				recon = vol6.getSubGrid(i);
				obj.keepFOV(recon);
				rmse6 = obj.getSSIMDoubleValue(ref, recon);
				if(i == 29 || i == 139 || i == 149) {
					System.out.println("i = " + i + ": "+ rmse2 + ", " + rmse3 + ", " + rmse4 + ", " + rmse5 + ", " + rmse6);
				}
				if(i >= 20 && i < vol1.getSize()[2] - 20)
				{
				sum2 += rmse2; sum3 += rmse3; sum4 += rmse4; sum5 += rmse5; sum6 += rmse6;
				}
			}
			sum2 = sum2/(vol1.getSize()[2] - 40);
			sum3 = sum3/(vol1.getSize()[2] - 40);
			sum4 = sum4/(vol1.getSize()[2] - 40);
			sum5 = sum5/(vol1.getSize()[2] - 40);
			sum6 = sum6/(vol1.getSize()[2] - 40);
			System.out.println("average rmse = " + sum2 + ", " + sum3 + ", " + sum4 + ", " + sum5 + ", " + sum6);
		}
		
		
	}
	


	
	public double getSSIMDoubleValue(Grid2D img1, Grid2D img2) {
		ImagePlus Imp1 = ImageUtil.wrapGrid(img1, null);
		ImagePlus Imp2 = ImageUtil.wrapGrid(img2, null);
		double returnSSIMDoubleValue = 0;
		int lengthOfImp1 = Imp1.getWidth() * Imp1.getHeight();
		int lengthOfImp2 = Imp2.getWidth() * Imp2.getHeight();
		if (lengthOfImp1 != lengthOfImp2) {
			System.out.println("[!] Error: ROI sizes are not equal!");
		} else {
			double[] xCoord = new double[lengthOfImp1];
			double[] yCoord = new double[lengthOfImp2];
			int XnonFinitiyCounts = 0;
			int YnonFinitiyCounts = 0;
			for (int i = 0; i < lengthOfImp1; i++) {

				if (Float.isFinite(Imp1.getProcessor().getf(i))) {
					xCoord[i] = Imp1.getProcessor().getf(i);
				} else {// kick out non-Finite values of Imp1
					XnonFinitiyCounts = XnonFinitiyCounts + 1;
					xCoord[i] = 0;
				}

				if (Float.isFinite(Imp2.getProcessor().getf(i))) {
					yCoord[i] = Imp2.getProcessor().getf(i);
				} else {// kick out non-Finite values of Imp2
					YnonFinitiyCounts = YnonFinitiyCounts + 1;
					yCoord[i] = 0;
				}

			}

			// Calculate the Correlation result.
			double SSIMDoubleValue = DoubleArrayUtil.computeSSIMDoubleArrays(xCoord, yCoord);
			returnSSIMDoubleValue = SSIMDoubleValue;

		}
		return returnSSIMDoubleValue;

	}
	
	private void keepFOV(Grid2D phan)
	{

		for(int i = 0; i < phan.getSize()[0]; i ++)
			for(int j = 190; j < phan.getSize()[1]; j ++)
			{
				phan.setAtIndex(i, j, 0);
			}
	}

}
