package it.musichub.server.search.model;

public class BasicClause extends AbstractClause {
	private String expression;

	public BasicClause(String expression) {
		super();
		this.expression = expression;
	}

	public BasicClause(LogicalOperator logicalOperator, String expression) {
		super(logicalOperator);
		this.expression = expression;
	}

	@Override
	public String getExpression() {
		return expression;
	}
}