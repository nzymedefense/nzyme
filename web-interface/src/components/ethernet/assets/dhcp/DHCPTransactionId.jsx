import React from "react";

export default function DHCPTransactionId(props) {

  const id = props.id;

  return <span className="dhcp-transaction-id">{id.toString()}</span>

}