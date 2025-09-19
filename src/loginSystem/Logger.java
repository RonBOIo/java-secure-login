package loginSystem;
/* used ro record and display events
 * 
 */
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class Logger {
	
	private static final String LOG_FILE = "eventLog.txt";// constant used for eventlog 
	
	//used to record events
	public static void logEvent(String eventDiscription) {
		
		
		try(FileWriter writer = new FileWriter(LOG_FILE, true)){
			
			//import datetime, then format for file write
			String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"));
			writer.write(dateTime+"," + eventDiscription + "\n");
			
			//catches IO error
		}catch (IOException e) {
			System.out.println("Error, Please contact Admin!");
		}
	}
	
	//Used to print logs in eventLog.txt
	public static List<String> getLogs() { 
		List<String> logs = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))){
			String log;
			
			while((log = reader.readLine()) !=null) {
				logs.add(log);
			}
		} catch (IOException e) {
			System.out.println("Error, Please contact an Admin!");
		}
		return logs;
		
		
	}
}
