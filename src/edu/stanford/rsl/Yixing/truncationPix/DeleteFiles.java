package edu.stanford.rsl.Yixing.truncationPix;

import java.io.IOException;

import java.io.File;

import ij.ImageJ;

public class DeleteFiles {
	public static void main(String[] args) throws IOException{
		File outPutDir;
		int last = 10;
		String pathFolder = "C:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\FISTA_Projection_wTV_Pix\\";
		String fileName;
		for(int patient = 1; patient <= 18; patient++) {
			if(patient == 4)
				continue;
			for(int i = 0; i < last; i = i+5)
			{
				fileName = pathFolder + patient + "\\" + i + "thResult.tif";
				outPutDir = new File(fileName);
				if(outPutDir.exists()){
				    outPutDir.delete();
				}
			}
		}
		System.out.println("finished!");
	}
}
