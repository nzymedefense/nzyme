import {beaconClassificationToName} from "../../../ethernet/beacons/BeaconClassification";

export const ALLOWED_BEACON_CLASSIFICATION_VALUES = ["UNKNOWN", "BENIGN", "SUSPICIOUS", "MALICIOUS"];

export const BEACON_CLASSIFICATION_SELECT_OPTIONS = ALLOWED_BEACON_CLASSIFICATION_VALUES.map((c) => {
  return {name: beaconClassificationToName(c), value: c};
});
