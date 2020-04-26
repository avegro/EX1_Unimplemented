package edu.cg;

import java.awt.image.BufferedImage;
import java.lang.*;
import java.util.Arrays;

public class SeamsCarver extends ImageProcessor {

	// MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage resize();
	}

	// MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;
	boolean[][] imageMask;
	int[][] indicesMatrix;
	long[][] costMatrix;
	long[][] energyMatrix;
	int outWidth;
	int initialHeight;
	int[] seam;
	BufferedImage greyImageMain;
	int[][] savedSeams;
	int[] currentSeam;
	int[][] initialIndices;

	public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
			boolean[][] imageMask) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());
		this.initialHeight = workingImage.getHeight();
		initializeIndices();
		numOfSeams = Math.abs(outWidth - inWidth);
		this.savedSeams = new int[numOfSeams][workingImage.getHeight()];
		this.currentSeam = new int[workingImage.getHeight()];
		this.imageMask = imageMask;
		this.outWidth = outWidth;
		if (inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");

		if (numOfSeams > inWidth / 2)
			throw new RuntimeException("Can not apply seam carving: too many seams...");

		// Setting resizeOp by with the appropriate method reference
		if (outWidth > inWidth)
			resizeOp = this::increaseImageWidth;
		else if (outWidth < inWidth)
			resizeOp = this::reduceImageWidth;
		else
			resizeOp = this::duplicateWorkingImage;

		greyImageMain = this.greyscale();
		for(int i = 0; i < numOfSeams; i++){
			createEnergyMatrix();
			calculateCostMatrix();
			findSeam();
			updateIndicesMatrix();
			this.savedSeams[i] = this.currentSeam;
		}


		this.logger.log("preliminary calculations were ended.");
	}


	public void createEnergyMatrix(){
		try{
			int height = this.indicesMatrix.length;
			int width = this.indicesMatrix[0].length;
			long[][] ans = new long[height][width];
			int e1;
			int e2;
			int e3;
			for(int i = 0; i < height; i++){
				for(int j = 0; j< width; j++){
					e3 = (this.imageMask[i][indicesMatrix[i][j]]) ? Integer.MIN_VALUE: 0;

					e1 = (j < width-1) ? Math.abs(greyImageMain.getRGB(indicesMatrix[i][j],i) - greyImageMain.getRGB(indicesMatrix[i][j+1],i)): Math.abs(greyImageMain.getRGB(indicesMatrix[i][j],i) - greyImageMain.getRGB(indicesMatrix[i][j-1],i));

					e2 = (i < height-1) ? Math.abs(greyImageMain.getRGB(indicesMatrix[i][j],i) - greyImageMain.getRGB(indicesMatrix[i][j], i+1)): Math.abs(greyImageMain.getRGB(indicesMatrix[i][j],i) - greyImageMain.getRGB(indicesMatrix[i][j],i-1));

					ans[i][j] = e1 + e2 + e3;
				}
			}
			this.energyMatrix = ans;
		}
		catch(Exception e){
			throw new RuntimeException("calculate energy matrix failed");
		}
	}

	private long cL(int i, int j){
		try{
			long cLVal = 0;
			if(j < this.energyMatrix[0].length-1 && j > 0){
				cLVal = Math.abs(greyImageMain.getRGB(indicesMatrix[i][j],i-1) - greyImageMain.getRGB(indicesMatrix[i][j-1], i)) + Math.abs(greyImageMain.getRGB(indicesMatrix[i][j-1],i) - greyImageMain.getRGB(indicesMatrix[i][j+1], i));
			}
			else if(j == this.energyMatrix[0].length-1){
				cLVal = Math.abs(greyImageMain.getRGB(indicesMatrix[i][j-1],i) - greyImageMain.getRGB(indicesMatrix[i][j], i-1));
			}
			return cLVal;
		}
		catch(Exception e){
			throw new RuntimeException("cL function failed");

		}
	}

	private long cV(int i, int j){
		try{
			long cVVal = 0;

			if(j < this.energyMatrix[0].length-1 && j > 0){
				cVVal = Math.abs(greyImageMain.getRGB(indicesMatrix[i][j+1],i) - greyImageMain.getRGB(indicesMatrix[i][j-1], i));
			}
			return cVVal;
		}
		catch(Exception e){
			throw new RuntimeException("cV function failed");
		}
	}

	private long cR(int i, int j){
		try{
			long cRVal = 0;
			if(j < this.energyMatrix[0].length-1 && j > 0){
				cRVal = Math.abs(greyImageMain.getRGB(indicesMatrix[i][j-1],i) - greyImageMain.getRGB(indicesMatrix[i][j+1], i)) + Math.abs(greyImageMain.getRGB(indicesMatrix[i][j],i-1) - greyImageMain.getRGB(indicesMatrix[i][j+1], i));
			}
			else if(j == 0){
				cRVal = Math.abs(greyImageMain.getRGB(indicesMatrix[i][j],i-1) - greyImageMain.getRGB(indicesMatrix[i][j+1], i));
			}
			return cRVal;
		}
		catch(Exception e){
			throw new RuntimeException("cR function failed");
		}
	}
	public void calculateCostMatrix(){
		try{
			int height = this.energyMatrix.length;
			int width = this.energyMatrix[0].length;
			long[][] ans = new long[height][width];
			for(int i = 0; i < height; i++){
				for(int j = 0; j < width; j++){
					if (i == 0) {
						ans[i][j] = this.energyMatrix[i][j];
					}
					else {
						if(j > 0 && j < width-1){
							ans[i][j] = this.energyMatrix[i][j] + Math.min(Math.min(ans[i-1][j-1]+cL(i,j),ans[i-1][j]+cV(i,j)), ans[i-1][j+1] + cR(i,j));
						}
						else if(j == 0){
							ans[i][j] = this.energyMatrix[i][j] + Math.min(ans[i-1][j]+cV(i,j),ans[i-1][j+1]+cR(i,j));
						}
						else{
							ans[i][j] = this.energyMatrix[i][j] + Math.min(ans[i-1][j-1]+cL(i,j),ans[i-1][j]+cV(i,j));
						}
					}

				}
			}
			this.costMatrix = ans;
		}
		catch(Exception e){
			throw new RuntimeException("calculate cost matrix failed");
		}
	}

	public void findSeam(){
		try{
			int height = this.costMatrix.length;
			int width = this.costMatrix[0].length;
			long minim = this.costMatrix[height-1][0];
			int ind = 0;
			int[] ans = new int[height];

			for(int i = 0; i < width; i++){
				if(this.costMatrix[height-1][i] < minim){
					minim = this.costMatrix[height-1][i];
					ind = i;
				}
			}
			for(int j = height-1; j>0; j--){
				ans[j] = ind;

				if(this.costMatrix[j][ind] == this.energyMatrix[j][ind] + this.costMatrix[j-1][ind] + cV(j,ind)){
					ind = ind;
				}
				else if(ind > 0 && this.costMatrix[j][ind] == this.energyMatrix[j][ind] + this.costMatrix[j-1][ind-1] + cL(j,ind)){
					ind--;
				}
				else{
					ind = (ind < costMatrix[0].length - 1)? ind + 1: ind;
				}
			}
			this.seam = ans;
		}
		catch(Exception e){
			throw new RuntimeException("find seam function failed");
		}

	}
	public void initializeIndices(){
		try{
			int[][] ans = new int[workingImage.getHeight()][workingImage.getWidth()];
			for(int i = 0; i < workingImage.getHeight(); i++){
				for(int j = 0; j < workingImage.getWidth(); j++){
					ans[i][j] = j;

				}
			}
			this.initialIndices = ans;
			this.indicesMatrix = ans;
		}
		catch(Exception e){
			throw new RuntimeException("initializeIndices failed");
		}
	}

	public void updateIndicesMatrix(){
		try{
			int[] seamIndices = new int[this.workingImage.getHeight()];
			int [][] ans = new int[this.seam.length][this.indicesMatrix[0].length - 1];
			Boolean found;
			for(int k = 0; k <  this.seam.length; k++){
				found = false;
				for(int m = 0; m < this.indicesMatrix[0].length-1; m++){
					if(m == this.seam[k]){
						ans[k][m] = indicesMatrix[k][m+1];
						found = true;
						seamIndices[k] = indicesMatrix[k][m];
					}
					if (found){
						ans[k][m] = indicesMatrix[k][m+1];
					}
					else{
						ans[k][m] = indicesMatrix[k][m];
					}
				}
			}
			this.currentSeam = seamIndices;
			this.indicesMatrix = ans;
		}
		catch(Exception e){
			throw new RuntimeException("update indices matrix function failed");
		}
	}

	public BufferedImage resize() {
		return resizeOp.resize();
	}


	private BufferedImage reduceImageWidth() {

        try{
			BufferedImage ans = newEmptyOutputSizedImage();
			int pixelColor;
			System.out.println(this.outWidth);
			for(int i = 0; i < workingImage.getHeight(); i++){
				for(int j = 0; j < this.outWidth; j++){
					pixelColor = workingImage.getRGB(indicesMatrix[i][j],i);
					ans.setRGB(j,i,pixelColor);
				}
			}
			return ans;
		}
		catch(Exception e){
			throw new RuntimeException("reduceImageWidth function failed");

		}



	}

	public static int[][] transposeMatrix(int[][] matrix){
		for(int i = 0; i < matrix.length; i++){
			for(int j = i+1; j < matrix[0].length; j++){
				int temp = matrix[i][j];
				matrix[i][j] = matrix[j][i];
				matrix[j][i] = temp;
			}
		}
		return matrix;
	}
	private BufferedImage increaseImageWidth() {
		int pixelColor;
		BufferedImage ans = newEmptyOutputSizedImage();
		int whichColumn = initialIndices[0].length;
		int[][] indxArr = new int[workingImage.getHeight()][this.workingImage.getWidth() + this.numOfSeams];
		System.arraycopy (initialIndices, 0, indxArr, 0, initialIndices.length);
		int count = 0;
		int[][] transposedSeams = transposeMatrix(savedSeams);

		for(int p = 0; p < indxArr.length; p++){
			count = 0;
			for(int q = 0; q < indxArr[0].length; q++){
				if(q >= whichColumn){
					indxArr[p][q] = transposedSeams[p][count++];
				}
			}
		}



		for(int j = 0; j < indxArr.length; j++){
			System.out.println("sorting array #: " + j);
			Arrays.sort(indxArr[j]);
		}

		for(int k = 0; k < indxArr.length; k++){
			for(int m = 0; m < indxArr[0].length; m++){
				System.out.print(" " + indxArr[k][m]);
			}
			System.out.println();
		}

		for(int i = 0; i < workingImage.getHeight(); i++){
			for(int j = 0; j < this.outWidth; j++){
				pixelColor = workingImage.getRGB(indxArr[i][j],i);
				System.out.println("copying from index: " + i + ", " + indxArr[i][j] + " in the original image");
				ans.setRGB(j,i,pixelColor);
			}
		}
		return ans;
	}


	public BufferedImage showSeams(int seamColorRGB) {
		BufferedImage ans = newEmptyOutputSizedImage();
		// copy original image
		for(int i = 0; i < workingImage.getHeight(); i++){
			for(int j = 0; j < this.outWidth; j++){
				ans.setRGB(i,j,workingImage.getRGB(i,j));
			}
		}
		// change color of seams
		for(int i = 0; i < savedSeams.length; i++){
			for(int j = 0; j < savedSeams[0].length; j++){
				ans.setRGB(j,savedSeams[i][j],seamColorRGB);
			}
		}
		return ans;
	}

	public boolean[][] getMaskAfterSeamCarving() {
		int jVal;
		boolean[][] newMask = new boolean[indicesMatrix.length][indicesMatrix[0].length];
		for(int i = 0; i < indicesMatrix.length; i++){
			for(int j = 0; j < indicesMatrix[0].length; j++){
				jVal = indicesMatrix[i][j];
				newMask[i][j] = (this.imageMask[i][jVal]);
			}
		}
		return newMask;
		// This method should return the mask of the resize image after seam carving.
		// Meaning, after applying Seam Carving on the input image,
		// getMaskAfterSeamCarving() will return a mask, with the same dimensions as the
		// resized image, where the mask values match the original mask values for the
		// corresponding pixels.
		// HINT: Once you remove (replicate) the chosen seams from the input image, you
		// need to also remove (replicate) the matching entries from the mask as well.
	}
}
