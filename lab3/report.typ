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
  *ПО ЛАБОРАТОРНОЙ РАБОТЕ №3* \
  *дисциплины «Патерны проектирования»* \
  *Тема: Структурные паттерны: адаптер, мост* \
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

Реализовать решение задачи из предметной области с использованием структурных паттернов «Адаптер» и «Мост». Предметная область: приложение бронирования авиабилетов aviacassa (Java, Spring Boot).

= Краткие теоретические сведения

*Адаптер (Adapter)* позволяет объектам с несовместимыми интерфейсами работать вместе: адаптер реализует требуемый интерфейс и внутри себя переадресует вызовы методам существующего класса, переводя данные из одного формата в другой.

*Мост (Bridge)* разделяет одну большую систему на два независимых дерева классов: абстракцию (что делается) и реализацию (как делается). Абстракция хранит ссылку на интерфейс реализации, и обе иерархии могут изменяться независимо, а конкретная реализация подставляется во время выполнения.

= Ход работы

== Анализ кода приложения

Паттерн «Адаптер» применяется в интеграции со Spring Security: класс CustomUserDetailsService реализует интерфейс UserDetailsService и адаптирует JPA-сущность User к типу UserDetails, переводя вызов loadUserByUsername() в обращение к репозиторию.

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь не найден: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
```

Адаптер применяется и при интеграции с внешней платежной системой. Внешний шлюз возвращает идентификатор транзакции в собственном формате (строка EXT-...), а внутреннему коду нужен объект PaymentResult. Адаптер реализует интерфейс PaymentGateway и скрывает протокол внешней системы.

Паттерн «Мост» применяется в подсистеме уведомлений. Тип уведомления (о бронировании, о платеже) независим от канала доставки (email, SMS): абстракция Notification хранит ссылку на интерфейс MessageSender, уведомления и отправители подставляются независимо.

== Демонстрационная программа

Для демонстрации написана программа на Java. Адаптер ExternalPaymentApiAdapter оборачивает внешнюю платежную систему и приводит ее ответы к интерфейсу PaymentGateway. Мост составлен из двух иерархий: уведомления и отправители; связь задается конструктором.

```java
class ExternalPaymentApiAdapter implements PaymentGateway {
    private final ExternalPaymentApi api;

    public ExternalPaymentApiAdapter(ExternalPaymentApi api) {
        this.api = api;
    }

    @Override
    public PaymentResult pay(String cardNumber, BigDecimal amount) {
        String raw = api.makeTransaction(amount.doubleValue(), cardNumber);
        return new PaymentResult(raw.startsWith("EXT-"), raw);
    }
}

abstract class Notification {
    protected final MessageSender sender;

    protected Notification(MessageSender sender) {
        this.sender = sender;
    }

    public void send(String recipient) {
        String answer = sender.deliver(recipient, buildText());
        System.out.println("  отправлено: " + answer);
    }

    protected abstract String buildText();
}
```

Диаграмма классов демонстрационной программы приведена на рисунке 1.

#align(center)[
  #image("screens/uml.svg", width: 88%)
  Рисунок 1 - Диаграмма классов паттернов «Адаптер» и «Мост»
]

В точке входа программы клиент работает с внешней платежной системой только через адаптер, а уведомления собираются из любого сочетания типа и канала доставки:

```java
public static void main(String[] args) {
    System.out.println("Адаптер:");
    PaymentGateway gateway =
            new ExternalPaymentApiAdapter(new ExternalPaymentApi());
    PaymentResult result =
            gateway.pay("4111111111111111", new BigDecimal("15800.00"));
    System.out.println("  результат: " + result);

    System.out.println();
    System.out.println("Мост:");
    Notification bookingEmail = new BookingNotification(new EmailSender());
    Notification paymentSms = new PaymentNotification(new SmsSender());
    bookingEmail.send("user@example.com");
    paymentSms.send("+79001234567");
}
```

Результат запуска программы показан на рисунке 2.

#align(center)[
  #image("screens/out_task1.png", width: 100%)
  Рисунок 2 - Результат выполнения программы
]

Результат: адаптер переадресовал вызов pay() внешнему API и вернул PaymentResult с success=true (EXT-1111:1580000). Мост собрал уведомления из разных сочетаний иерархий: бронирование по email, платеж по SMS.

= Вывод

Изучены структурные паттерны «Адаптер» и «Мост». В aviacassa адаптер применяется в интеграции со Spring Security (CustomUserDetailsService), мост в подсистеме уведомлений отделяет тип уведомления от канала доставки. Работа паттернов подтверждена демонстрационной программой.
