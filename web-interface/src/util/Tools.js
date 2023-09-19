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
  let channel;
  if(frequency >= 2412 && frequency <= 2484) {
    channel = (frequency - 2412) / 5 + 1;
  } else if(frequency >= 5170 && frequency <= 5825) {
    channel = (frequency - 5000) / 5;
  } else if(frequency >= 3000 && frequency <= 3100) {
    channel = (frequency - 3000) / 5;
  } else if(frequency >= 5950 && frequency <= 7125) {
    channel = (frequency - 5950) / 5 + 1;
  } else {
    channel = -1;
  }
  return channel;
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
};