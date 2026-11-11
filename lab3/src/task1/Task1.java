// Задание 1. Демонстрация структурных паттернов «Адаптер» и «Мост»
// на коде приложения бронирования авиабилетов (aviacassa).

import java.math.BigDecimal;

public class Task1 {
    public static void main(String[] args) {
        System.out.println("Адаптер:");
        PaymentGateway gateway = new ExternalPaymentApiAdapter(new ExternalPaymentApi());
        PaymentResult result = gateway.pay("4111111111111111", new BigDecimal("15800.00"));
        System.out.println("  результат: " + result);

        System.out.println();
        System.out.println("Мост:");
        Notification bookingEmail = new BookingNotification(new EmailSender());
        Notification paymentSms = new PaymentNotification(new SmsSender());
        bookingEmail.send("user@example.com");
        paymentSms.send("+79001234567");
    }
}

// Паттерн «Адаптер»: внешний платежный шлюз говорит на своем протоколе,
// адаптер переводит вызовы к нему в интерфейс PaymentGateway.
class ExternalPaymentApi {
    public String makeTransaction(double amountRub, String cardNumber) {
        return "EXT-" + cardNumber.substring(cardNumber.length() - 4)
                + ":" + (int) (amountRub * 100);
    }
}

interface PaymentGateway {
    PaymentResult pay(String cardNumber, BigDecimal amount);
}

record PaymentResult(boolean success, String transactionId) {
}

class ExternalPaymentApiAdapter implements PaymentGateway {
    private final ExternalPaymentApi api;

    public ExternalPaymentApiAdapter(ExternalPaymentApi api) {
        this.api = api;
    }

    @Override
    public PaymentResult pay(String cardNumber, BigDecimal amount) {
        String raw = api.makeTransaction(amount.doubleValue(), cardNumber);
        return new PaymentResult(raw.startsWith("EXT-"), raw);
    }
}

// Паттерн «Мост»: тип уведомления (абстракция) не зависит
// от канала доставки (реализация), они соединяются на этапе выполнения.
interface MessageSender {
    String deliver(String recipient, String text);
}

class EmailSender implements MessageSender {
    @Override
    public String deliver(String recipient, String text) {
        return "email на " + recipient + ": " + text;
    }
}

class SmsSender implements MessageSender {
    @Override
    public String deliver(String recipient, String text) {
        return "SMS на " + recipient + ": " + text;
    }
}

abstract class Notification {
    protected final MessageSender sender;

    protected Notification(MessageSender sender) {
        this.sender = sender;
    }

    public void send(String recipient) {
        String answer = sender.deliver(recipient, buildText());
        System.out.println("  отправлено: " + answer);
    }

    protected abstract String buildText();
}

class BookingNotification extends Notification {
    public BookingNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    protected String buildText() {
        return "Бронирование PENDING, оплата в течение 15 минут";
    }
}

class PaymentNotification extends Notification {
    public PaymentNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    protected String buildText() {
        return "Платеж успешно завершен, билеты доступны в личном кабинете";
    }
}
