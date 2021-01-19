package edu.stanford.rsl.Yixing.truncationClinic.evaluation.gtreference;

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
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class CalculateSSIMFOVForEachPatient {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\recon\\";
		String uNetPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\UNetRecons\\";
		String wTvPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\wTV\\";
		String DcrPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\DCR\\";
		String pixPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\Pix2pix\\";
		String directPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FDKParker\\";
		String scalePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FDKScale\\";
		String subPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FDKSubtraction\\";
		String fistaPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FISTA\\";
		String fistaProjPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FISTA_Projection\\";
		String fistaWtvPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FISTA_wTV\\";
		String fistaProjWtvPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FISTA_Projection_wTV\\";

		ImagePlus imp1;
		String nameGT, nameWCE, nameFBP, nameUNet, nameWTV, nameDCR, namePix, nameDirect, nameScale, nameSub, nameFista, nameFistaProj, nameFistaWtv, nameFistaProjWtv;
		Grid3D gt, fbp, wce, wtv, unet, dcr, pix, direct, scale, sub, fista, fistaProj, fistaWtv, fistaProjWtv;
		Grid2D gt2d, fbp2d, wce2d, wtv2d, unet2d, dcr2d, pix2d, direct2d, scale2d, sub2d, fista2d, fistaProj2d, fistaWtv2d, fistaProjWtv2d;
		double rmseFbp, rmseWce, rmseWtv, rmseUnet, rmseDcr, rmsePix, rmseDirect, rmseScale, rmseSub, rmseFista, rmseFistaProj, rmseFistaWtv, rmseFistaProjWtv;
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
			
			nameGT = path + "reconGT" + idx + ".tif";
			nameWCE = path + "reconLimited" + idx + ".tif";
			nameFBP = path + "reconFbp" + idx + ".tif";
			nameWTV = wTvPath + idx + "\\20_FinalReconCL.tif";
			nameUNet = uNetPath + "UNetP" + idx + ".tif";
			nameDCR = DcrPath + idx + "\\10_FinalReconCL.tif";
			namePix = pixPath + "pix2pix" + idx + ".tif";
			nameDirect = directPath + "FDK" + idx + ".tif";
			nameScale = scalePath + "FDK" + idx + ".tif";
			nameSub = subPath + "FDK" + idx + ".tif";
			nameFista = fistaPath + idx + "\\10_FinalReconCL.tif";
			nameFistaProj = fistaProjPath + idx + "\\10_FinalReconCL.tif";
			nameFistaWtv = fistaWtvPath + idx + "\\10_FinalReconCL.tif";
			nameFistaProjWtv = fistaProjWtvPath + idx + "\\10_FinalReconCL.tif";
			
			imp1=IJ.openImage(nameGT);
			gt = ImageUtil.wrapImagePlus(imp1);
		
			imp1=IJ.openImage(nameWCE);
			wce = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameFBP);
			fbp = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameWTV);
			wtv = ImageUtil.wrapImagePlus(imp1);
			
			
			imp1=IJ.openImage(nameUNet);
			unet = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameDCR);
			dcr = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(namePix);
			pix = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameDirect);
			direct = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameScale);
			scale = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameSub);
			sub = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameFista);
			fista = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameFistaProj);
			fistaProj = ImageUtil.wrapImagePlus(imp1);
			
			imp1=IJ.openImage(nameFistaWtv);
			fistaWtv = ImageUtil.wrapImagePlus(imp1);
			
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
				fbp2d = (Grid2D) fbp.getSubGrid(i).clone();
				wce2d = (Grid2D) wce.getSubGrid(i).clone();
				wtv2d = (Grid2D) wtv.getSubGrid(i).clone();
				unet2d = (Grid2D) unet.getSubGrid(i).clone();
				dcr2d = (Grid2D) dcr.getSubGrid(i).clone();
				pix2d = (Grid2D) pix.getSubGrid(i).clone();
				direct2d = (Grid2D) direct.getSubGrid(i).clone();
				scale2d = (Grid2D) scale.getSubGrid(i).clone();
				sub2d = (Grid2D) sub.getSubGrid(i).clone();
				fista2d = (Grid2D) fista.getSubGrid(i).clone();
				fistaProj2d = (Grid2D) fistaProj.getSubGrid(i).clone();
				fistaWtv2d = (Grid2D) fistaWtv.getSubGrid(i).clone();
				fistaProjWtv2d = (Grid2D) fistaProjWtv.getSubGrid(i).clone();
				
				rmseFbp = getSSIM_FOV(fbp2d, gt2d);
				rmseWce = getSSIM_FOV(wce2d, gt2d);
				rmseWtv = getSSIM_FOV(wtv2d, gt2d);
				rmseUnet = getSSIM_FOV(unet2d, gt2d);
				rmseDcr = getSSIM_FOV(dcr2d, gt2d);
				rmsePix = getSSIM_FOV(pix2d, gt2d);
				rmseDirect = getSSIM_FOV(direct2d, gt2d);
				rmseScale = getSSIM_FOV(scale2d, gt2d);
				rmseSub = getSSIM_FOV(sub2d, gt2d);
				rmseFista = getSSIM_FOV(fista2d, gt2d);
				rmseFistaProj = getSSIM_FOV(fistaProj2d, gt2d);
				rmseFistaWtv = getSSIM_FOV(fistaWtv2d, gt2d);
				rmseFistaProjWtv = getSSIM_FOV(fistaProjWtv2d, gt2d);
				
				sumFbp += rmseFbp;
				sumWce += rmseWce;
				sumWtv += rmseWtv;
				sumUnet += rmseUnet;
				sumDcr += rmseDcr;
				sumPix += rmsePix;
				sumDirect += rmseDirect;
				sumScale += rmseScale;
				sumSub += rmseSub;
				sumFista += rmseFista;
				sumFistaProj += rmseFistaProj;
				sumFistaWtv += rmseFistaWtv;
				sumFistaProjWtv += rmseFistaProjWtv;
//				bw0.write(rmse0 + "\r\n");
//				bw0.flush();	
//		
//				bw1.write(rmse1 + "\r\n");
//				bw1.flush();
				if(idx == 3 && i == 174)
					System.out.println(idx + " " + i + ":" + rmseFbp + " " + rmseWce + " " + rmseWtv + " " + rmseUnet + " " + rmseDcr + " " + rmsePix + " " + rmseDirect + " " + rmseScale + " " + rmseSub + " " + rmseFista + " " + rmseFistaProj + " " + rmseFistaWtv + " " + rmseFistaProjWtv );
				
				if(idx == 18 && i == 149)
					System.out.println(idx + " " + i + ":" + rmseFbp + " " + rmseWce + " " + rmseWtv + " " + rmseUnet + " " + rmseDcr + " " + rmsePix + " " + rmseDirect + " " + rmseScale + " " + rmseSub + " " + rmseFista + " " + rmseFistaProj + " " + rmseFistaWtv + " " + rmseFistaProjWtv );
				
				if(idx == 2 && i == 186)
					System.out.println(idx + " " + i + ":" + rmseFbp + " " + rmseWce + " " + rmseWtv + " " + rmseUnet + " " + rmseDcr + " " + rmsePix + " " + rmseDirect + " " + rmseScale + " " + rmseSub + " " + rmseFista + " " + rmseFistaProj + " " + rmseFistaWtv + " " + rmseFistaProjWtv );
				
				if(idx == 9 && i == 122)
					System.out.println(idx + " " + i + ":" + rmseFbp + " " + rmseWce + " " + rmseWtv + " " + rmseUnet + " " + rmseDcr + " " + rmsePix + " " + rmseDirect + " " + rmseScale + " " + rmseSub + " " + rmseFista + " " + rmseFistaProj + " " + rmseFistaWtv + " " + rmseFistaProjWtv );
				
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
			System.out.println(sumFbp + " " + sumWce + " " + sumWtv + " " + sumUnet + " " + sumDcr + " " + sumPix + " " + sumDirect + " " + sumScale + " " + sumSub + " " + sumFista + " " + sumFistaProj + " " + sumFistaWtv + " " + sumFistaProjWtv );
			
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
		
		System.out.println("average: " + meanFbp/17.0 + " " + meanWce/17.0 + " " + meanWtv/17.0 + " " + meanUnet/17.0 + " " + meanDcr/17.0 + " " + meanPix/17.0 + " " + meanDirect/17.0 + " " + meanScale/17.0 + " " + meanSub/17.0 + " " + meanFista/17.0 + " " + meanFistaProj/17.0 + " " + meanFistaWtv/17.0 + " " + meanFistaProjWtv/17.0);
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
	
//	
//	/**
//	 * Full Body RMSE
//	 * @param recon
//	 * @param recon_data
//	 * @return
//	 */
//	private static double RMSE_FullBody(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		double sum = 0;
//		int count = 0;
//
//		for(int i = 0; i < recon.getSize()[0]; i ++)
//		{
//
//			for(int j = 0; j < recon.getSize()[1]; j++)
//				if(j < 190)
//				{
//					count ++;
//					sum = sum + temp.getAtIndex(i, j);
//				}
//		}
//		err = sum /count;
//		err = Math.sqrt(err);
//		return err * 2040.0;
//	}
	
	public static double getSSIM_FOV(Grid2D img1, Grid2D img2) {
		img1 = keepFOV(img1);
		img2 = keepFOV(img2);
		ImagePlus Imp1 = ImageUtil.wrapGrid(img1, null);
		ImagePlus Imp2 = ImageUtil.wrapGrid(img2, null);
		double returnSSIMDoubleValue = 0;
		int lengthOfImp1 = Imp1.getWidth() * Imp1.getHeight();
		int lengthOfImp2 = Imp2.getWidth() * Imp2.getHeight();
		if (lengthOfImp1 != lengthOfImp2) {
			System.out.println("[!] Error: ROI sizes are not equal!");
		} else {
			double[] xCoord = new double[lengthOfImp1];
			double[] yCoord = new double[lengthOfImp2];
			int XnonFinitiyCounts = 0;
			int YnonFinitiyCounts = 0;
			for (int i = 0; i < lengthOfImp1; i++) {

				if (Float.isFinite(Imp1.getProcessor().getf(i))) {
					xCoord[i] = Imp1.getProcessor().getf(i);
				} else {// kick out non-Finite values of Imp1
					XnonFinitiyCounts = XnonFinitiyCounts + 1;
					xCoord[i] = 0;
				}

				if (Float.isFinite(Imp2.getProcessor().getf(i))) {
					yCoord[i] = Imp2.getProcessor().getf(i);
				} else {// kick out non-Finite values of Imp2
					YnonFinitiyCounts = YnonFinitiyCounts + 1;
					yCoord[i] = 0;
				}

			}

			// Calculate the Correlation result.
			double SSIMDoubleValue = DoubleArrayUtil.computeSSIMDoubleArrays(xCoord, yCoord);
			returnSSIMDoubleValue = SSIMDoubleValue;

		}
		return returnSSIMDoubleValue;

	}
	private static Grid2D keepFOV(Grid2D phan)
	{
		int thres = 97;
		int thres2 = thres * thres;
		int width = thres * 2 + 1;
		Grid2D phanCrop = new Grid2D(width, width);
		int xcent = phan.getSize()[0]/2;
		for(int i = -thres; i <= thres; i ++)
			for(int j = -thres; j <= thres; j ++)
			{
				if(i * i + j * j <= thres2)
					phanCrop.setAtIndex(i + thres, j + thres, phan.getAtIndex(i + xcent, j + xcent));
				else
					phanCrop.setAtIndex(i + thres, j + thres, 0);
			}
		
		return phanCrop;
	}
}
