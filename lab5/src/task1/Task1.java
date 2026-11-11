// Задание 1. Демонстрация структурных паттернов «Фасад»
// и «Приспособленец» на коде приложения aviacassa.

import java.util.HashMap;
import java.util.Map;

public class Task1 {
    public static void main(String[] args) {
        System.out.println("Фасад:");
        BookingFacade facade = new BookingFacade();
        facade.book("SU-1360", new String[]{"Иванов И.И.", "Петров П.П."});

        System.out.println();
        System.out.println("Приспособленец:");
        for (int i = 0; i < 2; i++) {
            ServiceClass economy = TicketFactory.get("ECONOMY");
            System.out.println("  билет " + i + ": класс " + economy.getServiceClass()
                    + ", объект " + economy.hashCode());
        }
        ServiceClass business = TicketFactory.get("BUSINESS");
        System.out.println("  бизнес-класс: объект " + business.hashCode());
    }
}

// Паттерн «Фасад»: единая точка бронирования скрывает взаимодействие
// поиска рейса, резервирования мест и создания платежа.
class FlightSearchService {
    public String find(String flightNumber) {
        return "рейс " + flightNumber + " найден";
    }
}

class SeatReservationService {
    public String reserve(int seats) {
        return "зарезервировано мест: " + seats;
    }
}

class PaymentService {
    public String init(int seats) {
        return "платеж создан на " + seats + " пассажира(ов)";
    }
}

class BookingFacade {
    private final FlightSearchService search = new FlightSearchService();
    private final SeatReservationService seats = new SeatReservationService();
    private final PaymentService payment = new PaymentService();

    public void book(String flightNumber, String[] passengers) {
        System.out.println("  " + search.find(flightNumber));
        System.out.println("  " + seats.reserve(passengers.length));
        System.out.println("  " + payment.init(passengers.length));
        System.out.println("  бронирование создано со статусом PENDING");
    }
}

// Паттерн «Приспособленец»: общие неизменяемые объекты тарифов
// разделяются между всеми билетами вместо дублирования.
class ServiceClass {
    private final String name;
    private final String baggage;

    public ServiceClass(String name, String baggage) {
        this.name = name;
        this.baggage = baggage;
    }

    public String getServiceClass() {
        return name + " (багаж: " + baggage + ")";
    }
}

class TicketFactory {
    private static final Map<String, ServiceClass> pool = new HashMap<>();

    public static ServiceClass get(String name) {
        pool.computeIfAbsent(name, n -> new ServiceClass(n, n.equals("BUSINESS") ? "40 кг" : "20 кг"));
        System.out.println("  выдан приспособленец из пула, всего объектов: " + pool.size());
        return pool.get(name);
    }
}
