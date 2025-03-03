import React from "react";
import {capitalizeFirstLetter} from "../../../util/Tools";

export default function UavModelType(props) {

  const type = props.type;

  if (!type) {
    return "Unknown";
  }

  switch (type) {
    case "AERIAL_INTELLIGENCE":
      return "Aerial Intelligence"
    case "PHOTO_VIDEO":
      return "Photo/Video";
    case "PRO_MULTI":
      return "Professional Multi-Use";
    case "PRO_SURVEY":
      return "Professional Survey";
    default:
      return capitalizeFirstLetter(type.toLowerCase());
  }

}