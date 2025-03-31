export default function CotConnectionType(props) {

  const type = props.type;

  switch (type) {
    case "UDP_PLAINTEXT": return "Plaintext (UDP)"
    default: return type
  }

}