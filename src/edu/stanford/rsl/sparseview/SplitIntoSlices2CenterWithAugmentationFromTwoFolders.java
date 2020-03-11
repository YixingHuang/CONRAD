package edu.stanford.rsl.sparseview;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class SplitIntoSlices2CenterWithAugmentationFromTwoFolders {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\SparseViewCT\\Noisy3D\\90Degree\\recon\\";
		String path0 = "D:\\Tasks\\FAU4\\SparseViewCT\\Noisy3D\\360DegreeRecon\\";
		String path2 = "D:\\Tasks\\FAU4\\SparseViewCT\\Noisy3D\\90Degree\\trainingData_d10\\";
		String path3;
		ImagePlus imp1, imp2;
		String name1, name2, saveName1, saveName2;
		Grid3D data, mask;
		Grid2D data2D, mask2D, data2D2, mask2D2;
		
		SplitIntoSlices2CenterWithAugmentationFromTwoFolders obj = new SplitIntoSlices2CenterWithAugmentationFromTwoFolders();
		int saveIndex, getIndex;
		for(int idx = 1; idx<= 18; idx ++){
			name1 = path + "reconTruncated" + idx + ".tif";
			name2 = path0 + "reconTruncated" + idx + ".tif";//GTs
			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
		    
			imp2=IJ.openImage(name2);
			mask = ImageUtil.wrapImagePlus(imp2);
			
			mask.getGridOperator().multiplyBy(mask, -1);
			mask.getGridOperator().addBy(mask, data);
			
			path3 = path2;
			for(int i = 0; i < 26; i++){
				System.out.println( idx + ", " + i);
				
				//getIndex = i*20+10;
				getIndex = i * 6 + 50;
				
				
				data2D = data.getSubGrid(getIndex);
				mask2D = mask.getSubGrid(getIndex);
				for(int k = 0; k < 1; k++)
				{
					saveIndex = idx * 10000 + getIndex * 10 + k;
					saveName1 = path3 + "data" + saveIndex + ".tif";
					saveName2 = path3 + "data" + saveIndex + "_mask.tif";
					data2D2 = obj.augmentImage(data2D, k);
					mask2D2 = obj.augmentImage(mask2D, k);
				    imp1 = ImageUtil.wrapGrid(data2D2, null);
				    IJ.saveAs(imp1, "Tiff", saveName1);
				    imp2 = ImageUtil.wrapGrid(mask2D2, null);
				    IJ.saveAs(imp2, "Tiff", saveName2);
				}

			

			}
		
		}
		
		
	}
	
	
	/**
	 * rotate or flip an image
	 * 
	 * @param img
	 * @param tag   0： nothing
	 * 				1 - 3: rotate tag * 90 degrees
	 *              4: flip horizontally
	 *              5: flip vertically
	 * @return
	 */
	private Grid2D augmentImage(Grid2D img, int tag)
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
			
		case 4: 
			for(int i = 0; i < img.getSize()[0]; i++)
				for(int j = 0; j < img.getSize()[1]; j++)
					imgRot.setAtIndex(img.getSize()[0] -  i - 1, j, img.getAtIndex(i, j));
			break;
			
		case 5: 
			for(int i = 0; i < img.getSize()[0]; i++)
				for(int j = 0; j < img.getSize()[1]; j++)
					imgRot.setAtIndex(i, img.getSize()[1] - j - 1, img.getAtIndex(i, j));
			break;
			
		default:
			break;
		}
		return imgRot;
	}
	
}
