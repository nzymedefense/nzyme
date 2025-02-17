import {capitalizeFirstLetter} from "../../../util/Tools";

export default function UavType(props) {
    const type = props.type;

    if (!type) {
      return "Unknown";
    }

    switch (type) {
      case "MULTIROTOR_HELICOPTER":
        return "Multirotor/Helicopter"
      case "FREE_BALLOON":
        return "Free Balloon";
      case "CAPTIVE_BALLOON":
        return "Captive Balloon";
      case "UNPOWERED_FREEFALL":
        return "Unpowered Free Fall";
      case "TETHERED_POWERED":
        return "Tethered (Powered)";
      case "GROUND_OBSTACLE":
        return "Ground Obstacle";
      default:
        return capitalizeFirstLetter(type.toLowerCase());
    }

}