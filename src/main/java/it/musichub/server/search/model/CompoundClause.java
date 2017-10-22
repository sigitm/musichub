package it.musichub.server.search.model;

import java.util.ArrayList;
import java.util.List;

public class CompoundClause extends AbstractClause {
	private List<Clause> clauses = new ArrayList<>();
	
	public CompoundClause() {
		super();
	}

	public CompoundClause(LogicalOperator logicalOperator) {
		super(logicalOperator);
	}

	public void addClause(Clause clause) {
		clauses.add(clause);
	}

	@Override
	public String getExpression() {
		if (clauses.isEmpty())
			return "1==1";
		
		StringBuilder sb = new StringBuilder();
		boolean firstClause = true; //TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX importare le StringUtils.concat
		for (Clause clause : clauses){
			if (!firstClause){
				sb.append(" ");
				sb.append(clause.getLogicalOperator().getExpression());
				sb.append(" ");
			}
			sb.append(" ( ");
			sb.append(clause.getExpression());
			sb.append(" ) ");
			firstClause = false;
		}
		return sb.toString();
	}
}