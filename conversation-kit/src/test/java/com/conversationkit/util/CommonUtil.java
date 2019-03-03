/**
 * 
 */
package com.conversationkit.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Prashanth_Meka
 *
 */
public class CommonUtil {
	
	public static void main(String[] args) {
		String matchRegex = "i would like to travel from (\\w+) to (\\w+) on ([0-9]{1,2}.+[a-zA-Z]{3})";
		String response = "i would like to travel from Chennai to Eluru on 02nd-Feb";
		Pattern pattern = Pattern.compile(matchRegex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(response);
		while(matcher.find()) {
			System.out.println(matcher.group());
			for (int i = 1; i <= matcher.groupCount(); i++) {
				System.out.println(matcher.group(i));
			}
		}
	}

}
