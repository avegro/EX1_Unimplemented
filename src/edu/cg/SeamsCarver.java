package edu.cg;

import java.awt.image.BufferedImage;
import java.lang.*;
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

	public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
			boolean[][] imageMask) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());
		this.initialHeight = workingImage.getHeight();
		this.indicesMatrix = initializeIndices(outWidth, this.initialHeight);
		numOfSeams = Math.abs(outWidth - inWidth);
		this.imageMask = imageMask;
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



		this.logger.log("preliminary calculations were ended.");
	}


	public void createEnergyMatrix(){
		int height = this.indicesMatrix.length;
		int width = this.indicesMatrix[0].length;
		long[][] ans = new long[height][width];
		int e1;
		int e2;
		int e3;

		for(int i = 0; i < height; i++){
			for(int j = 0; j< width; j++){
				e3 = (this.imageMask[i][indicesMatrix[i][j]]) ? Integer.MIN_VALUE: 0;
				e1 = (indicesMatrix[i][j] < width-1) ? Math.abs(greyImageMain.getRGB(i,indicesMatrix[i][j]) - greyImageMain.getRGB(i,indicesMatrix[i][j+1])): Math.abs(greyImageMain.getRGB(i,indicesMatrix[i][j]) - greyImageMain.getRGB(i,indicesMatrix[i][j-1]));
				e2 = (i < height-1) ? Math.abs(greyImageMain.getRGB(i,indicesMatrix[i][j]) - greyImageMain.getRGB(i+1,indicesMatrix[i][j])): Math.abs(greyImageMain.getRGB(i,indicesMatrix[i][j]) - greyImageMain.getRGB(i-1,indicesMatrix[i][j]));
				ans[i][j] = e1 + e2 + e3;
			}
		}
		this.energyMatrix = ans;
	}

	public void calculateCostMatrix(){
		int height = this.energyMatrix.length;
		int width = this.energyMatrix[0].length;
		long[][] ans = new long[height][width];
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				if (i == 0) {
					ans[i][j] = this.energyMatrix[i][j];
				}
				if(j > 0 && j < width-1){
					ans[i][j] = this.energyMatrix[i][j] + Math.min(Math.min(ans[i-1][j-1],ans[i-1][j]), ans[i-1][j+1]);
				}
				else if(j == 0){
					ans[i][j] = this.energyMatrix[i][j] + Math.min(ans[i-1][j],ans[i-1][j+1]);
				}
				else{
					ans[i][j] = this.energyMatrix[i][j] + Math.min(ans[i-1][j-1],ans[i-1][j]);
				}
			}
		}
		this.costMatrix = ans;
	}

	public void findSeam(){
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

			if(this.costMatrix[j][ind] == this.energyMatrix[j][ind] + this.costMatrix[j-1][ind] + __________){

			}
			else if(this.costMatrix[j][ind] == this.energyMatrix[j][ind] + this.costMatrix[j-1][ind-1] + __________){
				ind--;
			}
			else{
				ind++;
			}
		}
		this.seam = ans;

	}
	public int[][] initializeIndices(){
		int[][] ans = new int[workingImage.getHeight()][workingImage.getWidth()];
		for(int i = 0; i < workingImage.getHeight(); i++){
			for(int j = 0; j < workingImage.getWidth(); j++){
				ans[i][j] = j;

			}
		}
		this.indicesMatrix = ans;
	}

	public void updateIndicesMatrix(){
		int [][] ans = new int[this.seam.length][this.indicesMatrix[0].length - 1];
		Boolean found;
		for(int k = 0; k <  this.seam.length; k++){
			found = false;
			for(int m = 0; m < this.indicesMatrix[0].length-1; m++){
				if(m == this.seam[k]){
					ans[k][m] = indicesMatrix[k][m+1];
					found = true;
				}
				if (found){
					ans[k][m] = indicesMatrix[k][m+1];
				}
				else{
					ans[k][m] = indicesMatrix[k][m];
				}
			}
		}
		this.indicesMatrix = ans;
	}

	public BufferedImage resize() {
		return resizeOp.resize();
	}


		//	greyImageMain = this.greyscale();
		//	this.energyMatrix = createEnergyMatrix(greyImageMain, this.imageMask);
		//	this.costMatrix = calculateCostMatrix(energyMatrix);
		//	int[] seam = findSeam(this.costMatrix, this.energyMatrix);
		//	this.indicesMatrix = getIndicesMatrix(seam, this.outWidth);

	private BufferedImage reduceImageWidth() {
		greyImageMain = this.greyscale();
		while(this.indicesMatrix[0].length > this.outWidth){
			createEnergyMatrix();
			calculateCostMatrix();
			findSeam();
			updateIndicesMatrix();
		}



		throw new UnimplementedMethodException("reduceImageWidth");
	}

	private BufferedImage increaseImageWidth() {
		// TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("increaseImageWidth");
	}

	public BufferedImage showSeams(int seamColorRGB) {
		// TODO: Implement this method (bonus), remove the exception.
		throw new UnimplementedMethodException("showSeams");
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
		// This method should return the mask of the resize image after seam carving.
		// Meaning, after applying Seam Carving on the input image,
		// getMaskAfterSeamCarving() will return a mask, with the same dimensions as the
		// resized image, where the mask values match the original mask values for the
		// corresponding pixels.
		// HINT: Once you remove (replicate) the chosen seams from the input image, you
		// need to also remove (replicate) the matching entries from the mask as well.
		throw new UnimplementedMethodException("getMaskAfterSeamCarving");
	}
}
