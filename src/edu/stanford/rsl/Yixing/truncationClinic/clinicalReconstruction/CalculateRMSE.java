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

public class CalculateRMSE {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicRecons\\";
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicRecons\\PNGs\\";
		String saveName;
		CalculateRMSE obj = new CalculateRMSE();
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
		double rmseWce, rmsePix, rmseScale, rmsePdp;
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
				
				rmseWce = obj.RMSE(fbpgt2d, wce2d);
				rmsePix = obj.RMSE(pix2d, fbpgt2d);
				rmseScale = obj.RMSE(scale2d, fbpgt2d);
				rmsePdp = obj.RMSE(pdp2d, gt2d);
				
				System.out.println(i + " wce: " + rmseWce + " pix: " + rmsePix + " scale: " + rmseScale + " pdp: " + rmsePdp);


		
			}

		}
		
		
	}
	
	
	/**
	 * ROI RMSE
	 * @param recon
	 * @param recon_data
	 * @return
	 */
//	private double RMSE_FOV(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		double sum = 0;
//		int count = 0;
//		float x, y;
//		float thres = 122;
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
//		return err * 2000.0;
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
				{
					count ++;
					sum = sum + temp.getAtIndex(i, j);
				}
		}
		err = sum /count;
		err = Math.sqrt(err);
		return err * 2000.0;
	}
//	
	
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
