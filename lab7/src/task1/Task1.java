// Задание 1. Демонстрация поведенческих паттернов «Команда»
// и «Итератор» на коде приложения aviacassa.

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class Task1 {
    public static void main(String[] args) {
        System.out.println("Команда:");
        BookingProcessor processor = new BookingProcessor();
        processor.submit(new CreateBookingCommand("SU-1360", 2));
        processor.submit(new ConfirmPaymentCommand("8E58F476B45E"));
        processor.submit(new CancelBookingCommand("000000000001"));
        processor.processAll();

        System.out.println();
        System.out.println("Итератор:");
        Booking booking = new Booking("8E58F476B45E");
        booking.addPassenger("Иванов И.И.");
        booking.addPassenger("Петров П.П.");
        booking.addPassenger("Сидоров С.С.");
        Iterator<String> it = booking.iterator();
        while (it.hasNext()) {
            System.out.println("  пассажир: " + it.next());
        }
    }
}

// Паттерн «Команда»: каждая операция бронирования инкапсулирована
// в отдельном объекте-команде с методом execute().
interface BookingCommand {
    void execute();
}

class CreateBookingCommand implements BookingCommand {
    private final String flight;
    private final int passengers;

    public CreateBookingCommand(String flight, int passengers) {
        this.flight = flight;
        this.passengers = passengers;
    }

    @Override
    public void execute() {
        System.out.println("  бронирование создано: рейс " + flight
                + ", мест " + passengers);
    }
}

class ConfirmPaymentCommand implements BookingCommand {
    private final String reference;

    public ConfirmPaymentCommand(String reference) {
        this.reference = reference;
    }

    @Override
    public void execute() {
        System.out.println("  платеж по брони " + reference + " подтвержден");
    }
}

class CancelBookingCommand implements BookingCommand {
    private final String reference;

    public CancelBookingCommand(String reference) {
        this.reference = reference;
    }

    @Override
    public void execute() {
        System.out.println("  бронирование " + reference + " отменено");
    }
}

// Инвокер накапливает команды в очереди и выполняет их по порядку.
class BookingProcessor {
    private final Deque<BookingCommand> queue = new ArrayDeque<>();

    public void submit(BookingCommand command) {
        queue.add(command);
    }

    public void processAll() {
        while (!queue.isEmpty()) {
            queue.poll().execute();
        }
    }
}

// Паттерн «Итератор»: обход пассажиров бронирования не раскрывает
// внутреннее хранилище списка.
class Booking implements Iterable<String> {
    private final String reference;
    private final java.util.List<String> passengers = new java.util.ArrayList<>();

    public Booking(String reference) {
        this.reference = reference;
    }

    public void addPassenger(String name) {
        passengers.add(name);
    }

    @Override
    public Iterator<String> iterator() {
        return passengers.iterator();
    }
}
