import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../App";
import TLSService from "../../../services/ethernet/TLSService";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import Paginator from "../../misc/Paginator";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import ColumnSorting from "../../shared/ColumnSorting";
import numeral from "numeral";
import TLSCertificatesTableRow from "./TLSCertificatesTableRow";

const tlsService = new TLSService();

export default function TLSCertificatesTable(props) {

  const [organizationId, tenantId] = useSelectedTenant();

  const timeRange = props.timeRange;
  const filters = props.filters;
  const setFilters = props.setFilters;
  const revision = props.revision;

  // Set to a number of days to list only certificates expired or expiring within that many days.
  const expiringDays = props.expiringDays;

  const [orderColumn, setOrderColumn] = useState(expiringDays ? "not_after" : "last_seen");
  const [orderDirection, setOrderDirection] = useState(expiringDays ? "ASC" : "DESC");

  const [data, setData] = useState(null);

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const perPage = props.perPage ? props.perPage : 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setData(null);
    if (expiringDays) {
      tlsService.findExpiringCertificates(organizationId, tenantId, timeRange, filters, expiringDays, orderColumn, orderDirection, selectedTaps, perPage, (page-1)*perPage, setData);
    } else {
      tlsService.findAllCertificates(organizationId, tenantId, timeRange, filters, orderColumn, orderDirection, selectedTaps, perPage, (page-1)*perPage, setData);
    }
  }, [organizationId, tenantId, selectedTaps, timeRange, filters, orderColumn, orderDirection, page, revision, expiringDays]);

  const columnSorting = (columnName) => {
    return <ColumnSorting thisColumn={columnName}
                          orderColumn={orderColumn}
                          setOrderColumn={setOrderColumn}
                          orderDirection={orderDirection}
                          setOrderDirection={setOrderDirection} />
  }

  if (!data) {
    return <GenericWidgetLoadingSpinner height={150} />
  }

  if (data.certificates.length === 0) {
    if (expiringDays) {
      return <div className="mb-0 alert alert-info">No certificates in use during the selected time range are expired or expire within {expiringDays} days.</div>
    }

    return <div className="mb-0 alert alert-info">No certificates were observed during selected time range.</div>
  }

  return (
      <React.Fragment>
        <strong>Total:</strong> {numeral(data.total).format("0,0")}

        <table className="table table-sm table-hover table-striped mb-4 mt-3">
          <thead>
          <tr>
            <th>Fingerprint {columnSorting("fingerprint")}</th>
            <th>Common Name {columnSorting("common_name")}</th>
            <th>Issuer {columnSorting("issuer")}</th>
            <th className="hide-narrow">SANs</th>
            <th className="hide-narrow">Type</th>
            <th className="hide-narrow">Valid From {columnSorting("not_before")}</th>
            <th>Valid Until {columnSorting("not_after")}</th>
            <th className="hide-narrow">Key {columnSorting("public_key")}</th>
            <th className="hide-narrow">Signature {columnSorting("signature_algorithm")}</th>
            <th>Sessions {columnSorting("session_count")}</th>
            <th className="hide-narrow">Source</th>
            <th className="hide-narrow">First Seen {columnSorting("first_seen")}</th>
            <th>Last Seen {columnSorting("last_seen")}</th>
          </tr>
          </thead>
          <tbody>
          {data.certificates.map((certificate, i) => {
            return <TLSCertificatesTableRow certificate={certificate} key={i} setFilters={setFilters}/>
          })}
          </tbody>
        </table>

        <Paginator itemCount={data.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}
