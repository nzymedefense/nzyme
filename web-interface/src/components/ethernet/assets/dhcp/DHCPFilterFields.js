import {FILTER_TYPE} from "../../../shared/filtering/Filters";

export const DHCP_FILTER_FIELDS = {
  transaction_type: { title: "Transaction Type", type: FILTER_TYPE.STRING },
  client_mac: { title: "Client MAC Address", type: FILTER_TYPE.MAC_ADDRESS },
  server_mac: { title: "Server MAC Address", type: FILTER_TYPE.MAC_ADDRESS },
  requested_ip: { title: "Requested IP Address", type: FILTER_TYPE.IP_ADDRESS },
  fingerprint: { title: "Fingerprint", type: FILTER_TYPE.STRING }
}