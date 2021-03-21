import React from 'react';

class NotFoundPage extends React.Component {

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>404 - Not found!</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <div className="alert alert-danger">
                            <strong>Page not found.</strong>
                        </div>
                    </div>
                </div>
            </div>
        )
    }

}

export default NotFoundPage;



