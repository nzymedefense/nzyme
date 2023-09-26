import {useContext} from "react";
import {UserContext} from "../../App";

function WithMinimumRole(props) {

  const user = useContext(UserContext);

  const role = props.role;

  let show = false;
  switch(role) {
    case "SUPERADMIN":
      show = user.is_superadmin;
      break;
    case "ORGADMIN":
      show = user.is_orgadmin || user.is_superadmin;
      break;
    default:
      return null;
  }

  if (!show) {
    return null;
  }

  return props.children;

}

export default WithMinimumRole;