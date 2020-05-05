package edu.stanford.rsl.Yixing.cellImaging;

/**
 * @author Yixing Huang
 * Split the 3D volume into small slices for training
 */
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import java.io.File;
import java.io.IOException;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.weightedtv.Perform2DWeightedTV;
import edu.stanford.rsl.tutorial.weightedtv.TVGradient;


public class RemoveFOV {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		RemoveFOV obj = new RemoveFOV();
		String folder = "D:\\Tasks\\FAU4\\CellImaging\\FbpCellRecons100DegreePwls\\";
		String path = folder + "reconFbp3DCombinedOriginalP.tif";

		String path4 = folder + "reconFbp3DCombinedOriginalPNoFOV5.tif";
		ImagePlus imp0 =IJ.openImage(path);
		Grid3D vol = ImageUtil.wrapImagePlus(imp0);
		vol.clone().show("horizontal");
		for(int i = 0; i < vol.getSize()[2]; i++)
			obj.keepFOV(vol.getSubGrid(i));
		vol.getGridOperator().divideBy(vol, 75f);
		vol.getGridOperator().removeNegative(vol);
		obj.applyWeight(vol);
		vol.clone().show("after FOV");
		imp0 = ImageUtil.wrapGrid3D(vol, null);
		IJ.saveAs(imp0, "Tiff", path4);
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
				if(dd > rr - 900)
					phan.setAtIndex(i, j, 0);
			}
	}
	
	private Grid3D downSamplingZ(Grid3D vol)
	{
		Grid3D vol2 = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2]/2);
		for(int i = 0; i < vol2.getSize()[2]; i++)
			vol2.setSubGrid(i, (Grid2D)vol.getSubGrid(i * 2).clone());
		
		return vol2;
	}
	
	private void applyWeight(Grid3D vol)
	{
		float w;
		int xl = 40; 
		int xr = 180; 
		for(int i = 0; i < vol.getSize()[0]; i ++)
		{
			if(i < xl)
				w = 0.8f;
			else if(i > xr)
				w = 1.0f;
			else
				w = 0.8f + (i - xl)/(xr - xl) * 0.2f;
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++)
				{
					vol.setAtIndex(i, j, k, vol.getAtIndex(i, j, k) * w);
				}
		}
	}
}
