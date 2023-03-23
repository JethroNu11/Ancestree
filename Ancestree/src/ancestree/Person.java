/*
 * Title:          Assignment1ICT373, Person
 * Author:         Geoffrey Lawson (33176674)
 * Date:           22/03/2021
 * File Name:      Person.java
 * Purpose:            
 * Conditions:         
 */
package ancestree;

import java.io.Serializable;
import java.util.ArrayList;


/**
 *
 * @author Geoffrey Lawson (33176674)
 */
public class Person implements Serializable{
	
	//hints from lab
	/*
	people can have parents, children, spouse, siblings
	each person is person
	spouse, siblings on same level
	children below, parents above
	maybe change structure- after getting number of generations, go through each person that will be shown and count the number of people in
	the rows, then use that to build the dimensions of the tree??
	*/
	
	private void debug(String output){
		boolean debug = false;
		
		if(debug){
			System.out.println(output);
		}
	}
	String ID, fName, mName, lName_current, lName_birth, gender;
	String add_street, add_suburb, add_postcode, add_state, add_country;
	String dob, dod, notes, relationship;
	int[] xy;
	
	Person(){
		ID = fName = mName = lName_current = lName_birth = gender = add_street = add_suburb = add_postcode = add_state = add_country = dob = dod = notes = relationship = "-";
		xy = new int[2];
	}
	
	Person(String details[]){
		//new id structure:
		//id, fname, mnames, lname_current, lname_birth, gender, address_street 
		//0		1		2		3				4			5			6
		//address_suburb, address_postcode, address_state, address_country, dob, dod, notes
		//	 7					8				9					10		 11	  12	13
		ID = details[0];
		fName = details[1];
		mName = details[2];
		lName_current = details[3];
		lName_birth = details[4];
		gender = details[5];
		add_street = details[6];
		add_suburb = details[7];
		add_postcode = details[8];
		add_state = details[9];
		add_country = details[10];
		dob = details[11];
		dod = details[12];
		notes = details[13];
	
		xy = new int[2];
	}
	
	public void setXY(int x, int y){
		xy[0] = x;
		xy[1] = y;
	}
	
	public String getID(){
		return(ID);
	}
	
	public String getFName(){
		return(fName);
	}
	
	public String getMName(){
		return(mName);
	}
	
	public String getLNameCurrent(){
		return(lName_current);
	}
	
	public String getLNameBirth(){
		return(lName_birth);
	}
	
	public String getGender(){
		return(gender);
	}
	
	public String getDOB(){
		return(dob);
	}
	
	public String getDOD(){
		return(dod);
	}
	
	public int[] getXY(){
		return(xy);
	}
	
	public int getXY(int choice){
		return(xy[choice]);
	}
	
	public String getMotherID(){
		String motherID;
		if(ID.equals("0")){
			motherID = "1";
		}else{
			motherID = (ID + "1");
		}
		return(motherID);
	}
	
	public String getFatherID(){
		String fatherID;
		if(ID.equals("0")){
			fatherID = "2";
		}else if(ID.charAt(0) == '0'){
			fatherID = (ID.substring(0, ID.lastIndexOf('b')));
		}else{
			fatherID = (ID + "2");
		}
		return(fatherID);
	}
	
	public String[] exportPersonInternal(){
		String mNameOutput, lNameBirthOutput, addStreetOutput, addSuburbOutput, addPostcodeOutput, addStateOutput, addCountryOutput, notesOutput;
		mNameOutput = lNameBirthOutput = addStreetOutput = addSuburbOutput = addPostcodeOutput = addStateOutput = addCountryOutput = notesOutput = "";
		
		if(!mName.equals("-")){
			mNameOutput = mName;
		}
		if(!lName_birth.equals("-")){
			lNameBirthOutput = lName_birth;
		}
		if(!add_street.equals("-")){
			addStreetOutput = add_street;
		}
		if(!add_suburb.equals("-")){
			addSuburbOutput = add_suburb;
		}
		if(!add_postcode.equals("-")){
			addPostcodeOutput = add_postcode;
		}
		if(!add_state.equals("-")){
			addStateOutput = add_state;
		}
		if(!add_country.equals("-")){
			addCountryOutput = add_country;
		}
		
		
		if(!notes.equals("-")){
			notesOutput = notes;
		}
		
		String[] person = new String[]{
			(ID),
			(fName),
			(mNameOutput),
			(lName_current),
			(lNameBirthOutput),
			(gender),
			(addStreetOutput),
			(addSuburbOutput),
			(addPostcodeOutput),
			(addStateOutput),
			(addCountryOutput),
			(dob),
			(dod),
			(notesOutput)
		};
		return(person);
	}
	
	public String exportPerson(){
		String person = (ID) + "," + (fName) + "," + (mName) + "," + (lName_current) + "," + (lName_birth) + "," + (gender) + "," + (add_street) + "," + (add_suburb) + "," + (add_postcode) + "," + (add_state) + "," + (add_country) + "," + (dob) + "," + (dod) + "," + (notes);
		return(person);
	}
	
	public void printDetails(){
		System.out.println("Name: " + fName + " " + mName + " " + lName_current);
		System.out.println("ID: " + ID);
		System.out.println("DOB: " + dob);
		System.out.println("DOD: " + dod);
	}
	
}
