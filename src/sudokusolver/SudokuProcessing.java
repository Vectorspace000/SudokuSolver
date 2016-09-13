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


/***********************************************************************
* class SudokuProcessing
* 
* Builds and displays a UI for solving Sudoku puzzles
* Class SudokuGrid generates and updates the Sudoku Game grid
* Class SudokuProcessing actions user operations on the game:
* 	Load user input from grid
* 	clear grid
* 	Apply solutions
* 	Highlight cells
***********************************************************************/
public class SudokuProcessing
{
	private SudokuGrid sudokuGrid;
	private LogTextArea log;
	private SudokuGameState gameState = new SudokuGameState();
	private SudokuSolutions solutions;

	private int[][] previousGrid = new int[9][9];

	// Default Sudoku puzzle for Load Previous. Array usage is transposed [x][y] instead of [y][x] so this array is transposed when loaded
	private final int[][] defaultGridTransposed = {
			{0,0,4,7,0,0,0,0,0},
			{0,0,3,0,8,0,6,0,4},
			{6,0,0,0,3,0,0,0,0},
			{2,5,0,1,9,0,0,4,0},
			{3,0,0,0,6,0,0,0,9},
			{0,6,0,0,7,3,0,5,1},
			{0,0,0,0,5,0,0,0,7},
			{5,0,8,0,1,0,2,0,0},
			{0,0,0,0,0,9,5,0,0}
		};

	public static final String[][] LOG_TEXT = {
			{"Single Possibility         +","Y"},
			{"Single In Column           +","Y"},
			{"Single In Row              +","Y"},
			{"Single In 3x3              +","Y"},
			{"Remove by Value            -","N"},
			{"Remove by Row In 3x3       -","N"},
			{"Remove by Column In 3x3    -","N"},
			{"Remove by 3x3 In Row       -","N"},
			{"Remove by 3x3 In Column    -","N"},
			{"n Sets of n By Row         -","N"},
			{"n Sets of n By Column      -","N"},
			{"n Sets of n By 3x3         -","N"}
		};

	public static final int S_SINGLE_P		= 0;
	public static final int S_SINGLE_C		= 1;
	public static final int S_SINGLE_R		= 2;
	public static final int S_SINGLE_3		= 3;
	public static final int S_POSS_VAL		= 4;
	public static final int S_POSS_ROW_3	= 5;
	public static final int S_POSS_COL_3	= 6;
	public static final int S_POSS_3_ROW	= 7;
	public static final int S_POSS_3_COL	= 8;
	public static final int S_POSS_N_ROW	= 9;
	public static final int S_POSS_N_COL	= 10;
	public static final int S_POSS_N_3X3	= 11;

	/***********************************************************************
	* SudokuProcessing()
	* 
	* Constructor. Store reference to game grid UI and import default previous puzzle.
	* 
	***********************************************************************/
	SudokuProcessing(SudokuGrid sudokuGrid, LogTextArea log)
	{
		this.sudokuGrid = sudokuGrid;
		this.log = log;
		solutions = new SudokuSolutions(gameState,log);
		for (int x = 0; x <9; x++)
		{
			for (int y = 0; y <9; y++)
			{
				previousGrid[x][y] = defaultGridTransposed[y][x];
			}
		}
	}

	/***********************************************************************
	* clearGame()
	* 
	* Clear game grid UI and gamestate, set game grid UI to input mode
	***********************************************************************/
	void clearGame()
	{
		sudokuGrid.clear();
		gameState.clear();
	}

	/***********************************************************************
	* importFromGrid()
	* 
	* Initial load of gamestate from user input. Update previous puzzle at the same time
	***********************************************************************/
	// Update gameState with input values then redisplay
	void importFromGrid()
	{
		for (int y = 1; y <= 9; y++)
		{
			for (int x = 1; x <= 9; x++)
			{
				previousGrid[x-1][y-1] = sudokuGrid.getInputValue(x,y);
				if (sudokuGrid.getInputValue(x,y) > 0)
					gameState.setValue(x,y,sudokuGrid.getInputValue(x,y),true);
			}
		}
		updateGrid(true);
		sudokuGrid.getPanel().revalidate();
		sudokuGrid.getPanel().repaint();
	}

	/***********************************************************************
	* importFromPrevious()
	* 
	* Initial load of gamestate from previous puzzle.
	***********************************************************************/
	void importFromPrevious()
	{
		for (int y = 1; y <= 9; y++)
		{
			for (int x = 1; x <= 9; x++)
			{
				if (previousGrid[x-1][y-1] > 0)
					gameState.setValue(x,y,previousGrid[x-1][y-1],true);
			}
		}
		updateGrid(true);
		sudokuGrid.getPanel().revalidate();
		sudokuGrid.getPanel().repaint();
	}

	/***********************************************************************
	* updateGrid()
	* 
	* Update game grid from current gamestate.
	* If initialise = true, load all values as INITIAL format
	* Otherwise, load.format changed values
	***********************************************************************/
	// Update sudoku grid from current gameState
	void updateGrid(boolean initialise)
	{
		for (int x = 1; x <= 9; x++)
		{
			for (int y = 1; y <= 9; y++)
			{
				updateGrid(x, y, initialise);
			}
		}
		sudokuGrid.getPanel().revalidate();
		sudokuGrid.getPanel().repaint();
	}
	void updateGrid(int x, int y, boolean initialise)
	{

		// Set initial values, value with gray background or possibility grid
		if (initialise)
		{
			if (gameState.isInitialValue(x,y))
			{
				sudokuGrid.setValue(x,y,gameState.getValue(x,y));
				sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_INITIAL);
				sudokuGrid.setValueMode(x,y);
			}
			else
				sudokuGrid.setPossibilityMode(x,y);

			// Initialise this cell's possibility grid to its initial gameState values
			for (int i = 1; i <= 9; i++)
			{
				if (gameState.getPossibility(x,y,i) == false)		// If false, set to blank
					sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_BLANK,i);
			}
		}	

		// Update from gamestate
		else
		{
			// Value Changed this iteration, update and highlight as just changed
			if (gameState.valueChanged(x,y))
			{
				sudokuGrid.setValue(x,y,gameState.getValue(x,y));		// Update stored value, display as value
				sudokuGrid.setValueMode(x,y);
				if (gameState.getValue(x,y) == 0)
					sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_REMOVED_TRANSPARENT);	// new value is zero, format as change-removed
				else
					sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_CHANGED_TRANSPARENT);	// Else format as changed
			}
			// Value Changed previous iteration, format as appropriate for current state
			else if (gameState.prevValueChanged(x,y))
			{
				if (gameState.getValue(x,y) == 0)
				{
					sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_BLANK);		// Value is zero, format as blank and revert to possibility mode
					sudokuGrid.setPossibilityMode(x,y);
				}
				else
					sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_SET);		// Else format as normal
			}

			// Update possibilities
			for (int i = 1; i <= 9; i++)
			{
				// Possibilities changed this iteration, update and highlight as just changed
				if (gameState.possibilityChanged(x,y,i))
				{
					if (gameState.getPossibility(x,y,i))			// If new possibility is true
						sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_CHANGED,i);		// new possibility is true, set changed
					else
						sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_REMOVED,i);		// Else, set change-removed
				}
				// Possibilities changed previous iteration, format as appropriate for current state
				else if (gameState.prevPossibilityChanged(x,y,i))
				{
					if (gameState.getPossibility(x,y,i))			// If new possibility is true
						sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_SET,i);			// new possibility is true, fornat as normal
					else
						sudokuGrid.setFormat(x,y,SudokuGrid.FORMAT_BLANK,i);		// Else format as blank
				}
			}
		}

		// Refresh the display for this cell
		sudokuGrid.refreshFormat(x,y);
		for (int i = 1; i <= 9; i++)
		{
			sudokuGrid.refreshFormat(x,y,i);
		}

	}

	/***********************************************************************
	* solve()
	* 
	* Instruct SudokSolutions class to attempt to solve puzzle using all methods
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	* Update twice to remove last changed markers from last iteration
	***********************************************************************/
	void solve()
	{
		int updatesThisIter = 0;
		int maxIter = 50;


		System.out.println("Iteration 0");
		log.addLine("\nSolve Puzzle.\nIteration 0");
		updatePossibilities(false);

		for (int i = 1; i <= maxIter; i++)
		{
			System.out.println("\nIteration " + i);
			log.addLine("\nIteration " + i);

			//*****************************
			// Stanard solutions

			// Value Updates
			updatesThisIter = singlePossibility(false);
			if (gameState.getRemainingValues() == 0)
				break;
			updatesThisIter += singleInRow(false);
			if (gameState.getRemainingValues() == 0)
				break;
			updatesThisIter += singleInColumn(false);
			if (gameState.getRemainingValues() == 0)
				break;
			updatesThisIter += singleIn3x3(false);
			if (gameState.getRemainingValues() == 0)
				break;

			//*****************************
			// Possibility Updates

			updatesThisIter += updatePossibilities(false);
			updatesThisIter += rowIn3x3(false);
			updatesThisIter += columnIn3x3(false);
			updatesThisIter += x3InColumn(false);
			updatesThisIter += x3InRow(false);

			//*****************************
			// Recursive solutions
		//	if (updatesThisIter == 0)
			{
				updatesThisIter += nSetsRow(0,false);
				updatesThisIter += nSetsColumn(0,false); 
				updatesThisIter += nSets3x3(0,false);
			}

			System.out.println("Updates: " + updatesThisIter);
			log.addLine("Updates: " + updatesThisIter);
			if (updatesThisIter == 0)
				break;
		}

		updateGrid(false);
		gameState.cycleIteration();
		updateGrid(false);

		// Results
		if (gameState.getRemainingValues() == 0)
		{
			System.out.println("Success");
			log.addLine("Success");
		}
		else if (updatesThisIter == 0)
		{
			System.out.println("Failed - no updates this iteration.");
			log.addLine("Failed - no updates this iteration.");
		}
		else
		{
			System.out.println("Failed - max iterations reached.");
			log.addLine("Failed - max iterations reached.");
		}

	}



	void solveRecursive()
	{
		if(solutions.solveRecursive())
		{
			System.out.println("\nRecursive solution successful");
			log.addLine("Recursive solution successful");
		}
		else
		{
			System.out.println("\nRecursive solution failed");
			log.addLine("Recursive solution failed");
		}
		updateGrid(false);
		gameState.cycleIteration();
		updateGrid(false);
	}

	/***********************************************************************
	* updatePossibilities()
	* 
	* Instruct SudokcSolutions class to update the possibilities of all cells in gamestate based on current values
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int updatePossibilities(boolean update)
	{
		int i = solutions.updatePossibilities();
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("UP Possibilties removed: " + i);
		logResult(S_POSS_VAL,i);
		return i;
	}

	/***********************************************************************
	* singlePossibility()
	* 
	* Instruct SudokcSolutions class to apply Single Possibilities solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int singlePossibility(boolean update)
	{
		int i = solutions.singlePossibility();
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("SP Values set: " + i);
		logResult(S_SINGLE_P,i);
		return i;
	}

	/***********************************************************************
	* singleInColumn()
	* 
	* Instruct SudokcSolutions class to apply Single In Column solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int singleInColumn(boolean update)
	{
		int i = solutions.singleInColumn();
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("SIC Values set: " + i);
		logResult(S_SINGLE_C,i);
		return i;
	}

	/***********************************************************************
	* singleInRow()
	* 
	* Instruct SudokcSolutions class to apply Single In Row solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int singleInRow(boolean update)
	{
		int i = solutions.singleInRow();
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("SIR Values set: " + i);
		logResult(S_SINGLE_R,i);
		return i;
	}

	/***********************************************************************
	* singleIn3x3()
	* 
	* Instruct SudokcSolutions class to apply Single In 3x3 solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int singleIn3x3(boolean update)
	{
		int i = solutions.singleIn3x3();
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("SI3 Values set: " + i);
		logResult(S_SINGLE_3,i);
		return i;
	}

	/***********************************************************************
	* nSetsRow()
	* 
	* Instruct SudokuSolutions class to apply n Sets of n By Row solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int nSetsRow(int nRange, boolean update)
	{
		int i = solutions.nSetsRow(nRange);
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("NSR Possibilities removed: " + i);
		logResult(S_POSS_N_ROW,i);
		return i;
	}

	/***********************************************************************
	* nSetsColumn()
	* 
	* Instruct SudokuSolutions class to apply n Sets of n By Row solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int nSetsColumn(int nRange, boolean update)
	{
		int i = solutions.nSetsColumn(nRange);
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("NSC Possibilities removed: " + i);
		logResult(S_POSS_N_COL,i);
		return i;
	}

	/***********************************************************************
	* nSets3x3()
	* 
	* Instruct SudokuSolutions class to apply n Sets of n By Row solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int nSets3x3(int nRange, boolean update)
	{
		int i = solutions.nSets3x3(nRange);
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("NS3 Possibilities removed: " + i);
		logResult(S_POSS_N_3X3,i);
		return i;
	}

	/***********************************************************************
	* rowIn3x3()
	* 
	* Instruct SudokuSolutions class to apply Remove By Row in 3x3 solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int rowIn3x3(boolean update)
	{
		int i = solutions.rowIn3x3();
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("RI3 Possibilities removed: " + i);
		logResult(S_POSS_ROW_3,i);
		return i;
	}

	/***********************************************************************
	* columnIn3x3()
	* 
	* Instruct SudokuSolutions class to apply Remove By Column in 3x3 solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int columnIn3x3(boolean update)
	{
		int i = solutions.columnIn3x3();
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("CI3 Possibilities removed: " + i);
		logResult(S_POSS_COL_3,i);
		return i;
	}

	/***********************************************************************
	* x3InRow()
	* 
	* Instruct SudokuSolutions class to apply Remove By 3x3 in Row solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int x3InRow(boolean update)
	{
		int i = solutions.x3InRow();
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("3IR Possibilities removed: " + i);
		logResult(S_POSS_3_ROW,i);
		return i;
	}

	/***********************************************************************
	* x3InColumn()
	* 
	* Instruct SudokuSolutions class to apply Remove By 3x3 in Column solution to gamestate
	* Update game grid from updated gamestate
	* Cycle iteration (inform gamestate this iteration is finished, cycle changed flags)
	***********************************************************************/
	int x3InColumn(boolean update)
	{
		int i = solutions.x3InColumn();
		if (update)
		{
			updateGrid(false);
			gameState.cycleIteration();
		}
		System.out.println("3IC Possibilities removed: " + i);
		logResult(S_POSS_3_COL,i);
		return i;
	}

	

	/***********************************************************************
	* logResult()
	* 
	* Log the results of a specific solution
	***********************************************************************/
	void logResult(int id, int value)
	{

		if (id >= 0 && id < LOG_TEXT.length)
		{
			if(LOG_TEXT[id][1].equals("Y"))
				log.addLine(LOG_TEXT[id][0] + value + ". Remaining: " + (gameState.getRemainingValues()));
			else
				log.addLine(LOG_TEXT[id][0] + value);
		}

	}


}
