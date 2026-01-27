export const translateInfrastructureType = (type) => {
  switch (type) {
    case "AccessPoint":
      return "Infrastructure";
    case "AdHoc":
      return "Ad-Hoc";
    case "Invalid":
      return "Unknown/Invalid";
  }
}