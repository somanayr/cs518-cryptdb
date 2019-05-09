package cs518.cryptdb.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.proxy.SchemaManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.deparser.CreateTableDeParser;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.InsertDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class Parser {
	private static SchemaManager schemaMgr;
	
	public Parser(SchemaManager cryptoMgr) {
		schemaMgr = cryptoMgr;
	}
	
	static class substituteEncryptedCols extends ExpressionDeParser {
		
		@Override
		public void visit(Column col) {
			Table table = col.getTable();
			String encryptedCol = schemaMgr.getPhysicalColumnName(table.getName(), col.getColumnName());
			this.getBuffer().append(encryptedCol);
		}
	}
	
	public class insertEncrypted extends InsertDeParser {
		
		protected StringBuilder buffer;
	    private ExpressionVisitor expressionVisitor;
	    private SelectVisitor selectVisitor;
		
		public insertEncrypted(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor, StringBuilder buffer) {
			super(expressionVisitor, selectVisitor, buffer);
			this.buffer = buffer;
	        this.expressionVisitor = expressionVisitor;
	        this.selectVisitor = selectVisitor;
		}
		
		@Override
		public void deParse(Insert insert) {
	        buffer.append("INSERT ");
	        if (insert.getModifierPriority() != null) {
	            buffer.append(insert.getModifierPriority()).append(" ");
	        }
	        if (insert.isModifierIgnore()) {
	            buffer.append("IGNORE ");
	        }
	        buffer.append("INTO ");
	        
	        String tableId = insert.getTable().getName();
	        buffer.append(schemaMgr.getPhysicalTableName(tableId));
	        
	        if (insert.getColumns() != null) {
	            buffer.append(" (");
	            for (Iterator<Column> iter = insert.getColumns().iterator(); iter.hasNext();) {
	                Column column = iter.next();
	                buffer.append(schemaMgr.getPhysicalColumnName(tableId, column.getColumnName()));
	                if (iter.hasNext()) {
	                    buffer.append(", ");
	                }
	            }
	            buffer.append(")");
	        }
	        
	        // TODO: modify to extract and encrypt values
	        if (insert.getItemsList() != null) {
	            insert.getItemsList().accept(this);
	        }

	        if (insert.getSelect() != null) {
	            buffer.append(" ");
	            if (insert.isUseSelectBrackets()) {
	                buffer.append("(");
	            }
	            if (insert.getSelect().getWithItemsList() != null) {
	                buffer.append("WITH ");
	                for (WithItem with : insert.getSelect().getWithItemsList()) {
	                    with.accept(selectVisitor);
	                }
	                buffer.append(" ");
	            }
	            insert.getSelect().getSelectBody().accept(selectVisitor);
	            if (insert.isUseSelectBrackets()) {
	                buffer.append(")");
	            }
	        }

	        if (insert.isUseSet()) {
	            buffer.append(" SET ");
	            for (int i = 0; i < insert.getSetColumns().size(); i++) {
	                Column column = insert.getSetColumns().get(i);
	                column.accept(expressionVisitor);

	                buffer.append(" = ");

	                Expression expression = insert.getSetExpressionList().get(i);
	                expression.accept(expressionVisitor);
	                if (i < insert.getSetColumns().size() - 1) {
	                    buffer.append(", ");
	                }
	            }
	        }
		}
		
	}
	
	public class createEncryptedTable extends CreateTableDeParser {
		
		private StatementDeParser statementDeParser;
		
		public createEncryptedTable(StatementDeParser statementDeParser, StringBuilder buffer) {
			super(statementDeParser, buffer);
			this.statementDeParser = statementDeParser;
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
	        
	        if (colDefs != null) {
	        	for (ColumnDefinition colDef : colDefs) {
	        		colIds.add(colDef.getColumnName());
	        	}
		        schemaMgr.addTable(tblName, (String[]) colIds.toArray());
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
	                buffer.append(" (");
	                for (Iterator<ColumnDefinition> iter = createTable.getColumnDefinitions().iterator(); iter.
	                        hasNext();) {
	                    ColumnDefinition columnDefinition = iter.next();
	                    buffer.append(schemaMgr.getPhysicalColumnName(tblName, columnDefinition.getColumnName()));
	                    buffer.append(" ");
	                    buffer.append("VARBINARY(1000000)"); // because we're using H2 and storing everything as byte[]
	                    if (columnDefinition.getColumnSpecStrings() != null) {
	                        for (String s : columnDefinition.getColumnSpecStrings()) {
	                            buffer.append(" ");
	                            buffer.append(s);
	                        }
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
	
	/**
	 * Determines the encryption scheme needs to fulfill the statement for each column
	 * @param statement
	 * @return A map of table name -> column name -> scheme
	 */
	public static Map<String, Map<String, CryptoScheme>> getNeedSchemes(String statement) {
		throw new NotImplementedException();
	}
	
	public QueryPacket parseQuery(QueryPacket qp) throws JSQLParserException { // TODO: rewrite with new DeParsers
		String originalQuery = qp.getQuery();
		
		StringBuilder buffer = new StringBuilder();
		ExpressionDeParser expr = new substituteEncryptedCols();
		TablesNamesFinder tnf = new TablesNamesFinder();
		
		SelectDeParser selectDeparser = new SelectDeParser(expr, buffer);
        expr.setSelectVisitor(selectDeparser);
        expr.setBuffer(buffer);
        StatementDeParser stmtDeparser = new StatementDeParser(expr, selectDeparser, buffer);

		Statement stmt = CCJSqlParserUtil.parse(originalQuery);
		List<String> tableList = tnf.getTableList(stmt);
		
		List<String> encryptedTableNames = new ArrayList<String>();
		for (String tblName : tableList) {
			encryptedTableNames.add(schemaMgr.getPhysicalTableName(tblName));
		}
		
		stmt.accept(stmtDeparser);
		String output = stmtDeparser.getBuffer().toString();
        return (QueryPacket) Packet.instantiate(Packet.QUERY_PACKET_ID, output.getBytes());
	}

}
