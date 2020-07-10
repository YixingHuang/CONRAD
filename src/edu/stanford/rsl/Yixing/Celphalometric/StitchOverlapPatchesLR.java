package edu.stanford.rsl.Yixing.Celphalometric;

import java.io.IOException;

import edu.stanford.rsl.Yixing.Celphalometric.superResolution.GenerateTestPatches;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class StitchOverlapPatchesLR {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		int tag = 0;
		GenerateTestPatches obj = new GenerateTestPatches();
		String path;
		String savePath;
		if(tag == 0) {
			path = "D:\\imageSuperResolutionV2_1\\testResults\\RDN5_celp\\";
			savePath = "D:\\Tasks\\FAU4\\Cephalometric\\generatedCelps\\celpRDNMask";
		}
		else
		{
			path = "D:\\imageSuperResolutionV2_1\\testResults\\RRDN5_celp\\";
			savePath = "D:\\Tasks\\FAU4\\Cephalometric\\generatedCelps\\celpRRDN";
		}
		String saveName;
		ImagePlus imp;
		Grid2D patch;
		String imgNameIn;
		int startX, startY;
		int saveId = 1;
		int sz = 320;
		
		String maskPath = "D:\\Tasks\\FAU4\\Cephalometric\\generatedCelps\\smask0.png";
		imp = IJ.openImage(maskPath);
		Grid2D mask = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		for(int i = 0; i < mask.getWidth(); i++)
			for(int j = 0; j < mask.getHeight(); j++)
				if(mask.getAtIndex(i, j) < 125)
					mask.setAtIndex(i, j, 0);
				else
					mask.setAtIndex(i, j, 1);
		mask.show("smask");

		Grid2D us = new Grid2D(2560, 2560);
		for(int idx = 0; idx <= 0; idx ++) {
            for(int i = 0; i <= 16; i++) {
            	for(int j = 0; j <= 16; j++ ) {
            		startX = i * 128 * 5;
            		startY = j * 128 * 5;
            		saveId = idx * 10000 + j * 100 + i;
            		System.out.println("i = " + i + ", j =" + j);
            		imgNameIn = path + saveId + ".png";
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
            us.clone().show("us");
            us.getGridOperator().multiplyBy(us, mask);

            saveName = savePath + idx + ".png";
    		imp = ImageUtil.wrapGrid(us, null);
    		imp.setDisplayRange(0, 255);
    		IJ.saveAs(imp, "png", saveName);
            System.out.print(idx + " ");
		}
	}

}
