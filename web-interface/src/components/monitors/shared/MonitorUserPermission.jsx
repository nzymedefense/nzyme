import ApiRoutes from "../../../util/ApiRoutes";
import {userHasPermission} from "../../../util/Tools";

export default function requiredUserPermissionForMonitorWriteAccess(monitorType) {
  switch (monitorType) {
    case "DOT11_BSSID":
      return "dot11_monitoring_manage"
  }
}