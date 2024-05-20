import React, {useState} from "react";
import moment from "moment/moment";
import IPAddressLink from "../../shared/IPAddressLink";

import numeral from "numeral";
import DNSData from "../DNSData";
import ETLD from "../../shared/ETLD";
import DNSEntropyLogResponseTable from "../DNSEntropyLogResponseTable";

export default function DNSEntropyLogTableRow(props) {

  const log = props.log;
  const [showResponses, setShowResponses] = useState(false);

  const toggleVisibility = (e) => {
    e.preventDefault();

    setShowResponses(!showResponses);
  }

  return (
      <React.Fragment>
        <tr>
          <td>
            <a href="#">
              <DNSData value={log.query.data_value}/>
            </a>&nbsp;&nbsp;

            <a href="#" title="Show Responses" onClick={toggleVisibility}>
              <i className="fa-solid fa-chevron-down"></i>
            </a>
          </td>
          <td>{log.query.data_type}</td>
          <td title={log.query.timestamp}>{moment(log.query.timestamp).format()}</td>
          <td title={log.query.data_value_etld}><ETLD etld={log.query.data_value_etld}/></td>
          <td><IPAddressLink ip={log.query.client_address}/></td>
          <td><IPAddressLink ip={log.query.server_address} port={log.query.server_port}/></td>
          <td title={log.entropy + " / " + log.entropy_mean}>
            {numeral(log.entropy).format("0,0.00")} / {numeral(log.entropy_mean).format("0,0.00")}
          </td>
          <td title={log.zscore}>{numeral(log.zscore).format("0,0.0")}</td>
        </tr>

        <DNSEntropyLogResponseTable responses={log.responses} show={showResponses}/>
      </React.Fragment>
  )

}