#!/bin/bash
BASE="https://samadhan-production.up.railway.app"
VOUT=/tmp/seed/vehicle_ids.csv
AOUT=/tmp/seed/agent_ids.csv
> "$VOUT"
> "$AOUT"

TYPES=("TATA_ACE:SMALL_VEHICLE" "TRUCK_14FT:CLOSED_CONTAINER_TRUCK" "PICKUP_8FT:OPEN_BODY_TRUCK")

declare -A EMAIL
while IFS=, read -r vid email; do
  EMAIL[$vid]="$email"
done < /tmp/seed/vendor_login.csv

login_vendor() {
  local vendorId="$1" email="$2"
  local password="${email%%@*}"
  resp=$(curl -s --max-time 20 -X POST "$BASE/v1/transfervendor-login" --data-urlencode "UserName=$email" --data-urlencode "password=$password")
  echo "$resp" | grep -o '"token":"[^"]*"' | head -1 | sed 's/"token":"//;s/"$//'
}

create_vehicle() {
  local vendorId="$1" idx="$2" lat="$3" lng="$4" group="$5" token="$6"
  local pair="${TYPES[$((idx % 3))]}"
  local vtype="${pair%%:*}"
  local vcat="${pair##*:}"
  local vnum="TEST${vendorId}V${idx}"
  resp=$(curl -s --max-time 30 -X POST "$BASE/driver/register-Vehicle" \
    -H "Authorization: Bearer $token" \
    -F "vehicleNumber=$vnum" \
    -F "vehicleContactNumber=91$(printf '%08d' $((vendorId*10+idx)))" \
    -F "currentLocation=Jaipur" \
    -F "vendorVehicle=$vtype" \
    -F "vehicleCategory=$vcat" \
    -F "vehicleLatitude=$lat" \
    -F "vehicleLongitude=$lng" \
    -F "transferVendorId=$vendorId")
  id=$(echo "$resp" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
  if [ -z "$id" ]; then
    echo "VEHICLE FAILED vendor=$vendorId -> $resp"
  else
    echo "$group,$vendorId,$id,$vnum,$vtype,$vcat,$lat,$lng" >> "$VOUT"
  fi
}

create_agent() {
  local vendorId="$1" idx="$2" lat="$3" lng="$4" group="$5" token="$6"
  local contact="92$(printf '%08d' $((vendorId*10+idx)))"
  resp=$(curl -s --max-time 30 -X POST "$BASE/driver/register-Driver" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "{\"driverName\":\"Agent${vendorId}_${idx}\",\"driverContactNumber\":\"$contact\",\"driverEmail\":\"agent${vendorId}_${idx}@example.com\",\"driverCity\":\"Jaipur\",\"currentLocation\":\"Jaipur\",\"driverLatitude\":\"$lat\",\"driverLongitude\":\"$lng\",\"transferVendor\":{\"id\":$vendorId},\"isActive\":true}")
  id=$(echo "$resp" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
  if [ -z "$id" ]; then
    echo "AGENT FAILED vendor=$vendorId -> $resp"
  else
    echo "$group,$vendorId,$id,Agent${vendorId}_${idx}" >> "$AOUT"
  fi
}

while IFS=, read -r group vendorId name contact lat lng; do
  case "$group" in
    SUBSCRIBED) vcount=5; acount=3 ;;
    NONSUBSCRIBED) vcount=3; acount=3 ;;
    INDIVIDUAL) vcount=1; acount=0 ;;
    *) continue ;;
  esac
  email="${EMAIL[$vendorId]}"
  token=$(login_vendor "$vendorId" "$email")
  if [ -z "$token" ]; then
    echo "LOGIN FAILED vendor=$vendorId email=$email"
    continue
  fi
  for i in $(seq 1 $vcount); do
    create_vehicle "$vendorId" "$i" "$lat" "$lng" "$group" "$token"
  done
  for i in $(seq 1 $acount); do
    create_agent "$vendorId" "$i" "$lat" "$lng" "$group" "$token"
  done
  echo "done vendor $vendorId ($group): $vcount vehicles, $acount agents"
done < /tmp/seed/vendor_ids.csv

echo "---- VEHICLE COUNT ----"
wc -l "$VOUT"
echo "---- AGENT COUNT ----"
wc -l "$AOUT"
