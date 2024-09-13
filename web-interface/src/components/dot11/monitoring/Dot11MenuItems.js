import ApiRoutes from "../../../util/ApiRoutes";

export const MONITORING_MENU_ITEMS = [
  {name: "Monitored Networks", href: ApiRoutes.DOT11.MONITORING.INDEX},
  {name: "Bandits", href: ApiRoutes.DOT11.MONITORING.BANDITS.INDEX},
  {name: "Probe Requests", href: ApiRoutes.DOT11.MONITORING.PROBE_REQUESTS.INDEX}
]