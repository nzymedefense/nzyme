export function monitorTypeToSubsystem(monitorType) {
  if (!monitorType) {
    return null;
  }

  if (monitorType.startsWith("DOT11_")) {
    return "DOT11";
  }

  if (monitorType.startsWith("BLUETOOTH_")) {
    return "BLUETOOTH";
  }

  if (monitorType.startsWith("ETHERNET_")) {
    return "ETHERNET";
  }

  return null;
}

export default function requiredUserPermissionForMonitorWriteAccess(monitorType) {
  switch (monitorTypeToSubsystem(monitorType)) {
    case "DOT11":
      return "dot11_monitoring_manage"
    case "BLUETOOTH":
      return "bluetooth_monitoring_manage"
    case "ETHERNET":
      return "ethernet_monitoring_manage"
  }
}
