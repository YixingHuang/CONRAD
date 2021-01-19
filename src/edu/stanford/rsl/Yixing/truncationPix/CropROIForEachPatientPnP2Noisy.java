package edu.stanford.rsl.Yixing.truncationPix;

/**
 * P18, S149, x=42, y=91, w = 40
 * P9, S122, x = 30, y=128
 * P3, S174, x=56, y=120
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

public class CropROIForEachPatientPnP2Noisy {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
//		String path = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\recon\\";
//		String uNetPath = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\UNetRecons\\";
//		String wTvPath = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\wTV\\";
//		String DcrPath = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\DCR\\";
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\recon\\";
		String directPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FDKParker\\";
		String scalePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\FDKScale\\";
		String subPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FDKSubtraction\\";
		String fistaPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FISTA\\";
		String fistaProjPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\FISTA_Projection\\";
		String fistaWtvPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FISTA_wTV\\";
		String fistaProjWtvPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\FISTA_Projection_wTV\\";
		CropROIForEachPatientPnP2Noisy obj = new CropROIForEachPatientPnP2Noisy();
		ImagePlus imp1, imp;
		String nameGT, nameDirect, nameScale, nameSub, nameFista, nameFistaProj, nameFistaWtv, nameFistaProjWtv;
		Grid3D gt, direct, scale, sub, fista, fistaProj, fistaWtv, fistaProjWtv;
		Grid2D gt2d, direct2d, scale2d, sub2d, fista2d, fistaProj2d, fistaWtv2d, fistaProjWtv2d;
		Grid2D gtRoi, directRoi, scaleRoi, subRoi, fistaRoi, fistaProjRoi, fistaWtvRoi, fistaProjWtvRoi;
		int width = 40;
		int x0 = 56, y0 = 120;
		gtRoi = new Grid2D(width, width);
		directRoi = new Grid2D(width, width);
		scaleRoi = new Grid2D(width, width);
		subRoi = new Grid2D(width, width);
		fistaRoi = new Grid2D(width, width);
		fistaProjRoi = new Grid2D(width, width);
		fistaWtvRoi = new Grid2D(width, width);
		fistaProjWtvRoi = new Grid2D(width, width);
		
		String savePath = "D:\\Dropbox\\Dropbox\\TMI\\DCR_ID_DLP\\truncation\\P18\\";
		String saveName;
		
		int idx0 = 3;
		int sIdx = 174;
		
		for(int idx = idx0; idx <= idx0; idx ++){			
			nameGT = path + "reconGT" + idx + ".tif";
			nameDirect = directPath + "FDK" + idx + ".tif";
			nameScale = scalePath + "FDK" + idx + ".tif";
			nameSub = subPath + "FDK" + idx + ".tif";
			nameFista = fistaPath + idx + "\\10_FinalReconCL.tif";
			nameFistaProj = fistaProjPath + idx + "\\10_FinalReconCL.tif";
//			nameFista = fistaPath + idx + "\\dot5.tif";
//			nameFistaProj = fistaProjPath + idx + "\\dot5.tif";
			nameFistaWtv = fistaWtvPath + idx + "\\10_FinalReconCL.tif";
			nameFistaProjWtv = fistaProjWtvPath + idx + "\\10_FinalReconCL.tif";
			
			imp1=IJ.openImage(nameGT);
			gt = ImageUtil.wrapImagePlus(imp1);
		
			imp1=IJ.openImage(nameDirect);
			direct = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameScale);
			scale = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameSub);
			sub = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameFista);
			fista = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameFistaProj);
			fistaProj = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameFistaWtv);
			fistaWtv = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameFistaProjWtv);
			fistaProjWtv = ImageUtil.wrapImagePlus(imp1);
			
			

			for(int i = sIdx; i <= sIdx; i++) {
				gt2d = (Grid2D) gt.getSubGrid(i).clone();
				direct2d = (Grid2D) direct.getSubGrid(i).clone();
				scale2d = (Grid2D) scale.getSubGrid(i).clone();
				sub2d = (Grid2D) sub.getSubGrid(i).clone();
				fista2d = (Grid2D) fista.getSubGrid(i).clone();
				fistaProj2d = (Grid2D) fistaProj.getSubGrid(i).clone();
				fistaWtv2d = (Grid2D) fistaWtv.getSubGrid(i).clone();
				fistaProjWtv2d = (Grid2D) fistaProjWtv.getSubGrid(i).clone();
				
			


				for(int ix = 0; ix < width; ix++) {
					for(int iy = 0; iy < width; iy++)
					{
						gtRoi.setAtIndex(ix, iy, gt2d.getAtIndex(ix + x0, iy + y0));
						directRoi.setAtIndex(ix, iy, direct2d.getAtIndex(ix + x0, iy + y0));
						scaleRoi.setAtIndex(ix, iy, scale2d.getAtIndex(ix + x0, iy + y0));
						subRoi.setAtIndex(ix, iy, sub2d.getAtIndex(ix + x0, iy + y0));
						fistaRoi.setAtIndex(ix, iy, fista2d.getAtIndex(ix + x0, iy + y0));
						fistaProjRoi.setAtIndex(ix, iy, fistaProj2d.getAtIndex(ix + x0, iy + y0));
						fistaWtvRoi.setAtIndex(ix, iy, fistaWtv2d.getAtIndex(ix + x0, iy + y0));
						fistaProjWtvRoi.setAtIndex(ix, iy, fistaProjWtv2d.getAtIndex(ix + x0, iy + y0));
					}
				}
				int ii = i + 1;
				gtRoi.clone().show( "gtRoiNoisy" + idx + "S" + ii);
				directRoi.clone().show( "FBPDirectROI" + idx + "S" + ii);
				scaleRoi.clone().show( "FBPScaleROI" + idx + "S" + ii);
				subRoi.clone().show( "FBPSubROI" + idx + "S" + ii);
				fistaRoi.clone().show( "FistaROI" + idx + "S" + ii);
				fistaProjRoi.clone().show( "FistaProjROI" + idx + "S" + ii);
				fistaWtvRoi.clone().show( "FistaWtvROI" + idx + "S" + ii);
				fistaProjWtvRoi.clone().show( "FistaProjWtvROI" + idx + "S" + ii);
				
//				saveName = savePath + "gtRoiNoisy" + idx + "S" + ii + ".png";
//	            imp = ImageUtil.wrapGrid(gtRoi, null);
//	            imp.setDisplayRange(0.4, 0.6);
//	            IJ.saveAs(imp, "png", saveName);
//	            imp.close();	
//	            
//				saveName = savePath + "gtRoiNoisy" + idx + "S" + ii + ".png";
//	            imp = ImageUtil.wrapGrid(gtRoi, null);
//	            imp.setDisplayRange(0.4, 0.6);
//	            IJ.saveAs(imp, "png", saveName);
//	            imp.close();	
//	            
//				saveName = savePath + "FBPDirectROI" + idx + "S" + ii + ".png";
//	            imp = ImageUtil.wrapGrid(directRoi, null);
//	            imp.setDisplayRange(0.4, 0.6);
//	            IJ.saveAs(imp, "png", saveName);
//	            imp.close();	
	            
				saveName = savePath + "FBPScaleROINoisy" + idx + "S" + ii + ".png";
	            imp = ImageUtil.wrapGrid(scaleRoi, null);
	            imp.setDisplayRange(0.4, 0.6);
	            IJ.saveAs(imp, "png", saveName);
	            imp.close();	
	            
//				saveName = savePath + "FBPSubROI" + idx + "S" + ii + ".png";
//	            imp = ImageUtil.wrapGrid(subRoi, null);
//	            imp.setDisplayRange(0.4, 0.6);
//	            IJ.saveAs(imp, "png", saveName);
//	            imp.close();	
//	            
//				saveName = savePath + "FistaROI" + idx + "S" + ii + ".png";
//	            imp = ImageUtil.wrapGrid(fistaRoi, null);
//	            imp.setDisplayRange(0.4, 0.6);
//	            IJ.saveAs(imp, "png", saveName);
//	            imp.close();	
	            
				saveName = savePath + "FistaProjROINoisy" + idx + "S" + ii + ".png";
	            imp = ImageUtil.wrapGrid(fistaProjRoi, null);
	            imp.setDisplayRange(0.4, 0.6);
	            IJ.saveAs(imp, "png", saveName);
	            imp.close();	
	            
//				saveName = savePath + "FistaWtvROI" + idx + "S" + ii + ".png";
//	            imp = ImageUtil.wrapGrid(fistaWtvRoi, null);
//	            imp.setDisplayRange(0.4, 0.6);
//	            IJ.saveAs(imp, "png", saveName);
//	            imp.close();
//	            
	    		saveName = savePath + "FistaProjWtvROINoisy" + idx + "S" + ii + ".png";
	            imp = ImageUtil.wrapGrid(fistaProjWtvRoi, null);
	            imp.setDisplayRange(0.4, 0.6);
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
