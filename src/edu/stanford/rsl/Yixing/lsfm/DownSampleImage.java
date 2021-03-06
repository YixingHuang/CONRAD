package edu.stanford.rsl.Yixing.lsfm;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.pwls.PenalizedWeightedLeastSquare;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class DownSampleImage {
	
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		DownSampleImage obj = new DownSampleImage();
		String path =  "E:\\LSFM Data\\Subtracted\\NZ17T0Subtracted180.tif";
		ImagePlus imp0 =IJ.openImage(path);
		Grid3D img = ImageUtil.wrapImagePlus(imp0);
		
		Grid3D img2 =  new Grid3D(img.getSize()[0]/4, img.getSize()[1]/4, img.getSize()[2]);
		for(int i = 0; i < img.getSize()[2]; i++)
		{
			System.out.print(i + " ");
			img2.setSubGrid(i, obj.downSampling(img.getSubGrid(i), 4));
		}
		    
		System.out.println(" ");
		//String path3 = "D:\\Tasks\\FAU4\\CellImaging\\projectionsPwls1Iter.tif";
		String path3 = "E:\\LSFM Data\\SubtractedDS\\NZ17T0SubtractedDS180.tif";

		imp0 = ImageUtil.wrapGrid3D(img2, null);
	    IJ.saveAs(imp0, "Tiff", path3);
	    System.out.println("finished");
	}
	
	public Grid2D downSampling(Grid2D img, int factor)
	{
		int sizeX = img.getSize()[0];
		int sizeY = img.getSize()[1];
		int sizeX2 = (int)(sizeX/factor);
		int sizeY2 = (int)(sizeY/factor);
		int factor2 = factor * factor;
		Grid2D imgDown = new Grid2D(sizeX2, sizeY2);
		float val = 0;
		for(int i = 0; i < sizeX2; i++)
			for(int j = 0; j < sizeY2; j++)
			{
				val = 0;
				for(int m = 0; m <factor; m++)
					for(int n = 0; n < factor; n++)
						val = val + img.getAtIndex(i * factor + m, j * factor + n);
				val = val/factor2;
				imgDown.setAtIndex(i, j, val);
			}
		return imgDown;			
	}

}
