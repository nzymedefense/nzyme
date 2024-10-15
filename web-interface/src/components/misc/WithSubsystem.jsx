import {useContext} from "react";
import {UserContext} from "../../App";
import {userHasSubsystem} from "../../util/Tools";

export default function WithSubsystem(props) {

  const user = useContext(UserContext);

  const subsystem = props.subsystem;

  if (!userHasSubsystem(user, subsystem)) {
    return null;
  }

  return props.children;

}