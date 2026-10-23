import {reconTargetOutcomeToName} from "../../../ethernet/recon/ReconScanOutcome";

export const ALLOWED_RECON_TARGET_OUTCOME_VALUES = ["OPEN", "CLOSED", "UNANSWERED"];

export const RECON_TARGET_OUTCOME_SELECT_OPTIONS = ALLOWED_RECON_TARGET_OUTCOME_VALUES.map((o) => {
  return {name: reconTargetOutcomeToName(o), value: o};
});
