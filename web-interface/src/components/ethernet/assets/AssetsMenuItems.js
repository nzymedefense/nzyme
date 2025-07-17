import ApiRoutes from "../../../util/ApiRoutes";

export const ASSETS_MENU_ITEMS = [
  {name: "Asset List", href: ApiRoutes.ETHERNET.ASSETS.INDEX},
  {name: "DHCP", href: ApiRoutes.ETHERNET.ASSETS.DHCP.INDEX},
  {name: "ARP", href: ApiRoutes.ETHERNET.ASSETS.ARP.INDEX},
]