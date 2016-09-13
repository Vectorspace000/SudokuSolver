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
import javax.swing.* ;
import java.awt.* ;
import java.awt.event.* ;
import javax.swing.text.*;
import java.util.*;
import java.util.regex.*;

/***********************************************************************
* class SudokuGrid
* 
* Creates a JPanel containing a Sudoku game grid.
* Displays either 9x9 JTextField inputs supporting blank or 1-9, or a fixed grid
* Each cell in the fixed grid is either:
*	Single value
*	3x3 grid showing the possible numbers
* Methods for manipulating each 
*	Set value and possibilitiies for each cell
*	Set display format
*		Black on grey - initial (user input) value
*		Black on white - system calculated value
*		Red on white - just changed value
*		Red with strikethrough on white - just removed value
*		White on white - blank (removed or not yet specified) value
*	Can also highlight a number by setting text colours for all other numbers to very light grey.
***********************************************************************/
public class SudokuGrid
{

	public static final int FORMAT_BLANK = 0;
	public static final int FORMAT_SET = 1;
	public static final int FORMAT_INITIAL = 2;
	public static final int FORMAT_CHANGED = 3;
	public static final int FORMAT_REMOVED = 4;
	public static final int FORMAT_CHANGED_TRANSPARENT = 5;
	public static final int FORMAT_REMOVED_TRANSPARENT = 6;
	
	JPanel gridPane;
	CellFormat format = new CellFormat();			// Contains the default font and size for a panel and the whole grid
	GridBagConstraints gridConstraints = new GridBagConstraints();
	ActionListener enterListener;					// Action Listener for pressing Enter in the JTextFields

	static final Color RED_TRANSPARENT = new Color(Color.RED.getRed(),Color.RED.getGreen(),Color.RED.getBlue(),192);
	static final Color WHITE_TRANSPARENT = new Color(Color.WHITE.getRed(),Color.WHITE.getGreen(),Color.WHITE.getBlue(),128);

	boolean highlightEnabled;
	boolean[] highlight = new boolean[9];			//Numbers to highlight (accomplish by greying all other numbers).

	GridCell[][] gridCells = new GridCell[9][9];

	/****************************************
	 * SudokuGrid()
	 * 
	 * Constructor. Create and initialise the grid
	 ***************************************/
	SudokuGrid(ActionListener enterListener)
	{
		this.enterListener = enterListener;
		gridPane = new JPanel(new GridBagLayout()) ;
		// Build 9x9 grid
		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				gridConstraints.gridx = x;
				gridConstraints.gridy = y;
				
				gridCells[x][y] = new GridCell(x, format , x+1, y+1, this);		// Create a 9x9 array of grid cell objects and add to grid panel
				gridPane.add(gridCells[x][y].getPanel(),gridConstraints);
			}
		}

		gridPane.setMinimumSize(format.getGridDimension());
		gridPane.setMaximumSize(format.getGridDimension());
		gridPane.setPreferredSize(format.getGridDimension());	// Set the preferred dimension for the grid (calculated by the first "new GridCell()")

		setInputMode();
	}

	/****************************************
	 * getPanel()
	 * 
	 * Return the game grid JPanel
	 ***************************************/
	JPanel getPanel()
	{
		return gridPane;
	}
		
	/****************************************
	 * getInputValue()
	 * 
	 * Get the user input value for this cell
	 ***************************************/
	int getInputValue(int x, int y)
	{
		return gridCells[x-1][y-1].getInputValue();
	}
		
	/****************************************
	 * getModeValue()
	 * 
	 * Get the user input value or set value, depending on mode for this cell
	 ***************************************/
	int getModeValue(int x, int y)
	{
		return gridCells[x-1][y-1].getModeValue();
	}
		
	/****************************************
	 * setInputValue()
	 * 
	 * Set the user input value for this cell
	 ***************************************/
	void setInputValue(int x, int y, int value)
	{
		gridCells[x-1][y-1].setInputValue(value);
	}
		
	/****************************************
	 * setValue()
	 * 
	 * Set the value for this cell
	 ***************************************/
	void setValue(int x, int y, int value)
	{
		gridCells[x-1][y-1].setValue(value);
	}

	/****************************************
	 * focusCell()
	 * 
	 * Request focus of a specific cell.
	 ***************************************/
	void focusCell(int x, int y)
	{
		gridCells[x-1][y-1].focusCell();
	}

	/****************************************
	 * getCell()
	 * 
	 * Return a GridCell object for the specified cell
	 ***************************************/
	GridCell getCell(int x, int y)
	{
		return gridCells[x-1][y-1];
	}

	/****************************************
	 * clear()
	 * 
	 * Clear the cells (value 0, all possibilities enabled, no highlight)
	 * Focus cell 1.1
	 ***************************************/
	void clear()
	{
		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				gridCells[x][y].clearCellValue();
				gridCells[x][y].clearPossibilities();
				gridCells[x][y].setInputMode();
			}
		}
		clearHighlight();
		// Cause the grid to re-render for the changes made in the gameState update
		gridPane.revalidate();
		gridPane.repaint();

		// Focus cell 1,1 ready for input
		focusCell(1,1);
	}

	/****************************************
	 * setInputMode()
	 * 
	 * Set all cells or specific cell to input mode
	 ***************************************/
	void setInputMode()
	{
		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 9; x++)
			{				
				gridCells[x][y].setInputMode();
			}
		}
	}
	void setInputMode(int x, int y)
	{		
		gridCells[x-1][y-1].setInputMode();
	}

	/****************************************
	 * setValueMode()
	 * 
	 * Set all cells to Value mode
	 ***************************************/
	void setValueMode()
	{
		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 9; x++)
			{				
				gridCells[x][y].setValueMode();
			}
		}
	}
	void setValueMode(int x, int y)
	{		
		gridCells[x-1][y-1].setValueMode();
	}

	/****************************************
	 * setPossibilityMode()
	 * 
	 * Set all cells to input mode
	 ***************************************/
	void setPossibilityMode()
	{
		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 9; x++)
			{				
				gridCells[x][y].setPossibilityMode();
			}
		}
	}
	void setPossibilityMode(int x, int y)
	{		
		gridCells[x-1][y-1].setPossibilityMode();
	}

	/****************************************
	 * clearHighlight()
	 * 
	 * Set the numbers to be highlighted throughout the grid
	 * 
	 ***************************************/
	void clearHighlight()
	{
		highlightEnabled = false;
		Arrays.fill(highlight,false);
	}

	/****************************************
	 * setHighhlight()
	 * 
	 * Set the numbers to be highlighted throughout the grid
	 * 
	 ***************************************/
	void setHighlight(boolean[] highlight)
	{
		int sumSelected = 0;
		for (int i = 0; i < 9; i++)
		{
			this.highlight[i] = highlight[i];
			if (highlight[i])
				sumSelected++;
		}
		// Highlight disabled if all or none selected (highlight none = highlight all = normal display)
		if (sumSelected == 0 || sumSelected == 9)
			highlightEnabled = false;
		else
			highlightEnabled = true;
	}

	/****************************************
	 * setFormat()
	 * 
	 * Set the display format for the specified cell
	 * 
	 ***************************************/
	void setFormat(int x, int y, int formatMode)
	{
		setFormat(x, y, formatMode, 0);
	}
	void setFormat(int x, int y, int formatMode, int value)
	{
		gridCells[x-1][y-1].setFormat(formatMode, value);
	}

	/****************************************
	 * refreshFormats()
	 * 
	 * Refresh the Display Format for this cell
	 * 
	 ***************************************/
	void refreshFormat(int x, int y)
	{
		gridCells[x-1][y-1].refreshFormat();
	}
	void refreshFormat(int x, int y, int value)
	{
		gridCells[x-1][y-1].refreshFormat(value);
	}

	/****************************************
	 * refreshFormats()
	 * 
	 * Refresh the Display Format for all cells
	 * 
	 ***************************************/
	void refreshFormats()
	{
		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 9; x++)
			{				
				gridCells[x][y].refreshFormat();
				for (int i = 1; i <= 9; i++)
				{
					gridCells[x][y].refreshFormat(i);
				}
			}
		}
		// Cause the grid to re-render for the changes made in the gameState update
		gridPane.revalidate();
		gridPane.repaint();
	}

	/***********************************************************************
	* class GridCell
	* 
	* Sub class of SudokuGrid
	* 
	* Creates a JPanel containing an individual Grid Cell
	* This panel is either a JTextField input supporting blank or 1-9, or a fixed panel
	* The fixed panel is either:
	*	Single value
	*	3x3 grid showing the possible numbers
	* Methods for manipulating each 
	*	Set value and possibilities
	*	Set display format
	*		Black on grey - initial (user input) value
	*		Black on white - system calculated value
	*		Red on white - just changed value
	*		Red with strikethrough on white - just removed value
	*		White on white - blank (removed or not yet specified) value
	*	Can ahso highlight a number by setting text colours for all other numbers to very light grey.
	***********************************************************************/
	class GridCell
	{
		SudokuGrid grid;
		JPanel cellValuePanel;
		JPanel cellValueEditablePanel;
		JPanel cellPossibilityPanel;
		JPanel current = new JPanel(new GridLayout(1,1));
		JLayeredPane currentLayers = new JLayeredPane();

		GridCellFilter gridCellFilter = new GridCellFilter(this);
		AbstractDocument cellValueEditableDoc;

		JLabel cellValue;
		JTextField cellValueEditable;
		JLabel[] cellPossibility;
		int cellSetValue = 0;

		CellFormat format;

		int formatMode = SudokuGrid.FORMAT_BLANK;						//0 = blank, 1 = set value, 2 = initial value, 3 = changed, 4 = removed
		int[] possibilityFormatMode = new int[9];			//0 = blank, 1 = set value, 2 = initial value, 3 = changed, 4 = removed

		public static final int DISPLAY_NULL = 0;
		public static final int DISPLAY_INPUT = 1;
		public static final int DISPLAY_VALUE = 2;
		public static final int DISPLAY_POSSIBILITY = 3;
		int displayMode = DISPLAY_NULL;						//0 = unspecified, 1 = input, 2 = value, 3 = possibility


		public final Color VERY_LIGHT_GRAY = new Color(223,223,223);

		int coordX = 0;
		int coordY = 0;
		
		/****************************************
		 * GridCell()
		 * 
		 * Constructor. Import variables then initialise the cell
		 ***************************************/
		GridCell(int type, CellFormat format, int coordX, int coordY, SudokuGrid grid)
		{
			this.grid = grid;
			this.format = format;
			this.coordX = coordX;
			this.coordY = coordY;
			inzCells();					// Initialise value, editable valuue, & 3x3 possibility grid panels for this cell

		}
		
		/****************************************
		 * getPanel()
		 * 
		 * Return the JPanel for this cell
		 ***************************************/
		JPanel getPanel()
		{
			return current;
		}
		
		/****************************************
		 * setValue()
		 * 
		 * Set the value for this cell
		 ***************************************/
		void setValue(int cellSetValue)
		{
			this.cellSetValue = cellSetValue;
		}
		
		/****************************************
		 * getInputValue()
		 * 
		 * Get the user input value for this cell
		 ***************************************/
		int getInputValue()
		{
			String temp = cellValueEditable.getText();
			if (temp != null && temp.length() > 0 && temp.equals(" ") == false)
				return Integer.parseInt(temp);
			return 0;

		}
		
		/****************************************
		 * getModeValue()
		 * 
		 * Get the user input value or set value for this cell, depending on mode
		 ***************************************/
		int getModeValue()
		{
			String temp = cellValueEditable.getText();
			// If input mode, get input value if any
			if (displayMode == DISPLAY_INPUT)
			{
				if(temp != null && temp.length() > 0 && temp.equals(" ") == false)
					return Integer.parseInt(temp);
				return 0;
			}
			return cellSetValue;		// Else return set value

		}
		
		/****************************************
		 * setInputValue()
		 * 
		 * Set the user input value for this cell
		 ***************************************/
		void setInputValue(int value)
		{
			cellValueEditableDoc.setDocumentFilter(null);		// Set editable value to blank (remove listener temporarily so it does not fire during reset)
			if (value == 0)
				cellValueEditable.setText("");
			else
				cellValueEditable.setText(""+value);
			cellValueEditableDoc.setDocumentFilter(gridCellFilter);

		}
		
		/****************************************
		 * inzCells()
		 * 
		 * Create and format the possibility (3x3 grid of labels, each contains 1-9), value, and value editable panels
		 *
		 ***************************************/
		private void inzCells()
		{
			int maxSize = 0;
			int tempSize;
			
			cellPossibilityPanel = new JPanel(new GridLayout(3,3));							// Create the 3x3 grid to hold the 1-9 numbers
			cellPossibilityPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));	// Black border, white background
			cellPossibilityPanel.setBackground(Color.WHITE);

			cellPossibility = new JLabel[9];												// Create each of the 9 labels (1-9) to go in the grid
			for (int i = 0; i <= 8; i++)
			{

				cellPossibility[i] = new JLabel(""+(i+1));									// Create label, non-opaque, centred
				cellPossibility[i].setOpaque(false);
				cellPossibility[i].setFont(format.getPossibilityFont());
				cellPossibility[i].setHorizontalAlignment(SwingConstants.CENTER);

				// Set interior borders only (between the labels in the 3x3)
				if (i == 0 || i == 1 || i == 3 || i == 4)
					cellPossibility[i].setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
				else if (i == 2 || i == 5)
					cellPossibility[i].setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
				else if (i == 6 || i == 7)
					cellPossibility[i].setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
				else
					cellPossibility[i].setBorder(BorderFactory.createEmptyBorder());

				cellPossibilityPanel.add(cellPossibility[i]);								// Add to the panel

				if (format.dimensionSet() == false)											// If preferred dimension not defined yet
				{																				// Find max of all widths and heights of all labels in this cell
					tempSize = Math.max(cellPossibility[i].getPreferredSize().height,cellPossibility[i].getPreferredSize().width);
					if (tempSize > maxSize)
						maxSize = tempSize;

				}
			}
			
			if (format.dimensionSet() == false)				// If preferred dimension not defined yet, calculate it based on the computed max size
				format.setDimension(maxSize);

			cellPossibilityPanel.setPreferredSize(format.getCellDimension());	// Set this panel to the preferred dimension
			setBorder(cellPossibilityPanel);								// Define the borders for this panel base don its x,y coordinates

			clearPossibilities();											// Reset the values and formatting of the possibilities


			cellValue = new JLabel();									// Fixed value text label
			cellValue.setBorder(BorderFactory.createEmptyBorder());
			cellValue.setHorizontalAlignment(SwingConstants.CENTER);
			cellValue.setFont(format.getValueFont());


			cellValuePanel = new JPanel(new GridLayout(1,1));			// Panel for fixed value
			cellValuePanel.setPreferredSize(format.getCellDimension()); 
			cellValuePanel.add(cellValue);
			setBorder(cellValuePanel);

			cellValueEditable = new JTextField();						// Editable value text field
			cellValueEditable.setForeground(Color.BLACK);
			cellValueEditable.setBorder(BorderFactory.createEmptyBorder());
			cellValueEditable.setHorizontalAlignment(SwingConstants.CENTER);
			cellValueEditable.setFont(format.getValueFont());
			if (grid.enterListener != null)
			{
				cellValueEditable.addActionListener(grid.enterListener);
				cellValueEditable.setActionCommand("gridEnter");
			}
				

			cellValueEditableDoc = (AbstractDocument) cellValueEditable.getDocument();
			cellValueEditableDoc.setDocumentFilter(gridCellFilter);						// Filter provides validation to ensure only blank or 1-9 can be entered

			// Set Input/Action maps for arrow key listeners in the editable cells (arrow key shifts focus to adjacent cell)
			cellValueEditable.getInputMap().put(KeyStroke.getKeyStroke("LEFT"),"LEFT");
			cellValueEditable.getActionMap().put("LEFT",new LeftArrowAction(this));
			cellValueEditable.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"),"RIGHT");
			cellValueEditable.getActionMap().put("RIGHT",new RightArrowAction(this));
			cellValueEditable.getInputMap().put(KeyStroke.getKeyStroke("UP"),"UP");
			cellValueEditable.getActionMap().put("UP",new UpArrowAction(this));
			cellValueEditable.getInputMap().put(KeyStroke.getKeyStroke("DOWN"),"DOWN");
			cellValueEditable.getActionMap().put("DOWN",new DownArrowAction(this));

			cellValueEditablePanel = new JPanel(new GridLayout(1,1));			// Panel for editable text field
			cellValueEditablePanel.setBackground(Color.WHITE);
			cellValueEditablePanel.setPreferredSize(format.getCellDimension()); 
			cellValueEditablePanel.add(cellValueEditable);
			setBorder(cellValueEditablePanel);

			clearCellValue();


			// Set up layers for layered panels
			
			cellPossibilityPanel.setBounds(0,0,format.getCellDimension().width,format.getCellDimension().height);
			cellValuePanel.setBounds(0,0,format.getCellDimension().width,format.getCellDimension().height);
			currentLayers.setPreferredSize(format.getCellDimension()); 
			currentLayers.add(cellPossibilityPanel, new Integer[1],1);
			currentLayers.add(cellValuePanel, new Integer[1],0);			// Top

		}

		/****************************************
		 * clearPossibilities()
		 * 
		 * Reset possibility grid for this cell - value to 1-9 only (no HTML formatting) and format as Set
		 ***************************************/
		void clearPossibilities()
		{
			for (int i = 1; i <= 9; i++)
			{
				cellPossibility[i-1].setText(""+i);
				setFormat(SudokuGrid.FORMAT_SET, i);
			}
		}

		/****************************************
		 * focusUp()
		 * 
		 * Focus the cell below this, if not on first row
		 * 
		 ***************************************/
		void focusUp()
		{
			if (coordY > 1)
				grid.focusCell(coordX, coordY-1);
		}

		/****************************************
		 * focusDown()
		 * 
		 * Focus the cell below this, if not on last row
		 * 
		 ***************************************/
		void focusDown()
		{
			if (coordY < 9)
				grid.focusCell(coordX, coordY+1);
		}

		/****************************************
		 * focusRight()
		 * 
		 * Focus the cell to the right of this one
		 * If end of row, go to start of next
		 * If on last cell, shift focus off grid
		 * 
		 ***************************************/
		void focusRight()
		{
			if (coordX < 9)							// Not at end of row, focus next cell
				grid.focusCell(coordX+1, coordY);
			else if (coordY < 9)					// end of not last row, focus start of next row
				grid.focusCell(1, coordY+1);
			else									// At last cell, shift focus to whatever is next (probably load button)
				KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
		}

		/****************************************
		 * focusLeft()
		 * 
		 * Focus the cell to the left of this one
		 * If start of row, go to end of previous
		 * If at 1,1, go to last cell
		 * 
		 ***************************************/
		void focusLeft()
		{
			if (coordX > 1)							// Not at start of row, focus previous cell
				grid.focusCell(coordX-1, coordY);
			else if (coordY > 1)					// start of not first row, focus end of previous row row
				grid.focusCell(9, coordY-1);
			else									// At first cell, shift focus to last cell
				grid.focusCell(9, 9);
		}

		/****************************************
		 * focusCell()
		 * 
		 * Request to focus this editable cell
		 * 
		 ***************************************/
		void focusCell()
		{
			cellValueEditable.requestFocus();
		}

		/****************************************
		 * clearCellValue()
		 * 
		 * Clear this cell's value (both input and display)
		 * and restore default colour
		 * 
		 ***************************************/
		void clearCellValue()
		{
			cellValue.setText("");								// Default display format mode to 0, blank
			cellSetValue = 0;
			setFormat(SudokuGrid.FORMAT_BLANK);						

			cellValueEditableDoc.setDocumentFilter(null);		// Set editable value to blank (remove listener temporarily so it does not fire during reset)
			cellValueEditable.setText("");
			cellValueEditableDoc.setDocumentFilter(gridCellFilter);

		}

		/****************************************
		 * setFormat()
		 * 
		 * Set the display format mode for this cell or possibility
		 * 
		 ***************************************/
		void setFormat(int formatMode)
		{
			setFormat(formatMode,0);
		}
		void setFormat(int formatMode, int value)
		{
			if (value == 0)
			{
				this.formatMode = formatMode;
			}
			else
			{
				this.possibilityFormatMode[value-1] = formatMode;				
			}
			refreshFormat(value);
		}
		
		/****************************************
		 * refreshFormat()
		 * 
		 * Refresh the display format of this cell or possibility
		 * 
		 ***************************************/
		void refreshFormat()
		{
			refreshFormat(0);
		}
		void refreshFormat(int value)
		{
			JLabel textItem;
			JPanel textPanel;
			String textString;
			int textValue;
			int formatMode;
			
			if (value == 0)
			{
				textItem = cellValue;
				textPanel = cellValuePanel;
				formatMode = this.formatMode;
				textValue = cellSetValue;
				if (cellSetValue == 0)
					textString = "";
				else
					textString = Integer.toString(textValue);
			}
			else
			{
				textItem = cellPossibility[value-1];
				textPanel = cellPossibilityPanel;
				formatMode = possibilityFormatMode[value-1];
				textValue = value;
				textString = Integer.toString(value);
			}
			
			switch(formatMode)							// Set the foreground/background colour and text value as specified by format mode
			{
				case SudokuGrid.FORMAT_BLANK:
					textItem.setForeground(Color.WHITE);
					textItem.setText(textString);
					textPanel.setBackground(Color.WHITE);	
					break;
				case SudokuGrid.FORMAT_SET:
					textItem.setForeground(Color.BLACK);
					textItem.setText(textString);
					textPanel.setBackground(Color.WHITE);	
					break;
				case SudokuGrid.FORMAT_INITIAL:
					textItem.setForeground(Color.BLACK);
					textItem.setText(textString);
					textPanel.setBackground(Color.LIGHT_GRAY);
					break;
				case SudokuGrid.FORMAT_CHANGED:
					textItem.setForeground(Color.RED);
					textItem.setText(textString);
					textPanel.setBackground(Color.WHITE);	
					break;
				case SudokuGrid.FORMAT_REMOVED:
					textItem.setForeground(Color.RED);
					textItem.setText("<html><body><span style='text-decoration: line-through;'>"+textString+"</span></body></html>");
					textPanel.setBackground(Color.WHITE);	
					break;
				case SudokuGrid.FORMAT_CHANGED_TRANSPARENT:
					textItem.setForeground(grid.RED_TRANSPARENT);
					textItem.setText(textString);
					textPanel.setBackground(grid.WHITE_TRANSPARENT);	
					break;
				case SudokuGrid.FORMAT_REMOVED_TRANSPARENT:
					textItem.setForeground(grid.RED_TRANSPARENT);
					textItem.setText("<html><body><span style='text-decoration: line-through;'>"+textString+"</span></body></html>");
					textPanel.setBackground(grid.WHITE_TRANSPARENT);	
					break;
			}
			
			// If we are highlighting a number other than this one, grey this one
			if (grid.highlightEnabled && textValue != 0 && grid.highlight[textValue-1] == false && formatMode != SudokuGrid.FORMAT_BLANK)
			{
					textItem.setForeground(VERY_LIGHT_GRAY);
			}
		}

		/****************************************
		 * setBorder()
		 * Compute borders for this Sudoku grid cell panel
		 * 
		 * All cells have bottom and right border:
		 * First/last row/column has double thickness outer border for grtid edge
		 * 
		 * Rows 4 and 7 have top border, columns 4&7 have right border
		 * These combine with the border of the previous cell to make a double thickness border around 3x3 groups
		 * 
		 ***************************************/
		void setBorder(JPanel panel)
		{
			// Set border (top left bottom right). Default is right and bottom only
			int topBorder = 0;
			int leftBorder = 0;
			int bottomBorder = 1;
			int rightBorder = 1;
			
			switch (coordX)
			{
				case 1:								// First column has double thickness left border for left grid edge
					leftBorder = 2;
					break;
				case 9:								// Last column has double thickness right border for right grid edge
					rightBorder = 2;
					break;
				case 4: case 7:						// Columns 4 and 7 have left border (combines with preceeding column right border for double thickness between 3x3 blocks
					leftBorder = 1;
					break;
			}
			
			switch (coordY)
			{
				case 1:								// First row has double thickness top border for left grid edge
					topBorder = 2;
					break;
				case 9:								// Last row has double thickness bottom border for right grid edge
					bottomBorder = 2;
					break;
				case 4: case 7:						// Rows 4 and 7 have top border (combines with preceeding column bottom border for double thickness between 3x3 blocks
					topBorder = 1;
					break;
			}
			
			// Apply computed borders to this cell panel
			panel.setBorder(BorderFactory.createMatteBorder(topBorder, leftBorder, bottomBorder, rightBorder, Color.BLACK));

		}

		/****************************************
		 * setInputMode()
		 * 
		 * Set this cell to user input mode (set cell current panel to the editable value panel)
		 ***************************************/
		void setInputMode()
		{
			cellValue.setText("");
			if (displayMode != DISPLAY_INPUT)
			{
				current.removeAll();
				current.add(cellValueEditablePanel);
				displayMode = DISPLAY_INPUT; //0 = unspecified, 1 = input, 2 = value, 3 = possibility
			}
		}
		
		/****************************************
		 * setPossibilityMode()
		 * 
		 * Set this cell to Possibility mode (set cell current panel to the 3x3 possibility grid panel)
		 ***************************************/
		void setPossibilityMode()
		{
		/*	if (displayMode != DISPLAY_POSSIBILITY)
			{
				current.removeAll();
				current.add(cellPossibilityPanel);
				displayMode = DISPLAY_POSSIBILITY; //0 = unspecified, 1 = input, 2 = value, 3 = possibility
			}*/

			if (displayMode == DISPLAY_INPUT)
			{
				current.removeAll();
				current.add(currentLayers);
			}
			currentLayers.moveToFront(cellPossibilityPanel);
			displayMode = DISPLAY_POSSIBILITY; //0 = unspecified, 1 = input, 2 = value, 3 = possibility
		}
		
		/****************************************
		 * setValueMode()
		 * 
		 * Set this cell to value mode (set cell current panel to the fixed alue panel)
		 ***************************************/
		void setValueMode()
		{
			/*if (displayMode != DISPLAY_VALUE)
			{
				current.removeAll();
				current.add(cellValuePanel);
				displayMode = DISPLAY_VALUE; //0 = unspecified, 1 = input, 2 = value, 3 = possibility
			}*/

			if (displayMode == DISPLAY_INPUT)
			{
				current.removeAll();
				current.add(currentLayers);
			}
			currentLayers.moveToFront(cellValuePanel);
			displayMode = DISPLAY_VALUE; //0 = unspecified, 1 = input, 2 = value, 3 = possibility
		}

		/***********************************************************************
		* class GridCellFilter
		* 
		* Sub class of SudokuGrid
		* 
		* Defines the document filter used by the editable cell JTextField that applies the validation rules:
		* 	Validates on every key input, so always receives one character (or none if deleting or replacing)
		* 	New character always replaced current even if input at end of field
		* 	New character must be zero length, a blank or 1-9
		* 	Field is not upadted if input character is not valid
		* 
		***********************************************************************/
		class GridCellFilter extends DocumentFilter
		{
			private Pattern pattern;
			GridCell cell;

			/****************************************
			 * GridCellFilter()
			 * 
			 * Constructor. Compile Regex pattern (one blank or one of 1-9)
			 * 
			 ***************************************/
			public GridCellFilter(GridCell cell)
			{
				this.cell = cell;
				pattern = Pattern.compile("^ $|^[1-9]$");	// Regex pattern - string is one blank, or is one of 1-9
			}

			/****************************************
			 * insertString()
			 * 
			 * Override of DocumentFilter method
			 * Reroute to replace() but with zero length to replace
			 ***************************************/
			// Insert is a replace with replace length of 0
			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
					throws BadLocationException
			{
				replace(fb, offset, 0, string, attr);
			}

			/****************************************
			 * insertString()
			 * 
			 * Override of DocumentFilter method
			 * Validate regex pattern.
			 * If valid, replace whatever the current value is with the new character and shift focus to next cell
			 ***************************************/
			// Replace
			@Override
			public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr)
					throws BadLocationException
			{
				// Valid only if new string matches regex
				Matcher matcher = pattern.matcher(string);
				if (matcher.matches())												// It matches
				{
					int prevLength = fb.getDocument().getLength();
					if (prevLength > 0)												// Remove whatever was there before
						super.remove(fb, 0, prevLength);
					super.insertString(fb, 0, string.substring(0,1), attr);			// Replace with first character of new string

					cell.focusRight();
					//KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();

				}

			}

			/****************************************
			 * remove()
			 * 
			 * Override of DocumentFilter method
			 * Same as DocumentFilter but then focus next cell
			 ***************************************/
			// New remove - normal remove plus focus next field
			@Override
			public void remove(FilterBypass fb, int offset, int length)
					throws BadLocationException
			{
				super.remove(fb, offset, length);
				cell.focusRight();
				//KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
			}
		}

		/***********************************************************************
		* class LeftArrowAction
		* 
		* Sub class of GridCell
		* 
		* ActionListener for editable JTextField
		* Detects Left Arrow press, and instructs cell to focus the cell to the left
		* 
		***********************************************************************/
		private class LeftArrowAction extends AbstractAction
		{
			GridCell cell;
			LeftArrowAction(GridCell cell)
			{
				this.cell = cell;
			}
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cell.focusLeft();
			}
		}
		/***********************************************************************
		* class RightArrowAction
		* 
		* Sub class of GridCell
		* 
		* ActionListener for editable JTextField
		* Detects Right Arrow press, and instructs cell to focus the cell to the right
		* 
		***********************************************************************/
		private class RightArrowAction extends AbstractAction
		{
			GridCell cell;
			RightArrowAction(GridCell cell)
			{
				this.cell = cell;
			}
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cell.focusRight();
			}
		}
		/***********************************************************************
		* class UpArrowAction
		* 
		* Sub class of GridCell
		* 
		* ActionListener for editable JTextField
		* Detects Up Arrow press, and instructs cell to focus the cell to above
		* 
		***********************************************************************/
		private class UpArrowAction extends AbstractAction
		{
			GridCell cell;
			UpArrowAction(GridCell cell)
			{
				this.cell = cell;
			}
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cell.focusUp();
			}
		}
		/***********************************************************************
		* class DownArrowAction
		* 
		* Sub class of GridCell
		* 
		* ActionListener for editable JTextField
		* Detects Down Arrow press, and instructs cell to focus the cell below
		* 
		***********************************************************************/
		private class DownArrowAction extends AbstractAction
		{
			GridCell cell;
			DownArrowAction(GridCell cell)
			{
				this.cell = cell;
			}
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cell.focusDown();
			}
		}

	}

	/***********************************************************************
	* class CellFormat
	* 
	* Sub class of SudokuGrid
	* 
	* Contains formatting info for grid cells - fonts and panel size
	* Is instantiated by SodokuGrid and passed to all GirdCells
	* First GridCell to be initialised calculates the panel size from its possibility grid subcell sizes
	* Then it and all other cells use that calculated panel size
	* 
	***********************************************************************/
	class CellFormat
	{
		Dimension cellSize;
		Dimension gridSize;
		Font possibilityFont;
		Font valueFont;
		boolean dimensionAvailable = false;
		final float POSSIBILITY_SIZE_MULTIPLIER = 0.75f;		// Possibility grid subcell font size is 0.75 of system default
		final float PANEL_SIZE_MULTIPLIER = 3f;					// Value cell font and panel size is 3x the Possibility grid subcell font/panel size

		/****************************************
		 * CellFormat()
		 * 
		 * Constructor. Creates Font objects for cell values and cell possibilities based on system default.
		 * 
		 ***************************************/
		CellFormat()
		{
			// Define fonts for grid - possibility font use UI default size * 0.75, value font is possibility size * 3
			possibilityFont = UIManager.getDefaults().getFont("TextPane.font");
			possibilityFont = possibilityFont.deriveFont(possibilityFont.getSize2D()*POSSIBILITY_SIZE_MULTIPLIER);
			valueFont = possibilityFont.deriveFont(possibilityFont.getSize2D()*PANEL_SIZE_MULTIPLIER);
		}
		
		/****************************************
		 * getPossibilityFont()
		 * 
		 * Return the derived font for the cell possibility grid text
		 ***************************************/
		Font getPossibilityFont()
		{
			return possibilityFont;
		}
		
		/****************************************
		 * getValueFont()
		 * 
		 * Return the derived font for the cell value text
		 ***************************************/
		Font getValueFont()
		{
			return valueFont;
		}
		
		/****************************************
		 * setDimension()
		 * 
		 * First GridCell to initialise calculates the max preferred of the heights and widths of all posisbility subcells
		 * This is passed here and used to calculate the desired panel size, which all GridCells will use, and the grid size itself (9x that)
		 ***************************************/
		void setDimension(int size) 
		{
			size *= PANEL_SIZE_MULTIPLIER;
			cellSize = new Dimension(size, size);
			size *= 9;
			gridSize = new Dimension(size, size);
			dimensionAvailable = true;
		}
		
		/****************************************
		 * getCellDimension()
		 * 
		 * Return the Dimension object that is the preferred size for all GridCells
		 ***************************************/
		Dimension getCellDimension()
		{
			return cellSize;
		}
		
		/****************************************
		 * getGridDimension()
		 * 
		 * Return the Dimension object that is the preferred size for the 9x9 Sudoku Grid
		 ***************************************/
		Dimension getGridDimension()
		{
			return gridSize;
		}
		
		/****************************************
		 * dimensionSet()
		 * 
		 * Returns if the Dimension object has been created yet or not
		 ***************************************/
		boolean dimensionSet()
		{
			return dimensionAvailable;
		}
	}

}
