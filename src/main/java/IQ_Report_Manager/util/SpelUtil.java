package IQ_Report_Manager.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@Slf4j
@Component
public class SpelUtil {

    private final ExpressionParser parser = new SpelExpressionParser();

    public String evaluate(String expression, Map<String, Object> root) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext(root);
            Expression exp = parser.parseExpression(expression);

            Object value = exp.getValue(context);

            if (value == null) return "";

            // Auto format time fields
            if (value instanceof Number && expression.toLowerCase().contains("time")) {
                long millis = Long.parseLong(value.toString());
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new Date(millis));
            }

            return value.toString();

        } catch (Exception e) {
            log.error("Error evaluating SpEL expression:" + expression, e);
            return "INVALID_EXPRESSION: " + expression;
        }
    }
}
