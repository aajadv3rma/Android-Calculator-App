package com.avtech.avcalculator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * CalculatorLogic - Handles all mathematical calculations
 */
public class CalculatorLogic {

    private static final int MAX_DECIMAL_PLACES = 10;
    private DecimalFormat decimalFormat;
    private DecimalFormat scientificFormat;

    public CalculatorLogic() {
        StringBuilder pattern = new StringBuilder("#,##0.");
        for (int i = 0; i < MAX_DECIMAL_PLACES; i++) {
            pattern.append("#");
        }
        decimalFormat = new DecimalFormat(pattern.toString());
        scientificFormat = new DecimalFormat("0.######E0");
    }

    /**
     * Evaluate a mathematical expression
     */
    public String evaluate(String expression) {
        try {
            expression = expression.trim();

            if (expression.isEmpty()) {
                return "0";
            }

            // Replace display operators with standard operators
            expression = expression
                    .replace("×", "*")
                    .replace("÷", "/")
                    .replace("−", "-")
                    .replace("(-", "(0-")
                    .replace(",", "")
                    .replace(" ", "");

            // Remove trailing operators
            while (expression.endsWith("+") || expression.endsWith("-") ||
                    expression.endsWith("*") || expression.endsWith("/")) {
                expression = expression.substring(0, expression.length() - 1);
            }

            if (expression.isEmpty()) {
                return "0";
            }

            List<String> tokens = tokenize(expression);

            if (tokens.isEmpty()) {
                return "0";
            }

            double result = calculate(tokens);
            return formatResult(result);

        } catch (Exception e) {
            return "Error";
        }
    }

    /**
     * Tokenize an expression into numbers and operators
     */
    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);
            } else if (c == '-' && (i == 0 || isOperatorChar(expression.charAt(i - 1)) ||
                    expression.charAt(i - 1) == '(')) {
                currentNumber.append(c);
            } else if (c == '(' || c == ')') {
                if (currentNumber.length() > 0) {
                    tokens.add(currentNumber.toString());
                    currentNumber = new StringBuilder();
                }
                tokens.add(String.valueOf(c));
            } else if (isOperatorChar(c)) {
                if (currentNumber.length() > 0) {
                    tokens.add(currentNumber.toString());
                    currentNumber = new StringBuilder();
                }
                tokens.add(String.valueOf(c));
            }
        }

        if (currentNumber.length() > 0) {
            tokens.add(currentNumber.toString());
        }

        return tokens;
    }

    private boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    /**
     * Calculate result from tokens
     */
    private double calculate(List<String> tokens) {
        List<String> rpn = toRPN(tokens);
        return evaluateRPN(rpn);
    }

    /**
     * Convert tokens to Reverse Polish Notation
     */
    private List<String> toRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                output.add(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                if (!operators.isEmpty()) {
                    operators.pop();
                }
            } else if (isOperator(token)) {
                while (!operators.isEmpty() &&
                        !operators.peek().equals("(") &&
                        getPrecedence(operators.peek()) >= getPrecedence(token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        return output;
    }

    /**
     * Evaluate Reverse Polish Notation expression
     */
    private double evaluateRPN(List<String> rpn) {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            if (isNumber(token)) {
                stack.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid expression");
                }
                double b = stack.pop();
                double a = stack.pop();
                stack.push(applyOperator(a, b, token));
            }
        }

        if (stack.isEmpty()) {
            return 0;
        }

        return stack.pop();
    }

    /**
     * Apply an operator to two operands
     */
    private double applyOperator(double a, double b, String operator) {
        switch (operator) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    /**
     * Get operator precedence
     */
    private int getPrecedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default:
                return 0;
        }
    }

    /**
     * Check if token is a number
     */
    private boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if token is an operator
     */
    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") ||
                token.equals("*") || token.equals("/");
    }

    /**
     * Format the result for display
     */
    public String formatResult(double result) {
        if (Double.isInfinite(result) || Double.isNaN(result)) {
            return "Error";
        }

        if (Math.abs(result) >= 1e10 || (Math.abs(result) < 1e-6 && result != 0)) {
            return scientificFormat.format(result);
        }

        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return decimalFormat.format((long) result);
        }

        String formatted = decimalFormat.format(result);

        if (formatted.contains(".")) {
            formatted = formatted.replaceAll("0+$", "");
            formatted = formatted.replaceAll("\\.$", "");
        }

        return formatted;
    }
}