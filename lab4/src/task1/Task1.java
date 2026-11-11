// Задание 1. Демонстрация структурных паттернов «Компоновщик»
// и «Декоратор» на коде приложения бронирования авиабилетов (aviacassa).

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Task1 {
    public static void main(String[] args) {
        System.out.println("Компоновщик: состав бронирования и стоимость");
        BookingOrder order = new BookingOrder("8E58F476B45E");
        order.addComponent(new FlightFare());
        PassengerBlock first = new PassengerBlock("Иванов И.И.");
        first.addComponent(new BaggageOption());
        first.addComponent(new InsuranceOption());
        PassengerBlock second = new PassengerBlock("Петров П.П.");
        second.addComponent(new MealOption());
        order.addComponent(first);
        order.addComponent(second);
        order.print("  ");
        System.out.println("  ИТОГО: " + order.cost() + " руб.");

        System.out.println();
        System.out.println("Декоратор: сборка тарифа");
        FareComponent fare = new BasicFare();
        show(fare);
        fare = new BaggageDecorator(fare);
        show(fare);
        fare = new InsuranceDecorator(fare);
        show(fare);
    }

    private static void show(FareComponent fare) {
        System.out.printf("  %s = %s руб.%n", fare.describe(), fare.cost());
    }
}

// Паттерн «Компоновщик»: листы и группы стоимости реализуют один
// интерфейс, заказ считает итоговую сумму рекурсивно.
interface PriceComponent {
    String label();

    BigDecimal cost();

    void print(String indent);
}

class FlightFare implements PriceComponent {
    @Override
    public String label() {
        return "Тариф SU-1360 Ставрополь-Москва";
    }

    @Override
    public BigDecimal cost() {
        return new BigDecimal("7900.00");
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + label() + " = " + cost());
    }
}

class BaggageOption implements PriceComponent {
    @Override
    public String label() {
        return "Багаж 20 кг";
    }

    @Override
    public BigDecimal cost() {
        return new BigDecimal("2500.00");
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + label() + " = " + cost());
    }
}

class MealOption implements PriceComponent {
    @Override
    public String label() {
        return "Питание на борту";
    }

    @Override
    public BigDecimal cost() {
        return new BigDecimal("800.00");
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + label() + " = " + cost());
    }
}

class InsuranceOption implements PriceComponent {
    @Override
    public String label() {
        return "Страховка";
    }

    @Override
    public BigDecimal cost() {
        return new BigDecimal("1500.00");
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + label() + " = " + cost());
    }
}

class PassengerBlock implements PriceComponent {
    private final String passenger;
    private final List<PriceComponent> children = new ArrayList<>();

    public PassengerBlock(String passenger) {
        this.passenger = passenger;
    }

    public void addComponent(PriceComponent component) {
        children.add(component);
    }

    @Override
    public String label() {
        return "Пассажир " + passenger;
    }

    @Override
    public BigDecimal cost() {
        BigDecimal total = BigDecimal.ZERO;
        for (PriceComponent child : children) {
            total = total.add(child.cost());
        }
        return total;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + label() + " = " + cost());
        for (PriceComponent child : children) {
            child.print(indent + "    ");
        }
    }
}

class BookingOrder implements PriceComponent {
    private final String reference;
    private final List<PriceComponent> children = new ArrayList<>();

    public BookingOrder(String reference) {
        this.reference = reference;
    }

    public void addComponent(PriceComponent component) {
        children.add(component);
    }

    @Override
    public String label() {
        return "Бронирование " + reference;
    }

    @Override
    public BigDecimal cost() {
        BigDecimal total = BigDecimal.ZERO;
        for (PriceComponent child : children) {
            total = total.add(child.cost());
        }
        return total;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + label());
        for (PriceComponent child : children) {
            child.print(indent + "  ");
        }
    }
}

// Паттерн «Декоратор»: каждый декоратор оборачивает тариф,
// добавляя свою стоимость к стоимости обернутого объекта.
interface FareComponent {
    BigDecimal cost();

    String describe();
}

class BasicFare implements FareComponent {
    @Override
    public BigDecimal cost() {
        return new BigDecimal("15800.00");
    }

    @Override
    public String describe() {
        return "Эконом-тариф";
    }
}

abstract class FareDecorator implements FareComponent {
    protected final FareComponent inner;

    protected FareDecorator(FareComponent inner) {
        this.inner = inner;
    }
}

class BaggageDecorator extends FareDecorator {
    public BaggageDecorator(FareComponent inner) {
        super(inner);
    }

    @Override
    public BigDecimal cost() {
        return inner.cost().add(new BigDecimal("2500.00"));
    }

    @Override
    public String describe() {
        return inner.describe() + " + багаж 20 кг";
    }
}

class InsuranceDecorator extends FareDecorator {
    public InsuranceDecorator(FareComponent inner) {
        super(inner);
    }

    @Override
    public BigDecimal cost() {
        return inner.cost().add(new BigDecimal("1500.00"));
    }

    @Override
    public String describe() {
        return inner.describe() + " + страховка";
    }
}
