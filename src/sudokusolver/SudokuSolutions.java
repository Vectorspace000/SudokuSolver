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
import java.util.*;

/***********************************************************************
* class SudokuSolutions
* 
* Contains methods for solving Sudoku puzzles solutions
* Methods are applied to SudokuGameState
* 
***********************************************************************/
public class SudokuSolutions
{

	private SudokuGameState gameState;
	private SudokuGameState recursiveSolvedState;
	private LogTextArea log;

	/***********************************************************************
	* SudokuSolutions()
	* 
	* Constructor. Store reference to game state
	***********************************************************************/
	SudokuSolutions(SudokuGameState gameState, LogTextArea log)
	{
		this.gameState = gameState;
		this.log = log;
	}

	/***********************************************************************
	* Solve()
	* 
	* Try all solutions = 1 iteration
	* Repeat until solved, or until all solutions produced zero udpates in one interation
	***********************************************************************/
	boolean solve()
	{
		int updatesThisIter;
		int maxIter = 50;

		for (int i = 0; i < maxIter; i++)
		{
			updatesThisIter = 0;
			updatesThisIter += updatePossibilities();
			updatesThisIter += singlePossibility();
			if (gameState.getRemainingValues() == 0)
				return true;
			updatesThisIter += singlePossibility();
			if (gameState.getRemainingValues() == 0)
				return true;
			updatesThisIter += singleInColumn();
			if (gameState.getRemainingValues() == 0)
				return true;
			updatesThisIter += singleInRow();
			if (gameState.getRemainingValues() == 0)
				return true;
			updatesThisIter += singleIn3x3();
			if (gameState.getRemainingValues() == 0)
				return true;
			if (updatesThisIter == 0)
			{
				System.out.println("Solve failed, no updates this iteration");
				return false;
			}
		}
		System.out.println("Solve failed, max iterations reached");
		return false;
		
	}

	/***********************************************************************
	* updatePossibilities()
	* 
	* Possibility Updates - By Value
	* For every cell that has a value
	* 	No other cell in the same row/col/3x3 can have that value. Remove it as a possibility if set
	*   Return number of possibilities removed.
	* Can be called for a specific cell or all cells
	* Can be called with a specific gamestate or use the current gamestate
	***********************************************************************/
	int updatePossibilities()
	{
		return updatePossibilities(gameState);
	}
	int updatePossibilities(SudokuGameState currentState)
	{
		int count = 0;

		for (int y = 1; y <= 9; y++)					// For every cell
		{
			for (int x = 1; x <= 9; x++)
			{
				count += updatePossibilities(currentState,x,y);		// Update possibilities in the row/column/3x3
			}
		}
		return count;
	}

	int updatePossibilities(int x, int y)
	{
		return updatePossibilities(gameState, x, y);
	}
	int updatePossibilities(SudokuGameState currentState, int x, int y)
	{
		int count = 0;
		int value;
		int x3Start;			// Start and end of this 3x3 block
		int x3End;
		int y3Start;
		int y3End;

		value = currentState.getValue(x,y);
		if (value > 0)							// If the cell has a value
		{

			for (int x1 = 1; x1 <= 9; x1++)		// Remove this possibility from every other cell in the row
			{
				if (x != x1 && currentState.getPossibility(x1,y,value))	// If not same cell, and if not already removed
				{
					currentState.clearPossibility(x1,y,value);
					count++;
				}
			}
			for (int y1 = 1; y1 <= 9; y1++)		// Remove this possibility from the other cells in this column
			{
				if (y != y1 && currentState.getPossibility(x,y1,value))	// If not same cell, and if not already removed
				{
					currentState.clearPossibility(x,y1,value);
					count++;
				}
			}

			// Remove this possibility from the other cells in this 3x3 box
			x3Start = (((x-1)/3)*3)+1;				// Start and end of this 3x3 block
			x3End = x3Start + 3;
			y3Start = (((y-1)/3)*3)+1;
			y3End = y3Start + 3;
			for (int y1 = y3Start; y1 < y3End; y1++)	// For every cell in this 3x3
			{
				for (int x1 = x3Start; x1 < x3End; x1++)
				{
					if (x != x1 && y != y1 && currentState.getPossibility(x1,y1,value))	// If not same cell, and if not already removed
					{
						currentState.clearPossibility(x1,y1,value);
						count++;
					}
				}
			}
		}

		return count;
	}

	/***********************************************************************
	* singlePossibility()
	* 
	* Value Updates - Single Possibility
	* 
	* For every cell.
	* 	If that cell has only a single possibility, it must be that value. Set it to that value
	*   Return number of values added.
	***********************************************************************/
	int singlePossibility()
	{
		int count = 0;

		int valueFound;

		for (int x = 1; x <= 9; x++)				// For every cell  
		{
			for (int y = 1; y <= 9; y++)
			{
				valueFound = 0;
				if (gameState.getValue(x,y) == 0)		// If this cell does not have a value yet
				{
					for (int i = 1; i <= 9; i++)	// Check every possibile value, if only one possibility store it
					{
						if (gameState.getPossibility(x,y,i))	// If possible
						{
							if (valueFound == 0)				// If first find, store it
								valueFound = i;
							else								// If this the second find, clear found and stop checking this cell - does not have single possibility
							{
								valueFound = 0;
								break;
							}
						}
					}
				}
				if (valueFound > 0)				// If we only found possibility, this cell must have that value
				{
					gameState.setValue(x,y,valueFound,false);
					count++;
				}
			}
					
		}		

		return count;
	}

	/***********************************************************************
	* singleInColumn()
	* 
	* Value Updates - Single In Column
	* 
	* For every column
	* 	For Every cell in that column
	* 		If only one cell in this column has a specific possibility, it must be that value. Set it to that value
	*  		 Return number of values added.
	***********************************************************************/
	int singleInColumn()
	{
		int count = 0;

		int yFound;

		for (int i = 1; i <= 9; i++)				// for every possibility 1-9
		{
			for (int x = 1; x <= 9; x++)				// For every column
			{
				yFound = 0;
				for (int y = 1; y <= 9; y++)			// for every cell in this column y 1-9, check if only one cell with this possibility
				{
					if (gameState.getValue(x,y) == i)	// If a cell in this column is already set to this value, no other cell can be so forget it
					{
						yFound = 0;
						break;
					}	

					if (gameState.getPossibility(x,y,i))	// If this cell has this possibility
					{
						// We have found this possibility in this cell
						if (yFound == 0)				// If this the first find, set found and record the coordinate
							yFound = y;
						else							// If this the second find, clear found and stop checking this column
						{
							yFound = 0;
							break;
						}
					}
				}
				if (yFound > 0)				// If we only found one cell with this possibility, that cell must have that value (unless it does already)
				{
					gameState.setValue(x,yFound,i,false);
					count++;
				}
			}
					
		}
		return count;
	}

	/***********************************************************************
	* singleInRow()
	* 
	* Value Updates - Single In Row
	* 
	* For every row
	* 	For Every cell in that row
	* 		If only one cell in this row has a specific possibility, it must be that value. Set it to that value
	*  		 Return number of values added.
	***********************************************************************/
	int singleInRow()
	{
		int count = 0;

		int xFound;

		for (int i = 1; i <= 9; i++)				// for every possibility 1-9
		{
			for (int y = 1; y <= 9; y++)				// For every row
			{
				xFound = 0;
				for (int x = 1; x <= 9; x++)			// for every cell in this row x 1-9, check if only one cell with this possibility
				{
					if (gameState.getValue(x,y) == i)	// If a cell in this row is already set to this value, no other cell can be so forget it
					{
						xFound = 0;
						break;
					}	
					if (gameState.getPossibility(x,y,i))	// If this cell has this possibility
					{

						// We have found this possibility in this cell
						if (xFound == 0)				// If this the first find, set found and record the coordinate
							xFound = x;
						else							// If this the second find, clear found and stop checking this row
						{
							xFound = 0;
							break;
						}
					}
				}
				if (xFound > 0)				// If we only found one cell with this possibility, that cell must have that value
				{
					gameState.setValue(xFound,y,i,false);
					count++;
				}
			}
					
		}
		return count;
	}

	/***********************************************************************
	* singleIn3x3()
	* 
	* Value Updates - Single In 3x3
	* 
	* For every 3x3
	* 	For Every cell in that 3x3
	* 		If only one cell in this 3x3 has a specific possibility, it must be that value. Set it to that value
	*  		 Return number of values added.
	***********************************************************************/
	int singleIn3x3()
	{
		int count = 0;
		int xFound;
		int yFound;


		for (int i = 1; i <= 9; i++)				// for every possibility 1-9
		{
			for (int y1 = 1; y1 <= 9; y1+=3)					// For every 3x3 box (top left coord)
			{
				for (int x1 = 1; x1 <= 9; x1+=3)
				{
					xFound = 0;
					yFound = 0;
					loopWithin3x3:
					for (int y = y1; y < y1+3; y++)				// For every cell in this 3x3, check if only one cell with this possibility
					{
						for (int x = x1; x < x1+3; x++)
						{
							if (gameState.getValue(x,y) == i)	// If a cell in this 3x3 is already set to this value, no other cell can be so forget it
							{
								xFound = 0;
								break loopWithin3x3;
							}	
							if (gameState.getPossibility(x,y,i))	// If this cell has this possibility
							{
								// We have found this possibility in this cell
								if (xFound == 0)				// If this the first find, set found and record the coordinate
								{
									xFound = x;
									yFound = y;
								}
								else							// If this the second find, clear found and stop checking this row
								{
									xFound = 0;
									break loopWithin3x3;
								}
							}

						}
					}
					if (xFound > 0)				// If we only found one cell with this possibility, that cell must have that value
					{
						gameState.setValue(xFound,yFound,i,false);
						count++;
					}
				}
			}
		}
		return count;
	}

	/***********************************************************************
	* nSetsRow()
	* 
	* Possibility Updates -  n sets of n, by row
	* 
	* For every row
	* 	If n cells in that row have the same n possibilities, then no other cell in that row can be any of those possibilities. Remove them
	*  		Return number of possibilities removed.
	***********************************************************************/
	int nSetsRow(int nRange)
	{
		int total = 0;
		int[][] cellList = new int[9][3];

		// Get coordinates of every cell in row
		for (int y = 1; y <= 9; y++)				// For every row
		{
			int index = 0;
			for (int x = 1; x <= 9; x++)			// for every cell in this row
			{
				if (gameState.getValue(x,y) == 0)	// If cell does not have a value, it is eligible for inclusion - log coordinates
				{
					cellList[index][0] = x;			//x
					cellList[index][1] = y;			//y
					cellList[index++][2] = 0;		//setting (0 = not in set, 1 = in set)
				}
			}
			for (int i = index; i < 9; i++)
			{
				Arrays.fill(cellList[i],0);			// Empty the remainder of the coordinates array
			}

			if (index >= 2)							// Process if at least 2 cells found in this row
				total += nSetsProcessing(cellList, index, nRange);
		}
		return total;
	}

	/***********************************************************************
	* nSetsColumn()
	* 
	* Possibility Updates -  n sets of n, by column
	* 
	* For every column
	* 	If n cells in that column have the same n possibilities, then no other cell in that column can be any of those possibilities. Remove them
	*  		Return number of possibilities removed.
	***********************************************************************/
	int nSetsColumn(int nRange)
	{
		int total = 0;
		int[][] cellList = new int[9][3];

		// Get coordinates of every cell in column
		for (int x = 1; x <= 9; x++)				// For every column
		{
			int index = 0;
			for (int y = 1; y <= 9; y++)			// for every cell in this column
			{
				if (gameState.getValue(x,y) == 0)	// If cell does not have a value, it is eligible for inclusion - log coordinates
				{
					cellList[index][0] = x;			//x
					cellList[index][1] = y;			//y
					cellList[index++][2] = 0;		//setting (0 = not in set, 1 = in set)
				}
			}
			for (int i = index; i < 9; i++)
			{
				Arrays.fill(cellList[i],0);			// Empty the remainder of the coordinates array
			}

			if (index >= 2)							// Process if at least 2 cells found in this column
				total += nSetsProcessing(cellList, index, nRange);
		}
		return total;
	}

	/***********************************************************************
	* nSets3x3()
	* 
	* Possibility Updates -  n sets of n, by 3x3
	* 
	* For every 3x3
	* 	If n cells in that 3x3 have the same n possibilities, then no other cell in that 3x3 can be any of those possibilities. Remove them
	*  		Return number of possibilities removed.
	***********************************************************************/
	int nSets3x3(int nRange)
	{
		int total = 0;
		int[][] cellList = new int[9][3];


		// Get coordinates of every cell in row
		for (int y1 = 1; y1 <= 9; y1+=3)					// For every 3x3 box (top left coord)
		{
			for (int x1 = 1; x1 <= 9; x1+=3)
			{
				int index = 0;
				for (int y = y1; y < y1+3; y++)				// For every cell in this 3x3
				{
					for (int x = x1; x < x1+3; x++)
					{

						if (gameState.getValue(x,y) == 0)	// If cell does not have a value, it is eligible for inclusion - log coordinates
						{
							cellList[index][0] = x;			//x
							cellList[index][1] = y;			//y
							cellList[index++][2] = 0;		//setting (0 = not in set, 1 = in set)
						}

					}
				}
				for (int i = index; i < 9; i++)
				{
					Arrays.fill(cellList[i],0);			// Empty the remainder of the coordinates array
				}

				if (index >= 2)							// Process if at least 2 cells found in this row
					total += nSetsProcessing(cellList, index, nRange);
			}
		}

		return total;
	}

	/***********************************************************************
	* nSetsProcessing()
	* 
	* Possibility Updates -  n sets of n, processing
	* 
	* called with a list of cells that make up the current row/col/3x3
	* For each set size (2-7, or specified set size), recursively calculate if an n set of n exists. If so, process the results
	***********************************************************************/
	int nSetsProcessing(int[][] cellList, int index, int nRange)
	{
		int total = 0;
		int[][] foundCellList = new int[9][3];
		boolean[] possibilities = new boolean[9];
		boolean[] finalPossibilities = new boolean[9];

		// Clear out empty cells in array
		for(int i = index; i < 9; i++)
		{
			cellList[i][0] = cellList[i][1] = 0;
		}


		// For each value of n (2 to 7, not exceeding the number of cells
		// minimum 2, set of 1 makes no sense
		// max of number of cells -1
			//(-1 means the last cell must be sole possibility, so should have been picked up by value by row/col/3x3)
		for (int n = 2; n <= 7 && n < index-1; n++)
		{
			if (nRange == 0 || nRange == n)		// If the nRange setting allows this n number
			{

			// Clear out arrays for this iteration
				for (int i = 0; i < 9; i++)
				{
					cellList[i][2] = 0;					//setting (-1 = do not process, 0 = not in set, 1 = in set)
				}
				Arrays.fill(possibilities,false);
				Arrays.fill(finalPossibilities,false);

				total += nSetsRecursion(9, 0, n, index-1, cellList, 0, possibilities, 0);


			}
		}


		return total;
	}

	/***********************************************************************
	* nSetsProcessResults()
	* 
	* Possibility Updates -  n sets of n, processing
	* 
	* n set of n was found, remove the possibilties for every cell not in the set (cellList where [i][2] = 0)
	* Return number of possibilities removed
	***********************************************************************/
	int nSetsProcessResults(int[][] cellList, boolean[] possibilities)
	{
		int total = 0;
		boolean cellPossibility;
		//It was successful.
		//cellList contains every cell, with [i][2] = 1 if part of set
		//possibilities contains the possibilities int he set

		// Every cell outside of the set (cellList[i][2] = 0) can not be any of those possibilities

		for (int i = 0; i < cellList.length && cellList[i][0] != 0; i++)
		{
			if (cellList[i][2] == 0)
			{
				for (int j = 1; j <= possibilities.length; j++)
				{
					if (possibilities[j-1] == true)
					{
						cellPossibility = gameState.getPossibility(cellList[i][0],cellList[i][1],j);
						if (cellPossibility == true)
						{
							gameState.clearPossibility(cellList[i][0],cellList[i][1],j);
							total += 1;
						}
					}
				}
			}
		}
		return total;
	}

//cell		this cell (index of cellList) INCREMENT WITH EVERY CALL
//n		the number of cells to include in the set CONSTANT
//maxi	the max number of cells that can be checked (size of cellList)
// cellList
	/***********************************************************************
	* nSetsRecursion()
	* 
	* Possibility Updates -  n sets of n, processing
	* 
	* Recursive scn through list of cells to try every possible combination, to see if an n set of n can be found
	* If found, return true and set:
	* 	foundCellList contains all cells in row. Set flag on those cells included
	***********************************************************************/
	int nSetsRecursion(int id, int cell, int n, int maxCells, int[][] cellList, int setCount, boolean[] possibilities, int possibilitiesCount)
	{
		int total = 0;
		boolean cellAdded;
		boolean cellPossibility;
		int newPossibilitiesCount = possibilitiesCount;
		boolean[] newPossibilities = new boolean[9];
		System.arraycopy(possibilities,0,newPossibilities,0,9);
		id*=10;

		//************************
		// Add this cell to the set

		// Mark cell as added to the set
		cellAdded = true;
		cellList[cell][2] = 1;
		setCount++;

		// Add to the possibilites list/count
		for (int i = 1; i <= 9; i++)
		{
			cellPossibility = gameState.getPossibility(cellList[cell][0],cellList[cell][1],i);
			if (cellPossibility == true && newPossibilities[i-1] == false)
			{
				newPossibilities[i-1] = true;
				newPossibilitiesCount++;
			}
		}
		//************************
		// Are we at max depth? We have n sets?
		if (setCount == n)
		{
			// Do the n cells have n possibilties? We are at n sets of n?
			if (newPossibilitiesCount == n)
			{
				total += nSetsProcessResults(cellList, newPossibilities);		// Perform the possibility updates for this set, add them to the total
			}

			// Max depth means we cannot add another cell to this set. Remove this cell from the set
			cellAdded = false;
			cellList[cell][2] = 0;
			setCount--;

		}


		//************************
		// Iterate down, add other cells to the set

		// If there are more cells available
		if (cell < maxCells)
		{
			// If this cell is in the set, try with this cell included in the set. Then remove it from the set so we can try without this cell
			if (cellAdded)
			{
				total += nSetsRecursion(id+1,cell+1, n, maxCells, cellList, setCount, newPossibilities, newPossibilitiesCount);
				cellAdded = false;
				cellList[cell][2] = 0;
				setCount--;
			}

			// Try with this cell included in thi set
			total += nSetsRecursion(id,cell+1, n, maxCells, cellList, setCount, possibilities, possibilitiesCount);
		}

		//Return total possibiltiies removed by this iteration and any subsequent iterations
		return total;

	}

	/***********************************************************************
	* rowIn3x3()
	* 
	* Possibility Updates -  Remove by Row in 3x3
	* 
	* For every row
	* If 3 or less cells in a row can be value n and they are in the same 3x3, then no other cell in that 3x3 ouside the row can be n. Remove it
	*  		Return number of possibilities removed.
	***********************************************************************/
	int rowIn3x3()
	{
		int total = 0;
		int x3Start;
		int y3Start;

		for (int i = 1; i < 9; i++)			// For each possibility
		{
			for (int y = 1; y <= 9; y++)		// For each row
			{
				y3Start = (((y-1)/3)*3)+1;		// Calculate Y coordinate of the 3x3s it intersects
				x3Start = 0;

				for (int x = 1; x <= 9; x++)		// For each cell in this row
				{
					if (gameState.getPossibility(x,y,i))		// If it can be this possibility
					{
						if (x3Start == 0)						// If this is the first matching cell, record the 3x3 box x coord. Wth y coord of row we now have both
							x3Start = (((x-1)/3)*3)+1;
						else if (x3Start != (((x-1)/3)*3)+1)	// If this is the second matching cell, is it in the same 3x3? If not, stop here and set no matches
						{
							x3Start = 0;
							break;
						}
					}
				}

				if (x3Start != 0)								// If all cells in the row that could be possibility i were in the same 3x3, x3Start will not be zero
				{
					// Remove possibiltiy i for all cells in this 3x3 that are otside this row
					for (int y1 = y3Start; y1 < y3Start + 3; y1++)			// For every cell in this 3x3 box 
					{
						for (int x1 = x3Start; x1 < x3Start + 3; x1++)
						{
							if (y1 != y)											// Excluding the row in question
							{
								if (gameState.getPossibility(x1,y1,i))				// Remove this possibilty if set
								{
									gameState.clearPossibility(x1,y1,i);
									total ++;
								}
							}
						}
					}
				}
			}
		}

		return total;
	}

	/***********************************************************************
	* columnIn3x3()
	* 
	* Possibility Updates -  Remove by Column in 3x3
	* 
	* For every column
	* If 3 or less cells in a column can be value n and they are in the same 3x3, then no other cell in that 3x3 ouside the column can be n. Remove it
	*  		Return number of possibilities removed.
	***********************************************************************/
	int columnIn3x3()
	{
		int total = 0;
		int x3Start;
		int y3Start;

		for (int i = 1; i < 9; i++)			// For each possibility
		{
			for (int x = 1; x <= 9; x++)		// For each column
			{
				x3Start = (((x-1)/3)*3)+1;		// Calculate c coordinate of the 3x3s it intersects
				y3Start = 0;

				for (int y = 1; y <= 9; y++)		// For each cell in this column
				{
					if (gameState.getPossibility(x,y,i))		// If it can be this possibility
					{
						if (y3Start == 0)						// If this is the first matching cell, record the 3x3 box y coord. Wth c coord of row we now have both
							y3Start = (((y-1)/3)*3)+1;
						else if (y3Start != (((y-1)/3)*3)+1)	// If this is the second matching cell, is it in the same 3x3? If not, stop here and set no matches
						{
							y3Start = 0;
							break;
						}
					}
				}

				if (y3Start != 0)								// If all cells in the column that could be possibility i were in the same 3x3, x3Start will not be zero
				{
					// Remove possibiltiy i for all cells in this 3x3 that are otside this column
					for (int y1 = y3Start; y1 < y3Start + 3; y1++)			// For every cell in this 3x3 box 
					{
						for (int x1 = x3Start; x1 < x3Start + 3; x1++)
						{
							if (x1 != x)											// Excluding the column in question
							{
								if (gameState.getPossibility(x1,y1,i))				// Remove this possibilty if set
								{
									gameState.clearPossibility(x1,y1,i);
									total ++;
								}
							}
						}
					}
				}
			}
		}

		return total;
	}

	/***********************************************************************
	* x3InRow()
	* 
	* Possibility Updates -  Remove by 3x3 In Row
	* 
	* For every 3x3
	* If 3 or less cells in a 3x3 can be value n and they are in the same row, then no other cell in that row ouside the 3x3 can be n. Remove it
	*  		Return number of possibilities removed.
	***********************************************************************/
	int x3InRow()
	{
		int total = 0;
		int coordY;

		for (int i = 1; i < 9; i++)			// For each possibility
		{

			for (int y1 = 1; y1 <= 9; y1+=3)					// For every 3x3 box (top left coord)
			{
				for (int x1 = 1; x1 <= 9; x1+=3)
				{
					coordY = 0;
					loopWithin3x3:
					for (int y = y1; y < y1+3; y++)				// For every cell in this 3x3
					{
						for (int x = x1; x < x1+3; x++)
						{
							if (gameState.getPossibility(x,y,i))		// If it can be this possibility
							{
								if (coordY == 0)						// If this is the first matching cell, record the row
									coordY = y;
								else if (coordY != y)					// If this is the second matching cell, is it in the same row? If not, stop here and set no matches
								{
									coordY = 0;
									break loopWithin3x3;
								}
							}
						}
					}

					if (coordY != 0)								// If all cells in the 3x3 that could be possibility i were in the same row, coordY will not be zero
					{
						// Remove possibiltiy i for all cells in this row that are otside this 3x3
						for (int x = 1; x <= 9; x++)				// For every cell in this row
						{
							if (x < x1 || x >= x1+3)				// Excluding those that intersect the 3x3 in question
							{
								if (gameState.getPossibility(x,coordY,i))				// Remove this possibilty if set
								{
									gameState.clearPossibility(x,coordY,i);
									total ++;
								}
							}
						}
					}
				}
			}
		}
		return total;
	}

	/***********************************************************************
	* x3InColumn()
	* 
	* Possibility Updates -  Remove by 3x3 In Column
	* 
	* For every 3x3
	* If 3 or less cells in a 3x3 can be value n and they are in the same column, then no other cell in that column ouside the 3x3 can be n. Remove it
	*  		Return number of possibilities removed.
	***********************************************************************/
	int x3InColumn()
	{
		int total = 0;
		int coordX;

		for (int i = 1; i < 9; i++)			// For each possibility
		{

			for (int y1 = 1; y1 <= 9; y1+=3)					// For every 3x3 box (top left coord)
			{
				for (int x1 = 1; x1 <= 9; x1+=3)
				{
					coordX = 0;
					loopWithin3x3:
					for (int y = y1; y < y1+3; y++)				// For every cell in this 3x3
					{
						for (int x = x1; x < x1+3; x++)
						{
							if (gameState.getPossibility(x,y,i))		// If it can be this possibility
							{
								if (coordX == 0)						// If this is the first matching cell, record the column
									coordX = x;
								else if (coordX != x)					// If this is the second matching cell, is it in the same column? If not, stop here and set no matches
								{
									coordX = 0;
									break loopWithin3x3;
								}
							}
						}
					}

					if (coordX != 0)								// If all cells in the 3x3 that could be possibility i were in the same column, coordY will not be zero
					{
						// Remove possibiltiy i for all cells in this column that are otside this 3x3
						for (int y = 1; y <= 9; y++)				// For every cell in this column
						{
							if (y < y1 || y >= y1+3)				// Excluding those that intersect the 3x3 in question
							{
								if (gameState.getPossibility(coordX,y,i))				// Remove this possibilty if set
								{
									gameState.clearPossibility(coordX,y,i);
									total ++;
								}
							}
						}
					}
				}
			}
		}
		return total;
	}

	/***********************************************************************
	* solveRecursive()
	* 
	* Solve gameState - recursive
	* 
	* Recurse thourgh all possible combinations until a valid solution is found
	***********************************************************************/
	boolean solveRecursive()
	{
		int cellList[][] = new int[81][2];
		int maxID = 0;

		updatePossibilities();			//Ensure possibility grid is trimmed

		// Build 2d array list of cells with no value, (where [i][0] is x and [i][1] is y), for easlier looping by recursive function
		for (int y = 1; y <= 9; y++)	// For each cell in this column
		{
			for (int x = 1; x <= 9; x++)		// For each column
			{
				if (gameState.getValue(x,y) == 0)
				{
					cellList[maxID][0] = x;
					cellList[maxID++][1] = y;
				}
			}
		}

		if (maxID != 0 && solveRecursive(0, maxID-1,cellList, gameState))
		{
			System.out.println("Success!");
			System.out.println(recursiveSolvedState.valueStateToString());			
			gameState.copyFrom(recursiveSolvedState);
			return true;
		}
		return false;
	}
	boolean solveRecursive(int cellID, int maxID, int cellList[][], SudokuGameState currentState)
	{
		//Duplicate the GameState for this iteration
		SudokuGameState newState;

		// Try every available possibility for this cell
		for (int i = 1; i <= 9; i++)
		{
			if (currentState.getPossibility(cellList[cellID][0],cellList[cellID][1],i))
			{
				newState = currentState.cloneState();									// Start with a fresh gameState
				newState.setValue(cellList[cellID][0],cellList[cellID][1],i,false);		// Set this cell value
				updatePossibilities(newState,cellList[cellID][0],cellList[cellID][1]);	// Update surrounding possibilities accordingly (reduces number of future possibles so massively reduces iterations required)

				// If this is the last cell, validate the grid, Return true if valid
				if (cellID == maxID)
				{
					if (newState.validate())		// is it valid? Return accordingly
					{
						recursiveSolvedState = newState;
						return true;
					}
				}

				// This is not the last cell. Recursively try the next cell ,Return true if successful
				else
				{
					if (solveRecursive(cellID+1, maxID, cellList, newState))
						return true;
				}

			}
		}
		return false;
	}
}
