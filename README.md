## Distributed Microservices Example

This project demonstrates a Java-based microservices architecture featuring:

* **Config Service** — central registry of service instances
* **Logging Service** — stores logs in a Hazelcast distributed map
* **Messages Service** — consumes messages from a Kafka topic and stores in local memory
* **Facade Service** — client‐facing gateway that dispatches requests randomly to available instances

It covers two labs:

1. **Lab 3** — Hazelcast Distributed Map for logging
2. **Lab 4** — Kafka message queue for inter‐service messaging and failure resilience

---

### 📁 Repo Structure

```
/.
├── config-service
│   └── src/main/java/...        # Spring Boot app, /services endpoints
│   └── src/main/resources/application.properties
├── logging-service
│   └── src/main/java/...        # Spring Boot app, HazelcastConfig + /logs endpoints
│   └── src/main/resources/
├── messages-service
│   └── src/main/java/...        # Spring Boot app, KafkaConsumer + /messages endpoint
│   └── src/main/resources/application.properties
├── facade-service
│   └── src/main/java/...        # Spring Boot app, /facade endpoints
│   └── src/main/resources/application.properties
├── docker-compose.yml           # Zookeeper + 3× Kafka brokers
└── pom.xml                      # Parent multi-module POM
```

---

## 🚀 Prerequisites

* JDK 17+
* Maven
* Docker & Docker Compose
* `jq` (optional, for JSON pretty-print)

---

## 🛠️ Setup & Run

### 1. Start Kafka cluster

```bash
# from project root
docker compose up -d
```

This brings up:

* Zookeeper (2181)
* 3 Kafka brokers, exposed on 29092, 29093, 29094

Verify:

```bash
docker ps
docker exec -it kafka1 \
  kafka-topics --bootstrap-server localhost:29092 \
    --describe --topic message-topic || \
  kafka-topics --bootstrap-server localhost:29092 \
    --create --topic message-topic \
    --partitions 2 --replication-factor 2
```

### 2. Build the project

```bash
mvn clean install -DskipTests
```

### 3. Run microservices

Open separate terminals (or use background jobs):

```bash
# Config Service (port 8000)
mvn -pl config-service spring-boot:run

# Logging Service ×3 (ports 8081, 8082, 8083)
mvn -pl logging-service spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=8081" &
mvn -pl logging-service spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=8082" &
mvn -pl logging-service spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=8083" &

# Messages Service ×2 (ports 8091, 8092)
mvn -pl messages-service spring-boot:run \
  -Dspring-boot.run.arguments="--PORT=8091" &
mvn -pl messages-service spring-boot:run \
  -Dspring-boot.run.arguments="--PORT=8092" &

# Facade Service (port 7070)
mvn -pl facade-service spring-boot:run
```

---

## 🔍 Endpoints

### Config Service (8000)

* `GET /services/logging-service` → list of `{ host, port }`
* `GET /services/messages-service` → list of `{ host, port }`

### Logging Service (`/logs`)

* `POST /logs`  `{ "msg": "..." }` → store in Hazelcast
* `GET  /logs`  → returns `Map<id, msg>`

### Messages Service (`/messages`)

* `GET  /messages` → returns stored `List<String>` from in-memory queue

*(service listens on a port and consumes Kafka topic `message-topic` automatically)*

### Facade Service (`/facade`)

* **Logs**

  * `POST /facade/logs`    → proxy to a random logging-service
  * `GET  /facade/logs`    → read from a random logging-service
* **Messages**

  * `POST /facade/messages`→ send `msg` to Kafka
  * `GET  /facade/messages`→ fetch from a random messages-service
* **Combined**

  * `GET /facade/all` → JSON `{ logs: {...}, messages: [...] }`

---

## 🧪 Testing

1. **Basic Logging**

   ```bash
   for i in {1..10}; do
     curl -s -X POST -H "Content-Type:application/json" \
          -d "{\"msg\":\"msg$i\"}" \
          http://localhost:7070/facade/logs
   done

   curl -s http://localhost:7070/facade/logs | jq
   ```

2. **Basic Messaging**

   ```bash
   for i in {1..10}; do
     curl -s -X POST -H "Content-Type:application/json" \
          -d "{\"msg\":\"msg$i\"}" \
          http://localhost:7070/facade/messages
   done

   # verify messages-service consoles show “Consumed: msg#”
   curl -s http://localhost:7070/facade/messages | jq
   ```

3. **Combined View**

   ```bash
   curl -s http://localhost:7070/facade/all | jq
   ```

4. **Kafka Failover**

   ```bash
   # stop leader broker (e.g. kafka1)
   docker stop kafka1
   sleep 10

   # continue sending
   for i in {11..20}; do
     curl -s -X POST -H "Content-Type:application/json" \
          -d "{\"msg\":\"msg$i\"}" \
          http://localhost:7070/facade/messages
   done

   # messages-service should auto-consume msg11…msg20
   curl -s http://localhost:7070/facade/messages | jq
   ```

---

## ⚙️ Configuration

Each service reads its ports and bootstrap addresses from
`src/main/resources/application.properties` or CLI args:

```properties
# Example for messages-service:
spring.kafka.bootstrap-servers=localhost:29092,localhost:29093,localhost:29094
server.port=${PORT:8091}
spring.kafka.consumer.group-id=message-group-${server.port}
```

Adjust as needed for your environment.

---

## 📚 Further Reading

* [Spring Boot Official Docs](https://spring.io/projects/spring-boot)
* [Hazelcast Documentation](https://docs.hazelcast.org/)
* [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
* [Kafka Quickstart](https://kafka.apache.org/quickstart)

---

**Enjoy exploring distributed maps & message queues in microservices!**
