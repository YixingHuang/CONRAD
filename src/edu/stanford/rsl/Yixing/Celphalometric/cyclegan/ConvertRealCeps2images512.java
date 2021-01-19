package edu.stanford.rsl.Yixing.Celphalometric.cyclegan;

import java.io.File;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;

public class ConvertRealCeps2images512 {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		ConvertRealCeps2images512 obj = new ConvertRealCeps2images512();

		
		String inputPath = "D:\\Tasks\\FAU4\\Cephalometric\\SuperResolutionImages2\\";
		String savePathA = "D:\\CycleGAN-tensorflow\\datasets\\volume2cepImg\\trainB\\";

		String inputName, outputName;
	
		ImagePlus imp;
		Grid2D input;
		Grid2D patch1, patch2, patch3, patch4;

		for(int idx = 1; idx <= 150; idx++)
		{
			inputName = inputPath + "data" + idx + ".png";
			File outPutDir=new File(inputName);
			if (!outPutDir.exists())
				continue;
			imp = IJ.openImage(inputName);
			input = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
	
			patch1 = obj.imgPadding(input, 512);
			imp = ImageUtil.wrapGrid(patch1, null);
			imp.setDisplayRange(0, 255);
			IJ.run(imp, "RGB Color", "");
    		outputName = savePathA + idx + ".png";
    		IJ.saveAs(imp, "png", outputName); 		
    		
		}
		System.out.println("finished!");
	}
	
	
	Grid2D imgPadding(Grid2D input, int sz)
	{
		Grid2D img = new Grid2D(sz, sz);
		int offsetx = sz - input.getWidth();
		int offsety = sz - input.getHeight();


		
		for(int i = 0; i < input.getWidth(); i++)
			for(int j = 0; j < input.getHeight(); j++)
				img.setAtIndex(i + offsetx, j + offsety, input.getAtIndex(i, j));
		
		return img;
	}
}
