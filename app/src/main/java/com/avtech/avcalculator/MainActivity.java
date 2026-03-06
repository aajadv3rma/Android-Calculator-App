package com.avtech.avcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvExpression;
    private TextView tvResult;
    private ImageButton btnHistory;
    private ImageButton btnBackspace;

    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    private Button btnPlus, btnMinus, btnMultiply, btnDivide;
    private Button btnClear, btnPlusMinus, btnPercent, btnDecimal, btnEquals;

    private CalculatorLogic calculatorLogic;
    private HistoryManager historyManager;

    private Animation scaleDown;
    private Animation scaleUp;

    private StringBuilder currentExpression = new StringBuilder();
    private String currentResult = "0";
    private boolean isNewCalculation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make status bar transparent
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        initializeViews();
        initializeLogic();
        loadAnimations();
        setupButtonListeners();
    }

    private void initializeViews() {
        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);

        btnHistory = findViewById(R.id.btnHistory);
        btnBackspace = findViewById(R.id.btnBackspace);

        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);

        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        btnMultiply = findViewById(R.id.btnMultiply);
        btnDivide = findViewById(R.id.btnDivide);

        btnClear = findViewById(R.id.btnClear);
        btnPlusMinus = findViewById(R.id.btnPlusMinus);
        btnPercent = findViewById(R.id.btnPercent);
        btnDecimal = findViewById(R.id.btnDecimal);
        btnEquals = findViewById(R.id.btnEquals);
    }

    private void initializeLogic() {
        calculatorLogic = new CalculatorLogic();
        historyManager = new HistoryManager(this);
    }

    private void loadAnimations() {
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.button_press);
        scaleUp = AnimationUtils.loadAnimation(this, R.anim.button_release);
    }

    private void setupButtonListeners() {
        View[] allButtons = {
                btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9,
                btnPlus, btnMinus, btnMultiply, btnDivide,
                btnClear, btnPlusMinus, btnPercent, btnDecimal, btnEquals,
                btnBackspace
        };

        for (View btn : allButtons) {
            btn.setOnClickListener(this);
            addPressAnimation(btn);
        }

        btnBackspace.setOnLongClickListener(v -> {
            performHaptic(v);
            clearAll();
            return true;
        });

        btnHistory.setOnClickListener(v -> {
            performHaptic(v);
            openHistoryActivity();
        });
        addPressAnimation(btnHistory);
    }

    private void addPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.92f)
                            .scaleY(0.92f)
                            .setDuration(100)
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .setInterpolator(new android.view.animation.OvershootInterpolator())
                            .start();
                    break;
            }
            return false;
        });
    }

    @Override
    public void onClick(View v) {
        performHaptic(v);

        int id = v.getId();

        if (id == R.id.btn0) appendNumber("0");
        else if (id == R.id.btn1) appendNumber("1");
        else if (id == R.id.btn2) appendNumber("2");
        else if (id == R.id.btn3) appendNumber("3");
        else if (id == R.id.btn4) appendNumber("4");
        else if (id == R.id.btn5) appendNumber("5");
        else if (id == R.id.btn6) appendNumber("6");
        else if (id == R.id.btn7) appendNumber("7");
        else if (id == R.id.btn8) appendNumber("8");
        else if (id == R.id.btn9) appendNumber("9");
        else if (id == R.id.btnPlus) appendOperator("+");
        else if (id == R.id.btnMinus) appendOperator("−");
        else if (id == R.id.btnMultiply) appendOperator("×");
        else if (id == R.id.btnDivide) appendOperator("÷");
        else if (id == R.id.btnClear) clearAll();
        else if (id == R.id.btnPlusMinus) toggleSign();
        else if (id == R.id.btnPercent) calculatePercent();
        else if (id == R.id.btnDecimal) appendDecimal();
        else if (id == R.id.btnEquals) calculateResult();
        else if (id == R.id.btnBackspace) deleteLastCharacter();
    }

    private void appendNumber(String number) {
        if (isNewCalculation) {
            currentExpression = new StringBuilder();
            isNewCalculation = false;
        }

        String expr = currentExpression.toString();
        if (expr.equals("0") && number.equals("0")) return;
        if (expr.equals("0")) currentExpression = new StringBuilder();

        currentExpression.append(number);
        updateDisplay();
        calculateRealTimeResult();
    }

    private void appendOperator(String operator) {
        if (currentExpression.length() == 0) {
            if (!currentResult.equals("0") && !currentResult.equals("Error")) {
                currentExpression.append(currentResult.replace(",", ""));
            } else {
                return;
            }
        }

        String lastChar = getLastCharacter();
        if (isOperator(lastChar)) {
            while (currentExpression.length() > 0 &&
                    (currentExpression.toString().endsWith(" ") ||
                            isOperator(getLastCharacter()))) {
                currentExpression.deleteCharAt(currentExpression.length() - 1);
            }
        }

        currentExpression.append(" ").append(operator).append(" ");
        isNewCalculation = false;
        updateDisplay();
    }

    private void appendDecimal() {
        if (isNewCalculation) {
            currentExpression = new StringBuilder("0");
            isNewCalculation = false;
        }

        String lastNumber = getLastNumber();
        if (!lastNumber.contains(".")) {
            if (currentExpression.length() == 0 || isOperator(getLastCharacter())) {
                currentExpression.append("0");
            }
            currentExpression.append(".");
            updateDisplay();
        }
    }

    private void toggleSign() {
        if (currentExpression.length() == 0) {
            if (!currentResult.equals("0") && !currentResult.equals("Error")) {
                try {
                    double value = Double.parseDouble(currentResult.replace(",", ""));
                    value = -value;
                    currentResult = calculatorLogic.formatResult(value);
                    tvResult.setText(currentResult);
                } catch (NumberFormatException ignored) {}
            }
            return;
        }

        String expr = currentExpression.toString();
        String[] parts = expr.split(" ");

        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            if (!lastPart.isEmpty() && !isOperator(lastPart)) {
                if (lastPart.startsWith("(-") && lastPart.endsWith(")")) {
                    lastPart = lastPart.substring(2, lastPart.length() - 1);
                } else {
                    lastPart = "(-" + lastPart + ")";
                }

                StringBuilder newExpr = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    newExpr.append(parts[i]).append(" ");
                }
                newExpr.append(lastPart);
                currentExpression = new StringBuilder(newExpr.toString().trim());

                updateDisplay();
                calculateRealTimeResult();
            }
        }
    }

    private void calculatePercent() {
        if (currentExpression.length() > 0) {
            try {
                String lastNumber = getLastNumber().replace("(-", "-").replace(")", "").trim();
                if (!lastNumber.isEmpty()) {
                    double value = Double.parseDouble(lastNumber) / 100;
                    String formattedValue = calculatorLogic.formatResult(value);

                    String expr = currentExpression.toString();
                    int lastIndex = expr.lastIndexOf(getLastNumber());
                    if (lastIndex >= 0) {
                        currentExpression = new StringBuilder(expr.substring(0, lastIndex) + formattedValue);
                        updateDisplay();
                        calculateRealTimeResult();
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void deleteLastCharacter() {
        if (currentExpression.length() > 0) {
            String expr = currentExpression.toString();

            if (expr.endsWith(" ")) {
                int charsToRemove = Math.min(3, currentExpression.length());
                currentExpression.delete(currentExpression.length() - charsToRemove, currentExpression.length());
            } else {
                currentExpression.deleteCharAt(currentExpression.length() - 1);
            }

            if (currentExpression.length() == 0) {
                currentResult = "0";
                tvResult.setText("0");
            } else {
                calculateRealTimeResult();
            }
            updateDisplay();
        }
    }

    private void clearAll() {
        currentExpression = new StringBuilder();
        currentResult = "0";
        isNewCalculation = true;

        tvExpression.animate().alpha(0f).setDuration(100).withEndAction(() -> {
            tvExpression.setText("");
            tvExpression.animate().alpha(1f).setDuration(100).start();
        }).start();

        tvResult.animate().alpha(0f).setDuration(100).withEndAction(() -> {
            tvResult.setText("0");
            tvResult.animate().alpha(1f).setDuration(100).start();
        }).start();
    }

    private void calculateResult() {
        if (currentExpression.length() == 0) return;

        String expression = currentExpression.toString();
        String result = calculatorLogic.evaluate(expression);

        if (!result.equals("Error")) {
            historyManager.addCalculation(expression, result);

            tvExpression.setText(expression + " =");
            animateResultChange(result);

            currentResult = result;
            currentExpression = new StringBuilder();
            isNewCalculation = true;
        } else {
            animateResultChange("Error");
            currentResult = "Error";
        }
    }

    private void animateResultChange(String newResult) {
        tvResult.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100)
                .withEndAction(() -> {
                    tvResult.setText(newResult);
                    tvResult.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .setInterpolator(new android.view.animation.OvershootInterpolator())
                            .start();
                }).start();
    }

    private void calculateRealTimeResult() {
        if (currentExpression.length() == 0) {
            currentResult = "0";
            return;
        }

        String expression = currentExpression.toString().trim();

        if (expression.endsWith("+") || expression.endsWith("−") ||
                expression.endsWith("×") || expression.endsWith("÷")) {
            return;
        }

        String result = calculatorLogic.evaluate(expression);
        if (!result.equals("Error")) {
            currentResult = result;
            tvResult.setText(currentResult);
        }
    }

    private void updateDisplay() {
        tvExpression.setText(currentExpression.toString());

        if (currentExpression.length() == 0) {
            tvResult.setText(currentResult.isEmpty() ? "0" : currentResult);
        }
    }

    private String getLastCharacter() {
        if (currentExpression.length() == 0) return "";
        String expr = currentExpression.toString().trim();
        return expr.isEmpty() ? "" : String.valueOf(expr.charAt(expr.length() - 1));
    }

    private String getLastNumber() {
        String expr = currentExpression.toString().trim();
        String[] parts = expr.split("[+−×÷]");
        return parts.length > 0 ? parts[parts.length - 1].trim() : expr;
    }

    private boolean isOperator(String str) {
        return str.equals("+") || str.equals("−") || str.equals("×") ||
                str.equals("÷") || str.equals(" ");
    }

    private void performHaptic(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    private void openHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}