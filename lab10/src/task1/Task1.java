// Задание 1. Демонстрация поведенческих паттернов «Наблюдатель»
// и «Состояние» на коде приложения aviacassa.

import java.util.ArrayList;
import java.util.List;

public class Task1 {
    public static void main(String[] args) {
        System.out.println("Наблюдатель:");
        BookingEvents events = new BookingEvents();
        events.subscribe(new TicketIssuer());
        events.subscribe(new NotificationSender());
        events.publish("8E58F476B45E", "PAID");

        System.out.println();
        System.out.println("Состояние:");
        Booking booking = new Booking("8E58F476B45E");
        booking.pay();
        booking.pay();
        booking.expire();
        booking.pay();
    }
}

// Паттерн «Наблюдатель»: подписчики получают событие об оплате брони
// и реагируют независимо друг от друга.
interface BookingListener {
    void onEvent(String reference, String status);
}

class TicketIssuer implements BookingListener {
    @Override
    public void onEvent(String reference, String status) {
        System.out.println("  выпуск билетов по брони " + reference);
    }
}

class NotificationSender implements BookingListener {
    @Override
    public void onEvent(String reference, String status) {
        System.out.println("  письмо пользователю по брони " + reference);
    }
}

class BookingEvents {
    private final List<BookingListener> listeners = new ArrayList<>();

    public void subscribe(BookingListener listener) {
        listeners.add(listener);
    }

    public void publish(String reference, String status) {
        System.out.println("  событие: брони " + reference + " статус " + status);
        for (BookingListener listener : listeners) {
            listener.onEvent(reference, status);
        }
    }
}

// Паттерн «Состояние»: поведение брони (переходы статусов)
// определяется текущим объектом-состоянием.
class Booking {
    private BookingState state = new PendingState();
    private final String reference;

    public Booking(String reference) {
        this.reference = reference;
    }

    public void pay() {
        state.pay(this);
    }

    public void expire() {
        state.expire(this);
    }

    void setState(BookingState state) {
        this.state = state;
    }

    String reference() {
        return reference;
    }
}

interface BookingState {
    void pay(Booking booking);

    void expire(Booking booking);
}

class PendingState implements BookingState {
    @Override
    public void pay(Booking booking) {
        System.out.println("  бронь " + booking.reference() + ": PENDING -> CONFIRMED");
        booking.setState(new ConfirmedState());
    }

    @Override
    public void expire(Booking booking) {
        System.out.println("  бронь " + booking.reference() + ": PENDING -> EXPIRED");
        booking.setState(new ExpiredState());
    }
}

class ConfirmedState implements BookingState {
    @Override
    public void pay(Booking booking) {
        System.out.println("  повторная оплата отклонена: бронь уже CONFIRMED");
    }

    @Override
    public void expire(Booking booking) {
        System.out.println("  истечение срока не применяется: бронь CONFIRMED");
    }
}

class ExpiredState implements BookingState {
    @Override
    public void pay(Booking booking) {
        System.out.println("  оплата отклонена: бронь EXPIRED");
    }

    @Override
    public void expire(Booking booking) {
        System.out.println("  бронь уже EXPIRED");
    }
}
