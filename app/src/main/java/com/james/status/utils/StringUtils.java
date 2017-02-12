package com.james.status.utils;

public class StringUtils {

    public static String difference(String str1, String str2) {
        if (str1 == null) return str2;
        else if (str2 == null) return str1;
        else if (!str1.equals(str2)) {
            int i;
            for (i = 0; i < str1.length() && i < str2.length(); ++i) {
                if (str1.charAt(i) != str2.charAt(i))
                    break;
            }

            if (i < str2.length() || i < str1.length())
                return str2.substring(i);
        }

        return "";
    }

}
