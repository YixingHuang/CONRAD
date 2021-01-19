package edu.stanford.rsl.Yixing.truncationClinic.clinicalReconstruction;

/**
 * S count from 0
 * P18, S149, x=42, y=91, w = 40
 * P9, S122, x = 30, y=128
 * P5 S125, x = 46, y = 184, w = 30
 * P18, S149, x = 30, y = 56. w = 40
 * clinical S67, x = 301, y = 105, w = 25
 */
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
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicRecons\\";
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicRecons\\PNGs\\";
		String saveName;
		CropROIForEachPatient obj = new CropROIForEachPatient();
		ImagePlus imp;
		String nameGT, nameWCE, nameFBPGT, nameScale, namePix, namePDP;
		Grid3D gt, fbpgt, wce, pix, scale, pdp;
		Grid2D gt2d, fbpgt2d, wce2d, pix2d, scale2d, pdp2d;
		Grid2D gtRoi, fbpgtRoi, wceRoi, pixRoi, scaleRoi, pdpRoi;
		int width = 25;
		int x0 = 309, y0 = 117;
		gtRoi = new Grid2D(width, width);
		fbpgtRoi = new Grid2D(width, width);
		wceRoi = new Grid2D(width, width);
		pixRoi = new Grid2D(width, width);
		scaleRoi = new Grid2D(width, width);
		pdpRoi = new Grid2D(width, width);
		
		int idx0 = 18;
		int sIdx = 67;
		
		for(int idx = idx0; idx <= idx0; idx ++){			
			nameGT = path + "wTV_Ref_30.tif";
			nameWCE = path + "WCE2.tif";
			nameFBPGT = path + "FDKPWLS1.tif";
			namePix = path + "pix2pix512_4.tif";
			nameScale = path + "FDKscale5.tif";
			namePDP = path+ "PDP.tif";
			
			imp=IJ.openImage(nameGT);
			gt = ImageUtil.wrapImagePlus(imp);
		
			imp=IJ.openImage(nameWCE);
			wce = ImageUtil.wrapImagePlus(imp);
			
			imp=IJ.openImage(nameFBPGT);
			fbpgt = ImageUtil.wrapImagePlus(imp);
			
			imp=IJ.openImage(namePix);
			pix = ImageUtil.wrapImagePlus(imp);
			
			
			imp=IJ.openImage(nameScale);
			scale = ImageUtil.wrapImagePlus(imp);
			
			imp=IJ.openImage(namePDP);
			pdp = ImageUtil.wrapImagePlus(imp);
			
			

			for(int i = sIdx; i <= sIdx; i++) {
				gt2d = (Grid2D) gt.getSubGrid(i).clone();
				fbpgt2d = (Grid2D) fbpgt.getSubGrid(i).clone();
						
				wce2d = (Grid2D) wce.getSubGrid(i).clone();
				pix2d = (Grid2D) pix.getSubGrid(i).clone();
				scale2d = (Grid2D) scale.getSubGrid(i).clone();
				pdp2d = (Grid2D) pdp.getSubGrid(i).clone();
				
				pdp2d.show("dcr2d");


				for(int ix = 0; ix < width; ix++) {
					for(int iy = 0; iy < width; iy++)
					{
						gtRoi.setAtIndex(ix, iy, gt2d.getAtIndex(ix + x0, iy + y0));
						fbpgtRoi.setAtIndex(ix, iy, fbpgt2d.getAtIndex(ix + x0, iy + y0));
						wceRoi.setAtIndex(ix, iy, wce2d.getAtIndex(ix + x0, iy + y0));
						pixRoi.setAtIndex(ix, iy, pix2d.getAtIndex(ix + x0, iy + y0));
						scaleRoi.setAtIndex(ix, iy, scale2d.getAtIndex(ix + x0, iy + y0));
						pdpRoi.setAtIndex(ix, iy, pdp2d.getAtIndex(ix + x0, iy + y0));
					}
				}
				
				int ii = i + 1;
				gtRoi.clone().show( "gtRoi" + "S" + ii);
				fbpgtRoi.clone().show( "fbpgtRoi" + "S" + ii);
				wceRoi.clone().show( "wceRoi" + "S" + ii);
				pixRoi.clone().show( "pixRoi" + "S" + ii);
				scaleRoi.clone().show( "scaleRoi" + "S" + ii);
				pdpRoi.clone().show( "pdpRoi" + "S" + ii);
				
					
				saveName = savePath + "gtRoi2" + "S" + ii + ".png";
	            imp = ImageUtil.wrapGrid(gtRoi, null);
	            imp.setDisplayRange(0, 1);
	            IJ.saveAs(imp, "png", saveName);
	            imp.close();
	            
	            saveName = savePath + "fbpgtRoi2" + "S" + ii + ".png";
	            imp = ImageUtil.wrapGrid(fbpgtRoi, null);
	            imp.setDisplayRange(0, 1);
	            IJ.saveAs(imp, "png", saveName);
	            imp.close();
	            
	            saveName = savePath + "wceRoi2" + "S" + ii + ".png";
	            imp = ImageUtil.wrapGrid(wceRoi, null);
	            imp.setDisplayRange(0, 1);
	            IJ.saveAs(imp, "png", saveName);
	            imp.close();
	            
	            saveName = savePath + "pixRoi2" + "S" + ii + ".png";
	            imp = ImageUtil.wrapGrid(pixRoi, null);
	            imp.setDisplayRange(0, 1);
	            IJ.saveAs(imp, "png", saveName);
	            imp.close();
	            
	            saveName = savePath + "scaleRoi2" + "S" + ii + ".png";
	            imp = ImageUtil.wrapGrid(scaleRoi, null);
	            imp.setDisplayRange(0, 1);
	            IJ.saveAs(imp, "png", saveName);
	            imp.close();
				
	            saveName = savePath + "pdpRoi2" + "S" + ii + ".png";
	            imp = ImageUtil.wrapGrid(pdpRoi, null);
	            imp.setDisplayRange(0, 1);
	            IJ.saveAs(imp, "png", saveName);
	            imp.close();
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
