package cs518.cryptdb.proxy.parser;

import java.util.Base64;

import cs518.cryptdb.common.Util;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.common.pair.Pair;
import cs518.cryptdb.proxy.SchemaManager;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class EncryptExpression extends ExpressionDeParser {
	protected StringBuilder buffer;
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
		super(selectVisitor, buffer);
		this.buffer = buffer;
		this.selectVisitor = selectVisitor;
		this.cryptoScheme = CryptoScheme.DET;
	}
	
	public void updateEncryption(Table table, String columnId, String rowId) {
		this.table = table;
		this.columnId = columnId;
		this.rowId = rowId;
	}
	
	public void setBuffer(StringBuilder buffer) {
		this.buffer = buffer;
	}
	
	public void setSchemaManager(SchemaManager schemaMgr) {
		this.schemaMgr = schemaMgr;
	}
	
	@Override
	public void visit(Column col) {
		this.columnId = col.getFullyQualifiedName();
		this.tableId = table.getFullyQualifiedName();
		String subCol = schemaMgr.getSubcolumnForScheme(table.getFullyQualifiedName(),
														col.getFullyQualifiedName(), CryptoScheme.DET);
		String encryptedCol = schemaMgr.getPhysicalColumnName(table.getFullyQualifiedName(), subCol);
		System.out.println("encryptedCol: " + encryptedCol);
		buffer.append(encryptedCol);
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
				null, op.getBytes(), CryptoScheme.DET);
		byte[] base64 = Base64.getEncoder().encode(encrypted.getSecond());
		String ciphertext = new String(base64);
	    buffer.append("'").append(ciphertext).append("'");
	}
	
	
	@Override
	public void visitOldOracleJoinBinaryExpression(OldOracleJoinBinaryExpression expression, String operator) {
//      if (expression.isNot()) {
//          buffer.append(NOT);
//      }
      expression.getLeftExpression().accept(this);
      if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_RIGHT) {
          buffer.append("(+)");
      }
      buffer.append(operator);
      expression.getRightExpression().accept(this);
      if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_LEFT) {
          buffer.append("(+)");
      }
  }
		
}	
