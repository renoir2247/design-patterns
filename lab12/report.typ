#import "@local/modern-g7-32:0.2.0": gost

#set page(
  paper: "a4",
  margin: (top: 2cm, bottom: 2cm, left: 3cm, right: 1.5cm)
)

#set text(
  font: "Times New Roman",
  size: 14pt
)

#show raw: set text(font: ("JetBrainsMono NF", "Menlo"), size: 10pt)
#show raw.where(block: true): it => block(
  width: 100%,
  breakable: false,
  fill: luma(248),
  stroke: 0.5pt + luma(180),
  inset: 10pt,
  radius: 3pt,
  it,
)

#align(center)[
  Министерство науки и высшего образования Российской Федерации  
  Федеральное государственное автономное образовательное учреждение  
  высшего образования \
  «СЕВЕРО-КАВКАЗСКИЙ ФЕДЕРАЛЬНЫЙ УНИВЕРСИТЕТ»
]

#v(1.2cm)

#align(center)[
  Департамент цифровых, робототехнических  
  систем и электроники \
  Институт перспективной инженерии
]

#v(2cm)

#align(center)[
  *ОТЧЕТ* \
  *ПО ЛАБОРАТОРНОЙ РАБОТЕ №12* \
  *дисциплины «Патерны проектирования»* \
  *Тема: Архитектурные паттерны (MVC)* \
]

#v(2cm)

#show grid: set text(size: 12pt)
#grid(
  columns: (1fr, 1fr),

  align(left)[],

  align(left)[
    Выполнил: \
    Чекалин Егор Юрьевич \
    4 курс, группа ПИЖ-б-о-23-2 \
    09.03.04 «Программная инженерия», \
    очная форма обучения \

    #v(0.5em)
    #line(length: 100%)
    #v(-0.7em)
    #align(center)[(подпись)]

    Работу проверил: \
    Николаев Е.И.

    #v(0.5em)
    #line(length: 100%)
    #v(-0.7em)
    #align(center)[(подпись)]
  ]
)


#v(2cm)

#align(left)[
  Отчет защищен с оценкой #box(width: 3cm, height: 1em, stroke: (bottom: 0.5pt))[]
  Дата защиты #box(width: 3cm, height: 1em, stroke: (bottom: 0.5pt))[]
]

#align(center)[
  \
  Ставрополь, 2026 г.
]

#pagebreak()

#show: gost.with(
  hide-title: true,
  add-pagebreaks: false,
  city: "Ставрополь",
  year: 2026,
)

= Цель работы

Реализовать решение задачи из предметной области с использованием архитектурного паттерна MVC (Model-View-Controller). Предметная область: приложение бронирования авиабилетов aviacassa (Java, Spring Boot).

= Краткие теоретические сведения

MVC разделяет приложение на три части: *Model* хранит состояние и бизнес-правила, *View* отображает данные модели, *Controller* принимает действия пользователя, обновляет модель и организует обновление представления.

= Ход работы

== Анализ кода приложения

Приложение aviacassa построено по MVC на стеке Spring: роль Controller выполняют классы BookingController, FlightController, PaymentController (аннотации \@RestController, \@RequestMapping), роль Model: сервисы и JPA-сущности с бизнес-правилами (Booking, BookingServiceImpl), роль View: JSON-ответы DTO и клиентское React-приложение frontend, которое рендерит данные модели.

```java
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final IBookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDTO> create(
            \@RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request));
    }
}
```

== Демонстрационная программа

Для демонстрации написана консольная программа на Java. BookingModel хранит бронирование и правило перехода статуса, BookingView выводит состояние, BookingController принимает действия и связывает модель с представлением.

```java
class BookingController {
    private final BookingModel model;
    private final BookingView view;

    public void displayBooking() {
        view.render(model.getReference(), model.getStatus());
    }

    public void confirmPayment() {
        System.out.println("  [CONTROLLER] запрос: подтвердить оплату");
        model.confirm();
        System.out.println("  [CONTROLLER] модель обновлена");
    }
}
```

Схема разделения ответственности приведена на рисунке 1.

#align(center)[
  #image("screens/uml.svg", width: 84%)
  Рисунок 1 - Схема паттерна MVC
]

В точке входа контроллер дважды отображает бронирование, между которыми подтверждает оплату:

```java
public static void main(String[] args) {
    BookingModel model = new BookingModel("8E58F476B45E", "PENDING");
    BookingView view = new BookingView();
    BookingController controller = new BookingController(model, view);
    controller.displayBooking();
    controller.confirmPayment();
    controller.displayBooking();
}
```

Результат запуска программы показан на рисунке 2.

#align(center)[
  #image("screens/out_task1.png", width: 100%)
  Рисунок 2 - Результат выполнения программы
]

Результат: представление показало бронь в статусе PENDING, контроллер обработал действие подтверждения оплаты, модель применила бизнес-правило и перешла в CONFIRMED, повторный вывод представления отразил новое состояние.

= Вывод

Изучен архитектурный паттерн MVC. Приложение aviacassa разделено на контроллеры, модель (сервисы и JPA-сущности) и представление (JSON DTO и React-клиент); консольная демонстрация подтвердила разделение ответственности слоев.
