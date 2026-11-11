// Задание 1. Демонстрация порождающих паттернов «Фабричный метод»
// и «Абстрактная фабрика» на коде приложения aviacassa.

import java.math.BigDecimal;
import java.util.UUID;

public class Task1 {
    public static void main(String[] args) {
        PaymentService cardService = new CardPaymentService();
        PaymentService bankService = new BankTransferPaymentService();

        System.out.println("Фабричный метод:");
        System.out.println("  " + cardService.initPayment().describe());
        System.out.println("  " + bankService.initPayment().describe());

        System.out.println();
        System.out.println("Абстрактная фабрика (карта):");
        PaymentProviderFactory cardFactory = new CardProviderFactory();
        System.out.println("  валидатор: " + cardFactory.createValidator().validate("4111111111111111"));
        System.out.println("  комиссия:  " + cardFactory.createFeeCalculator()
                .calculateFee(new BigDecimal("15800.00")));

        System.out.println("Абстрактная фабрика (банковский перевод):");
        PaymentProviderFactory bankFactory = new BankTransferProviderFactory();
        System.out.println("  валидатор: " + bankFactory.createValidator().validate("40817810099000000000"));
        System.out.println("  комиссия:  " + bankFactory.createFeeCalculator()
                .calculateFee(new BigDecimal("15800.00")));
    }
}

abstract class Payment {
    private final String transactionId;

    protected Payment() {
        this.transactionId = UUID.randomUUID().toString();
    }

    public String describe() {
        return "платёж " + method() + ", транзакция " + transactionId;
    }

    protected abstract String method();
}

class CardPayment extends Payment {
    @Override
    protected String method() {
        return "CREDIT_CARD";
    }
}

class BankTransferPayment extends Payment {
    @Override
    protected String method() {
        return "BANK_TRANSFER";
    }
}

abstract class PaymentService {
    public Payment initPayment() {
        Payment payment = createPayment();
        System.out.println("  создан объект типа " + payment.getClass().getSimpleName());
        return payment;
    }

    protected abstract Payment createPayment();
}

class CardPaymentService extends PaymentService {
    @Override
    protected Payment createPayment() {
        return new CardPayment();
    }
}

class BankTransferPaymentService extends PaymentService {
    @Override
    protected Payment createPayment() {
        return new BankTransferPayment();
    }
}

interface PaymentValidator {
    String validate(String details);
}

interface FeeCalculator {
    BigDecimal calculateFee(BigDecimal amount);
}

interface PaymentProviderFactory {
    PaymentValidator createValidator();

    FeeCalculator createFeeCalculator();
}

class CardProviderFactory implements PaymentProviderFactory {
    @Override
    public PaymentValidator createValidator() {
        return details -> details.matches("\\d{16}")
                ? "номер карты корректен"
                : "некорректный номер карты";
    }

    @Override
    public FeeCalculator createFeeCalculator() {
        return amount -> amount.multiply(new BigDecimal("0.02"));
    }
}

class BankTransferProviderFactory implements PaymentProviderFactory {
    @Override
    public PaymentValidator createValidator() {
        return details -> details.startsWith("408")
                ? "счёт плательщика корректен"
                : "некорректный счёт плательщика";
    }

    @Override
    public FeeCalculator createFeeCalculator() {
        return amount -> new BigDecimal("50.00");
    }
}
