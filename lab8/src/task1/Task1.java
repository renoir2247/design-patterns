// Задание 1. Демонстрация поведенческого паттерна «Цепочка обязанностей»
// на коде приложения aviacassa.

public class Task1 {
    public static void main(String[] args) {
        BookingRequest ok = new BookingRequest(2, 50);
        BookingRequest tooMany = new BookingRequest(12, 50);
        BookingRequest noSeats = new BookingRequest(2, 1);

        ValidationChain chain = new ValidationChain();
        System.out.println("запрос 1 (2 места, свободно 50):");
        System.out.println("  итог: " + chain.handle(ok));
        System.out.println("запрос 2 (12 мест, свободно 50):");
        System.out.println("  итог: " + chain.handle(tooMany));
        System.out.println("запрос 3 (2 места, свободно 1):");
        System.out.println("  итог: " + chain.handle(noSeats));
    }
}

class BookingRequest {
    final int passengers;
    final int availableSeats;

    public BookingRequest(int passengers, int availableSeats) {
        this.passengers = passengers;
        this.availableSeats = availableSeats;
    }
}

// Паттерн «Цепочка обязанностей»: запрос последовательно проходит
// обработчиков, каждый решает, отклонить его или передать дальше.
abstract class ValidationHandler {
    private ValidationHandler next;

    public ValidationHandler linkWith(ValidationHandler next) {
        this.next = next;
        return next;
    }

    public String handle(BookingRequest request) {
        String error = check(request);
        if (error != null) {
            return error;
        }
        return next != null ? next.handle(request) : "запрос принят, бронирование создано";
    }

    protected abstract String check(BookingRequest request);
}

class PassengersNotEmptyHandler extends ValidationHandler {
    @Override
    protected String check(BookingRequest request) {
        if (request.passengers <= 0) {
            return "ошибка 400: список пассажиров не может быть пустым";
        }
        return null;
    }
}

class MaxPassengersHandler extends ValidationHandler {
    @Override
    protected String check(BookingRequest request) {
        if (request.passengers > 9) {
            return "ошибка 400: максимальное количество пассажиров 9";
        }
        return null;
    }
}

class SeatsAvailableHandler extends ValidationHandler {
    @Override
    protected String check(BookingRequest request) {
        if (request.availableSeats < request.passengers) {
            return "ошибка 409: недостаточно свободных мест на рейсе";
        }
        return null;
    }
}

class ValidationChain {
    private final ValidationHandler first;

    public ValidationChain() {
        ValidationHandler second = new PassengersNotEmptyHandler();
        ValidationHandler third = second.linkWith(new MaxPassengersHandler());
        third.linkWith(new SeatsAvailableHandler());
        this.first = second;
    }

    public String handle(BookingRequest request) {
        return first.handle(request);
    }
}
