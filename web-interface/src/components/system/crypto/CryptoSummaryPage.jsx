import React from 'react';
import PGPKeyTable from "./PGPKeyTable";

function CryptoSummaryPage() {

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
                                information in the database. It is important to have the same keys on all nzyme leader
                                nodes or decryption will not work and configuration breaks. If you run multiple nzyme
                                leader nodes, you must copy the existing keys from another node. Learn more about PGP keys
                                in the <a href="https://go.nzyme.org/crypto-pgp" target="_blank">nzyme documentation</a>.
                            </p>

                            <PGPKeyTable />
                        </div>
                    </div>
                </div>
            </div>

        </div>
    )

}

export default CryptoSummaryPage;