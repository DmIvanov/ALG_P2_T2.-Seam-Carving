import edu.princeton.cs.algs4.*;

import java.awt.*;
import java.sql.Timestamp;

public class SeamCarver {

    private Picture picture;
    private Color[][] picturePixels;
    private EdgeWeightedDigraph energyDigraph;

    private int topVert;
    private int bottomVert;
    private int leftVert;
    private int rightVert;


    private class SeamRequestStruct {
        EdgeWeightedDigraph digraph;
        int startVert;
        int endVert;
        int alongDimension;
        int acrossDimension;
    }


    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        setNewPicture(picture);
    }

    // current picture
    public Picture picture() {
        return picture;
    }

    public static void main(String[] args) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp);

        //String picPath = "pics/1.png";
        String picPath = "pics/balloon-2331488_960_720.jpg";
        //String picPath = "seam-test/6x5.png";
        Picture picture = new Picture(picPath);
        SeamCarver carver = new SeamCarver(picture);

        StdOut.printf("Passed: ");
        for (int i = 0; i < 200; i++) {
            int[] seam = carver.findVerticalSeam();
            carver.removeVerticalSeam(seam);
            StdOut.printf(" %d,", i);
        }
        StdOut.println("");

        carver.picture().save("pics/output.png");

        timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp);
    }

    // width of current picture
    public int width() {
        return picture.width();
    }

    // height of current picture
    public int height() {
        return picture.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        return SeamCarver.energy(picture, x, y);
    }
//
//    // sequence of indices for horizontal seam
//    public   int[] findHorizontalSeam() {
//
//    }
//
    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        SeamRequestStruct requestData = new SeamRequestStruct();
        requestData.startVert = topVert;
        requestData.endVert = bottomVert;
        requestData.alongDimension = picture.height();
        requestData.acrossDimension = picture.width();
        requestData.digraph = energyDigraph;
        return findSeam(requestData);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam.length != picture.height()) {
            throw new IllegalArgumentException("Wrong seam length");
        }
        Picture newPicture = new Picture(picture.width()-1, picture.height());
        for (int y = 0; y < picture.height(); y++) {
            int newX = 0;
            for (int x = 0; x < picture.width(); x++) {
                if (x == seam[y]) { continue; }
                Color pixelColor = picture.get(x,y);
                newPicture.set(newX,y,pixelColor);
                newX++;
            }
        }
        setNewPicture(newPicture);
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam.length != picture.width()) {
            throw new IllegalArgumentException("Wrong seam length");
        }
        for (int x = 0; x < picture.width(); x++) {
            for (int y = 0; y < picture.height(); y++) {

            }
        }
    }


    // PRIVATE
    private void setNewPicture(Picture newPicture) {
        this.picture = new Picture(newPicture);
        double[][] energyMatrix = SeamCarver.energyMatrix(newPicture);
        this.energyDigraph = digraph(energyMatrix);
        //StdOut.println(energyDigraph.toString());
    }

    static private double energy(Picture picture, int x, int y) {
        if (x <= 0 || y <= 0 || x >= picture.width()-1 || y >= picture.height()-1) {
            throw new IllegalArgumentException("Pixel is out of range");
        }
        Color colorXLeft = picture.get(x-1,y);
        Color colorXRight = picture.get(x+1,y);
        Color colorYUp = picture.get(x,y-1);
        Color colorYBottom = picture.get(x,y+1);

        int RXLeft = colorXLeft.getRed();
        int GXLeft = colorXLeft.getGreen();
        int BXLeft = colorXLeft.getBlue();

        int RXRight = colorXRight.getRed();
        int GXRight = colorXRight.getGreen();
        int BXRight = colorXRight.getBlue();

        int RYUp = colorYUp.getRed();
        int GYUp = colorYUp.getGreen();
        int BYUp = colorYUp.getBlue();

        int RYBottom = colorYBottom.getRed();
        int GYBottom = colorYBottom.getGreen();
        int BYBottom = colorYBottom.getBlue();

        double delta_x_2 = Math.pow((double)(RXLeft - RXRight), 2) + Math.pow((double)(GXLeft - GXRight), 2) + Math.pow((double)(BXLeft - BXRight), 2);
        double delta_y_2 = Math.pow((double)(RYUp - RYBottom), 2) + Math.pow((double)(GYUp - GYBottom), 2) + Math.pow((double)(BYUp - BYBottom), 2);
        double delta = delta_x_2 + delta_y_2;

        return Math.sqrt(delta);
    }

    static private double[][] energyMatrix(Picture picture) {
        double[][] matrix = new double[picture.width()][picture.height()];
        for (int x = 0; x < picture.width(); x++) {
            for (int y = 0; y < picture.height(); y++) {
                if (x == 0 || y == 0 || x == picture.width()-1 || y == picture.height()-1) {
                    matrix[x][y] = 1000;
                    continue;
                }
                double energy = energy(picture, x, y);
                matrix[x][y] = energy;
            }
        }

        return matrix;
    }

    private EdgeWeightedDigraph digraph(double[][] valueMatrix) {

        int width = valueMatrix.length;
        int height = valueMatrix[0].length;

        float divider = 1000;

        topVert = height * width;
        bottomVert = topVert + 1;
        leftVert = bottomVert + 1;
        rightVert = leftVert + 1;

        EdgeWeightedDigraph digraph = new EdgeWeightedDigraph(rightVert + 1);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //StdOut.printf("x: %d, y: %d\n", x, y);
                float en = (float)valueMatrix[x][y];
                //Color color = new Color(en,en,en);
                //newPic.set(x,y,color);
                int vert = index(width, x, y);
                if (x < width-1 && y < height-1) {
                    int toVert = index(width,x+1,y+1);
                    DirectedEdge edge = new DirectedEdge(vert, toVert, en);
                    digraph.addEdge(edge);
                }
                if (y < height-1) {
                    int toVert = index(width, x,y+1);
                    DirectedEdge edge = new DirectedEdge(vert, toVert, en);
                    digraph.addEdge(edge);
                }
                if (x > 0 && y < height-1) {
                    int toVert = index(width,x-1,y+1);
                    DirectedEdge edge = new DirectedEdge(vert, toVert, en);
                    digraph.addEdge(edge);
                }

                if (y == 0) {
                    DirectedEdge edge = new DirectedEdge(topVert, vert, divider);
                    digraph.addEdge(edge);
                }
                if (x == width-1) {
                    DirectedEdge edge = new DirectedEdge(vert, rightVert, divider);
                    digraph.addEdge(edge);
                }
                if (x == 0) {
                    DirectedEdge edge = new DirectedEdge(leftVert, vert, divider);
                    digraph.addEdge(edge);
                }
                if (y == height-1) {
                    DirectedEdge edge = new DirectedEdge(vert, bottomVert, divider);
                    digraph.addEdge(edge);
                }
            }
        }

        return digraph;
    }

    static private int[] findSeam(SeamRequestStruct data) {
        AcyclicSP search = new AcyclicSP(data.digraph, data.startVert);
        Iterable<DirectedEdge> path = search.pathTo(data.endVert);
        int[] seamIndeces = new int[data.alongDimension];
        int i = 0;
        boolean firstVertPassed = false;
        for (DirectedEdge edge: path) {
            if (!firstVertPassed) {
                firstVertPassed = true;
                continue;
            }
            int vert = edge.from();
            seamIndeces[i++] = SeamCarver.x(data.acrossDimension, vert);
        }
        return seamIndeces;
    }

    static private Color[][] pixelMatrix(Picture picture) {
        int width = picture.width();
        int height = picture.height();
        Color[][] matrix = new Color[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = picture.get(x,y);
                matrix[x][y] = color;
            }
        }
        return matrix;
    }

    static private Picture picture(Color[][] pixelMatrix) {
        int width = pixelMatrix.length;
        int height = pixelMatrix[0].length;
        Picture picture = new Picture(width,height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                picture.set(x,y,pixelMatrix[x][y]);
            }
        }
        return picture;
    }

    static private int index(int width, int x, int y) {
        return y * width + x;
    }

    static private int x(int width, int index) {
        return index % width;
    }

    static private int y(int width, int index) {
        return index / width;
    }
}
