import React, {useState} from "react";

export default function BluetoothMacAddressConditionForm(props) {

  const [mac, setMac] = useState("");
  const [matchType, setMatchType] = useState("EXACT");

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  return (
    <React.Fragment>
      <div className="mb-1">
        <div className="form-check form-check-inline">
          <input className="form-check-input" type="radio" name="condition_mac_address_type"
                 id="condition_mac_address_type-exact"
                 checked={matchType === "EXACT"}
                 onChange={() => setMatchType("EXACT")} />
          <label className="form-check-label" htmlFor="condition_mac_address_type-exact">
            Exact/Full Match
          </label>
        </div>

        <div className="form-check form-check-inline">
          <input className="form-check-input" type="radio" name="condition_mac_address_type"
                 id="condition_mac_address_type-prefix"
                 checked={matchType === "PREFIX"}
                 onChange={() => setMatchType("PREFIX")} />
          <label className="form-check-label" htmlFor="condition_mac_address_type-prefix">
            Prefix Match
          </label>
        </div>
      </div>

      <div>
        <label htmlFor="condition_mac_address_mac" className="form-label">MAC Address</label>
        <input type="text" className="form-control" id="condition_mac_address_mac"
               value={mac} onChange={(e) => { updateValue(e, setMac) }} />
        <div className="form-text">{matchType === "EXACT"
          ? <span>The <strong>full</strong> MAC address to match.</span>
          : <span>The <strong>prefix</strong> of the MAC address to match. For example: The
            prefix <code>0C:FA:22</code> would match the MAC address <code>0C:FA:22:8A:37:1C</code>.</span>} </div>
      </div>
    </React.Fragment>

  )

}