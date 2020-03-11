package edu.stanford.rsl.cellImaging;

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
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;

public class CalculateSSIMForPhanReconstructions {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		CalculateSSIMForPhanReconstructions obj = new CalculateSSIMForPhanReconstructions();
//		String folderPath = "D:\\Tasks\\FAU4\\CellImaging\\Tifs\\phantome5Results\\";
//		String path0 = folderPath + "referenceRecons.tif";
//		String path1 = folderPath + "phanFbp.tif";
//		String path2 = folderPath + "phanSEUnet.tif";
//		String path3 = folderPath + "reconFbp3DCombined.tif";
//		String path4 = folderPath + "phanFbpPwls.tif";
//		String path5 = folderPath + "phanSEUnetPwls.tif";
//		String path6 = folderPath + "reconFbp3DCombinedPwls.tif";
		
		String folderPath = "D:\\Tasks\\FAU4\\CellImaging\\Tifs\\phantome4Results\\";
		String path0 = folderPath + "referenceRecons.tif";
		String path1 = folderPath + "reconFbp3D.tif";
		String path2 = folderPath + "SEUNet.tif";
		String path3 = folderPath + "reconFbp3DCombined.tif";
		String path4 = folderPath + "reconFbp3DPwls2.tif";
		String path5 = folderPath + "SEUNetPwls.tif";
		String path6 = folderPath + "reconFbp3DCombinedPwls2.tif";

		ImagePlus imp;
		imp = IJ.openImage(path0);
		Grid3D ref3D = ImageUtil.wrapImagePlus(imp);
		
		imp = IJ.openImage(path1);
		Grid3D recon3D1 = ImageUtil.wrapImagePlus(imp);
		recon3D1.getGridOperator().removeNegative(recon3D1);
		imp = IJ.openImage(path2);
		Grid3D recon3D2 = ImageUtil.wrapImagePlus(imp);
		imp = IJ.openImage(path3);
		Grid3D recon3D3 = ImageUtil.wrapImagePlus(imp);
		imp = IJ.openImage(path4);
		Grid3D recon3D4 = ImageUtil.wrapImagePlus(imp);
		imp = IJ.openImage(path5);
		Grid3D recon3D5 = ImageUtil.wrapImagePlus(imp);
		recon3D5.getGridOperator().removeNegative(recon3D5);
		imp = IJ.openImage(path6);
		Grid3D recon3D6 = ImageUtil.wrapImagePlus(imp);
		
		double r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0, r6 = 0;
		double ar1 = 0, ar2 = 0, ar3 = 0, ar4 = 0, ar5 = 0, ar6 = 0;
		double r1s2 = 0, r2s2 = 0, r3s2 = 0, r4s2 = 0, r5s2 = 0, r6s2 = 0; //for slice 250;
		double r1s3 = 0, r2s3 = 0, r3s3 = 0, r4s3 = 0, r5s3 = 0, r6s3 = 0; // for slice 355
		Grid2D ref2D, recon2D;
		int n1 = 214;
		for(int i = 0; i < ref3D.getSize()[2]; i++) {
			ref2D = ref3D.getSubGrid(i);
			obj.keepFOV(ref2D);
			recon2D = recon3D1.getSubGrid(i);
			obj.keepFOV(recon2D);
			r1 = obj.getSSIMDoubleValue(ref2D, recon2D);
			ar1 += r1;
			if(i == n1)
				r1s2 = r1;
			else if(i == 354)
				r1s3 = r1;
			
			recon2D = recon3D2.getSubGrid(i);
			obj.keepFOV(recon2D);
			r2 = obj.getSSIMDoubleValue(ref2D, recon2D);
			ar2 += r2;
			if(i == n1)
				r2s2 = r2;
			else if(i == 354)
				r2s3 = r2;
			
			recon2D = recon3D3.getSubGrid(i);
			obj.keepFOV(recon2D);
			r3 = obj.getSSIMDoubleValue(ref2D, recon2D);
			ar3 += r3;
			if(i == n1)
				r3s2 = r3;
			else if(i == 354)
				r3s3 = r3;
			
			recon2D = recon3D4.getSubGrid(i);
			obj.keepFOV(recon2D);
			r4 = obj.getSSIMDoubleValue(ref2D, recon2D);
			ar4 += r4;
			if(i == n1)
				r4s2 = r4;
			else if(i == 354)
				r4s3 = r4;
			
			recon2D = recon3D5.getSubGrid(i);
			obj.keepFOV(recon2D);
			r5 = obj.getSSIMDoubleValue(ref2D, recon2D);
			ar5 += r5;
			if(i == n1)
				r5s2 = r5;
			else if(i == 354)
				r5s3 = r5;
			
			recon2D = recon3D6.getSubGrid(i);
			obj.keepFOV(recon2D);
			r6 = obj.getSSIMDoubleValue(ref2D, recon2D);
			ar6 += r6;
			if(i == n1)
				r6s2 = r6;
			else if(i == 354)
				r6s3 = r6;
		}
		
		System.out.print("slice " + n1 + ": " + r1s2 + ", " + r2s2 + ", " + r3s2 + ", " + r4s2 + ", " + r5s2 + ", " + r6s2 + "");
		System.out.println(" ");
		
		System.out.print("slice 355: " + r1s3 + ", " + r2s3 + ", " + r3s3 + ", " + r4s3 + ", " + r5s3 + ", " + r6s3 + "");
		System.out.println(" ");
		
		System.out.print("average error: " + ar1/512.0 + ", " + ar2/512.0 + ", " + ar3/512.0 + ", " + ar4/512.0 + ", " + ar5/512.0 + ", " + ar6/512.0 + "");
		System.out.println(" ");
		
		
	}
	
	
	/**
	 * Return a double SSIM result of two ImagePlus.
	 *
	 * @param Imp1
	 * @param Imp2
	 * @return returnSSIMDoubleValue
	 */
	public double getSSIMDoubleValue(ImagePlus Imp1, ImagePlus Imp2) {
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
		float r = (phan.getSize()[0] - 1.0f)/2.0f;
		float rr = r * r;
		float xCent = r;
		float yCent = xCent;
		float dd;
		for(int i = 0; i < phan.getSize()[0]; i ++)
			for(int j = 0; j < phan.getSize()[1]; j ++)
			{
				dd = (i - xCent) * (i - xCent) + (j - yCent) * (j - yCent);
				if(dd > rr - 400)
					phan.setAtIndex(i, j, 0);
			}
	}
}
