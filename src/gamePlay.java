import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * The gamePlay is where all the methods used for 'playing' the game are included.
 * The main function in this class allows this game to be user interactive. Plants are killed 
 * by the Zombies if they are not strategically placed on the grid by the user and visa-versa.
 *
 * @author Sarah Lamonica, Mounica Pillarisetty, Fatima Hashi, Shoana Sharma 
 * @version December 5th, 2018 
 *
 */
public class gamePlay implements Serializable {
	
	private ArrayList<Plant> plants = new ArrayList<Plant>(); //array list for all plants on the board
	private ArrayList<Zombie> zombies = new ArrayList<Zombie>();  //array list for all zombies on the board
	private static char plantType;
	private int nTurns = 0;
	private boolean isAllZombiesDead = true;
	private ArrayList<gamePlayListener> gameListeners; //List of listeners for event model design pattern
	private boolean startGame = false;
	private int nRows, nColumns, sunshine;
	private char[][] board; //board variable, the view reads this when it's updated
	private boolean lvlOneWon,lvlTwoWon,lvlThreeWon;
	int lvl;
	//These stacks take care of undo/redo. 
	private Stack<Plant> plantUndoStack;
	private Stack<Plant> plantRedoStack;
	
	/**
	 * Constructor for the game play
	 * @param nRows is an int that sets the row on the gird
	 * @param nColumns is an int that sets the column on the grid
	 * @param sunshine is an int that shows the sunshine left in the game
	 */
	public gamePlay(int nRows, int nColumns, int sunshine)
	{
		plantUndoStack = new Stack<Plant>();
		plantRedoStack = new Stack<Plant>();
		this.nRows = nRows;
		this.nColumns = nColumns;
		this.sunshine = sunshine;
		plantType = 'p';
		gameListeners = new ArrayList<gamePlayListener>();
		board = new char[6][6];
		lvlOneWon = false;
		lvlTwoWon = false;
		lvlThreeWon = false;
	}
	
	/**
	 * This method adds Gameplay listener event 
	 * @param tttl is a type of game play listener added according to the occured event 
	 */
	public void addGamePlayListener(gamePlayListener tttl) {
        gameListeners.add(tttl);
   	 }
	
	/**
	 * This method checks for the plant's turn 
	 * @param row is an int sets the row on the gird when the plant is placed
	 * @param column is an int sets the column on the grid where the plant is placed
	 * @param plantType is an int could be a sunflower or a peashooter
	 */
	public void plantTurn(int row, int column)
	{
		nTurns++;
		
		//We move the zombies each move.
		if(nTurns == 1)
		{
			moveZombies();
			nTurns = 0;
		}
		
		Object[] options = {"Peashooter - 100", "Sunflower - 50","CherryBomb - 200", "Walnut - 200"};
		int n = JOptionPane.showOptionDialog(null, "Choose your plant type!", "Choice", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[3]);
		
		//Option Dialog so the user can choose their plants.
		if(n == 0)
		{
			plantType = 'p';
		}
		
		if(n == 1)
		{
			plantType = 's';
		}
		if(n == 2)
		{
			plantType = 'c';
		}
		if(n == 3)
		{
			plantType = 'w';
			
		}
		
		//EACH plant has different behaviour! We also have to add to the board[][] variable
		if(sunshine >= 50) {
			if(plantType == 'p' && sunshine >= 100) {
				sunshine -= 100;
				Peashooter p = new Peashooter(100, row, column, false); 
				plantUndoStack.push(p);
				board[row][column] = 'p';
				plants.add(p); //Add a peashooter to the array list
			}
			if(plantType == 'c' && sunshine >= 200)
			{
				sunshine -= 200;
				CherryBomb c = new CherryBomb(200, row, column, false);
				board[row][column] = 'c';
				plantUndoStack.push(c);
				plants.add(c);
			}
			if(plantType == 'w' && sunshine >= 200)
			{
				sunshine -= 200;
				Walnut w = new Walnut(200, row, column, false);
				plantUndoStack.push(w);
				board[row][column] = 'w';
				plants.add(w);
			}

			if(plantType == 's' && sunshine >= 50) {
				if(plantType == 's' && sunshine >= 50) {
					   JDialog.setDefaultLookAndFeelDecorated(true);
					      int response = JOptionPane.showConfirmDialog(null, "Do you want to collect the sun? It gives you 25 sunshines!", "Confirm",
					          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					      if (response == JOptionPane.NO_OPTION) {
					        System.out.println("No button clicked");
					      } else if (response == JOptionPane.YES_OPTION) {
					        System.out.println("Yes button clicked");
					        sunshine += 25;
					      } else if (response == JOptionPane.CLOSED_OPTION) {
					       System.out.println("JOptionPane closed");
					      }
				}
				
				sunshine -= 50;
				Sunflower s = new Sunflower(50, row, column, false);
				board[row][column] = 's';
				plantUndoStack.push(s);
				plants.add(s); //Add a sunflower to the array list
			}
		}
		
		//EVENT MODEL DESIGN PATTERN
		gamePlayEvent e = new gamePlayEvent (this, row, column, plantType, zombies, sunshine, plants, board);
        for (gamePlayListener tttl: gameListeners) tttl.handleGameEvent(e);
	}
	
	/**
	 * This method checks if the plants or zombies win
	 */
	public void plantsOrZombies()
	{
				//variable to check if all the zombies' damage is < 0
				isAllZombiesDead = true;
				for(Zombie z : zombies) {
					//If they reach the house, zombies win
					if(z.getPositionY() == 0) { 
						System.out.println("Zombies reached the house. Plants LOSE");
						JOptionPane.showMessageDialog(null,"Zombies reached the house! \n ZOMBIES WON");
						System.exit(-1);
					}
					if(z.getDmg() > 0)
					{
						isAllZombiesDead = false;
					}
				}
				
				for(Zombie z : zombies)
				{
					for(Plant p : plants)
					{
						//Need to check what kind of plant it is?
						if(p instanceof CherryBomb)
						{
							//Kills all the zombies in the area.
							if(p.getPositionX() - z.getPositionX() == 1 || p.getPositionY() - z.getPositionY() <= 1)
							{
								z.setDmg(0);
								p.setEaten();
							}
						}
						if(p instanceof Walnut)
						{
							//checking if the zombie is in the same grid space
							if(p.getPositionX() == z.getPositionX() && p.getPositionY() == z.getPositionY())
							{
								
								Walnut w = (Walnut) p;
								//Pauses the zombie for two full turns
								if(w.getLife() < 2)
								{
									z.setWalnutStatus(true);
									w.setLife();
									z.setPositionX(p.getPositionX());
									z.setPositionY(p.getPositionY());
								}
								else {
									z.setWalnutStatus(false);
								}
							}
						}
						
						//If the zombies and peashooters are in the same row, plants will shoot at the zombies &
						//Decrease their lives
						if(z.getPositionX() == p.getPositionX())
						{
							if(p instanceof Peashooter)
							{
								z.setDmg(z.getDmg() - 100);
								if(z.getDmg() <= 0)
								{
									z.setEaten();
									board[z.getPositionX()][z.getPositionY()] = ' ';
								}
								if(z.getPositionY() == p.getPositionY())
								{
									p.setEaten();
								}
							}
						}
					}
				}
				
				if(isAllZombiesDead)
				{
					
					if(lvl == 1)
					{
						JOptionPane.showMessageDialog(null,"YOU BEAT LEVEL 1! Plants defeated all the zombies!\nPress on 'Game' to play Level 2");
						lvlOneWon = true;
					}
					if(lvl == 2 && lvlOneWon)
					{
						JOptionPane.showMessageDialog(null,"YOU BEAT LEVEL 2! Plants defeated all the zombies!\nPress on 'Game' to play Level 3");
						lvlTwoWon = true;
					}
					if(lvl == 3&& lvlTwoWon)
					{
						JOptionPane.showMessageDialog(null,"YOU BEAT LEVEL 3! Plants defeated all the zombies!\nPlants defeated all the zombies!");
						lvlThreeWon = true;
					}
				}
	}
	
	/**
	 * This is getter method for level 1
	 * @return a boolean true if level 1 won, false otherwise
	 */
	public boolean getLvl1() {
		return lvlOneWon;
	}
	
	/**
	 * This is getter method for level 2
	 * @return a boolean true if level 2 won, false otherwise
	 */
	public boolean getLvl2() {
		return lvlTwoWon;
	}
	
	/**
	 * This is getter method for level 3
	 * @return a boolean true if level 3 won, false otherwise
	 */
	public boolean getLvl3() {
		return lvlThreeWon;
	}
		
	/**
	 * This method moves every zombie one space 
	 */
	public void moveZombies() {
		for(Zombie p: zombies) {
			if(!p.walnutStatus() && !p.getEaten()) {
				p.move();
				board[p.getPositionX()][p.getPositionY()] = p.getType();
				board[p.getPositionX()][p.getPositionY() + 1] = ' ';
			}
		}
	}
	
	/**
	 * This is a getter method for the board
	 * @return a gird of character
	 */
	public char[][] getBoard() {
		return board;
	}
	
	/**
	 * This is a getter method to get the type of plant that is being placed into the grid
	 * @return char containing a peashooter, a sunflower or a cherry bomb 
	 */
	public char getPlantType() {
		return plantType;
	}

	/**
	 * This is a setter method for the plant type
	 * @param char s returns the sunflower in this case
	 */
	public void setPlantType(char s) {
		this.plantType = s;
	}

	/**
	 * This method keeps track of the affects on zombie when the game state
	 * is zombie time.
	 * @param numZombies the number of zombies in the game at that moment
	 */
	public void lvlThree(int numZombies) {
		lvl = 3;
		clr();
		sunshine = 800;
		for(int i = 0; i < board.length; i++)
		{
			for(int j = 0; j < board.length; j++) 
			{
				board[i][j] = ' ';
			}
		}
		//Random variable for placing zombies at a random grid space
		Random r = new Random();
		flagZombieIncoming();
		
		if(!startGame) {
			for(int i = 0; i < numZombies; i++) {
				int random = r.nextInt(nRows);
				PylonZombie z = new PylonZombie(random, (nRows -1), false, 200, 'x', false);
				
				board[random][nRows - 1] = 'x';
				zombies.add(z);
			}
			
			for(int i = 0; i < numZombies; i++) {
				int random = r.nextInt(nRows);
				NormalZombie z = new NormalZombie(random, (nRows -1), false, 100, 'z', false);
				
				board[random][nRows - 1] = 'z';
				zombies.add(z);
			}
			
			startGame = true;
		}
	}
	
	
	/**
	 * This method laces a flag zombie at the beginning of the game in the centre of the board
	 */
	public void flagZombieIncoming() {
		if(!startGame) {
			FlagZombie f = new FlagZombie(2,5, false, 100, 'f', false);
			board[2][5] = 'f';
			zombies.add(f);
		}
		
	}
	
	/**
	 * This method undoes the latest player move
	 */
	public void undo()
	{
		//Different plants need to update score
		if(plantUndoStack.peek() instanceof Peashooter)
		{
			sunshine += 100;
		}
		if(plantUndoStack.peek() instanceof Sunflower)
		{
			sunshine += 50;
		}
		if(plantUndoStack.peek() instanceof Walnut)
		{
			sunshine += 200;
		}
		if(plantUndoStack.peek() instanceof CherryBomb)
		{
			sunshine += 200;
		}
		
		//Taking the plant off the board and placing it into the stack.
		board[plantUndoStack.peek().getPositionX()][plantUndoStack.peek().getPositionY()] = ' ';
		plants.remove(plantUndoStack.peek());
		plantRedoStack.push(plantUndoStack.pop());
	}
	
	/**
	 * This method places the previously undid plants back in the game
	 */
	public void redo()
	{
		//Different plants need to redo certain behaviours on the board, so we're checking that here and
		//updating the board accordingly
		if(plantRedoStack.peek() instanceof Peashooter)
		{
			board[plantRedoStack.peek().getPositionX()][plantRedoStack.peek().getPositionY()] = 'p';
			sunshine -= 100;
		}
		if(plantRedoStack.peek() instanceof Sunflower)
		{
			board[plantRedoStack.peek().getPositionX()][plantRedoStack.peek().getPositionY()] = 's';
			sunshine -= 50;
		}
		if(plantRedoStack.peek() instanceof Walnut)
		{
			board[plantRedoStack.peek().getPositionX()][plantRedoStack.peek().getPositionY()] = 'w';
			sunshine -= 200;
		}
		if(plantRedoStack.peek() instanceof CherryBomb)
		{
			board[plantRedoStack.peek().getPositionX()][plantRedoStack.peek().getPositionY()] = 'c';
			sunshine -= 200;
		}
		
		
		plants.add(plantRedoStack.peek());
		plantUndoStack.push(plantRedoStack.pop());
	}

	/**
	 * This method saves the current state of the game
	 * @throws IOException
	 */
	public void save() throws IOException
	{
		FileOutputStream fileOut = new FileOutputStream("file.txt");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(plants);
		out.writeObject(zombies);
		out.writeObject(board);
		out.writeObject(plantUndoStack);
		out.writeObject(plantRedoStack);
		out.writeObject(lvl);
		out.writeObject(sunshine);
	}
	
	/**
	 * This ethod loads the saved feature.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void load() throws IOException, ClassNotFoundException
	{
		 FileInputStream fileIn = new FileInputStream("file.txt");
		 ObjectInputStream in = new ObjectInputStream(fileIn);
		 
		 plants = (ArrayList<Plant>) in.readObject();
		 zombies = (ArrayList<Zombie>) in.readObject();
		 board = (char[][]) in.readObject();
		 plantUndoStack = (Stack<Plant>) in.readObject();
		 plantRedoStack = (Stack<Plant>) in.readObject();
		 lvl = (int) in.readObject();
		 sunshine = (int) in.readObject();
		 in.close();
		 fileIn.close();
		 
	}
	
	/**
	 * This method initializes level one
	 */
	public void lvlOne()
	{
		lvl = 1;
		sunshine = 800;
		clr();

		Random r = new Random();	
		for(int i = 0; i < board.length; i++)
		{
			for(int j = 0; j < board.length; j++) 
			{
				board[i][j] = ' ';
			}
		}
		
		for(int i = 0; i < 2; i++) {
			int random = r.nextInt(nRows);
			NormalZombie z = new NormalZombie(random, (nRows -1), false, 100, 'z', false);
			
			board[random][nRows - 1] = 'z';
			zombies.add(z);
		}
	}
	
	/**
	 * This method intializes level two
	 */
	public void lvlTwo()
	{
		lvl = 2;
		sunshine = 800;
		clr();
		
		for(int i = 0; i < board.length; i++)
		{
			for(int j = 0; j < board.length; j++) 
			{
				board[i][j] = ' ';
			}
		}
		
		flagZombieIncoming();
		Random r = new Random();
		for(int i = 0; i < 2; i++) {
			int random = r.nextInt(nRows);
			NormalZombie z = new NormalZombie(random, (nRows -1), false, 100, 'z', false);
			
			board[random][nRows - 1] = 'z';
			zombies.add(z);
		}
	}
	
	/**
	 * This method clr the array of all plants and zombies, use this later for levels 
	 */
	public void clr()
	{
		plants.removeAll(plants); 
		zombies.removeAll(zombies);
	}
	



}