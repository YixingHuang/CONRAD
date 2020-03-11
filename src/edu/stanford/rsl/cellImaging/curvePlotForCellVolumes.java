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

public class curvePlotForCellVolumes {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		curvePlotForCellVolumes obj = new curvePlotForCellVolumes();

		String folderPath = "D:\\Tasks\\FAU4\\CellImaging\\Tifs\\";
		String path0 = "D:\\Tasks\\FAU4\\CellImaging\\AlgaePhantoms\\11.tif";
		String path1 = folderPath + "Fbp.tif";
		String path2 = folderPath + "FbpPwls.tif";
		String path3 = folderPath + "SEUnet.tif";
		String path4 = folderPath + "SEUnetPwls.tif";
		String path5 = folderPath + "DCR.tif";
		String path6 = folderPath + "DCRPwls.tif";

		ImagePlus imp, imp1;
		imp = IJ.openImage(path0);
		Grid3D ref3D = ImageUtil.wrapImagePlus(imp);
		
		imp = IJ.openImage(path1);
		Grid3D recon3D1 = ImageUtil.wrapImagePlus(imp);
		imp = IJ.openImage(path2);
		Grid3D recon3D2 = ImageUtil.wrapImagePlus(imp);
		imp = IJ.openImage(path3);
		Grid3D recon3D3 = ImageUtil.wrapImagePlus(imp);
		imp = IJ.openImage(path4);
		Grid3D recon3D4 = ImageUtil.wrapImagePlus(imp);
		imp = IJ.openImage(path5);
		Grid3D recon3D5 = ImageUtil.wrapImagePlus(imp);
		imp = IJ.openImage(path6);
		Grid3D recon3D6 = ImageUtil.wrapImagePlus(imp);
		
		double r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0, r6 = 0;
		double ar1 = 0, ar2 = 0, ar3 = 0, ar4 = 0, ar5 = 0, ar6 = 0;
		double r1s2 = 0, r2s2 = 0, r3s2 = 0, r4s2 = 0, r5s2 = 0, r6s2 = 0; //for slice 250;
		double r1s3 = 0, r2s3 = 0, r3s3 = 0, r4s3 = 0, r5s3 = 0, r6s3 = 0; // for slice 355
		Grid2D ref2D, recon2D;
		for(int i = 0; i < ref3D.getSize()[2]; i++) {
			ref2D = ref3D.getSubGrid(i);
			ref2D = obj.downSampling(ref2D);
			if(i == 212) {
				imp1 = ImageUtil.wrapGrid(ref2D, null);
			    IJ.saveAs(imp1, "Tiff", folderPath + "refS213.tif");
			}
			obj.keepFOV(ref2D);
			
			recon2D = recon3D1.getSubGrid(i);
			if(i == 212) {
				imp1 = ImageUtil.wrapGrid(recon2D, null);
			    IJ.saveAs(imp1, "Tiff", folderPath + "FbpS213.tif");
			}
			obj.keepFOV(recon2D);
			r1 = RMSE(ref2D, recon2D);
			ar1 += r1;
			if(i == 249)			
				r1s2 = r1;
			else if(i == 354)
				r1s3 = r1;
			
			recon2D = recon3D2.getSubGrid(i);
			if(i == 212) {
				imp1 = ImageUtil.wrapGrid(recon2D, null);
			    IJ.saveAs(imp1, "Tiff", folderPath + "FbpPwlsS213.tif");
			}
			obj.keepFOV(recon2D);
			r2 = RMSE(ref2D, recon2D);
			ar2 += r2;
			if(i == 249)
				r2s2 = r2;
			else if(i == 354)
				r2s3 = r2;
			
			recon2D = recon3D3.getSubGrid(i);
			if(i == 212) {
				imp1 = ImageUtil.wrapGrid(recon2D, null);
			    IJ.saveAs(imp1, "Tiff", folderPath + "UNetS213.tif");
			}
			obj.keepFOV(recon2D);
			r3 = RMSE(ref2D, recon2D);
			ar3 += r3;
			if(i == 249)
				r3s2 = r3;
			else if(i == 354)
				r3s3 = r3;
			
			recon2D = recon3D4.getSubGrid(i);
			if(i == 212) {
				imp1 = ImageUtil.wrapGrid(recon2D, null);
			    IJ.saveAs(imp1, "Tiff", folderPath + "UNetPwlsS213.tif");
			}
			obj.keepFOV(recon2D);
			r4 = RMSE(ref2D, recon2D);
			ar4 += r4;
			if(i == 249)
				r4s2 = r4;
			else if(i == 354)
				r4s3 = r4;
			
			recon2D = recon3D5.getSubGrid(i);
			obj.keepFOV(recon2D);
			r5 = RMSE(ref2D, recon2D);
			ar5 += r5;
			if(i == 249)
				r5s2 = r5;
			else if(i == 354)
				r5s3 = r5;
			
			recon2D = recon3D6.getSubGrid(i);
			obj.keepFOV(recon2D);
			r6 = RMSE(ref2D, recon2D);
			ar6 += r6;
			if(i == 249)
				r6s2 = r6;
			else if(i == 354)
				r6s3 = r6;
		}
		
		System.out.print("slice 250: " + r1s2 + ", " + r2s2 + ", " + r3s2 + ", " + r4s2 + ", " + r5s2 + ", " + r6s2 + "");
		System.out.println(" ");
		
		System.out.print("slice 355: " + r1s3 + ", " + r2s3 + ", " + r3s3 + ", " + r4s3 + ", " + r5s3 + ", " + r6s3 + "");
		System.out.println(" ");
		
		System.out.print("average error: " + ar1/512.0 + ", " + ar2/512.0 + ", " + ar3/512.0 + ", " + ar4/512.0 + ", " + ar5/512.0 + ", " + ar6/512.0 + "");
		System.out.println(" ");
	}
	
	public Grid2D downSampling(Grid2D img)
	{
		int sizeX = img.getSize()[0];
		int sizeY = img.getSize()[1];
		int sizeX2 = sizeX/2;
		int sizeY2 = sizeY/2;
		Grid2D imgDown = new Grid2D(sizeX2, sizeY2);
		float val = 0;
		for(int i = 0; i < sizeX2; i++)
			for(int j = 0; j < sizeY2; j++)
			{
				val = 0;
				for(int m = 0; m <2; m++)
					for(int n = 0; n < 2; n++)
						val = val + img.getAtIndex(i * 2 + m, j * 2 + n);
				val = val/4.0f;
				imgDown.setAtIndex(i, j, val);
			}
		return imgDown;			
	}
	
	private static double RMSE(Grid2D recon, Grid2D recon_data) {
		double err = 0;
		Grid2D temp = new Grid2D(recon);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		err = temp.getGridOperator().sum(temp);
		err = err / (temp.getSize()[0] * temp.getSize()[1]);
		err = Math.sqrt(err) /50.0;
		return err;
	}
	
	private void addFOVCircle(Grid2D phan)
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
				if(dd <= rr && dd > rr - 250)
					phan.setAtIndex(i, j, 1.0f);
			}
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
