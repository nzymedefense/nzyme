import {FILTER_TYPE} from "../../shared/filtering/Filters";

export const RECON_SCAN_TARGETS_FILTER_FIELDS = {
  target_address: { title: "Target Address", type: FILTER_TYPE.IP_ADDRESS },
  target_port: { title: "Target Port", type: FILTER_TYPE.NUMERIC },
  outcome: { title: "Outcome", type: FILTER_TYPE.RECON_TARGET_OUTCOME },
  probe_count: { title: "Probe Count", type: FILTER_TYPE.NUMERIC }
}
