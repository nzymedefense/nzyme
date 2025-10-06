import {FILTER_TYPE} from "../../../shared/filtering/Filters";

export const SSH_FILTER_FIELDS = {
  session_key: { title: "Session ID", type: FILTER_TYPE.STRING },
  client_address: { title: "Client IP Address", type: FILTER_TYPE.IP_ADDRESS },
  server_address: { title: "Server IP Address", type: FILTER_TYPE.IP_ADDRESS },
  connection_status: { title: "Status", type: FILTER_TYPE.STRING },
  tunneled_bytes: { title: "Tunneled Bytes", type: FILTER_TYPE.NUMERIC }
}