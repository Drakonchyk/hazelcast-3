#!/usr/bin/env bash
set -eu

##############################################################################
# 0.  housekeeping – kill any leftovers from previous runs
##############################################################################
echo "⏹  Cleaning existing containers (ignore errors)…"
docker rm -f consul zookeeper kafka1 kafka2 kafka3 2>/dev/null || true

pkill -f "logging-service.*--server.port" 2>/dev/null || true
pkill -f "messages-service.*--server.port" 2>/dev/null || true
pkill -f "facade-service.*--server.port"  2>/dev/null || true
sleep 2


##############################################################################
# 1.  build all jars
##############################################################################
echo "🔨  Packaging micro-services…"
mvn -q clean package -DskipTests


##############################################################################
# 2.  start infra (Consul + ZooKeeper + 3 Kafka brokers)
##############################################################################
echo "🚀  Starting Consul + Kafka…"
docker run -d --name consul \
  -p 8500:8500 -p 8600:8600/udp \
  hashicorp/consul:1.19 agent -dev -client=0.0.0.0

docker run -d --name zookeeper \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  confluentinc/cp-zookeeper:7.6.0

for id in {1..3}; do
  docker run -d --name kafka${id} --link zookeeper \
    -e KAFKA_BROKER_ID=${id} \
    -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
    -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka${id}:2909${id} \
    -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
    -p 2909${id}:2909${id} \
    confluentinc/cp-kafka:7.6.0
done

echo "⌛  Waiting 20 s for brokers to settle…"
sleep 20


##############################################################################
# 3.  create Kafka topic (idempotent)
##############################################################################

##############################################################################
# 4.  seed Consul KV
##############################################################################
echo "🔑  Seeding Consul KV…"
consul kv put config/application/kafka.bootstrap-servers \
  "localhost:29091,localhost:29092,localhost:29093"
consul kv put config/logging-service/hazelcast.tcp.members "localhost"


##############################################################################
# 5.  launch micro-services (all in background)
##############################################################################
echo "▶️  Booting services…"

# logging-service replicas 8081/8082/8083
java -jar logging-service/target/logging-service-*.jar --server.port=8081 &
java -jar logging-service/target/logging-service-*.jar --server.port=8082 &
java -jar logging-service/target/logging-service-*.jar --server.port=8083 &

# messages-service replicas 8091/8092
java -jar messages-service/target/messages-service-*.jar --server.port=8091 &
java -jar messages-service/target/messages-service-*.jar --server.port=8092 &

# single facade-service 7070
java -jar facade-service/target/facade-service-*.jar  --server.port=7070 &

echo "⌛  Waiting 15 s for Spring Boot contexts…"
sleep 15


##############################################################################
# 6.  tail every replica in its own terminal tab / window (optional)
##############################################################################
if command -v osascript >/dev/null; then           # macOS – open new Terminal tabs
  echo "🔍  Opening log tails (macOS Terminal)…"
  for p in 8081 8082 8083 8091 8092; do
    osascript -e "tell app \"Terminal\" to do script \"tail -f -n +1 *${p}.log || cat < /dev/null\""
  done
fi

##############################################################################
# 7.  functional test – send 20 logs + 20 messages
##############################################################################
echo "🧪  Sending 20 log entries through façade…"
for i in {1..20}; do
  curl -s -X POST localhost:7070/logs \
       -H 'Content-Type: application/json' \
       -d "{\"message\":\"log-$i\"}" > /dev/null
done

echo "🧪  Sending 20 messages through façade…"
for i in {1..20}; do
  curl -s -X POST localhost:7070/messages \
       -H 'Content-Type: application/json' \
       -d "{\"msg\":\"msg-$i\"}" > /dev/null
done

##############################################################################
# 8.  show distribution results
##############################################################################
echo
echo "📊  Fetch aggregated logs:"
curl -s localhost:7070/logs | jq .

echo
echo "📊  Fetch aggregated messages:"
curl -s localhost:7070/messages | jq .
echo
echo "📊  Fetch *all* logs across replicas:"
curl -s localhost:7070/all_logs | jq length

echo
echo "📊  Fetch combined object:"
curl -s localhost:7070/all | jq '.logs|length, .messages|length'

cat <<'EOF'

=====================================================================
Look at the console output of ports 8081-8083 and 8091-8092.
You should see requests land on replicas in roughly round-robin order,
proving Spring Cloud LoadBalancer is distributing them dynamically.

Open http://localhost:8500 in a browser:
  • There should be 1×facade-service, 3×logging-service, 2×messages-service.
Kill one replica (Ctrl-C in its window) and hit the façade again —
traffic keeps flowing to the healthy ones.
=====================================================================
EOF

