import {FILTER_TYPE} from "../../shared/filtering/Filters";

export const DISCONNECTED_CLIENT_FILTER_FIELDS = {
  client_mac: { title: "Client MAC Address", type: FILTER_TYPE.MAC_ADDRESS },
  probe_request: { title: "Probe Request SSID", type: FILTER_TYPE.STRING }
}