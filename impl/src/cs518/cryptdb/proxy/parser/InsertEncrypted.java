package cs518.cryptdb.proxy.parser;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import cs518.cryptdb.common.Util;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.common.pair.Pair;
import cs518.cryptdb.proxy.SchemaManager;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.deparser.InsertDeParser;

public class InsertEncrypted extends InsertDeParser {
	
	protected StringBuilder buffer;
    private ExpressionVisitor expressionVisitor;
    private SelectVisitor selectVisitor;
    private String tableId;
	private SchemaManager schemaMgr;
	
	public InsertEncrypted(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor,
							StringBuilder buffer, SchemaManager schemaMgr) {
		super(expressionVisitor, selectVisitor, buffer);
		this.buffer = buffer;
        this.expressionVisitor = expressionVisitor;
        this.selectVisitor = selectVisitor;
        this.schemaMgr = schemaMgr;
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
        
        tableId = insert.getTable().getFullyQualifiedName();
        buffer.append(schemaMgr.getPhysicalTableName(tableId));
        
        if (insert.getColumns() != null) {
            buffer.append(" (ROWID, ");
            for (Iterator<Column> iter = insert.getColumns().iterator(); iter.hasNext();) {
                Column column = iter.next();
                List<String> subcols = schemaMgr.getAllSubcolumns(tableId, column.getColumnName());
                String lastSubCol = subcols.get(subcols.size() - 1);
                for(String subcol : subcols) {
                	buffer.append(schemaMgr.getPhysicalColumnName(tableId, subcol));
                    if(subcol != lastSubCol)
                    	buffer.append(", ");
                }
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
        String rowId = schemaMgr.getNewRowId(tableId);
        buffer.append(rowId);
    	List<String> virtualColumns = schemaMgr.getVirtualColumns(tableId);
    	Iterator<String> virtColsIter = virtualColumns.iterator();
    	virtColsIter.next(); //Skip ROWID
        buffer.append(", ");
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
            Expression expression = iter.next();
            String virtColName = virtColsIter.next();
            List<byte[]> encryptedList = null;
            if (expression instanceof StringValue) {
            	StringValue sv = (StringValue) expression;
            	StringBuffer temp = new StringBuffer();
        		if (sv.getPrefix() != null) {
        	        temp.append(sv.getPrefix());
        	    }
        		String op = temp.append(sv.getValue()).toString();
            	encryptedList = schemaMgr.encryptAllSubcols(tableId, virtColName, rowId, op.getBytes());
            } else if (expression instanceof LongValue) {
            	LongValue lv = (LongValue) expression;
        		byte[] plaintext = Util.intToBytes(Integer.parseInt(lv.toString()) - Integer.MIN_VALUE);
            	encryptedList = schemaMgr.encryptAllSubcols(tableId, virtColName, rowId, plaintext);
            }
            int off = 0;
            for(byte[] encrypted : encryptedList) {
	        	String hex = Util.bytesToHex(encrypted);
	        	System.out.println("Inserting 0x" + hex); //FIXME we need to find a way to insert binary data -- hex values only work up to 0x7AAAAAAA
            	System.out.println("Params: " + tableId + ", " + virtColName + "_" + off + ", " + rowId);
            	System.out.println("Decrypted: " + new String(schemaMgr.decrypt(tableId, virtColName + "_" + off, rowId, encrypted)));
            	//buffer.append(new BigInteger(encrypted));
	        	//buffer.append("0x").append(hex);
	        	byte[] base64 = Base64.getEncoder().encode(encrypted);
	        	buffer.append("'").append(new String(base64)).append("'");
	        	if(off != encryptedList.size() - 1)
	                buffer.append(", ");
	        	off++;
	        }
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
