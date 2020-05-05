package edu.stanford.rsl.Yixing.truncation;

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

public class CalculateSSIMForEachPatient {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\recon\\";
		String uNetPath = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\UNetRecons\\";
		String wTvPath = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\wTV\\";
		String DcrPath = "E:\\FAU4\\TruncationCorrection\\NoiseFree3D\\DCR\\";
		CalculateSSIMForEachPatient obj = new CalculateSSIMForEachPatient();
		ImagePlus imp1;
		String nameGT, nameWCE, nameFBP, nameUNet, nameWTV, nameDCR;
		Grid3D gt, fbp, wce, wtv, unet, dcr;
		Grid2D gt2d, fbp2d, wce2d, wtv2d, unet2d, dcr2d;
		double ssimFbp, ssimWce, ssimWtv, ssimUnet, ssimDcr;
		File outPutDir0, outPutDir1;
		BufferedWriter bw0, bw1; 
		double sumFbp, sumWce, sumWtv, sumUnet, sumDcr;
		for(int idx = 1; idx <= 18; idx ++){
			if(idx == 4 )
				continue;
			sumFbp = 0;
			sumWce = 0;
			sumWtv = 0;
			sumUnet = 0;
			sumDcr = 0;
			
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
			
			imp1=IJ.openImage(nameFBP);
			fbp = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameWTV);
			wtv = ImageUtil.wrapImagePlus(imp1);
			
			
			imp1=IJ.openImage(nameUNet);
			unet = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameDCR);
			dcr = ImageUtil.wrapImagePlus(imp1);
			
			
//			outPutDir0 = new File(path3+"RMSE_FBP_ROI.txt");
//			outPutDir1 = new File(path3+"UNet_ROI.txt");
//			if(!outPutDir0.exists()){
//				outPutDir0.getParentFile().mkdirs();
//				outPutDir0.createNewFile();
//			}
//			
//			if(!outPutDir1.exists()){
//				outPutDir1.getParentFile().mkdirs();
//				outPutDir1.createNewFile();
//			}
//
//			
//			bw0 = new BufferedWriter(new FileWriter(outPutDir0));
//			bw1 = new BufferedWriter(new FileWriter(outPutDir1));
			for(int i = 20; i < gt.getSize()[2] - 20; i++) {
				gt2d = (Grid2D) gt.getSubGrid(i).clone();
				fbp2d = (Grid2D) fbp.getSubGrid(i).clone();
				wce2d = (Grid2D) wce.getSubGrid(i).clone();
				wtv2d = (Grid2D) wtv.getSubGrid(i).clone();
				unet2d = (Grid2D) unet.getSubGrid(i).clone();
				dcr2d = (Grid2D) dcr.getSubGrid(i).clone();
				
				ssimFbp = obj.getSSIMDoubleValue(fbp2d, gt2d);
				ssimWce = obj.getSSIMDoubleValue(wce2d, gt2d);
				ssimWtv = obj.getSSIMDoubleValue(wtv2d, gt2d);
				ssimUnet = obj.getSSIMDoubleValue(unet2d, gt2d);
				ssimDcr = obj.getSSIMDoubleValue(dcr2d, gt2d);
				
				sumFbp += ssimFbp;
				sumWce += ssimWce;
				sumWtv += ssimWtv;
				sumUnet += ssimUnet;
				sumDcr += ssimDcr;
//				bw0.write(rmse0 + "\r\n");
//				bw0.flush();	
//		
//				bw1.write(rmse1 + "\r\n");
//				bw1.flush();
				if(idx == 3 && i == 174)
					System.out.println(idx + " " + i + ":" + ssimFbp + " " + ssimWce + " " + ssimWtv + " " + ssimUnet + " " + ssimDcr);
				
				if(idx == 18 && i == 149)
					System.out.println(idx + " " + i + ":" + ssimFbp + " " + ssimWce + " " + ssimWtv + " " + ssimUnet + " " + ssimDcr);
				
			}
//			bw1.close();
//			bw0.close();

			
			sumFbp = sumFbp/(gt.getSize()[2] - 40);
			sumWce = sumWce/(gt.getSize()[2] - 40);
			sumWtv = sumWtv/(gt.getSize()[2] - 40);
			sumUnet = sumUnet/(gt.getSize()[2] - 40);
			sumDcr = sumDcr/(gt.getSize()[2] - 40);
			System.out.println(sumFbp + " " + sumWce + " " + sumWtv + " " + sumUnet + " " + sumDcr);
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
