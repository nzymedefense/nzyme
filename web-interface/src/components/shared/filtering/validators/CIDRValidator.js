export default function validateCIDRValid(value) {
  if (!value) return false;
  const cidr = value.trim();
  if (cidr.includes(".")) return isValidIPv4CIDR(cidr);
  if (cidr.includes(":")) return isValidIPv6CIDR(cidr);
  return false;
}

function isValidIPv4CIDR(cidr) {
  const m = /^(\d{1,3}(?:\.\d{1,3}){3})\/([0-9]|[12]\d|3[0-2])$/.exec(cidr);
  if (!m) return false;

  const ipInt = ipv4ToInt(m[1]);
  if (ipInt == null) return false;

  const prefix = Number(m[2]);
  const mask = prefix === 0 ? 0 : (~0 >>> 0) << (32 - prefix) >>> 0;
  const network = ipInt & mask;

  return ipInt === network;
}

function ipv4ToInt(ip) {
  const octets = ip.split('.').map(Number);
  if (octets.length !== 4) return null;
  for (const o of octets) if (o < 0 || o > 255) return null;
  return ((octets[0] << 24) >>> 0) | (octets[1] << 16) | (octets[2] << 8) | octets[3];
}

function isValidIPv6CIDR(cidr) {
  const m = /^(.+)\/(12[0-8]|[0-9]{1,2})$/i.exec(cidr);
  if (!m) return false;

  const ip = m[1];
  const prefix = Number(m[2]);

  const int = ipv6ToBigInt(ip);
  if (int == null) return false;

  const FULL = (1n << 128n) - 1n;
  if (prefix === 0) {
    return int === 0n;
  }
  const mask = FULL ^ ((1n << (128n - BigInt(prefix))) - 1n); // top 'prefix' bits set
  const network = int & mask;

  return int === network;
}

function ipv6ToBigInt(ip) {
  let head = ip, v4Tail = null;

  if (ip.includes('.')) {
    const lastColon = ip.lastIndexOf(':');
    if (lastColon === -1) return null;
    head = ip.slice(0, lastColon);
    const v4 = ip.slice(lastColon + 1);
    const v4Int = ipv4ToInt(v4);
    if (v4Int == null) return null;
    const hi = (v4Int >>> 16) & 0xffff;
    const lo = v4Int & 0xffff;
    v4Tail = [hi.toString(16), lo.toString(16)];
  }

  const parts = head.split('::');
  if (parts.length > 2) return null;

  let left = parts[0] ? parts[0].split(':') : [];
  let right = parts.length === 2 && parts[1] ? parts[1].split(':') : [];

  left = left.filter(s => s.length > 0);
  right = right.filter(s => s.length > 0);

  if (v4Tail) right = right.concat(v4Tail);

  const missing = 8 - (left.length + right.length);
  if (missing < 0) return null;

  const hextets = [
    ...left,
    ...Array(missing).fill('0'),
    ...right
  ];

  if (hextets.length !== 8) return null;

  let acc = 0n;
  for (const h of hextets) {
    if (!/^[0-9a-f]{1,4}$/i.test(h)) return null;
    const v = BigInt(parseInt(h, 16));
    acc = (acc << 16n) | v;
  }
  return acc;
}