package edu.stanford.rsl.Yixing.Celphalometric;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class ErosionDilationOperators {
	
	public void erode(Grid2D img, int diameter){
		
		Grid2D erodedVersion = new Grid2D(img.getWidth(), img.getHeight());

		for(int i = 0; i < erodedVersion.getHeight(); ++i){
			for(int j = 0; j < erodedVersion.getWidth(); ++j){
				erodedVersion.setAtIndex(j, i, 1.0f);
			}
		}

		for(int i = 0; i < img.getHeight(); ++i){
			for(int j = 0; j < img.getWidth(); ++j){
				ArrayList<Float> myList = new ArrayList<Float>();
				for(int k = 1; k < diameter/2; ++k){
					if(j-k < 0 || j+k >= img.getWidth()){

					}else{
						myList.add(img.getAtIndex(j-k, i));
						myList.add(img.getAtIndex(j+k, i));
					}
					float checker = 0;
					for(Float flag:myList){
						checker += (flag != null ? flag:Float.NaN);
					}
					if(checker == 0.0){
						erodedVersion.setAtIndex(j, i, 0.0f);
					}else{
						erodedVersion.setAtIndex(j, i, 1.0f);
					}
				}
			}
		}
		for(int i = 0; i < img.getHeight(); ++i){
			for(int j = 0; j < img.getWidth(); ++j){
				img.setAtIndex(j, i, erodedVersion.getAtIndex(j, i));
			}
		}
	}
}
