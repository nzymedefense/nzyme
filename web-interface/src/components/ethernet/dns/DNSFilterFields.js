import {FILTER_TYPE} from "../../shared/filtering/Filters";

export const DNS_FILTER_FIELDS = {
  query_value: { title: "Query Value", type: FILTER_TYPE.STRING },
  query_type: { title: "Query Type", type: FILTER_TYPE.DNS_TYPE },
  query_etld: { title: "Query eTLD", type: FILTER_TYPE.STRING },
  response_value: { title: "Response Value", type: FILTER_TYPE.STRING },
  response_type: { title: "Response Type", type: FILTER_TYPE.DNS_TYPE },
  response_etld: { title: "Response eTLD", type: FILTER_TYPE.STRING },
  client_address: { title: "Client Address", type: FILTER_TYPE.IP_ADDRESS },
  server_address: { title: "Server Address", type: FILTER_TYPE.IP_ADDRESS },
  server_port: { title: "Server Port", type: FILTER_TYPE.PORT_NUMBER }
}