package com.sapheneous.sayantan.ActionTest;

/**
 * 
 */
import java.time.ZonedDateTime;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

/**
 * @author me
 *
 */
public class OffsetReport {

	/**
	 * @param args
	 * @throws IOException
	 */

	private static final NumberFormat numberFormat = new java.text.DecimalFormat("0.00");
	private static String TIME_SERVER;

	/**
	 * Process <code>TimeInfo</code> object and print its details.
	 * 
	 * @param info
	 *            <code>TimeInfo</code> object.
	 * @throws IOException 
	 */
	public static void processResponse(TimeInfo info) throws IOException {
		NtpV3Packet message = info.getMessage();
		int stratum = message.getStratum();
		String refType;
		if (stratum <= 0) {
			refType = "(Unspecified or Unavailable)";
		} else if (stratum == 1) {
			refType = "(Primary Reference; e.g., GPS)"; // GPS, radio clock,
														// etc.
		} else {
			refType = "(Secondary Reference; e.g. via NTP or SNTP)";
		}
		// stratum should be 0..15...
		System.out.println(">> Roundtrip attempt 1:");
		System.out.println(" Stratum: " + stratum + " " + refType);
		int version = message.getVersion();
		int li = message.getLeapIndicator();
		System.out.println(" leap=" + li + ", version=" + version + ", precision=" + message.getPrecision());

		System.out.println(" mode: " + message.getModeName() + " (" + message.getMode() + ")");
		int poll = message.getPoll();
		// poll value typically btwn MINPOLL (4) and MAXPOLL (14)
		System.out.println(" poll: " + (poll <= 0 ? 1 : (int) Math.pow(2, poll)) + " seconds" + " (2 ** " + poll + ")");
		double disp = message.getRootDispersionInMillisDouble();
		System.out.println(" rootdelay=" + numberFormat.format(message.getRootDelayInMillisDouble())
				+ ", rootdispersion(ms): " + numberFormat.format(disp));

		int refId = message.getReferenceId();
		String refAddr = NtpUtils.getHostAddress(refId);
		String refName = null;
		if (refId != 0) {
			if (refAddr.equals("127.127.1.0")) {
				refName = "LOCAL"; // This is the ref address for the Local
									// Clock
			} else if (stratum >= 2) {
				// If reference id has 127.127 prefix then it uses its own
				// reference clock
				// defined in the form 127.127.clock-type.unit-num (e.g.
				// 127.127.8.0 mode 5
				// for GENERIC DCF77 AM; see refclock.htm from the NTP software
				// distribution.
				if (!refAddr.startsWith("127.127")) {
					try {
						InetAddress addr = InetAddress.getByName(refAddr);
						String name = addr.getHostName();
						if (name != null && !name.equals(refAddr)) {
							refName = name;
						}
					} catch (UnknownHostException e) {
						// some stratum-2 servers sync to ref clock device but
						// fudge stratum level higher... (e.g. 2)
						// ref not valid host maybe it's a reference clock name?
						// otherwise just show the ref IP address.
						refName = NtpUtils.getReferenceClock(message);
					}
				}
			} else if (version >= 3 && (stratum == 0 || stratum == 1)) {
				refName = NtpUtils.getReferenceClock(message);
				// refname usually have at least 3 characters (e.g. GPS, WWV,
				// LCL, etc.)
			}
			// otherwise give up on naming the beast...
		}
		if (refName != null && refName.length() > 1) {
			refAddr += " (" + refName + ")";
		}
		System.out.println(" Reference Identifier:\t" + refAddr);

		TimeStamp refNtpTime = message.getReferenceTimeStamp();
		System.out.println(" Reference Timestamp:\t" + refNtpTime + "  " + refNtpTime.toDateString());

		// Originate Time is time request sent by client (t1)
		TimeStamp origNtpTime = message.getOriginateTimeStamp();
		System.out.println(" Originate Timestamp:\t" + origNtpTime + "  " + origNtpTime.toDateString());

		long destTime = info.getReturnTime();
		// Receive Time is time request received by server (t2)
		TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
		System.out.println(" Receive Timestamp:\t" + rcvNtpTime + "  " + rcvNtpTime.toDateString());

		// Transmit time is time reply sent by server (t3)
		TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
		System.out.println(" Transmit Timestamp:\t" + xmitNtpTime + "  " + xmitNtpTime.toDateString());

		// Destination time is time reply received by client (t4)
		TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
		System.out.println(" Destination Timestamp:\t" + destNtpTime + "  " + destNtpTime.toDateString());

		info.computeDetails(); // compute offset/delay if not already done
		Long offsetValue = info.getOffset();
		Long delayValue = info.getDelay();
		String delay = (delayValue == null) ? "N/A" : delayValue.toString();
		String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();

		System.out.println(" Roundtrip delay(ms)=" + delay);
		
		try {
		    Thread.sleep(10000);                 
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		long sysTime = System.currentTimeMillis();
		
		NTPUDPClient timeClient = new NTPUDPClient();
		InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
		TimeInfo timeInfo = timeClient.getTime(inetAddress);
		long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
		long diffTime = returnTime - sysTime;
		
		System.out.println(">> Round trip attempt 2:");
		String ntpTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXXzzzz").format(new Date(returnTime));
		System.out.println(" Local: " + ZonedDateTime.now());
		System.out.println(" " + TIME_SERVER + ": " + ntpTime);
		System.out.println(" Roundtrip delay (ms)=" + diffTime + "\n");
		
		System.out.println(" Clock Offset(ms)=" + offset + "\n\n\n");
		
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length == 0) {
			System.err.println("Usage: NTPClient <hostname-or-address-list>");
			System.exit(1);
		}
		NTPUDPClient client = new NTPUDPClient();
		// We want to timeout if a response takes longer than 10 seconds
		client.setDefaultTimeout(10000);
		try {
			client.open();
			for (String arg : args) {
				System.out.println();
				try {
					TIME_SERVER = args[0];
					InetAddress hostAddr = InetAddress.getByName(arg);
					System.out.println("> " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
					TimeInfo info = client.getTime(hostAddr);
					processResponse(info);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		client.close();
	}

}
