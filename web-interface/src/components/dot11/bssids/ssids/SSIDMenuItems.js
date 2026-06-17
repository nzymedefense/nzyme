import ApiRoutes from "../../../../util/ApiRoutes";

export const SSID_MENU_ITEMS = (bssid, ssid, frequency) => [
  {name: "Details", href: ApiRoutes.DOT11.NETWORKS.SSIDS.DETAILS(bssid, ssid, frequency) },
  {name: "Timeline", href: ApiRoutes.DOT11.NETWORKS.SSIDS.TIMELINE(bssid, ssid, frequency) }
]