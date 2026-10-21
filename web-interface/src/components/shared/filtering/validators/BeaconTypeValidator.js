import {beaconTypeToName} from "../../../ethernet/beacons/BeaconType";

export const ALLOWED_BEACON_TYPE_VALUES = ["TLS", "HTTP", "SSH", "SOCKS", "DNS", "TCP", "UDP"];

export const BEACON_TYPE_SELECT_OPTIONS = ALLOWED_BEACON_TYPE_VALUES.map((type) => {
  return {name: beaconTypeToName(type), value: type};
});
