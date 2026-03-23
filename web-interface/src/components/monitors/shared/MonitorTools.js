import {BSSID_FILTER_FIELDS} from "../../dot11/bssids/BssidFilterFields";

export default function monitorTypeToFilterFields(type) {
  switch (type) {
    case "DOT11_BSSID":
      return BSSID_FILTER_FIELDS
  }
}