import {capitalizeFirstLetter} from "../../../util/Tools";

export default function UavOperationalStatus(props) {
  const status = props.status;

  if (!status) {
    return "Unknown";
  }

  switch (status) {
    case "REMOTE_ID_SYSTEM_FAILURE":
      return "Remote ID System Failure";
    default:
      return capitalizeFirstLetter(status.toLowerCase());
  }

}