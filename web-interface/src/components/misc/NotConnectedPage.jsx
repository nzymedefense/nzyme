import React from 'react'
import AssetStylesheet from "./AssetStylesheet";

function NotConnectedPage() {
    return (
        <React.Fragment>
            <AssetStylesheet filename="onebox.css" />

            <section className="vh-100 start">
                <div className="container py-5 h-100 mb-5">
                    <div className="row d-flex justify-content-center align-items-center h-100">
                        <div className="col col-xl-10">
                            <div className="card main-card">
                                <div className="row g-0 vh-100">
                                    <div className="col-md-5 d-flex align-items-center">
                                        <div className="card-body p-4 p-lg-5 text-black">
                                            <h1 className="mb-3 pb-3 text-danger">Error.</h1>
                                            <p className="text-danger">
                                                <strong>Web interface unable to connect to nzyme REST API.</strong>
                                            </p>

                                            <div className="mt-4">
                                                <h6>Common issues include:</h6>
                                                <ul>
                                                    <li>No nzyme node is running.</li>
                                                    <li>A firewall blocks access from your browser to the leader node REST API.</li>
                                                    <li>The REST API is listening on an interface that is not reachable by your browser.</li>
                                                    <li>A node tells your browser a
                                                        wrong <code>http_external_uri</code> and your browser cannot connect to it.</li>
                                                </ul>
                                            </div>

                                            <div className="mt-4">
                                                <a href="https://go.nzyme.org/help" className="btn btn-sm btn-dark" target="_blank" rel="noreferrer">
                                                    Get Help in the Documentation
                                                </a>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="col-md-7 d-none d-md-block justify-content-center right-half">
                                        <video id="background-video" autoPlay loop muted poster={window.appConfig.assetsUri + "static/loginsplash_preview.jpg"}>
                                            <source src={window.appConfig.assetsUri + "static/loginsplash.mp4"} type="video/mp4" />
                                        </video>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </React.Fragment>
    )
}

export default NotConnectedPage
