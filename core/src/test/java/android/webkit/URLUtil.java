package android.webkit;

public class URLUtil {
    public static boolean isValidUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        return true;
    }
}
