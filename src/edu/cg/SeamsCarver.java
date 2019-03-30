package edu.cg;



import java.awt.*;
import java.awt.image.BufferedImage;

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
	// TODO: Add some additional fields
	BufferedImage result = workingImage;
	long[][] costMatrix;
	int[][] greyScaledWorkingImg;

	private int[] currentSeamIndices;
	private int[] currentSeamValues;
    private int[][] seamsIndices;
	private int[][] seamsValues;

	private static final int DELTA_X = 1;
	private static final int FACTOR = 50;


	public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
			boolean[][] imageMask) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());

		numOfSeams = Math.abs(outWidth - inWidth);
		this.imageMask = imageMask;
		if (inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");

		if (numOfSeams > inWidth / 2)
			throw new RuntimeException("Can not apply seam carving: too many seamsIndices...");

		// Setting resizeOp by with the appropriate method reference
		if (outWidth > inWidth)
			resizeOp = this::increaseImageWidth;
		else if (outWidth < inWidth)
			resizeOp = this::reduceImageWidth;
		else
			resizeOp = this::duplicateWorkingImage;

		// TODO: You may initialize your additional fields and apply some preliminary
		seamsIndices = new int[numOfSeams][];
        seamsValues = new int[numOfSeams][];

		this.logger.log("preliminary calculations were ended.");
	}

	public BufferedImage resize() {
		return resizeOp.resize();
	}

    public BufferedImage showSeams(int seamColorRGB) {
        // TODO: Implement this method (bonus), remove the exception.
        throw new UnimplementedMethodException("showSeams");
    }

    public boolean[][] getMaskAfterSeamCarving() {

        return new boolean[result.getHeight()][result.getWidth() - 1];
        // TODO: Implement this method, remove the exception.
        // This method should return the mask of the resize image after seam carving. Meaning,
        // after applying Seam Carving on the input image, getMaskAfterSeamCarving() will return
        // a mask, with the same dimensions as the resized image, where the mask values match the
        // original mask values for the corresponding pixels.
        // HINT:
        // Once you remove (replicate) the chosen seamsIndices from the input image, you need to also
        // remove (replicate) the matching entries from the mask as well.
//		throw new UnimplementedMethodException("getMaskAfterSeamCarving");
    }

    //Privates

	private BufferedImage reduceImageWidth() {
		for (int i = 0; i < numOfSeams; i++) {
            findMinSeamIndices(i);
            removeSeam(currentSeamIndices);
		}

		return result;
	}

    private void findMinSeamIndices(int seamNumber) {
        int x = minimumSeamIndex();
        int y = result.getHeight() - 1;
        currentSeamIndices = new int[result.getHeight()];
        currentSeamValues = new int[result.getHeight()];

        currentSeamIndices[y] = x;

        for (y = result.getHeight() - 1; y > 0; y--) {
            x = findNextMin(y, x);
            currentSeamValues[y - 1] = result.getRGB(x, y - 1);
            currentSeamIndices[y - 1] = x;
        }
        seamsIndices[seamNumber] = currentSeamIndices;
        seamsValues[seamNumber] = currentSeamValues;
    }

    private void removeSeam(int[] seamIndex) {
		BufferedImage ans = newEmptyImage(result.getWidth() - 1, result.getHeight());
		boolean[][] newMask = new boolean[result.getHeight()][result.getWidth() - 1];

		int colIndex;
		for (int i = 0; i < result.getHeight(); i++) {
			colIndex = 0;
			for (int j = 0; j < result.getWidth(); j++) {

				if (seamIndex[i] == j) {
					continue;
				}

				ans.setRGB(colIndex, i, result.getRGB(j, i));
				newMask[i][colIndex] = imageMask[i][j];
				colIndex++;
			}
		}
		imageMask = newMask;
		result = ans;
	}

	private int findNextMin(int i, int j) {

		long leftValue = -1;
		long centerValue = costMatrix[i - 1][j];
		long rightValue = -1;
		long min;

		//Initialize minimum candidates based on validity check.
		if (j == 0) {
			rightValue = costMatrix[i - 1][j + 1];
			min = Math.min(rightValue, centerValue);
		}
		else if (j == result.getWidth() - 1) {
			leftValue = costMatrix[i - 1][j - 1];
			min = Math.min(leftValue, centerValue);
		}
		else {
			leftValue = costMatrix[i - 1][j - 1];
			rightValue = costMatrix[i - 1][j + 1];
			min = min(leftValue, centerValue, rightValue);
		}

		//Return index accordingly
		if (min == leftValue) {
			return  j - 1;
		}
		else if (min == centerValue) {
			return j;
		}
		else {
			return j + 1;
		}
	}

	private BufferedImage increaseImageWidth() {

        result = reduceImageWidth();

        for (int i = numOfSeams - 1; i >= 0; i--) {
            result = recoverDoublesOfSeam(i);
        }

        return result;
	}

    private BufferedImage recoverDoublesOfSeam(int seamNumber) {

        BufferedImage ans = newEmptyImage(result.getWidth() + 2, result.getHeight());
        int colIndex;

        for (int i = 0; i < result.getHeight(); i++) {
            colIndex = 0;
            for (int j = 0; j < result.getWidth(); j++) {

                if (seamsIndices[seamNumber][i] == j) {
                    ans.setRGB(colIndex, i, seamsValues[seamNumber][i]);
                    ans.setRGB(colIndex + 1, i, seamsValues[seamNumber][i]);

                    colIndex += 2;
                }

                ans.setRGB(colIndex, i, result.getRGB(j, i));
                colIndex++;
            }
        }

        return ans;
    }

	private long pixelEnergy(int x, int y) {

		long forbiddenCost = imageMask[x][y] ? Integer.MAX_VALUE : 0;
        int verticalNeighbor = (x + 1 >= result.getHeight()) ? (x - 1) : (x + 1);
        int horizontalNeighbor = (y + 1 >= result.getWidth()) ? (y - 1) : (y + 1);
        long pixelValue = greyScaledWorkingImg[x][y];

        long horizontalNeighborValue;
		long verticalNeighborValue;


		verticalNeighborValue = greyScaledWorkingImg[verticalNeighbor][y];


		horizontalNeighborValue = greyScaledWorkingImg[x][horizontalNeighbor];

		long res = Math.abs(horizontalNeighborValue -
                pixelValue);
		res += Math.abs(verticalNeighborValue - pixelValue);
        res += forbiddenCost;


		return res;
	}

	private void calculateCostMatrix() {

		for (int i = 0; i < result.getHeight(); i++) {
			for (int j = 0; j < result.getWidth(); j++) {
				costMatrix[i][j] = (long)pixelEnergy(i, j) + calcForwardMin(i, j);
			}
		}
	}

	private long calcForwardMin(int i, int j) {

		long mv;
		long ml;
		long mr;

		//Maybe all three should be factor or maybe factor should be 255L test which works better
		long cv = FACTOR;
		long cr;
		long cl;

		//legal
		if (i > 0 && j > 0 && j < result.getWidth() - 1) {
			cv = Math.abs(greyScaledWorkingImg[i][j + 1] - greyScaledWorkingImg[i][j - 1]);
			cl = cv + Math.abs(greyScaledWorkingImg[i - 1][j] - greyScaledWorkingImg[i][j - 1]);
			cr = cv + Math.abs(greyScaledWorkingImg[i - 1][j] - greyScaledWorkingImg[i][j + 1]);

			mv = costMatrix[i - 1][j];
			ml = costMatrix[i - 1][j - 1];
			mr = costMatrix[i - 1][j + 1];
		}
		// i != 0, j == 0
		else if (i != 0 && j == 0) {
			cr = cv + Math.abs(greyScaledWorkingImg[i - 1][j] - greyScaledWorkingImg[i][j + 1]);

			mv = costMatrix[i - 1][j];
			mr = costMatrix[i - 1][j + 1];
			return Math.min(mv + cv, mr + cr);
		}
		// i != 0, j == width
		else if (i != 0 && j == result.getWidth() - 1) {
			cl = cv + Math.abs(greyScaledWorkingImg[i - 1][j] - greyScaledWorkingImg[i][j - 1]);

			mv = costMatrix[i - 1][j];
			ml = costMatrix[i - 1][j - 1];
			return Math.min(ml + cl, mv + cv);
		}
		// i == 0
		else {
			return 0L;
		}

		return min(ml + cl, mv + cv, mr + cr);
	}

	private static long min(long x, long y, long z) {
		long minXY = Math.min(x, y);
		return Math.min(minXY, z);
	}

	private void initializeCostMatrix() {
		costMatrix = new long[result.getHeight()][result.getWidth()];
		calculateCostMatrix();

	}

	private int[][] convertGreyScaleTo2DArray() {
		BufferedImage img = new ImageProcessor(logger, result, rgbWeights).greyscale();
		int[][] image = new int[this.result.getHeight()][this.result.getWidth()];

		for (int i = 0; i < result.getHeight(); i++) {

			for (int j = 0; j < result.getWidth(); j++) {
				Color c = new Color(img.getRGB(j, i));
				image[i][j] = c.getBlue();
			}
		}


		return image;
	}

	private int minimumSeamIndex() {
		greyScaledWorkingImg = convertGreyScaleTo2DArray();
		initializeCostMatrix();
		long min = Integer.MAX_VALUE;
		min <<= 8;
		int j = 0;
		int minIndex = -1;
		for (j = 0; j < result.getWidth(); j++) {
			if (min > costMatrix[result.getHeight() - 1][j]) {
				min = costMatrix[result.getHeight() - 1][j];
				minIndex = j;
			}
		}
		return minIndex;
	}
}
