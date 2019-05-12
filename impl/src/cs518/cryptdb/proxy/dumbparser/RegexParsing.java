package cs518.cryptdb.proxy.dumbparser;

import java.util.regex.Pattern;

public class RegexParsing {
	
	private static final String[] dataTypes = new String[] {"INT","BOOLEAN","TINYINT","SMALLINT","BIGINT","IDENTITY","DECIMAL","DOUBLE","REAL","TIME","DATE","TIMESTAMP","TIMESTAMP WITH TIME ZONE","BINARY","OTHER","VARCHAR","VARCHAR_IGNORECASE","CHAR","BLOB","CLOB","UUID","ARRAY","ENUM","GEOMETRY","INTERVAL"};
	
	/*
	 * Group 1: Selected columns
	 * Group 3: Table
	 * Group 4: Conditions, if any
	 */
	private static Pattern selectRegex = Pattern.compile("^SELECT((\\s+[a-zA-Z][a-zA-Z0-9_]{0,30}+,?)+)\\s+FROM\\s+([a-zA-Z][a-zA-Z0-9_]{0,30}+)\\s*(.*)$");
	/*
	 * Group 1: Table
	 * Group 2: Conditions, if any
	 */
	private static Pattern deleteRegex = Pattern.compile("^DELETE FROM\\s+([a-zA-Z][a-zA-Z0-9_]{0,30}+)\\s*(.*)$");
	/*
	 * Group 1: Table name
	 * Group 2: Columns
	 */
	private static Pattern createTableRegex = Pattern.compile("^CREATE TABLE\\s+([a-zA-Z][a-zA-Z0-9_]{0,30}+)\\s+(\\(([a-zA-Z][a-zA-Z0-9_]{0,30}+\\s+(" + String.join("|", dataTypes) +")\\s*,?\\s*)\\))");
	/*
	 * Group 1: Table name
	 * Group 2: Selected columns, if any
	 * Group 3: Values
	 */
	private static Pattern insertRegex = Pattern.compile("^INSERT INTO\\s([a-zA-Z][a-zA-Z0-9_]{0,30}+)(\\s+\\(([a-zA-Z][a-zA-Z0-9_]{0,30}+(\\s*,\\s*)?)+\\))?\\s+VALUES\\s+(.*)$");
	/*
	 * Group 1: Selected columns
	 */
	private static Pattern groupByRegex = Pattern.compile("GROUP BY ((\\s+[a-zA-Z][a-zA-Z0-9_]{0,30}+,?)+)");
	/*
	 * Group 1: Selected columns
	 */
	private static Pattern orderByRegex = Pattern.compile("ORDER BY ((\\s+[a-zA-Z][a-zA-Z0-9_]{0,30}+,?)+)");
	/*
	 * Group 1: Conditions presented
	 */
	private static Pattern whereRegex = Pattern.compile("WHERE((\\s+(NOT\\s+)?[a-zA-Z][a-zA-Z0-9_]{0,30}+\\s(LIKE|IN|IS|NOT IN|=|<>|!=|<|>|>=|<=).*?(AND|OR)?)+)");
	
}
