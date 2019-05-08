package cs518.cryptdb.proxy;

import java.util.Map;

import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.common.crypto.CryptoScheme;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class Parser {
	
	static class substituteEncryptedCols extends ExpressionDeParser {
		private Map<String, Map<String, CryptoScheme>> schemeMap; // TODO: tells DeParser what to substitute
		
		@Override
		public void visit(StringValue col) {
			this.getBuffer().append("encrypted column here");
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
	
	public static QueryPacket parseQuery(QueryPacket qp) throws JSQLParserException {
		String originalQuery = qp.getQuery();
		
		StringBuilder buffer = new StringBuilder();
		ExpressionDeParser expr = new substituteEncryptedCols();
		
		SelectDeParser selectDeparser = new SelectDeParser(expr, buffer);
        expr.setSelectVisitor(selectDeparser);
        expr.setBuffer(buffer);
        StatementDeParser stmtDeparser = new StatementDeParser(expr, selectDeparser, buffer);

		Statement stmt = CCJSqlParserUtil.parse(originalQuery);
		
		stmt.accept(stmtDeparser);
		String output = stmtDeparser.getBuffer().toString();
        return (QueryPacket) Packet.instantiate(Packet.QUERY_PACKET_ID, output.getBytes());
	}

}
