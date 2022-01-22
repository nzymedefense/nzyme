import React from 'react'

class IdentifierExplanation extends React.Component {
  render () {
    if (!this.props.explanation) {
      return (
                <div className="alert alert-primary">
                    ...
                </div>
      )
    }

    return (
            <div className="alert alert-primary">
                Identifies a bandit when {this.props.explanation} and all other bandit identifiers match.
            </div>
    )
  }
}

export default IdentifierExplanation
