package edu.stanford.rsl.Yixing.truncationClinic.clinicalReconstruction;

import java.io.File;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import flanagan.interpolation.*;

public class getDifferenceImages2 {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		getDifferenceImages2 obj = new getDifferenceImages2();
		String pathInput = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicRecons\\";
		String pathOutput = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\trainingData_d10\\";
		String trainingPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\trainingSlices\\";
		String valPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\pix2pixValData_d10\\";
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\test3_10\\";
		String saveName, saveName2;
		ImagePlus imp;
		Grid2D input, output;
		Grid2D ds, us, bs;
		Grid2D gtcopy;
		String FDKRefPath, wTVRefPath, pixPath;
		String imgNameIn, imgNameOut;
		int startX, startY;
		int saveId = 1;
		Grid3D fakRef, wtvRef, pix;
		Grid2D patchIn, patchOut, merge, patchIn2, patchOut2, pix2d;;
		int szIn = 256;
		int ycut = 0;
		Grid3D fdkRef256, wtvRef256;
		
		
		patchIn = new Grid2D(szIn, szIn);
		patchOut = new Grid2D(szIn, szIn);
		int id = 0;
		Grid2D pixDiff1, pixDiff2, diff2D512;
		diff2D512 = new Grid2D(512, 512);
		Grid3D diff1, diff2;
		diff1 = new Grid3D(512, 512, 300);
		diff2 = new Grid3D(512, 512, 300);
		double rmse1, rmse2;
		for(int patient = 2; patient <= 2; patient ++) {
			FDKRefPath = pathInput + "FDKPWLS1.tif";
			wTVRefPath = pathInput + "wTV_Ref_30.tif";
			pixPath = pathInput + "pix2pix4.tif";
			imp =IJ.openImage(FDKRefPath);
			fakRef = ImageUtil.wrapImagePlus(imp);
			imp =IJ.openImage(wTVRefPath);
			wtvRef = ImageUtil.wrapImagePlus(imp);
			imp =IJ.openImage(pixPath);
			pix = ImageUtil.wrapImagePlus(imp);
//			for(int z = 0; z < fakRef.getSize()[2]; z++) {
			for(int z = 67; z < 68; z++) {
				id = (patient) * 100000 + z;
				
				patchIn2 = (Grid2D)fakRef.getSubGrid(z).clone();
				patchOut2 = (Grid2D)wtvRef.getSubGrid(z).clone();
				patchIn = obj.subsampling(patchIn2, 2);
				patchOut = obj.subsampling(patchOut2, 2);
            	pix2d = (Grid2D)pix.getSubGrid(z).clone();
            	rmse1 = obj.RMSE(patchIn, pix2d);
            	rmse2 = obj.RMSE(patchOut, pix2d);
            	System.out.println(rmse1 + " " + rmse2);
            	pixDiff1 = new Grid2D(pix2d);
            	pixDiff2 = new Grid2D(pix2d);
            	pixDiff1.getGridOperator().subtractBy(pixDiff1, patchIn);
            	pixDiff2.getGridOperator().subtractBy(pixDiff2, patchOut);
	            System.out.print(z + " ");
	            pixDiff1.setSpacing(1, 1);
	            pixDiff1.setOrigin(-(256 - 1.0) * 1.0/2.0, -(256 - 1.0) * 1.0/2.0);
	            pixDiff2.setSpacing(1, 1);
	            pixDiff2.setOrigin(-(256 - 1.0) * 1.0/2.0, -(256 - 1.0) * 1.0/2.0);
				diff2D512.setSpacing(0.5, 0.5);
				diff2D512.setOrigin(-(512 - 1.0) * 0.5/2.0, -(512 - 1.0) * 0.5/2.0);
				obj.upsampling2(pixDiff1, diff2D512);
				diff1.setSubGrid(z, (Grid2D)diff2D512.clone());
				obj.upsampling2(pixDiff2, diff2D512);
				diff2.setSubGrid(z, (Grid2D)diff2D512.clone());
			}
			diff1.clone().show("DiffFDK");
			diff2.clone().show("DiffwTV");
		}
		System.out.println("Finished!");
		System.out.println("Finished!");
	}
	
	/**
	 * ROI RMSE
	 * @param recon
	 * @param recon_data
	 * @return
	 */
	private double RMSE_FOV(Grid2D recon, Grid2D recon_data) {
		double err = 0;
		Grid2D temp = new Grid2D(recon);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		double sum = 0;
		int count = 0;
		float x, y;
		float thres = 61;
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
		return err * 2000.0;
	}
	
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
	
	/**
	 * Upsampling using bicubic interpolation
	 * @param imgInput
	 * @param imgOutPut
	 */
	public void upsampling2(Grid2D imgInput, Grid2D imgOutPut)
	{
		double[][] y = new double[imgInput.getHeight()][imgInput.getWidth()];
		for(int j = 0; j < imgInput.getHeight(); j++)
			for(int i = 0; i < imgInput.getWidth(); i++)
				y[j][i] = (double)imgInput.getAtIndex(i, j);
		
		double[] x1 = new double[imgInput.getHeight()];
		for(int i = 0; i < imgInput.getHeight(); i++)
		{
			x1[i] = i * imgInput.getSpacing()[1] + imgInput.getOrigin()[1];
		}
		
		double[] x2 = new double[imgInput.getWidth()];
		for(int i = 0; i < imgInput.getWidth(); i++)
		{
			x2[i] = i * imgInput.getSpacing()[0] + imgInput.getOrigin()[0];
		}
		
		BiCubicInterpolation bci = new BiCubicInterpolation(x1, x2, y, 0);
//		BiCubicSpline bci = new BiCubicSpline(x1, x2, y);
		double x1Min = imgInput.getOrigin()[1];
		double x1Max = imgInput.getOrigin()[1] + imgInput.getSpacing()[1] * (imgInput.getSize()[1] - 1);
		double x2Min = imgInput.getOrigin()[0];
		double x2Max = imgInput.getOrigin()[0] + imgInput.getSpacing()[0] * (imgInput.getSize()[0] - 1);
		double[] xx1 = new double[imgOutPut.getHeight()];
		
		for(int i = 0; i < imgOutPut.getHeight(); i++)
			xx1[i] = i * imgOutPut.getSpacing()[1] + imgOutPut.getOrigin()[1];

		double[] xx2 = new double[imgOutPut.getWidth()];
		for(int i = 0; i < imgOutPut.getWidth(); i++)
			xx2[i] = i * imgOutPut.getSpacing()[0] + imgOutPut.getOrigin()[0];
		
		for(int i = 0; i < imgOutPut.getWidth(); i++)
			for(int j = 0; j < imgOutPut.getHeight(); j++)
			{
				if(xx1[j] < x1Min || xx1[j] > x1Max || xx2[i] < x2Min || xx2[i] > x2Max)
					imgOutPut.setAtIndex(i, j, 0);
				else
					imgOutPut.setAtIndex(i, j, (float)(bci.interpolate(xx1[j], xx2[i])));
			}
		
	}
	
	Grid2D mergeImages(Grid2D data2D, Grid2D mask2D) {
		Grid2D merge = new Grid2D(data2D.getSize()[0] * 2, data2D.getSize()[1]);
		for(int i = 0; i < data2D.getSize()[0]; i++)
			for(int j = 0; j < data2D.getSize()[1]; j++)
			{
				merge.setAtIndex(i, j, data2D.getAtIndex(i, j));
				merge.setAtIndex(i + data2D.getSize()[0], j, mask2D.getAtIndex(i, j));
			}
		
		return merge;
	}
	
	public void thresholding(Grid2D img, float thres) {
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				if(img.getAtIndex(i, j) > thres)
					img.setAtIndex(i, j, thres);
	}

	public Grid2D subsampling(Grid2D img, int factor)
	{
		float val = 0;
		int count = 0;
		int nx, ny;
		int width = (img.getWidth() - 1)/factor + 1;
		int height = (img.getHeight() - 1)/factor + 1;
		Grid2D img2 = new Grid2D(width, height);
		img2.setSpacing(img.getSpacing()[0] * factor, img.getSpacing()[1] * factor);
		img2.setOrigin(img.getOrigin()[0] + img.getSpacing()[0] * (factor - 1)/2.0, img.getOrigin()[1] + img.getSpacing()[1] * (factor - 1)/2.0);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				val = 0; count = 0;
				for(int m = 0; m < factor; m++)
					for(int n = 0; n < factor; n++)
					{
						nx = i * factor + m;
						ny = j * factor + n;
						if(nx < img.getWidth() && ny < img.getHeight())
						{
							val = val + img.getAtIndex(nx, ny);
							count++;
						}
					}
				img2.setAtIndex(i, j, val/count);
			}
		
		return img2;
	}
	
	public Grid2D upsampling(Grid2D img, int factor)
	{
		double[][] y = new double[img.getHeight()][img.getWidth()];
		for(int j = 0; j < img.getHeight(); j++)
			for(int i = 0; i < img.getWidth(); i++)
				y[j][i] = (double)img.getAtIndex(i, j);
		
		double[] x1 = new double[img.getHeight()];
		for(int i = 0; i < img.getHeight(); i++)
		{
			x1[i] = i * img.getSpacing()[1];
		}
		System.out.println(" ");
		
		double[] x2 = new double[img.getWidth()];
		for(int i = 0; i < img.getWidth(); i++)
		{
			x2[i] = i * img.getSpacing()[0];
		}
		System.out.println(" ");
		
		BiCubicInterpolation bci = new BiCubicInterpolation(x1, x2, y, 0);
//		BiCubicSpline bci = new BiCubicSpline(x1, x2, y);
		Grid2D img2 = new Grid2D((img.getWidth() - 1) * factor + 1, (img.getHeight() - 1) * factor + 1);
		img2.setSpacing(img.getSpacing()[0]/factor, img.getSpacing()[1]/factor);
		img2.setOrigin(-(img2.getSize()[0] - 1.0) * img2.getSpacing()[0]/2.0, -(img2.getSize()[1] - 1.0) * img2.getSpacing()[1]/2.0);
		
		double[] xx1 = new double[img2.getHeight()];
		for(int i = 0; i < img2.getHeight(); i++)
			xx1[i] = i * img2.getSpacing()[1];

		
		double[] xx2 = new double[img2.getWidth()];
		for(int i = 0; i < img2.getWidth(); i++)
			xx2[i] = i * img2.getSpacing()[0];
		

		for(int i = 0; i < img2.getWidth(); i++)
			for(int j = 0; j < img2.getHeight(); j++)
			{
				img2.setAtIndex(i, j, (float)(bci.interpolate(xx1[j], xx2[i])));
			}
		
		return img2;
	}
	
	
	public float gridGetAtPhysical(Grid2D img, double x, double y)
	{
		double[] origin = img.getOrigin();
		double idx = (x - origin[0])/img.getSpacing()[0];
		double idy = (y - origin[1])/img.getSpacing()[1];
		int x_floor = (int) Math.floor(idx);
		int x_ceiling = x_floor + 1;
		int y_floor = (int) Math.floor(idy);
		int y_ceiling = y_floor + 1;
		if (x_floor < 0 || x_floor > img.getSize()[0] - 1)
			return 0;
		else if(x_ceiling < 0 || x_ceiling > img.getSize()[0] - 1)
			return 0;
		else if(y_floor < 0 || y_floor > img.getSize()[1] - 1)
			return 0;
		else if(y_ceiling < 0 || y_ceiling > img.getSize()[1] - 1)
			return 0;
		else
		{
			float val = 0;
			float x_res = (float)(idx - x_floor);
			float y_res = (float)(idy - y_floor);
			val = img.getAtIndex(x_floor, y_floor) * (1 - x_res) * (1 - y_res)
					+ img.getAtIndex(x_ceiling, y_floor) * x_res * (1 - y_res)
					+ img.getAtIndex(x_floor, y_ceiling) * (1 - x_res) * y_res
					+ img.getAtIndex(x_ceiling, y_ceiling) * x_res * y_res;
			return val;
		}
	}
	
	public double[] index2physical(Grid2D img, int i, int j)
	{
		double [] idxy = new double[2];
	    if(img.getOrigin()[0] == 0)
	    {
	    	img.setOrigin(-(img.getSize()[0] - 1.0) * img.getSpacing()[0]/2.0, -(img.getSize()[1] - 1.0) * img.getSpacing()[1]/2.0);
	    }
	        idxy[0] = img.getSpacing()[0] * i + img.getOrigin()[0];
	        idxy[1] = img.getSpacing()[1] * j + img.getOrigin()[1];
	        
	        return idxy;
	}
	
	public void resampleGrid(Grid2D output, Grid2D input) {
		double[] idxy;
		for(int i = 0; i < output.getSize()[0]; i++)
			for(int j = 0; j < output.getSize()[1]; j++)
			{
				idxy = index2physical(output, i, j);
				output.setAtIndex(i, j, gridGetAtPhysical(input, idxy[0], idxy[1]));
			}
	}
}
