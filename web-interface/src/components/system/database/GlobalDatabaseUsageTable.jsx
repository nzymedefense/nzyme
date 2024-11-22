import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";

import numeral from "numeral";
import OrganizationDatabaseUsageTableRows from "./OrganizationDatabaseUsageTableRows";
import SystemService from "../../../services/SystemService";

const systemService = new SystemService();

export default function GlobalDatabaseUsageTable(props) {
  
  const sizes = props.sizes;
  const onPurge = props.onPurge;

  const humanReadableCategoryName = (category) => {
    switch (category) {
      case "DOT11":
        return "802.11/WiFi"
      case "BLUETOOTH":
        return "Bluetooth"
      case "ETHERNET_DNS":
        return "Ethernet: DNS"
      case "ETHERNET_L4":
        return "Ethernet: Layer 4"
    }

    return category;
  }

  const purge = (e, category) => {
    e.preventDefault();

    if (!confirm("Really purge all data in the selected data category? This cannot be undone.")) {
      return;
    }

    systemService.purgeDatabaseGlobalCategory(category, onPurge);
  }

  if (!sizes) {
    return <LoadingSpinner />
  }

  return (
      <table className="table table-sm table-hover table-striped mb-0">
        <thead>
        <tr>
          <th>Data Category</th>
          <th>Data Size</th>
          <th>Rows</th>
          <th>Retention Time</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {Object.keys(sizes.global_sizes).sort().map((key, i) => {
          return (
              <React.Fragment key={i}>
                <tr>
                  <td><strong>{humanReadableCategoryName(key)}</strong></td>
                  <td>{numeral(sizes.global_sizes[key].bytes).format("0,0b")}</td>
                  <td>{numeral(sizes.global_sizes[key].rows).format("0,0")}</td>
                  <td className="text-muted" title="Retention time is configured per tenant only.">n/a</td>
                  <td>
                    <a href="#" onClick={(e) => purge(e, key)}>Purge</a>
                  </td>
                </tr>
                <OrganizationDatabaseUsageTableRows organizations={sizes.organizations}
                                                    category={key}
                                                    onPurge={onPurge} />
              </React.Fragment>
          )
        })}
        </tbody>
      </table>
  )
  
}