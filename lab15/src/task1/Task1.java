// Задание 1. Демонстрация идиоматических паттернов «Пул потоков»
// и «Future» на примере обработки платежей приложения aviacassa.

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Task1 {
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        List<Future<String>> results = new ArrayList<>();

        System.out.println("постановка 4 платежей в пул из 3 потоков:");
        for (int i = 1; i <= 4; i++) {
            String txId = "TX-" + i;
            Future<String> future = pool.submit(new PaymentTask(txId));
            results.add(future);
            System.out.println("  " + txId + " принят в обработку");
        }

        System.out.println("результаты:");
        for (Future<String> future : results) {
            System.out.println("  " + future.get());
        }
        pool.shutdown();
    }
}

// Задача обработки платежа, выполняется потоком из пула.
class PaymentTask implements Callable<String> {
    private final String transactionId;

    public PaymentTask(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String call() {
        String thread = Thread.currentThread().getName();
        System.out.println("  [" + thread + "] обработка " + transactionId);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return transactionId + " -> PAID (" + thread + ")";
    }
}
