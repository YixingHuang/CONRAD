package edu.stanford.rsl.Yixing.Celphalometric;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class StitchOverlapPatches {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateTestPatches obj = new GenerateTestPatches();
		String path = "D:\\Pix2pix\\superResolution_testCep2\\images\\";
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
            for(int i = 0; i <= 18; i++) {
            	for(int j = 0; j <= 18; j++ ) {
            		startX = i * 128;
            		startY = j * 128;
            		saveId = idx * 10000 + j * 100 + i;
            		System.out.println("i = " + i + ", j =" + j);
            		imgNameIn = path + saveId + "-outputs.png";
            		imp = IJ.openImage(imgNameIn);
        			patch = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
        			
        			//dealing with boundaries
        			if(j == 0) 
        			{
        				int sx = (i == 0) ? 0 : sz/4;
        				int ex = (i == 18) ? sz : sz * 3/4;
        				for(int x = sx; x < ex; x++) 
        					for(int y = 0; y < sz/4; y++)
        						us.setAtIndex(startX + x, startY + y, patch.getAtIndex(x, y));
        			}
        			if(i == 0)
        			{
        				int sy = (j == 0) ? 0 : sz/4;
        				int ey = (j == 18) ? sz : sz * 3/4;
        				for(int x = 0; x < sz/4; x++)
        					for(int y = sy; y < ey; y++)
        						us.setAtIndex(startX + x, startY + y, patch.getAtIndex(x, y));
        			}
        			if(j == 18) 
        			{
        				int sx = (i == 0) ? 0 : sz/4;
        				int ex = (i == 18) ? sz : sz * 3/4;
        				for(int x = sx; x < ex; x++) 
        					for(int y = sz * 3/4; y < sz; y++)
        						us.setAtIndex(startX + x, startY + y, patch.getAtIndex(x, y));
        			}
        			if(i == 18)
        			{
        				int sy = (j == 0) ? 0 : sz/4;
        				int ey = (j == 18) ? sz : sz * 3/4;
        				for(int x = sz *3/4; x < sz; x++)
        					for(int y = sy; y < ey; y++)
        						us.setAtIndex(startX + x, startY + y, patch.getAtIndex(x, y));
        			}
        			
        			//dealing with centeral areas
        			for(int x = sz/4; x < sz *3/4; x++) {
            			for(int y = sz/4; y < sz*3/4; y++)
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
