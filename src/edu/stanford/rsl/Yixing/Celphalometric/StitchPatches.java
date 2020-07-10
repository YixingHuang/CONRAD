package edu.stanford.rsl.Yixing.Celphalometric;

import java.io.IOException;

import edu.stanford.rsl.Yixing.Celphalometric.superResolution.GenerateTestPatches;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class StitchPatches {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateTestPatches obj = new GenerateTestPatches();
		String path = "D:\\Pix2pix\\superResolution_test2\\images\\";
		String savePath = "D:\\Tasks\\FAU4\\Cephalometric\\generatedCelps\\";
		String saveName;
		ImagePlus imp;
		Grid2D patch;
		String imgNameIn;
		int startX, startY;
		int saveId = 1;
		int sz = 256;
		

		Grid2D us = new Grid2D(2560, 2560);
		for(int idx = 0; idx <= 0; idx ++) {
            for(int i = 0; i < 10; i++) {
            	for(int j = 0; j < 10; j++ ) {
            		startX = i * 254;
            		startY = j * 254;
            		saveId = idx * 100 + j * 10 + i;
            		imgNameIn = path + saveId + "-outputs.png";
            		imp = IJ.openImage(imgNameIn);
        			patch = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
//        			patch.clone().show("patch" + i + " " + j);
            		for(int x = 0; x < sz; x++) {
            			for(int y = 0; y < sz; y++)
            			{
            				us.setAtIndex(startX + x, startY + y, patch.getAtIndex(x, y));
            			}
            		}
            	}
            }
            us.show("us");
    		saveName = savePath + "SR" + idx + ".png";
    		imp = ImageUtil.wrapGrid(us, null);
    		IJ.saveAs(imp, "png", saveName);
            System.out.print(idx + " ");
		}
	}

}
