package cs518.cryptdb.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.common.pair.Pair;
import cs518.cryptdb.proxy.SchemaManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
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
	
	public class encryptValues extends ExpressionDeParser {
		protected StringBuilder buffer = new StringBuilder();
	    private SelectVisitor selectVisitor;
		private String tableId;
		private String columnId;
		private String rowId;
		private CryptoScheme cryptoScheme;
		private SchemaManager schemaMgr;
		
		public encryptValues(SelectVisitor selectVisitor, StringBuilder buffer,
				String tableId, String rowId) {
			this.buffer = buffer;
			this.selectVisitor = selectVisitor;
			this.tableId = tableId;
			this.rowId = rowId;
			this.cryptoScheme = CryptoScheme.DET;
		}
		
		@Override
		public void visit(StringValue stringValue) {
			StringBuffer temp = new StringBuffer();
			if (stringValue.getPrefix() != null) {
	            temp.append(stringValue.getPrefix());
	        }
			String op = temp.append(stringValue.getValue()).toString();
			Pair<String, byte[]> encrypted = schemaMgr.encrypt(tableId, columnId, rowId, op.getBytes(), cryptoScheme);
	        buffer.append("'").append(new String(encrypted.getSecond())).append("'");
		}
		
	}
	
	// TODO: create custom SelectDeParser (implements SelectVisitor) to pass to InsertDeParser
	public class selectOnEncrypted extends SelectDeParser {
		
		protected StringBuilder buffer = new StringBuilder();
	    private ExpressionVisitor expressionVisitor = new ExpressionVisitorAdapter();
	    
	    public selectOnEncrypted(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
	        this.buffer = buffer;
	        this.expressionVisitor = expressionVisitor;
	    }
	    
	    
	}
	
	public class insertEncrypted extends InsertDeParser {
		
		protected StringBuilder buffer;
	    private ExpressionVisitor expressionVisitor;
	    private SelectVisitor selectVisitor;
	    private String tableId;
		
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
	        
	        tableId = insert.getTable().getName();
	        buffer.append(schemaMgr.getPhysicalTableName(tableId));
	        
	        if (insert.getColumns() != null) {
	            buffer.append(" (ROWID ");
	            for (Iterator<Column> iter = insert.getColumns().iterator(); iter.hasNext();) {
	                Column column = iter.next();
	                buffer.append(schemaMgr.getPhysicalColumnName(tableId, column.getColumnName()));
	                if (iter.hasNext()) {
	                    buffer.append(", ");
	                }
	            }
	            buffer.append(")");
	        }
	        
	        // TODO: modify to extract and encrypt values. should do this already, given the right inputs
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
		
		@Override
	    public void visit(ExpressionList expressionList) {
	        buffer.append(" VALUES (");
	        buffer.append(schemaMgr.getNewRowId(tableId));
	        buffer.append(", ");
	        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
	            Expression expression = iter.next();
	            expression.accept(expressionVisitor);
	            if (iter.hasNext()) {
	                buffer.append(", ");
	            }
	        }
	        buffer.append(")");
	    }
		
		@Override
	    public void visit(MultiExpressionList multiExprList) {
	        buffer.append(" VALUES ");
	        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext();) {
	            buffer.append("(" + schemaMgr.getNewRowId(tableId) + ", ");
	            for (Iterator<Expression> iter = it.next().getExpressions().iterator(); iter.hasNext();) {
	                Expression expression = iter.next();
	                expression.accept(expressionVisitor);
	                if (iter.hasNext()) {
	                    buffer.append(", ");
	                }
	            }
	            buffer.append(")");
	            if (it.hasNext()) {
	                buffer.append(", ");
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
