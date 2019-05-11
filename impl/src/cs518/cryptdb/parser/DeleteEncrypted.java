package cs518.cryptdb.parser;

import cs518.cryptdb.proxy.SchemaManager;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.util.deparser.DeleteDeParser;
import net.sf.jsqlparser.util.deparser.LimitDeparser;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;

public class DeleteEncrypted extends DeleteDeParser {
	
	protected StringBuilder buffer = new StringBuilder();
    private ExpressionVisitor expressionVisitor;
    private SchemaManager schemaMgr;
	
	public DeleteEncrypted(ExpressionVisitor expressionVisitor, StringBuilder buffer, SchemaManager schemaMgr) {
		super(expressionVisitor, buffer);
		this.buffer = buffer;
        this.expressionVisitor = expressionVisitor;
        this.schemaMgr = schemaMgr;
	}
	
	public void deParse(Delete delete) {
        buffer.append("DELETE");
        if (delete.getTables() != null && delete.getTables().size() > 0) {
            for (Table table : delete.getTables()) {
                buffer.append(" ").append(schemaMgr.getPhysicalTableName(table.getFullyQualifiedName()));
            }
        }
        buffer.append(" FROM ").append(delete.getTable().toString());

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
