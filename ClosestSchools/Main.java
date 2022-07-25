package ClosestSchools;
/*
* Author: Nusrat Atiya
* Implements the closest pair of points recursive algorithm
* on locations of K-12 schools in Vermont obtained from http://geodata.vermont.gov/datasets/vt-school-locations-k-12

*/

import java.io.File;
import java.util.Scanner;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import java.io.File;

public class Main {


	public static void main(String[] args) throws IOException{

		//Creates an ArrayList containing School objects from the .csv file
		// Based on https://stackoverflow.com/questions/49599194/reading-csv-file-into-an-arrayliststudent-java
		String line = null;
		ArrayList<School> schoolList = new ArrayList <School>();
		// You may have to adjust the file address in the following line to your computer
		BufferedReader br = new BufferedReader(new FileReader("../Closest Schools/src/ClosestSchools/Data/VT_School_Locations__K12(1).csv"));
		if ((line=br.readLine())==null){
			return;
		}
		while ((line = br.readLine())!=null) {
			String[] temp = line.split(",");
			schoolList.add(new School(temp[4],Double.parseDouble(temp[0]),Double.parseDouble(temp[1])));
		}


		//Preprocess the data to create two sorted arrayLists (one by X-coordinate and one by Y-coordinate):
		ArrayList<School> Xsorted = new ArrayList <School>();
		ArrayList<School> Ysorted = new ArrayList <School>();
		Collections.sort(schoolList, new SortbyX());
		Xsorted.addAll(schoolList);
		Collections.sort(schoolList, new SortbyY());
		Ysorted.addAll(schoolList);

		//Run the Recursive Algorithm
		School[] cp = new School[2];
		cp = ClosestPoints(Xsorted,Ysorted);
		if(cp[0]!=null)
			System.out.println("The two closest schools are "+ cp[0].name + " and " + cp[1].name +".");
		
	}

	public static School[] ClosestPoints(ArrayList<School> sLx, ArrayList<School> sLy){
		// Recursive divide and conquer algorithm for closest points
		// sLx should be sorted by x coordinate and sLy should be sorted by y coordinate
		// Returns an array containing the two closest School objects

		School[] closestPair = new School[2];
		
		// brute force
		if (sLx.size() <= 3) {
			closestPair = BRUTEFORCE (sLx);
			return closestPair;
		}
		
		// DIVIDE INTO LEFT AND RIGHT
		
		// new ArrayLists for dividing 
			//L and R of x-sorted list
		ArrayList<School> Xl = new ArrayList<School> (); // left half of x-sorted list
		ArrayList<School> Xr = new ArrayList<School> (); // right half of x-sorted list
			//L and R of y-sorted list
		ArrayList<School> Yl = new ArrayList<School> (); // left half of y-sorted list
		ArrayList<School> Yr = new ArrayList<School> (); // right half of y-sorted list
		
		
		// find middle of array
		int midArray = sLx.size() / 2;
		
		//get midline of coordinate plane
		double midLine = (sLx.get(midArray - 1).getX() + sLx.get(midArray).getX()) / 2;
		
		// split x-sorted list into left and right based on x-coordinates
		for (int i = 0; i < midArray ; i++) {
			Xl.add(sLx.get(i)); // left half 
		}
		for (int i = midArray; i < sLx.size() ; i++) {
			Xr.add(sLx.get(i)); // right half
		}
		
		
		// split y-sorted list into left and right based on x-coordinates
		//splits the left half of the y-sorted list
		for (int i = 0; i < sLy.size(); i++) {
			if (sLy.get(i).getX() < midLine) { 
				Yl.add(sLy.get(i));
			}
		}
		//makes sure that Yl and Xl are the same size if the previous for loop didn't populate Yl enough
		for (int i = 0; i < sLy.size(); i++) {
			if (Yl.size() < Xl.size()) {
				if (sLy.get(i).getX() == midLine) {
					Yl.add(sLy.get(i));
				}
			}
		}
		
		// splits the right half of the y-sorted list
		//splits the right half of the y-sorted list
		for (int i = 0; i < sLy.size(); i++) {
			if (sLy.get(i).getX() > midLine) {
				Yr.add(sLy.get(i));
			}
		}
		//makes sure that Yr and Xr are the same size if the previous for loop didn't populate Yr enough
		for (int i = sLy.size()-1; i > -1; i--) {
			if (Yr.size() < Xl.size()) {
				if (sLy.get(i).getX() == midLine) {
					Yr.add(sLy.get(i));
				}
			}
		}

		
		//RECURSIVE STEP TO DIVIDE LEFT HALF AND RIGHT HALF
		
		School[] leftHalf = ClosestPoints(Xl, Yl);
		School[] rightHalf = ClosestPoints(Xr, Yr);

		// CACLCULATES CURRENT CLOSEST PAIR BASED ON RECURSIVE STEP
		double delta = 0;
		double deltaL = distance(leftHalf[0], leftHalf[1]);
		double deltaR = distance(rightHalf[0],rightHalf[1]);
		// compares the two pairs of closest points: one in the left half and one in the right half
		if (deltaL <= deltaR) {
			delta = deltaL;
			closestPair[0] = leftHalf[0];
			closestPair[1] = leftHalf[1];	
		}
		else {
			delta = deltaR;
			closestPair[0] = rightHalf[0];
			closestPair[1] = rightHalf[1];
		}

		// CREATE YDELTA 
		//y-sorted list of pts in P with x-corrdinates within delta of the midline
		ArrayList<School> ydelta = new ArrayList<School>();
		for (int i = 0; i < sLy.size(); i++) {
			if (Math.abs(sLy.get(i).getX() - midLine) <= delta) {
				ydelta.add(sLy.get(i));
			}
		}
		
		// LOOK AT THE NEXT 7 POINTS OF EACH POINT OF YDELTA
		int max = 7; // automatically check next 7 points if there are 7 points available
		for (int i = 0; i < ydelta.size() - 1; i++) {
			if (ydelta.size() - i <= 7) {
				max = ydelta.size() - (i + 1); // if there are less than 7 points to check, calculate the remaining points and check that
			}
			else {
				max = 7;
			}
			
			for (int j = 1; j <= max ; j++) {
				School p1 = ydelta.get(i);
				School p2 = ydelta.get(i + j);
				double deltaprime = distance (p1, p2);
				// check if there is a closest pair compared to the one previously found in the recursive step
				if (deltaprime < delta) {
					delta = deltaprime;
					closestPair[0] = p1;
					closestPair[1] = p2;
				}
			
			}
		}
		
		return closestPair;

	}

	// calculates euclidean distance between two points
	public static double distance(School p1, School p2) {       
			    return Math.sqrt((p2.getY() - p1.getY()) * (p2.getY() - p1.getY()) + 
			    		(p2.getX() -  p1.getX()) * (p2.getX() - p1.getX()));
			}
	

	// brute force method for finding closest points 
	// only used in base case
	public static School[] BRUTEFORCE(ArrayList<School> points) {
		
		School[] closestPair = new School [2];
		double min = Double.POSITIVE_INFINITY;
		
		//check distance for every possible pair
		for (int i = 0; i < points.size(); i++) { // tried size -1 didnt work
			for (int j = i + 1; j < points.size(); j++) {
				double currDist = distance(points.get(i), points.get(j));
				if (currDist < min) {
					closestPair[0] = points.get(i);
					closestPair[1] = points.get(j);
					min = currDist;
				}
			}
		}
		return closestPair;	
	}
	
}


