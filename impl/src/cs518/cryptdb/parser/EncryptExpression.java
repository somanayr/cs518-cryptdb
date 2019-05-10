package cs518.cryptdb.parser;

import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.common.pair.Pair;
import cs518.cryptdb.proxy.SchemaManager;

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class EncryptExpression extends ExpressionDeParser {
	protected StringBuilder buffer = new StringBuilder();
	private SelectVisitor selectVisitor;
	private String tableId;
	private String columnId;
	private String rowId;
	private CryptoScheme cryptoScheme;
	private SchemaManager schemaMgr;
		
	public EncryptExpression(SelectVisitor selectVisitor, StringBuilder buffer,
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
