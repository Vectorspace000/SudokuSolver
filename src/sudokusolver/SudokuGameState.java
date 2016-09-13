/*
 * Sudoku Solver
 * Author:    Jamie Speed
 * Date:      2016-09-13
 * Description:
 * I wanted to teach myself Java, so I built this.
 * This is a Sudoku Solving tool. The user can input a Sudoku puzzle and apply various solutions to it to see the results.
 * It also supports solving the whole puzzle, using logical solutions or brute force recursion.
 */
package sudokusolver;

/**
 *
 * @author Jamie Speed
 */

// Import swing and awt classes
import java.util.*;


/***********************************************************************
* class SudokuGameState
* 
* Contains the current state of the Sudoku grid - a 9x9 array of 
* SudokuCellState objects
* Methods for manipulating each cell, which contain:
*	Array of possibilities for each cell
*	Value of each cell (if it has a value), with initial value yes/no flag
*	Value has changed this iteration yes/no
*	Possibilities have changed this iteration yes/no
*	Each individual possibility has changed this iteration yes/no


***********************************************************************/
public class SudokuGameState
{

	private int valuesSet = 0;

	private int[][] value = new int[9][9];
	private boolean[][][] possibilities = new boolean[9][9][9];

	private boolean[][] isInitialValue = new boolean[9][9];

	private boolean[][] changed = new boolean[9][9];
	private boolean[][] valueChanged = new boolean[9][9];
	private boolean[][][] possibilitiesChanged = new boolean[9][9][9];
	private boolean[][] possibilityChanged = new boolean[9][9];

	private boolean[][] prevChanged = new boolean[9][9];
	private boolean[][] prevValueChanged = new boolean[9][9];
	private boolean[][][] prevPossibilitiesChanged = new boolean[9][9][9];
	private boolean[][] prevPossibilityChanged = new boolean[9][9];

	/****************************************
	 * SudokuGameState constructor
	 * 
	 * build 9x9 array of SudokuSudokuCellState objects
	 * 
	 ****************************************/
	SudokuGameState()
	{
		clear();
	}


	/****************************************
	 * cloneState()
	 * 
	 * Create a new, identical gamestate
	 ***************************************/
	SudokuGameState cloneState()
	{
		SudokuGameState newState = new SudokuGameState();
		newState.setAll(
				valuesSet,
				value,
				possibilities,
				isInitialValue,
				changed,
				valueChanged,
				possibilitiesChanged,
				possibilityChanged,
				prevChanged,
				prevValueChanged,
				prevPossibilitiesChanged,
				prevPossibilityChanged
			);
		return newState;
	}


	/****************************************
	 * setAll()
	 * 
	 * Set gameState using entry values
	 ***************************************/
	void setAll(
						int valuesSet,
						int[][] value,
						boolean[][][] possibilities,
						boolean[][] isInitialValue,
						boolean[][] changed,
						boolean[][] valueChanged,
						boolean[][][] possibilitiesChanged,
						boolean[][] possibilityChanged,
						boolean[][] prevChanged,
						boolean[][] prevValueChanged,
						boolean[][][] prevPossibilitiesChanged,
						boolean[][] prevPossibilityChanged
					)
	{
		this.valuesSet = valuesSet;
		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				this.value[x][y] = value[x][y];
				this.isInitialValue[x][y] = isInitialValue[x][y];
				this.changed[x][y] = changed[x][y];
				this.valueChanged[x][y] = valueChanged[x][y];
				this.possibilityChanged[x][y] = possibilityChanged[x][y];
				this.prevChanged[x][y] = prevChanged[x][y];
				this.prevValueChanged[x][y] = prevValueChanged[x][y];
				this.prevPossibilityChanged[x][y] = prevPossibilityChanged[x][y];
				for (int i = 0; i < 9; i++)
				{
					this.possibilities[x][y][i] = possibilities[x][y][i];
					this.possibilitiesChanged[x][y][i] = possibilitiesChanged[x][y][i];
					this.prevPossibilitiesChanged[x][y][i] = prevPossibilitiesChanged[x][y][i];
				}
			}
		}
	}

	/****************************************
	 * copyFrom()
	 * 
	 * Update this GameState instance's fields from the specified GameState
	 ***************************************/
	void copyFrom(SudokuGameState fromState)
	{

		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				value[x][y] = fromState.value[x][y];
				isInitialValue[x][y] = fromState.isInitialValue[x][y];
				changed[x][y] = fromState.changed[x][y];
				valueChanged[x][y] = fromState.valueChanged[x][y];
				possibilityChanged[x][y] = fromState.possibilityChanged[x][y];
				prevChanged[x][y] = fromState.prevChanged[x][y];
				prevValueChanged[x][y] = fromState.prevValueChanged[x][y];
				prevPossibilityChanged[x][y] = fromState.prevPossibilityChanged[x][y];
				for (int i = 0; i < 9; i++)
				{
					possibilities[x][y][i] = fromState.possibilities[x][y][i];
					possibilitiesChanged[x][y][i] = fromState.possibilitiesChanged[x][y][i];
					prevPossibilitiesChanged[x][y][i] = fromState.prevPossibilitiesChanged[x][y][i];
				}
			}
		}

	}

	

	/****************************************
	 * clear()
	 * 
	 * Clear game  state (clear state of each cell)
	 ***************************************/
	void clear()
	{
		valuesSet = 0;
		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				value[x][y] = 0;
				isInitialValue[x][y] = false;

				prevValueChanged[x][y] = false;						// Default changed flags to all false
				prevChanged[x][y] = false;
				prevPossibilityChanged[x][y] = false;
				Arrays.fill(prevPossibilitiesChanged[x][y],false);

				cycleIteration(x+1,y+1);								// Cycle iteration to set previous flags to false
				Arrays.fill(possibilities[x][y],true);				// Set all possibilities
			}
		}
	}

	/****************************************
	 * getValue()
	 *
	 * Retrieve the value of a particular cell
	 * 
	 ***************************************/
	int getValue(int x, int y)
	{
		return value[x-1][y-1];
	}

	/****************************************
	 * isInitialValue()
	 *
	 * Returns if the specified cell is an Initial Value
	 ***************************************/
	boolean isInitialValue(int x, int y)
	{
		return isInitialValue[x-1][y-1];
	}

	/****************************************
	 * setValue()
	 *
	 * Set the value for a specific cell, then update the
	 * possibilities accordingly
	 * 
	 ***************************************/
	void setValue(int x, int y, int value, boolean initialValue)
	{
		if (value > 0)
		{
			this.value[x-1][y-1] = value;
			if(initialValue)
				isInitialValue[x-1][y-1] = true;
			else
			{
				changed[x-1][y-1] = true;
				valueChanged[x-1][y-1] = true;
			}
			for (int i = 1; i <= 9; i++)
			{
				if (i != value)
					clearPossibility(x,y,i);
			}

			valuesSet++;
		}
	}

	/****************************************
	 * getSetValues()
	 *
	 * Get the number of values set so far (81 = solved)
	 ***************************************/
	int getSetValues()
	{
		return valuesSet;
	}

	/****************************************
	 * getRemainingValues()
	 *
	 * Get the number of values set so far (81 = solved)
	 ***************************************/
	int getRemainingValues()
	{
		return 81-valuesSet;
	}

	/****************************************
	 * getPossibility()
	 *
	 * Get if a particular value is possible in a particular cell
	 ***************************************/
	boolean getPossibility(int x, int y, int value)
	{
		return possibilities[x-1][y-1][value-1];
	}


	/****************************************
	 * clearPossibility()
	 *
	 * Set that a particular value is not possible in a particular cell
	 ***************************************/
	void clearPossibility(int x, int y, int value)
	{
		clearPossibility(x,y,value, false);
	}
	void clearPossibility(int x, int y, int value, boolean initialValue)
	{
		if (possibilities[x-1][y-1][value-1])
		{
			possibilities[x-1][y-1][value-1] = false;
			if (initialValue == false)
			{
				changed[x-1][y-1] = true;
				possibilitiesChanged[x-1][y-1][value-1] = true;
				possibilityChanged[x-1][y-1] = true;
			}
		}
	}

	/****************************************
	 * possibilityChanged()
	 *
	 * Get if one or more possibilities in this cell have changed this iteration 
	 ***************************************/
	boolean possibilityChanged(int x, int y)
	{
		return possibilityChanged[x-1][y-1];
	}
	boolean possibilityChanged(int x, int y, int value)
	{
		return possibilitiesChanged[x-1][y-1][value-1];
	}

	/****************************************
	 * valueChanged()
	 *
	 * Get if the value this cell has changed this iteration 
	 ***************************************/
	boolean valueChanged(int x, int y)
	{
		return valueChanged[x-1][y-1];
	}

	/****************************************
	 * prevPossibilityChanged()
	 *
	 * Get if one or more possibilities in this cell have changed in the previous iteration 
	 ***************************************/
	boolean prevPossibilityChanged(int x, int y)
	{
		return prevPossibilityChanged[x-1][y-1];
	}
	boolean prevPossibilityChanged(int x, int y, int value)
	{
		return prevPossibilitiesChanged[x-1][y-1][value-1];
	}

	/****************************************
	 * prevValueChanged()
	 *
	 * Get if the value this cell has changed in the previous iteration 
	 ***************************************/
	boolean prevValueChanged(int x, int y)
	{
		return prevValueChanged[x-1][y-1];
	}

	/****************************************
	 * cycleIteration()
	 *
	 * Iteration has changed, udpate changed flags accordingly
	 ***************************************/
	void cycleIteration()
	{
		for (int y = 1; y <= 9; y++)
		{
			for (int x = 1; x <= 9; x++)
			{
				cycleIteration(x,y);
			}
		}
	}
	void cycleIteration(int x, int y)
	{
		prevValueChanged[x-1][y-1] = valueChanged[x-1][y-1];
		prevChanged[x-1][y-1] = changed[x-1][y-1];
		prevPossibilityChanged[x-1][y-1] = possibilityChanged[x-1][y-1];
		System.arraycopy(possibilitiesChanged[x-1][y-1],0,prevPossibilitiesChanged[x-1][y-1],0,9);

		valueChanged[x-1][y-1] = false;
		changed[x-1][y-1] = false;
		possibilityChanged[x-1][y-1] = false;
		Arrays.fill(possibilitiesChanged[x-1][y-1],false);
	}

	/****************************************
	 * validate()
	 *
	 * Validate the GameState
	 ***************************************/
	boolean validate()
	{
		boolean[] found = new boolean[9];

		// Validate by Row
		for (int y = 0; y < 9; y++)			// For every row
		{
			Arrays.fill(found,false);
			for (int x = 0; x < 9; x++)		// For every cell in that row
			{
				if (value[x][y] != 0)		// If cell has a value	
				{
					if (found[value[x][y]-1] == true)		// If we have already encountered this vlaue in this row, return false, gameState is invalid
					{
					//	System.out.println("Invalid by Row: " + (x+1) + "," + (y+1));
						return false;
					}
					found[value[x][y]-1] = true;			// Else set this value as found
				}
			}
		}

		// Validate by Column
		for (int x = 0; x < 9; x++)			// For every column
		{
			Arrays.fill(found,false);
			for (int y = 0; y < 9; y++)		// For every cell in that column
			{
				if (value[x][y] != 0)		// If cell has a value	
				{
					if (found[value[x][y]-1] == true)		// If we have already encountered this vlaue in this row, return false, gameState is invalid
					{
						//System.out.println("Invalid by Column: " + (x+1) + "," + (y+1));
						return false;
					}
					found[value[x][y]-1] = true;			// Else set this value as found
				}
			}
		}

		// validate by 3x3
		for (int y1 = 0; y1 < 9; y1+=3)					// For every 3x3 box (top left coord)
		{
			for (int x1 = 0; x1 < 9; x1+=3)
			{
				Arrays.fill(found,false);
				for (int y = y1; y < y1+3; y++)				// For every cell in this 3x3
				{
					for (int x = x1; x < x1+3; x++)
					{
						if (value[x][y] != 0)		// If cell has a value	
						{
							if (found[value[x][y]-1] == true)		// If we have already encountered this vlaue in this row, return false, gameState is invalid
							{
								//System.out.println("Invalid by 3x3: " + (x+1) + "," + (y+1));
								return false;
							}
							found[value[x][y]-1] = true;			// Else set this value as found
						}
					}
				}
			}
		}

		//System.out.println("GameState is valid");
		return true;
	}

	/****************************************
	 * valueStateToString()
	 *
	 * Output the value state as a string - 9 rows of 9 with line breaks
	 ***************************************/
	String valueStateToString()
	{
		// Copy state to string
		StringBuilder textOut = new StringBuilder();
		for (int y = 0; y < 9; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				textOut.append(Integer.toString(value[x][y]));
			}
			textOut.append("%n");	// Format code for platform specific newline character
		}
		return String.format(textOut.toString());	// Format string, to convert %n to platform specific newline character, and return it
	}
		
}
