/**
 * @author Brian Weir (https://github.com/bweir27)
 * ConvxHull -> QuickHull
 */

import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import edu.princeton.cs.introcs.StdDraw;

public class ConvexHull {
    public static final int CANVAS_SIZE=600;
    public static final int NUM_POINTS = 50;

    public static void main(String[] args) {
        System.out.println("This Class is a proof-of correctness implementation for QuickHull, see Question3b for running time analysis.");

        //set up StdDraw
        StdDraw.setCanvasSize(CANVAS_SIZE,CANVAS_SIZE);
        StdDraw.setXscale(0,CANVAS_SIZE);
        StdDraw.setYscale(0,CANVAS_SIZE);

        //set font
        Font currentFont = StdDraw.getFont();
        float fontSize = 10;
        currentFont = currentFont.deriveFont(fontSize);
        StdDraw.setFont(currentFont);

        Random rand = new Random();
        ArrayList<Point> points = new ArrayList<Point>();
        for(int i = 0; i < NUM_POINTS; i++) {
            points.add(new Point(rand.nextInt(CANVAS_SIZE - 100) + 50, rand.nextInt(CANVAS_SIZE - 100) + 50));
        }

        for(int i = 0; i < points.size(); i++) {
            double xPos = points.get(i).getX();
            double yPos = points.get(i).getY();
            StdDraw.filledCircle(xPos, yPos, 2);
            StdDraw.text(xPos, yPos + 10, "(" + xPos +", " + yPos+")");
        }

        StdDraw.show();

        ArrayList<Point> ch = quickHull(points);

        //draw line ab
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.line(ch.get(0).x, ch.get(0).y, ch.get(1).x, ch.get(1).y);

        //draw whole convexHull
        for(int i = 0; i< ch.size()-1; i++) {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.line(ch.get(i).x, ch.get(i).y, ch.get(i+1).x, ch.get(i+1).y);
        }
    }

    public static ArrayList<Point> quickHull(ArrayList<Point> pointList) {
        //find the points with the minimum and maximum X coordinates (a,b)
        //(a) is min, (b) is max
        Point minX = new Point(-1,-1);
        Point maxX = new Point(-1,-1);
        int a = Integer.MAX_VALUE;
        int b = Integer.MIN_VALUE;
        for(int i = 0; i < pointList.size(); i++) {
            //find minX
            if(pointList.get(i).x < a) {
                a = pointList.get(i).x;
                minX = pointList.get(i);
            }
            //find maxX
            if(pointList.get(i).x > b) {
                b = pointList.get(i).x;
                maxX = pointList.get(i);
            }
        }

        //create two arrayLists: upper containing points above line ab
        //and lower containing points below the line ab

        ArrayList<Point> upper = new ArrayList<Point>();
        ArrayList<Point> lower = new ArrayList<Point>();

        for(Point p: pointList) {
            if(leftTurn(minX, maxX, p)) {
                upper.add(p);
            }
            else if(!leftTurn(minX, maxX, p)) {
                lower.add(p);
            }
        }

        //create an ArrayList of points what will be on the convex hull
        ArrayList<Point> ch = new ArrayList<Point>();

        //upperHull and lowerHull
        ArrayList<Point> upperFinal = new ArrayList<Point>();
        ArrayList<Point> lowerFinal = new ArrayList<Point>();

        ch.add(minX);
        ch.add(maxX);

        upperFinal.add(minX);
        upperFinal.add(maxX);
        lowerFinal.add(minX);
        lowerFinal.add(maxX);

        pointList.remove(minX);
        pointList.remove(maxX);

        //make two calls to a recursive method that computes the upper and lower hulls
        upperFinal = quickHull(upper, minX, maxX, upperFinal);
        lowerFinal = quickHull(lower, maxX, minX , lowerFinal);

        sortDrawHulls(upperFinal,lowerFinal);

        return ch;
    }

    //recursive method that computes hull
    public static ArrayList<Point> quickHull(ArrayList<Point> pointList, Point a, Point b, ArrayList<Point> result) {
        //furthest = furtherest away from line ab
        Point furthest = null;

        double maxDist = Double.MIN_VALUE;
        for(Point p : pointList) {
            if(valueBasedOnLineDistance(a, b, p)/2 > maxDist && !result.contains(p)) {
                furthest = p;
                maxDist = valueBasedOnLineDistance(a, b, p)/2;
            }
        }

        //add furthest to result if it exists
        if(furthest != null) {
            result.add(furthest);
            pointList.remove(furthest);

            //create two arrays to store points: left and right
            ArrayList<Point> left = new ArrayList<Point>();
            ArrayList<Point> right = new ArrayList<Point>();

            for(Point p : pointList){
                if(leftTurn(a, furthest, p)){
                    left.add(p);
                }
                else if(!leftTurn(b, furthest, p)){
                    right.add(p);
                }
            }

            quickHull(left, a, furthest, result);
            quickHull(right, furthest, b, result);
        }
        return result;
    }


    //find value that corresponds to twice the area of triangle abp
    public static double valueBasedOnLineDistance(Point a, Point b, Point p) {
        double v1x = b.x - a.x;
        double v1y = b.y - a.y;

        double v2x = p.x - a.x;
        double v2y = p.y - a.y;

        return Math.abs((v1x * v2y) - (v1y * v2x));
    }


    public static boolean leftTurn(Point a, Point b, Point i) {
        //Vab = b-a
        double vABx = b.x - a.x;
        double vABy = b.y - a.y;

        //Vbc = c-b
        double vBCx = i.x - b.x;
        double vBCy = i.y - b.y;

        //crossP = Vab.x Vbc.y - Vab.y Vbc.x // cross product Vab * Vbc
        double crossP = (vABx * vBCy) - (vABy * vBCx);


        return (crossP > 0);
    }

    public static void sortDrawHulls(ArrayList<Point> upperFinal, ArrayList<Point> lowerFinal) {
        //sort upperFinal hull
        //make array of the x values, sort they array, then use it as a reference to sort the ArrayList
        int[] upperXVals = new int[upperFinal.size()];
        //input unsorted x values into array
        for(int i = 0; i < upperXVals.length; i++) {
            upperXVals[i] = upperFinal.get(i).x;
        }

        Arrays.sort(upperXVals);

        //use array as reference to sort ArrayList
        for(int i = 0; i < upperXVals.length; i++) {
            for(int j = 0; j < upperFinal.size(); j++) {
                if(upperXVals[i] == upperFinal.get(j).x) {
                    Point temp = upperFinal.remove(j);
                    upperFinal.add(i, temp);
                }
            }
        }


        //make array of the x values, sort the array, then use it as a reference to sort the ArrayList
        int[] lowerXVals = new int[lowerFinal.size()];

        //input unsorted x values into array
        for(int i = 0; i < lowerXVals.length; i++) {
            lowerXVals[i] = lowerFinal.get(i).x;
        }
        Arrays.sort(lowerXVals);

        //use array as reference to sort ArrayList
        for(int i = 1; i < lowerXVals.length; i++) {
            for(int j = 0; j < lowerFinal.size(); j++) {
                if(lowerXVals[i] == lowerFinal.get(j).x) {
                    Point temp = lowerFinal.remove(j);
                    lowerFinal.add(i, temp);
                }
            }
        }

        //draw upper hull
        for(int i = 1; i < upperFinal.size(); i++) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.line(upperFinal.get(i-1).x, upperFinal.get(i-1).y, upperFinal.get(i).x, upperFinal.get(i).y);
        }
        //Draw lower hull
        for(int i = 1; i < lowerFinal.size(); i++) {
            StdDraw.setPenColor(StdDraw.GREEN);
            StdDraw.line(lowerFinal.get(i-1).x, lowerFinal.get(i-1).y, lowerFinal.get(i).x, lowerFinal.get(i).y);
        }
    }
}
