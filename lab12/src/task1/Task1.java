// Задание 1. Демонстрация архитектурного паттерна MVC
// на примере приложения бронирования aviacassa.

public class Task1 {
    public static void main(String[] args) {
        BookingModel model = new BookingModel("8E58F476B45E", "PENDING");
        BookingView view = new BookingView();
        BookingController controller = new BookingController(model, view);

        controller.displayBooking();
        controller.confirmPayment();
        controller.displayBooking();
    }
}

// Model: состояние бронирования и бизнес-правила переходов.
class BookingModel {
    private final String reference;
    private String status;

    public BookingModel(String reference, String status) {
        this.reference = reference;
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public String getStatus() {
        return status;
    }

    public void confirm() {
        if (!status.equals("PENDING")) {
            throw new IllegalStateException("Подтвердить можно только бронь в статусе PENDING");
        }
        status = "CONFIRMED";
    }
}

// View: отображение состояния модели.
class BookingView {
    public void render(String reference, String status) {
        System.out.println("  [VIEW] бронирование " + reference + ": статус " + status);
    }
}

// Controller: принимает действия пользователя, обновляет модель
// и организует обновление представления.
class BookingController {
    private final BookingModel model;
    private final BookingView view;

    public BookingController(BookingModel model, BookingView view) {
        this.model = model;
        this.view = view;
    }

    public void displayBooking() {
        view.render(model.getReference(), model.getStatus());
    }

    public void confirmPayment() {
        System.out.println("  [CONTROLLER] запрос: подтвердить оплату");
        model.confirm();
        System.out.println("  [CONTROLLER] модель обновлена");
    }
}
