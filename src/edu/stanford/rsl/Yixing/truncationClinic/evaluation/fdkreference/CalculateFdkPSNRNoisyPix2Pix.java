package edu.stanford.rsl.Yixing.truncationClinic.evaluation.fdkreference;

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

public class CalculateFdkPSNRNoisyPix2Pix {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String fdkGTPath = "C:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FDK_GT\\";
//		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\recon\\";
//		String uNetPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\UNetRecons\\";
//		String wTvPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\wTV\\";
//		String DcrPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\DCR\\";
		String pixPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\Pix2pix\\";
//		String directPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FDKParker\\";
		String scalePath = "C:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\FDKScalePix\\";
//		String subPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FDKSubtraction\\";
//		String fistaPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FISTA\\";
//		String fistaProjPath = "C:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FISTA_Projection_Pix\\";
//		String fistaWtvPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FISTA_wTV\\";
		String fistaProjWtvPath = "C:\\Tasks\\FAU4\\TruncationCorrection\\Noisy3D\\FISTA_Projection_wTV_Pix\\";

		ImagePlus imp1;
		String nameGT, nameWCE, nameFBP, nameUNet, nameWTV, nameDCR, namePix, nameDirect, nameScale, nameSub, nameFista, nameFistaProj, nameFistaWtv, nameFistaProjWtv;
		Grid3D gt, fbp, wce, wtv, unet, dcr, pix, direct, scale, sub, fista, fistaProj, fistaWtv, fistaProjWtv;
		Grid2D gt2d, fbp2d, wce2d, wtv2d, unet2d, dcr2d, pix2d, direct2d, scale2d, sub2d, fista2d, fistaProj2d, fistaWtv2d, fistaProjWtv2d;
		double rmseFbp=0, rmseWce, rmseWtv, rmseUnet, rmseDcr, rmsePix, rmseDirect, rmseScale, rmseSub, rmseFista, rmseFistaProj, rmseFistaWtv, rmseFistaProjWtv;
		File outPutDir0, outPutDir1;
		BufferedWriter bw0, bw1; 
		double sumFbp, sumWce, sumWtv, sumUnet, sumDcr, sumPix, sumDirect, sumScale, sumSub, sumFista, sumFistaProj, sumFistaWtv, sumFistaProjWtv;
		double meanFbp=0, meanWce=0, meanWtv = 0, meanUnet=0, meanDcr=0, meanPix=0, meanDirect=0, meanScale=0, meanSub=0, meanFista=0, meanFistaProj=0, meanFistaWtv=0, meanFistaProjWtv=0;
		for(int idx = 1; idx <= 18; idx ++){
			if(idx == 4 )
				continue;
			sumFbp = 0;
			sumWce = 0;
			sumWtv = 0;
			sumUnet = 0;
			sumDcr = 0;
			sumPix = 0;
			sumDirect = 0;
			sumScale = 0;
			sumSub = 0;
			sumFista=0;
			sumFistaProj=0;
			sumFistaWtv=0;
			sumFistaProjWtv=0;
			
			nameGT = fdkGTPath + "FDK" + idx + ".tif";
//			nameWCE = path + "reconLimited" + idx + ".tif";
//			nameFBP = path + "reconFbp" + idx + ".tif";
//			nameWTV = wTvPath + idx + "\\20_FinalReconCL.tif";
//			nameUNet = uNetPath + "UNetP" + idx + ".tif";
//			nameDCR = DcrPath + idx + "\\10_FinalReconCL.tif";
			namePix = pixPath + "pix2pix" + idx + ".tif";
//			nameDirect = directPath + "FDK" + idx + ".tif";
			nameScale = scalePath + "FDK" + idx + ".tif";
//			nameSub = subPath + "FDK" + idx + ".tif";
//			nameFista = fistaPath + idx + "\\10_FinalReconCL.tif";
//			nameFistaProj = fistaProjPath + idx + "\\10_FinalReconCL.tif";
//			nameFista = fistaPath + idx + "\\dot5.tif";
//			nameFistaProj = fistaProjPath + idx + "\\dot5.tif";
//			nameFistaWtv = fistaWtvPath + idx + "\\10_FinalReconCL.tif";
			nameFistaProjWtv = fistaProjWtvPath + idx + "\\10_FinalReconCL.tif";
			
			imp1=IJ.openImage(nameGT);
			gt = ImageUtil.wrapImagePlus(imp1);
		
//			imp1=IJ.openImage(nameWCE);
//			wce = ImageUtil.wrapImagePlus(imp1);
//			
////			imp1=IJ.openImage(nameFBP);
////			fbp = ImageUtil.wrapImagePlus(imp1);
//			
//			imp1=IJ.openImage(nameWTV);
//			wtv = ImageUtil.wrapImagePlus(imp1);
//			
//			
//			imp1=IJ.openImage(nameUNet);
//			unet = ImageUtil.wrapImagePlus(imp1);
//			
//			imp1=IJ.openImage(nameDCR);
//			dcr = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(namePix);
			pix = ImageUtil.wrapImagePlus(imp1);
			
//			imp1=IJ.openImage(nameDirect);
//			direct = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameScale);
			scale = ImageUtil.wrapImagePlus(imp1);
			
//			imp1=IJ.openImage(nameSub);
//			sub = ImageUtil.wrapImagePlus(imp1);
//			
//			imp1=IJ.openImage(nameFista);
//			fista = ImageUtil.wrapImagePlus(imp1);
//			
//			imp1=IJ.openImage(nameFistaProj);
//			fistaProj = ImageUtil.wrapImagePlus(imp1);
//			
//			imp1=IJ.openImage(nameFistaWtv);
//			fistaWtv = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameFistaProjWtv);
			fistaProjWtv = ImageUtil.wrapImagePlus(imp1);
//			outPutDir0 = new File(path3+"RMSE_FBP_ROI.txt");
//			outPutDir1 = new File(path3+"UNet_ROI.txt");
//			if(!outPutDir0.exists()){
//				outPutDir0.getParentFile().mkdirs();
//				outPutDir0.createNewFile();
//			}
//			
//			if(!outPutDir1.exists()){
//				outPutDir1.getParentFile().mkdirs();
//				outPutDir1.createNewFile();
//			}
//
//			
//			bw0 = new BufferedWriter(new FileWriter(outPutDir0));
//			bw1 = new BufferedWriter(new FileWriter(outPutDir1));
			for(int i = 20; i < gt.getSize()[2] - 20; i++) {
				gt2d = (Grid2D) gt.getSubGrid(i).clone();
//				fbp2d = (Grid2D) fbp.getSubGrid(i).clone();
//				wce2d = (Grid2D) wce.getSubGrid(i).clone();
//				wtv2d = (Grid2D) wtv.getSubGrid(i).clone();
//				unet2d = (Grid2D) unet.getSubGrid(i).clone();
//				dcr2d = (Grid2D) dcr.getSubGrid(i).clone();
				pix2d = (Grid2D) pix.getSubGrid(i).clone();
//				direct2d = (Grid2D) direct.getSubGrid(i).clone();
				scale2d = (Grid2D) scale.getSubGrid(i).clone();
//				sub2d = (Grid2D) sub.getSubGrid(i).clone();
//				fista2d = (Grid2D) fista.getSubGrid(i).clone();
//				fistaProj2d = (Grid2D) fistaProj.getSubGrid(i).clone();
//				fistaWtv2d = (Grid2D) fistaWtv.getSubGrid(i).clone();
				fistaProjWtv2d = (Grid2D) fistaProjWtv.getSubGrid(i).clone();
				
//				rmseFbp = RMSE_FullBody(fbp2d, gt2d);
//				rmseWce = RMSE_FullBody(wce2d, gt2d);
//				rmseWtv = RMSE_FullBody(wtv2d, gt2d);
//				rmseUnet = RMSE_FullBody(unet2d, gt2d);
//				rmseDcr = RMSE_FullBody(dcr2d, gt2d);
				rmsePix = PSNR_FullBody(pix2d, gt2d);
//				rmseDirect = RMSE_FullBody(direct2d, gt2d);
				rmseScale = PSNR_FullBody(scale2d, gt2d);
//				rmseSub = RMSE_FullBody(sub2d, gt2d);
//				rmseFista = RMSE_FullBody(fista2d, gt2d);
//				rmseFistaProj = RMSE_FullBody(fistaProj2d, gt2d);
//				rmseFistaWtv = RMSE_FullBody(fistaWtv2d, gt2d);
				rmseFistaProjWtv = PSNR_FullBody(fistaProjWtv2d, gt2d);
				
//				sumFbp += rmseFbp;
//				sumWce += rmseWce;
//				sumWtv += rmseWtv;
//				sumUnet += rmseUnet;
//				sumDcr += rmseDcr;
				sumPix += rmsePix;
//				sumDirect += rmseDirect;
				sumScale += rmseScale;
//				sumSub += rmseSub;
//				sumFista += rmseFista;
//				sumFistaProj += rmseFistaProj;
//				sumFistaWtv += rmseFistaWtv;
				sumFistaProjWtv += rmseFistaProjWtv;
//				bw0.write(rmse0 + "\r\n");
//				bw0.flush();	
//		
//				bw1.write(rmse1 + "\r\n");
//				bw1.flush();
				if(idx == 3 && i == 174)
					System.out.println(idx + " " + i + ":" + rmsePix +  " " + rmseScale + " "  + rmseFistaProjWtv );
				
				if(idx == 18 && i == 149)
					System.out.println(idx + " " + i + ":" + rmsePix +  " " + rmseScale + " " + rmseFistaProjWtv );
				
				if(idx == 2 && i == 186)
					System.out.println(idx + " " + i + ":"  + rmsePix + " " + rmseScale +  " " + rmseFistaProjWtv );
				
				if(idx == 9 && i == 122)
					System.out.println(idx + " " + i + ":"  + rmsePix +  " " + rmseScale + " "  + rmseFistaProjWtv );
				
			}
//			bw1.close();
//			bw0.close();

			
			sumFbp = sumFbp/(gt.getSize()[2] - 40);
			sumWce = sumWce/(gt.getSize()[2] - 40);
			sumWtv = sumWtv/(gt.getSize()[2] - 40);
			sumUnet = sumUnet/(gt.getSize()[2] - 40);
			sumDcr = sumDcr/(gt.getSize()[2] - 40);
			sumPix = sumPix/(gt.getSize()[2] - 40);
			sumDirect = sumDirect/(gt.getSize()[2] - 40);
			sumScale = sumScale/(gt.getSize()[2] - 40);
			sumSub = sumSub/(gt.getSize()[2] - 40);
			sumFista = sumFista/(gt.getSize()[2] - 40);
			sumFistaProj = sumFistaProj/(gt.getSize()[2] - 40);
			sumFistaWtv = sumFistaWtv/(gt.getSize()[2] - 40);
			sumFistaProjWtv = sumFistaProjWtv/(gt.getSize()[2] - 40);
			System.out.println( sumPix + " " + sumScale + " " + sumFistaProjWtv );
			
			meanFbp += sumFbp;
			meanWce += sumWce;
			meanWtv += sumWtv;
			meanUnet += sumUnet;
			meanDcr += sumDcr;
			meanPix += sumPix;
			meanDirect += sumDirect;
			meanScale += sumScale;
			meanSub += sumSub;
			meanFista += sumFista;
			meanFistaProj += sumFistaProj;
			meanFistaWtv += sumFistaWtv;
			meanFistaProjWtv += sumFistaProjWtv;
		}
		
		System.out.println("average: "  + meanPix/17.0 + " " + meanScale/17.0  + " " + meanFistaProjWtv/17.0);
	}
	
//	private static double RMSE(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		err = temp.getGridOperator().sum(temp);
//		err = err / (temp.getSize()[0] * temp.getSize()[1]);
//		err = Math.sqrt(err);
//		return err;
//	}
//	
	/**
	 * ROI RMSE
	 * @param recon
	 * @param recon_data
	 * @return
	 */
//	private static double RMSE(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		double sum = 0;
//		int count = 0;
//		float x, y;
//		float thres = 97;
//		float thres2 = thres * thres;
//		for(int i = 0; i < recon.getSize()[0]; i ++)
//		{
//			x = i - (recon.getSize()[0] - 1)/2.0f;
//			for(int j = 0; j < recon.getSize()[1]; j++)
//			{
//				y = j - (recon.getSize()[1] - 1)/2.0f;
//				if(x * x + y * y < thres2)
//				{
//					count ++;
//					sum = sum + temp.getAtIndex(i, j);
//				}
//			}
//		}
//		err = sum /count;
//		err = Math.sqrt(err);
//		return err * 2040.0;
//	}
//	

	
	private static double PSNR_FullBody(Grid2D recon_data, Grid2D ref) {
		double err = 0;
		double maxV = 0;
		Grid2D temp = new Grid2D(ref);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		double sum = 0;
		int count = 0;

		for(int i = 0; i < ref.getSize()[0]; i ++)
		{

			for(int j = 0; j < ref.getSize()[1]; j++)
				if(j < 190)
				{
					count ++;
					if(ref.getAtIndex(i, j) > maxV)
						maxV = ref.getAtIndex(i, j);
					sum = sum + temp.getAtIndex(i, j);
				}
		}
		err = sum /count;
		err = Math.sqrt(err);
		double psnr = 20 * Math.log10(maxV/err);
		return psnr;
	}
}
