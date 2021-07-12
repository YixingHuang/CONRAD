package edu.stanford.rsl.Yixing.truncation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.filtering.PoissonNoiseFilteringTool;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.tutorial.parallel.ParallelBackprojector2D;
import edu.stanford.rsl.tutorial.parallel.ParallelProjector2D;
import edu.stanford.rsl.tutorial.fan.FanBeamProjector2D;
import edu.stanford.rsl.tutorial.fan.FanBeamBackprojector2D;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.conrad.geometry.transforms.Transform;

import edu.stanford.rsl.conrad.geometry.Rotations;

import edu.stanford.rsl.conrad.geometry.transforms.ScaleRotate;

import edu.stanford.rsl.conrad.numerics.SimpleMatrix;

/**
 * Do parallel-beam FBP reconstruction from truncated projections,
 * 
 * @author Yixing Huang
 *
 *
 *         read 18 patients data, sinogram with water cylinder extrapolation,
 *         truncation and no truncation,reconstructed by FBP , .
 */

public class FBPforSinogram {

	public static void main(String[] args) throws Exception {
		new ImageJ();
		boolean Imshow = true;
		boolean debug = true;

		File directoryPath = new File("C:\\Users\\Yixing Huang\\Desktop\\Lei");

		FilenameFilter textFilefilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith("_unet.tif")) {
					return true;
				} else {
					return false;
				}
			}
		};
		// List of all the text files
		File filesList[] = directoryPath.listFiles(textFilefilter);
		System.out.println("List of the text files in the specified directory:");
		for (File file : filesList) {

			System.out.println(file.getName());

			System.out.println(file.getAbsolutePath());

			String UnetVolPath = file.getAbsolutePath();
			String refPath = UnetVolPath.replace("_unet.tif", "_ref.tif");
			String reconUnetPath = UnetVolPath.replace("_unet.tif", "_unetRecon.tif");
			String ArtifactPath = UnetVolPath.replace("_unet.tif", "_artifact.tif");

			String FBPPath = UnetVolPath.replace("_unet.tif", "_FBP.tif");

			int sizeX = 512;
			int sizeY = sizeX;
			ImagePlus imp, impRef, impreconUnetArtifact, ImreconUnet, ImreconRef,impUnet;
			FBPforSinogram obj = new FBPforSinogram();
			Grid2D filteredSinogram, reconUnet;

		//	ParallelBackprojector2D backproj = new ParallelBackprojector2D(sizeX / 2, sizeY / 2, 2, 2);
			ParallelBackprojector2D backproj = new ParallelBackprojector2D(sizeX , sizeY , 1, 1);

			// reconstruct from reference sinogram
			impRef = IJ.openImage(refPath);
			Grid3D volRef = ImageUtil.wrapImagePlus(impRef);

			Grid2D filteredSinogramRef = volRef.getSubGrid(0);
			if (Imshow)
				filteredSinogramRef.clone().show("filteredSinogramRef ");

			Grid2D reconRef = backproj.backprojectPixelDriven(filteredSinogramRef);
			if (Imshow)
				reconRef.clone().show("reconRef");

			ImreconRef = ImageUtil.wrapGrid(reconRef, null);
			IJ.saveAs(ImreconRef, "Tiff", FBPPath);

			// reconstruct from Unet sinogram
			impUnet = IJ.openImage(UnetVolPath);
			Grid3D volUnet = ImageUtil.wrapImagePlus(impUnet);

			Grid2D filteredSinogramUnet = volUnet.getSubGrid(0);
			filteredSinogramUnet.setSpacing(filteredSinogramRef.getSpacing());
			if (Imshow)
				filteredSinogramUnet.clone().show("filteredSinogramUnet ");

			reconUnet = backproj.backprojectPixelDriven(filteredSinogramUnet);
			if (Imshow)
				reconUnet.clone().show("reconUnet");

			ImreconUnet = ImageUtil.wrapGrid(reconUnet, null);
			IJ.saveAs(ImreconUnet, "Tiff", FBPPath);
									
			//project again
			ParallelProjector2D projector = new ParallelProjector2D(Math.PI, Math.PI / 256.0, 768, 1);
			Grid2D sinogramUnet=projector.projectRayDrivenCL(reconUnet);

			if (Imshow)
				sinogramUnet.clone().show("sinogramUnet project again");
			
			Grid2D filteredsinogramUnet = new Grid2D(sinogramUnet);

			filteredsinogramUnet.getGridOperator().divideBy(filteredsinogramUnet, 50f);
			double deltaT = 1;
			double maxT = 768;
			int numDet = (int) (maxT / deltaT);
			RamLakKernel ramLak = new RamLakKernel(numDet, deltaT);
			for (int theta = 0; theta < sinogramUnet.getSize()[1]; ++theta) {
				ramLak.applyToGrid(filteredsinogramUnet.getSubGrid(theta));
			}

			filteredsinogramUnet.getGridOperator().multiplyBy(filteredsinogramUnet, 50f);

			Grid2D reconUnetAgain = backproj.backprojectPixelDriven(filteredsinogramUnet);
			if (Imshow)
				reconUnetAgain.clone().show("reconUnetAgain");
			

			ImreconUnet = ImageUtil.wrapGrid(reconUnet, null);
			IJ.saveAs(ImreconUnet, "Tiff", reconUnetPath);
			
			// read reference image, imref

			reconUnet.getGridOperator().subtractBy(reconUnet, reconRef);
			if (Imshow)
				reconUnet.clone().show("reconUnet artifact");
			impreconUnetArtifact = ImageUtil.wrapGrid(reconUnet, null);
			IJ.saveAs(impreconUnetArtifact, "Tiff", ArtifactPath);

			System.out.println();
			System.out.println("Finished!");

			break;

		}

	}

	private void addPoissonNoise(Grid2D sinogram, double d) throws Exception {
		double val;
		float amp = 50.f;
		sinogram.getGridOperator().divideBy(sinogram, amp);
		Grid2D I = new Grid2D(sinogram.getWidth(), sinogram.getHeight());
		for (int i = 0; i < sinogram.getWidth(); i++)
			for (int j = 0; j < sinogram.getHeight(); j++) {
				val = d * Math.pow(Math.E, -sinogram.getAtIndex(i, j));
				I.setAtIndex(i, j, (float) (val));
			}

		PoissonNoiseFilteringTool poisson = new PoissonNoiseFilteringTool();
		poisson.applyToolToImage(I);

		for (int i = 0; i < sinogram.getWidth(); i++)
			for (int j = 0; j < sinogram.getHeight(); j++) {
				val = -Math.log(I.getAtIndex(i, j) / d);
				sinogram.setAtIndex(i, j, (float) (val >= 0 ? val : 0));

			}
		sinogram.getGridOperator().multiplyBy(sinogram, amp);
	}

	private void truncateSinogram(Grid2D sinogram, Grid2D sinogramTrunc, int truncWidth) {
		for (int i = 0; i < sinogramTrunc.getWidth(); i++)
			for (int j = 0; j < sinogramTrunc.getHeight(); j++)
				sinogramTrunc.setAtIndex(i, j, sinogram.getAtIndex(i + truncWidth, j));
	}

	private void truncateSinogram2(Grid2D sinogram, Grid2D sinogramTrunc, int truncWidth) {

		for (int i = truncWidth; i < sinogramTrunc.getWidth() - truncWidth; i++) {
			for (int j = 0; j < sinogramTrunc.getHeight(); j++) {
				sinogramTrunc.setAtIndex(i, j, sinogram.getAtIndex(i, j));
			}
		}
	}

	private void phantomRotation(Grid2D phantom, double angle) {

		phantom.setOrigin(-phantom.getWidth() / 2, -phantom.getHeight() / 2);

		SimpleMatrix rotation = Rotations.createBasicZRotationMatrix(angle);

		ScaleRotate rot = new ScaleRotate(rotation.getSubMatrix(2, 2));
		phantom.applyTransform(rot);
		phantom.setOrigin(phantom.getWidth() / 2, phantom.getHeight() / 2);

	}

	private void phantomFlip(Grid2D phantom) {
		for (int i = 0; i < phantom.getWidth() / 2; i++) {
			for (int j = 0; j < phantom.getHeight(); j++) {
				float tempV = phantom.getAtIndex(i, j);
				phantom.setAtIndex(i, j, phantom.getAtIndex(phantom.getWidth() - i - 1, j));
				phantom.setAtIndex(phantom.getWidth() - i - 1, j, tempV);
			}
		}

	}
}
