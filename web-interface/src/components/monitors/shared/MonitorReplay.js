import ApiRoutes from "../../../util/ApiRoutes";

export default function monitorTypeToSearchLink(monitor) {
  switch (monitor.type) {
    case "DOT11_BSSID":
      return ApiRoutes.DOT11.NETWORKS.BSSIDS + "?monitor=" + monitor.uuid;
  }
}