import numeral from 'numeral'
import moment from "moment/moment";
import React from "react";

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

export function userHasSubsystem(user, subsystem) {
  return user.subsystems.includes(subsystem)
}

export function truncate(str, n, useWordBoundary) {
  if (!str) {
    return null;
  }

  if (str.length <= n) {
    return str;
  }

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
  if (!Array.isArray(a) || !Array.isArray(b)) {
    return false;
  }
  
  if (a == null || b == null) {
    return a == null && b == null // Both NULL.
  }

  if (a.length !== b.length) return false

  a.sort()
  b.sort()

  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) return false
  }

  return true
}

export function calculateConnectionDuration(connectionStatus, establishedAt, terminatedAt, mostRecentSegmentTime) {
  let endTime;
  if (connectionStatus === "Inactive") {
    if (!terminatedAt) {
      // Timed out. Never recorded a termination time.
      return "n/a (Last Segment: " + moment(mostRecentSegmentTime).format() + ")";
    }

    endTime = moment(terminatedAt);
  } else {
    endTime = moment(new Date());
  }

  const duration = moment.duration(endTime.diff(establishedAt));

  if (duration.asSeconds() > 60) {
    if (duration.asMinutes() > 60) {
      return numeral(duration.asHours()).format("0,0.0") + " Hours";
    } else {
      return numeral(duration.asMinutes()).format("0,0.0") + " Minutes";
    }
  } else {
    return numeral(duration.asSeconds()).format("0,0.0") + " Seconds";
  }
}

export function formatDurationMs(ms) {
  const totalSeconds = Math.floor(ms / 1000);

  const days = Math.floor(totalSeconds / 86400); // 24 * 3600
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  const milliseconds = Math.floor(ms % 1000);

  const parts = [];
  if (days) parts.push(`${days}d`);
  if (hours) parts.push(`${hours}h`);
  if (minutes) parts.push(`${minutes}m`);
  if (seconds) parts.push(`${seconds}s`);
  if (milliseconds && parts.length === 0) parts.push(`${milliseconds}ms`);

  return parts.join(' ') || '0ms';
}

export function convertGenericChartData(data) {
  const result = {}

  Object.keys(data).sort().forEach(function(key) {
    result[key] = data[key];
  })

  return result
}

export function humanReadableDatabaseCategoryName(category) {
  switch (category) {
    case "DOT11":
      return "802.11/WiFi"
    case "BLUETOOTH":
      return "Bluetooth"
    case "ETHERNET_DNS":
      return "Ethernet: DNS"
    case "ETHERNET_L4":
      return "Ethernet: Layer 4"
  }

  return category;
}

export function capitalizeFirstLetter(val) {
  return String(val).charAt(0).toUpperCase() + String(val).slice(1);
}

export function capitalizeFirstLetterAndLowercase(val) {
  return val.charAt(0).toUpperCase() + val.slice(1).toLowerCase();
}

export function metersToFeet(x) {
  return x / 0.3048;
}

export function uavHorizontalAccuracyNoHtml(x) {
  switch (x) {
    case 0:
      return ">11mi"
    case 1:
      return "<11mi"
    case 2:
      return "<4.6mi"
    case 3:
      return "<2.3mi"
    case 4:
      return "<1.1mi"
    case 5:
      return "<0.6mi"
    case 6:
      return "<0.3mi"
    case 7:
      return "<600ft"
    case 8:
      return "<300ft"
    case 9:
      return "<100ft"
    case 10:
      return "<33ft"
    case 11:
      return "<10ft"
    case 12:
      return "<3ft"
    default:
      return "Unknown Accuracy"
  }
}

export function uavVerticalAccuracyNoHtml(x) {
  switch (x) {
    case 0:
      return ">492ft"
    case 1:
      return "<492ft"
    case 2:
      return "<148ft"
    case 3:
      return "<82ft"
    case 4:
      return "<33ft"
    case 5:
      return "<10ft"
    case 6:
      return "<3ft"
    default:
      return "Unknown Accuracy"
  }
}