import {FILTER_TYPE} from "../../../shared/filtering/Filters";

export const BEACON_EXCLUSIONS_FILTER_FIELDS = {
  name: { title: "Name", type: FILTER_TYPE.STRING },
  description: { title: "Description", type: FILTER_TYPE.STRING },
  enabled: { title: "Enabled", type: FILTER_TYPE.BOOLEAN },
  destination_host: { title: "Destination Host", type: FILTER_TYPE.STRING },
  destination_cidr: { title: "Destination Address", type: FILTER_TYPE.STRING },
  destination_ports: { title: "Destination Ports", type: FILTER_TYPE.STRING },
  beacon_type: { title: "Beacon Type", type: FILTER_TYPE.BEACON_TYPE },
  source_cidr: { title: "Device Address", type: FILTER_TYPE.STRING },
  source_mac: { title: "Device MAC", type: FILTER_TYPE.STRING },
  match_count: { title: "Suppressed Candidates", type: FILTER_TYPE.NUMERIC }
}
