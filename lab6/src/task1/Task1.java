// Задание 1. Демонстрация структурного паттерна «Заместитель»
// на коде приложения aviacassa.

import java.math.BigDecimal;

public class Task1 {
    public static void main(String[] args) {
        System.out.println("Защитный заместитель:");
        IBookingService user = new BookingServiceProxy("USER");
        user.confirm("8E58F476B45E");

        System.out.println();
        IBookingService admin = new BookingServiceProxy("ADMIN");
        admin.confirm("8E58F476B45E");

        System.out.println();
        System.out.println("Виртуальный заместитель:");
        IFlightLoader lazy = new FlightLazyProxy();
        System.out.println("  объект получен, запрос к БД еще не выполнен");
        System.out.println("  " + lazy.getFlightInfo());
    }
}

interface IBookingService {
    void confirm(String bookingReference);
}

class BookingServiceImpl implements IBookingService {
    @Override
    public void confirm(String bookingReference) {
        System.out.println("  бронирование " + bookingReference
                + " подтверждено, билеты выпущены");
    }
}

// Защитный заместитель: проверяет роль пользователя до вызова сервиса.
class BookingServiceProxy implements IBookingService {
    private final String role;
    private final BookingServiceImpl inner;

    public BookingServiceProxy(String role) {
        this.role = role;
        this.inner = new BookingServiceImpl();
    }

    @Override
    public void confirm(String bookingReference) {
        if (!role.equals("ADMIN")) {
            System.out.println("  доступ запрещен: роль " + role);
            return;
        }
        System.out.println("  проверка роли пройдена (ADMIN)");
        inner.confirm(bookingReference);
    }
}

interface IFlightLoader {
    String getFlightInfo();
}

class RealFlightLoader implements IFlightLoader {
    @Override
    public String getFlightInfo() {
        System.out.println("  (запрос к базе данных выполнен)");
        return "рейс SU-1360 Ставрополь-Москва, цена 7900.00 руб.";
    }
}

// Виртуальный заместитель: реальный объект создается при первом обращении.
class FlightLazyProxy implements IFlightLoader {
    private RealFlightLoader real;

    @Override
    public String getFlightInfo() {
        if (real == null) {
            System.out.println("  создание реального объекта...");
            real = new RealFlightLoader();
        }
        return real.getFlightInfo();
    }
}
