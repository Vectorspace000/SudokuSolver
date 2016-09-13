# SudokuSolver
Sudoku solver

I have spend 8 years working as an RPGLE developer on IBM iSeries. Now I'm being made redundant, and RPGLE is not a commonly required skill, so employment options are scarce.
So I figured, why not try and retrain as a Java developer?
I learned a little bit of Java back at university, but only the basics. It's 15 years later and all I had left was basic knowledge of java syntax, and a basic understanding of the concept of Object Orientated programming. This may not seem like much to start with, but is huge to me given that RPGLE is a business applications language that is not object orientaned and is not free form.

This time round I started with some online learning via my employer's intranet. It was useful, but rather boring as it did not need me to go and actually write java code. Just multiple choice tests at the end of each section and the occastional text box to write a single code statement.
I did a few, including a very basic Swing GUI learning (which went as far as a window with 1 button and 3 checkboxes that changed a text label). By then I knew enough to be dangerous, so I started looking for things I could actually build. I did a few things on projecteuler.net (ok, but too much maths for my purpose) and completed codingbat.com (awesome site), and wrote a simple command line base64 encoder/decoder (using my own code as opposed to the existing java classes).

I wanted something meatier I could sink my teeth into. A long time ago I wrote a simple text based sudoku puzzle solver in Matlab, so I decided to try and build a GUI solver.

I did, and here it is. Writen almost entirely using notepad++, once it was finished I imported it into a Netbeans project to do a bit of neatening and to upload here.


Sudoku Solver displays a 9x9 text entry grid for the user to type in a puzzle. Arrow keys will navigate between cells, typing in a cell auto moves focus to the next cell. Load button will import the entered puzzle, and for each blank cell will display a 9x9 grid of the available possibilities for that cell

Load From Clipboard will load a puzzle from the clipboard. Clipboard must be a 9x9 block of plain text, with the numbers 0-9, e.g.:
004700000
003080604
600030000
250190040
300060009
060073051
000050007
508010200
000009500

Once imported, user can:
  click one of the Solve buttons (logic and brute force [try all possible combinations] are available)
  click the buttons to apply any of the individual solution methods provided, and update the grid accordingly:
    methods:
      set values based on possibilities
      or remove poossibilities based on logic patterns.
    Each method button has a tooltip describing it
    When a value is added, it is initially red to indicate just changed, and semitransparent so the underlying possibilities can be seen. On the next operation onwards it is the standard black on white
    When a possibility is removed, it is initially red and struck through to indicate just removed. On the next operation onwards it is the standard black on white

A log panel displays information for each action taken, such as number of values added.

Highlight controls allow one or more numbers to be highlighted, to make patterns more visible. This is actually accomplished by dimming all other numbers

Thgere are also buttons to clear the grid to allow re-input, clear the log, reload the previous grid (if used on first run this will load a default puzzle) and copy the current state of values to the clipboard (in the same format as load from clipboard requires)

I know I have not used proper Sudoku terminology, but that is because I don't know the proper terminology. This is just how I have always known them.



Because I have had no detailed formal java training, I am not knowledgeable on best practices. I would love feedback on what experienced devs think of this, my first large GUI Java program.
