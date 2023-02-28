import React from "react";

function TLSCertificateDetailsPrincipal(props) {

  const principal = props.principal;

  return (
      <dl>
        <dt>Alternative Names</dt>
        <dd>
          {principal.alternative_names && principal.alternative_names.length > 0 ?
              principal.alternative_names.join(", ") : "n/a"}
        </dd>
        <dt>Common Name (CN)</dt>
        <dd>{principal.cn ? principal.cn : "n/a"}</dd>
        <dt>Organization (O)</dt>
        <dd>{principal.o ? principal.o : "n/a"}</dd>
        <dt>Country (C)</dt>
        <dd>{principal.c ? principal.c : "n/a"}</dd>
      </dl>
  )

}

export default TLSCertificateDetailsPrincipal;