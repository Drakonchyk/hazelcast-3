# hazelcast-3

### **Project Overview**

This repository showcases a microservices-based system in **Java Spring Boot** leveraging a **Hazelcast** distributed map to store messages. The system consists of:

1. **Config-Service** – provides a REST endpoint listing the available addresses (IP/port) of other microservices (e.g., `logging-service`).
2. **Logging-Service** – stores messages in a **Hazelcast** distributed map. Multiple instances can run simultaneously, forming a Hazelcast cluster.
3. **Facade-Service** – receives HTTP requests from the client, then randomly selects one `logging-service` instance to forward requests for storing or retrieving messages.

### **Key Features**
- **Distributed Storage**: Messages are stored in a Hazelcast distributed map, ensuring high availability when multiple `logging-service` instances are running.
- **Service Discovery**: `facade-service` consults `config-service` to discover available `logging-service` addresses dynamically.
- **Scalability**: Multiple copies of `logging-service` can be launched, each embedding a Hazelcast node.
- **Load Balancing**: `facade-service` randomly selects a `logging-service` instance for each request. If one instance is offline, the service picks another.

### **System Architecture**

```
             +---------------+
             |config-service |
             |(REST: /services/logging-service
             +---------------+
                    ^
                    | (Gets IP/port list)
                    |
+-------------+      |       +---------------+
| facade      |---->|       | logging-service (port 8081)
| service     |              |   + embedded Hazelcast node
| (port 8080) |<----(random)------ logging-service (port 8082)
+-------------+              \---- logging-service (port 8083)
```

1. **config-service**: Returns an array of `{"host":"localhost","port":8081}`, etc.  
2. **facade-service**:  
   - Receives HTTP `POST/GET` from the client.  
   - Calls `config-service` to obtain the list of `logging-service` endpoints.  
   - Randomly picks one endpoint to forward the request.  
3. **logging-service**:  
   - Uses **Hazelcast** to store data in a distributed map.  
   - Each instance runs on a different port (e.g., 8081, 8082, 8083).  
   - The embedded Hazelcast nodes automatically form a cluster, replicating messages across instances.

### **Requirements**
- **Java 17+**  
- **Maven** 3.x  
- **Spring Boot** 3.x (already declared in the `pom.xml` files)  

### **Building**

In the root folder (where the parent `pom.xml` is located):
```bash
mvn clean install
```
This command will compile and package all modules.

### **Usage**

1. **Start `config-service`** (e.g., port `8000`):
   ```bash
   cd config-service
   mvn spring-boot:run
   ```
   Verify:
   ```bash
   curl http://localhost:8000/services/logging-service
   ```
   Should return a JSON array with IP/port of logging-service instances.

2. **Launch multiple copies** of `logging-service` (e.g., ports 8081, 8082, 8083). Each uses embedded Hazelcast:
   ```bash
   cd ../logging-service
   # 1st instance on port 8081
   mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

   # 2nd instance on port 8082
   mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"

   # 3rd instance on port 8083
   mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
   ```

3. **Start `facade-service`**:
   ```bash
   cd ../facade-service
   mvn spring-boot:run
   ```
   Now it is ready to handle HTTP requests from the client.

4. **Test**:
   - **Send messages** (POST):  
     ```bash
     curl -X POST -H "Content-Type: application/json" \
          -d '{"msg":"Hello Hazelcast!"}' \
          http://localhost:8080/facade/messages
     ```
   - **Retrieve messages** (GET):  
     ```bash
     curl http://localhost:8080/facade/messages
     ```
     Will return a JSON object/array of all stored messages.

5. **Resilience check**: Stop 1-2 of the `logging-service` instances. Repeat the GET request. You should still get all messages via the remaining instance(s).


