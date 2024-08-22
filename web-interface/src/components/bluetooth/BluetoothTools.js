import React from "react";

export const transformTransport = (transport, fullName) => {
  if (transport === "bredr") {
    return <span title="Bluetooth Classic">{fullName ? "Bluetooth Classic" : "Classic"}</span>;
  }

  if (transport === "le") {
    return <span title="Bluetooth Low Energy">{fullName ? "Bluetooth Low Energy" : "LE"}</span>;
  }

  return transport;
}

export const transformTag = (tag) => {
  switch (tag) {
    case "apple_find_my_paired": return "Apple \"Find My\" (Paired)";
    case "apple_find_my_unpaired": return "Apple \"Find My\" (Unpaired)";
    default: return tag;
  }
}
