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
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class CropROIForEachPatient {
	public static void main(String[] args) throws IOException{
		new ImageJ();

		String path = "D:\\Dropbox\\Dropbox\\BVM2021\\Autorenkit-BVM2020\\";
		CropROIForEachPatient obj = new CropROIForEachPatient();
		ImagePlus imp1;
		String nameGT, nameFDK, namePWLS, nameUNet, nameWTV, nameDCR;
		String gtRoiName, fdkRoiName, pwlsRoiName, wtvRoiName;
		Grid2D gt, pwls, fdk, wtv, unet, dcr;
		Grid2D gtRoi, fdkRoi, pwlsRoi, wtvRoi;
		int width = 60;
		int x0 = 400, y0 = 264;
		gtRoi = new Grid2D(width, width);
		fdkRoi = new Grid2D(width, width);
		pwlsRoi = new Grid2D(width, width);
		wtvRoi = new Grid2D(width, width);
		
		
		nameGT = path + "reference.png";
		imp1=IJ.openImage(nameGT);
		gt = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
		gtRoi = obj.getROI(gt, width, width, x0, y0);
		gtRoiName = path + "refRoi.png";
		imp1 = ImageUtil.wrapGrid(gtRoi, null);
		imp1.setDisplayRange(0, 255);
	    IJ.saveAs(imp1, "png", gtRoiName);
	    
		for(int idx = 1; idx <= 8; idx ++){			
			
			nameFDK = path + "RFDK" + idx + ".png";
			namePWLS = path + "RPWLS" + idx + ".png";
			nameWTV = path + "wTV" + idx + ".png";
			
			
		
			imp1=IJ.openImage(nameFDK);
			fdk = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
			
			imp1=IJ.openImage(namePWLS);
			pwls = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
			
			imp1=IJ.openImage(nameWTV);
			wtv = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
			
			
			fdkRoi = obj.getROI(fdk, width, width, x0, y0);
			pwlsRoi = obj.getROI(pwls, width, width, x0, y0);
			wtvRoi = obj.getROI(wtv, width, width, x0, y0);
			
			
			fdkRoiName = path + "fdkRoi" + idx + ".png";	
			imp1 = ImageUtil.wrapGrid(fdkRoi, null);
			imp1.setDisplayRange(0, 255);
		    IJ.saveAs(imp1, "png", fdkRoiName);
		    
		    pwlsRoiName = path + "pwlsRoi" + idx + ".png";	
			imp1 = ImageUtil.wrapGrid(pwlsRoi, null);
			imp1.setDisplayRange(0, 255);
		    IJ.saveAs(imp1, "png", pwlsRoiName);
		    
		    
		    wtvRoiName = path + "wtvRoi" + idx + ".png";	
			imp1 = ImageUtil.wrapGrid(wtvRoi, null);
			imp1.setDisplayRange(0, 255);
		    IJ.saveAs(imp1, "png", wtvRoiName);
		}
		
		
	}

	public Grid2D getROI(Grid2D img, int width, int height, int x0, int y0)
	{
		Grid2D roi = new Grid2D(width, height);
		for(int i = 0; i < width; i ++)
			for(int j = 0; j < height; j++)
				roi.setAtIndex(i, j, img.getAtIndex(i + x0, j + y0));
		
		return roi;
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
