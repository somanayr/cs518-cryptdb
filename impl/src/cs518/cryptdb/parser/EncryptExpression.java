package cs518.cryptdb.parser;

import cs518.cryptdb.common.Util;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.common.pair.Pair;
import cs518.cryptdb.proxy.SchemaManager;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class EncryptExpression extends ExpressionDeParser {
	protected StringBuilder buffer = new StringBuilder();
	private SelectVisitor selectVisitor;
	private CryptoScheme cryptoScheme;
	private SchemaManager schemaMgr;
	private Table table;
	private String tableId;
	private String columnId;
	private String rowId;
	
	public EncryptExpression(SchemaManager schemaMgr) {
		this.schemaMgr = schemaMgr;
	}
		
	public EncryptExpression(SelectVisitor selectVisitor, StringBuilder buffer) {
		this.buffer = buffer;
		this.selectVisitor = selectVisitor;
		this.cryptoScheme = CryptoScheme.DET;
	}
	
	public void updateEncryption(Table table, String columnId, String rowId) {
		this.table = table;
		this.columnId = columnId;
		this.rowId = rowId;
	}
	
	@Override
	public void visit(Column col) {
		if (table.getFullyQualifiedName() == null)
			throw new RuntimeException();
		String encryptedCol = schemaMgr.getPhysicalColumnName(table.getFullyQualifiedName(), col.getColumnName());
		this.getBuffer().append(encryptedCol);
	}
		
	@Override
	public void visit(StringValue stringValue) {
		StringBuffer temp = new StringBuffer();
		if (stringValue.getPrefix() != null) {
	        temp.append(stringValue.getPrefix());
	    }
		String op = temp.append(stringValue.getValue()).toString();
		// TODO: how to get table, column, row info from just a stringValue?
		Pair<String, byte[]> encrypted = schemaMgr.encrypt(this.tableId, this.columnId,
				this.rowId, op.getBytes(), cryptoScheme);
	    buffer.append("'").append(new String(encrypted.getSecond())).append("'");
	}
	
	public void setSchemaManager(SchemaManager schemaMgr) {
		this.schemaMgr = schemaMgr;
	}
		
}	
