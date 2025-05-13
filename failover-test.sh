#!/usr/bin/env bash
set -euo pipefail

FACADE_PORT=7070
LOG_PORTS=(8081 8082 8083)
MSG_PORTS=(8091 8092)

run_id=$(date +%s%N)        # унікальний префікс для цього прогону
echo "▶ run-id = $run_id"

# ---------- helper to post ----------
post () { curl -s -o /dev/null -X POST "$1" -H 'Content-Type:text/plain' -d "$2"; }

echo "↪︎ sending 10 initial logs…"
for i in {1..10}; do
  post "localhost:${FACADE_PORT}/logs"     "log-${run_id}-${i}"
done

echo "↪︎ sending 10 initial messages…"
for i in {1..10}; do
  post "localhost:${FACADE_PORT}/messages" "msg-${run_id}-${i}"
done

# ---------- kill one random replica of each service ----------
rand_log=${LOG_PORTS[$RANDOM % ${#LOG_PORTS[@]}]}
rand_msg=${MSG_PORTS[$RANDOM % ${#MSG_PORTS[@]}]}

echo "☠️  killing logging-service on port $rand_log"
pkill -f "logging-service.*--server.port=$rand_log" || true

echo "☠️  killing messages-service on port $rand_msg"
pkill -f "messages-service.*--server.port=$rand_msg" || true
  # дати Spring Cloud час оновити кеш
echo "⏳ waiting for Consul to deregister dead logging replica…"
for i in {1..20}; do          # ~20×1s = 20 секунд тайм-аут
  alive=$(curl -s "localhost:8500/v1/health/service/logging-service?passing" | jq 'length')
  [[ $alive -eq 2 ]] && break
  sleep 1
done
echo "↪︎ sending 10 more logs after failure…"
for i in {11..20}; do
  post "localhost:${FACADE_PORT}/logs"     "log-${run_id}-${i}"
done

echo "↪︎ sending 10 more messages after failure…"
for i in {11..20}; do
  post "localhost:${FACADE_PORT}/messages" "msg-${run_id}-${i}"
done

# ---------- aggregate + filter by run_id ----------
log_count=$(curl -s "localhost:${FACADE_PORT}/all_logs" |
            jq "[to_entries[] | select(.value | contains(\"$run_id\"))] | length")

msg_count=$(curl -s "localhost:${FACADE_PORT}/all" |
            jq ".messages | map(select(. | contains(\"$run_id\"))) | length")

echo "📊 logs for this run      : $log_count / expected 20"
echo "📊 messages for this run  : $msg_count / expected 20"

if [[ $log_count -eq 20 && $msg_count -eq 20 ]]; then
  echo "✅  FAILOVER TEST PASS"
  exit 0
else
  echo "❌  FAILOVER TEST FAIL"
  exit 1
fi

