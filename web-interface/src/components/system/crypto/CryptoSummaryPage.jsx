import React, { useEffect, useState } from 'react'
import PGPKeyTable from './PGPKeyTable'
import PGPMetrics from './PGPMetrics'
import CryptoService from '../../../services/CryptoService'
import TLSCertificateTable from "./TLSCertificateTable";

const cryptoService = new CryptoService()

function CryptoSummaryPage () {
  const [crypto, setCrypto] = useState(null)
  const [pgpMetrics, setPGPMetrics] = useState(null)

  useEffect(() => {
    cryptoService.getPGPSummary(setCrypto, setPGPMetrics)
  }, [])

  return (
        <div>
            <div className="row">
                <div className="col-md-12">
                    <h1>Keys &amp; Certificates</h1>
                </div>
            </div>

            <div className="row">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>PGP Keys</h3>

                            <p>
                                The nzyme system will automatically generate PGP keys for you to encrypt sensitive
                                information in the database. Learn more about PGP keys
                                in the <a href="https://go.nzyme.org/crypto-pgp" target="_blank" rel="noreferrer">nzyme documentation</a>.
                            </p>

                            <PGPKeyTable crypto={crypto} />
                        </div>
                    </div>
                </div>

                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3 style={{display: "inline-block"}}>PGP Metrics</h3>

                            <PGPMetrics metrics={pgpMetrics} />
                        </div>
                    </div>
                </div>
            </div>

          <div className="row mt-3">
            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3>Individual TLS Certificates</h3>

                  <p>
                    Individual TLS certificates per node. By default, nzyme will generate a self-signed certificate for
                    you, but you can also upload your own. For auto-provisioning, please look at the wildcard
                    certificate configuration on this page.
                  </p>

                  <TLSCertificateTable crypto={crypto} />
                </div>
              </div>
            </div>

            <div className="col-md-6">
              <div className="card">
                <div className="card-body">
                  <h3>Wildcard TLS Certificates</h3>

                  <p>
                    Every node name that matches a regular expression below will be automatically provisioned with
                    the corresponding wildcard TLS certificate. Wildcard certificates take precedence over individual
                    certificates.
                  </p>
                </div>
              </div>
            </div>
          </div>

        </div>
  )
}

export default CryptoSummaryPage
