package cs518.cryptdb.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.proxy.CryptoManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class Parser {
	private static CryptoManager cmgr;
	
	public Parser(CryptoManager cryptoMgr) {
		cmgr = cryptoMgr;
	}
	
	static class substituteEncryptedCols extends ExpressionDeParser {
		
		@Override
		public void visit(Column col) {
			Table table = col.getTable();
			String encryptedCol = cmgr.getPhysicalColumnName(table.getName(), col.getColumnName());
			this.getBuffer().append(encryptedCol);
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
	
	public QueryPacket parseQuery(QueryPacket qp) throws JSQLParserException {
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
			encryptedTableNames.add(cmgr.getPhysicalTableName(tblName));
		}
		
		stmt.accept(stmtDeparser);
		String output = stmtDeparser.getBuffer().toString();
        return (QueryPacket) Packet.instantiate(Packet.QUERY_PACKET_ID, output.getBytes());
	}

}
