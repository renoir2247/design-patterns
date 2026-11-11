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
  *ПО ЛАБОРАТОРНОЙ РАБОТЕ №13* \
  *дисциплины «Патерны проектирования»* \
  *Тема: Паттерны микросервисов (Circuit Breaker)* \
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

Реализовать решение задачи из предметной области с использованием паттерна распределенных систем Circuit Breaker. Предметная область: интеграция приложения бронирования авиабилетов aviacassa с внешним платежным шлюзом.

= Краткие теоретические сведения

*Circuit Breaker (размыкатель)* защищает сервис от каскадных сбоев при недоступности внешней зависимости: после серии подряд идущих ошибок вызовы временно отклоняются сразу (OPEN), не тратя время на таймауты; после паузы размыкатель пробует пропустить вызовы снова.

= Ход работы

== Анализ кода приложения

Место применения: PaymentServiceImpl обращается к внешней платежной системе через вебхуки и ожидает статусы PAID или FAILED. При недоступности шлюза платеж зависает в PENDING, а потоки томкат-пула занимаются ожиданием таймаутов. Размыкатель между сервисом и шлюзом отклоняет вызовы на время сбоя и возвращает понятную ошибку клиенту.

```java
@Override
@Transactional
public void processWebhook(String transactionId, PaymentStatus status) {
    Payment payment = paymentRepository
            .findByTransactionId(transactionId)
            .orElseThrow(...);
    if (status == PaymentStatus.FAILED) {
        payment.setStatus(PaymentStatus.FAILED);
        throw new PaymentFailedException("Платеж не прошёл");
    }
    ...
}
```

== Демонстрационная программа

Для демонстрации написана программа на Java. Размыкатель CircuitBreaker оборачивает шлюз: в состоянии CLOSED вызовы пропускаются и считаются сбои; после двух подряд сбоев размыкатель размыкается (OPEN) и отклоняет вызовы без обращения к шлюзу; после паузы сбрасывается в CLOSED.

```java
public String charge(String transactionId) {
    if (open) {
        return "отклонено размыкателем (состояние OPEN)";
    }
    try {
        String result = inner.charge(transactionId);
        failures = 0;
        return result;
    } catch (IllegalStateException e) {
        failures++;
        if (failures >= failureThreshold) {
            open = true;
            System.out.println("  размыкатель разомкнут (OPEN)");
        }
        return "ошибка: " + e.getMessage();
    }
}
```

Схема работы размыкателя приведена на рисунке 1.

#align(center)[
  #image("screens/uml.svg", width: 84%)
  Рисунок 1 - Схема паттерна Circuit Breaker
]

В точке входа выполняется серия из семи вызовов шлюза: два падают, размыкатель размыкается, вызовы 3-6 отклоняются мгновенно, после восстановления вызов 7 проходит:

```java
public static void main(String[] args) {
    PaymentGateway gateway = new UnreliablePaymentGateway();
    CircuitBreaker breaker = new CircuitBreaker(gateway, 2);
    for (int i = 1; i <= 6; i++) {
        System.out.println("вызов " + i + ": " + breaker.charge("TX-" + i));
    }
    breaker.reset();
    System.out.println("вызов 7: " + breaker.charge("TX-7"));
}
```

Результат запуска программы показан на рисунке 2.

#align(center)[
  #image("screens/out_task1.png", width: 100%)
  Рисунок 2 - Результат выполнения программы
]

Результат: после двух сбоев размыкатель перешел в OPEN, вызовы 3-6 отклонены мгновенно без обращения к шлюзу; после сброса вызов 7 успешно прошел (PAID).

= Вывод

Изучен паттерн распределенных систем Circuit Breaker. Он применяется на границе aviacassa с внешним платежным шлюзом: защищает сервис от ожидания таймаутов и каскадных сбоев. Работа паттерна подтверждена демонстрационной программой.
