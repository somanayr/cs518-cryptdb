package cs518.cryptdb.proxy.parser;

import java.util.Iterator;

import cs518.cryptdb.proxy.SchemaManager;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.util.deparser.GroupByDeParser;
import net.sf.jsqlparser.util.deparser.LimitDeparser;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class SelectEncrypted extends SelectDeParser {
	
	protected StringBuilder buffer;
    private ExpressionVisitor expressionVisitor;
    private SchemaManager schemaMgr;
    
    public SelectEncrypted(ExpressionVisitor expressionVisitor, StringBuilder buffer, SchemaManager schemaMgr) {
    	super(expressionVisitor, buffer);
    	this.buffer = buffer;
    	this.expressionVisitor = expressionVisitor;
    	this.schemaMgr = schemaMgr;
    }
    
    @Override
    public void visit(PlainSelect plainSelect) {
    	FromItem fromItem = plainSelect.getFromItem();
    	System.out.println(fromItem.getClass());
    	if (!(fromItem instanceof Table)) {
    		return;	// we only accept one table
    	}
    	
        if (plainSelect.isUseBrackets()) {
            buffer.append("(");
        }
        buffer.append("SELECT ");

        OracleHint hint = plainSelect.getOracleHint();
        if (hint != null) {
            buffer.append(hint).append(" ");
        }

        Skip skip = plainSelect.getSkip();
        if (skip != null) {
            buffer.append(skip).append(" ");
        }

        First first = plainSelect.getFirst();
        if (first != null) {
            buffer.append(first).append(" ");
        }

        if (plainSelect.getDistinct() != null) {
            if (plainSelect.getDistinct().isUseUnique()) {
                buffer.append("UNIQUE ");
            } else {
                buffer.append("DISTINCT ");
            }
            if (plainSelect.getDistinct().getOnSelectItems() != null) {
                buffer.append("ON (");
                for (Iterator<SelectItem> iter = plainSelect.getDistinct().getOnSelectItems().
                        iterator(); iter.hasNext();) {
                    SelectItem selectItem = iter.next();
                    // this redirects to the standard SelectDeParser, which will use the given ExpressionVisitor
                    selectItem.accept(this);
                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                }
                buffer.append(") ");
            }

        }

        Top top = plainSelect.getTop();
        if (top != null) {
            buffer.append(top).append(" ");
        }

        if (plainSelect.getMySqlSqlNoCache()) {
            buffer.append("SQL_NO_CACHE").append(" ");
        }

        if (plainSelect.getMySqlSqlCalcFoundRows()) {
            buffer.append("SQL_CALC_FOUND_ROWS").append(" ");
        }

        for (Iterator<SelectItem> iter = plainSelect.getSelectItems().iterator(); iter.hasNext();) {
            SelectItem selectItem = iter.next();
            selectItem.accept(this);	// these better be Columns, in which case we're fine
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }

        if (plainSelect.getIntoTables() != null) {
            buffer.append(" INTO ");
            for (Iterator<Table> iter = plainSelect.getIntoTables().iterator(); iter.hasNext();) {
                visit(iter.next());
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }

        if (plainSelect.getFromItem() != null) {
            buffer.append(" FROM ");
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                deparseJoin(join);
            }
        }

        if (plainSelect.getWhere() != null) {
            buffer.append(" WHERE ");
            plainSelect.getWhere().accept(expressionVisitor);
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(expressionVisitor);
        }

        if (plainSelect.getGroupBy() != null) {
            buffer.append(" ");
            new GroupByDeParser(expressionVisitor, buffer).deParse(plainSelect.getGroupBy());
        }

        if (plainSelect.getHaving() != null) {
            buffer.append(" HAVING ");
            plainSelect.getHaving().accept(expressionVisitor);
        }

        if (plainSelect.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).
                    deParse(plainSelect.isOracleSiblings(), plainSelect.getOrderByElements());
        }

        if (plainSelect.getLimit() != null) {
            new LimitDeparser(buffer).deParse(plainSelect.getLimit());
        }
        if (plainSelect.getOffset() != null) {
            deparseOffset(plainSelect.getOffset());
        }
        if (plainSelect.getFetch() != null) {
            deparseFetch(plainSelect.getFetch());
        }
        if (plainSelect.isForUpdate()) {
            buffer.append(" FOR UPDATE");
            if (plainSelect.getForUpdateTable() != null) {
                buffer.append(" OF ").append(plainSelect.getForUpdateTable());
            }
            if (plainSelect.getWait() != null) {
                // wait's toString will do the formatting for us
                buffer.append(plainSelect.getWait());
            }
        }
        if (plainSelect.getForXmlPath() != null) {
            buffer.append(" FOR XML PATH(").append(plainSelect.getForXmlPath()).append(")");
        }
        if (plainSelect.isUseBrackets()) {
            buffer.append(")");
        }
    }
    
    @Override
    public void visit(Table tableName) {
        buffer.append(schemaMgr.getPhysicalTableName(tableName.getFullyQualifiedName()));
        // no alias, no pivot, no hints
    }
    
    @Override
    public void visit(AllTableColumns allTableColumns) {
    	String encryptedTable = schemaMgr.getPhysicalTableName(allTableColumns.getTable().getFullyQualifiedName());
        buffer.append(encryptedTable).append(".*");
    }
    
}
