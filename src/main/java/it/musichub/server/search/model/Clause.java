package it.musichub.server.search.model;

public interface Clause {

	public static enum LogicalOperator {
		AND {
			@Override
			public String getExpression() {
				return "&&";
			}
		},

		OR {
			@Override
			public String getExpression() {
				return "||";
			}
		};

		public abstract String getExpression();
	};

	public LogicalOperator getLogicalOperator();

	public String getExpression();
}