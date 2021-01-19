package edu.stanford.rsl.Yixing.truncationPix;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.io.FileInfo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import ij.io.FileOpener;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class ConvertTestResults {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		ConvertTestResults obj = new ConvertTestResults();
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\recon\\";
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\Pix2pixRecons\\";
		String testPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\Pix2pixResults\\";
		String testFolder;
		ImagePlus imp1, imp2;
		String name1, name2, saveName1, testName;
		Grid3D data, mask;
		Grid2D data2D, mask2D, merge2D;
		String saveName;
		int saveIndex, getIndex;
		File outPutDir;
		Grid3D test, result;
		Grid2D test2D;
		test = new Grid3D(256, 256, 256);
		for(int idx = 1; idx<=18; idx ++){
			if(idx == 4) continue;
			name1 = path + "reconTruncated" + idx + ".tif";
			//name2 = path + "reconGT" + idx + ".tif";
			name2 = path + "artifacts" + idx + ".tif";
			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
		
			imp2=IJ.openImage(name2);
			mask = ImageUtil.wrapImagePlus(imp2);
		
			for(int i=0; i < data.getSize()[2]; i++) {
				System.out.println( idx + ", " + i);
				
				getIndex = i;
		
				saveIndex = idx*1000+getIndex;
				testName = saveIndex + "-outputs.raw";
				testFolder = testPath + "testResult" + idx + "\\images";
				test2D = obj.getRawImage(testFolder, testName);
//				test2D.show("test2D");
				test.setSubGrid(i, test2D);
			}
			
			test.clone().show("test");
			data.getGridOperator().subtractBy(data, test);
			saveName = savePath + "pix2pix" + idx + ".tif";
			imp1 = ImageUtil.wrapGrid(data, null);
        	IJ.saveAs(imp1, "Tiff", saveName);
		}
			
	}
	
	Grid2D mergeImages(Grid2D data2D, Grid2D mask2D) {
		Grid2D merge = new Grid2D(512, 256);
		for(int i = 0; i < data2D.getSize()[0]; i++)
			for(int j = 0; j < data2D.getSize()[1]; j++)
			{
				merge.setAtIndex(i, j, data2D.getAtIndex(i, j));
				merge.setAtIndex(i + data2D.getSize()[0], j, mask2D.getAtIndex(i, j));
			}
		
		return merge;
	}
	
	
	private Grid2D getRawImage(String rawPath, String rawName){
		
		FileInfo fi=new FileInfo();
		fi.width=256;
		fi.height=256;
		fi.nImages=1;
		fi.offset=0;
		fi.intelByteOrder=true;
		fi.fileFormat=FileInfo.RAW;
		fi.fileType=FileInfo.GRAY32_FLOAT;
		fi.directory=rawPath;
		fi.fileName=rawName;
		FileOpener fopen=new FileOpener(fi);
		ImagePlus imp=fopen.open(false);
		Grid2D img2D = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		
		return img2D;
		
	}
	
	/**
	 * Full Body RMSE
	 * @param recon
	 * @param recon_data
	 * @return
	 */
	private static double RMSE_FullBody(Grid2D recon, Grid2D recon_data) {
		double err = 0;
		Grid2D temp = new Grid2D(recon);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		double sum = 0;
		int count = 0;

		for(int i = 0; i < recon.getSize()[0]; i ++)
		{

			for(int j = 0; j < recon.getSize()[1]; j++)
				if(j < 190)
				{
					count ++;
					sum = sum + temp.getAtIndex(i, j);
				}
		}
		err = sum /count;
		err = Math.sqrt(err);
		return err * 2040.0;
	}
	
}
