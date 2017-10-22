package it.musichub.server.search.model;

public class SimpleClause extends AbstractClause {

	public static enum Operator {
		EQUALS {
			@Override
			public String getExpression() {
				return "==";
			}
		},

		NOT_EQUALS {
			@Override
			public String getExpression() {
				return "!=";
			}
		},

		LIKE {
			@Override
			public String getExpression() {
				return "=~";
			}
		},

		NOT_LIKE {
			@Override
			public String getExpression() {
				return "!~";
			}
		},

		LESS {
			@Override
			public String getExpression() {
				return "<";
			}
		},

		LESS_EQUALS {
			@Override
			public String getExpression() {
				return "<=";
			}
		},

		GREATER {
			@Override
			public String getExpression() {
				return ">";
			}
		},

		GREATER_EQUALS {
			@Override
			public String getExpression() {
				return ">=";
			}
		};

		public abstract String getExpression();
	};

	private String property;
	private Operator operator;
	private Object value;
	// private boolean caseSensitive = false; //TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

	public SimpleClause(String property, Operator operator, Object value) {
		super();
		this.property = property;
		this.operator = operator;
		this.value = value;
	}

	public SimpleClause(LogicalOperator logicalOperator, String property, Operator operator, Object value) {
		super(logicalOperator);
		this.property = property;
		this.operator = operator;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String getExpression() {
		String propertyStr = "song." + property;
		String valueStr = value != null ? value.toString() : null;

		if (valueStr != null) {
			// regex escapes
			if (operator == Operator.LIKE || operator == Operator.NOT_LIKE)
				valueStr = wildcardToRegex(valueStr);

			// setting string delimiters (+apostrophes escape)
			valueStr = valueStr.replaceAll("'", "\\\\'");
			valueStr = "'" + valueStr + "'";

			// handling case insensitive
			if (/* !caseSensitive && */ value instanceof String && (operator == Operator.EQUALS || operator == Operator.NOT_EQUALS || operator == Operator.LIKE || operator == Operator.NOT_LIKE)) {
				propertyStr += ".toLowerCase()";
				valueStr += ".toLowerCase()"; // TODO XXXXXXXXX CASO NULL
			}
		}

		return propertyStr + " " + operator.getExpression() + " " + valueStr;
	}

	private static String wildcardToRegex(String wildcardString) {
		if (wildcardString == null)
			return null;

		// The 12 is arbitrary, you may adjust it to fit your needs depending
		// on how many special characters you expect in a single pattern.
		StringBuilder sb = new StringBuilder(wildcardString.length() + 12);
		sb.append('^');
		for (int i = 0; i < wildcardString.length(); ++i) {
			char c = wildcardString.charAt(i);
			if (c == '*') {
				sb.append("\\w*");
			} else if (c == '?') {
				sb.append("\\w");
			} else if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
				sb.append('\\');
				sb.append(c);
			} else {
				sb.append(c);
			}
		}
		sb.append('$');
		return sb.toString();
	}
}