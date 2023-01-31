import React, { useEffect, useState } from 'react'
import PGPKeyTable from './PGPKeyTable'
import PGPMetrics from './PGPMetrics'
import CryptoService from '../../../services/CryptoService'

const cryptoService = new CryptoService()

function CryptoSummaryPage () {
  const [pgpKeys, setPGPKeys] = useState(null)
  const [pgpMetrics, setPGPMetrics] = useState(null)

  useEffect(() => {
    cryptoService.getPGPSummary(setPGPKeys, setPGPMetrics)
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

                            <PGPKeyTable keys={pgpKeys} />
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

        </div>
  )
}

export default CryptoSummaryPage
