import {FILTER_TYPE} from "../../shared/filtering/Filters";

export const ETHERNET_MONITORS_FILTER_FIELDS = {
  name: { title: "Name", type: FILTER_TYPE.STRING },
  description: { title: "Description", type: FILTER_TYPE.STRING },
  type: { title: "Type", type: FILTER_TYPE.ETHERNET_MONITOR_TYPE },
  enabled: { title: "Enabled", type: FILTER_TYPE.BOOLEAN },
  alerted: { title: "Alerted", type: FILTER_TYPE.BOOLEAN },
  interval: { title: "Interval (Minutes)", type: FILTER_TYPE.NUMERIC },
  lookback: { title: "Lookback (Minutes)", type: FILTER_TYPE.NUMERIC },
  trigger_condition: { title: "Trigger Condition", type: FILTER_TYPE.NUMERIC },
  tap_count: { title: "Tap Count", type: FILTER_TYPE.NUMERIC }
}
