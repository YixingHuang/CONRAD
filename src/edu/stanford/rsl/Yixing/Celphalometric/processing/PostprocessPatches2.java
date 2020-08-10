package edu.stanford.rsl.Yixing.Celphalometric.processing;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class PostprocessPatches2 {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		PostprocessPatches2 obj = new PostprocessPatches2();
		String inputPath = "C:\\Users\\Yixing Huang\\Desktop\\results\\2-outputsEdgeLoss.png";
		String inputPath2 = "C:\\Users\\Yixing Huang\\Desktop\\results\\2-outputs.png";
		ImagePlus imp = IJ.openImage(inputPath);
		Grid2D img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img.clone().show("img");
		imp = IJ.openImage(inputPath2);
		Grid2D img2 = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2.clone().show("img2");
		for(int i = 0; i < 256; i++)
			for(int j = 0; j < 256; j++)
			{
				if((img2.getAtIndex(i, j) < 50) && (img.getAtIndex(i, j) > img2.getAtIndex(i, j)))
					img2.setAtIndex(i, j, img.getAtIndex(i, j));
			}
		img2.clone().show("img22");
	}
	

	
}
