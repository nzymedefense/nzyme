import {FILTER_TYPE} from "../../shared/filtering/Filters";

export const BEACON_CHECKINS_FILTER_FIELDS = {
  source_address: { title: "Client Address", type: FILTER_TYPE.IP_ADDRESS },
  source_mac: { title: "Client MAC", type: FILTER_TYPE.MAC_ADDRESS },
  destination_address: { title: "Destination Address", type: FILTER_TYPE.IP_ADDRESS },
  destination_mac: { title: "Destination MAC", type: FILTER_TYPE.MAC_ADDRESS },
  bytes_total: { title: "Size (Bytes)", type: FILTER_TYPE.NUMERIC },
  session_count: { title: "Parallel Connections", type: FILTER_TYPE.NUMERIC }
}
