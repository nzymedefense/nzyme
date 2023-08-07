function Subsystem(props) {

  const subsystem = props.subsystem;

  switch (subsystem) {
    case "DOT11":
      return "802.11 / WiFi";
    case "ETHERNET":
      return "Ethernet";
    default:
      return "Unknown";
  }

}

export default Subsystem;