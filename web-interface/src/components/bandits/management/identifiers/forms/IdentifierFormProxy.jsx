import React from 'react'
import FingerprintIdentifierForm from './FingerprintIdentifierForm'
import SignalStrengthIdentifierForm from './SignalStrengthIdentifierForm'
import SSIDIdentifierForm from './SSIDIdentifierForm'
import PwnagotchiIdentityForm from './PwnagotchiIdentityForm'

class IdentifierFormProxy extends React.Component {
  render () {
    const formType = this.props.formType
    if (formType === 'FINGERPRINT') {
      return (
                <FingerprintIdentifierForm
                  setConfiguration={this.props.setConfiguration}
                  setExplanation={this.props.setExplanation}
                  setFormReady={this.props.setFormReady} />
      )
    }

    if (formType === 'SIGNAL_STRENGTH') {
      return (
                <SignalStrengthIdentifierForm
                  setConfiguration={this.props.setConfiguration}
                  setExplanation={this.props.setExplanation}
                  setFormReady={this.props.setFormReady} />
      )
    }

    if (formType === 'SSID') {
      return (
                <SSIDIdentifierForm
                  setConfiguration={this.props.setConfiguration}
                  setExplanation={this.props.setExplanation}
                  setFormReady={this.props.setFormReady} />
      )
    }

    if (formType === 'PWNAGOTCHI_IDENTITY') {
      return (
                <PwnagotchiIdentityForm
                  setConfiguration={this.props.setConfiguration}
                  setExplanation={this.props.setExplanation}
                  setFormReady={this.props.setFormReady} />
      )
    }

    return (
            <div className="alert alert-danger">UNKNOWN FORM TYPE. NOT IMPLEMENTED.</div>
    )
  }
}

export default IdentifierFormProxy
