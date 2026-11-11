// Задание 1. Демонстрация паттерна микросервисов «Circuit Breaker»
// на примере интеграции приложения aviacassa с платежным шлюзом.

public class Task1 {
    public static void main(String[] args) {
        PaymentGateway gateway = new UnreliablePaymentGateway();
        CircuitBreaker breaker = new CircuitBreaker(gateway, 2);

        for (int i = 1; i <= 6; i++) {
            System.out.println("вызов " + i + ": " + breaker.charge("TX-" + i));
        }
        System.out.println("пауза: размыкатель полностью восстановлен");
        breaker.reset();
        System.out.println("вызов 7: " + breaker.charge("TX-7"));
    }
}

interface PaymentGateway {
    String charge(String transactionId);
}

// Внешний шлюз: первые вызовы падают по таймауту.
class UnreliablePaymentGateway implements PaymentGateway {
    private int calls = 0;

    @Override
    public String charge(String transactionId) {
        calls++;
        if (calls <= 2) {
            throw new IllegalStateException("шлюз недоступен (таймаут)");
        }
        return "PAID";
    }
}

// Circuit Breaker: CLOSED (пропускает), OPEN (отклоняет сразу),
// HALF_OPEN после паузы пробует пропустить снова.
class CircuitBreaker {
    private final PaymentGateway inner;
    private final int failureThreshold;
    private int failures = 0;
    private boolean open = false;

    public CircuitBreaker(PaymentGateway inner, int failureThreshold) {
        this.inner = inner;
        this.failureThreshold = failureThreshold;
    }

    public String charge(String transactionId) {
        if (open) {
            return "отклонено размыкателем (состояние OPEN)";
        }
        try {
            String result = inner.charge(transactionId);
            failures = 0;
            System.out.println("  размыкатель: CLOSED, успех");
            return result;
        } catch (IllegalStateException e) {
            failures++;
            System.out.println("  размыкатель: сбой " + failures + " из " + failureThreshold);
            if (failures >= failureThreshold) {
                open = true;
                System.out.println("  размыкатель разомкнут (OPEN)");
            }
            return "ошибка: " + e.getMessage();
        }
    }

    public void reset() {
        open = false;
        failures = 0;
    }
}
