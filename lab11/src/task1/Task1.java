// Задание 1. Демонстрация поведенческих паттернов «Стратегия»
// и «Шаблонный метод» на коде приложения aviacassa.

import java.math.BigDecimal;

public class Task1 {
    public static void main(String[] args) {
        System.out.println("Стратегия:");
        BigDecimal amount = new BigDecimal("15800.00");
        PriceCalculator calculator = new PriceCalculator(new NoDiscount());
        System.out.println("  без скидки:        " + calculator.finalPrice(amount));
        calculator.setStrategy(new GroupDiscount());
        System.out.println("  групповая скидка:  " + calculator.finalPrice(amount));
        calculator.setStrategy(new PromoDiscount());
        System.out.println("  промокод 15%:      " + calculator.finalPrice(amount));

        System.out.println();
        System.out.println("Шаблонный метод:");
        PaymentProcessor card = new CardPaymentProcessor();
        card.process("TX-1", amount);
        System.out.println();
        PaymentProcessor bank = new BankTransferProcessor();
        bank.process("TX-2", amount);
    }
}

// Паттерн «Стратегия»: алгоритм расчета скидки подменяется на лету.
interface DiscountStrategy {
    BigDecimal apply(BigDecimal amount);
}

class NoDiscount implements DiscountStrategy {
    @Override
    public BigDecimal apply(BigDecimal amount) {
        return amount;
    }
}

class GroupDiscount implements DiscountStrategy {
    @Override
    public BigDecimal apply(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.90"));
    }
}

class PromoDiscount implements DiscountStrategy {
    @Override
    public BigDecimal apply(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.85"));
    }
}

class PriceCalculator {
    private DiscountStrategy strategy;

    public PriceCalculator(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(DiscountStrategy strategy) {
        this.strategy = strategy;
        System.out.println("  стратегия заменена");
    }

    public BigDecimal finalPrice(BigDecimal amount) {
        return strategy.apply(amount);
    }
}

// Паттерн «Шаблонный метод»: каркас обработки платежа задан в базовом
// классе, подклассы реализуют только шаги валидации и списания.
abstract class PaymentProcessor {

    public final void process(String transactionId, BigDecimal amount) {
        System.out.println("  платеж " + transactionId + ":");
        if (!validate(amount)) {
            System.out.println("    валидация не пройдена, платеж отклонен");
            return;
        }
        withdraw(amount);
        System.out.println("    платеж завершен, статус PAID");
    }

    protected abstract boolean validate(BigDecimal amount);

    protected abstract void withdraw(BigDecimal amount);
}

class CardPaymentProcessor extends PaymentProcessor {
    @Override
    protected boolean validate(BigDecimal amount) {
        System.out.println("    валидация карты и лимита (комиссия 2%)");
        return true;
    }

    @Override
    protected void withdraw(BigDecimal amount) {
        System.out.println("    списание с карты: " + amount.multiply(new BigDecimal("1.02")));
    }
}

class BankTransferProcessor extends PaymentProcessor {
    @Override
    protected boolean validate(BigDecimal amount) {
        System.out.println("    валидация банковских реквизитов");
        return true;
    }

    @Override
    protected void withdraw(BigDecimal amount) {
        System.out.println("    списание со счета: " + amount + " + 50.00");
    }
}
