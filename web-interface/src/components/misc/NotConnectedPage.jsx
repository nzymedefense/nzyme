import React from 'react'

class NotConnectedPage extends React.Component {
    render () {
        return (
            <section className="vh-100 start">
                <div className="container py-5 h-100">
                    <div className="row d-flex justify-content-center align-items-center h-100">
                        <div className="col col-xl-10">
                            <div className="card main-card">
                                <div className="row g-0">
                                    <div className="col-md-6 col-lg-5 d-none d-md-block left-half justify-content-center">
                                        <img src="/static/logo_small.png" className="d-block mx-auto"
                                            id="logo" alt="nzyme logo" />
                                    </div>

                                    <div className="col-md-6 col-lg-7 d-flex align-items-center">
                                        <div className="card-body p-4 p-lg-5 text-black">
                                            <h5>
                                                Could not connect to nzyme REST API
                                                at <code>{window.appConfig.nzymeApiUri}</code>
                                            </h5>

                                            <div className="mt-4">
                                                <h6>Common issues include:</h6>
                                                <ul>
                                                    <li>Nzyme is not running on the leader node.</li>
                                                    <li>A firewall blocks access from your browser to the leader node REST API.</li>
                                                    <li>The leader node REST API is listening on an interface that is not
                                                        reachable by your browser.</li>
                                                    <li>The leader node tells your browser a
                                                        wrong <code>transport_address</code> and your browser cannot connect to it.</li>
                                                </ul>
                                            </div>

                                            <p className="mt-4">
                                                <a href="https://go.nzyme.org/help" className="btn btn-sm btn-dark" target="_blank">
                                                    Help
                                                </a>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        )
    }
}

export default NotConnectedPage
