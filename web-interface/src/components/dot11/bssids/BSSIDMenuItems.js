import ApiRoutes from "../../../util/ApiRoutes";

export const BSSID_MENU_ITEMS = (bssid) => [
  {name: "Details", href: ApiRoutes.DOT11.NETWORKS.BSSIDS.DETAILS(bssid) },
  {name: "Timeline", href: ApiRoutes.DOT11.NETWORKS.BSSIDS.TIMELIME(bssid) }
]