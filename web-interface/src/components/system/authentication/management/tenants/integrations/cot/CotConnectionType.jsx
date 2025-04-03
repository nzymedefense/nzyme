export default function CotConnectionType(props) {

  const type = props.type;

  switch (type) {
    case "UDP_PLAINTEXT": return "Plaintext (UDP)"
    case "TCP_X509": return "Secure Streaming (TCP)"
    default: return type
  }

}