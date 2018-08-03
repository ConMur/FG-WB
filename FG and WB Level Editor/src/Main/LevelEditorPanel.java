package Main;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class LevelEditorPanel extends JPanel implements MouseListener,
		MouseMotionListener, KeyListener
{
	private boolean running = true;
	private int FPS = 60;
	private long targetTime = 1000 / FPS;

	private enum State {
		MENU, DESIGN
	};

	private State state;

	private enum Choice {
		NEW, LOAD
	}

	private Choice choice;

	private final int WIDTH = 1024;
	private final int HEIGHT = 768;
	private BufferedImage bg;

	private BufferedImage tileSet;
	private BufferedImage[][] tiles;
	private BufferedImage buoy, grill;
	private BufferedImage[] obstacles;
	private BufferedImage[] treasures;
	private BufferedImage redDoor, blueDoor;

	private int tileSize = 32;
	private int tilesAcross;
	private int[][] level;
	private int noObstacles;

	private int grabbedX, grabbedY;
	private int tileGrabbed;
	private boolean aTileIsGrabbed;
	private BufferedImage grabbedTile;

	private boolean showTilePanel;

	private String[] options = { "New", "Load" };

	private PrintWriter writer;
	private String whereToWrite = "LevelX.lvl";

	private boolean selectObstacle;
	private Selection[] selections;
	private int currentSelection;
	private boolean done, firstOneDone, secondOneDone, laserDone;

	private Cursor c, selector;

	public LevelEditorPanel() throws IOException
	{
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		init();
		setCursor(c);
	}

	public void start()
	{
		run();
	}

	private void init() throws IOException
	{
		// Set the look and feel to system default
		try
		{
			// Set System L&F
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		}
		catch (UnsupportedLookAndFeelException e)
		{
			// handle exception
		}
		catch (ClassNotFoundException e)
		{
			// handle exception
		}
		catch (InstantiationException e)
		{
			// handle exception
		}
		catch (IllegalAccessException e)
		{
			// handle exception
		}
		// Load all the images
		bg = ImageIO.read(getClass().getResourceAsStream(
				"/Backgrounds/CastleBackground.png"));
		level = new int[24][32];

		// Set cursors
		// Regular cursor
		Image cursor = ImageIO.read(getClass().getResourceAsStream(
				"/cursor.png"));
		Point tip = new Point(16, 16);
		c = Toolkit.getDefaultToolkit().createCustomCursor(cursor, tip,
				"cursor");

		// Selection cursor
		Image selection = ImageIO.read(getClass().getResourceAsStream(
				"/selector.png"));
		Point point = new Point(16, 16);
		selector = Toolkit.getDefaultToolkit().createCustomCursor(
				selection, point,
				"cursor");

		tileSet = ImageIO.read(getClass().getResourceAsStream(
				"/Tilesets/GrassTileSet.png"));
		tilesAcross = tileSet.getWidth() / tileSize;
		tiles = new BufferedImage[3][tilesAcross];

		// Load the tileset
		tiles[0][0] = tileSet.getSubimage(32, 0, 32, 32);
		tiles[0][1] = tileSet.getSubimage(64, 0, 32, 32);
		tiles[0][2] = tileSet.getSubimage(96, 0, 32, 32);
		tiles[1][0] = tileSet.getSubimage(0, 32, 32, 32);
		tiles[1][1] = tileSet.getSubimage(32, 32, 32, 32);
		tiles[1][2] = tileSet.getSubimage(64, 32, 32, 32);
		tiles[1][3] = tileSet.getSubimage(96, 32, 32, 32);

		// Load players
		buoy = ImageIO.read(getClass().getResourceAsStream(
				"/Players/32Buoy.png"));
		grill = ImageIO.read(getClass().getResourceAsStream(
				"/Players/32Grill.png"));

		// Load the doors
		redDoor = ImageIO.read(getClass().getResourceAsStream(
				"/Obstacles/doorClosedRed.png"));
		blueDoor = ImageIO.read(getClass().getResourceAsStream(
				"/Obstacles/doorClosedBlue.png"));

		// load the obstacles
		obstacles = new BufferedImage[7];

		obstacles[0] = ImageIO.read(getClass()
				.getResourceAsStream("/Obstacles/buttonBlueOn.png"));
		obstacles[1] = ImageIO.read(getClass()
				.getResourceAsStream("/Obstacles/buttonGreenOn.png"));
		obstacles[2] = ImageIO.read(getClass()
				.getResourceAsStream("/Obstacles/buttonRedOn.png"));

		obstacles[3] = ImageIO.read(getClass()
				.getResourceAsStream("/Obstacles/laserSwitchBlueOn.png"));
		obstacles[4] = ImageIO.read(getClass()
				.getResourceAsStream("/Obstacles/laserSwitchGreenOn.png"));
		obstacles[5] = ImageIO.read(getClass()
				.getResourceAsStream("/Obstacles/laserSwitchRedOn.png"));

		obstacles[6] = ImageIO.read(getClass()
				.getResourceAsStream("/Obstacles/laser.png"));

		// load the items
		treasures = new BufferedImage[5];

		treasures[0] = ImageIO.read(getClass()
				.getResourceAsStream("/Items/coinBronze.png"));
		treasures[1] = ImageIO.read(getClass()
				.getResourceAsStream("/Items/coinSilver.png"));
		treasures[2] = ImageIO.read(getClass()
				.getResourceAsStream("/Items/coinGold.png"));
		treasures[3] = ImageIO.read(getClass()
				.getResourceAsStream("/Items/gemRed.png"));
		treasures[4] = ImageIO.read(getClass()
				.getResourceAsStream("/Items/gemBlue.png"));

		state = State.MENU;

		grabbedX = -1;
		grabbedY = -1;
		tileGrabbed = 0;
		aTileIsGrabbed = false;
		grabbedTile = null;

		noObstacles = 0;

		showTilePanel = true;

		// Probably no more than 10 obstacles
		selections = new Selection[10];
		currentSelection = 0;
		done = false;
		firstOneDone = false;
		secondOneDone = false;
		laserDone = false;
	}

	private void run()
	{
		long start;
		long elapsed;
		long wait;
		while (running)
		{
			start = System.nanoTime();

			update();
			repaint();

			elapsed = System.nanoTime() - start;
			wait = targetTime - (elapsed / 1000000);
			if (wait < 0)
			{
				wait = 5;
			}

			try
			{
				Thread.sleep(wait);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void update()
	{
		if (state == State.MENU)
		{

		}
		else if (state == State.DESIGN)
		{

		}
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();

		if (state == State.MENU)
		{
			if (x > 200 && x < 550 && y > 300 && y < 384)
			{
				choice = Choice.NEW;
			}
			else if (x > 200 && x < 550 && y > 384 && y < 460)
			{
				choice = Choice.LOAD;
			}
			try
			{
				load(choice);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
		else if (state == State.DESIGN)
		{
			int row = y / tileSize;
			int col = x / tileSize;

			if (e.getButton() == MouseEvent.BUTTON3)
			{
				level[row][col] = 0;
			}
			if (selectObstacle)
			{
				selectObstacles(x, y);
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		grabbedX = e.getX();
		grabbedY = e.getY();

		if (showTilePanel)
		{
			// Mouse pressed in the tile selection panel
			if (grabbedX > 912 && grabbedX < 1024)
			{
				// Mouse pressed in the players section
				if (grabbedX > 912 && grabbedX < 1008 && grabbedY > 16
						&& grabbedY < 48)
				{
					if (grabbedX > 912 && grabbedX < 944)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = buoy;
							tileGrabbed = 100;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = grill;
							tileGrabbed = 101;
						}
					}
				}
				// Mouse pressed in the liquids section
				else if (grabbedX > 912 && grabbedX < 1008 && grabbedY > 48
						&& grabbedY < 128)
				{
					if (grabbedX > 912 && grabbedX < 944 && grabbedY > 48
							&& grabbedY < 80)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = tiles[0][0];
							tileGrabbed = 1;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008 && grabbedY > 48
							&& grabbedY < 80)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = tiles[0][1];
							tileGrabbed = 2;
						}
					}
					else if (grabbedX > 912 && grabbedX < 944 && grabbedY > 96
							&& grabbedY < 128)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = tiles[0][2];
							tileGrabbed = 3;
						}
					}
				}
				// Mouse pressed in the tiles section
				else if (grabbedX > 912 && grabbedX < 1008 && grabbedY > 160
						&& grabbedY < 260)
				{
					if (grabbedX > 912 && grabbedX < 944 && grabbedY > 160
							&& grabbedY < 192)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = tiles[1][0];
							tileGrabbed = 10;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008
							&& grabbedY > 160
							&& grabbedY < 192)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = tiles[1][1];
							tileGrabbed = 11;
						}
					}
					else if (grabbedX > 912 && grabbedX < 944 && grabbedY > 224
							&& grabbedY < 256)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = tiles[1][2];
							tileGrabbed = 12;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008
							&& grabbedY > 224
							&& grabbedY < 256)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = tiles[1][3];
							tileGrabbed = 13;
						}
					}
				}
				// Mouse pressed in the treasures section
				else if (grabbedX > 912 && grabbedX < 1008 && grabbedY > 292
						&& grabbedY < 454)
				{
					if (grabbedX > 912 && grabbedX < 944 && grabbedY > 292
							&& grabbedY < 324)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = treasures[0];
							tileGrabbed = 200;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008
							&& grabbedY > 292
							&& grabbedY < 324)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = treasures[1];
							tileGrabbed = 201;
						}
					}
					else if (grabbedX > 912 && grabbedX < 944 && grabbedY > 358
							&& grabbedY < 390)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = treasures[2];
							tileGrabbed = 202;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008
							&& grabbedY > 358
							&& grabbedY < 390)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = treasures[3];
							tileGrabbed = 203;
						}
					}
					else if (grabbedX > 912 && grabbedX < 944 && grabbedY > 422
							&& grabbedY < 454)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = treasures[4];
							tileGrabbed = 204;
						}
					}
				}
				// MAKE IT SOME NUMBER LIKE 400 - 406 then write the numbers to
				// a temp file then when saving, copy the noObstacles lines from
				// the temp file to the end of the level
				// Mouse pressed in the obstacles section
				else if (grabbedX > 912 && grabbedX < 1008 && grabbedY > 486
						&& grabbedY < 710)
				{
					if (grabbedX > 912 && grabbedX < 944 && grabbedY > 486
							&& grabbedY < 518)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = obstacles[0];
							tileGrabbed = 400;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008
							&& grabbedY > 486
							&& grabbedY < 518)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = obstacles[1];
							tileGrabbed = 401;
						}
					}
					else if (grabbedX > 912 && grabbedX < 944 && grabbedY > 550
							&& grabbedY < 582)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = obstacles[2];
							tileGrabbed = 402;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008
							&& grabbedY > 550
							&& grabbedY < 582)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = obstacles[3];
							tileGrabbed = 403;
						}
					}
					else if (grabbedX > 912 && grabbedX < 944 && grabbedY > 614
							&& grabbedY < 646)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = obstacles[4];
							tileGrabbed = 404;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008
							&& grabbedY > 614
							&& grabbedY < 646)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = obstacles[5];
							tileGrabbed = 405;
						}
					}
					else if (grabbedX > 912 && grabbedX < 944 && grabbedY > 678
							&& grabbedY < 710)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = obstacles[6];
							tileGrabbed = 406;
						}
					}
				}
				// Mouse pressed in the doors section
				else if (grabbedX > 896 && grabbedX < 1024 && grabbedY > 704
						&& grabbedY < 774)
				{
					if (grabbedX > 912 && grabbedX < 944)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = redDoor;
							tileGrabbed = 300;
						}
					}
					else if (grabbedX > 976 && grabbedX < 1008)
					{
						aTileIsGrabbed = true;
						if (grabbedTile == null)
						{
							grabbedTile = blueDoor;
							tileGrabbed = 301;
						}
					}
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// Place tile on the map
		int col = grabbedX / tileSize;
		int row = grabbedY / tileSize;

		if (grabbedTile != null)
		{
			level[row][col] = tileGrabbed;
			grabbedTile = null;
			aTileIsGrabbed = false;
			tileGrabbed = 0;
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (state == State.DESIGN)
		{
			if (e.getKeyCode() == KeyEvent.VK_SPACE
					|| e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				if (showTilePanel)
				{
					showTilePanel = false;
				}
				else
				{
					showTilePanel = true;
				}
			}

			if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S)
			{
				try
				{
					save();
				}
				catch (FileNotFoundException fnf)
				{
					fnf.printStackTrace();
				}
			}

			if (e.getKeyCode() == KeyEvent.VK_C)
			{
				// Exit with another press of c
				if (selectObstacle)
				{
					selectObstacle = false;
					setCursor(c);
				}
				else
				{
					selectObstacle = true;
					// Reset all values in preparation for the next round of
					// selections
					done = false;
					firstOneDone = false;
					secondOneDone = false;
					laserDone = false;
					JOptionPane
							.showMessageDialog(
									null,
									"Welcome to the Selection wizard!\nPlease select the first button or a switch",
									"Selection",
									JOptionPane.PLAIN_MESSAGE);
					setCursor(selector);
				}
			}
		}
	}

	/**
	 * This method is used to link obstacles together (button with other button
	 * + laser or switch + laser)
	 */
	private void selectObstacles(int x, int y)
	{
		int theX = x / tileSize;
		int theY = y / tileSize;
		int obstacle = level[theY][theX];

		if (firstOneDone == false)
		{
			if (done == false)
			{
				showTilePanel = false;
				selections[currentSelection] = new Selection();
				// Button
				if (obstacle >= 400 && obstacle <= 402)
				{
					selections[currentSelection].selectFirstButton(theX,
							theY,
							(obstacle - 400) * 2);
					done = true;
					firstOneDone = true;
					JOptionPane.showMessageDialog(null,
							"Select the second button",
							"Selection",
							JOptionPane.PLAIN_MESSAGE);

				}
				// Switch
				else if (obstacle >= 403 && obstacle <= 405)
				{
					selections[currentSelection].selectSwitch(theX, theY,
							(obstacle - 400) * 2);
					done = true;
					firstOneDone = true;
					// Skip second check b/c there is no second switch
					secondOneDone = true;
					JOptionPane.showMessageDialog(null,
							"Select the laser", "Selection",
							JOptionPane.PLAIN_MESSAGE);
				}
			}
			done = false;
		}
		else if (secondOneDone == false)
		{
			if (done == false)
			{
				// Button
				if (obstacle >= 400 && obstacle <= 402)
				{
					selections[currentSelection].selectSecondButton(theX,
							theY,
							(obstacle - 400) * 2);
					done = true;
					secondOneDone = true;
					JOptionPane.showMessageDialog(null,
							"Select the laser", "Selection",
							JOptionPane.PLAIN_MESSAGE);
				}
			}
			done = false;
		}
		else if (laserDone == false)
		{
			if (done == false)
			{
				if (obstacle == 406)
				{
					selections[currentSelection].selectLaser(theX, theY);
					done = true;
					laserDone = true;
					JOptionPane.showMessageDialog(null,
							"Thank you for selecting!", "Selection",
							JOptionPane.PLAIN_MESSAGE);
					currentSelection++;
					noObstacles++;
					selectObstacle = false;
					showTilePanel = true;
					setCursor(c);
				}
			}
			done = false;
		}
	}

	private void load(Choice choice) throws IOException
	{
		if (choice == Choice.NEW)
		{

		}
		else if (choice == Choice.LOAD)
		{
			/*
			 * fc = new JFileChooser(); FileFilter fileFilter = new FileFilter()
			 * {
			 * 
			 * @Override public boolean accept(File file) { if
			 * (file.isDirectory()) { return true; // return directories for
			 * recursion } return file.getName().endsWith(".lvl"); // return
			 * .lvl files }
			 * 
			 * @Override public String getDescription() { return ".lvl files"; }
			 * }; fc.setFileFilter(fileFilter); int option =
			 * fc.showOpenDialog(null); if (option ==
			 * JFileChooser.APPROVE_OPTION) { File file = fc.getSelectedFile();
			 * System.out.println(file); }
			 */
			File file = new File("LevelX.lvl");

			// ~~~~~~~~~~~~DOES NOT READ FOR SOME
			// REASON~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			BufferedReader reader = new BufferedReader(new FileReader(file));

			String delims = "\\s+";

			// Skip first 2 lines
			reader.readLine();
			reader.readLine();
			for (int row = 0; row < 24; row++)
			{
				String line = reader.readLine();
				String[] tokens = line.split(delims);
				for (int column = 0; column < 32; column++)
				{
					level[row][column] = Integer.parseInt(tokens[column]);
				}
			}
			int noObstacles = Integer.parseInt(reader.readLine());
			for (int obstacle = 0; obstacle < noObstacles; obstacle++)
			{
				String line = reader.readLine();
				String[] params = line.split(" ");
				if (params[0].equalsIgnoreCase("Button"))
				{
					int buttonOneCol = Integer.parseInt(params[4]);
					int buttonOneRow = Integer.parseInt(params[5]);
					int buttonTwoCol = Integer.parseInt(params[6]);
					int buttonTwoRow = Integer.parseInt(params[7]);
					int laserCol = Integer.parseInt(params[8]);
					int laserRow = Integer.parseInt(params[9]);

					int buttonOneType = Integer.parseInt(params[1]);
					int buttonTwoType = Integer.parseInt(params[2]);

					int buttonOne = 0, buttonTwo = 0;
					if (buttonOneType == 0)
					{
						buttonOne = 400;
					}
					else if (buttonOneType == 2)
					{
						buttonOne = 401;
					}
					else if (buttonOneType == 4)
					{
						buttonOne = 402;
					}

					if (buttonTwoType == 0)
					{
						buttonTwo = 400;
					}
					else if (buttonTwoType == 2)
					{
						buttonTwo = 401;
					}
					else if (buttonTwoType == 4)
					{
						buttonTwo = 402;
					}

					level[buttonOneRow][buttonOneCol] = buttonOne;
					level[buttonTwoRow][buttonTwoCol] = buttonTwo;
					level[laserRow][laserCol] = 406;
				}
				else if (params[0].equalsIgnoreCase("Switch"))
				{
					int switchCol = Integer.parseInt(params[5]);
					int switchRow = Integer.parseInt(params[6]);
					int laserCol = Integer.parseInt(params[7]);
					int laserRow = Integer.parseInt(params[8]);

					int switchType = Integer.parseInt(params[1]);

					int theSwitch = 0;

					if (switchType == 6)
					{
						theSwitch = 403;
					}
					else if (switchType == 8)
					{
						theSwitch = 404;
					}
					else if (switchType == 10)
					{
						theSwitch = 405;
					}

					level[switchRow][switchCol] = theSwitch;
					level[laserRow][laserCol] = 406;
				}
			}
			reader.close();
		}
		loadDesignState();
	}

	private void loadDesignState()
	{
		state = State.DESIGN;
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(bg, 0, 0, WIDTH, HEIGHT, null);

		if (state == State.MENU)
		{
			g.setColor(Color.BLACK);
			g.fillRect(256, 192, 470, 480);
			g.setColor(Color.WHITE);
			for (int index = 0; index < options.length; index++)
			{
				g.drawString(options[index], WIDTH / 2,
						320 + (index * 96));
			}
		}
		else if (state == State.DESIGN)
		{
			// draw a grid to mark where tiles go
			// draw horisontalLines
			g.setColor(Color.BLACK);
			for (int y = 0; y < 24; y++)
			{
				g.drawLine(0, y * 32, 1024, y * 32);
			}

			for (int x = 0; x < 32; x++)
			{
				g.drawLine(x * 32, 0, x * 32, 768);
			}

			// Draw the level
			for (int row = 0; row < 24; row++)
			{
				for (int col = 0; col < 32; col++)
				{
					int tile = level[row][col];
					BufferedImage tileImage = null;
					// LiquidTile here
					if (tile < 10)
					{
						if (tile == 1)
							tileImage = tiles[0][0];
						else if (tile == 2)
							tileImage = tiles[0][1];
						else if (tile == 3)
							tileImage = tiles[0][2];
					}
					// Tile here
					else if (tile < 100)
					{
						if (tile == 10)
							tileImage = tiles[1][0];
						else if (tile == 11)
							tileImage = tiles[1][1];
						else if (tile == 12)
							tileImage = tiles[1][2];
						else if (tile == 13)
							tileImage = tiles[1][3];
					}
					// Player start here
					else if (tile < 200)
					{
						if (tile == 100)
							tileImage = buoy;
						else if (tile == 101)
							tileImage = grill;
					}
					// Treasure here
					else if (tile < 300)
					{
						if (tile == 200)
							tileImage = treasures[0];
						else if (tile == 201)
							tileImage = treasures[1];
						else if (tile == 202)
							tileImage = treasures[2];
						else if (tile == 203)
							tileImage = treasures[3];
						else if (tile == 204)
							tileImage = treasures[4];
					}
					// Door here
					else if (tile < 400)
					{
						if (tile == 300)
							tileImage = redDoor;
						else if (tile == 301)
							tileImage = blueDoor;
					}
					// Obstacle here
					else if (tile < 500)
					{
						if (tile == 400)
							tileImage = obstacles[0];
						else if (tile == 401)
							tileImage = obstacles[1];
						else if (tile == 402)
							tileImage = obstacles[2];
						else if (tile == 403)
							tileImage = obstacles[3];
						else if (tile == 404)
							tileImage = obstacles[4];
						else if (tile == 405)
							tileImage = obstacles[5];
						else if (tile == 406)
							tileImage = obstacles[6];
					}

					g.drawImage(tileImage, col * 32, row * 32, null);
				}
			}

			if (showTilePanel)
			{
				// draw the tile select pane
				g.setColor(Color.BLACK);
				g.fillRect(896, 0, 128, 768);

				// draw buoy and grill
				g.drawImage(buoy, 912, 16, null);
				g.drawImage(grill, 976, 16, null);

				// draw the tiles
				g.drawImage(tiles[0][0], 912, 48, null);
				g.drawImage(tiles[0][1], 976, 48, null);
				g.drawImage(tiles[0][2], 912, 96, null);

				g.drawImage(tiles[1][0], 912, 160, null);
				g.drawImage(tiles[1][1], 976, 160, null);
				g.drawImage(tiles[1][2], 912, 224, null);
				g.drawImage(tiles[1][3], 976, 224, null);

				// Draw treasures
				g.drawImage(treasures[0], 912, 292, null);
				g.drawImage(treasures[1], 976, 292, null);
				g.drawImage(treasures[2], 912, 358, null);
				g.drawImage(treasures[3], 976, 358, null);
				g.drawImage(treasures[4], 912, 422, null);

				// Draw obstacles
				g.drawImage(obstacles[0], 912, 486, null);
				g.drawImage(obstacles[1], 976, 486, null);
				g.drawImage(obstacles[2], 912, 550, null);
				g.drawImage(obstacles[3], 976, 550, null);
				g.drawImage(obstacles[4], 912, 614, null);
				g.drawImage(obstacles[5], 976, 614, null);
				g.drawImage(obstacles[6], 912, 678, null);

				// Draw the doors
				g.drawImage(redDoor, 912, 704, null);
				g.drawImage(blueDoor, 976, 704, null);

			}
			// Draw a tile by the mouse if it is grabbed
			if (aTileIsGrabbed)
			{
				g.drawImage(grabbedTile, grabbedX - 16, grabbedY - 16, null);
			}

		}
	}

	private void save() throws FileNotFoundException
	{
		// ~~~~~~gives a null pointer exception for some
		// reason~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		/*
		 * int option = fc.showSaveDialog(null); if (option ==
		 * JFileChooser.APPROVE_OPTION) { whereToWrite =
		 * fc.getSelectedFile().getAbsolutePath(); }
		 */
		writer = new PrintWriter(whereToWrite);

		writer.println("32");
		writer.println("24");

		for (int row = 0; row < level.length; row++)
		{
			for (int col = 0; col < level[row].length; col++)
			{
				if (level[row][col] < 400)
					writer.print(level[row][col] + " ");
				else
					writer.print(0 + " ");
			}
			writer.println();
		}

		// Print obstacle stuff
		writer.println(noObstacles);

		for (int obstacle = 0; obstacle < selections.length; obstacle++)
		{
			if (selections[obstacle] != null)
				writer.println(selections[obstacle]);
		}

		// Print legend
		writer.println();

		writer.println("Switch int switchType, int laserType, int laserSize, int switchX, int switchY, int laserX, int laserY");
		writer.println("Button int buttonOneType, int buttonTwoType, int laserType, int laserSize, int buttonOneX,	int buttonOneY, int buttonTwoX,	int buttonTwoY, int laserX, int laserY");
		writer.println("100 - buoy");
		writer.println("101 - grill");
		writer.println("200 - bronze coin");
		writer.println("201 - silver coin");
		writer.println("202 - gold coin");
		writer.println("203 - Red diamond");
		writer.println("204 - blue diamond");
		writer.println("300 - red door");
		writer.print("301 - blue door");

		writer.flush();
	}

	@Override
	public void mouseClicked(MouseEvent arg0)
	{

	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{

	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{

	}

	@Override
	public void mouseMoved(MouseEvent e)
	{

	}

	@Override
	public void keyReleased(KeyEvent arg0)
	{

	}

	@Override
	public void keyTyped(KeyEvent arg0)
	{

	}
}
