package it.musichub.server.search;

import java.io.Serializable;

import org.apache.commons.jexl3.JexlExpression;

public class QueryOLD implements Serializable {

	private JexlExpression expression;

	public QueryOLD(JexlExpression expression) {
		super();
		this.expression = expression;
	}

	public JexlExpression getExpression() {
		return expression;
	}
	
}
