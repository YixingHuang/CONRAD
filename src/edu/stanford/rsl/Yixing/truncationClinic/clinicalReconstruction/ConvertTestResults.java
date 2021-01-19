package edu.stanford.rsl.Yixing.truncationClinic.clinicalReconstruction;

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
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicRecons\\";
		String path2 = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\test\\";
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicRecons\\";
		String testPath = "C:\\Tasks\\FAU4\\TruncationCorrection\\Pix2pix\\Clinical\\";
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
		test = new Grid3D(256, 256, 300);
		Grid3D data2 = new Grid3D(256, 256, 300);
			
		name1 = path + "WCE2.tif";
		imp1=IJ.openImage(name1);
		data = ImageUtil.wrapImagePlus(imp1);
	
		for(int i=0; i < data.getSize()[2]; i++) {		
			getIndex = i;
	
			saveIndex = 200000+getIndex;
			testName = saveIndex + "-outputs.raw";
			testFolder = testPath + "testResult4\\images";
			test2D = obj.getRawImage(testFolder, testName);
//			test2D.show("test2D");
			test.setSubGrid(i, test2D);
			data2.setSubGrid(i, obj.subsampling(data.getSubGrid(i), 2));
		}
		
		test.clone().show("test");
		data2.getGridOperator().subtractBy(data2, test);
		saveName = savePath + "pix2pix4.tif";
		imp1 = ImageUtil.wrapGrid(data2, null);
    	IJ.saveAs(imp1, "Tiff", saveName);
		
			
	}
	
	public Grid2D subsampling(Grid2D img, int factor)
	{
		float val = 0;
		int count = 0;
		int nx, ny;
		int width = (img.getWidth() - 1)/factor + 1;
		int height = (img.getHeight() - 1)/factor + 1;
		Grid2D img2 = new Grid2D(width, height);
		img2.setSpacing(img.getSpacing()[0] * factor, img.getSpacing()[1] * factor);
		img2.setOrigin(-(img2.getSize()[0] - 1.0) * img2.getSpacing()[0]/2.0, -(img2.getSize()[1] - 1.0) * img2.getSpacing()[1]/2.0);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				val = 0; count = 0;
				for(int m = 0; m < factor; m++)
					for(int n = 0; n < factor; n++)
					{
						nx = i * factor + m;
						ny = j * factor + n;
						if(nx < img.getWidth() && ny < img.getHeight())
						{
							val = val + img.getAtIndex(nx, ny);
							count++;
						}
					}
				img2.setAtIndex(i, j, val/count);
			}
		
		return img2;
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
