package cs518.cryptdb.parser;

import cs518.cryptdb.proxy.SchemaManager;

import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class EncryptedStatementDeParser extends StatementDeParser {
	
	private ExpressionDeParser expressionDeParser;
    private SelectDeParser selectDeParser;
    protected StringBuilder buffer;
	private SchemaManager schemaMgr;

	public EncryptedStatementDeParser(ExpressionDeParser expressionDeParser, SelectDeParser selectDeParser,
			StringBuilder buffer, SchemaManager schemaMgr) {
		super(expressionDeParser, selectDeParser, buffer);
		this.expressionDeParser = expressionDeParser;
	    this.selectDeParser = selectDeParser;
	    this.buffer = buffer;
		this.schemaMgr = schemaMgr;
	}
	
	@Override
	public void visit(CreateTable createTable) {
		CreateEncryptedTable createTableDeParser = new CreateEncryptedTable(this, buffer, schemaMgr);
        createTableDeParser.deParse(createTable);
	}
	
	@Override
    public void visit(Insert insert) {
        selectDeParser.setBuffer(buffer);
        expressionDeParser.setSelectVisitor(selectDeParser);
        expressionDeParser.setBuffer(buffer);
        selectDeParser.setExpressionVisitor(expressionDeParser);
        InsertEncrypted insertDeParser = new InsertEncrypted(expressionDeParser, selectDeParser, buffer, schemaMgr);
        insertDeParser.deParse(insert);
    }
	
}
