package edu.stanford.rsl.Yixing.Celphalometric.crop;

import java.io.IOException;

import edu.stanford.rsl.Yixing.Celphalometric.superResolution.GenerateTestPatches;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class CropImagesFor512CephalogramEvaluation {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		CropImagesFor512CephalogramEvaluation obj = new CropImagesFor512CephalogramEvaluation();
		String path = "D:\\PIRM2018\\your_results\\";
		String savePath = "D:\\PIRM2018\\your_results\\crop\\";
		String saveName;
		ImagePlus imp;
		String imgNameIn;
		int width = 387;
		int height = 480;
		Grid2D img;
		int offset = 125;
		int offset2 = 28;
		Grid2D img2;
		
		
		for(int idx = 9; idx <= 9; idx++)
		{
			imgNameIn = path + "data" + idx + ".png";
			imp = IJ.openImage(imgNameIn);
			img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			img2 = obj.cropImage(img, offset, offset2, width, height);
			
			imp = ImageUtil.wrapGrid(img2, null);
			imgNameIn = savePath + "data" + idx + ".png";
			imp.setDisplayRange(0, 255);
        	IJ.run(imp, "8-bit", "");
        	IJ.run(imp, "RGB Color", "");
        	IJ.saveAs(imp, "png", imgNameIn);
		}
		System.out.println("finished!");

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
	
	public void resampleCelp(Grid2D celp, Grid2D input) {
		double[] idxy;
		for(int i = 0; i < celp.getSize()[0]; i++)
			for(int j = 0; j < celp.getSize()[1]; j++)
			{
				idxy = index2physical(celp, i, j);
				celp.setAtIndex(i, j, gridGetAtPhysical(input, idxy[0], idxy[1]));
			}
	}
	
	private Grid2D cropImage(Grid2D img, int offsetX, int offsetY, int width, int height) {
		Grid2D img2 = new Grid2D(width, height);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				img2.setAtIndex(i, j, img.getAtIndex(i + offsetX, j + offsetY));
		
		return img2;
	}

}
