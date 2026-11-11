// Задание 1. Демонстрация паттерна работы с данными «Repository»
// на примере приложения aviacassa.

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Task1 {
    public static void main(String[] args) {
        BookingRepository repo = new InMemoryBookingRepository();

        Booking first = new Booking("8E58F476B45E", "PENDING");
        Booking second = new Booking("111111111111", "PENDING");
        repo.save(first);
        repo.save(second);

        System.out.println("  всего бронирований: " + repo.count());
        System.out.println("  поиск по номеру:    "
                + repo.findByReference("8E58F476B45E").map(Booking::reference).orElse("не найден"));
        System.out.println("  в статусе PENDING:  " + repo.findByStatus("PENDING").size());

        first.confirm();
        System.out.println("  после подтверждения в PENDING: "
                + repo.findByStatus("PENDING").size());
    }
}

// Model доменной области, не зависит от способа хранения.
class Booking {
    private final String reference;
    private String status;

    public Booking(String reference, String status) {
        this.reference = reference;
        this.status = status;
    }

    public String reference() {
        return reference;
    }

    public String status() {
        return status;
    }

    public void confirm() {
        status = "CONFIRMED";
    }
}

// Repository: коллекция объектов домена с интерфейсом доступа к данным.
interface BookingRepository {
    void save(Booking booking);

    Optional<Booking> findByReference(String reference);

    List<Booking> findByStatus(String status);

    long count();
}

// Конкретная реализация: хранение в памяти вместо базы данных.
class InMemoryBookingRepository implements BookingRepository {
    private final List<Booking> storage = new ArrayList<>();

    @Override
    public void save(Booking booking) {
        System.out.println("  сохранение брони " + booking.reference());
        storage.add(booking);
    }

    @Override
    public Optional<Booking> findByReference(String reference) {
        return storage.stream()
                .filter(b -> b.reference().equals(reference))
                .findFirst();
    }

    @Override
    public List<Booking> findByStatus(String status) {
        return storage.stream()
                .filter(b -> b.status().equals(status))
                .toList();
    }

    @Override
    public long count() {
        return storage.size();
    }
}
