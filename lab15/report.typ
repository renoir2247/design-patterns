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
  *ПО ЛАБОРАТОРНОЙ РАБОТЕ №15* \
  *дисциплины «Патерны проектирования»* \
  *Тема: Идиоматические паттерны (пул потоков, Future)* \
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

Реализовать решение задачи из предметной области с использованием идиоматических паттернов «Пул потоков» и «Future». Предметная область: обработка платежей приложения бронирования авиабилетов aviacassa (Java, Spring Boot).

= Краткие теоретические сведения

*Пул потоков (Thread Pool)* содержит фиксированное множество рабочих потоков, которые забирают задачи из очереди: создание потоков выполняется один раз, а перегрузка контролируется размером пула. *Future* представляет результат асинхронной задачи: метод get() блокирует вызывающий поток до готовности результата.

= Ход работы

== Анализ кода приложения

Место применения: пул потоков встроен в саму платформу. Tomcat обрабатывает HTTP-запросы приложения рабочими потоками из пула, а аннотация \@Transactional обеспечивает атомарность каждой задачи. Обработка вебхуков платежной системы (processWebhook) выполняется в таком потоке; при всплеске платежей задачи выстраиваются в очередь пула, что защищает приложение от перегрузки.

```java
@Override
@Transactional
public void processWebhook(String transactionId, PaymentStatus status) {
    Payment payment = paymentRepository
            .findByTransactionId(transactionId)
            .orElseThrow(...);
    payment.setStatus(status);
    ...
}
```

== Демонстрационная программа

Для демонстрации написана программа на Java. Пул из трех потоков создается через Executors.newFixedThreadPool(3); задачи PaymentTask (Callable) ставятся методом submit(), который возвращает Future. Метод get() дожидается результата каждой задачи.

```java
ExecutorService pool = Executors.newFixedThreadPool(3);
List<Future<String>> results = new ArrayList<>();
for (int i = 1; i <= 4; i++) {
    String txId = "TX-" + i;
    results.add(pool.submit(new PaymentTask(txId)));
}
for (Future<String> future : results) {
    System.out.println("  " + future.get());
}
pool.shutdown();
```

Схема взаимодействия приведена на рисунке 1.

#align(center)[
  #image("screens/uml.svg", width: 84%)
  Рисунок 1 - Схема паттернов «Пул потоков» и «Future»
]

В точке входа четыре платежа ставятся в пул из трех потоков, затем собираются результаты:

```java
public static void main(String[] args) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(3);
    List<Future<String>> results = new ArrayList<>();
    for (int i = 1; i <= 4; i++) {
        String txId = "TX-" + i;
        results.add(pool.submit(new PaymentTask(txId)));
        System.out.println("  " + txId + " принят в обработку");
    }
    for (Future<String> future : results) {
        System.out.println("  " + future.get());
    }
    pool.shutdown();
}
```

Результат запуска программы показан на рисунке 2.

#align(center)[
  #image("screens/out_task1.png", width: 100%)
  Рисунок 2 - Результат выполнения программы
]

Результат: четыре платежа приняты мгновенно; первые три обрабатывались параллельно в потоках 1-3, четвертый дождался освобождения потока 1. Все Future вернули результат PAID, что подтверждает работу пула и сбор результатов.

= Вывод

Изучены идиоматические паттерны «Пул потоков» и «Future». В aviacassa пул потоков встроен в Tomcat и обслуживает HTTP-запросы и вебхуки; демонстрация показала ограничение параллелизма размером пула и сбор результатов через Future.
