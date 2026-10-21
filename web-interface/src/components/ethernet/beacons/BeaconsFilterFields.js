import {FILTER_TYPE} from "../../shared/filtering/Filters";

export const BEACONS_FILTER_FIELDS = {
  name: { title: "Name", type: FILTER_TYPE.STRING },
  description: { title: "Description", type: FILTER_TYPE.STRING },
  type: { title: "Type", type: FILTER_TYPE.BEACON_TYPE },
  classification: { title: "Classification", type: FILTER_TYPE.BEACON_CLASSIFICATION },
  destination_host: { title: "Destination Host", type: FILTER_TYPE.STRING },
  destination_address: { title: "Destination Address", type: FILTER_TYPE.IP_ADDRESS },
  destination_port: { title: "Destination Port", type: FILTER_TYPE.NUMERIC },
  destination_is_site_local: { title: "Destination is Site-Local", type: FILTER_TYPE.BOOLEAN },
  source_count: { title: "Device Count", type: FILTER_TYPE.NUMERIC },
  checkin_count: { title: "Check-In Count", type: FILTER_TYPE.NUMERIC },
  interval_seconds: { title: "Interval (Seconds)", type: FILTER_TYPE.NUMERIC }
}
