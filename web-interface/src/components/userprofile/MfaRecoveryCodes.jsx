import React, {useEffect, useState} from "react";
import UserProfileService from "../../services/UserProfileService";
import LoadingSpinner from "../misc/LoadingSpinner";

const userProfileService = new UserProfileService();

function MfaRecoveryCodes(props) {

  const show = props.show;

  const [codes, setCodes] = useState(null);

  useEffect(() => {
    setCodes(null);
    userProfileService.findOwnMfaRecoveryCodes(setCodes);
  }, [])

  if (!show) {
    return (
        <ul className="mfa-recovery-codes">
          <li>****-****-****-****</li>
          <li>****-****-****-****</li>
          <li>****-****-****-****</li>
          <li>****-****-****-****</li>
          <li>****-****-****-****</li>
          <li>****-****-****-****</li>
          <li>****-****-****-****</li>
          <li>****-****-****-****</li>
        </ul>
    )
  }

  if (!codes) {
    return <LoadingSpinner />
  }

  return (
      <ul className="mfa-recovery-codes">
        {Object.keys(codes).map(function(code, i) {
          return <li key={"mfacode-" + i} className={codes[code] ? "strikethrough" : null}>
            {code}
          </li>
        })}
      </ul>
  )

}

export default MfaRecoveryCodes;