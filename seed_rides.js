const fs = require('fs');
const BASE = 'https://samadhan-production.up.railway.app';

function readCsv(path) {
  return fs.readFileSync(path, 'utf8').trim().split('\n').filter(Boolean).map(l => l.split(','));
}

const vendors = readCsv('C:/Users/shiva/AppData/Local/Temp/seed/vendor_ids.csv').map(r => ({
  group: r[0], id: +r[1], name: r[2], contact: r[3], lat: r[4], lng: r[5]
}));
const vendorEmails = Object.fromEntries(readCsv('C:/Users/shiva/AppData/Local/Temp/seed/vendor_login.csv').map(r => [+r[0], r[1]]));
const vehicles = readCsv('C:/Users/shiva/AppData/Local/Temp/seed/vehicle_ids.csv').map(r => ({
  group: r[0], vendorId: +r[1], id: +r[2], number: r[3], vtype: r[4], vcat: r[5], lat: r[6], lng: r[7]
}));
const agents = readCsv('C:/Users/shiva/AppData/Local/Temp/seed/agent_ids.csv').map(r => ({
  group: r[0], vendorId: +r[1], id: +r[2], name: r[3]
}));

const vendorsByGroup = g => vendors.filter(v => v.group === g);
const vehiclesByVendor = vid => vehicles.filter(v => v.vendorId === vid);
const agentsByVendor = vid => agents.filter(a => a.vendorId === vid);

const results = [];
const log = (...a) => { console.log(...a); };

const sleep = ms => new Promise(r => setTimeout(r, ms));

// Several sequential requests per ride from one IP can trip the app's own 60-req/30s-per-IP
// rate limiter. Retrying a 429 after a short backoff instead of treating it as a real failure
// keeps this script's own throughput from being mistaken for the thing the limiter targets.
async function withRetry(doFetch) {
  for (let attempt = 0; attempt < 5; attempt++) {
    const res = await doFetch();
    if (res.status !== 429) return res;
    await sleep(3000 + attempt * 1000);
  }
  return doFetch();
}

async function postForm(path, fields) {
  return withRetry(async () => {
    const body = new URLSearchParams(fields);
    const res = await fetch(BASE + path, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body });
    const text = await res.text();
    let json; try { json = JSON.parse(text); } catch { json = text; }
    return { status: res.status, body: json };
  });
}

async function putForm(path, fields, token) {
  return withRetry(async () => {
    const qs = new URLSearchParams(fields).toString();
    const headers = {};
    if (token) headers['Authorization'] = 'Bearer ' + token;
    const res = await fetch(BASE + path + '?' + qs, { method: 'PUT', headers });
    const text = await res.text();
    let json; try { json = JSON.parse(text); } catch { json = text; }
    return { status: res.status, body: json };
  });
}

async function postFormAuth(path, fields, token) {
  return withRetry(async () => {
    const body = new URLSearchParams(fields);
    const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    const res = await fetch(BASE + path, { method: 'POST', headers, body });
    const text = await res.text();
    let json; try { json = JSON.parse(text); } catch { json = text; }
    return { status: res.status, body: json };
  });
}

async function getAuth(path, params, token) {
  return withRetry(async () => {
    const qs = new URLSearchParams(params).toString();
    const headers = {};
    if (token) headers['Authorization'] = 'Bearer ' + token;
    const res = await fetch(BASE + path + '?' + qs, { headers });
    const text = await res.text();
    let json; try { json = JSON.parse(text); } catch { json = text; }
    return { status: res.status, body: json };
  });
}

const vendorTokenCache = {};
async function vendorToken(vendorId) {
  if (vendorTokenCache[vendorId]) return vendorTokenCache[vendorId];
  const email = vendorEmails[vendorId];
  const password = email.split('@')[0];
  const r = await postForm('/v1/transfervendor-login', { UserName: email, password });
  const token = r.body && r.body.response && r.body.response.token;
  vendorTokenCache[vendorId] = token;
  return token;
}

function offset(lat, lng, kmE, kmN) {
  const dLat = kmN / 111.0;
  const dLng = kmE / (111.0 * Math.cos(lat * Math.PI / 180));
  return { lat: (lat + dLat).toFixed(6), lng: (lng + dLng).toFixed(6) };
}

function haversine(lat1, lng1, lat2, lng2) {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLng = (lng2 - lng1) * Math.PI / 180;
  const a = Math.sin(dLat/2)**2 + Math.cos(lat1*Math.PI/180)*Math.cos(lat2*Math.PI/180)*Math.sin(dLng/2)**2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
}

const HUB = { lat: 26.9124, lng: 75.7873 };
// Official bypass accounts (otp=1234, no real SMS) -- recreated fresh after the full DB wipe:
// userId=1 is "User" (914060test), userId=2 is "WebUser" (7233925077, was 4 before the wipe).
const customers = { User: [6], WebUser: [7] };

let rideCounter = 0;

async function createRide(opts) {
  rideCounter++;
  const pickup = offset(HUB.lat, HUB.lng, (rideCounter % 5) * 0.3, (rideCounter % 3) * 0.2);
  const drop = offset(+pickup.lat, +pickup.lng, opts.km, 0);
  const distanceKm = haversine(+pickup.lat, +pickup.lng, +drop.lat, +drop.lng);

  const fields = {
    rideCost: String(opts.rideCost || Math.round(distanceKm * 20)),
    gstCost: String(opts.gstCost || Math.round(distanceKm * 20 * 0.05)),
    pickuplatitude: pickup.lat, pickuplongitude: pickup.lng,
    source: `Test Pickup ${rideCounter}, Jaipur`,
    destination: `Test Drop ${rideCounter}, Jaipur rural`,
    destinationlatitude: drop.lat, destinationlongitude: drop.lng,
    userType: opts.userType,
    serviceType: opts.serviceType,
    distanceKm: String(distanceKm.toFixed(1)),
    pickupSchedule: opts.instantBooking === false ? '12:00 - 15:00' : 'Immediate',
    instantBooking: opts.instantBooking === false ? 'false' : 'true',
  };
  if (opts.instantBooking === false) {
    fields.pickupDate = new Date(Date.now() + 2 * 86400000).toISOString().slice(0, 10);
  }
  if (opts.userId) fields.userId = String(opts.userId);
  if (opts.vendorId) fields.vendorId = String(opts.vendorId);
  if (opts.parcelType) fields.parcelType = opts.parcelType;
  if (opts.vendorPickupVehicle) fields.vendorPickupVehicle = opts.vendorPickupVehicle;
  if (opts.vehicleCategory) fields.vehicleCategory = opts.vehicleCategory;
  if (opts.serviceType === 'HOMESHIFTING') {
    fields.homeType = '2BHK'; fields.packingType = 'Standard'; fields.goodsType = 'Household';
    fields.fromFloor = '2'; fields.liftAvailable = 'true';
  }
  if (opts.parcelType === 'Car') { fields.carNumber = 'RJ14TEST1'; fields.carModel = 'Hatchback'; }
  if (opts.parcelType === 'Bike') { fields.bikeNumber = 'RJ14BTEST1'; fields.bikeModel = 'Commuter'; }
  if (opts.parcelType === 'Package') { fields.packageWeight = '10'; fields.packageDescription = 'Test package'; }
  fields.receiverName = 'Test Receiver';
  fields.receiverContact = '9999999999';

  const r = await postForm('/transfer/requestRideTransfer', fields);
  return { ...r, distanceKm, meta: opts };
}

async function acceptAndRun(transferId, acceptingVendor, acceptingVehicle, creatorType, label) {
  const token = await vendorToken(acceptingVendor.id);
  if (!token) return { ok: false, step: 'login', label };

  const acc = await postFormAuth('/transfer/requestTransferApproval', {
    transferId: String(transferId), vendorId: String(acceptingVendor.id), transferApproval: '1',
    vehicleId: String(acceptingVehicle.id), acceptedBy: 'Vendor', serviceType: 'TRANSFERSERVICE', userType: creatorType
  }, token);
  if (acc.status !== 200) return { ok: false, step: 'accept', label, resp: acc.body };
  const startOtp = acc.body.startOtp;

  const start = await putForm('/transfer/requestTransferUpdate', {
    transferId: String(transferId), vehicleId: String(acceptingVehicle.id), rideStatusflag: '0', otp: String(startOtp), userType: creatorType
  }, token);
  if (start.status !== 200) return { ok: false, step: 'start', label, resp: start.body, accResp: acc.body };
  const closureOtp = start.body.closureotp;

  const close = await getAuth('/transfer/otpvalidate', {
    transferId: String(transferId), otp: String(closureOtp), flag: 'true', userType: creatorType
  }, token);

  return { ok: close.status === 200, step: 'complete', label, startOtp, closureOtp, closeResp: close.body };
}

async function getWallet(vendorId, token) {
  const { body } = await withRetry(async () => {
    const r = await fetch(`${BASE}/transferVendor/wallet-vendor/${vendorId}`, {
      headers: token ? { Authorization: 'Bearer ' + token } : {}
    });
    const j = await r.json().catch(() => null);
    return { status: r.status, body: j };
  });
  return body ? body.balance : null;
}

async function runAgentHandoffFlow(transferId, acceptingVendor, acceptingVehicle, agent, creatorType, label) {
  const token = await vendorToken(acceptingVendor.id);
  const acc = await postFormAuth('/transfer/requestTransferApproval', {
    transferId: String(transferId), vendorId: String(acceptingVendor.id), transferApproval: '1',
    acceptedBy: 'Vendor', serviceType: 'TRANSFERSERVICE', userType: creatorType
  }, token);
  if (acc.status !== 200) return { ok: false, step: 'accept(no-vehicle)', label, resp: acc.body };

  const assignAgent = await putForm('/transfer/requestTransferUpdate', {
    transferId: String(transferId), driverId: String(agent.id), userType: creatorType
  }, token);
  if (assignAgent.status !== 200) return { ok: false, step: 'assignAgent', label, resp: assignAgent.body };
  const handoverOtp = assignAgent.body.otp;

  const handover = await getAuth('/transfer/otpvalidate', {
    transferId: String(transferId), otp: String(handoverOtp), flag: 'false', userType: creatorType
  }, token);
  if (handover.status !== 200) return { ok: false, step: 'handover', label, resp: handover.body };

  const assignVehicle = await putForm('/transfer/requestTransferUpdate', {
    transferId: String(transferId), vehicleId: String(acceptingVehicle.id), userType: creatorType
  }, token);
  if (assignVehicle.status !== 200) return { ok: false, step: 'assignVehicle', label, resp: assignVehicle.body };
  const startOtp = assignVehicle.body.startOtp;

  const start = await putForm('/transfer/requestTransferUpdate', {
    transferId: String(transferId), vehicleId: String(acceptingVehicle.id), rideStatusflag: '0', otp: String(startOtp), userType: creatorType
  }, token);
  if (start.status !== 200) return { ok: false, step: 'start', label, resp: start.body };
  const closureOtp = start.body.closureotp;

  const close = await getAuth('/transfer/otpvalidate', {
    transferId: String(transferId), otp: String(closureOtp), flag: 'true', userType: creatorType
  }, token);
  return { ok: close.status === 200, step: 'complete(agent-handoff)', label, closeResp: close.body };
}

(async () => {
  const subVendors = vendorsByGroup('SUBSCRIBED');
  const nonSubVendors = vendorsByGroup('NONSUBSCRIBED');
  const indivVendors = vendorsByGroup('INDIVIDUAL');
  const allAcceptingPool = [...subVendors, ...nonSubVendors, ...indivVendors];

  const KM_TIERS = [15, 45, 90, 150];
  const SERVICE_COMBOS = [
    { serviceType: 'TRANSFERSERVICE', parcelType: 'Package', creators: ['User', 'WebUser'] },
    { serviceType: 'TRANSFERSERVICE', parcelType: 'Bike', creators: ['User', 'WebUser'] },
    { serviceType: 'TRANSFERSERVICE', parcelType: 'Car', creators: ['User', 'WebUser'] },
    { serviceType: 'BOOKVEHICLE', creators: ['User', 'WebUser', 'Vendor'], vendorPickupVehicle: 'TATA_ACE', vehicleCategory: 'SMALL_VEHICLE' },
    { serviceType: 'HOMESHIFTING', creators: ['User', 'WebUser', 'Vendor'] },
  ];

  let poolIdx = 0;
  const nextAcceptor = () => allAcceptingPool[(poolIdx++) % allAcceptingPool.length];

  const summary = [];

  for (const isInstant of [true, false]) {
  for (const combo of SERVICE_COMBOS) {
    for (const km of KM_TIERS) {
      for (const creatorType of combo.creators) {
        await sleep(1500);
        const acceptor = nextAcceptor();
        const vehiclesForAcceptor = vehiclesByVendor(acceptor.id);
        const vehicle = vehiclesForAcceptor[0];
        const label = `${combo.serviceType}${combo.parcelType ? '/' + combo.parcelType : ''}|${km}km|${isInstant ? 'instant' : 'scheduled'}|by=${creatorType}|acceptor=${acceptor.group}#${acceptor.id}`;

        let userId = null, vendorIdForCreate = null;
        if (creatorType === 'Vendor') {
          vendorIdForCreate = acceptor.id;
        } else {
          const pool = customers[creatorType];
          userId = pool[rideCounter % pool.length];
        }

        const createOpts = {
          serviceType: combo.serviceType, parcelType: combo.parcelType, km,
          userType: creatorType === 'Vendor' ? 'Vendor' : creatorType,
          userId, vendorId: vendorIdForCreate, instantBooking: isInstant,
          vendorPickupVehicle: combo.vendorPickupVehicle, vehicleCategory: combo.vehicleCategory,
        };

        try {
        const created = await createRide(createOpts);
        if (created.status !== 200 || !created.body || !created.body.id) {
          summary.push({ label, ok: false, phase: 'create', resp: created.body });
          log('CREATE FAIL', label, JSON.stringify(created.body).slice(0, 200));
          continue;
        }
        const transferId = created.body.id;
        const acceptorToken = await vendorToken(acceptor.id);
        const walletBefore = await getWallet(acceptor.id, acceptorToken);

        let outcome;
        if (creatorType === 'Vendor') {
          outcome = { ok: true, step: 'vendor-self-created', note: 'left as-is; vendor-created rides skip approval' };
        } else if (combo.serviceType === 'TRANSFERSERVICE' && km > 100) {
          const agent = agentsByVendor(acceptor.id)[0];
          if (!agent) {
            outcome = { ok: false, step: 'no-agent-available' };
          } else {
            outcome = await runAgentHandoffFlow(transferId, acceptor, vehicle, agent, creatorType, label);
          }
        } else {
          outcome = await acceptAndRun(transferId, acceptor, vehicle, creatorType, label);
        }

        const walletAfter = await getWallet(acceptor.id, acceptorToken);
        summary.push({
          label, transferId, ok: outcome.ok, phase: outcome.step,
          walletBefore, walletAfter, walletDelta: (walletBefore != null && walletAfter != null) ? +(walletAfter - walletBefore).toFixed(2) : null,
          detail: outcome.ok ? undefined : outcome
        });
        log((outcome.ok ? 'OK ' : 'FAIL'), label, 'id=' + transferId, 'walletDelta=', summary[summary.length-1].walletDelta);
        } catch (err) {
          summary.push({ label, ok: false, phase: 'exception', error: String(err) });
          log('EXCEPTION', label, String(err));
        }
      }
    }
  }
  }

  fs.writeFileSync('C:/Users/shiva/AppData/Local/Temp/seed/ride_results.json', JSON.stringify(summary, null, 2));
  const okCount = summary.filter(s => s.ok).length;
  log(`\n==== DONE: ${summary.length} rides attempted, ${okCount} fully OK, ${summary.length - okCount} had issues ====`);
})();
