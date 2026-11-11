// Задание 1. Демонстрация порождающих паттернов «Одиночка» и «Строитель»
// на коде приложения бронирования авиабилетов (aviacassa).

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task1 {
    public static void main(String[] args) {
        JwtService first = JwtService.getInstance();
        JwtService second = JwtService.getInstance();
        System.out.println("Одиночка: первый экземпляр  = " + first.hashCode());
        System.out.println("Одиночка: второй экземпляр  = " + second.hashCode());
        System.out.println("Одиночка: один и тот же объект? " + (first == second));
        System.out.println("Одиночка: токен для user = " + first.generateToken("user"));

        Booking booking = new Booking.Builder()
                .flight("SU-1360 Stavropol-Moscow")
                .totalAmount(new BigDecimal("15800.00"))
                .addPassenger("Иванов И.И.")
                .addPassenger("Петров П.П.")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        System.out.println();
        System.out.println("Строитель: " + booking);
    }
}

// Паттерн «Одиночка»: сервис JWT-токенов существует в приложении
// в единственном экземпляре и предоставляет глобальную точку доступа.
class JwtService {
    private static volatile JwtService instance;
    private final String secretKey;

    private JwtService() {
        this.secretKey = "aviacassa-secret-key-2026";
    }

    public static JwtService getInstance() {
        JwtService result = instance;
        if (result == null) {
            synchronized (JwtService.class) {
                result = instance;
                if (result == null) {
                    instance = result = new JwtService();
                }
            }
        }
        return result;
    }

    public String generateToken(String username) {
        return username + ":" + secretKey.hashCode();
    }
}

// Паттерн «Строитель»: сложный объект бронирования собирается пошагово.
class Booking {
    private final String bookingReference;
    private final String flight;
    private final String status;
    private final BigDecimal totalAmount;
    private final LocalDateTime expiresAt;
    private final List<String> passengers = new ArrayList<>();

    private Booking(Builder builder) {
        this.bookingReference = builder.bookingReference;
        this.flight = builder.flight;
        this.status = builder.status;
        this.totalAmount = builder.totalAmount;
        this.expiresAt = builder.expiresAt;
        this.passengers.addAll(builder.passengers);
    }

    @Override
    public String toString() {
        return "Бронирование " + bookingReference
                + ": рейс " + flight
                + ", статус " + status
                + ", сумма " + totalAmount
                + ", пассажиры " + passengers;
    }

    public static class Builder {
        private String bookingReference;
        private String flight;
        private String status = "PENDING";
        private BigDecimal totalAmount;
        private LocalDateTime expiresAt;
        private final List<String> passengers = new ArrayList<>();

        public Builder bookingReference(String value) {
            this.bookingReference = value;
            return this;
        }

        public Builder flight(String value) {
            this.flight = value;
            return this;
        }

        public Builder totalAmount(BigDecimal value) {
            this.totalAmount = value;
            return this;
        }

        public Builder expiresAt(LocalDateTime value) {
            this.expiresAt = value;
            return this;
        }

        public Builder addPassenger(String name) {
            this.passengers.add(name);
            return this;
        }

        public Booking build() {
            if (bookingReference == null) {
                bookingReference = UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 12).toUpperCase();
            }
            if (flight == null) {
                throw new IllegalStateException("Рейс обязателен для бронирования");
            }
            if (totalAmount == null) {
                throw new IllegalStateException("Сумма обязательна для бронирования");
            }
            return new Booking(this);
        }
    }
}
