import React from "react";
import moment from "moment/moment";
import Flag from "../../../../../misc/Flag";

const countryNames = new Intl.DisplayNames(['en'], {type: 'region'})

function LastUserActivity(props) {

  const timestamp = props.timestamp;
  const remoteAddress = props.remoteAddress;
  const remoteCountry = props.remoteCountry;
  const remoteCity = props.remoteCity;
  const remoteAsn = props.remoteAsn;

  return (
      <dl className="mb-0">
        <dt>Timestamp</dt>
        <dd>{moment(timestamp).format()}</dd>

        <dt>Remote Address</dt>
        <dd>{remoteAddress}</dd>

        <dt>Geo Information</dt>
        <dd>
          {remoteCountry ? <Flag code={remoteCountry} /> : null}{' '}
          {remoteCountry ? countryNames.of(remoteCountry) : "Unknown Country"}{' '}
          ({remoteCity ? remoteCity : "Unknown City"}){' '}
          ({remoteAsn ? remoteAsn : "Unknown ASN"})
        </dd>
      </dl>
  )

}

export default LastUserActivity;