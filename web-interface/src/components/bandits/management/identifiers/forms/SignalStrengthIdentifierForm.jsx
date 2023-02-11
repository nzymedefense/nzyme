import React from 'react'

class SignalStrengthIdentifierForm extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      from: -1,
      to: -100,
      errorMessage: ''
    }

    this._handleFromUpdate = this._handleFromUpdate.bind(this)
    this._handleToUpdate = this._handleToUpdate.bind(this)
    this._handleUpdate = this._handleUpdate.bind(this)
  }

  _handleFromUpdate (e) {
    const from = e.target.value
    this.setState({ from: from, errorMessage: '' })

    if (from > 0 || from < -100) {
      this.setState({ errorMessage: "Invalid. The 'from' value must be between 0 and -100." })
      this.props.setFormReady(false)
      return
    }

    this._handleUpdate(from, this.state.to)
  }

  _handleToUpdate (e) {
    const to = e.target.value
    this.setState({ to: to, errorMessage: '' })

    if (to > 0 || to < -100) {
      this.setState({ errorMessage: "Invalid. The 'to' value must be between 0 and -100." })
      this.props.setFormReady(false)
      return
    }

    this._handleUpdate(this.state.from, to)
  }

  _handleUpdate (from, to) {
    from = parseInt(from, 10)
    to = parseInt(to, 10)

    if (from <= to) {
      this.setState({ errorMessage: "Invalid. The 'from' value must be larger than the 'to' value." })
      this.props.setFormReady(false)
      return
    }

    const explanation = 'a frame with a signal strength between ' + from + ' and ' + to + ' is recorded'

    this.props.setConfiguration({
      from: parseInt(from, 10),
      to: parseInt(to, 10)
    })

    this.props.setExplanation(explanation)
    this.props.setFormReady(true)
  }

  render () {
    return (
            <form onSubmit={(e) => e.preventDefault()}>
                <div className="form-group">
                    <label htmlFor="from">From</label>
                    <input type="number" className="form-control" id="from" value={this.state.from} onChange={this._handleFromUpdate} required />

                    <label htmlFor="to">To</label>
                    <input type="number" className="form-control" id="to" value={this.state.to} onChange={this._handleToUpdate} required />

                    <span className="text-danger">{this.state.errorMessage}</span>
                </div>
            </form>
    )
  }
}

export default SignalStrengthIdentifierForm
