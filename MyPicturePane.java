package seamcarve;

import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import support.seamcarve.*;


/**
 * This class is your seam carving picture pane.  It is a subclass of PicturePane,
 * an abstract class that takes care of all the drawing, displaying, carving, and
 * updating of seams and images for you.  Your job is to override the abstract
 * method of PicturePane that actually finds the lowest cost seam through
 * the image.
 *
 * See method comments and handouts for specifics on the steps of the seam carving algorithm.
 *
 *
 * @version 01/17/2019
 */

public class MyPicturePane extends PicturePane {



	/**
	 * The constructor accepts an image filename as a String and passes
	 * it to the superclass for displaying and manipulation.
	 *
	 * @param pane
	 * @param filename
	 */
	public MyPicturePane(BorderPane pane, String filename) {
		super(pane, filename);

	}


	/**
	 * In this method, you'll implement the dynamic programming algorithm
	 * that you learned on the first day of class to find the lowest cost seam from the top
	 * of the image to the bottom. BEFORE YOU START make sure you fully understand how the algorithm works
	 * and what it's doing.
	 * See the handout for some helpful resources and use hours/piazza to clarify conceptual blocks
	 * before you attempt to write code.
	 *
	 * This method returns an array of ints that represents a seam.  This size of this array
	 * is the height of the image.  Each entry of the seam array corresponds to one row of the
	 * image.  The data in each entry should be the x coordinate of the seam in this row.
	 * For example, given the below "image" where s is a seam pixel and - is a non-seam pixel
	 *
	 * - s - -
	 * s - - -
	 * - s - -
	 * - - s -
	 *
	 *
	 * the following code will properly return a seam:
	 *
	 * int[] currSeam = new int[4];
	 * currSeam[0] = 1;
	 * currSeam[1] = 0;
	 * currSeam[2] = 1;
	 * currSeam[3] = 2;
	 * return currSeam;
	 *
	 *
	 * This method is protected so it is accessible to the class MyPicturePane and is not
	 * accessible to other classes. PLEASE DO NOT CHANGE THIS!
	 *
	 * @return the lowest cost seam of the current image
 	 */
	protected int[] findLowestCostSeam() {

		// This calculates the importance of each pixel.
		int[][] importanceArray = new int[this.getPicHeight()][this.getPicWidth()];
		for (int i = 0; i < this.getPicHeight(); i++) {
			for (int j = 0; j < this.getPicWidth(); j++) {

				importanceArray[i][j] =

					this.getImportance(i, j, i - 1, j) +
					this.getImportance(i, j, i + 1, j) +
					this.getImportance(i, j, i, j - 1) +
					this.getImportance(i, j, i, j + 1);
			}
		}

		// This calculates the lowest cost it takes to get to each pixel and the direction.
		int[][] costsArray = new int[this.getPicHeight()][this.getPicWidth()];
		int[][] dirsArray = new int[this.getPicHeight()][this.getPicWidth()];
		for (int i = this.getPicHeight() - 1; i >= 0 ; i--) {
			for (int j = this.getPicWidth() - 1; j >= 0; j--) {

				if (i + 1 >= this.getPicHeight()) {

					costsArray[i][j] = importanceArray[i][j];
				}

				else {

					costsArray[i][j] = this.getCostDirs(i, j, costsArray)[0] + importanceArray[i][j];
					dirsArray[i][j] = this.getCostDirs(i, j, costsArray)[1];
				}
			}
		}

		// This finds the lowest-cost value on the top row of the cost array.
		int minCost = costsArray[0][0];
		int minColumn = 0;
		for (int j = 0; j < this.getPicWidth(); j++) {

			if (costsArray[0][j] < minCost) {

				minCost = costsArray[0][j];
				minColumn = j;
			}
		}

		// This makes a seam array that contains the lowest-cost vertical seam.
		int[] seamArray = new int[this.getPicHeight()];
		seamArray[0] = minColumn;
		for (int x = 1; x < this.getPicHeight(); x++) {

			int seamColumn = 0;
			switch (dirsArray[x][seamArray[x - 1]]) {
				case -1:
					seamColumn = seamArray[x - 1] - 1;
					break;
				case 0:
					seamColumn = seamArray[x - 1];
					break;
				case 1:
					seamColumn = seamArray[x - 1] + 1;
					break;
			}
			seamArray[x] = seamColumn;
		}

		return seamArray;
	}

	/**
	 * This is a helper method used in the findLowestCostSeam() method. It takes in the row and column coordinates of
	 * the current pixel and a neighboring pixel and returns the importance of the current pixel with respect to one
	 * neighboring cell. It does this by finding the sum of the the differences of the red, green, and blue color values
	 * between the current pixel and the neighboring pixel. The findLowestCostSeam() method then adds all the sub-importance
	 * values of the current pixel to calculate the total importance value.
	 *
	 * @param currentRow
	 * @param currentColumn
	 * @param neighborRow
	 * @param neighborColumn
	 * @return the sub-importance value of the current pixel with respect to one neighboring cell
	 */
	public int getImportance(int currentRow, int currentColumn, int neighborRow, int neighborColumn) {

		int importanceValue = 0;
		Color currentColor = this.getPixelColor(currentRow, currentColumn);
		if (neighborRow >= 0 && neighborRow < this.getPicHeight() &&
			neighborColumn >= 0 && neighborColumn < this.getPicWidth()) {

			Color neighborColor = this.getPixelColor(neighborRow, neighborColumn);
			importanceValue = Math.abs(this.getColorRed(currentColor) - this.getColorRed(neighborColor)) +
					Math.abs(this.getColorGreen(currentColor) - this.getColorGreen(neighborColor)) +
					Math.abs(this.getColorBlue(currentColor) - this.getColorBlue(neighborColor));
		}

		return importanceValue;
	}

	/**
	 * This is a helper method used in the findLowestCostSeam() method. It takes in a 2D array of integers and
	 * the i and j values of the current cell, and it returns a 1D array that contains the cost and direction values.
	 * This method finds the minimum between the 3 cells below and adjacent to the current cell, while checking for out
	 * of bounds errors. This value is put into the 1D array. Next, this method puts a -1, 0, 1--corresponding to the
	 * left, straight, or right direction--into the 1D array. The findLowestCostSeam then uses these values to fill in
	 * a 2D costArray and directionArray.
	 *
	 * @param i
	 * @param j
	 * @param costsArray
	 * @return 1D array containing cost and direction values
	 */
	public int[] getCostDirs(int i, int j, int[][] costsArray) {

		int[] costDirs = new int[2];

		if (j - 1 < 0) {

			costDirs[0] = Math.min(costsArray[i + 1][j], costsArray[i + 1][j + 1]);
		}

		else if (j + 1 >= this.getPicWidth()) {

			costDirs[0] = Math.min(costsArray[i + 1][j], costsArray[i + 1][j - 1]);
		}

		else {

			costDirs[0] = Math.min(costsArray[i + 1][j - 1], Math.min(costsArray[i + 1][j], costsArray[i + 1][j + 1]));
		}

		if (j - 1 >= 0 && costDirs[0] == costsArray[i + 1][j - 1]) {

			costDirs[1] = -1;
		}

		else if (costDirs[0] == costsArray[i + 1][j]) {

			costDirs[1] = 0;
		}

		else if (j + 1 < this.getPicWidth() && costDirs[0] == costsArray[i + 1][j + 1]) {

			costDirs[1] = 1;
		}

		return costDirs;
	}
}