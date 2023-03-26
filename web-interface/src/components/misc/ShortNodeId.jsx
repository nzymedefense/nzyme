import React from "react";
import ApiRoutes from "../../util/ApiRoutes";

function ShortNodeId(props) {

  const id = props.id;

  return <a href={ApiRoutes.SYSTEM.CLUSTER.NODES.DETAILS(id)}>{id.substr(0, 8)}</a>;

}

export default ShortNodeId;