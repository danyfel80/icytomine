package org.bioimageanalysis.icy.icytomine.core.connection.client;

import java.io.UnsupportedEncodingException;

public abstract class CytomineClientUtils {
	public static String convertFromSystenEncodingToUTF8(String string) {
		String result = string;
		try {
			result = new String(result.getBytes(System.getProperty("file.encoding")), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}
}
