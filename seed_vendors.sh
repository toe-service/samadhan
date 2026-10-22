#!/bin/bash
BASE="https://samadhan-production.up.railway.app"
DOC=/tmp/seed/doc.txt
OUT=/tmp/seed/vendor_ids.csv
> "$OUT"

register_vendor() {
  local name="$1" email="$2" contact="$3" lat="$4" lng="$5" individual="$6" group="$7"
  resp=$(curl -s --max-time 30 -X POST "$BASE/transferVendor/register-vendor" \
    -F "vendorName=$name" \
    -F "vendorEmail=$email" \
    -F "vendorContactNumber=$contact" \
    -F "vendorCity=Jaipur" \
    -F "vendorAddress=Test Address, Jaipur" \
    -F "vendorLatitude=$lat" \
    -F "vendorLongitude=$lng" \
    -F "gst=22AAAAA0000A1Z5" \
    -F "services=TRANSFERSERVICE,BOOKVEHICLE,HOMESHIFTING" \
    -F "isIndividual=$individual" \
    -F "termsAccepted=true" \
    -F "termsVersion=v1" \
    -F "termsText=accepted" \
    -F "aadhaarFile=@$DOC" \
    -F "signatureFile=@$DOC")
  id=$(echo "$resp" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
  if [ -z "$id" ]; then
    echo "FAILED: $name -> $resp"
  else
    echo "$group,$id,$name,$contact,$lat,$lng" >> "$OUT"
    echo "OK: $name -> id=$id"
  fi
}

for i in $(seq 1 10); do
  lat=$(awk -v b=26.9124 -v i=$i 'BEGIN{print b + (i*0.01)}')
  lng=$(awk -v b=75.7873 -v i=$i 'BEGIN{print b + (i*0.01)}')
  register_vendor "TestSub Movers $i" "testsub$i.seed@example.com" "90000000$(printf '%02d' $i)" "$lat" "$lng" "false" "SUBSCRIBED"
done

for i in $(seq 1 10); do
  lat=$(awk -v b=26.9124 -v i=$i 'BEGIN{print b - (i*0.01)}')
  lng=$(awk -v b=75.7873 -v i=$i 'BEGIN{print b - (i*0.01)}')
  register_vendor "TestNonSub Movers $i" "testnonsub$i.seed@example.com" "90000001$(printf '%02d' $i)" "$lat" "$lng" "false" "NONSUBSCRIBED"
done

for i in $(seq 1 5); do
  lat=$(awk -v b=27.0124 -v i=$i 'BEGIN{print b + (i*0.01)}')
  lng=$(awk -v b=75.9873 -v i=$i 'BEGIN{print b + (i*0.01)}')
  register_vendor "TestIndividual $i" "testindiv$i.seed@example.com" "90000002$(printf '%02d' $i)" "$lat" "$lng" "true" "INDIVIDUAL"
done

echo "---- SUMMARY ----"
cat "$OUT"
