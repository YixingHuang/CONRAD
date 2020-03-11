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

public class CropROIForEachPatient {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
//		String path = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\recon\\";
//		String uNetPath = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\UNetRecons\\";
//		String wTvPath = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\wTV\\";
//		String DcrPath = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\DCR\\";
		
		String path = "E:\\FAU4\\TruncationCorrection\\Noisy3D\\recon\\";
		String uNetPath = "E:\\FAU4\\TruncationCorrection\\Noisy3D\\UNetRecons\\";
		String wTvPath = "E:\\FAU4\\TruncationCorrection\\Noisy3D\\wTV\\";
		String DcrPath = "E:\\FAU4\\TruncationCorrection\\Noisy3D\\DCR\\";
		CropROIForEachPatient obj = new CropROIForEachPatient();
		ImagePlus imp1;
		String nameGT, nameWCE, nameFBP, nameUNet, nameWTV, nameDCR;
		Grid3D gt, fbp, wce, wtv, unet, dcr;
		Grid2D gt2d, fbp2d, wce2d, wtv2d, unet2d, dcr2d;
		Grid2D gtRoi, fbpRoi, wceRoi, wtvRoi, unetRoi, dcrRoi;
		int width = 40;
		int x0 = 42, y0 = 91;
		gtRoi = new Grid2D(width, width);
		fbpRoi = new Grid2D(width, width);
		wceRoi = new Grid2D(width, width);
		wtvRoi = new Grid2D(width, width);
		unetRoi = new Grid2D(width, width);
		dcrRoi = new Grid2D(width, width);
		
		int idx0 = 18;
		int sIdx = 149;
		
		for(int idx = idx0; idx <= idx0; idx ++){			
			nameGT = path + "reconGT" + idx + ".tif";
			nameWCE = path + "reconLimited" + idx + ".tif";
			nameFBP = path + "reconFbp" + idx + ".tif";
			nameWTV = wTvPath + idx + "\\20_FinalReconCL.tif";
			nameUNet = uNetPath + "UNetP" + idx + ".tif";
			nameDCR = DcrPath + idx + "\\10_FinalReconCL.tif";
			
			imp1=IJ.openImage(nameGT);
			gt = ImageUtil.wrapImagePlus(imp1);
		
			imp1=IJ.openImage(nameWCE);
			wce = ImageUtil.wrapImagePlus(imp1);
			
//			imp1=IJ.openImage(nameFBP);
//			fbp = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameWTV);
			wtv = ImageUtil.wrapImagePlus(imp1);
			
			
			imp1=IJ.openImage(nameUNet);
			unet = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameDCR);
			dcr = ImageUtil.wrapImagePlus(imp1);
			
			

			for(int i = sIdx; i <= sIdx; i++) {
				gt2d = (Grid2D) gt.getSubGrid(i).clone();
//				fbp2d = (Grid2D) fbp.getSubGrid(i).clone();
				
				String path0 = "D:\\tasks\\FAU4\\Lab\\PhDThesis\\Dropbox\\TMI\\DCR_ID_DLP\\truncation\\P18\\FBP18S150PoiSW.png";
				imp1=IJ.openImage(path0);
				fbp2d = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
				
				wce2d = (Grid2D) wce.getSubGrid(i).clone();
				wtv2d = (Grid2D) wtv.getSubGrid(i).clone();
				unet2d = (Grid2D) unet.getSubGrid(i).clone();
				dcr2d = (Grid2D) dcr.getSubGrid(i).clone();
				
				dcr2d.show("dcr2d");


				for(int ix = 0; ix < width; ix++) {
					for(int iy = 0; iy < width; iy++)
					{
						gtRoi.setAtIndex(ix, iy, gt2d.getAtIndex(ix + x0, iy + y0));
						fbpRoi.setAtIndex(ix, iy, fbp2d.getAtIndex(ix + x0, iy + y0));
						wceRoi.setAtIndex(ix, iy, wce2d.getAtIndex(ix + x0, iy + y0));
						wtvRoi.setAtIndex(ix, iy, wtv2d.getAtIndex(ix + x0, iy + y0));
						unetRoi.setAtIndex(ix, iy, unet2d.getAtIndex(ix + x0, iy + y0));
						dcrRoi.setAtIndex(ix, iy, dcr2d.getAtIndex(ix + x0, iy + y0));
					}
				}
				int ii = i + 1;
				gtRoi.clone().show( "gtRoiNoisy" + idx + "S" + ii);
				fbpRoi.clone().show( "fbpRoiNoisy" + idx + "S" + ii);
				wceRoi.clone().show( "wceRoiNoisy" + idx + "S" + ii);
				wtvRoi.clone().show( "wtvRoiNoisy" + idx + "S" + ii);
				unetRoi.clone().show( "unetRoiNoisy" + idx + "S" + ii);
				dcrRoi.clone().show( "dcrRoiNoisy" + idx + "S" + ii);
				
					
				
					
				
			}

		}
		
		
	}
	
	public double getSSIMDoubleValue(Grid2D img1, Grid2D img2) {
		img1 = keepFOV(img1);
		img2 = keepFOV(img2);
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
	
//	private void keepFOV(Grid2D phan)
//	{
//
//		for(int i = 0; i < phan.getSize()[0]; i ++)
//			for(int j = 190; j < phan.getSize()[1]; j ++)
//			{
//				phan.setAtIndex(i, j, 0);
//			}
//	}
	
	private Grid2D keepFOV(Grid2D phan)
	{
		Grid2D phanCrop = new Grid2D(phan.getSize()[0], 190);
		for(int i = 0; i < phan.getSize()[0]; i ++)
			for(int j = 0; j < phanCrop.getSize()[1]; j ++)
			{
				phanCrop.setAtIndex(i, j, phan.getAtIndex(i, j));
			}
		
		return phanCrop;
	}
}
