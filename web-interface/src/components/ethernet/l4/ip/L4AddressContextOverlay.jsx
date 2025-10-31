import React from "react";
import ApiRoutes from "../../../../util/ApiRoutes";

const countries = require("i18n-iso-countries");
countries.registerLocale(require("i18n-iso-countries/langs/en.json"));

export default function L4AddressContextOverlay(props) {

  const address = props.address;
  const attributeSummary = () => {
    if (address.attributes === null) {
      return null;
    }

    if (address.attributes.is_site_local) {
      return "Site-Local / RFC 1918 IP Address"
    }

    if (address.attributes.is_loopback) {
      return "Loopback IP Address"
    }

    if (address.attributes.is_multicast) {
      return "Multicast IP Address"
    }
  }

  const attributes = () => {
    if (address.attributes === null) {
      return ["None"];
    }

    let attributes = [];

    if (address.attributes.is_site_local) {
      attributes.push("Site-Local");
    }

    if (address.attributes.is_loopback) {
      attributes.push("Loopback");
    }

    if (address.attributes.is_multicast) {
      attributes.push("Multicast");
    }

    if (attributes.length === 0) {
      return "None";
    } else {
      return attributes.join(", ")
    }
  }

  const asn = () => {
    if (!address.geo || !address.geo.asn_name) {
      return <span className="text-muted">n/a</span>
    }

    return address.geo.asn_name + " (" + address.geo.asn_number + ")";
  }

  const country = () => {
    if (!address.geo || !address.geo.country_code) {
      return <span className="text-muted">n/a</span>
    }

    return countries.getName(address.geo.country_code, "en", {select: "official"}) + " (" + address.geo.country_code +")";
  }

  const city = () => {
    if (!address.geo || !address.geo.city) {
      return <span className="text-muted">n/a</span>
    }

    return address.geo.city;
  }

  if (address.attributes) {
    // This is a GEO-enriched address.
    if (address.attributes.is_site_local || address.attributes.is_multicast || address.attributes.is_loopback) {
      // Local IP.
      return (
          <React.Fragment>
            <h6><i className="fa-solid fa-map-location-dot"/> {address.address}</h6>

            <p className="context-description">
              <i className="fa-solid fa-circle-info"></i> {attributeSummary()}
            </p>

            <dl style={{marginBottom: 120}}>
              <dt>Attributes:</dt>
              <dd>{attributes()}</dd>
            </dl>

            <a href={ApiRoutes.ETHERNET.IP.ADDRESS_DETAILS(address.address)} className="btn btn-sm btn-outline-primary">
              Open Address Details
            </a>
          </React.Fragment>
      )
    } else {
      // Not a local IP.
      return (
          <React.Fragment>
            <h6><i className="fa-solid fa-map-location-dot"/> {address.address}</h6>

            <dl style={{marginBottom: 77}}>
              <dt>Attributes:</dt>
              <dd>{attributes()}</dd>
              <dt>ASN:</dt>
              <dd>{asn()}</dd>
              <dt>ASN Domain:</dt>
              <dd>{address.geo && address.geo.asn_domain ? address.geo.asn_domain :
                  <span className="text-muted">n/a</span>}</dd>
              <dt>Country:</dt>
              <dd>{country()}</dd>
              <dt>City:</dt>
              <dd>{city()}</dd>
            </dl>

            <a href={ApiRoutes.ETHERNET.IP.ADDRESS_DETAILS(address.address)} className="btn btn-sm btn-outline-primary">
              Open Address Details
            </a>
          </React.Fragment>
      )
    }
  } else {
    // Not a GEO-enriched address and no attributes.

    return (
        <React.Fragment>
          <h6><i className="fa-solid fa-map-location-dot"/> {address.address}</h6>

          <p className="context-description">No attributes.</p>

          <dl style={{marginBottom: 120}}>
            <dt>Attributes:</dt>
            <dd>{attributes()}</dd>
          </dl>

          <a href={ApiRoutes.ETHERNET.IP.ADDRESS_DETAILS(address.address)} className="btn btn-sm btn-outline-primary">
            Open Address Details
          </a>
        </React.Fragment>
    )
  }

}