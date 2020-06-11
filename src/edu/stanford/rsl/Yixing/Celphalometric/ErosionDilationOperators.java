package edu.stanford.rsl.Yixing.Celphalometric;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class ErosionDilationOperators {
	
	public void erode(Grid2D img, int diameter){
		
		Grid2D erodedVersion = new Grid2D(img.getWidth(), img.getHeight());

		for(int i = 0; i < erodedVersion.getWidth(); ++i){
			for(int j = 0; j < erodedVersion.getHeight(); ++j){
				erodedVersion.setAtIndex(i, j, 1.0f);
			}
		}

		for(int i = 0; i < img.getWidth(); ++i){
			for(int j = 0; j < img.getHeight(); ++j){
				ArrayList<Float> myList = new ArrayList<Float>();
				for(int k = 1; k < diameter/2; ++k){
					if(i-k < 0 || i+k >= img.getWidth()){

					}else{
						myList.add(img.getAtIndex(i-k, j));
						myList.add(img.getAtIndex(i+k, j));
					}
					float checker = 0;
					for(Float flag:myList){
						checker += (flag != null ? flag:Float.NaN);
					}
					if(checker == 0.0){
						erodedVersion.setAtIndex(i, j, 0.0f);
					}else{
						erodedVersion.setAtIndex(i, j, 1.0f);
					}
				}
			}
		}
		for(int i = 0; i < img.getWidth(); ++i){
			for(int j = 0; j < img.getHeight(); ++j){
				img.setAtIndex(i, j, erodedVersion.getAtIndex(i, j));
			}
		}
	}
}
