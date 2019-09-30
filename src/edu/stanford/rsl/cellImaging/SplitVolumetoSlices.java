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


public class SplitVolumetoSlices {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\CellImaging\\\\FOVRecon120\\AlgaePhantoms\\";
		String path2 = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon120\\AlgaePhantomSlices\\";
		
		ImagePlus imp1;
		String name1, saveName1;
		Grid3D data;
		Grid2D data2D;
		int  getIndex;
		int gidx = 5000;
		int shift = 0;
		for(int idx = 30; idx<= 39; idx ++){
			name1 = path + idx + ".tif";

			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
		
			for(int i = 0; i <= 35; i++) {
				System.out.print( idx + "_" + i + " ");
				
				getIndex = i * 10 + 75 + shift;
				
				data2D = data.getSubGrid(getIndex);
	
				saveName1 = path2 + gidx + ".tif";

			    imp1 = ImageUtil.wrapGrid(data2D, null);
			    IJ.saveAs(imp1, "Tiff", saveName1);
			    gidx ++;
			}
			shift ++;
		}	
	}
		
		
}
