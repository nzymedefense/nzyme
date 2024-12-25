export const translateInfrastructureType = (type) => {
  switch (type) {
    case "accesspoint":
      return "Infrastructure";
    case "adhoc":
      return "Ad-Hoc";
    case "invalid":
      return "Unknown/Invalid";
  }
}