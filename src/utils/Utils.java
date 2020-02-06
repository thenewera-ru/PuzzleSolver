package utils;


public class Utils {

    public static int[] getPosition(int value, int width) {
        int i = value / width;
        int j = value % width;
        int[] ans = {i, j};
        return ans;
    }
}
