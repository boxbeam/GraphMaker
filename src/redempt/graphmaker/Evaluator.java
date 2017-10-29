package redempt.graphmaker;

import java.text.DecimalFormat;

public class Evaluator {
	
	private String expression;
	private String[] conditions = null;
	private static DecimalFormat format;
	
	static {
		format = new DecimalFormat("#");
		format.setMaximumFractionDigits(10);
	}
	
	public Evaluator(String expression) {
		this.expression = expression.replaceAll("\\s", "");
		String[] split = expression.split(";");
		this.expression = split[0];
		if (split.length > 1) {
			conditions = new String[split.length - 1];
			for (int i = 1; i < split.length; i++) {
				conditions[i - 1] = split[i];
			}
		}
	}
	
	public double evaluate(double input) {
		if (conditions != null) {
			for (String condition : conditions) {
				System.out.println(condition);
				String[] split;
				if ((split = condition.split(">=")).length > 1) {
					System.out.println(split[0]);
					if (!(new Evaluator(split[0]).evaluate(input) >= new Evaluator(split[1]).evaluate(input))) {
						return Double.NaN;
					}
					continue;
				}
				if ((split = condition.split(">")).length > 1) {
					System.out.println(split[0]);
					if (!(new Evaluator(split[0]).evaluate(input) > new Evaluator(split[1]).evaluate(input))) {
						return Double.NaN;
					}
					continue;
				}
				if ((split = condition.split("<=")).length > 1) {
					System.out.println(split[0]);
					if (!(new Evaluator(split[0]).evaluate(input) <= new Evaluator(split[1]).evaluate(input))) {
						return Double.NaN;
					}
					continue;
				}
				if ((split = condition.split("<")).length > 1) {
					System.out.println(split[0]);
					if (!(new Evaluator(split[0]).evaluate(input) < new Evaluator(split[1]).evaluate(input))) {
						return Double.NaN;
					}
					continue;
				}
			}
		}
		String expression = this.expression.replaceAll("(\\d|\\))x", "$1*" + format.format(input)).replace("x", format.format(input));
		expression = expression.replace("sin", "s").replace("cos", "c").replace("tan", "t").replace("abs", "a");
		expression = expression.replace(")(", ")*(").replaceAll("(\\d|x)\\(", "$1*(");
		char[] chars = expression.toCharArray();
		while (expression.contains("(") || expression.contains(")")) {
			int depth = 0;
			int start = -1;
			int end = -1;
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (c == '(') {
					depth++;
					if (depth == 1) {
						start = i;
					}
				}
				if (c == ')') {
					depth--;
					if (depth == 0 && start != -1) {
						end = i;
						String exp = expression.substring(start + 1, end);
						expression = expression.substring(0, start) + new Evaluator(exp).evaluate(0) + expression.substring(end + 1);
						chars = expression.toCharArray();
						break;
					}
				}
			}
			if (depth != 0) {
				throw new IllegalArgumentException("Mistmatched parenthesis!");
			}
		}
		if (expression.contains("NaN")) {
			return Double.NaN;
		}
		char operation = ' ';
		while (expression.contains("s") || expression.contains("c")) {
			int first = expression.contains("s") ? expression.indexOf('s') : expression.indexOf('c');
			String firstNumber = getNumberForwards(expression, first + 1);
			int start = first;
			int end = first + firstNumber.length();
			double firstVal = Double.parseDouble(firstNumber);
			double value = expression.charAt(first) == 's' ? Math.sin(firstVal) : Math.cos(firstVal);
			expression = expression.substring(0, start) + format.format(value) + expression.substring(end + 1, expression.length());
		}
		while (expression.contains("t") || expression.contains("a")) {
			int first = expression.contains("t") ? expression.indexOf('t') : expression.indexOf('a');
			String firstNumber = getNumberForwards(expression, first + 1);
			int start = first;
			int end = first + firstNumber.length();
			double firstVal = Double.parseDouble(firstNumber);
			double value = expression.charAt(first) == 't' ? Math.tan(firstVal) : Math.abs(firstVal);
			expression = expression.substring(0, start) + format.format(value) + expression.substring(end + 1, expression.length());
		}
		while (expression.contains("^")) {
			int first = expression.indexOf('^');
			String firstNumber = getNumberBackwards(expression, first - 1);
			String secondNumber = getNumberForwards(expression, first + 1);
			int start = first - firstNumber.length();
			int end = first + secondNumber.length();
			double firstVal = Double.parseDouble(firstNumber);
			double secondVal = Double.parseDouble(secondNumber);
			double value = Math.pow(firstVal, secondVal);
			expression = expression.substring(0, start) + format.format(value) + expression.substring(end + 1, expression.length());
		}
		while (expression.contains("/") || expression.contains("*")) {
			int first = expression.contains("/") ? expression.indexOf("/") : expression.indexOf("*");
			operation = expression.charAt(first);
			String firstNumber = getNumberBackwards(expression, first - 1);
			String secondNumber = getNumberForwards(expression, first + 1);
			int start = first - firstNumber.length();
			int end = first + secondNumber.length();
			double firstVal = Double.parseDouble(firstNumber);
			double secondVal = Double.parseDouble(secondNumber);
			double value = operation == '/' ? firstVal / secondVal : firstVal * secondVal;
			expression = expression.substring(0, start) + format.format(value) + expression.substring(end + 1, expression.length());
		}
		boolean ignoreSubtract = false;
		while (expression.contains("+") || expression.contains("-")) {
			int first = expression.contains("+") ? expression.indexOf("+") : expression.indexOf("-");
			if (expression.substring(1).contains("-")) {
				first = expression.substring(1).indexOf('-') + 1;
			}
			operation = expression.charAt(first);
			String firstNumber = getNumberBackwards(expression, first - 1);
			String secondNumber = getNumberForwards(expression, first + 1);
			int start = first - firstNumber.length();
			int end = first + secondNumber.length();
			try {
				double firstVal = Double.parseDouble(firstNumber);
				double secondVal = Double.parseDouble(secondNumber);
				double value = operation == '+' ? firstVal + secondVal : firstVal - secondVal;
				expression = expression.substring(0, start) + format.format(value) + expression.substring(end + 1, expression.length());
			} catch (NumberFormatException e) {
				if (ignoreSubtract) {
					break;
				}
				ignoreSubtract = true;
			}
		}
		try {
			return Double.parseDouble(expression);
		} catch (NumberFormatException e) {
			return Double.NaN;
		}
	}
	
	private String getNumberForwards(String expression, int pos) {
		String combined = "";
		for (int i = pos; i < expression.length(); i++) {
			char c = expression.charAt(i);
			if (Character.isDigit(c) || c == '.' || (c == '-' && i == pos)) {
				combined += c;
			} else {
				break;
			}
		}
		return combined;
	}
	
	private String getNumberBackwards(String expression, int pos) {
		String combined = "";
		for (int i = pos; i >= 0; i--) {
			char c = expression.charAt(i);
			if (Character.isDigit(c) || c == '.' || c == '-') {
				combined = c + combined;
				if (c == '-') {
					break;
				}
			} else {
				break;
			}
		}
		return combined;
	}
	
}
