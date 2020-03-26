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

public class CalculateRmseFOVForEachPatient {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\recon\\";
		String uNetPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\UNetRecons\\";
		String wTvPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\wTV\\";
		String DcrPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\DCR\\";

		ImagePlus imp1;
		String nameGT, nameWCE, nameFBP, nameUNet, nameWTV, nameDCR;
		Grid3D gt, fbp = null, wce, wtv, unet, dcr;
		Grid2D gt2d, fbp2d, wce2d, wtv2d, unet2d, dcr2d;
		double rmseFbp = 0, rmseWce, rmseWtv, rmseUnet, rmseDcr;
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
			
//			if(idx==3)
//			{
//			imp1=IJ.openImage(nameFBP);
//			fbp = ImageUtil.wrapImagePlus(imp1);
//			}
//			
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
//				if(idx == 3)
//				{
//					fbp2d = (Grid2D) fbp.getSubGrid(i).clone();
//					rmseFbp = RMSE_FOV(fbp2d, gt2d);
//					
//				}
				wce2d = (Grid2D) wce.getSubGrid(i).clone();
				wtv2d = (Grid2D) wtv.getSubGrid(i).clone();
				unet2d = (Grid2D) unet.getSubGrid(i).clone();
				dcr2d = (Grid2D) dcr.getSubGrid(i).clone();
				
				
				rmseWce = RMSE_FOV(wce2d, gt2d);
				rmseWtv = RMSE_FOV(wtv2d, gt2d);
				rmseUnet = RMSE_FOV(unet2d, gt2d);
				rmseDcr = RMSE_FOV(dcr2d, gt2d);
				
				sumFbp += rmseFbp;
				sumWce += rmseWce;
				sumWtv += rmseWtv;
				sumUnet += rmseUnet;
				sumDcr += rmseDcr;
//				bw0.write(rmse0 + "\r\n");
//				bw0.flush();	
//		
//				bw1.write(rmse1 + "\r\n");
//				bw1.flush();
//				if(idx == 3 && i == 174)
//					System.out.println(idx + " " + i + ":" + rmseFbp + " " + rmseWce + " " + rmseWtv + " " + rmseUnet + " " + rmseDcr);
//				
//				if(idx == 18 && i == 149)
//					System.out.println(idx + " " + i + ":" + rmseFbp + " " + rmseWce + " " + rmseWtv + " " + rmseUnet + " " + rmseDcr);
//				
//				if(idx == 2 && i == 186)
//					System.out.println(idx + " " + i + ":" + rmseFbp + " " + rmseWce + " " + rmseWtv + " " + rmseUnet + " " + rmseDcr);
//				
//				if(idx == 9 && i == 122)
//					System.out.println(idx + " " + i + ":" + rmseFbp + " " + rmseWce + " " + rmseWtv + " " + rmseUnet + " " + rmseDcr);
				
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
	
//	private static double RMSE(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		err = temp.getGridOperator().sum(temp);
//		err = err / (temp.getSize()[0] * temp.getSize()[1]);
//		err = Math.sqrt(err);
//		return err;
//	}
//	
	/**
	 * ROI RMSE
	 * @param recon
	 * @param recon_data
	 * @return
	 */
	private static double RMSE_FOV(Grid2D recon, Grid2D recon_data) {
		double err = 0;
		Grid2D temp = new Grid2D(recon);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		double sum = 0;
		int count = 0;
		float x, y;
		float thres = 97;//correct one
//		float thres = 90;//correct one
		float thres2 = thres * thres;
		for(int i = 0; i < recon.getSize()[0]; i ++)
		{
			x = i - (recon.getSize()[0] - 1)/2.0f;
			for(int j = 0; j < recon.getSize()[1]; j++)
			{
				y = j - (recon.getSize()[1] - 1)/2.0f;
				if(x * x + y * y < thres2)
				{
					count ++;
					sum = sum + temp.getAtIndex(i, j);
				}
			}
		}
		err = sum /count;
		err = Math.sqrt(err);
		return err * 2040.0;
	}
//	

}
