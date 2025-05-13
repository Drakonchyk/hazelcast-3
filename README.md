# Micro-services Demo (Spring Boot 3 + Spring Cloud Consul + Kafka + Hazelcast)

Лабораторний проєкт демонструє:

* **Service Discovery & Config** через HashiCorp Consul  
* **Повідомлення** через Apache Kafka  
* **Розподілений кеш/сховище** через Hazelcast  
* **Відмовостійкість** — автоматичний reroute запитів при падінні інстанса

## Сервіси

| Модуль | Порт(и) за замовч. | Призначення |
|--------|-------------------|-------------|
| `facade-service`   | 7070 | API-шлюз: приймає REST-запити клієнтів, розподіляє їх на backend-сервіси через Spring Cloud LoadBalancer |
| `logging-service`  | 8081-8083 | Приймає `/logs` та зберігає їх у мапі Hazelcast (*log-cluster*) |
| `messages-service` | 8091-8092 | Читає з Kafka тему `message-topic`, віддає накопичені повідомлення на `/messages` |

## Швидкий запуск

```bash
./run-demo.sh
````

Скрипт виконує:

1. **Maven build** усіх модулів
2. Старт **Consul** та **3× Kafka broker** (Docker)
3. Посів ключів у KV (`kafka.bootstrap-servers`, `hazelcast.tcp.members`)
4. Запуск 1× facade, 3× logging, 2× messages
5. Надсилання 20 логів і 20 повідомлень → перевірка, що все працює

## Перевірка вручну

```bash
# створити лог
curl -X POST localhost:7070/logs     -d 'hello'

# створити повідомлення
curl -X POST localhost:7070/messages -d 'hi'

# вибрати тільки свої записи (prefixed)
curl localhost:7070/all_logs
curl localhost:7070/messages
curl localhost:7070/all          # об’єднана відповідь { logs:…, messages:… }
```

## Тест відмовостійкості

```bash
./failover-test.sh          # потрібні jq та curl
```

Скрипт:

* генерує унікальний `run-id`;
* надсилає 10 логів + 10 повідомлень;
* kill-ає випадковий `logging-service` та `messages-service`;
* чекає, доки Consul викине мертвий інстанс із кешу;
* надсилає ще 10 + 10;
* рахує лише записи з цим `run-id`; очікувано 20 / 20.

## Конфігурація Consul

```
config/application/kafka.bootstrap-servers     localhost:29092,…
config/logging-service/hazelcast.tcp.members    localhost:5701,5702,5703
```

Можна редагувати через **UI** ([http://localhost:8500](http://localhost:8500)) або CLI:

```bash
consul kv put config/application/kafka.bootstrap-servers "kafka1:29092,kafka2:29093"
consul kv get -recurse config/
```

## Налаштування сервісів

У кожному `application.properties` лише базові рядки:

```properties
spring.application.name=logging-service
server.port=8081        # інший порт у кожній копії через аргумент
spring.config.import=optional:consul:
spring.cloud.consul.host=localhost
spring.cloud.consul.port=8500
```

Інстанси реєструються автоматично (`spring-cloud-starter-consul-discovery`).

## Зменшення TTL кешу LoadBalancer (опційно)

```properties
spring.cloud.loadbalancer.cache.enabled=true
spring.cloud.loadbalancer.cache.ttl=2s
```

Так фасад швидше «забуває» про мертві репліки.

---

### Папка /scripts

* `run-demo.sh` — повний цикл build → deploy → smoke-test
* `failover-test.sh` — автоматичний тест відмовостійкості з унікальним префіксом

