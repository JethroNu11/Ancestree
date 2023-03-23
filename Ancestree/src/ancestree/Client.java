/*
 * Title:          Assignment2ICT373, Client
 * Author:         Geoffrey Lawson (33176674)
 * Date:           22/03/2021
 * File Name:      Client.java
 * Purpose:            
 * Conditions:         
 */
package ancestree;

/**
* PLANNED UPDATES: Things I'm going to do but haven't implemented yet.
* show main person and their age next to profile button on main menu screen (figure out how to get current date from machine)
* change main menu button to say 'load profile X' or 'create profile X' depending on if it has details or not
* set validation on person inputs- parent cannot be born after child
* implement serialization
* add check box to person form for date of death - deceased, date unknown
* add check to main menu buttons to validate that the given profile number exists- go to Create New for profile if it doesn't.
* find a way to set a max number of rows in the tree, and if the user tries to go up another generation, the bottom row is cut off from view.
*/

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author Geoffrey Lawson (33176674)
 */
public class Client extends Application {

	//================================================================================================================
	//											DECLARE GLOBAL VARIABLES
	//================================================================================================================
	
	
	/**
	 * ArrayList that stores the ID information for each box in the main GridPane board. Indexes are directly related to BOX_XY ArrayList's indexes, so 
	 *	the BOX_ID information for a given BOX_XY.get(n) index is simply BOX_ID.get(n).
	 * [0]: Type of box.
	 * [1]: Further information for the box, depending on the [0] value.
	 * Relationships between [0] and [1] are as follows:
	 *	---------------------------------------------------------------------------------------------------------------------
	 *	|	Where [0] is:	|	[1] will be:	|							Notes:											|
	 *	---------------------------------------------------------------------------------------------------------------------
	 *	|	"void"			|	"-1"			|	Default values set when the box is created.								|
	 *	|	"connector"		|	Connector Type	|	Directly relates to a '.png' file in the src/images/connectors folder.	|
	 *	|	"person"		|	Person ID		|	ID identifying the relationship between this box and the main user.		|
	 *	|	"empty"			|	Person ID		|	ID identifying the relationship between this box and the main user.		|
	 *	|	"up_arrow"		|	Person ID		|	ID of the person in the box directly underneath this box.				|
	 *	|	"down_arrow"	|	Person ID		|	ID of the person in the box directly above this box.					|
	 *	---------------------------------------------------------------------------------------------------------------------
	 */
	private static final ArrayList<String[]> BOX_ID = new ArrayList<>();
	
	/**
	 * ArrayList that stores the XY coordinates for each box in the main GridPane board. Indexes are directly related to BOX_ID ArrayList's indexes, so 
	 *	the BOX_ID information for a given BOX_XY.get(n) index is simply BOX_ID.get(n).
	 * [0]: X value (column index of the given box). X value for the furthest left column is 0. 
	 * [1]: Y value (row index of the given box). Y value for the top row is 0.
	 */
	private static final ArrayList<Integer[]> BOX_XY = new ArrayList<>();
	
	/**
	 * Actual ArrayList of people from the file. Includes everyone, even if they're not actively being shown on the tree.
	 */
	public static ArrayList<Person> people = new ArrayList<>();
	
	/**
	 * Details of the current highest selected person.
	 * [0]: Index in the people ArrayList that this ancestor is located. Default is -1.
	 * //[0]: Index in the BOX_ID and BOX_XY ArrayLists that this ancestor is located. Default is -1.
	 * [1]: Column of this ancestor in relation to the user.
	 * [2]: Number of rows above user that the ancestor is located.
	 * [3]: Maximum number of columns to the left of the main user that the ancestor path has, used for setting the main user's column.
	 */
	private static final int[] TOP_ANCESTOR = new int[4];
	
	/**
	 * Index in the people ArrayList of the current lowest selected person.
	 */
	private static int LOW_DESCENDANT;
	
	/**
	 * Total size of the main tree grid.
	 * [0] is number of columns
	 * [1] is number of rows
	 */
	private static final int[] TREE_XY = new int[2];
	
	/**
	 * Denotes if the currently loaded profile has been modified since being loaded.
	 * Set to false on profile load, set to true when a person form is successfully applied.
	 * Used to decide whether to prompt the user to save or not when they're trying to change profile or close the program.
	 */
	private static boolean changesMade; 
	
	//Profile number that is currently loaded into the people ArrayList.
	private static int currentProfile;
	
	//Main screen in the window. Global so that the different panes (primarily top, centre and bottom) can be changed from different methods
	private static BorderPane root;
	
	//enable to print 'debug()' statements
	private static void debug(String output){
		boolean debug = true;
		if(debug){
			System.out.println(output);
		}
	}
	
	
	//================================================================================================================
	//											SET ELEMENT STYLES
	//================================================================================================================
	
	
	/**
	 * Sets the given Node with the chosen CSS style. This may define the item's colour, shape, clicking action and/or hovering effect.
	 * @pre		Node object has been declared.
	 * @param	item	Node object that will have the chosen style applied to it.
	 * @param	style	String that defines which style should be applied to the specified Node.
	 * @post	Node will display on screen in the chosen style.
	 */
	private static void nodeStyle(Node item, String style){
		if(item instanceof Button){				//item is a Button object
			//debug("    Is Button");
			switch(style){
				case "menu":
					//debug("        Setting Menu style");
					((Button) item).setPrefSize(150, 30);
					item.getStyleClass().add("unselected_button");
					item.setOnMouseEntered(e -> {
						item.getStyleClass().remove("unselected_button");
						item.getStyleClass().add("selected_button");
					});
					item.setOnMouseExited(e -> {
						item.getStyleClass().remove("selected_button");
						item.getStyleClass().add("unselected_button");
					});
					break;
				default:
					//debug("        Setting Default style");
					item.getStyleClass().add(style);
					break;
			}
		}else if(item instanceof GridPane){		//item is a GridPane box.
			//debug("    Is GridPane");
			switch(style){
				case "entity":	//regular box. Used for setting hover effect.
					item.getStyleClass().add("unselected_box");
					item.setOnMouseEntered(e -> {
						item.getStyleClass().remove("unselected_box");
						item.getStyleClass().add("selected_box");
					});
					item.setOnMouseExited(e -> {
						item.getStyleClass().remove("selected_box");
						item.getStyleClass().add("unselected_box");
					});
					break;
				case "empty":	//box without a person's details in it.
					//debug("        Setting Empty Style");
					item.setOnMouseClicked((MouseEvent e) -> {
						debug("clicked empty, index: " + item.getId() + ", ID: " + BOX_ID.get(Integer.parseInt(item.getId()))[1]);
						switchContent("person-form", BOX_ID.get(Integer.parseInt(item.getId()))[1]);
					});	
					break;
				case "person":	//box with a person's details in it.
					//debug("        Setting Person Style");
					item.setOnMouseClicked((MouseEvent e) -> {
						debug("clicked person, index: " + item.getId());
						switchContent("select-menu", BOX_ID.get(Integer.parseInt(item.getId()))[1]);
					});	
					break;
				case "up_arrow":	//arrow above a person that displays their parents when clicked. Item ID is the box's index in the BOX_ID ArrayList.
					//set up_arrow click action to the box index of the current person
					item.setOnMouseClicked( (MouseEvent e) -> {
						debug("clicked up arrow, index: " + item.getId() + ", BOX_ID.get(id)[1]: " + BOX_ID.get(Integer.parseInt(item.getId()))[1]);
						debug("+1: index: " + (Integer.parseInt(item.getId())+1) + ", BOX_ID.get(id)[1]: " + BOX_ID.get(Integer.parseInt(item.getId())+1)[1]);
						LOW_DESCENDANT = -1;
						TOP_ANCESTOR[0] = getPersonIndex(BOX_ID.get(Integer.parseInt(item.getId()))[1]);
						switchContent("tree", String.valueOf(currentProfile));
					});
					item.getStyleClass().add(style);
					break;
				case "down_arrow":	//arrow below a person that displays their children and spouse when clicked. Item ID is the box's index in the BOX_ID ArrayList.
					//set down_arrow click action to the box index of the current person
					item.setOnMouseClicked( (MouseEvent e) -> {
						debug("clicked down arrow, index: " + item.getId() + ", BOX_ID.get(id)[1]: " + BOX_ID.get(Integer.parseInt(item.getId()))[1]);
						LOW_DESCENDANT = getPersonIndex(BOX_ID.get(Integer.parseInt(item.getId()))[1]);
						TOP_ANCESTOR[0] = -1;
						switchContent("tree", String.valueOf(currentProfile));
					});
					item.getStyleClass().add(style);
					break;
				case "connector":	//one of the various pipe connectors in the tree connecting one box to another.
				case "void":		//box with nothing in it.
					//debug("        Setting Void/Connector Style");
					item.setOnMouseClicked((MouseEvent e) -> {
						debug("clicked void or connector, index: " + item.getId());
						switchContent("select-menu", "-1");
					});	
					break;
				default:			//unknown box type, just setting its style to the given value.
					//debug("        Setting Default Style (itemID: " + item.getId() + ")");
					item.getStyleClass().add(style);
					item.setOnMouseClicked((MouseEvent e) -> {
						debug("clicked default styled cell, index: " + item.getId());
						switchContent("select-menu", "-1");
					});
					break;
			}
		}else{						//item type is unknown
			debug("    Is Unknown Type");
			item.getStyleClass().add(style);
		}
	}
	
	
	//================================================================================================================
	//											GET ELEMENT INDEX IN ARRAYLIST
	//================================================================================================================
	
	
	/**
	 * Finds the index of the given person's ID in the people ArrayList.
	 * @param	ID	String to look for while searching through the people ArrayList, using the getID() method. 
	 * @return	Index of the given ID in the ArrayList, or -1 if it cannot be found.
	 */
	private static int getPersonIndex(String ID){
		int i, personIndex = -1;
		
		for(i=0;i<people.size() && personIndex == -1;i++){
			//debug("comparing to " + people.get(i).getID());
			if(people.get(i).getID().equals(ID)){
				//debug("matched");
				personIndex = i;
			}
		}
		
		return(personIndex);
	}
	
	/**
	 * Finds the index of the given person's ID in the BOX_ID ArrayList.
	 * @param	ID	String to look for while searching through the BOX_ID ArrayList, querying the [1] value. 
	 *				Only results with a [0] value of "empty" or "person" are accepted.
	 * @return	Index of the given ID in the BOX_ID ArrayList, or -1 if it cannot be found.
	 */
	private static int getBoxIndex(String ID){
		int i, boxIndex = -1;
		for(i=0;i<BOX_ID.size() && boxIndex == -1;i++){
			if(BOX_ID.get(i)[1].equals(ID) && ((BOX_ID.get(i)[0].equals("person")) || (BOX_ID.get(i)[0].equals("empty")))){
				boxIndex = i;
			}
		}
		return(boxIndex);
	}
	
	/**
	 * Finds the index of the given XY coordinates in the BOX_ID ArrayList.
	 * @param	x	Integer that represents the X coordinate of the box (the column).
	 * @param	y	Integer that represents the Y coordinate of the box (the row).
	 * @return	Index of the given XY coordinates in the BOX_ID ArrayList, or -1 if it cannot be found.
	 */
	private static int getBoxIndex(int x, int y){
		int i, boxIndex = -1;
		for(i=0;i<BOX_ID.size() && boxIndex == -1;i++){
			if((BOX_XY.get(i)[0] == x) && (BOX_XY.get(i)[1] == y)){
				boxIndex = i;
			}
		}
		return(boxIndex);
	}
	
	//================================================================================================================
	//											INITIALISE GRIDS AND ELEMENTS
	//================================================================================================================
	
	
	/**
	 * Sets the people ArrayList with all the people in the given profile.
	 * @pre		The profileID passed in has a valid profile file in the ../src/profiles folder.
	 * @param	profileFile		String with the file path to the selected profile.
	 * @post	The people ArrayList is populated with people from the given file.
	 */
	private static void buildProfile(){
		people.clear();
		BOX_ID.clear();
		BOX_XY.clear();
		TOP_ANCESTOR[0] = -1;
		LOW_DESCENDANT = -1;
		
		String[] peopleIn;
		String filePath = "src/profiles/" + currentProfile + '/';
		File profile = new File(filePath);
		FileInputStream personIn;
		ObjectInputStream inStream;
		Person person;
		
		try{
			peopleIn = profile.list();
			for (String personFileName : peopleIn) {
				debug("file name: " + personFileName);
				personIn = new FileInputStream("src/profiles/" + currentProfile + '/' + personFileName);
				inStream = new ObjectInputStream(personIn);
				person = (Person)inStream.readObject();
				if(person != null){
					people.add(person);
				}
				//people.add(person);
				inStream.close();
				personIn.close();
			}
		}catch(FileNotFoundException e){
			System.out.println("Error: File Not Found (" + filePath + ").");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a grid within the GridPane with the given number of columns and rows, and sets each cell to the same size.
	 * @pre		The GridPane object is declared, the integers are greater than 0.
	 * @param	board			GridPane object that will have the grid built inside it.
	 * @param	numberColumns	Integer representing the total number of columns in the grid.
	 * @param	numberRows		Integer representing the total number of rows in the grid.
	 * @param	isTree			Boolean value that denotes if the grid is to be the main tree or not.
	 * @post	The GridPane object has (numberColumns*numberRows) cells, each sharing the same width and height values.
	 */
	private static void fillGrid(GridPane board, int numberColumns, int numberRows, boolean isTree){
		int i;
		
		if(isTree){
			BOX_ID.clear();
			BOX_XY.clear();
		}
		
		RowConstraints rc = new RowConstraints();
		ColumnConstraints cc = new ColumnConstraints();
		
		rc.setPercentHeight(100d / numberRows);
		cc.setPercentWidth(100d / numberColumns);
		
		for(int x = 0; x < numberColumns; x++){
			for (int y = 0; y < numberRows; y++) {
				GridPane box = new GridPane();
				GridPane.setConstraints(box, x, y);
				box.setId("-1");
				board.getChildren().add(box);
				if(isTree){
					BOX_ID.add(new String[]{"void", "-1"});
					BOX_XY.add(new Integer[]{x, y});
				}
			}
		}
		for(i = 0;i<numberRows;i++){
			board.getRowConstraints().add(rc);
		}
		
		for(i = 0;i<numberColumns;i++){
			board.getColumnConstraints().add(cc);
		}
		debug("CONSTRAINTS:");
		debug("    Columns: " + cc.toString());
		debug("    Rows: " + rc.toString());
	}
	
	/**
	 * Sets the element in the BOX_ID ArrayList at the index of the given XY grid coordinates with the given ID and boxType.
	 * @pre		The BOX_ID and BOX_XY ArrayLists have been fully built, and the XY coordinates match a value in the BOX_XY ArrayList.
	 * @param	ID		String that denotes the ID of the box. Used to set BOX_ID[1].
	 * @param	boxType	String that denotes the box type (empty, person, connector, arrow or void). Used to set BOX_ID[0].
	 * @param	x		Integer that represents the column of the box in the grid. Compared to the current value of BOX_XY[0] in the loop.
	 * @param	y		Integer that represents the row of the box in the grid. Compared to the current value of BOX_XY[1] in the loop.
	 * @post	The BOX_ID values at the index that matches the given coordinates are overwritten with the given ID and boxType values.
	 */
	private static void setBox(String ID, String boxType, int x, int y){
		boolean boxSet = false;
		for(int i=0;i<BOX_ID.size() && !boxSet;i++){
			if(BOX_XY.get(i)[0] == x && BOX_XY.get(i)[1] == y){
				//debug("found match with index " + i);
				BOX_ID.get(i)[0] = boxType;
				if(boxType.equals("person") && ID.equals("")){
					BOX_ID.get(i)[1] = "0";
				}else{
					BOX_ID.get(i)[1] = ID;
				}
				boxSet = true;
			}
		}
	}
	
	/**
	 * Identifies the boxes that need connectors added to them to link people in the grid, and sets those boxes with the necessary connector type.
	 * @pre		All people that are going to be displayed on the grid have had the necessary boxes assigned to them.
	 * @post	All the boxes between people in the grid have been assigned the relevant connector type.
	 */
	private static void generateConnectors(){
		int motherBoxIndex, fatherBoxIndex;
		int i, j;
		int personIndex;
		int leftCol, rightCol, numChildren, connectorRow, aboveIndex, belowIndex, currentGen;
		int childCol;
		String[] box;
		String connectorType;
		
		String[][] connArr = new String[][]{
			{"upper_T", "top_right_L", "top_left_L"},
			{"lower_T", "bottom_right_L", "bottom_left_L"},
			{"plus", "right_T", "left_T"}
		};
		
		int firstCon, secondCon;
		
		for(i=0;i<BOX_ID.size();i++){
			box = BOX_ID.get(i);
			if((box[0].equals("person")) || (box[0].equals("empty"))){
				currentGen = getGeneration(box[1]);
				if((currentGen <= 0) && (LOW_DESCENDANT != -1)){	//descendent or main user, and there is a descendant path displayed
					if(!(box[1].endsWith("a")) && people.get(LOW_DESCENDANT).getID().contains(box[1])){		//person is in low_desc path and is not a spouse
						leftCol = BOX_XY.get(i)[0]; //set the furthest left column to left parent's column
						rightCol = BOX_XY.get(getBoxIndex(box[1] + 'a'))[0];	//set the furthest right column to the spouse's column
						
						connectorRow = (BOX_XY.get(i)[1])+1;	//set the row that connectors will be placed
						numChildren = getChildrenCount(box[1]);	//get the number of children that the given person has
						debug("leftCol: " + leftCol + ", rightCol: " + rightCol + ", connectorRow: " + connectorRow + ", numChildren: " + numChildren);
						for(j=0;j<numChildren;j++){	//loop through the number of children to set the left and right columns, if necessary
							childCol = BOX_XY.get(getBoxIndex(box[1] + 'b' + j))[0];
							if(childCol < leftCol){
								leftCol = childCol;
							}else if(rightCol < childCol){
								rightCol = childCol;
							}
						}
						
						debug("Going through columns " + leftCol + " to " + rightCol);
						for(j=leftCol;j<=rightCol;j++){	//loop through the spaces from left to right
							firstCon = -1;
							secondCon = 0;
							aboveIndex = getBoxIndex(j, (connectorRow-1));
							belowIndex = getBoxIndex(j, (connectorRow+1));
							
							if(!((aboveIndex == -1) || (belowIndex == -1))){
								if(j==leftCol){
									secondCon++;
								}else if(j==rightCol){
									secondCon = secondCon+2;
								}
								if((BOX_ID.get(aboveIndex)[0].equals("person")) || (BOX_ID.get(aboveIndex)[0].equals("empty"))){	//box above has a person
									if((BOX_ID.get(aboveIndex)[1].equals(box[1])) || (BOX_ID.get(aboveIndex)[1].equals(box[1] + 'a'))){	//that box is either the current person or their spouse
										firstCon++;
									}
								}
								if((BOX_ID.get(belowIndex)[0].equals("person")) || (BOX_ID.get(belowIndex)[0].equals("empty"))){	//box below has a person
									if(!(BOX_ID.get(belowIndex)[1].endsWith("a"))){	//box beneath is not a spouse
										firstCon = firstCon+2;
									}
								}
								if(firstCon == -1){
									connectorType = "horizontal_line";
								}else{
									connectorType = connArr[firstCon][secondCon];
								}
								debug("setting connector: " + connectorType + " at XY: " + j + ", " + connectorRow);
								setBox(connectorType, "connector", j, connectorRow);
							}
						}
						
						
					}else if((!(box[1].endsWith("a"))) && (!(box[0].equals("empty")))){	//place downwards arrow, not if they're a spouse
						debug("setting down arrow at: " + BOX_XY.get(i)[0] + ", " + ((BOX_XY.get(i)[1])+1));
						setBox(box[1], "down_arrow", BOX_XY.get(i)[0], (BOX_XY.get(i)[1])+1);
					}
				}else if(currentGen == 0){
					setBox(people.get(getPersonIndex(box[1])).getID(), "down_arrow", BOX_XY.get(i)[0], (BOX_XY.get(i)[1])+1);
					debug("other box details: " + BOX_XY.get(i)[0] + ", " + (BOX_XY.get(i)[1])+1);
				}
				if(currentGen >= 0 && box[0].equals("person")){	//ancestor or main user
					debug("connectors for person ID at generation >=0: " + box[1]);
					personIndex = getPersonIndex(box[1]);
					motherBoxIndex = getBoxIndex(people.get(personIndex).getMotherID());
					fatherBoxIndex = getBoxIndex(people.get(personIndex).getFatherID());
					if(motherBoxIndex == -1 && fatherBoxIndex == -1){
						debug("setting up_arrow, box details: " + people.get(personIndex).getID() + ", up_arrow, " + BOX_XY.get(i)[0] + ", " + ((BOX_XY.get(i)[1])-1));
						setBox(people.get(personIndex).getID(), "up_arrow", BOX_XY.get(i)[0], (BOX_XY.get(i)[1])-1);	//set arrow above person
					}else{
						for(j = (BOX_XY.get(motherBoxIndex)[0]); j<=BOX_XY.get(fatherBoxIndex)[0]; j++){
							if(j == BOX_XY.get(i)[0]){
								connectorType = "lower_T";
							}else if(j == BOX_XY.get(fatherBoxIndex)[0]){
								connectorType = "top_left_L";
							}else if(j == BOX_XY.get(motherBoxIndex)[0]){
								connectorType = "top_right_L";
							}else{
								connectorType = "horizontal_line";
							}

							setBox(connectorType, "connector", j, (BOX_XY.get(i)[1])-1);
						}
					}
				}
			}
		}
	}

	/**
	 * Set the boxes for the default 7 people in the grid (Main user, both parents and all four grandparents). Main user column is shifted right if the
	 * ancestor path requires extra columns to the left.
	 * @pre		The TOP_ANCESTOR information has been set if required, and the BOX_ID and BOX_XY ArrayLists have been fully populated.
	 * @post	The main user has been assigned to the relevant box in the grid, their parents (if available) have been added and their 
	 *			grandparents (if available) have been added. Any missing family member has an empty space assigned and the tree branch ends with them.
	 */
	private static void displayDefaultPeople(){
		
		int userCol = 4, userRow = 5;
		
		if(TOP_ANCESTOR[0] != -1){	//move main user to the correct grid
			if(TOP_ANCESTOR[3] < -3){
				userCol -= (TOP_ANCESTOR[3] + 3);
			}
			userRow = TOP_ANCESTOR[2];
		}else if(LOW_DESCENDANT != -1){
			if(TREE_XY[0] > 9){
				userCol += (TREE_XY[0]-7)/2;
			}
		}
		setBox("0", "person", userCol, userRow);
		
		//set mother and her parents
		if(getPersonIndex("1") == -1){
			setBox("1", "empty", (userCol - 2), (userRow - 2));
		}else{
			setBox("1", "person", (userCol - 2), (userRow - 2));
			if(getPersonIndex("11") == -1){
				setBox("11", "empty", (userCol - 3), (userRow - 4));
			}else{
				setBox("11", "person", (userCol - 3), (userRow - 4));
			}
			if(getPersonIndex("12") == -1){
				setBox("12", "empty", (userCol - 1), (userRow - 4));
			}else{
				setBox("12", "person", (userCol - 1), (userRow - 4));
			}
		}
		
		if(getPersonIndex("2") == -1){
			setBox("2", "empty", (userCol + 2), (userRow - 2));
		}else{
			setBox("2", "person", (userCol + 2), (userRow - 2));
			if(getPersonIndex("21") == -1){
				setBox("21", "empty", (userCol + 1), (userRow - 4));
			}else{
				setBox("21", "person", (userCol + 1), (userRow - 4));
			}
			if(getPersonIndex("22") == -1){
				setBox("22", "empty", (userCol + 3), (userRow - 4));
			}else{
				setBox("22", "person", (userCol + 3), (userRow - 4));
			}
		}
		
	}
	
	/**
	 * Assign the boxes for all relevant family members up to the location of the currently selected ancestor in the TOP_ANCESTOR variable.
	 * @pre		The TOP_ANCESTOR variable has had values assigned to all fields.
	 * @post	The boxes for all family members up to the highest ancestor have been assigned to the relevant people.
	 */
	private static void displayAncestors(){
		debug("TOP_ANCESTOR: " + TOP_ANCESTOR[0] + ", " + TOP_ANCESTOR[1] + ", " + BOX_ID.get(TOP_ANCESTOR[0])[1]);
		ArrayList<String> idSegments = generateSegments(people.get(TOP_ANCESTOR[0]).getID());
		int i;
		for(i=0;i<idSegments.size();i++){
			debug("segment " + i + ": " + idSegments.get(i));
		}
		
		String grandparent = idSegments.get(0) + idSegments.get(1);
		int gpBox = getBoxIndex(grandparent);
		int currentCol = BOX_XY.get(gpBox)[0], currentRow = BOX_XY.get(gpBox)[1];
		String currentAncestor = grandparent, boxType;
		
		debug("grandparent: " + grandparent + ", gpBox: " + gpBox);
		
		for(i=2;i<idSegments.size();i++){
			debug("setting " + currentAncestor + "'s parents");
			if(getPersonIndex((currentAncestor + "1")) == -1){
				boxType = "empty";
			}else{
				boxType = "person";
			}
			setBox((currentAncestor + "1"), boxType, (currentCol-1), (currentRow-2));

			if(getPersonIndex((currentAncestor + "2")) == -1){
				boxType = "empty";
			}else{
				boxType = "person";
			}
			setBox((currentAncestor + "2"), boxType, (currentCol+1), (currentRow-2));
			
			if(idSegments.get(i).equals("1")){
				currentCol--;
			}else{
				currentCol++;
			}
			currentRow -= 2;
			currentAncestor += idSegments.get(i);
		}
		//now diplay the parents for the current ancestor
		debug("setting last ancestor's parents.");
		if(getPersonIndex((currentAncestor + "1")) == -1){
			boxType = "empty";
		}else{
			boxType = "person";
		}
		setBox((currentAncestor + "1"), boxType, (currentCol-1), (currentRow-2));
		
		if(getPersonIndex((currentAncestor + "2")) == -1){
			boxType = "empty";
		}else{
			boxType = "person";
		}
		setBox((currentAncestor + "2"), boxType, (currentCol+1), (currentRow-2));
		
	}
	
	/**
	 * Assign the boxes for all relevant family members down to the location of the currently selected descendant in the LOW_DESCENDANT variable.
	 * @pre		The LOW_DESCENDANT variable has been initialised with the relevant information.
	 * @post	The boxes down to the lowest ancestor have all been assigned to the relevant people
	 */
	private static void displayDescendants(){
		ArrayList<String> idSegments = generateSegments(people.get(LOW_DESCENDANT).getID());
		int i, j, centreCol, startCol, peopleInRow, placedChildren, currentRow, endCol, selectedChild;
		String currentDescendant;
		String boxType;
		
		currentRow = BOX_XY.get(getBoxIndex("0"))[1];
		
		//show main users spouse
		if(getPersonIndex("0a") == -1){
			boxType = "empty";
		}else{
			boxType = "person";
		}
		setBox("0a", boxType, (BOX_XY.get(getBoxIndex("0"))[0])+2, currentRow);
		debug("set spouse");
		//then start loop for the rest of the ID.
		centreCol = (TREE_XY[0]/2)+1;
		currentDescendant = "";
		for(i=0;i<idSegments.size();i++){
			currentRow += 2;
			selectedChild = -1;
			currentDescendant += idSegments.get(i);
			peopleInRow = getChildrenCount(currentDescendant);
			debug("    people in row: " + peopleInRow);
			if(peopleInRow == 0){	//no children, just display an empty box in the centre.
				setBox((currentDescendant + "b0"), "empty", centreCol, currentRow);
			}else{
				if(i != (idSegments.size()-1)){
					peopleInRow++;	//for someone's spouse
					debug("selected child set to int: " + idSegments.get((i+1)).charAt(1) + ", char(0): " + idSegments.get((i+1)).charAt(0) + ", string is: " + idSegments.get((i+1)));
					selectedChild =	Integer.parseInt(String.valueOf(idSegments.get((i+1)).charAt(1)));	//ID of the person to show the spouse next to
					debug("selectedChild: " + selectedChild);
				}
				startCol = centreCol - peopleInRow;
				endCol = centreCol + peopleInRow;
				placedChildren = 0;
				debug("selectedChild: " + selectedChild);
				for(j=startCol;j<endCol;j+=2){
					if(placedChildren < peopleInRow){
						setBox((currentDescendant + 'b' + placedChildren), "person", j, currentRow);
						debug("comparing " + selectedChild + " and " + placedChildren);
						if(selectedChild == placedChildren){
							j += 2;
							if(getPersonIndex((currentDescendant + 'b' + placedChildren + 'a')) == -1){
								boxType = "empty";
							}else{
								boxType = "person";
							}
							setBox((currentDescendant + 'b' + placedChildren + 'a'), boxType, j, currentRow);
						}
						placedChildren++;
					}
				}
			}
		}
	}
	
	/**
	 * Set the total number of rows and columns in the grid. Values are dynamically calculated if the user has selected an ancestor up the tree to display.
	 * @pre		The TOP_ANCESTOR[0] value has been set with the relevant person ID (if applicable).
	 * @post	The TOP_ANCESTOR values at indexes 1, 2, and 3 are set with the calculated values (if applicable), and the TREE_XY values have been set.
	 */
	private static void setFullTreeGrid(){
		TREE_XY[0] = 7;	//default number of columns
		TREE_XY[1] = 5;	//default number of rows
		ArrayList<String> idSegments;
		if(TOP_ANCESTOR[0] != -1){
			idSegments = generateSegments(people.get(TOP_ANCESTOR[0]).getID());
			int furthestRight = 3;

			if(idSegments.get(0).equals("1")){
				TOP_ANCESTOR[1] = -2;
			}else{
				TOP_ANCESTOR[1] = 2;
			}
			
			TOP_ANCESTOR[2] = 5;
			TOP_ANCESTOR[3] = -3;
			
			for(int i=1;i<idSegments.size();i++){
				if(idSegments.get(i).equals("1")){
					TOP_ANCESTOR[1]--;
				}else if(idSegments.get(i).equals("2")){
					TOP_ANCESTOR[1]++;
				}
				if((TOP_ANCESTOR[1]-1) < TOP_ANCESTOR[3]){
					TOP_ANCESTOR[3] = (TOP_ANCESTOR[1]-1);
				}
				if((TOP_ANCESTOR[1]+1) > furthestRight){
					furthestRight = (TOP_ANCESTOR[1]+1);
				}
				TOP_ANCESTOR[2] += 2;
			}
			
			if(TOP_ANCESTOR[2] > TREE_XY[1]){
				//debug("changing TREE_XY[1] to " + row);
				TREE_XY[1] = TOP_ANCESTOR[2];
			}
			
			if(((-TOP_ANCESTOR[3]) + furthestRight) > 6){
				TREE_XY[0] = (-TOP_ANCESTOR[3]) + furthestRight + 1;
			}
			
		}else if(LOW_DESCENDANT != -1){
			int numChildren, numCols;
			idSegments = generateSegments(people.get(LOW_DESCENDANT).getID());
			String currentDescendant = "";
			for(int i=0;i<idSegments.size();i++){
				//go through ID, get max columns needed for the current ID's children
				TREE_XY[1] += 2;
				currentDescendant += idSegments.get(i);
				numChildren = getChildrenCount(currentDescendant);
				if(idSegments.get(i).charAt(0) == 'b'){	//not the last set of parents- leave space for a child to have a spouse
					numChildren++;
				}
				numCols = ((numChildren*2)+1);
				debug("  numCols: " + numCols);
				if(numCols > TREE_XY[0]){
					debug("overwriting TREE_XY[0] with " + numCols);
					TREE_XY[0] = numCols;
				}
				
			}
			
		}
		//add buffers to tree grid
		TREE_XY[0] += 2;
		TREE_XY[1] += 2;
	}
	
	//================================================================================================================
	//											BUILD GRIDS AND ELEMENTS
	//================================================================================================================
	
	
	/**
	 * Uses the values of elements in the BOX_ID ArrayList to create actual GridPane nodes and display them on the screen.
	 * @param	board	GridPane object that will have the full tree built in it, box by box.
	 * @pre		All the boxes in the grid have their XY coordinates stored in the BOX_XY ArrayList and their ID and boxType stored in the BOX_ID ArrayList.
	 * @post	All the boxes in the BOX_ID ArrayList have been turned into GridPane nodes and added to the given GridPane object.
	 */
	private static void buildTree(GridPane board){
		GridPane box;
		Label name, age;
		String fullName, fullAge, boxStyle;
		int personIndex, i;
		
		for(i=0;i<BOX_ID.size();i++){
			box = new GridPane();
			boxStyle = "";
			switch (BOX_ID.get(i)[0]) {
				case "person":
					personIndex = getPersonIndex(BOX_ID.get(i)[1]);
					fullName = fullAge = "";
					fullName += people.get(personIndex).getFName() + " " + people.get(personIndex).getLNameCurrent();
					if(people.get(personIndex).getDOD().equals("-")){
						fullAge += people.get(personIndex).getDOB() + " - Present";
					}else{
						fullAge += people.get(personIndex).getDOB() + " - " + people.get(personIndex).getDOD();
					}
					name = new Label(fullName);
					age = new Label(fullAge);
					GridPane.setConstraints(name, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
					GridPane.setConstraints(age, 0, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
					box.getChildren().addAll(name, age);
					boxStyle = "entity";
					break;
				case "empty":
					name = new Label("No data");
					GridPane.setConstraints(name, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
					box.add(name, 0, 0);
					boxStyle = "entity";
					break;
				case "connector":
					boxStyle = BOX_ID.get(i)[1];
					break;
				case "up_arrow":
				case "down_arrow":
					boxStyle = BOX_ID.get(i)[0];
					break;
				case "void":
					break;
				default:
					debug("error with box type at box index " + i);
					break;
			}
			GridPane.setConstraints(box, BOX_XY.get(i)[0], BOX_XY.get(i)[1]);
			box.setId(String.valueOf(i));	//set the GridPane's ID to its index in the BOX_ID ArrayList
			nodeStyle(box, boxStyle);
			board.getChildren().add(box);
		}
	}
	
	
	//================================================================================================================
	//											BUILD VIEWS
	//================================================================================================================
	
	/**
	 * Displays the siblings of the selected person on the screen.
	 * @param	ID	String representing the ID of the person to be displayed along with their parents and any siblings.
	 * @pre		
	 * @post	
	 */
	private static void displaySiblingView(String ID){
		GridPane board = new GridPane();
		board.getStyleClass().add("tree_view");
		
		boolean siblingJustPlaced;
		int i, centreCol, numSiblings, siblingCount;
		String personID = ID, type;
		ArrayList<String> segments = generateSegments(ID);
		debug("segments size: " + segments.size());
		debug("segments first char of last: " + segments.get((segments.size()-1)).charAt(0));
		if(segments.get((segments.size()-1)).charAt(0) == 'b'){	//person is a sibling, get the base sibling ID instead
			debug("person is a sibling");
			if(getGeneration(ID) == 0){
				personID = "0";
			}else{
				personID = ID.substring(0,(ID.lastIndexOf('b')));
			}
			debug("new personID = " + personID);
		}
		
		numSiblings = getSiblingCount(personID);
		debug("numSiblings: " + numSiblings);
		
		TREE_XY[0] = 5;
		TREE_XY[1] = 7;
		
		//modify number of columns if required
		for(i=2;i<=numSiblings;i++){
			debug("adding to TREE_XY[0]");
			TREE_XY[0] += 2;
		}
		
		//fill the tree grid with the required number of boxes
		fillGrid(board, TREE_XY[0], TREE_XY[1], true);
		debug("created " + TREE_XY[0] + " by " + TREE_XY[1] + " grid.");
		
		//get centre column
		centreCol = TREE_XY[0]/2;
		debug("centreCol: " + centreCol);
		
		if(personID.equals("0")){
			personID = "";
		}
		
		//set mother box
		if(getPersonIndex(personID.concat("1")) == -1){
			setBox(personID.concat("1"), "empty", (centreCol - 1), 1);
		}else{
			setBox(personID.concat("1"), "person", (centreCol - 1), 1);
		}
		//set father box
		if(getPersonIndex(personID.concat("2")) == -1){
			setBox(personID.concat("2"), "empty", (centreCol + 1), 1);
		}else{
			setBox(personID.concat("2"), "person", (centreCol + 1), 1);
		}
		
		//	display connectors between parents in row 3
		//set top-right L in col (centreCol-1), horizontal line for centreCol, top-left L in (centreCol+1)
		setBox("top_right_L", "connector", (centreCol-1), 2);
		setBox("lower_T", "connector", centreCol, 2);
		setBox("top_left_L", "connector", (centreCol+1), 2);
		
		
		//	display vertical line in centre column in row 4
		setBox("vertical_line", "connector", centreCol, 3);
		
		//	display main user and their siblings
		if(numSiblings == 0){
			setBox("vertical_line", "connector", centreCol, 4);		//set centre connector
			setBox(personID, "person", centreCol, 5);
		}else{
			setBox("bottom_right_L", "connector", 1, 4);		//set main person connector
			setBox(personID, "person", 1, 5);			//set main person
			siblingJustPlaced = true;
			siblingCount = 0;
			for(i=2;i<(TREE_XY[0]-2);i++){
				debug("TREE_XY[0]: " + i);
				if(siblingJustPlaced){
					if(i == centreCol){
						setBox("upper_T", "connector", i, 4);
					}else{
						setBox("horizontal_line", "connector", i, 4);
					}
					siblingJustPlaced = false;
				}else{
					if(i == centreCol){
						type = "plus";
					}else{
						type = "lower_T";
					}
					setBox(type, "connector", i, 4);
					setBox((personID + "2b" + siblingCount), "person", i, 5);
					siblingCount++;
					siblingJustPlaced = true;
				}
			}
			
			setBox("bottom_left_L", "connector", (TREE_XY[0]-2), 4);
			setBox((personID + "2b" + (numSiblings-1)), "person", (TREE_XY[0]-2), 5);
		}
		
		buildTree(board);
		
		board.getChildren().forEach(item -> {
			if(Integer.parseInt(item.getId()) != -1){
				if(BOX_ID.get(Integer.parseInt(item.getId())) != null){
					nodeStyle(item, BOX_ID.get(Integer.parseInt(item.getId()))[0]);
				}
			}
		});
		
		root.setCenter(board);
		
	}
	
	/**
	 * Displays the family tree for the currently loaded profile.
	 * @pre		
	 * @post	
	 */
	private static void displayTreeView(){
		GridPane board = new GridPane();
		board.getStyleClass().add("tree_view");
		
		debug("    ========== setting tree grid");
		setFullTreeGrid();
		debug("    ========== filling grid");
		fillGrid(board, TREE_XY[0], TREE_XY[1], true);
		debug("    ========== displaying default people");
		displayDefaultPeople();
		if(TOP_ANCESTOR[0] != -1){	//show ancestor tree
			debug("    ========== displaying ancestors.");
			displayAncestors();
		}else if(LOW_DESCENDANT != -1){		//show descendant tree
			debug("    ========== displaying children.");
			displayDescendants();
		}
		
		debug("    ========== generating connectors");
		//generate connectors for all the set people
		generateConnectors();
		debug("    ========== building tree");
		//put the GUI elements in the center GridPane
		buildTree(board);
		
		board.getChildren().forEach(item -> {
			if(Integer.parseInt(item.getId()) != -1){
				if(BOX_ID.get(Integer.parseInt(item.getId())) != null){
					nodeStyle(item, BOX_ID.get(Integer.parseInt(item.getId()))[0]);
				}
			}
		});
		
		root.setCenter(board);
		
	}
	
	/**
	 * Display the details form for the given ID.
	 * @param	id	String representing the ID to display the form for.
	 * @pre		
	 * @post	
	 */
	private static void personForm(String id){
		int row = 0;
		int col = 2;
		
		GridPane form = new GridPane();
		fillGrid(form, 7, 15, false);
		
		Label pageTitle = new Label();
		pageTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
		Label pageSubTitle = new Label();
		Label relationship = new Label(calcUserRelationship(id));
		
		
		Button confirm = new Button();
		Button cancel = new Button("Cancel");
		
		Label fNameLab = new Label("First Name");
		Label mNameLab = new Label("Middle Name(s)");
		Label lNameLab = new Label("Last Name(s)");
		Label genderLab = new Label("Gender");
		Label addressLab = new Label("Address");
		Label dobLab = new Label("Date of Birth");
		Label dodLab = new Label("Date of Death");
		Label notesLab = new Label("Notes");
		
		TextField fNameText = new TextField();
		fNameText.setPromptText("First Name");
		TextField mNameText = new TextField();
		mNameText.setPromptText("Middle Name(s)");
		TextField lNameCurrentText = new TextField();
		lNameCurrentText.setPromptText("Last Name (current)");
		TextField lNameBirthText = new TextField();
		lNameBirthText.setPromptText("Last Name (at birth)");
		TextField genderText = new TextField();
		genderText.setPromptText("Gender");
		TextField addStreetText = new TextField();
		addStreetText.setPromptText("Street");
		TextField addSuburbText = new TextField();
		addSuburbText.setPromptText("Suburb");
		TextField addPostcodeText = new TextField();
		addPostcodeText.setPromptText("Postcode");
		TextField addStateText = new TextField();
		addStateText.setPromptText("State");
		TextField addCountryText = new TextField();
		addCountryText.setPromptText("Country");
		DatePicker dobPicker = new DatePicker();
		DatePicker dodPicker = new DatePicker();
		TextField notesText = new TextField();
		notesText.setPromptText("Anything interesting about this person");
		
		if(getPersonIndex(id) != -1){
			//person exists, editing
			pageTitle.setText("Edit Person");
			pageSubTitle.setText("Editing the details for " + people.get(getPersonIndex(id)).getFName() + " " + people.get(getPersonIndex(id)).getLNameCurrent());
			confirm.setText("Save Changes");
			//set text fields to have this person's existing info in them already
			String[] details = people.get(getPersonIndex(id)).exportPersonInternal();
			
			fNameText.setText(details[1]);
			mNameText.setText(details[2]);
			lNameCurrentText.setText(details[3]);
			lNameBirthText.setText(details[4]);
			genderText.setText(details[5]);
			addStreetText.setText(details[6]);
			addSuburbText.setText(details[7]);
			addPostcodeText.setText(details[8]);
			addStateText.setText(details[9]);
			addCountryText.setText(details[10]);
			debug("dob: " + details[11]);
			debug("dod: " + details[12]);
			dobPicker.setValue(LocalDate.parse(details[11], DateTimeFormatter.ofPattern("d/M/yyyy")));
			if(!details[12].equals("-")){
				dodPicker.setValue(LocalDate.parse(details[12], DateTimeFormatter.ofPattern("d/M/yyyy")));
			}
			notesText.setText(details[13]);
		}else{
			//person doesn't exist, creating profile
			pageTitle.setText("Create Person");
			pageSubTitle.setText("Entering the details for a new person");
			confirm.setText("Create Person");
		}
		
		GridPane.setConstraints(pageTitle,			col,		row,	3, 1, HPos.CENTER, VPos.BOTTOM);
		GridPane.setConstraints(pageSubTitle,		col,		++row,	3, 1, HPos.CENTER, VPos.BOTTOM);
		GridPane.setConstraints(relationship,		col,		++row,	3, 1, HPos.CENTER, VPos.CENTER);
		GridPane.setConstraints(fNameLab,			col,		++row,	1, 1, HPos.CENTER, VPos.CENTER);
		GridPane.setConstraints(mNameLab,			col+1,		row,	1, 1, HPos.CENTER, VPos.CENTER);
		GridPane.setConstraints(lNameLab,			col+2,		row,	1, 1, HPos.CENTER, VPos.CENTER);
		GridPane.setConstraints(fNameText,			col,		++row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(mNameText,			(col+1),	row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(lNameCurrentText,	(col+2),	row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(lNameBirthText,		(col+2),	++row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(genderLab,			col,		row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(genderText,			col+1,		row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(addressLab,			col,		++row,	3, 1, HPos.CENTER, VPos.CENTER);
		GridPane.setConstraints(addStreetText,		col-1,		++row,	3, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(addSuburbText,		col+2,		row,	2, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(addPostcodeText,	col,		++row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(addStateText,		col+1,		row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(addCountryText,		(col+2),	row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(dobLab,				col,		++row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(dobPicker,			(col+1),	row,	2, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(dodLab,				col,		++row,	1, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(dodPicker,			(col+1),	row,	2, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(notesLab,			col,		++row,	3, 1, HPos.CENTER, VPos.BASELINE);
		GridPane.setConstraints(notesText,			col,		++row,	3, 3, HPos.CENTER, VPos.BASELINE, Priority.ALWAYS, Priority.ALWAYS);
		GridPane.setConstraints(confirm,			col,		++row,	1, 1, HPos.CENTER, VPos.CENTER);
		GridPane.setConstraints(cancel,				(col+2),	row,	1, 1, HPos.CENTER, VPos.CENTER);
		
		nodeStyle(confirm, "menu");
		nodeStyle(cancel, "menu");
		nodeStyle(form, "form");
		
		confirm.setOnAction( e -> {
			//add person's details to people array
			//id, fname, mnames, lname_current, lname_birth, gender, address_street, address_suburb, address_postcode, address_state, address_country, dob, dod, notes
			String mName, lNameBirth, street, suburb, postcode, state, country, dob, dod, notes;
			mName = lNameBirth = street = suburb = postcode = state = country = notes = "-";
			if(!mNameText.getText().equals("")){
				mName = mNameText.getText();
			}
			if(!lNameBirthText.getText().equals("")){
				lNameBirth = lNameBirthText.getText();
			}
			if(!addStreetText.getText().equals("")){
				street = addStreetText.getText();
			}
			if(!addSuburbText.getText().equals("")){
				suburb = addSuburbText.getText();
			}
			if(!addPostcodeText.getText().equals("")){
				postcode = addPostcodeText.getText();
			}
			if(!addStateText.getText().equals("")){
				state = addStateText.getText();
			}
			if(!addCountryText.getText().equals("")){
				country = addCountryText.getText();
			}
			if(!notesText.getText().equals("")){
				notes = notesText.getText();
			}
			
			if(dodPicker.getValue() != null){
				dod = dodPicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));					
			}else{
				dod = "-";
			}
			
			if(dobPicker.getValue() != null){
				dob = dobPicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));					
			}else{
				dob = "-";
			}
			
			String personDetails[] = new String[]{
				id,
				fNameText.getText(),
				mName,
				lNameCurrentText.getText(),
				lNameBirth,
				genderText.getText(),
				street,
				suburb,
				postcode,
				state,
				country,
				dob,
				dod,
				notes
			};
			
			if(validatePersonInput(personDetails) == -1){
				if(getPersonIndex(id) == -1){
					debug("person created");
					people.add(new Person(personDetails));
				}else{
					debug("person updated");
					people.set(getPersonIndex(id), new Person(personDetails));
				}
				saveProfile();
			}else{
				debug("invalid input");
			}
			debug("confirmed");
			switchContent("tree", String.valueOf(currentProfile));
		});
		cancel.setOnAction( (ActionEvent e) -> {
			debug("cancelled");
			switchContent("tree", String.valueOf(currentProfile));
		});
		
		//add title
		form.getChildren().addAll(pageTitle, pageSubTitle, relationship);
		//add all labels
		form.getChildren().addAll(fNameLab, mNameLab, lNameLab, genderLab, addressLab, dobLab, dodLab, notesLab);
		//add all fields
		form.getChildren().addAll(fNameText, mNameText, lNameCurrentText, lNameBirthText, genderText, addStreetText, addSuburbText, addPostcodeText, addStateText, addCountryText, dobPicker, dodPicker, notesText);
		//add buttons
		form.getChildren().addAll(confirm, cancel);
		
		root.setCenter(form);
		
	}
	
	//================================================================================================================
	//											BUILD MENUS
	//================================================================================================================
	
	/**
	 * creates the Quick menu for the top of the page
	 * @param menuID 
	 */
	private static void displayQuickMenu(int menuID){
		if(menuID != -1){
			GridPane quickMenu = new GridPane();
		
			Button home = new Button("Home");
			home.setOnAction(e -> switchContent("main", "0"));
			Button reset = new Button("Reset Tree");
			reset.setOnAction(e -> {
				TOP_ANCESTOR[0] = -1;
				LOW_DESCENDANT = -1;
				switchContent("tree", String.valueOf(currentProfile));
			});
			Button save = new Button("Save");
			save.setOnAction(e -> saveProfile());

			GridPane.setConstraints(home, 2, 0, 1, 1, HPos.CENTER, VPos.CENTER);
			GridPane.setConstraints(save, 3, 0, 1, 1, HPos.CENTER, VPos.CENTER);
			GridPane.setConstraints(reset, 4, 0, 1, 1, HPos.CENTER, VPos.CENTER);
			
			fillGrid(quickMenu, 7, 1, false);
			
			nodeStyle(home, "menu");
			nodeStyle(save, "menu");
			nodeStyle(reset, "menu");
			
			quickMenu.getChildren().addAll(home, save, reset);
			quickMenu.getStyleClass().add("menu");
			quickMenu.setPadding(new Insets(10,5,10,5));
			root.setTop(quickMenu);
		}else{
			root.setTop(null);
		}
		
	}
	
	/**
	 * displays the Select menu for the chosen person using their personID
	 * @param ID 
	 */
	private static void displaySelectMenu(String ID){
		int personIndex = getPersonIndex(ID);
		if(personIndex != -1){
			GridPane bottomMenu = new GridPane();
			Text name = new Text(people.get(personIndex).getFName() + " ");
			if(!people.get(personIndex).getMName().equals("-")){
				name.setText(name.getText() + people.get(personIndex).getMName() + " ");
			}
			name.setText(name.getText() + people.get(personIndex).getLNameCurrent());

			Text dobd = new Text(people.get(personIndex).getDOB() + " - ");
			if(people.get(personIndex).getDOD().equals("-")){
				dobd.setText(dobd.getText() + "Present");
			}else{
				dobd.setText(dobd.getText() + people.get(personIndex).getDOD());
			}

			Button edit = new Button("Edit Person");
			Button child = new Button("Add Child");
			Button delete = new Button("Delete Person");
			
			nodeStyle(edit, "menu");
			nodeStyle(delete, "menu");
			nodeStyle(child, "menu");
			
			edit.setOnAction( e -> switchContent("person-form", ID));
			delete.setOnAction( e -> {
				deleteUser(ID);
				saveProfile();
				switchContent("select-menu", "-1");
				switchContent("tree", String.valueOf(currentProfile));
			});
			child.setOnAction( e -> switchContent("person-form", getNextChild(ID)));
			
			GridPane.setMargin(name, new Insets(0,5,0,5));
			GridPane.setMargin(dobd, new Insets(0,5,0,5));
			
			if(getGeneration(ID) >= 0){	//ancestor, show 'sibling' button
				fillGrid(bottomMenu, 10, 2, false);
				
				Button sibling = new Button("Show Siblings");
				
				GridPane.setConstraints(name, 3, 0, 2, 1, HPos.CENTER, VPos.CENTER, Priority.SOMETIMES, Priority.SOMETIMES);
				GridPane.setConstraints(dobd, 5, 0, 2, 1, HPos.CENTER, VPos.CENTER, Priority.SOMETIMES, Priority.SOMETIMES);
				
				GridPane.setConstraints(edit, 3, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
				GridPane.setConstraints(delete, 4, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
				GridPane.setConstraints(sibling, 5, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
				GridPane.setConstraints(child, 6, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
				
				nodeStyle(sibling, "menu");
				
				sibling.setOnAction( e -> switchContent("siblings", ID));
				bottomMenu.getChildren().addAll(name, dobd, edit, delete, sibling, child);
				
			}else{		//descndant, don't show 'sibling' button
				fillGrid(bottomMenu, 9, 2, false);
				
				GridPane.setConstraints(name, 3, 0, 1, 1, HPos.RIGHT, VPos.CENTER, Priority.SOMETIMES, Priority.SOMETIMES);
				GridPane.setConstraints(dobd, 5, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.SOMETIMES, Priority.SOMETIMES);
				
				GridPane.setConstraints(edit, 3, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
				GridPane.setConstraints(delete, 4, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
				GridPane.setConstraints(child, 5, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
				bottomMenu.getChildren().addAll(name, dobd, edit, delete, child);
				
			}
			
			bottomMenu.getStyleClass().add("menu");
			bottomMenu.setPadding(new Insets(10,0,10,0));
			root.setBottom(bottomMenu);
		}else{
			root.setBottom(null);
		}
	}
	
	/**
	 * Display the main menu screen.
	 */
	private static void displayMainMenu(){
		int numberRows = 10;
		int numberColumns = 5;
		
		GridPane board = new GridPane();
		fillGrid(board, numberColumns, numberRows, false);
		board.getStyleClass().add("main_menu");
		
		Image logo = new Image("images/elements/ANCESTREE.png");
		ImageView displayLogo = new ImageView(logo);
		displayLogo.setFitHeight(500);
		displayLogo.setPreserveRatio(true);
		displayLogo.setSmooth(true);
		
		Button p1Btn = new Button("Profile 1");
		Button p2Btn = new Button("Profile 2");
		Button p3Btn = new Button("Profile 3");
		
		GridPane.setConstraints(displayLogo, 1, 1, 3, 6, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
		GridPane.setConstraints(p1Btn, 1, 7, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		GridPane.setConstraints(p2Btn, 2, 7, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		GridPane.setConstraints(p3Btn, 3, 7, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		
		nodeStyle(p1Btn, "menu");
		nodeStyle(p2Btn, "menu");
		nodeStyle(p3Btn, "menu");
		p1Btn.setPrefSize(150, 40);
		p2Btn.setPrefSize(150, 40);
		p3Btn.setPrefSize(150, 40);
		p1Btn.setOnAction( (ActionEvent e) -> {
			switchContent("tree", "1");
			switchContent("quick-menu", "0");
		});
		p2Btn.setOnAction( (ActionEvent e) -> {
			switchContent("tree", "2");
			switchContent("quick-menu", "0");
		});
		p3Btn.setOnAction( (ActionEvent e) -> {
			switchContent("tree", "3");
			switchContent("quick-menu", "0");
		});
		
		board.getChildren().addAll(displayLogo, p1Btn, p2Btn, p3Btn);
		root.setCenter(board);
		
	}
	
	
	//================================================================================================================
	//											PERFORM USER FUNCTIONS
	//================================================================================================================
	
	
	//checks that a given ID is in the correct format for this program
	//MAY JUST BE REPLACED WITH generateSegments() METHOD
	/**
	 * Checks that the given ID is in the correct format for this program using a range of criteria.
	 * @param	idInput	String with the ID to be checked.
	 * @return	Boolean value. True if the ID is in the correct format, False if there are issues with the String.
	 */
	private static boolean validateID(String idInput){
		boolean isValid = true;
		int i, idInt;
		char[] id = idInput.toCharArray();
		String idSequence;
		/*
		fail conditions:
		first int is not 0, 1 or 2
		first part of ID, from second char up to first colon, is not made of numbers 1 or 2
		last char isnt an int
		char after a colon isn't an int
		two colons with only ints in between
		two letters in a row
		id without a colon has a char other than 1 or 2
		id has any chars other than a, b or colon
		*/
		
		//check that only ints, 'a', 'b' and ':' are in id, and also that there isn't a space between two colons with only ints, and that there aren't two letters in a row
		
		//debug("ID being checked: " + idInput);
		for(i=0;i<id.length && isValid == true;i++){
			isValid = ((String.valueOf(id[i]).matches("[ab:]")) || (Character.isDigit(id[i])));
			if(i < id.length){
				if(id[i] == ':'){
					if(!Character.isDigit(id[i+1])){
						//debug("id check 1");
						isValid = false;
					}else if(idInput.indexOf(":",(i+1)) != -1){
						if(!idInput.substring((i+1), idInput.indexOf(":",(i+1))).contains("ab")){
							//debug("id check 2");
							isValid = false;
						}
					}
				}else if((id[i] == 'a') || (id[i] == 'b')){
					if((id[i+1] == 'a') || (id[i+1] == 'b')){
						//debug("id check 3");
						isValid = false;
					}
				}
			}
		}
		
		//check that first int is 0, 1 or 2
		if(isValid && !String.valueOf(id[0]).matches("[012]")){
			//debug("id check 4");
			//debug("id[0] = " + id[0]);
			isValid = false;
			
		}
		
		if(isValid){
			if(id[(id.length)-1] == ':'){
				//debug("id check 5");
				isValid = false;
			}
		}
		
		if(isValid && idInput.contains(":")){	//then it's not a direct descendant
			idSequence = idInput.substring(1,idInput.indexOf(":"));
			if(idInput.substring(1,idInput.indexOf(":")).matches("[^12]")){
				isValid = false;
			}
		}
		//debug("ID is valid: " + isValid);
		return(isValid);
	}
	
	//confirm that the fields in the given input is valid
	/**
	 * 
	 * @param personDetails
	 * @return 
	 */
	private static int validatePersonInput(String[] personDetails){
		//-2 is an error during validation, -1 confirms that the ID is valid, otherwise the returned int is the index of the first field that's invalid
		int validCode = -2;
		if(validateID(personDetails[0])){
			validCode = -1;
		}
		
		if((personDetails[1].equals("")) || (personDetails[3].equals("")) || (personDetails[4].equals("")) || (personDetails[11].equals(""))){
			validCode = 0;
		}
		
		//id, fname, mnames, lname_current, lname_birth, gender, address_street, address_suburb, address_postcode, address_state, address_country, dob, dod, notes
		
		return(validCode);
	}
	
	/**
	 * Delete the chosen person, and all the tree connected by them
	 * @param ID 
	 */
	private static void deleteUser(String ID){
		int index = getPersonIndex(ID);
		int numSegments = generateSegments(ID).size();
		
		ArrayList<String> currentID;
		
		if(index != -1){	//check that the person actually exists
			int generation = getGeneration(ID);
			if(generation >= 0){	//person is above main user
				if(!(people.get(index).getID().contains("a")) && !(people.get(index).getID().contains("b"))){	//person is in main ancestor path
					deleteUser(ID + "1");	//delete mother
					deleteUser(ID + "2");	//delete father
					for(Person person : people){	//delete all siblings
						currentID = generateSegments(person.getID() + '2');
						if(currentID.size() == (numSegments+1)){
							if(currentID.get((currentID.size()-1)).charAt(0) == 'b'){
								deleteUser(person.getID());
							}
						}
					}
				}
			}else if(generation <= 0){	//person is below main user
				deleteUser(ID + 'a');	//delete spouse
				for(Person person : people){	//delete all children
					currentID = generateSegments(person.getID());
					if(currentID.size() == (numSegments+1)){
						if(currentID.get((currentID.size()-1)).charAt(0) == 'b'){
							deleteUser(person.getID());
						}
					}
				}
			}
			debug("deleting ID " + people.get(index).getID());
			people.remove(index);
		}
		
		
	}
	
	/**
	 * Get the individual ID segments from a given ID.
	 * @param	personID	String with the ID to split into segments.
	 * @return	ArrayList with the segments of the given ID if it is valid, or an ArrayList with a single element with the value "invalid" if it is not valid.
	 */
	private static ArrayList generateSegments(String personID){
		ArrayList<String> segments = new ArrayList<>();
		String segment;
		int j;
		boolean isValid = true, getNextChar;
		for(int i=0;i<personID.length() && isValid;i++){
			switch(personID.charAt(i)){
				case '0':		//user
				case '1':		//direct mother
				case '2':		//direct father
				case 'a':		//spouse
					segments.add(String.valueOf(personID.charAt(i)));			//add directly to segment array
					break;
				case 'b':		//child
				//case 'c':		//child
					//get next numbers, until the next letter, then add to segment array
					//increment i by however many numbers there are
					segment = "";
					do{
						segment += String.valueOf(personID.charAt(i));
						i++;
						if(i < personID.length()){
							getNextChar = Character.isDigit(personID.charAt(i));
						}else{
							getNextChar = false;
						}
					}while(getNextChar);
					i--;
					segments.add(segment);
					break;
				default:	//ID input is invalid, cancel generation and return error
					isValid = false;
					break;
			}
			
		}
		
		if(!isValid || (personID.length() == 0)){
			segments = new ArrayList<>();
			segments.add("invalid");
		}
		debug("Final ID segments from " + personID + ":");
		for (String currentSegment : segments) {
			debug(currentSegment + ',');
		}
		
		return(segments);
	}
	
	/**
	 * Calculate exactly how a given person is related to the main user
	 * @param personID
	 * @return 
	 */
	private static String calcUserRelationship(String personID){
		String rel;
		
		ArrayList<String> segments = generateSegments(personID);
		
		if(personID.equals("0")){
			rel = "Your details";
		}else{
			rel = "";
			debug("getting relationship for ID " + personID);
			for(String segment : segments){
				debug("segment: " + segment);
				switch(segment.charAt(0)){
					case '1':	//mother
						rel += "Mother's ";
						break;
					case '2':	//father
						rel += "Father's ";
						break;
					case 'a':	//spouse
						rel += "Spouse's ";
						break;
					case 'b':	//child
						rel += "Child's ";
						break;
					default:
						break;
				}
			}
			debug("rel: " + rel);
			rel = "Relationship: " + rel.substring(0, rel.lastIndexOf("'"));
		}
		return(rel);
	}
	
	/**
	 * Gets the total number of siblings in the people ArrayList from the given ID.
	 * @param	ID	String to get all the siblings for. Must be a direct relation (not a sibling of a person)
	 * @return	Integer representing the number of siblings the given ID has.
	 */
	private static int getSiblingCount(String ID){
		int numSiblings = 0, i;
		String baseID = "";// = ID;
		debug("getting siblings for " + ID);
		
		if((!ID.equals("0"))){	//person is not the main user
			ArrayList<String> idSegments = generateSegments(ID);
			
			if(idSegments.get(0).equals("invalid")){
				//error, dont proceed through method
			}else{
				if(idSegments.get(idSegments.size()-1).charAt(0) == 'b'){	//person is someone's sibling
					baseID = ID.substring(0,ID.lastIndexOf('b')-1);
				}else{	
					baseID = ID;
				}
			}
		}
		
		for(i=0;i<=50;i++){
			if(getPersonIndex(baseID + "2b" + numSiblings) != -1){
				numSiblings++;
			}
		}
				
		return(numSiblings);
		
	}
	
	/**
	 * Gets the number of children for the given ID.
	 * @param	ID	String representing the ID to search for children for.
	 * @pre		ID must be a person below the main user (must begin with '0').
	 * @return	Integer representing the number of children that the given person has.
	 */
	private static int getChildrenCount(String ID){
		int numChildren = 0;
		
		String personID = ID;
		debug("getting children for " + ID);
		
		if(ID.charAt((ID.length()-1)) == 'a'){
			personID = ID.substring(0, ID.lastIndexOf('a'));	//all IDs should have a max of 1 'a' in them
		}
		for(int i=0;i<=50;i++){
			if(getPersonIndex(personID + "b" + numChildren) != -1){
				numChildren++;
			}
		}
		
		return(numChildren);
	}
	
	/**
	 * Calculates the generation of the given ID in relation to the main user.
	 * @param ID	ID to check the generation of.
	 * @return Integer representing how many generations above (or below, if number is negative) the 
	 */
	private static int getGeneration(String ID){
		int generation = 0;
		
		ArrayList<String> segments = generateSegments(ID);
		for(String segment : segments){
			debug("    generation change from segment: " + segment);
			switch(segment.charAt(0)){
				case '1':	//mother
				case '2':	//father
					generation++;
					break;
				case 'b':	//child
					generation--;
					break;
				default:
					break;
			}
		}
		debug("    final gen: " + generation);
		return(generation);
	}
	
	/**
	 * Returns the String ID of the next available child for the given ID.
	 */
	private static String getNextChild(String ID){
		String nextChildID, baseID;
		int childCount = 0;
		boolean foundID = false;
		debug("getting child");
		if(getGeneration(ID) >= 0 && ID.endsWith("1")){	//ancestor or same generation as main user, and the person is the mother.
			baseID = ID.substring(0,(ID.length()-1)) + "2b";	//replace reference to the father
		}else{
			baseID = ID + 'b';
		}
		debug("baseID: " + baseID);
		while(!foundID){
			debug("Checking for child number: " + childCount);
			debug("result: " + getPersonIndex(baseID + String.valueOf(childCount)));
			if(getPersonIndex(baseID + String.valueOf(childCount)) != -1){	//not an empty child slot
				childCount++;
			}else{	//empty child slot
				foundID = true;
			}
		}
		
		nextChildID = baseID + String.valueOf(childCount);
		return(nextChildID);
	}
	
	
	
	//================================================================================================================
	//											PROCESS SCREEN DISPLAY
	//================================================================================================================
	
	/**
	 * Switch the main screen to the specified screen
	 * @param screen Screen to switch the main window to.
	 * @param info	Supplementary info for generating the required screen.
	 */
	private static void switchContent(String screen, String info){
		switch(screen){
			case "main":
				displayQuickMenu(-1);
				displaySelectMenu("-1");
				displayMainMenu();
				break;
			case "default-tree":
				TOP_ANCESTOR[0] = -1;
				LOW_DESCENDANT = -1;
			case "tree":
				if(Integer.parseInt(info) != currentProfile){
					//buildProfile("src/profiles/" + info + ".txt");
					currentProfile = Integer.parseInt(info);
					buildProfile();
				}
				if(people.isEmpty()){
					displaySelectMenu("-1");
					personForm("0");
				}else{
					displayQuickMenu(0);
					displayTreeView();
				}
				break;
			case "select-menu":
				displaySelectMenu(info);
				break;
			case "quick-menu":
				displayQuickMenu(0);
				break;
			case "person-form":
				displaySelectMenu("-1");
				personForm(info);
				break;
			case "siblings":
				displaySelectMenu("-1");
				displaySiblingView(info);
				break;
			default:
				System.out.println("error with switchContent");
				System.out.println("screen: " + screen + ", info: " + info);
				break;
		}
	}
	
	/**
	 * Starts running the program.
	 * @param stage Stage object to show the GUI on.
	 */
    @Override
	public void start(Stage stage) {
		currentProfile = -1;
		changesMade = false;
		root = new BorderPane();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
		int width = (int)screenSize.getWidth()/screenRes*100;
		int height = (int)screenSize.getHeight()/screenRes*100;
		debug("DIMENSIONS:");
		debug("    Width: " + width);
		debug("    Height: " + height);
		
		switchContent("main", "0");
		Scene scene = new Scene(root);
		scene.getStylesheets().add("css/default.css");
		stage.setTitle("Ancestree"); 
		
		stage.setMaximized(true);
		stage.setScene(scene);
		stage.setMinHeight(height); 
		stage.setMinWidth(width);
		
		//Displaying the contents of the stage 
		stage.show(); 
		
	} 
	
	/**
	 * Save the current state of the built profile
	 * @return 
	 */
	private static boolean saveProfile(){
		boolean exportSuccess = false;
		ObjectOutputStream outputStream;
		FileOutputStream fileOut;
		File profile = new File("src/profiles/" + currentProfile);
		
        try{
			for(File file: profile.listFiles()){
				file.delete();
			}
			for(Person person : people){
				fileOut = new FileOutputStream("src/profiles/" + currentProfile + '/' + person.getID() + ".person");
				outputStream = new ObjectOutputStream(fileOut);
				outputStream.writeObject(person);
				outputStream.close();
				fileOut.close();
			}
			exportSuccess = true;
			changesMade = false;
        }catch(FileNotFoundException e){
            System.out.println("Error accessing file: " + e);
        }catch(IOException e){
			System.out.println("Error writing to file: " + e);
		}
		
		return(exportSuccess);
	}
	
	/**
	 * Runs when the user clicks the 'X' to close the window.
	 */
	@Override
	public synchronized void stop(){
		System.out.println("Program closing...");
		debug("changesMade = " + changesMade);
		if(changesMade){
			ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
			ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
			Alert prompt = new Alert(AlertType.NONE, "Would you like to save your changes to this profile?", yes, no);
			prompt.setTitle("Save Profile");
			prompt.showAndWait().ifPresent(e -> {
				if(e == yes){
					if(saveProfile()){
						System.out.println("Profile saved");
					}
				}
			});
		}
		
		System.out.println("Program closed");
	}
	
	/**
	 * Runs the GUI.
	 * @param args Commend line arguments to use, if required.
	 */
	public static void main(String args[]){ 
		launch(args); 
	} 
} 
