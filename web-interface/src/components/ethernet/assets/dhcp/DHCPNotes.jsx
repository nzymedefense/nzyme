import React from "react";
import MacAddress from "../../../shared/context/macs/MacAddress";

export default function DHCPNotes(props) {

  const notes = props.tx.notes;
  const additionalServerMacs = props.tx.additional_server_macs;
  const additionalClientMacs = props.tx.additional_client_macs;
  const additionalOptions = props.tx.additional_options;
  const additionalVendorClasses = props.tx.additional_vendor_classes;

  const macList = (data, title) => {
    if (data == null || data.length === 0) {
      return null;
    }

    return (
        <div className="mt-3 mb-0">
          <h3>{title}</h3>
          <ul>
            {data.map((x, i) => {
              return (
                  <li key={i}><MacAddress address={x} /></li>
              )
            })}
          </ul>
        </div>
    )
  }

  const optionsList = (data, title) => {
    if (data == null || data.length === 0) {
      return null;
    }

    return (
        <div className="mt-3 mb-0">
          <h3>{title}</h3>

          <ul className="mb-0">
            {data.map((x, i) => {
              return (
                  <li key={i}><span className="machine-data">{x.join(",")}</span></li>
              )
            })}
          </ul>
        </div>
    )
  }

  const stringList = (data, title) => {
    if (data == null || data.length === 0) {
      return null;
    }

    return (
        <div className="mt-3 mb-0">
          <h3>{title}</h3>

          <ul className="mb-0">
            {data.map((x, i) => {
              return (
                  <li key={i}><span className="machine-data">{x}</span></li>
              )
            })}
          </ul>
        </div>
    )
  }

  if (notes == null || notes.length === 0) {
    return <div className="alert alert-success mb-0">No notes for this transaction. Everything looks OK!</div>
  }

  return (
      <React.Fragment>
        {notes.map((note, index) => {
          let text = note;
          switch (note) {
            case "OfferNoYiaddr":
              text = <span>A missing <code>yiaddr</code> (your IP address) in a DHCP offer or acknowledgment is
                anomalous and may indicate misconfiguration, IP pool exhaustion, or malformed/malicious traffic.
                The <code>yiaddr</code> field is where the DHCP server specifies the IP address being assigned to the
                client. Its absence suggests the server did not fulfill its core function, potentially disrupting
                client connectivity or signaling abnormal or suspicious network behavior.</span>
              break;
            case "ClientMacChanged":
              text = <span>A changing client MAC address during a DHCP transaction is unusual and may indicate spoofing,
                misconfigured devices, or non-compliant DHCP implementations. The client MAC address is expected to
                remain consistent throughout the exchange, as it uniquely identifies the requesting device. Changes
                mid-transaction can disrupt lease tracking, complicate attribution, and may signal attempts to evade
                monitoring or impersonate other hosts.</span>
              break;
            case "ServerMacChanged":
              text = <span>A changing server MAC address during a DHCP transaction is abnormal and may indicate rogue
                  DHCP servers, or spoofing attempts. The server MAC should remain consistent to ensure reliable lease
                  attribution and client trust. Mid-transaction changes can break expected behavior, confuse clients,
                  and may signal malicious activity or infrastructure issues.</span>
              break;
            case "OptionsChanged":
            case "VendorClassChanged":
              text = <span>Changing DHCP options or vendor class mid-transaction is unexpected and may indicate
                inconsistent server behavior, misconfiguration, or potential tampering. DHCP options&mdash;such as
                DNS servers, lease time, or router settings&mdash;should remain stable between offer and
                acknowledgment. Variations can lead to unreliable client configuration, operational issues, or signal
                an attempt to manipulate network behavior.</span>
              break;
          }
          return (
              <div className="alert alert-danger" key={index}>{text}</div>
          );
        })}

        {macList(additionalServerMacs, "Additional Server MAC Addresses")}
        {macList(additionalClientMacs, "Additional Client MAC Addresses")}
        {optionsList(additionalOptions, "Additional Options")}
        {stringList(additionalVendorClasses, "Additional Vendor Classes")}
      </React.Fragment>
  )

}