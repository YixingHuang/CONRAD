package edu.stanford.rsl.cellImaging;

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


public class SplitTomoVolumeToSlices {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		SplitTomoVolumeToSlices obj = new SplitTomoVolumeToSlices();
		Perform2DWeightedTV tv2D=new Perform2DWeightedTV();
		Grid2D temp = new Grid2D(512, 512);
		tv2D.imgSizeX = 512;
		tv2D.imgSizeY = 512;
		tv2D.TVGrad = new TVGradient(temp);
		tv2D.TVGrad.weps = 0.05f;
		String path = "D:\\Tasks\\FAU4\\CellImaging\\3DVolumes\\";
		String path2 = "D:\\Tasks\\FAU4\\CellImaging\\TomoSlices\\";
		
		ImagePlus imp1;
		String name1, saveName1;
		Grid3D data;
		Grid2D data2D;
		int  getIndex;
		int gidx = 0;
		int shift = 160;
		for(int idx = 1; idx<=1; idx ++){
			
			name1 = path + "reconTomo512.tif";
			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
		
			for(int i = 0; i < 140; i++) {
				System.out.print( i + " ");
				
				getIndex = i + shift;
				
				data2D = data.getSubGrid(getIndex);
				obj.thresholding(data2D, 0.10f);
				data2D.getGridOperator().divideBy(data2D, 0.5f);
//				tv2D.TVGrad.weightMatrixUpdate(data2D);
//				tv2D.recon = data2D;
//				for(int iter = 0; iter < 5; iter++)
//				{
//					tv2D.weightedTVIterate();
//				}
				saveName1 = path2 + gidx + ".tif";

			    imp1 = ImageUtil.wrapGrid(data2D, null);
			    IJ.saveAs(imp1, "Tiff", saveName1);
			    gidx ++;
			}
			shift ++;
		}	
	}
		
	private void thresholding(Grid2D img, float thres)
	{
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				if(img.getAtIndex(i, j) < thres)
					img.setAtIndex(i, j, 0);
	}
}
