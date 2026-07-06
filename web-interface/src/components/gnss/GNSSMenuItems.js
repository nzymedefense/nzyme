import ApiRoutes from "../../util/ApiRoutes";

export const GNSS_MENU_ITEMS = (tapId) => [
  {name: "Fix", href: ApiRoutes.GNSS.TAP_DETAILS.FIX(tapId)},
  {name: "RF", href: ApiRoutes.GNSS.TAP_DETAILS.RF(tapId)},
  {name: "Satellites", href: ApiRoutes.GNSS.TAP_DETAILS.SATELLITES(tapId)},
]