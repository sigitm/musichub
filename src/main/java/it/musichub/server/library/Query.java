package it.musichub.server.library;

import java.io.Serializable;

import org.apache.commons.jexl3.JexlExpression;

public class Query implements Serializable {

	private JexlExpression expression;

	public Query(JexlExpression expression) {
		super();
		this.expression = expression;
	}

	public JexlExpression getExpression() {
		return expression;
	}
	
}
