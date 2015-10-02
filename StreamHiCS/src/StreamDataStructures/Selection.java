package streamDataStructures;

import java.util.Random;
import org.apache.commons.math3.util.MathArrays;

/**
 * This class represents a selection of indexes.
 * 
 * @author Vincent
 *
 */
public class Selection {

	/**
	 * The indexes currently held.
	 */
	private double[] indexes;
	/**
	 * THe selection alpha.
	 */
	private double selectionAlpha;
	/**
	 * A generator for random numbers.
	 */
	private Random generator;

	/**
	 * Creates a {@link Selection} object.
	 * 
	 * @param initialSize
	 *            The initial size of the index array.
	 * @param selectionAlpha
	 *            The selection alpha.
	 */
	public Selection(int initialSize, double selectionAlpha) {
		indexes = new double[initialSize];
		this.selectionAlpha = selectionAlpha;
		generator = new Random();
	}

	/**
	 * Returns the number of indexes currently held.
	 * 
	 * @return The number of indexes.
	 */
	public int size() {
		return indexes.length;
	}

	/**
	 * Returns the index at the given position.
	 * 
	 * @param i
	 *            The position.
	 * @return The index at the given position.
	 */
	public int getIndex(int i) {
		return (int) indexes[i];
	}

	/**
	 * Fills the index array with the range beginning at 0.
	 */
	public void fillRange() {
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}
	}

	/**
	 * Selects a range of indexes according to given data. The indexes are
	 * arranged so that they correspond to the sorted data. Then a random block
	 * selection of the size selectionAlpha*(size of data) is drawn,
	 * corresponding to a block of values in the sorted data. The indexes of
	 * that selection are stored.
	 * 
	 * @param data
	 *            The data.
	 */
	public void select(double[] data) {
		int l = data.length;
		int selectionSize = (int) (l * selectionAlpha);

		if (selectionSize < l) {
			// Sort the data and arrange the indexes correspondingly
			MathArrays.sortInPlace(data, indexes);
			// Start at a random point and take the selectionSize
			int startingPoint = generator.nextInt(l - selectionSize + 1);
			selectRange(data, startingPoint, selectionSize);
		}
	}

	/**
	 * Selects a given range from the given indexes.
	 * 
	 * @param startingPoint
	 *            The starting point for selection.
	 * @param selectionSize
	 *            The number of elements to be selected.
	 * @throws IllegalArgumentException
	 *             if the range is out of the bounds of the ArrayList.
	 */
	private void selectRange(double[] data, int startingPoint, int selectionSize) {
		int endPoint = startingPoint + selectionSize - 1;
		if (startingPoint < 0 || endPoint > indexes.length - 1) {
			throw new IllegalArgumentException("Selection outside of range: [" + startingPoint + endPoint + "]");
		}
		if (data[startingPoint] == data[endPoint]) {
			// The special case that all the data values selected are the
			// same. This case needs special handling.
			System.out.println("Selection.selectRange(): Special handling!");
			// Broadening the range if possible, until data values on the
			// outside of the range differ
			while (data[startingPoint] == data[endPoint]
					&& (endPoint - startingPoint) < indexes.length - 1) {
				if (startingPoint > 0) {
					startingPoint--;
				}
				if (endPoint < indexes.length - 1) {
					endPoint++;
				}
			}
		}
		double[] newIndexes = new double[endPoint - startingPoint + 1];
		for (int i = startingPoint; i <= endPoint; i++) {
			newIndexes[i - startingPoint] = indexes[i];
		}

		// Set the indexes to the new range of indexes
		indexes = newIndexes;
	}

	/**
	 * Returns a String representation of this object.
	 * 
	 * @return A String representation of this object.
	 */
	public String toString() {
		String rep = "";
		for (int i = 0; i < indexes.length - 1; i++) {
			rep += ((int) indexes[i]) + ", ";
		}
		rep += (int) indexes[indexes.length - 1];
		return rep;
	}
}