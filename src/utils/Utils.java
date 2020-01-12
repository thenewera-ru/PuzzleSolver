package utils;


public class Utils {

    public static int[] getPositionFromMatrix(int value, int height, int width) {
        int i = value / width;
        int j = value % width;
        int[] ans = {i, j};
        return ans;
    }
}
