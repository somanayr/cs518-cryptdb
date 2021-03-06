package cs518.cryptdb.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import cs518.cryptdb.common.Profiling;

public class SQLSequenceReader {
	
	private static String lineComment = "^(.*?)--.*$";
	private static Pattern linePattern = Pattern.compile(lineComment);
	
	public static void runApplication(String sqlFile, ApplicationMain am) throws IOException {
		List<String> l = readSQLFile(sqlFile);
		
		Profiling.startTimer("Run time");
		int ct = 0;
		for(String s : l) {
			String timerName = "Latency: " + s.substring(0, s.indexOf(' '));
			Profiling.startTimer(timerName);
			if(!s.startsWith("INSERT"))
				System.out.println("Next statement: " + s);
			ct += 1;
			if(ct * 100 / l.size() != (ct - 1) * 100 / l.size()) {
				System.out.printf("%d%% of statements submitted\n", ct * 100 / l.size());
			}
			am.sendStatement(s);
			Profiling.stopTimer(timerName);
		}
		Profiling.pauseTimer("Run time");
	}
	
	private static List<String> readSQLFile(String sqlFile) throws IOException {
		String dir = new File(sqlFile).getParent();
		String child = new File(sqlFile).getName();
		return readSQLFile(child, dir);
	}
	
	private static List<String> readSQLFile(String fn, String dir) throws IOException {
		ArrayList<String> al = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(dir, fn)))) {
			StringBuffer seq = new StringBuffer();
			String line;
			int addedLines = 0;
			while((line=reader.readLine()) != null) {
				line = line.split("(--|#)")[0];
				line = line.trim();
				String[] commands = line.split(";",-50);
				for (int i = 0; i < commands.length; i++) {
					seq.append(commands[i].trim());
					if(i != commands.length - 1) {
						String statement = seq.toString();
						String command = statement.split("\\s+")[0];
						if(command.equals("source")) {
							//FIXME uncomment
							al.addAll(readSQLFile(statement.split("\\s+")[1], dir));
						} else if(command.equals("flush")){
							//nop
						} else {
							al.add(statement); 
							addedLines ++;

							if(addedLines >= 10000) { //FIXME stop max size
								return al;
							}
						}
						seq = new StringBuffer();
					} else {
						if(seq.length() != 0)
							seq.append(' ');
					}
				}
			}
			if(seq.length() != 0) {
				al.add(seq.toString());
			}
		}
		return al;
	}
	
	public static void main(String[] args) throws IOException {
		//System.out.println(Arrays.toString("abc;".split(";")));
		
		for(String l : readSQLFile("../test_db/employees.sql")) {
			if(l.length() > 100) {
				l = l.substring(0, 97) + "...";
				
			}
			System.out.println(l);
		}
	}

}
