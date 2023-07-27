import React from 'react'

class Footer extends React.Component {
  render () {
    return (
        <div className="row mt-3" id="footer">
            <div className="col-md-8">
                <hr />

                <p>
                    pugnantis latus defensantes
                </p>
            </div>

            <div className="col-md-4 text-end">
                <hr />

                Icons are <a href="https://fontawesome.com/" target="_blank" rel="noreferrer">FontAwesome CC BY 4.0</a>
            </div>
        </div>
    )
  }
}

export default Footer
