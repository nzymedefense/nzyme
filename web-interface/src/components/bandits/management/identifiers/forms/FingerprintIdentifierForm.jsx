import React from 'react'

class FingerprintIdentifierForm extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      fingerprint: '',
      errorMessage: ''
    }

    this._handleUpdate = this._handleUpdate.bind(this)
  }

  _handleUpdate (e) {
    const fingerprint = e.target.value.replace(/ /g, '')
    this.setState({ fingerprint: fingerprint, errorMessage: '' })

    if (fingerprint.length !== 64) {
      this.setState({ errorMessage: 'Invalid fingerprint. A valid fingerprint is 64 characters long.' })
      this.props.setFormReady(false);
      return
    }

    const explanation = fingerprint ? 'a frame with fingerprint "' + fingerprint + '" is recorded' : undefined

    this.props.setConfiguration({ fingerprint: fingerprint });

    this.props.setExplanation(explanation);
    this.props.setFormReady(true);
  }

  render () {
    return (
            <form onSubmit={(e) => e.preventDefault()}>
                <div className="form-group">
                    <label htmlFor="fingerprint">Fingerprint</label>
                    <input type="text" className="form-control" id="fingerprint" placeholder="Enter the fingerprint of the bandit"
                           value={this.state.fingerprint} onChange={this._handleUpdate} required />

                    <span className="text-danger">{this.state.errorMessage}</span>
                </div>
            </form>
    )
  }
}

export default FingerprintIdentifierForm
