package cs518.cryptdb.proxy.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cs518.cryptdb.proxy.SchemaManager;

import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.CreateTableDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class CreateEncryptedTable extends CreateTableDeParser {
	
	private StatementDeParser statementDeParser;
	private StringBuilder buffer;
	private SchemaManager schemaMgr;

	public CreateEncryptedTable(StatementDeParser statementDeParser, StringBuilder buffer, SchemaManager schemaMgr) {
		super(statementDeParser, buffer);
		this.statementDeParser = statementDeParser;
		this.buffer = buffer;
		this.schemaMgr = schemaMgr;
	}
	
	@Override
	public void deParse(CreateTable createTable) {
		buffer.append("CREATE ");
		if (createTable.isUnlogged()) {
            buffer.append("UNLOGGED ");
        }
		String params = PlainSelect.
                getStringList(createTable.getCreateOptionsStrings(), false, false);
        if (!"".equals(params)) {
            buffer.append(params).append(' ');
        }

        buffer.append("TABLE ");
        if (createTable.isIfNotExists()) {
            buffer.append("IF NOT EXISTS ");
        }
        
        // encrypt table and column names to complete query parsing
        String tblName = createTable.getTable().getFullyQualifiedName();
        List<ColumnDefinition> colDefs = createTable.getColumnDefinitions();
        List<String> colIds = new ArrayList<String>();
        List<List<String>> physicalTable;
        
        if (colDefs != null) {
        	colIds.add("ROWID");
        	for (ColumnDefinition colDef : colDefs) {
        		colIds.add(colDef.getColumnName());
        	}
        	String[] columnNames = new String[colIds.size()];
	        physicalTable = schemaMgr.addTable(tblName, colIds.toArray(columnNames)); // TODO: rewrite pending updates to SchemaManager 
        } else {
        	// no columns to enter -- just reject the query
        	buffer.delete(0, buffer.length());
        	return;
        }
        
        buffer.append(schemaMgr.getPhysicalTableName(tblName));
        
        if (createTable.getSelect() != null) {
            buffer.append(" AS ");
            if (createTable.isSelectParenthesis()) {
                buffer.append("(");
            }
            Select sel = createTable.getSelect();
            sel.accept(this.statementDeParser); // TODO: make sure you give this class a custom statementDeParser
            if (createTable.isSelectParenthesis()) {
                buffer.append(")");
            }
        } else {
            if (createTable.getColumnDefinitions() != null) {
                buffer.append(" ( ROWID INT, ");
                for (Iterator<ColumnDefinition> iter = createTable.getColumnDefinitions().iterator(); iter.
                        hasNext();) {
                    ColumnDefinition columnDefinition = iter.next();
                    List<String> subcols = schemaMgr.getAllSubcolumns(tblName, columnDefinition.getColumnName());
                    String lastSubCol = subcols.get(subcols.size() - 1);
                    for(String subcol : subcols) {
	                    buffer.append(schemaMgr.getPhysicalColumnName(tblName, subcol));
	                    buffer.append(" ");
	                    buffer.append("VARBINARY(1000000)"); // because we're using H2 and storing everything as byte[]
	                    if (columnDefinition.getColumnSpecStrings() != null) {
	                        for (String s : columnDefinition.getColumnSpecStrings()) {
	                            buffer.append(" ");
	                            buffer.append(s);
	                        }
	                    }
	                    if(subcol != lastSubCol)
	                    	buffer.append(", ");
                    }

                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                }

                if (createTable.getIndexes() != null) {
                    for (Iterator<Index> iter = createTable.getIndexes().iterator(); iter.hasNext();) {
                        buffer.append(", ");
                        Index index = iter.next();
                        buffer.append(index.toString());
                    }
                }

                buffer.append(")");
            }
        }
	}
	
}
