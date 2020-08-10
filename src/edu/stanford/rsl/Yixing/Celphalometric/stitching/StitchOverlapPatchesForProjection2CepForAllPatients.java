package edu.stanford.rsl.Yixing.Celphalometric.stitching;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * Do not need flip
 * @author Yixing Huang
 *
 */
public class StitchOverlapPatchesForProjection2CepForAllPatients {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		boolean isPix = false;
		String path = "D:\\Pix2pix\\cephalogramResultsC40\\";
		String[] folders = new String[] {"RGBT1Results", "RGBT2Results", "RGBL3Results", "RGBL4Results"};
		String savePath = path;
		String saveName;
		ImagePlus imp;
		Grid2D patch;
		String imgNameIn;
		int startX, startY;
		int saveId = 1;
		int szIn = 256;
		
//      startX = 10;
//      startY = 10;
		//part 2
//      startX = 245;
//      startY = 10;
//      
//      startX = 10;
//      startY = 245;
//      ycut = 27;
//      
//      startX = 245;
//      startY = 245;
//      ycut = 27;
		Grid3D patches = new Grid3D(szIn, szIn, 4);
		int idx2;
		for(int idx = 0; idx <= 4; idx++)
		{
			idx2 = idx + 1;
			for(int i = 1; i <= 4; i++)
			{
				imgNameIn = path + folders[i-1] + "\\images\\" + idx2 + "-outputs.png";
				imp = IJ.openImage(imgNameIn);
				patch = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
				patches.setSubGrid(i-1, (Grid2D)patch.clone());
			}
			
			Grid2D us = new Grid2D(512, 512);
			startX = 10;
			startY = 10;
			for(int x = 0; x < szIn; x++)
	            for(int y = 0; y < szIn; y++){
	            	us.setAtIndex(x + startX, y + startY, patches.getAtIndex(x, y, 0));
	            }
			
			startX = 245;
			startY = 10;
			for(int x = 10; x < szIn; x++)
	            for(int y = 0; y < szIn; y++){
	            	us.setAtIndex(x + startX, y + startY, patches.getAtIndex(x, y, 1));
	            }
			
			startX = 10;
			startY = 245;
			for(int x = 0; x < szIn; x++)
	            for(int y = 5; y < szIn; y++){
	            	us.setAtIndex(x + startX, y + startY, patches.getAtIndex(x, y, 2));
	            }
			
			startX = 245;
			startY = 245;
			for(int x = 10; x < szIn; x++)
	            for(int y = 5; y < szIn; y++){
	            	us.setAtIndex(x + startX, y + startY, patches.getAtIndex(x, y, 3));
	            }
			
			
	            us.clone().show("qq" + idx);
		}
		
            

	}
	//Part 1:
//  startX = 20;
//  startY = 10;
	//part 2
//  startX = 256;
//  startY = 10;
	//part 3
//	ycut = 56;
//	startX = 20;
//	startY = 246;
//	
//	//part 4
//	ycut = 56;
//	startX = 256;
//	startY = 246;

}
