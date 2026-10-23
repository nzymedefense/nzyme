import {FILTER_TYPE} from "../../../shared/filtering/Filters";

export const RECON_EXCLUSIONS_FILTER_FIELDS = {
  name: { title: "Name", type: FILTER_TYPE.STRING },
  description: { title: "Description", type: FILTER_TYPE.STRING },
  enabled: { title: "Enabled", type: FILTER_TYPE.BOOLEAN },
  scan_type: { title: "Scan Type", type: FILTER_TYPE.RECON_SCAN_TYPE },
  source_cidr: { title: "Scanner Address", type: FILTER_TYPE.STRING },
  source_mac: { title: "Scanner MAC", type: FILTER_TYPE.STRING },
  target_cidr: { title: "Target Network", type: FILTER_TYPE.STRING },
  match_count: { title: "Suppressed Scans", type: FILTER_TYPE.NUMERIC }
}
