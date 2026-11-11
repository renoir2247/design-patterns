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
  *ПО ЛАБОРАТОРНОЙ РАБОТЕ №10* \
  *дисциплины «Патерны проектирования»* \
  *Тема: Поведенческие паттерны: наблюдатель, состояние* \
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

Реализовать решение задачи из предметной области с использованием поведенческих паттернов «Наблюдатель» и «Состояние». Предметная область: приложение бронирования авиабилетов aviacassa (Java, Spring Boot).

= Краткие теоретические сведения

*Наблюдатель (Observer)* оповещает подписчиков об изменениях состояния объекта: издатель хранит список подписчиков и рассылает им события, не зная их конкретных типов.

*Состояние (State)* позволяет объекту менять поведение при изменении внутреннего состояния: поведение каждого статуса выносится в отдельный класс, а объект делегирует вызовы текущему состоянию.

= Ход работы

== Анализ кода приложения

Паттерн «Наблюдатель» применяется при обработке платежного вебхука: получение статуса PAID от внешней системы является событием, на которое реагируют сразу несколько частей подсистемы (бронирование переводится в CONFIRMED, выпускаются билеты, отправляется уведомление). Spring-инфраструктура (события приложения, транзакционные колбэки JPA) обеспечивает такую рассылку без прямых вызовов между сервисами.

Паттерн «Состояние» применяется в жизненном цикле бронирования: поведение брони определяется перечислением BookingStatus (PENDING, CONFIRMED, CANCELLED, EXPIRED). Для каждого статуса допустимы разные переходы: оплачена может быть только бронь в статусе PENDING, вебхук для уже подтвержденной брони игнорируется, истекший срок действует только для PENDING.

```java
if (booking.getStatus() != BookingStatus.PENDING) {
    throw new ValidationException("Оплата доступна только для бронирований в статусе PENDING");
}
...
if (booking.getStatus() != BookingStatus.PENDING) {
    throw new ValidationException("Бронирование не может быть подтверждено: неверный статус");
}
```

== Демонстрационная программа

Для демонстрации написана программа на Java. Издатель BookingEvents хранит подписчиков BookingListener и рассылает событие об оплате; подписчики TicketIssuer и NotificationSender реагируют независимо. Бронирование делегирует вызовы pay() и expire() текущему объекту-состоянию (PendingState, ConfirmedState, ExpiredState).

```java
class Booking {
    private BookingState state = new PendingState();
    private final String reference;

    public void pay() {
        state.pay(this);
    }

    public void expire() {
        state.expire(this);
    }

    void setState(BookingState state) {
        this.state = state;
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
```

Диаграмма классов приведена на рисунке 1.

#align(center)[
  #image("screens/uml.svg", width: 84%)
  Рисунок 1 - Диаграмма классов паттернов «Наблюдатель» и «Состояние»
]

В точке входа публикуется событие об оплате брони, затем проверяются переходы статусов: оплата, повторная оплата, попытка истечения и оплата после нее:

```java
public static void main(String[] args) {
    BookingEvents events = new BookingEvents();
    events.subscribe(new TicketIssuer());
    events.subscribe(new NotificationSender());
    events.publish("8E58F476B45E", "PAID");

    Booking booking = new Booking("8E58F476B45E");
    booking.pay();
    booking.pay();
    booking.expire();
    booking.pay();
}
```

Результат запуска программы показан на рисунке 2.

#align(center)[
  #image("screens/out_task1.png", width: 100%)
  Рисунок 2 - Результат выполнения программы
]

Результат: событие PAID получили оба подписчика (выпуск билетов и письмо). Бронь перешла PENDING в CONFIRMED, повторная оплата и истечение срока отклонены состоянием CONFIRMED, что подтверждает смену поведения при смене статуса.

= Вывод

Изучены поведенческие паттерны «Наблюдатель» и «Состояние». В aviacassa наблюдатель реализован в реакции подсистем на оплату, состояние: в жизненном цикле статусов бронирования. Работа паттернов подтверждена демонстрационной программой.
