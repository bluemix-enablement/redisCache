package com.ibm.cloudoe.ecaas.samples;

/**
 * Define the StringUtil.
 * To prevent the Xss issue from happening the JSON, so string value must be properly escaped.
 */
public class StringUtil {

	// String escaped characters mapping
	private static final String invalidStr[] = { ">", "<", "\"", "\\" };
	private static final String escapedStr[] = { "&gt;", "&lt;", "&quot;", "\\\\" };

	public static String htmlEncode(String str) {
		if (str == null) {
			return null;
		}

		for (int i = 0; i < invalidStr.length; i++)
			str = change(str, invalidStr[i], escapedStr[i]);
		return str;
	}

	public static String change(String in, String oldPat, String newPat) {
		if (oldPat.length() == 0)
			return in;
		if (oldPat.length() == 1 && newPat.length() == 1)
			return in.replace(oldPat.charAt(0), newPat.charAt(0));
		if (in.indexOf(oldPat) < 0)
			return in;
		int lastIndex = 0;
		int newIndex = 0;
		StringBuffer newString = new StringBuffer();
		for (;;) {
			newIndex = in.indexOf(oldPat, lastIndex);
			if (newIndex != -1) {
				newString.append(in.substring(lastIndex, newIndex) + newPat);
				lastIndex = newIndex + oldPat.length();
			} else {
				newString.append(in.substring(lastIndex));
				break;
			}
		}
		return newString.toString();
	}
}
