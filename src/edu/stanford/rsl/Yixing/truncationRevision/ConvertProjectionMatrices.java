package edu.stanford.rsl.Yixing.truncationRevision;

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
import edu.stanford.rsl.conrad.geometry.Projection;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.numerics.SimpleMatrix;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class ConvertProjectionMatrices {
	protected Trajectory geo = null;
	public static void main(String[] args) throws IOException{
		new ImageJ();
		ConvertProjectionMatrices obj = new ConvertProjectionMatrices();
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		obj.geo = conf.getGeometry();
		int trans = 190;
		int vtrans = 100;
		obj.getSparseProjectionMatrix(trans, vtrans);

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
	
	/**
	 * 
	 * @param trans: positive, extend the detector size horizontally 
	 * @param vtrans: positive, extend the detector size vertically
	 * @throws IOException
	 */
	private void getSparseProjectionMatrix(int trans, int vtrans) throws IOException{
		 String Rawpath="D:\\Tasks\\FAU1\\Research_LimitedAngleReconstruction\\TVresults\\MagdeburgBrain";
		 String Rawname="ProjMatrix.bin";
		
		 boolean isSave = true;
		String outName = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\sparseProjMatrxH" + trans + "V" + vtrans + ".txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(outName));
			
			
		FileInfo fi = new FileInfo();
		fi.width = 3;
		fi.height = 4;
		fi.nImages = 496;
		fi.offset = 6;
		fi.intelByteOrder = true;
		fi.fileFormat = FileInfo.RAW;
		fi.fileType = FileInfo.GRAY64_FLOAT;
		fi.directory = Rawpath;
		fi.fileName = Rawname;
		FileOpener fopen = new FileOpener(fi);
		ImagePlus ProjMatrixImp = fopen.open(true);
		Grid3D ProjGrid3D = ImageUtil.wrapImagePlus(ProjMatrixImp);
		Projection[] projMats = new Projection[ProjGrid3D.getSize()[2]];
     
		SimpleMatrix tempMat = new SimpleMatrix(ProjGrid3D.getSize()[0], ProjGrid3D.getSize()[1]);
		System.out.println(ProjGrid3D.getSize()[0] + " " + ProjGrid3D.getSize()[1]);
		
		if(isSave)
		{
			String header1, header2, header3;
			header1 = "CERA::io::WriterAxProjectionTable version 3";
			header2 = "Fri Mar 27 10:2653 2020";
			header3 = "# format: angle / entries of projection matrices";
			bw.write(header1 + "\r");
			bw.write(header2 + "\r\n");
			bw.write("\r\n");
			int n = 248;
			bw.write(header3 + "\r" + n + "\r\n");
			bw.write("\r\n");
		}
		
		for(int i = 0; i  <ProjGrid3D.getSize()[2]; i = i + 2){
			if(isSave)
			{
				int ii = i/2 + 1;
				float a = 0;
				bw.write("@" + ii+"\r\n");
				bw.write(a + " " + a +"\r\n");
			}
			for(int k = 0; k < ProjGrid3D.getSize()[0]; k++)
			{
				for(int m = 0; m < ProjGrid3D.getSize()[1]; m++) {
					if(k == 0)
						tempMat.setElementValue(k, m, ProjGrid3D.getSubGrid(i).getAtIndex(k, m)/2.0 + ProjGrid3D.getSubGrid(i).getAtIndex(2, m) * trans);
					else if(k == 1)
						tempMat.setElementValue(k, m, ProjGrid3D.getSubGrid(i).getAtIndex(k, m)/2.0 + ProjGrid3D.getSubGrid(i).getAtIndex(2, m) * vtrans);
					else
						tempMat.setElementValue(k, m, ProjGrid3D.getSubGrid(i).getAtIndex(k, m));

			        if(isSave)
			        {
			        		bw.write(String.format("%6.10f", tempMat.getElement(k, m)) + " ");
			        }
				}
				if(isSave)
					bw.write("\r\n");
			}
			if(isSave)
				bw.write("\r\n");
		}
		if(isSave)
		{
			bw.flush();
			bw.close();
		}
		geo.setNumProjectionMatrices(ProjGrid3D.getSize()[2]);
		
		geo.setProjectionMatrices(projMats);
		System.out.println("SAD = " + geo.getSourceToAxisDistance());
		System.out.println("SDD = " + geo.getSourceToDetectorDistance());		
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
