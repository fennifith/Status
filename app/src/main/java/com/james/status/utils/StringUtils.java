/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
