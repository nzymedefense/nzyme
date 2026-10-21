import ApiRoutes from "../../../util/ApiRoutes";

export const BEACONS_MENU_ITEMS = [
  {name: "Beacons", href: ApiRoutes.ETHERNET.BEACONS.INDEX},
  {name: "Exclusions", href: ApiRoutes.ETHERNET.BEACONS.EXCLUSIONS.INDEX},
  {name: "Settings", href: ApiRoutes.ETHERNET.BEACONS.SETTINGS, with_permission: "ethernet_monitoring_manage"},
]
