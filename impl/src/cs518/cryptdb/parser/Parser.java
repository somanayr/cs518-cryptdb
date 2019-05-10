package cs518.cryptdb.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.proxy.SchemaManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
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
	
	// TODO: create custom SelectDeParser (implements SelectVisitor) to pass to InsertDeParser
	public class selectOnEncrypted extends SelectDeParser {
		
		protected StringBuilder buffer = new StringBuilder();
	    private ExpressionVisitor expressionVisitor = new ExpressionVisitorAdapter();
	    
	    public selectOnEncrypted(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
	        this.buffer = buffer;
	        this.expressionVisitor = expressionVisitor;
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
