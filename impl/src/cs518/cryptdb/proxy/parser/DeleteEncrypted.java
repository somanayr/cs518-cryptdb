package cs518.cryptdb.proxy.parser;

import cs518.cryptdb.proxy.SchemaManager;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.util.deparser.DeleteDeParser;
import net.sf.jsqlparser.util.deparser.LimitDeparser;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;

public class DeleteEncrypted extends DeleteDeParser {
	
	protected StringBuilder buffer;
    private ExpressionVisitor expressionVisitor;
    private SchemaManager schemaMgr;
	
	public DeleteEncrypted(ExpressionVisitor expressionVisitor, StringBuilder buffer, SchemaManager schemaMgr) {
		super(expressionVisitor, buffer);
		this.buffer = new StringBuilder();
        this.expressionVisitor = expressionVisitor;
        this.schemaMgr = schemaMgr;
	}
	
	public void deParse(Delete delete) {
		if (!(expressionVisitor instanceof EncryptExpression))
			return;
		EncryptExpression encryptExpression = (EncryptExpression) expressionVisitor;
		encryptExpression.setBuffer(buffer);
        buffer.append("DELETE");
        
        // no. you can only delete from one table.
        /*if (delete.getTables() != null && delete.getTables().size() == 1) {
            for (Table table : delete.getTables()) {
            	System.out.println(table.getFullyQualifiedName());
            	encryptExpression.updateEncryption(table, null, null);
                buffer.append(" ").append(schemaMgr.getPhysicalTableName(table.getFullyQualifiedName()));
            }
        }*/
        
        encryptExpression.updateEncryption(delete.getTable(), null, null);
        buffer.append(" FROM ").append(schemaMgr.getPhysicalTableName(delete.getTable().getFullyQualifiedName()));

        if (delete.getJoins() != null) {
            for (Join join : delete.getJoins()) {
                if (join.isSimple()) {
                    buffer.append(", ").append(join);
                } else {
                    buffer.append(" ").append(join);
                }
            }
        }

        if (delete.getWhere() != null) {
            buffer.append(" WHERE ");
            delete.getWhere().accept(expressionVisitor);	// hopefully this works without further mods to EncryptExp
        }

        if (delete.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(delete.getOrderByElements());
        }
        if (delete.getLimit() != null) {
            new LimitDeparser(buffer).deParse(delete.getLimit());
        }

    }
	
}
