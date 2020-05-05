package edu.stanford.rsl.Yixing.cellImaging;


import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class ZoomOutImages {

	public static void main(String[] args) {
		new ImageJ();
		String folder = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon120\\AllSlices\\";
		String imagePath;
		int dim = 512;
		ZoomOutImages obj = new ZoomOutImages();
		ImagePlus imp;
		Grid2D imgRaw;
		for(int i = 1616; i < 2371; i++) {
			imagePath = folder + i + ".tif";
			imp = IJ.openImage(imagePath);
			imgRaw = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			imgRaw = obj.downSampling(imgRaw);
			imgRaw = obj.paddingImage(imgRaw, dim);
			imp = ImageUtil.wrapGrid(imgRaw, null);
		    IJ.saveAs(imp, "Tiff", imagePath);
		    System.out.print(i + " ");
		}
		System.out.println(" ");
		System.out.println("Finished!");
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
	
	public Grid2D paddingImage(Grid2D imgRaw, int dim)
	{
		Grid2D img = new Grid2D(dim, dim);
		int xDim = imgRaw.getSize()[0];
		int yDim = imgRaw.getSize()[1];
		int xShift = 0;
		int yShift = 0;
		int xRawShift = 0;
		int yRawShift = 0;
		int xDimMin = dim, yDimMin = dim;
		if(xDim < dim)
		{
			xShift = (int) ((dim - xDim)/2);
			xDimMin = xDim;
			
		}
		if(yDim < dim)
		{
			yShift = (int) ((dim - yDim)/2);
			yDimMin = yDim;
		}
		for(int i = 0; i < xDimMin; i++)
			for(int j = 0; j < yDimMin; j++)
			{
				img.setAtIndex(i + xShift, j + yShift, imgRaw.getAtIndex(i + xRawShift, j + yRawShift));
			}
		
		return img;
	}
}
