package it.musichub.server.search.model;

public abstract class AbstractClause implements Clause {
	private LogicalOperator logicalOperator = LogicalOperator.AND;

	public AbstractClause() {
		super();
	}

	public AbstractClause(LogicalOperator logicalOperator) {
		this();
		this.logicalOperator = logicalOperator;
	}

	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}

	public void setLogicalOperator(LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	public abstract String getExpression();

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [logicalOperator=" + getLogicalOperator() + ", expression="
				+ getExpression() + "]";
	}
}