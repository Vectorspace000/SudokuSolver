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


import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/***********************************************************************
* class LogTextArea
* 
* JTextArea to hold the activity log
* 
***********************************************************************/
public class LogTextArea
{
	public static final int MAX_LINES = 100;
	private final JTextArea log = new JTextArea();

	LogTextArea()
	{
		Font defaultFont = log.getFont();
		//log.setFont(new Font("Courier New",defaultFont.getStyle(), defaultFont.getSize()));
		log.setFont(new Font(Font.MONOSPACED,defaultFont.getStyle(), defaultFont.getSize()));
		log.setEditable(false);
		log.setLineWrap(true);
		log.setWrapStyleWord(true);
	}

	JTextArea getJTextArea()
	{
		return log;
	}

	void addLine(String text)
	{
		log.append(text);
		log.append("\n");
		if (log.getLineCount() > MAX_LINES)
		{
			int removeLines = log.getLineCount() - MAX_LINES;
			try
			{
				log.replaceRange("",0,log.getLineStartOffset(removeLines));
			}
			catch (BadLocationException e)
			{
				System.out.println("error removing lines");
			}
		}
		log.setCaretPosition(log.getDocument().getLength());
		log.update(log.getGraphics());
	}

	void clear()
	{
		log.setText(null);
	}

}
