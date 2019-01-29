package railway;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a method to read a track from a text file.
 */
public class TrackReader {

    /**
     * <p>
     * Reads a text file named fileName that describes the sections on a track,
     * and returns a track containing each of the sections in the file.
     * </p>
     * 
     * <p>
     * The file contains zero or more lines, each of which corresponds to a
     * section on the track.
     * 
     * Each line should contain five items separated by one or more whitespace
     * characters: a positive integer representing the length of the section,
     * followed by the name of a first junction, then the type of a first
     * branch, followed by the name of a second junction, and then the type of a
     * second branch. The section denoted by the line has the given length, and
     * two end-points: one constructed from the first junction and first branch
     * on the line, and the other constructed from the second junction and
     * section branch.
     * 
     * A junction name is simply an unformatted non-empty string that doesn't
     * contain any whitespace characters. The type of a branch is one of the
     * three strings "FACING", "NORMAL" or "REVERSE", which correspond to the
     * branches Branch.FACING, Branch.NORMAL, and Branch.REVERSE, respectively.
     * 
     * There may be leading or trailing whitespace on each line of the file.
     * (Refer to the Character.isWhitespace() method for the definition of a
     * white space in java.)
     * 
     * For example, the line <br>
     * <br>
     * 
     * 10 j1 FACING j2 NORMAL
     * 
     * <br>
     * <br>
     * denotes a section with length 10 and end-points (j1, FACING) and (j2,
     * NORMAL).
     * </p>
     * 
     * <p>
     * No two lines of the file should denote equivalent sections (as defined by
     * the equals method of the Section class), and no two sections described by
     * the input file should have a common end-point (since each junction can
     * only be connected to at most one section on each branch on a valid
     * track).
     * </p>
     * 
     * <p>
     * The method throws an IOException if there is an input error with the
     * input file (e.g. the file with name given by input parameter fileName
     * does not exist); otherwise it throws a FormatException if there is an
     * error with the input format (this includes the case where there is a
     * duplicate section, and the case where two or more sections have a common
     * end-point), otherwise it returns a track that contains each of the
     * sections described in the file (and no others).
     * 
     * If a FormatException is thrown, it will have a meaningful message that
     * accurately describes the problem with the input file format, including
     * the line of the file where the problem was detected.
     * </p>
     * 
     * @param fileName
     *            the file to read from
     * @return a track containing the sections from the file
     * @throws IOException
     *             if there is an error reading from the input file
     * @throws FormatException
     *             if there is an error with the input format. The
     *             FormatExceptions thrown should have a meaningful message that
     *             accurately describes the problem with the input file format,
     *             including the line of the file where the problem was
     *             detected.
     *             
     *             throws IOException,
            FormatException
     */
    public static Track read(String fileName) 
    		throws IOException, FormatException { 
    	if(!checkFileFormat(fileName)){
    		throw new IOException("Error reading input file - "
    				+ "file must be a text file.");
    	}
    	//Initialize the track to store all the sections from the file
    	Track track = new Track();
    	//A string array to store all the different parts of the section
    	String[] section;
    	//An integer to store the length of a section
    	int length;
    	//Junctions and branches to form the end-points of a section
    	Junction junction1;
    	Junction junction2;
    	Branch branch1 = null; 
    	Branch branch2 = null;
    	//JunctionBranches to form the end-points of a section
    	JunctionBranch endPoint1;
    	JunctionBranch endPoint2;
    	Section trackSection;
    	//A list to store the sections and used to check FormatExceptions
    	List<Section> listSections = new ArrayList<Section>();
    	try{
    		//Initialize the file reader
    		FileReader reader = new FileReader(fileName);
    		//Initialize the buffer reader
	    	BufferedReader bufferReader = new BufferedReader(reader);
	    	//A string to store each line of the file
	    	String line;
	    	//Loop through each line of the file while the file is not null
	    	while((line = bufferReader.readLine()) != null){
	    		section = line.split("\\s+");
	    		//Checks that each line has at least 5 elements
        		if(section.length != 5){
        			if(section[0].equals("")){
        				throw new FormatException(
        						"Each line must have 5 elements.");
        			}
        			throw new FormatException("Each line must have 5 elements :"
        			+ line);
        		}
        		length = Integer.parseInt(section[0]);
        		//Check that junction is in the right format
        		try{
        			junction1 = new Junction(section[1]);
        			junction2 = new Junction(section[3]);
        			}catch(NullPointerException e){
        			throw new FormatException(
        					"Junction cannot be empty :"+line);
        		}
        		//Check if branches are in the right format then assign branches
        		if(checkBranchSpelling(section[2])){
	        		if(section[2].equals("FACING")){
	        			branch1 = Branch.FACING;
	        		}else if(section[2].equals("NORMAL")){
	        			branch1 = Branch.NORMAL;
	        		}else if(section[2].equals("REVERSE")){
	        			branch1 = Branch.REVERSE;
	        		}
        		}else{
        			throw new FormatException("Branch format error: " + line);
        		}
        		if(checkBranchSpelling(section[4])){
	        		if(section[4].equals("FACING")){
	        			branch2 = Branch.FACING;
	        		}else if(section[4].equals("NORMAL")){
	        			branch2 = Branch.NORMAL;
	        		}else if(section[4].equals("REVERSE")){
	        			branch2 = Branch.REVERSE;
	        		}
        		}else{
        			throw new FormatException("Branch format error: "+ line);
        		}
        		//Check that end-points are not null
        		try{
        		endPoint1 = new JunctionBranch(junction1, branch1);
        		endPoint2 = new JunctionBranch(junction2, branch2);
        		}catch(NullPointerException e){
        			throw new FormatException(
        					"End-points cannot be empty :"+ line);
        		}
        		//Check that section format is correct
        		try{
        		trackSection = new Section(length, endPoint1, endPoint2);
        		}catch(NullPointerException | IllegalArgumentException e){
        			throw new FormatException("Section format error :" + line);        			
        		}
        		listSections.add(trackSection);
        		//Check for duplicate sections or end-points in the file
        		if(!checkSections(listSections).equals("false")){
        			throw new FormatException("File cannot have duplicate "
        					+ "sections: "+checkSections(listSections));
        		}
        		if(!checkEndPoints(listSections).equals("false")){
        			throw new FormatException("Two sections cannot have the "
        					+ "same end-points: "+checkEndPoints(listSections));
        		}
        		track.addSection(trackSection);
        	}
	    	bufferReader.close();
    	}catch(IOException e){    		
    		throw new IOException("Error reading input file.");
    	}    	
    	return track; 	
    }  
    
    
    /**
     * This method checks if the file name is a text file.
     * 
     * @param filename
     * 			A string containing the file name
     * @return a boolean value. It returns true if the file name is a text file
     * or false if the filename is not a text file. 
     */
    private static boolean checkFileFormat(String filename){
    	//String array to store the split up filename
    	String[] name = filename.split("\\.");
    	if(!name[1].equals("txt")){
    		return false;
    	}
    	return true;
    }

    
    /**
     * This method checks if the string format of a branch is in the right 
     * format and returns a boolean value.
     * 
     * @param branch
     * 			A string containing the string format of a branch
     * @return a boolean value - true if the branch format and spelling is
     * 			correct and false if not. 
     */
    private static boolean checkBranchSpelling(String branch){
    	if(!branch.equals("FACING") && 
	    				!branch.equals("NORMAL") && 
	    				!branch.equals("REVERSE")){
    		return false;
    	}else{
    		return true;
    	}
    }

    /**
     * This method loops through a list of sections to see if any sections
     * are the same. If they are the same then it returns the two sections
     * that are the same in a string format. If they are not any sections that
     * are the same then it will return a string "false". 
     * 
     * @require !listSections.contains(null)      	
     * @param listSections
     * 			A list of sections
     * @return a string based on whether or not there are two sections that are 
     * the same as per the section.equals() method. If it there are two
     * equivalent sections then a string will be returned containing the two
     * sections that are the same in a string format. If not then the string
     * "false" will be returned. 
     */
    private static String checkSections(List<Section> listSections){
    	//Loops through the list of sections twice to compare each section
		for(int i = 0; i < listSections.size() - 1; i++){
			for(int j = i + 1; j < listSections.size(); j++){
				if(listSections.get(i).equals(listSections.get(j))){
					return "These are the same: ("+listSections.get(i).
							toString()+"), "+listSections.get(j).toString();
				}
			}
		}
		return "false";    	
    }
    
    /**
     * This section checks if a list of section contains two sections with the 
     * same end-point as per the junctionBranch.equals() method. If there are two
     * end-points that are the same then it returns the section in string format
     * else it returns the string "false" 
     * 
     * @require !listSections.contains(null)      	
     * @param listSections
     * 			A list of sections
     * @return a string based on whether or not there are two sections with the
     * same end-points as per the junctionBranch.equals() method. If two 
     * end-points are the same then a string format of the two sections will
     * be returned, else it returns the string "false".
     */
    private static String checkEndPoints(List<Section> listSections){
    	//Loops through listSections twice to compare all sections against each
    	//other
    	for(int i = 0; i < listSections.size(); i++){
    		for(int j = 0; j < listSections.size(); j ++){
    			if(j == i){
    				continue;
    			}   	
    			//Loop through end-points of the sections
    			for(JunctionBranch endPointI : 
    				listSections.get(i).getEndPoints()){
    				for(JunctionBranch endPointJ : 
    					listSections.get(j).getEndPoints()){
    					if(endPointI.equals(endPointJ)){
    						return listSections.get(i).toString() + " and " + 
    					listSections.get(j).toString();
    					}
    				}    				
    			}
    		}
    	}
    	return "false";
    }
    

}
