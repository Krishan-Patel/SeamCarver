/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */


import edu.princeton.cs.algs4.Picture;
import java.awt.Color;

public class SeamCarver {
    private int[][] picture;
    private double[][] energyGrid;
    private int width;
    private int height;
    private boolean transposed;
    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) { throw  new IllegalArgumentException(); }
        width = picture.width();
        height = picture.height();
        transposed = false;
        this.picture = new int[width][height];
        this.energyGrid = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.picture[i][j] = picture.getRGB(i, j);
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                energyGrid[i][j] = calcEnergy(i, j);
            }
        }
    }

    // current picture
    public Picture picture() {
        if (transposed) transpose();
        Picture pic = new Picture(width, height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pic.setRGB(i, j, this.picture[i][j]);
            }
        }
        return pic;
    }

    // width of current picture
    public int width() {
        if (transposed) return height;
        return width;
    }

    // height of current picture
    public int height() {
        if (transposed) return width;
        return height;
    }

    public double energy(int x, int y) {
        if (transposed) return calcEnergy(y, x);
        return calcEnergy(x, y);
    }
    // energy of pixel at column x and row y
    private double calcEnergy(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) { throw new IllegalArgumentException(); }
        if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
            return 1000.00;
        }
        double e = Math.pow(calcX(x, y) + calcY(x, y), 0.5);
        return e;
    }

    private double calcX(int x, int y) {
        Color left = new Color(picture[x - 1][y]);
        Color right = new Color(picture[x + 1][y]);
        return Math.pow(left.getRed() - right.getRed(), 2) + Math.pow(left.getBlue() - right.getBlue(), 2) + Math.pow(left.getGreen() - right.getGreen(), 2);
    }

    private double calcY(int x, int y) {
        Color top = new Color(picture[x][y - 1]);
        Color bot = new Color(picture[x][y + 1]);
        return Math.pow(top.getRed() - bot.getRed(), 2) + Math.pow(top.getBlue() - bot.getBlue(), 2) + Math.pow(top.getGreen() - bot.getGreen(), 2);
    }

    private void relax(int x, int y, double[][] distTo, int[][] edgeTo) {
        if (y > 0) { // row above exists
            if (distTo[x][y - 1] + energyGrid[x][y - 1] < distTo[x][y]) {
                distTo[x][y] = distTo[x][y - 1] + energyGrid[x][y - 1];
                edgeTo[x][y] = (y - 1) * width + x;
            }
        }
        if (y > 0 && x < width - 1) {
            if (distTo[x + 1][y - 1] + energyGrid[x + 1][y - 1] < distTo[x][y]) {
                distTo[x][y] = distTo[x + 1][y - 1] + energyGrid[x + 1][y - 1];
                edgeTo[x][y] = (y - 1) * width + (x + 1);
            }
        }
        if (y > 0 && x > 0) {
            if (distTo[x - 1][y - 1] + energyGrid[x - 1][y - 1] < distTo[x][y]) {
                distTo[x][y] = distTo[x - 1][y - 1] + energyGrid[x - 1][y - 1];
                edgeTo[x][y] = (y - 1) * width + (x - 1);
            }
        }

    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        if (picture == null) { throw  new IllegalArgumentException(); }
        if (!transposed) { transpose(); }
        int[] result = findVerticalSeam(true);
        return result;
    }

    private int[] findVerticalSeam(boolean subroutine) {
        if (picture == null) { throw  new IllegalArgumentException(); }
        if (transposed && !subroutine) { transpose(); }
        int[][] edgeTo = new int[width][height];
        double[][] distTo = new double[width][height];
        int[] path = new int[height];
        // finds the shortest path
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++)
                if (row == 0) {
                    distTo[col][row] = 0.0;
                    edgeTo[col][row] = -1;
                }
                else {
                    distTo[col][row] = Double.POSITIVE_INFINITY;
                    relax(col, row, distTo, edgeTo);
                }
        }
        double minDist = Double.POSITIVE_INFINITY;
        int x = 0;
        for (int i = 0; i < width; i++) {
            if (distTo[i][height - 1] < minDist) {
                minDist = distTo[i][height - 1];
                x = i;
            }
        }

        int idx = edgeTo[x][height - 1];
        path[path.length - 1] = x;
        for (int j = 1; j < height; j++) {
            int col = idx % width;
            int row = idx / width;
            idx = edgeTo[col][row];
            path[path.length - 1 - j] = col;

        }
        return path;
    }
    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return findVerticalSeam(false);
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (!transposed) { transpose(); }
       /* if (seam == null || seam.length != width || height <= 1) { throw new IllegalArgumentException(); }
        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] >= height) throw new IllegalArgumentException();
            if (i < seam.length - 1 && Math.abs(seam[i] - seam[i + 1]) > 1) {
                throw new IllegalArgumentException();
            }
        } */
        removeVerticalSeam(seam, true);
    }

    private void removeVerticalSeam(int[] seam, boolean subroutine) {
        if (transposed && !subroutine) { transpose(); }
        if (seam == null || seam.length != height || width <= 1) { throw new IllegalArgumentException(); }
        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] >= width) throw new IllegalArgumentException();
            if (i < seam.length - 1 && Math.abs(seam[i] - seam[i + 1]) > 1) {
                throw new IllegalArgumentException();
            }
        }
        width -= 1;
        for (int row = 0; row < height; row++) {
            for (int col = seam[row]; col < width; col++) {
                picture[col][row] = picture[col + 1][row];
                energyGrid[col][row] = energyGrid[col + 1][row];
            }
        }
        for (int row = 0; row < height; row++) {
            if (seam[row] - 1 >= 0) {
                energyGrid[seam[row] - 1][row] = calcEnergy(seam[row] - 1, row);
            }
            if (seam[row] < width) {
                energyGrid[seam[row]][row] = calcEnergy(seam[row], row);
            }
        }
    }
    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        removeVerticalSeam(seam, false);
    }

    private void transpose() {
        int newHeight = width;
        int newWidth = height;
        int[][] pic = new int[newWidth][newHeight];
        double[][] energy = new double[newWidth][newHeight];
        for (int i = 0; i < newWidth; i++) {
            for (int j = 0; j < newHeight; j++) {
                pic[i][j] = this.picture[j][i];
                energy[i][j] = this.energyGrid[j][i];
            }
        }
        transposed = !transposed;
        width = newWidth;
        height = newHeight;
        this.energyGrid = energy;
        picture = pic;
    }

    public void toGrayScale() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color colour = new Color(picture[i][j]);
                int grayScale = (int) (colour.getRed() * 0.3 + colour.getGreen() * 0.59 + colour.getBlue() * 0.11);
                picture[i][j] = new Color(grayScale, grayScale, grayScale).getRGB();
            }
        }
    }

    public void invertColor() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color colour = new Color(picture[i][j]);
                picture[i][j] = new Color(255 - colour.getRed(), 255 - colour.getGreen(), 255 - colour.getBlue()).getRGB();
            }
        }
    }

    public void horizontalFlip() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width / 2; col++) {
                int copy = picture[col][row];
                picture[col][row] = picture[width - 1 - col][row];
                picture[width - 1 - col][row] = copy;
            }
        }
    }

    public void verticalFlip() {
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width; col++) {
                int copy = picture[col][row];
                picture[col][row] = picture[col][height - 1 - row];
                picture[col][height - 1 - row] = copy;
            }
        }
    }

    public void resize(int width, int height) {
        while (width != this.width) {
            removeVerticalSeam(findVerticalSeam());
        }
        while (height != this.height) {
            removeHorizontalSeam(findHorizontalSeam());
        }
    }
    //  unit testing (optional)
    public static void main(String[] args) {
    }

}
