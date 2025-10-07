import {FILTER_TYPE} from "../../../shared/filtering/Filters";

export const SOCKS_FILTER_FIELDS = {
  session_key: { title: "Tunnel ID", type: FILTER_TYPE.STRING },
  client_address: { title: "Client IP Address", type: FILTER_TYPE.IP_ADDRESS },
  server_address: { title: "Tunnel Server IP Address", type: FILTER_TYPE.IP_ADDRESS },
  tunnel_destination_address: { title: "Tunnel Destination IP Address", type: FILTER_TYPE.IP_ADDRESS },
  type: { title: "Type", type: FILTER_TYPE.STRING },
  status: { title: "Status", type: FILTER_TYPE.STRING },
  tunneled_bytes: { title: "Tunneled Bytes", type: FILTER_TYPE.NUMERIC },
}