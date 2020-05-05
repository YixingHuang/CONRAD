package edu.stanford.rsl.Yixing.cellImaging;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class MoveSlice {
	
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\CellImaging\\AlgaeCellsProcessed3\\";
		String path2 = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon120\\AllSlices\\";
		
		ImagePlus imp1;
		String name1, saveName1;
		Grid2D data;

		int gidx;
		int shift = 266;
		for(int idx = 0; idx<= 124; idx ++){
			gidx = idx + shift;
			name1 = path + idx + ".tif";

			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
			data = rotateImage90Deg(data, 1);
			saveName1 = path2 + gidx + ".tif";

			imp1 = ImageUtil.wrapGrid(data, null);
			IJ.saveAs(imp1, "Tiff", saveName1);
		}
		System.out.println("finished");
	}
	
	
	static private Grid2D rotateImage90Deg(Grid2D img, int tag)
	{
		int sizeX = img.getSize()[0];
		int sizeY = img.getSize()[1];
		Grid2D imgRot = new Grid2D(img);
		switch (tag)
		{
		case 1:
			for(int i = 0; i < img.getSize()[0]; i++)
				for(int j = 0; j < img.getSize()[1]; j++)
					imgRot.setAtIndex(sizeY - 1 - j, i, img.getAtIndex(i, j));
			break;
		case 2:
			for(int i = 0; i < img.getSize()[0]; i++)
				for(int j = 0; j < img.getSize()[1]; j++)
					imgRot.setAtIndex(sizeX - 1 - i, sizeY - 1 - j, img.getAtIndex(i, j));
			break;
		case 3:
			for(int i = 0; i < img.getSize()[0]; i++)
				for(int j = 0; j < img.getSize()[1]; j++)
					imgRot.setAtIndex(j, sizeX - 1 - i, img.getAtIndex(i, j));
			break;
		default:
			break;
		}
		
		
		
		return imgRot;
	}

}
