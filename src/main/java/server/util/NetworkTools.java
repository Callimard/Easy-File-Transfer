package server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkTools {

	// Constants.

	private final static String EXTERNAL_IP_ADDRESS = "192.168.1.14";

	// Methods.

//	private static String getBoxIpAddressOnline() {
//		URL whatismyip = null;
//
//		try {
//			whatismyip = new URL("http://checkip.amazonaws.com");
//
//			try (BufferedReader reader = new BufferedReader(new InputStreamReader(whatismyip.openStream()));) {
//				String ip = reader.readLine(); // you get the IP as a String
//				return ip;
//			} catch (IOException e) {
//				ErrorManager.writeError(e);
//				return null;
//			}
//
//		} catch (MalformedURLException e) {
//			ErrorManager.writeError(e);
//			return null;
//		}
//	}

	/**
	 * 
	 * Return the Ip address which external machin will use to dialog with you (this
	 * ip can be the ip of your box or your proxy etc).
	 * 
	 * @return the external adress ip, if there is a problem return null.
	 */
	public static String getBoxIpAddress() {
		return EXTERNAL_IP_ADDRESS;
	}

}
