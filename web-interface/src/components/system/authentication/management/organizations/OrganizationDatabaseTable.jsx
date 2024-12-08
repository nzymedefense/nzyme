import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import SystemService from "../../../../../services/SystemService";
import {humanReadableDatabaseCategoryName} from "../../../../../util/Tools";
import numeral from "numeral";
import {notify} from "react-notify-toast";
import TenantDatabaseUsageTableRows from "../../../database/TenantDatabaseUsageTableRows";

const systemService = new SystemService();

export default function OrganizationDatabaseTable(props) {

  const organization = props.organization;

  const [sizes, setSizes] = useState(null);

  const [revision, setRevision] = useState(new Date());

  const onPurge = () => {
    setRevision(new Date());
    notify.show('Data purge request submitted. It can take a while to complete.', 'success');
  }

  const purge = (e, category) => {
    e.preventDefault();

    if (!confirm("Really purge all data of this organization in the selected data category? This cannot be undone.")) {
      return;
    }

    systemService.purgeDatabaseOrganizationCategory(category, organization.id, onPurge);
  }

  useEffect(() => {
    if (organization) {
      setSizes(null);
      systemService.getDatabaseOrganizationSizes(setSizes, organization.id);
    }
  }, [organization, revision]);

  if (!organization || !sizes) {
    return <LoadingSpinner />;
  }

  return (
    <table className="table table-sm table-hover table-striped mb-0">
      <thead>
      <tr>
        <th>Data Category</th>
        <th>Rows</th>
        <th>Retention Time</th>
        <th>&nbsp;</th>
      </tr>
      </thead>
      <tbody>
      {Object.keys(sizes.total_sizes).sort().map((key, i) => {
        return (
          <React.Fragment key={i}>
            <tr>
              <td><strong>{humanReadableDatabaseCategoryName(key)}</strong></td>
              <td>{numeral(sizes.total_sizes[key].rows).format("0,0")}</td>
              <td className="text-muted" title="Retention time is configured per tenant only.">n/a</td>
              <td>
                <a href="#" onClick={(e) => purge(e, key)}>Purge</a>
              </td>
            </tr>
            <TenantDatabaseUsageTableRows tenants={sizes.tenants}
                                          category={key}
                                          onPurge={onPurge}
                                          skipSizeColumns={true} />
          </React.Fragment>
      )
      })}
      </tbody>
    </table>
  )

}