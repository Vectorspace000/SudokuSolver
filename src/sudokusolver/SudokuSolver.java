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
 * @author Jamie
 */
import javax.swing.* ;
import java.awt.* ;
import java.awt.event.* ;
import javax.swing.border.*;
import java.util.regex.*;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/***********************************************************************
* class SudokuSolver
* 
* Instantiates and runs an instance of SudokuSolverUI
* 
***********************************************************************/
public class SudokuSolver
{
	
	public static void main(String[] args)
	{
		SudokuSolverUI ui = new SudokuSolverUI();
		ui.run();
	}
}

/***********************************************************************
* class SudokuSolverUI
* 
* Builds and displays a UI for solving Sudoku puzzles
* Class SudokuGrid generates and updates the Sudoku Game grid
* Class SudokuProcessing actions user operations on the game:
* 	Load user input from grid
* 	clear grid
* 	Apply solutions
* 	Highlight cells
***********************************************************************/
class SudokuSolverUI implements ItemListener, ActionListener
{
	
	public static final String[] ABOUT_STRINGS = 
	{
		"",
		"About Sudoku Solver",
		"Author:    Jamie Speed",
		"Date:      13/09/2016",
		"Description:",
		"I wanted to teach myself Java, so I built this.",
		"This is a Sudoku Solving tool. The user can input a Sudoku puzzle and apply various solutions to it to see the results.",
		"It also supports solving the whole puzzle, using logical solutions or brute force recursion.",
		""
	};
	
	private JFrame windowFrame;
	private JPanel topPane;
	private LogTextArea log = new LogTextArea();

	private SudokuGrid sudokuGrid = new SudokuGrid(this);
	private SudokuProcessing processing = new SudokuProcessing(sudokuGrid, log);

	private JPanel controlPanel;
	private JPanel solutionsPanel;
	private JPanel highlightGrid;

	private JCheckBox[] highlightCheck = new JCheckBox[9];
	private JComboBox nSetsListBox;
	//JComboBox<String> nSetsListBox;	// Prevents "Note: SudokuSolver.java uses unchecked or unsafe operations." "Note: Recompile with -Xlint:unchecked for details." compile warnings, not supported in 1.6

	private static final int B_LOAD			= 0;
	private static final int B_LOAD_PREV	= 1;
	private static final int B_LOAD_CLIP	= 2;
	private static final int B_COPY_CLIP	= 3;
	private static final int B_CLEAR		= 4;
	private static final int B_CLEAR_L		= 5;
	private static final int B_CLEAR_H		= 6;
	private static final int B_SOLVE		= 7;
	private static final int B_SOLVE_REC	= 8;
	private static final int B_SINGLE_P		= 9;
	private static final int B_SINGLE_C		= 10;
	private static final int B_SINGLE_R		= 11;
	private static final int B_SINGLE_3		= 12;
	private static final int B_POSS_VAL		= 13;
	private static final int B_POSS_ROW_3	= 14;
	private static final int B_POSS_COL_3	= 15;
	private static final int B_POSS_3_ROW	= 16;
	private static final int B_POSS_3_COL	= 17;
	private static final int B_POSS_N_ROW	= 18;
	private static final int B_POSS_N_COL	= 19;
	private static final int B_POSS_N_3X3	= 20;
	private static final int B_ABOUT		= 21;
	private static final int B_TEST_1		= 22;

	private static final int BS_ID = 0;
	private static final int BS_TXT = 1;
	private static final int BS_INPUT_MODE = 2;
	private static final int BS_TOOLTIP = 3;

	private static final String[][] bStrings = {
// Grid Controls
		{"loadGrid",		"Load Grid",			"I",
			""},
		{"loadPrevGrid",	"Load Previous Grid",	"I",
			""},
		{"loadClip",		"Load from Clipboard",	"I",
			"<html>Load grid from clipboard (9 plaintext lines of 9 digits, blank or 0-9)</html>"},
		{"copyClip",		"Copy to Clipboard",	"B",
			"<html>Copy current grid values  to clipboard (9 plaintext lines of 9 digits, blank or 0-9)</html>"},
		{"clearGrid",		"Clear Grid",			"B",
			""},
		{"clearLog",		"Clear Log",			"B",
			""},
		{"clearHigh",		"Clear Highlights",		"R",
			""},
// Solutions - global
		{"solve",			"Solve (logic)",		"R",
			"Solve puzzle, using mutliple iterations of the individual solutions"},
		{"solveRec",		"Solve (brute force)",	"R",
			"Solve puzzle, using recursion to iterate over all possible combinations."},
// Solutions - value updates
		{"singlePoss",		"Single Possibility",	"R",
			"<html>If a cell has only one possible value, it must be that value</html>"},
		{"singleCol",		"Single In Column",		"R",
			"<html>If only one cell in a column can be a specific value, <br>then no other cell in that column can be that value</html>"},
		{"singleRow",		"Single In Row",		"R",
			"<html>If only one cell in a row can be a specific value, <br>then no other cell in that row can be that value</html>"},
		{"single3x3",		"Single In 3x3",		"R",
			"<html>If only one cell in a 3x3 can be a specific value, <br>then no other cell in that 3x3 can be that value</html>"},

// Solutions - possibility updates - remove by
		{"rmvVal",				"by value",	"R",
			"<html>If a cell is value n, then no other cells in this row/column/3x3 can be value n</html>"},
		{"rmvRow3x3",			"by row in 3x3",	"R",
			"<html>If 3 or less cells in a row can be value n and they are in the same 3x3, <br>then no other cell in that 3x3 ouside the row can be n</html>"},
		{"rmvCol3x3",			"by column in 3x3",	"R",
			"<html>If 3 or less cells in a column can be value n and they are in the same 3x3, <br>then no other cell in that 3x3 ouside the column can be n</html>"},
		{"rmv3x3Row",			"by 3x3 in row",	"R",
			"<html>If 3 or less cells in a 3x3 can be value n and they are in the same row, <br>then no other cell in that row ouside the 3x3 can be n</html>"},
		{"rmv3x3Col",			"by 3x3 in column",	"R",
			"<html>If 3 or less cells in a 3x3 can be value n and they are in the same column, <br>then no other cell in that column ouside the 3x3 can be n</html>"},

// Solutions - possibility updates - n sets of n
		{"nSetsRow",			"by row",		"R",
			"<html>If n cells in the same row have the same n possibilities, <br>then no other cells in this row can be those n possibilities</html>"},
		{"nSetsCol",			"by column",	"R",
			"<html>If n cells in the same column have the same n possibilities, <br>then no other cells in this column can be those n possibilities</html>"},
		{"nSets3x3",			"by 3x3",		"R",
			"<html>If n cells in the same 3x3 have the same n possibilities, <br>then no other cells in this 3x3 can be those n possibilities</html>"},

// Misc/Test buttons
		{"about",			"About",		"B",
			""},
		{"test1",			"test 1",		"B",
			""}


	};

	private static final String nSetsListStrings[][] = {
		{"All sets (n = 2-7)","n = 2","n = 3","n = 4","n = 5","n = 6","n = 7"},
		{"0","2","3","4","5","6","7"}
	};

	private JButton[] buttons;
	private JPanel[] bPanels;




	/****************************************
	 * run()
	 * 
	 * Build and display the UI
	 ***************************************/
	public void run()
	{
		buildWindow();
		dspWindow();
	}

	/****************************************
	 * buildWindow()
	 * 
	 * Build the UI
	 ***************************************/
	private void buildWindow()
	{

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());	// Set System L&F
		} 
		catch (UnsupportedLookAndFeelException e)
		{
		   System.out.println("UnsupportedLookAndFeelExceptionn");
		}
		catch (ClassNotFoundException e)
		{
		   System.out.println("ClassNotFoundException");
		}
		catch (InstantiationException e)
		{
		   System.out.println("InstantiationException");
		}
		catch (IllegalAccessException e)
		{
		   System.out.println("IllegalAccessException");
		}


		JFrame.setDefaultLookAndFeelDecorated(true) ;						//Fancy decorations
		windowFrame = new JFrame("Sudoku Solver");							//Create and set up the window frame
		windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;		//Close application when close button clicked

		ToolTipManager.sharedInstance().setDismissDelay(ToolTipManager.sharedInstance().getDismissDelay()*2);
		buttons = new JButton[bStrings.length];
		bPanels = new JPanel[bStrings.length];



//********************************
// Build game grid panel

		JPanel gameGridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		gameGridPanel.setBorder(new TitledBorder("Game grid"));
		gameGridPanel.add(sudokuGrid.getPanel());


//*****************************************
// Build control buttons panel

		JPanel controlOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.setBorder(new TitledBorder("Controls"));

		controlPanel.add(buildButton(B_LOAD,true));
		controlPanel.add(buildButton(B_LOAD_PREV,true));
		controlPanel.add(buildButton(B_LOAD_CLIP,true));
		controlPanel.add(buildButton(B_COPY_CLIP,true));
		controlPanel.add(buildButton(B_CLEAR,true));
		controlPanel.add(buildButton(B_CLEAR_L,true));
		controlPanel.add(buildButton(B_SOLVE,true));
		controlPanel.add(buildButton(B_SOLVE_REC,true));
		//controlPanel.add(buildButton(B_TEST_1,true));


		JPanel highlightOuterPanel = new JPanel(new GridLayout(1,1));
		JPanel highlightPanel = new JPanel();
		highlightPanel.setLayout(new BoxLayout(highlightPanel, BoxLayout.Y_AXIS));
		highlightPanel.setBorder(new TitledBorder("Highlight"));

		highlightGrid = new JPanel(new GridLayout(3,3));
		for (int i = 0; i < highlightCheck.length; i++)
		{
			highlightCheck[i] = new JCheckBox(""+(i+1));
			highlightCheck[i].addItemListener(this);
			highlightGrid.add(highlightCheck[i]);
		}
		highlightPanel.add(highlightGrid);
		highlightPanel.add(buildButton(B_CLEAR_H,true));
		highlightOuterPanel.add(highlightPanel);
		controlPanel.add(highlightOuterPanel);

		controlPanel.add(buildButton(B_ABOUT,true));

		controlOuterPanel.add(controlPanel);



//*****************************************
// Build solutions buttons panel

		JPanel solutionsOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		solutionsPanel = new JPanel();
		solutionsPanel.setLayout(new BoxLayout(solutionsPanel, BoxLayout.X_AXIS));
		solutionsPanel.setBorder(new TitledBorder("Individual Solutions"));

		//-----------------------------------------
		// Value Updates Sub-panel
			JPanel solutionsValueOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
			JPanel solutionsValuePanel = new JPanel();
			solutionsValuePanel.setLayout(new BoxLayout(solutionsValuePanel, BoxLayout.Y_AXIS));
			solutionsValuePanel.setBorder(new TitledBorder("Value Updates"));
			solutionsValuePanel.add(buildButton(B_SINGLE_P,true));
			solutionsValuePanel.add(buildButton(B_SINGLE_R,true));
			solutionsValuePanel.add(buildButton(B_SINGLE_C,true));
			solutionsValuePanel.add(buildButton(B_SINGLE_3,true));
			solutionsValueOuterPanel.add(solutionsValuePanel);
			solutionsPanel.add(solutionsValueOuterPanel);

		//-----------------------------------------
		// Possibility Updates Sub-panel
			JPanel solutionsPossibilityPanel = new JPanel();
			solutionsPossibilityPanel.setLayout(new BoxLayout(solutionsPossibilityPanel, BoxLayout.X_AXIS));
			solutionsPossibilityPanel.setBorder(new TitledBorder("Possibility Updates"));

			//-----------------------------------------
			// Possibility Remove By Sub-panel
				JPanel solutionsRemoveOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
				JPanel solutionsRemovePanel = new JPanel();
				solutionsRemovePanel.setLayout(new BoxLayout(solutionsRemovePanel, BoxLayout.Y_AXIS));
				solutionsRemovePanel.setBorder(new TitledBorder("Remove by"));
				solutionsRemovePanel.add(buildButton(B_POSS_VAL,true));
				solutionsRemovePanel.add(buildButton(B_POSS_ROW_3,true));
				solutionsRemovePanel.add(buildButton(B_POSS_COL_3,true));
				solutionsRemovePanel.add(buildButton(B_POSS_3_ROW,true));
				solutionsRemovePanel.add(buildButton(B_POSS_3_COL,true));
				solutionsRemoveOuterPanel.add(solutionsRemovePanel);
				solutionsPossibilityPanel.add(solutionsRemoveOuterPanel);

			//-----------------------------------------
			// Possibility n sets of n Sub-panel
				JPanel solutionsNSetsOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
				JPanel solutionsNSetsPanel = new JPanel();
				solutionsNSetsPanel.setLayout(new BoxLayout(solutionsNSetsPanel, BoxLayout.Y_AXIS));
				solutionsNSetsPanel.setBorder(new TitledBorder("n sets of n"));
				solutionsNSetsPanel.add(buildButton(B_POSS_N_ROW,true));
				solutionsNSetsPanel.add(buildButton(B_POSS_N_COL,true));
				solutionsNSetsPanel.add(buildButton(B_POSS_N_3X3,true));

				nSetsListBox = new JComboBox(nSetsListStrings[0]);
				//nSetsListBox = new JComboBox<>(nSetsListStrings[0]);		// Prevents "Note: SudokuSolver.java uses unchecked or unsafe operations." "Note: Recompile with -Xlint:unchecked for details." compile warnings, not supported in 1.6
				nSetsListBox.setSelectedIndex(0);


				solutionsNSetsPanel.add(nSetsListBox);

				solutionsNSetsOuterPanel.add(solutionsNSetsPanel);
				solutionsPossibilityPanel.add(solutionsNSetsOuterPanel);

			solutionsPanel.add(solutionsPossibilityPanel);
		solutionsOuterPanel.add(solutionsPanel);


//*****************************************
// Create text log
		JPanel logOuterPanel = new JPanel(new BorderLayout());
		// JTextArea logPanel = new JTextArea(); // defined globally
		JScrollPane logScrollPanel = new JScrollPane(log.getJTextArea());
		logOuterPanel.add(logScrollPanel, BorderLayout.CENTER);
		logOuterPanel.setBorder(new TitledBorder("Log"));
		log.getJTextArea().setRows(15);
		log.getJTextArea().setColumns(50);

//*****************************************
// Build top row

		topPane = new JPanel(new BorderLayout());
		JPanel topRow = new JPanel(new GridBagLayout());
		GridBagConstraints topData = new GridBagConstraints();


		topData.anchor = GridBagConstraints.NORTHWEST;
		topData.gridx = 0;
		topData.gridy = 0;
		topRow.add(gameGridPanel,topData);


		topData.anchor = GridBagConstraints.NORTHWEST;
		topData.gridx = 1;
		topData.gridy = 0;
		topRow.add(controlOuterPanel,topData);


		topData.anchor = GridBagConstraints.NORTHWEST;
		topData.gridx = 0;
		topData.gridy = 1;
		topData.gridwidth = 2;
		topData.weighty = 1.0;   //request any extra vertical space
		topRow.add(solutionsOuterPanel,topData);


		topPane.add(topRow,BorderLayout.LINE_START);
		topPane.add(logOuterPanel, BorderLayout.CENTER);
		
//*****************************************
// 

		setInputMode(true);


	}


	/****************************************
	 * dspWindow()
	 * 
	 * Display the UI window.
	 ***************************************/
	private void dspWindow()
	{
		windowFrame.getContentPane().add(topPane);
		windowFrame.pack();
		//windowFrame.setSize(500, 500);
		windowFrame.setVisible(true);
	}

	/****************************************
	 * buildSeparator()
	 * 
	 * 
	 ***************************************/
	private JPanel buildSeparator()
	{
		JPanel separatorPanel = new JPanel(new GridLayout(1,1));
		separatorPanel.add(Box.createVerticalStrut(5));
		separatorPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		separatorPanel.add(Box.createVerticalStrut(5));
		return separatorPanel;
	}

	/****************************************
	 * buildButton()
	 * 
	 * Set up required button
	 ***************************************/
	private JComponent buildButton(int i, boolean panel)
	{
		if (buttons[i] == null)
		{
			buttons[i] = new JButton(bStrings[i][BS_TXT]);
			buttons[i].addActionListener(this);
			buttons[i].setActionCommand(bStrings[i][BS_ID]);
			if(bStrings[i][BS_TOOLTIP].length() > 0)
				buttons[i].setToolTipText(bStrings[i][BS_TOOLTIP]);
			bPanels[i] = new JPanel(new GridLayout(1,1));
			bPanels[i].add(buttons[i]);
		}
		if(panel)
			return bPanels[i];
		else
			return buttons[i];
	}


	/****************************************
	 * actionPerformed()
	 * 
	 * Action Listener method for all buttons
	 ***************************************/
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (bStrings[B_LOAD][BS_ID].equals(e.getActionCommand()))
			loadGrid();
		else if (bStrings[B_LOAD_PREV][BS_ID].equals(e.getActionCommand()))
			loadPrevGrid();
		else if (bStrings[B_CLEAR][BS_ID].equals(e.getActionCommand()))
			clearGrid();
		else if (bStrings[B_CLEAR_L][BS_ID].equals(e.getActionCommand()))
			log.clear();
		else if (bStrings[B_SOLVE][BS_ID].equals(e.getActionCommand()))
			processing.solve();
		else if (bStrings[B_SOLVE_REC][BS_ID].equals(e.getActionCommand()))
			processing.solveRecursive();
		else if (bStrings[B_POSS_VAL][BS_ID].equals(e.getActionCommand()))
			processing.updatePossibilities(true);
		else if (bStrings[B_SINGLE_P][BS_ID].equals(e.getActionCommand()))
			processing.singlePossibility(true);
		else if (bStrings[B_SINGLE_C][BS_ID].equals(e.getActionCommand()))
			processing.singleInColumn(true);
		else if (bStrings[B_SINGLE_R][BS_ID].equals(e.getActionCommand()))
			processing.singleInRow(true);
		else if (bStrings[B_SINGLE_3][BS_ID].equals(e.getActionCommand()))
			processing.singleIn3x3(true);
		else if (bStrings[B_CLEAR_H][BS_ID].equals(e.getActionCommand()))
			clearHighlight();
		else if (bStrings[B_LOAD_CLIP][BS_ID].equals(e.getActionCommand()))
			loadFromClipboard();
		else if (bStrings[B_COPY_CLIP][BS_ID].equals(e.getActionCommand()))
			copyToClipboard();
		else if ("gridEnter".equals(e.getActionCommand()))
			buttons[B_LOAD].requestFocus();
		else if (bStrings[B_POSS_N_ROW][BS_ID].equals(e.getActionCommand()))
			nSetsProcessing(bStrings[B_POSS_N_ROW][BS_ID]);
		else if (bStrings[B_POSS_N_COL][BS_ID].equals(e.getActionCommand()))
			nSetsProcessing(bStrings[B_POSS_N_COL][BS_ID]);
		else if (bStrings[B_POSS_N_3X3][BS_ID].equals(e.getActionCommand()))
			nSetsProcessing(bStrings[B_POSS_N_3X3][BS_ID]);
		else if (bStrings[B_POSS_ROW_3][BS_ID].equals(e.getActionCommand()))
			processing.rowIn3x3(true);
		else if (bStrings[B_POSS_COL_3][BS_ID].equals(e.getActionCommand()))
			processing.columnIn3x3(true);
		else if (bStrings[B_POSS_3_ROW][BS_ID].equals(e.getActionCommand()))
			processing.x3InRow(true);
		else if (bStrings[B_POSS_3_COL][BS_ID].equals(e.getActionCommand()))
			processing.x3InColumn(true);
		else if (bStrings[B_ABOUT][BS_ID].equals(e.getActionCommand()))
			about();
		else if (bStrings[B_TEST_1][BS_ID].equals(e.getActionCommand()))
			test1();

		else
			System.out.println("unassigned action string: " + e.getActionCommand());

	}

	/****************************************
	 * test1()
	 * 
	 * Test button
	 ***************************************/
	void test1()
	{
		System.out.println("test1");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String time = ( sdf.format(cal.getTime()) );
		log.addLine(time);
	}

	/****************************************
	 * about()
	 * 
	 * Print About info to log
	 ***************************************/
	private void about()
	{
		for (String aboutLine: ABOUT_STRINGS)
		{
			log.addLine(aboutLine);
		}


	}

	/****************************************
	 * nSetsProcessing()
	 * 
	 * Instruct processing class to perform the appropriate n Sets of n processing
	 ***************************************/
	private void nSetsProcessing(String type)
	{

		// Get applicable n value (set size, 0 = all)
		int nRange = nSetsListBox.getSelectedIndex();
		if (nRange >= 0)
			nRange = Integer.parseInt(nSetsListStrings[1][nRange]);
		else
			nRange = 0;

		// Call appropriate processor - Row, Col, 3x3
		if(type.equals(bStrings[B_POSS_N_ROW][BS_ID]))
			processing.nSetsRow(nRange,true);
		else if(type.equals(bStrings[B_POSS_N_COL][BS_ID]))
			processing.nSetsColumn(nRange,true);
		else if(type.equals(bStrings[B_POSS_N_3X3][BS_ID]))
			processing.nSets3x3(nRange,true);
		

	}

	/****************************************
	 * loadFromClipboard()
	 * 
	 * Load grid from clipboard
	 ***************************************/
	private void loadFromClipboard()
	{
		String textIn;
		try
		{
			textIn = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor); 
			textIn = textIn.replaceAll("\\n|\\r","");		// Remove newline characters
			textIn = textIn.replaceAll(" ","0");			// Replace blanks with 0s
		}
		catch (UnsupportedFlavorException e)
		{
			System.out.println("invalid flavour");
			log.addLine("invalid flavour");
			return;
		}
		catch (IOException e)
		{
			System.out.println("invalid io");
			log.addLine("invalid io");
			return;
		}

		Pattern pattern = Pattern.compile("^[0-9]+$");	// Regex pattern - string can contain only 0-9
		Matcher matcher = pattern.matcher(textIn);
		if (matcher.matches() == false)
		{
			System.out.println("invalid characters - must be blanks and 0-9");
			log.addLine("invalid characters - must be blanks and 0-9");
			return;
		}

		//String is valid for import. Import it
		clearGrid();
		for (int y = 1, i = 0; y <= 9; y++)
		{
			for(int x = 1; x <= 9; x++)
			{
				if (i < textIn.length())
					sudokuGrid.setInputValue(x,y,Character.getNumericValue(textIn.charAt(i++)));
				else
					sudokuGrid.setInputValue(x,y,0);
			}
		}
	}

	/****************************************
	 * copyToClipboard()
	 * 
	 * Copy grid to clipboard
	 ***************************************/
	private void copyToClipboard()
	{

		// Copy state to string
		StringBuilder textOut = new StringBuilder();
		for (int y = 1; y <= 9; y++)
		{
			for(int x = 1; x <= 9; x++)
			{
				textOut.append(Integer.toString(sudokuGrid.getModeValue(x,y)));
			}
			textOut.append("%n");	// Format code for platform specific newline character
		}
		String textOutString = String.format(textOut.toString());	// Format string, to convert %n to platform specific newline character
		StringSelection copyOut = new StringSelection(textOutString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(copyOut, null);
	}

	/****************************************
	 * itemStateChanged()
	 * 
	 * Item listener for all checkboxes
	 ***************************************/
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getItemSelectable();
		for (int i = 0; i < highlightCheck.length; i++)
		{
			if (source == highlightCheck[i])
			{
				updateHighlight();
				break;
			}
		}
	}

	/****************************************
	 * loadGrid()
	 * 
	 * Instruct SudokuProcessing to:
	 * 	import the user-inputted values as the initial game state
	 * 	set grid cells from input mode to value/possibility mode
	 * Set UI to not be in input mode
	 ***************************************/
	private void loadGrid()
	{
		processing.importFromGrid();
		setInputMode(false);
	}

	/****************************************
	 * loadPrevGrid()
	 * 
	 * Instruct SudokuProcessing to:
	 * 	import the user-inputted values from the previous grid as the initial game state
	 * 	set grid cells from input mode to value/possibility mode
	 * Set UI to not be in input mode
	 ***************************************/
	private void loadPrevGrid()
	{
		processing.importFromPrevious();
		setInputMode(false);
	}

	/****************************************
	 * clearGrid()
	 * 
	 * Instruct SudokuProcessing to clear the game state and game grid
	 * Set UI to input mode
	 ***************************************/
	private void clearGrid()
	{
		processing.clearGame();
		setInputMode(true);
	}

	/****************************************
	 * setInputMode()
	 * 
	 * Enable/disbale Input mode on the UI
	 * Determines which controls are enabled
	 ***************************************/
	private void setInputMode(boolean inputModeOn)
	{

		for (int i = 0; i < bStrings.length;i++)				// For each button
		{
			if (buttons[i] != null)								// In use buttons only
			{
				if (bStrings[i][BS_INPUT_MODE].equals("I"))			// If input mode only
					buttons[i].setEnabled(inputModeOn);				// Enable if input mode, disable if not
				else if (bStrings[i][BS_INPUT_MODE].equals("R"))	// If not input mode only
					buttons[i].setEnabled(!inputModeOn);			// Disable if input mode, enable if not
				// impled else, if enabled for both, never enable/disable

			}
		}

		for (int i = 0; i < highlightCheck.length; i++)			// For all Highlight checkboxes
		{
			highlightCheck[i].setEnabled(!inputModeOn);			// Disable if input mode, enable if not
			highlightCheck[i].setSelected(false);				// Always deselect on mode change
		}

		nSetsListBox.setEnabled(!inputModeOn);					// n Sets of n dropdown enabled for not input mode

	}

	/****************************************
	 * clearHighlight()
	 * 
	 * Clear all the Highlighted checkboxes
	 * Instruct the grid to refresh the highlight
	 ***************************************/
	private void clearHighlight()
	{
		for (int i = 0; i < highlightCheck.length; i++)
		{
			highlightCheck[i].setSelected(false);
		}
		updateHighlight();
	}

	/****************************************
	 * updateHighlight()
	 * 
	 * Instruct the grid to update its highlighting settings based on the checkbox statuses
	 ***************************************/
	private void updateHighlight()
	{
		boolean highlight[] = new boolean[9];
		for (int i = 0; i < highlightCheck.length; i++)
		{
			highlight[i] = highlightCheck[i].isSelected();
		}
		sudokuGrid.setHighlight(highlight);
		sudokuGrid.refreshFormats();
	}

}
