/**
 * @author Brian Weir (https://github.com/bweir27)
 * ConvexHull/QuickHull runtime analysis
 */

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Random;


public class ConvexHullTimeAnalysis {
    public static void main(String[] args){
        System.out.println("This class is a running time analysis of QuickHull.\n");

        Random rand = new Random();
        System.out.println("Running time (ms):\n");
        System.out.println("n:\t\tQuickHull:\t\tBruteForce:");
        System.out.println("-------------------------------------");

        //n = number of elements (compare with brute force)
        for(int n = 10; n <= 1500; n*=2) {
            ArrayList<Point> points = new ArrayList<Point>();
            ArrayList<Point> ch = new ArrayList<Point>();
            ArrayList<Line2D> bh = new ArrayList<Line2D>();

            //n fills the ArrayList
            for(int i = 0; i < n; i++) {
                points.add(new Point(rand.nextInt() + 1, rand.nextInt() + 1));
            }
            long hullStart = System.currentTimeMillis();
            ch = quickHull(points);
            long hullEnd = System.currentTimeMillis();
            long hullTime = hullEnd - hullStart;

            long bruteStart = System.currentTimeMillis();
            bh = convexHullBrute(points);
            long bruteEnd = System.currentTimeMillis();
            long bruteTime = bruteEnd - bruteStart;

            System.out.println(n + "\t\t\t" + hullTime + "\t\t\t" + bruteTime);
        }

        System.out.println("\n\nRigorous running time analysis:\n");
        System.out.println("n:\t\tRunning Time (ms):");
        System.out.println("-------------------------------------");

        //i = #k of elements
        for(int i = 10; i <= 15000; i*=2) {
            ArrayList<Point> p = new ArrayList<Point>();
            ArrayList<Point> ch = new ArrayList<Point>();

            //n fills the ArrayList
            for(int n = 0; n < i*1000; n++) {
                p.add(new Point(rand.nextInt() + 1, rand.nextInt() + 1));
            }
            long hullStart = System.currentTimeMillis();
            ch = quickHull(p);
            long hullEnd = System.currentTimeMillis();
            long hullTime = hullEnd - hullStart;

            System.out.println(i + "k\t\t\t" + hullTime);
        }
    }

    //quickHull
    public static ArrayList<Point> quickHull(ArrayList<Point> s) {
        //find the points with the minimum and maximum X coordinates (a,b)
        //(a) is min, (b) is max
        Point minX = new Point(-1,-1);
        Point maxX = new Point(-1,-1);
        int a = Integer.MAX_VALUE;
        int b = Integer.MIN_VALUE;
        for(int i = 0; i < s.size(); i++) {
            //find minX
            if(s.get(i).x < a) {
                a = s.get(i).x;
                minX = s.get(i);
            }
            //find maxX
            if(s.get(i).x > b) {
                b=s.get(i).x;
                maxX = s.get(i);
            }
        }

        //create two arrayLists: upper containing points above line ab
        //and lower containing points below the line ab

        ArrayList<Point> upper = new ArrayList<Point>();
        ArrayList<Point> lower = new ArrayList<Point>();

        for(Point p: s) {
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

        s.remove(minX);
        s.remove(maxX);

        //make two calls to a recursive method that computes the upper and lower hulls
        quickHull(upper, minX, maxX, ch);
        quickHull(lower, maxX, minX , ch);

        return ch;
    }

    //recursive method that computes hull
    public static ArrayList<Point> quickHull(ArrayList<Point> s, Point a, Point b, ArrayList<Point> result) {
        //furthest = furtherest away from line ab
        Point furthest = null;

        double maxDist = Double.MIN_VALUE;
        for(Point p: s) {
            if(valueBasedOnLineDistance(a, b, p)/2 > maxDist && !result.contains(p)) {
                furthest = p;
                maxDist = valueBasedOnLineDistance(a, b, p)/2;
            }
        }

        //add furthest to result if it exists
        if(furthest != null) {
            result.add(furthest);
            s.remove(furthest);

            //create two arrays to store points: left and right
            ArrayList<Point> left = new ArrayList<Point>();
            ArrayList<Point> right = new ArrayList<Point>();

            for(Point p : s) {
                if(leftTurn(a, furthest, p)) {
                    left.add(p);
                }
                else if(!leftTurn(b, furthest, p)) {
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


    //determine if leftTurn
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

    public static ArrayList<Line2D> convexHullBrute(ArrayList<Point> s){
        ArrayList<Line2D> ch = new ArrayList<Line2D>();

        for(Point pi: s) {
            for(Point pj:s) {

                if(!pj.equals(pi)) {
                    int leftTurnCount = 0;
                    for(Point pk:s) {
                        if(!pk.equals(pi) && !pk.equals(pj)) {
                            if(leftTurn(pi,pj,pk)) {
                                leftTurnCount++;
                            }
                        }
                    }
                    if(leftTurnCount == 0 || leftTurnCount== s.size()-2) {
                        ch.add(new Line2D.Double(pi,pj));
                    }
                }
            }
        }
        return ch;
    }
}
