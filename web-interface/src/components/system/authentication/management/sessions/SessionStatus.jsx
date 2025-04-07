export default function SessionStatus(props) {

  const session = props.session;

  if (session.mfa_disabled) {
    return "Disabled"
  } else {
    if (session.mfa_valid) {
      return "Passed/Active";
    } else {
      return "Pending"
    }
  }

}