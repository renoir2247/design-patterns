// Задание 1. Демонстрация поведенческих паттернов «Посредник»
// и «Снимок» на коде приложения aviacassa.

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task1 {
    public static void main(String[] args) {
        System.out.println("Посредник:");
        BookingMediator mediator = new BookingMediator();
        PaymentWebhook.process("TX-1", "PAID", mediator);

        System.out.println();
        System.out.println("Снимок:");
        Booking booking = new Booking("8E58F476B45E", "PENDING");
        BookingMemento saved = booking.save();
        System.out.println("  сохранен снимок: " + saved.status());

        booking.setStatus("CONFIRMED");
        System.out.println("  текущий статус: " + booking.getStatus());

        booking.restore(saved);
        System.out.println("  восстановлен снимок: " + booking.getStatus());
    }
}

// Паттерн «Посредник»: вебхук платежной системы не обращается к сервисам
// напрямую, а сообщает посреднику о событии, тот координирует остальных.
class BookingMediator {
    private final List<String> eventLog = new ArrayList<>();

    public void onPaymentPaid(String transactionId) {
        eventLog.add("платеж " + transactionId + " подтвержден");
        System.out.println("  посредник: подтверждение платежа " + transactionId);
        notifyBooking("статус брони CONFIRMED");
        notifyTickets("выпуск билетов для пассажиров");
        notifyNotification("письмо пользователю отправлено");
    }

    private void notifyBooking(String event) {
        System.out.println("  сервис бронирований: " + event);
    }

    private void notifyTickets(String event) {
        System.out.println("  сервис билетов: " + event);
    }

    private void notifyNotification(String event) {
        System.out.println("  сервис уведомлений: " + event);
    }
}

class PaymentWebhook {
    public static void process(String transactionId, String status, BookingMediator mediator) {
        if (status.equals("PAID")) {
            mediator.onPaymentPaid(transactionId);
        }
    }
}

// Паттерн «Снимок»: состояние брони сохраняется и восстанавливается
// без нарушения инкапсуляции.
class Booking {
    private final String reference;
    private String status;

    public Booking(String reference, String status) {
        this.reference = reference;
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = status;
        System.out.println("  (статус изменен: " + status + ")");
    }

    public String getStatus() {
        return status;
    }

    public BookingMemento save() {
        return new BookingMemento(reference, status, LocalDateTime.now());
    }

    public void restore(BookingMemento memento) {
        this.status = memento.status();
    }
}

record BookingMemento(String reference, String status, LocalDateTime savedAt) {}
