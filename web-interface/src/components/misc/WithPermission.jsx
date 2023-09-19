import {useContext} from "react";
import {UserContext} from "../../App";
import {userHasPermission} from "../../util/Tools";

function WithPermission(props) {

  const user = useContext(UserContext);

  const permission = props.permission;

  if (!userHasPermission(user, permission)) {
    return null;
  }

  return props.children;

}

export default WithPermission;