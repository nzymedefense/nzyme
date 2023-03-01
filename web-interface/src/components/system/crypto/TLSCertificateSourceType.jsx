function TLSCertificateSourceType(props) {

  switch (props.type) {
    case "GENERATED_SELF_SIGNED":
      return "Generated / Self-Signed"
    case "INDIVIDUAL":
      return "Individual, loaded from database"
    case "FILE_LOADED":
      return "Individual, loaded from file"
    case "WILDCARD":
      return "Wildcard"
    case "TEST":
      return "Test/Transient"
    default:
      return "Unknown / Invalid"
  }

}

export default TLSCertificateSourceType;