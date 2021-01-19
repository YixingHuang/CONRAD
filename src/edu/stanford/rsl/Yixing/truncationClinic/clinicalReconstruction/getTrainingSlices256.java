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

public class getTrainingSlices256 {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		getTrainingSlices256 obj = new getTrainingSlices256();
		String pathInput = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\CQ500ReconsNoisy2\\";
		String pathOutput = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\trainingData_d10\\";
		String trainingPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\train3\\";
		String valPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\pix2pixValData_d10\\";
		String savePath, savePath2;
		String saveName, saveName2;
		ImagePlus imp;
		Grid2D input, output;
		Grid2D ds, us, bs;
		Grid2D gtcopy;
		String pathIn, pathOut;
		String imgNameIn, imgNameOut;
		int startX, startY;
		int saveId = 1;
		Grid3D wce, artifact;
		Grid2D patchIn, patchOut, merge, patchIn2, patchOut2;
		int szIn = 256;
		int ycut = 0;
		
		patchIn = new Grid2D(szIn, szIn);
		patchOut = new Grid2D(szIn, szIn);
		int id = 0;
		for(int patient = 0; patient <= 52; patient ++) {
			pathIn = pathInput + "WCE" + patient + ".tif";
			pathOut = pathInput + "artifact" + patient + ".tif";
			imp =IJ.openImage(pathIn);
			wce = ImageUtil.wrapImagePlus(imp);
			imp =IJ.openImage(pathOut);
			artifact = ImageUtil.wrapImagePlus(imp);
			for(int z = 15; z < 281; z = z + 10) {
				id = (patient + 1) * 1000 + z;
				patchIn2 = (Grid2D)wce.getSubGrid(z).clone();
				patchOut2 = (Grid2D)artifact.getSubGrid(z).clone();
				patchIn = obj.subsampling(patchIn2, 2);
				patchOut = obj.subsampling(patchOut2, 2);
				if(patient <= 52) {
					savePath = trainingPath;
				}
				else
				{
					savePath = valPath;
				}
            	merge = obj.mergeImages(patchIn, patchOut);
            	saveName = savePath + id + ".tif";
            	imp = ImageUtil.wrapGrid(merge, null);
            	IJ.saveAs(imp, "Tiff", saveName);	


	            System.out.print(patient + " ");
			}
		}
		System.out.println("Finished!");
		System.out.println("Finished!");
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
		img2.setOrigin(-(img2.getSize()[0] - 1.0) * img2.getSpacing()[0]/2.0, -(img2.getSize()[1] - 1.0) * img2.getSpacing()[1]/2.0);
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
