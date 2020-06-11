package edu.stanford.rsl.Yixing.Celphalometric;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import flanagan.interpolation.*;

public class GenerateSuperResolutionTrainingPatchesLR {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateSuperResolutionTrainingPatchesLR obj = new GenerateSuperResolutionTrainingPatchesLR();
		String path = "D:\\Tasks\\FAU4\\Cephalometric\\SuperResolutionImages2\\";
		String trainingPath = "D:\\imageSuperResolutionV2_1\\low_res\\trainingss\\";
		String trainingPath2 = "D:\\imageSuperResolutionV2_1\\high_res\\training\\";
		String valPath = "D:\\imageSuperResolutionV2_1\\low_res\\validationss\\";
		String valPath2 = "D:\\imageSuperResolutionV2_1\\high_res\\validation\\";
		String savePath, savePath2;
		String saveName, saveName2;
		ImagePlus imp;
		Grid2D input, output;
		Grid2D ds, us, bs;
		Grid2D gtcopy;
		String imgNameIn, imgNameOut;
		int startX, startY;
		int saveId = 1;
		Grid2D patchIn, patchOut, merge;
		int szIn = 64;
		int factor = 5;
		int szOut = szIn * factor;
		
		patchIn = new Grid2D(szIn, szIn);
		patchOut = new Grid2D(szOut, szOut);
		for(int idx = 1; idx <=150; idx ++) {
			imgNameIn = path + "data" + idx + "_rs2.png";
			imp = IJ.openImage(imgNameIn);
			input = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			imgNameOut = path + "data" + idx + "_mask.png";
			imp = IJ.openImage(imgNameOut);
			output = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			if(idx <= 100) {
				savePath = trainingPath;
				savePath2 = trainingPath2;
			}
			else
			{
				savePath = valPath;
				savePath2 = valPath2;
			}
            for(int i = 0; i < 6; i++) {
            	for(int j = 0; j < 7; j++ ) {
            		startX = i * szIn;
            		startY = j * szIn;
//            		saveId = idx * 100 + j * 10 + i;
            		for(int x = 0; x < szIn; x++)
            			for(int y = 0; y < szIn; y++)
            				patchIn.setAtIndex(x, y, input.getAtIndex(startX + x, startY + y));
            			
            		for(int x = 0; x < szOut; x++)
                		for(int y = 0; y < szOut; y++)
                			patchOut.setAtIndex(x, y, output.getAtIndex(startX * factor + x, startY * factor + y));

            		//this is for merged images
//            		merge = obj.mergeImages(patchIn, patchOut);
//            		saveName = savePath + saveId + ".png";
//            		imp = ImageUtil.wrapGrid(merge, null);
//            		imp.setDisplayRange(0, 255);
//            		IJ.saveAs(imp, "png", saveName);
            		
            		saveName = savePath + saveId + ".png";
            		imp = ImageUtil.wrapGrid(patchIn, null);
            		imp.setDisplayRange(0, 255);
//            		IJ.run(imp, "RGB Color", "");
            		IJ.saveAs(imp, "png", saveName);
            		saveName2 = savePath2 + saveId + ".png";
            		imp = ImageUtil.wrapGrid(patchOut, null);
            		imp.setDisplayRange(0, 255);
//            		IJ.run(imp, "RGB Color", "");
            		IJ.saveAs(imp, "png", saveName2);
            		saveId ++;
            	}
            }
            System.out.print(idx + " ");
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
	
	public Grid2D subsampling2(Grid2D img, int factor)
	{
		int width = (img.getWidth() - 1)/factor + 1;
		int height = (img.getHeight() - 1)/factor + 1;
		Grid2D img2 = new Grid2D(width, height);
		img2.setSpacing(img.getSpacing()[0] * factor + img.getSize()[1] * factor);
		img2.setOrigin(-(img2.getSize()[0] - 1.0) * img2.getSpacing()[0]/2.0, -(img2.getSize()[1] - 1.0) * img2.getSpacing()[1]/2.0);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				img2.setAtIndex(i, j, img.getAtIndex(i * factor, j * factor));
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
