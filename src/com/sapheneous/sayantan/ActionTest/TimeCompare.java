/**
 * 
 */
package com.sapheneous.sayantan.ActionTest;

import java.time.ZonedDateTime;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import org.apache.commons.net.ntp.NTPUDPClient; 
import org.apache.commons.net.ntp.TimeInfo;

/**
 * @author me
 *
 */
public class TimeCompare {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
<<<<<<< HEAD
		//System.out.println(ZonedDateTime.now());
=======
		System.out.println("Hello World");
		System.out.println(ZonedDateTime.now());
>>>>>>> 26a317bde8008b9936a40c3435bd68e50c55ca97
		long sysTime = System.currentTimeMillis();
		
		String TIME_SERVER = "time-a.nist.gov";   
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
        TimeInfo timeInfo = timeClient.getTime(inetAddress);
        long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
        long diffTime = returnTime - sysTime;
        
        Date ntpTime = new Date(returnTime);
        Date localTime = new Date(sysTime);
        System.out.println("Time on local machine: " + localTime);
        System.out.println("Time from " + TIME_SERVER + ": " + ntpTime);
        
<<<<<<< HEAD
        System.out.println("We have a time offset of " + diffTime + " milliseconds.\n\n\n");
=======
        System.out.println("We have a time offset of " + diffTime);
>>>>>>> 26a317bde8008b9936a40c3435bd68e50c55ca97
	}

}
