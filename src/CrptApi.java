import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final Semaphore semaphore;
    private final HttpClient httpClient;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        this.httpClient = HttpClients.createDefault();

        startIntervalUpdater(timeUnit.toMillis(1));
    }

    private void startIntervalUpdater(long intervalMillis) {
        Thread updaterThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(intervalMillis);
                    semaphore.release(semaphore.availablePermits()); // Обновляем счетчик разрешений
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        updaterThread.setDaemon(true);
        updaterThread.start();
    }

    public void createDocument(String documentJson, String signature) {
        try {
            semaphore.acquire();

            String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";
            HttpPost request = new HttpPost(apiUrl);

            // Настройка заголовков
            request.setHeader("Content-Type", "application/json");
            // Пример для Basic Authorization
            // request.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("username:password".getBytes()));

            // Настройка тела запроса
            StringEntity entity = new StringEntity(documentJson);
            request.setEntity(entity);

            // Выполнение запроса
            HttpResponse response = httpClient.execute(request);

            // Обработка ответа
            // int statusCode = response.getStatusLine().getStatusCode();
            // String responseBody = EntityUtils.toString(response.getEntity());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }
}
