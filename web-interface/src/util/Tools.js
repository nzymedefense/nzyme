import numeral from 'numeral'

export function byteAverageToMbit (byte) {
  const mbit = byte * 8 / 10 / 1024 / 1024

  if (mbit > 1000) {
    return numeral(mbit / 1024).format('0.0') + ' Gbit/sec'
  } else {
    return numeral(mbit).format('0.0') + ' Mbit/sec'
  }
}

export function dot11FrequencyToChannel(frequency) {
  let c;
  if (frequency === 2484) {
    c = 14;
  } else if (frequency === 5935) {
    /* see 802.11ax D6.1 27.3.23.2 and Annex E */
    c = 2;
  } else if (frequency < 2484) {
    c = (frequency - 2407) / 5;
  } else if (frequency >= 4910 && frequency <= 4980) {
    c = (frequency - 4000) / 5;
  } else if (frequency < 5950) {
    c = (frequency - 5000) / 5;
  } else if (frequency <= 7115) {
    c = (frequency - 5950) / 5;
  } else {
    c = -1;
  }

  return c;
}

export function singleTapSelected(selectedTaps) {
  return selectedTaps !== "*" && (Array.isArray(selectedTaps) && selectedTaps.length === 1)
}

export function userHasPermission(user, permission) {
  return user.is_orgadmin
      || user.is_superadmin
      || user.feature_permissions.includes(permission)
}

export function truncate(str, n, useWordBoundary){
  if (str.length <= n) { return str; }
  const subString = str.slice(0, n-1); // the original check
  return (useWordBoundary ? subString.slice(0, subString.lastIndexOf(" ")) : subString) + "…";
}

export function isValidMACAddress(mac) {
  if (!mac || mac.trim() === "") {
    return false;
  }

  return /^[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}$/.test(mac.trim());
}

export function sanitizeHtml(string) {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#x27;',
    "/": '&#x2F;',
  };
  const reg = /[&<>"'/]/ig;
  return string.replace(reg, (match) => (map[match]));
}

export function arraysAreEqual(a, b) {
  if (a.length !== b.length) return false

  a.sort()
  b.sort()

  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) return false
  }

  return true
}