package wirelessTurtle;

import java.io.BufferedInputStream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This code is beta-quality; it was developed for a science experiment in February 2016.
 */
public class TurtleProbe {

	public static final int 		TEST_ALPHA_COUNT = 5; //10;
	//public static final int 		TEST_BETA_COUNT = 5; // 3;
	public static final int 		TEST_PAUSE = 2000;
	public static final int 		NET_SWITCH_PAUSE = 10000;
	public static final boolean 	PERFORM_NET_SWITCH = false;
	public static final boolean 	PERFORM_QUICK_TEST = false;
	
	/* 
	 * global entry point 
	 */
	public static void main(String[] args) {
		
		System.out.println("starting test-suite...");
		long startTime = java.lang.System.currentTimeMillis();
		
		/* special test loop */
		if (PERFORM_QUICK_TEST) {
			try {
			while (true) {
					TurtleProbe tp = new TurtleProbe();
					tp.performWirelessTestOnMac();
					Thread.sleep(500);
				}
			} catch (Exception e) {
				
			}
		} 
		
		int numNetworks = 2;
		if (!PERFORM_NET_SWITCH)
			numNetworks = 1;
		
		/* loop for two networks. */
		for (int netrun = 0 ; (netrun < numNetworks) ; netrun++) {
			
			/* optionally switch wireless networks. */
			if (PERFORM_NET_SWITCH) {
				try {
				
					// each netrun switches to a different wifi network
					String netChangeCmd = null;
					switch (netrun) {
					case 0:
						netChangeCmd = "networksetup -setairportnetwork en1 coolguybri3 yo1yo2yo3!";
						break;
					case 1:
						netChangeCmd = "networksetup -setairportnetwork en1 coolguybri4 yo1yo2yo3!";
						break;
					}
			
					Runtime rt = Runtime.getRuntime();
					Process pr = rt.exec(netChangeCmd);
					int val = pr.waitFor();
					int exitValue = pr.exitValue();
					System.out.println("network switched, returned with " + exitValue + "(" + val + ")");
					Thread.sleep(NET_SWITCH_PAUSE);
				} catch (Exception e) {
					System.out.println("turtle exception while switching networks");
				}
			}
				
			try {
				System.out.println("starting tests...");
				
				TurtleProbe tp = new TurtleProbe();
				for (int i = 0 ; i < TEST_ALPHA_COUNT ; ++i) {
					tp.performAllAlphaTests(i);
					Thread.sleep(TEST_PAUSE);
					tp.performAllBetaTests(i);
					Thread.sleep(TEST_PAUSE);
				}
	
				tp.finishTests();
			} catch (Exception e) {
				System.out.println("turtle exception");
			}
		}
		
		long endTime = java.lang.System.currentTimeMillis();
		double duration = ((double)endTime - startTime) / 1000.0;
		System.out.format("results: testtime: %f minutes\n", duration / 60.0);
		java.awt.Toolkit.getDefaultToolkit().beep();
	}

	
	/* 
	 * instance members. 
	 */
	List<Double> list_rssi = new ArrayList<Double>();
	List<Double> list_noise = new ArrayList<Double>();
	List<Double> list_tx = new ArrayList<Double>();
	List<Double> list_maxtx = new ArrayList<Double>();
	List<Double> list_ping_time = new ArrayList<Double>();
	List<Double> list_ping_stddev = new ArrayList<Double>();
	List<Double> list_http_time = new ArrayList<Double>();
	List<Double> list_http_rate = new ArrayList<Double>();
	String		networkName = null;
	
	
	/*
	 * This routine calls every test.
	 */
	public void performAllAlphaTests(int testNum) {
		long startTime = java.lang.System.currentTimeMillis();
		//System.out.println("starting alpha test " + testNum + "...");
		performWirelessTestOnMac();
		//performPingTestOnMac();
		long endTime = java.lang.System.currentTimeMillis();
		double duration = ((double)endTime - startTime) / 1000.0;
		System.out.format("...finished alpha test %d in %f seconds\n", testNum, duration);
	}
	
	/*
	 * This routine calls every test.
	 */
	public void performAllBetaTests(int testNum) {
		long startTime = java.lang.System.currentTimeMillis();
		System.out.println("starting beta test " + testNum + "...");
		//performHttpTestOnMac();
		performHttpTest2OnMac();
		long endTime = java.lang.System.currentTimeMillis();
		double duration = ((double)endTime - startTime) / 1000.0;
		//System.out.format("...finished beta test %d in %f seconds\n", testNum, duration);
	}


	/*
	 performWirelessTestOnMac
	 This is what the command line tool prints out:
	 ---------------
	 agrCtlRSSI: -54
     agrExtRSSI: 0
    agrCtlNoise: -88
    agrExtNoise: 0
          state: running
        op mode: station 
     lastTxRate: 145
        maxRate: 217
lastAssocStatus: 0
    802.11 auth: open
      link auth: wpa2-psk
          BSSID: 0:26:bb:76:e:d7
           SSID: coolguybri2
            MCS: 15
        channel: 11
     --------------
     	Here we execute the command, then capture its output, and parse it to get the values we want.
	 */
	public void performWirelessTestOnMac() {
		try {
			/* execute the command. */
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("/usr/sbin/airport -I");
			int val = pr.waitFor();
			int exitValue = pr.exitValue();
			//System.out.println("airport executed, returned with " + exitValue + "(" + val + ")");
			
			/* print it out. */
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			while ((line = input.readLine()) != null) {
				//System.out.println(line);
				
				/* char 0-15 are the attribute name, then a colon, then the value. */
				String keyValue = line.substring(0,  15).trim();
				String lineValue = line.substring(16).trim();
				//System.out.println("\"" + keyValue + "\", \"" + lineValue + "\"");
				if (keyValue.equals("agrCtlRSSI")) {
					int latest_rssi = Integer.valueOf(lineValue);
					System.out.println("rssi = " + latest_rssi);
					list_rssi.add((double) latest_rssi);
				} else if (keyValue.equals("agrCtlNoise")) {
					int latest_noise = Integer.valueOf(lineValue);
					//System.out.println("noise = " + latest_noise);
					list_noise.add((double) latest_noise);
				} else if (keyValue.equals("lastTxRate")) {
					int latest_tx = Integer.valueOf(lineValue);
					System.out.println("tx = " + latest_tx);
					list_tx.add((double) latest_tx);
				} else if (keyValue.equals("maxRate")) {
					int latest_maxtx = Integer.valueOf(lineValue);
					//System.out.println("max-tx = " + latest_maxtx);
					list_maxtx.add((double) latest_maxtx);
				} else if (keyValue.equals("SSID")) {
					String latest_netname = lineValue;
					//System.out.println("name = " + latest_netname);
					
					if (networkName == null) {
						networkName = latest_netname;
					} else if (!networkName.equalsIgnoreCase(latest_netname)) {
						System.out.println("old=" + networkName + ", new=" + latest_netname);
						throw new Exception("network name changed");
					}	
				}
			}
			input.close();
			
		} catch (Exception e) {
			System.out.println("performWirelessTestOnMac: whoops!:" + e);
		}
	}
	
	/*
	 performPingTestOnMac
	 This is what the command line tool prints out:
	 ---------------
	PING www.coolguybri.com (71.216.1.217): 64 data bytes
	72 bytes from 71.216.1.217: icmp_seq=0 ttl=64 time=8.243 ms
	72 bytes from 71.216.1.217: icmp_seq=1 ttl=64 time=4.152 ms
	72 bytes from 71.216.1.217: icmp_seq=2 ttl=64 time=4.038 ms
	72 bytes from 71.216.1.217: icmp_seq=3 ttl=64 time=3.659 ms
	72 bytes from 71.216.1.217: icmp_seq=4 ttl=64 time=4.033 ms
	
	--- www.coolguybri.com ping statistics ---
	5 packets transmitted, 5 packets received, 0.0% packet loss
	round-trip min/avg/max/stddev = 3.659/4.825/8.243/1.717 ms
    --------------
	 */
	public void performPingTestOnMac() {
		try {
			/* execute the command. */
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("ping -i 1 -c 20 -v -s 64 192.168.1.1");
			int val = pr.waitFor();
			int exitValue = pr.exitValue();
			System.out.println("ping executed, returned with " + exitValue + "(" + val + ")");
			
			/* print it out. */
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			int summaryRow = 24; // the number of pings, + 4
			for (int currRow = 0 ; ((line = input.readLine()) != null) ; currRow++) {
				
				/* skip all the lines except for the last one. */
				//System.out.println(line);
				if (currRow < summaryRow)
					continue;
				
				/* parse it out. */
				String[] firstsplit = line.split("=");
				String[] tokens = firstsplit[1].split("/");
				for (String tok : tokens) {
					//System.out.println("token: \"" + tok + "\"");
				}
				double latest_ping_time = Double.valueOf(tokens[1]);
				double latest_ping_stddev = Double.valueOf(tokens[3].substring(0, tokens[3].length() - 3));
				
				System.out.println("ping = " + latest_ping_time + ", " + latest_ping_stddev);
				list_ping_time.add(latest_ping_time);
				list_ping_stddev.add(latest_ping_stddev);		
			}
			input.close();
			
		} catch (Exception e) {
			System.out.println("performPingTestOnMac: whoops!:" + e);
		}
	}
	
	
	/*
	 performHttpTestOnMac
	  file is 577,603,016 bytes
	curl -O http://www.coolguybri.com/vigil-test/test500.mp4
  	% Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
	100  550M  100  550M    0     0  10.8M      0  0:00:50  0:00:50 --:--:-- 11.0M
	 */
	public void performHttpTestOnMac() {
		try {
			
			// record the current time.
			long startTime = java.lang.System.currentTimeMillis();
			
			/* execute the command. */
			Runtime rt = Runtime.getRuntime();
			//Process pr = rt.exec("curl -O http://www.coolguybri.com/vigil-test/test500.mp4");
			Process pr = rt.exec("curl -O http://192.168.1.1/vigil-test/test500.mp4");
			
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			while (((line = input.readLine()) != null)) {
				
				/* skip all the lines except for the last one. */
				//System.out.println(line);
			}
			input.close();
			
			
			int val = pr.waitFor();
			int exitValue = pr.exitValue();
			System.out.println("http fetch executed, returned with " + exitValue + "(" + val + ")");
			
			// record the finish time.
			long endTime = java.lang.System.currentTimeMillis();
			double duration = ((double)endTime - startTime) / 1000.0;
			double rate = ((577603016.0 / (1024.0 * 1024.0)) * 8.0) / duration;
			list_http_time.add(duration);
			list_http_rate.add(rate);
			System.out.println("http fetch finished in " + duration + " seconds, rate=" + rate + " Mbps");
	
		} catch (Exception e) {
			System.out.println("performHttpTestOnMac: whoops!:" + e);	
		}
	}
	
	
	/*
	 performHttpTestOnMac
	  file is 577,603,016 bytes
	curl -O http://www.coolguybri.com/vigil-test/test500.mp4
 	% Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                Dload  Upload   Total   Spent    Left  Speed
	100  550M  100  550M    0     0  10.8M      0  0:00:50  0:00:50 --:--:-- 11.0M
	 */
	public void performHttpTest2OnMac() {
		try {
			
			// record the current time.
			long startTime = java.lang.System.currentTimeMillis();
			
			/* execute the command. */
			Runtime rt = Runtime.getRuntime();
			//Process pr = rt.exec("curl -O http://www.coolguybri.com/vigil-test/test500.mp4");
			Process pr = rt.exec("curl -O http://192.168.1.1/vigil-test/test50.mp4");
			
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			for (int lineNum = 0; ((line = input.readLine()) != null) ; lineNum++) {
				
				/* skip all the lines except for the last one. */
				if ((lineNum % 100) == 0)
				  System.out.println(line);
			}
			input.close();
			
			
			int val = pr.waitFor();
			int exitValue = pr.exitValue();
			System.out.println("http fetch executed, returned with " + exitValue + "(" + val + ")");
			
			// record the finish time.
			long endTime = java.lang.System.currentTimeMillis();
			double duration = ((double)endTime - startTime) / 1000.0;
			double rate = ((59588866.0 / (1024.0 * 1024.0)) * 8.0) / duration;
			list_http_time.add(duration);
			list_http_rate.add(rate);
			System.out.println("http fetch finished in " + duration + " seconds, rate=" + rate + " Mbps");
	
		} catch (Exception e) {
			System.out.println("performHttpTestOnMac: whoops!:" + e);	
		}
	}
	
	
	/*
	 * Final output reporting
	 */
	
	public void finishTests() {
		System.out.println("results: *********************************");
		System.out.println("network-name: " + networkName);
		computeResults("RSSI", list_rssi);
		computeResults("Noise", list_noise);
		computeResults("TX", list_tx);
		computeResults("Max-TX", list_maxtx);
		//computeResults("PingTime", list_ping_time);
		//computeResults("PingStdDev", list_ping_stddev);
		computeResults("Http-Duration", list_http_time);
		computeResults("Http-Rate", list_http_rate);
		System.out.println("results: *********************************");
	}
	
	public void computeResults(String listName, List<Double> list) {
		
		int samples = list.size();
		double avg = calcAvg(list);
		double stddev = calcStdDev(list, avg);
		
		System.out.println("results: " + listName);
		//System.out.println("results: sample-count: " + samples);
		for (int i = 0 ; i < samples ; i++) {
			System.out.println(list.get(i));
		}
		System.out.println(avg);
		//System.out.println("results: stddev: " + stddev);
	}
	
	
	/*
	 * 	Math routines: average, standard deviation.
	 */
	
	public static final double calcAvg(List<Double> list) {
		double sum = 0;
		for (Double val : list) {
			sum += val;
		}
		
		return (list.size() > 0) ? (sum / (double) list.size()) : 0.0;
	}
	
	
	public static final double calcStdDev(List<Double> list, double avg) {
		double sum = 0;
		for (Double val : list) {
			double delta = avg - val;
			double deltasquare = delta * delta;
			sum += deltasquare;
		}
		
		double variance = (list.size() > 0) ? (sum / (double) list.size()) : 0.0;
		return java.lang.Math.sqrt(variance);
	}
}
