package cs518.cryptdb.proxy.parser;

import java.util.Map;

import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.proxy.SchemaManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class Parser {
	private static SchemaManager schemaMgr;
	
	public Parser(SchemaManager cryptoMgr) {
		schemaMgr = cryptoMgr;
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
		ExpressionDeParser expr = new EncryptExpression(schemaMgr);
		
		SelectEncrypted selectDeparser = new SelectEncrypted(expr, buffer, schemaMgr);
        expr.setSelectVisitor(selectDeparser);
        expr.setBuffer(buffer);
        EncryptedStatementDeParser stmtDeparser = new EncryptedStatementDeParser(expr, selectDeparser, buffer, schemaMgr);

		Statement stmt = CCJSqlParserUtil.parse(originalQuery);
		
		stmt.accept(stmtDeparser);
		String output = stmtDeparser.getBuffer().toString();
        return new QueryPacket(output);
	}

}
