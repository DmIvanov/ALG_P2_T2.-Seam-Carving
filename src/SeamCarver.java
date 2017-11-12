import edu.princeton.cs.algs4.*;

import java.awt.*;
import java.sql.Timestamp;

public class SeamCarver {

    private Color[][] picturePixels;
    private double[][] energyMatrix;

    private class SeamRequestStruct {
        EdgeWeightedDigraph digraph;
        int startVert;
        int endVert;
        int alongDimension;
        int acrossDimension;
    }


    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        setNewPixelMatrix(pixelMatrix(picture));
    }

    // current picture
    public Picture picture() {
        return picture(picturePixels);
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
            if (i % 2 == 0) {
                int[] seam = carver.findHorizontalSeam();
                carver.removeHorizontalSeam(seam);
            } else {
                int[] seam = carver.findVerticalSeam();
                carver.removeVerticalSeam(seam);
            }
            StdOut.printf(" %d,", i);
        }
        StdOut.println("");

        carver.picture().save("pics/output.png");

        timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp);
    }

    // width of current picture
    public int width() {
        return picturePixels.length;
    }

    // height of current picture
    public int height() {
        if (picturePixels.length <= 0) {
            return 0;
        }
        return picturePixels[0].length;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        return energyMatrix[x][y];
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        EdgeWeightedDigraph verticalDigraph = digraph(energyMatrix);
        SeamRequestStruct requestData = new SeamRequestStruct();
        requestData.startVert = startVertex(verticalDigraph);
        requestData.endVert = endVertex(verticalDigraph);
        requestData.alongDimension = height();
        requestData.acrossDimension = width();
        requestData.digraph = verticalDigraph;

        return findSeam(requestData);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam.length != height()) {
            throw new IllegalArgumentException("Wrong seam length");
        }

        Color[][] newMatrix = new Color[width()-1][height()];
        for (int y = 0; y < height(); y++) {
            int newX = 0;
            for (int x = 0; x < width(); x++) {
                if (x == seam[y]) { continue; }
                newMatrix[newX][y] = picturePixels[x][y];
                newX++;
            }
        }
        setNewPixelMatrix(newMatrix);
    }


    // sequence of indices for horizontal seam
    public   int[] findHorizontalSeam() {
        double[][] transposedEnergyMatrix = transposedMatrix(energyMatrix);
        EdgeWeightedDigraph horizintalDigraph = digraph(transposedEnergyMatrix);
        SeamRequestStruct requestData = new SeamRequestStruct();
        requestData.startVert = startVertex(horizintalDigraph);
        requestData.endVert = endVertex(horizintalDigraph);
        requestData.alongDimension = width();
        requestData.acrossDimension = height();
        requestData.digraph = horizintalDigraph;

        return findSeam(requestData);
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam.length != width()) {
            throw new IllegalArgumentException("Wrong seam length");
        }

        Color[][] newMatrix = new Color[width()][height()-1];
        for (int x = 0; x < width(); x++) {
            int newY = 0;
            for (int y = 0; y < height(); y++) {
                if (y == seam[x]) { continue; }
                newMatrix[x][newY] = picturePixels[x][y];
                newY++;
            }
        }
        setNewPixelMatrix(newMatrix);
    }


    // PRIVATE
    private void setNewPixelMatrix(Color[][] newMatrix) {
        picturePixels = newMatrix;
        energyMatrix = energyMatrix(picturePixels);
    }

    static private double energy(Color[][] pixelMatrix, int x, int y) {
        if (x <= 0 || y <= 0 || x >= pixelMatrix.length-1 || y >= pixelMatrix[0].length-1) {
            throw new IllegalArgumentException("Pixel is out of range");
        }
        Color colorXLeft    = pixelMatrix[x-1][y];
        Color colorXRight   = pixelMatrix[x+1][y];
        Color colorYUp      = pixelMatrix[x][y-1];
        Color colorYBottom  = pixelMatrix[x][y+1];

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

    static private double[][] energyMatrix(Color[][] pixelMatrix) {
        int width = pixelMatrix.length;
        int height = pixelMatrix[0].length;
        double[][] matrix = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || y == 0 || x == width-1 || y == height-1) {
                    matrix[x][y] = 1000;
                    continue;
                }
                double energy = energy(pixelMatrix, x, y);
                matrix[x][y] = energy;
            }
        }

        return matrix;
    }

    static private EdgeWeightedDigraph digraph(double[][] valueMatrix) {

        int width = valueMatrix.length;
        int height = valueMatrix[0].length;

        float divider = 1000;

        int topVert = height * width;
        int bottomVert = topVert + 1;

        EdgeWeightedDigraph digraph = new EdgeWeightedDigraph(bottomVert + 1);

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

    static double[][] transposedMatrix(double[][] originalMatrix) {
        int width = originalMatrix.length;
        int height = originalMatrix[0].length;
        double[][] newMatrix = new double[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                newMatrix[y][x] = originalMatrix[x][y];
            }
        }
        return newMatrix;
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


    static private int startVertex(EdgeWeightedDigraph digraph) {
        return digraph.V()-2;
    }

    static private int endVertex(EdgeWeightedDigraph digraph) {
        return digraph.V()-1;
    }
}
