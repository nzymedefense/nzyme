import React from "react";

export default function UavModelType(props) {

  const type = props.type;

  if (!type) {
    return <span className="text-muted">n/a</span>
  }

  switch (type) {
    case "GENERIC_UNKNOWN": return "Generic/Unknown";
    case "AGRICULTURE": return "Agriculture";
    case "CARGO": return "Cargo/Delivery";
    case "HOBBY_TOY": return "Hobby/Toy";
    case "INDUSTRIAL_INSPECTION": return "Industrial/Inspection";
    case "MAPPING_SURVEYING": return "Mapping/Surveying";
    case "PHOTO_VIDEO": return "Photo/Video";
    case "PUBLIC_SAFETY": return "Public Safety";
    case "RID_MODULE": return "Remote ID Module";
    default: return type
  }

}