import React, {useEffect, useState} from "react";
import UserProfileService from "../../services/UserProfileService";
import LoadingSpinner from "../misc/LoadingSpinner";

const userProfileService = new UserProfileService();

function MfaRecoveryCodes(props) {

  const show = props.show;

  const [codes, setCodes] = useState(null);

  useEffect(() => {
    if (show) {
      setCodes(null);
      userProfileService.findOwnMfaRecoveryCodes(setCodes);
    } else {
      setCodes([
          "****-****-****-****",
          "****-****-****-****",
          "****-****-****-****",
          "****-****-****-****",
          "****-****-****-****",
          "****-****-****-****",
          "****-****-****-****",
          "****-****-****-****"
      ]);
    }
  }, [show])

  if (!codes) {
    return <LoadingSpinner />
  }

  return (
      <ul className="mfa-recovery-codes">
        {codes.map(function(code, i) {
          return <li key={"mfacode-" + i}>{code}</li>
        })}
      </ul>
  )

}

export default MfaRecoveryCodes;