import React, {useState} from "react";
import DNSData from "../DNSData";
import moment from "moment";
import ETLD from "../../shared/ETLD";
import L4Address from "../../shared/L4Address";
import DNSLogResponseTable from "../shared/DNSLogResponseTable";
import FilterValueIcon from "../../../shared/filtering/FilterValueIcon";
import {DNS_FILTER_FIELDS} from "../DNSFilterFields";

export default function DNSTransactionLogTableRow(props) {

  const log = props.log;
  const setFilters = props.setFilters;

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
          <td>
            {log.query.data_type}

            <FilterValueIcon setFilters={setFilters}
                             fields={DNS_FILTER_FIELDS}
                             field="query_type"
                             value={log.query.data_type} />
          </td>
          <td title={log.query.timestamp}>{moment(log.query.timestamp).format()}</td>
          <td title={log.query.data_value_etld}><ETLD etld={log.query.data_value_etld}/></td>
          <td><L4Address address={log.query.client} hidePort={true} /></td>
          <td><L4Address address={log.query.server} /></td>
        </tr>

        <DNSLogResponseTable responses={log.responses} show={showResponses}/>
      </React.Fragment>
  )

}