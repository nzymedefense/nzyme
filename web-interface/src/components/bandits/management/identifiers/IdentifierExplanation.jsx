import React from 'react';
import Reflux from 'reflux';

class IdentifierExplanation extends Reflux.Component {

    render() {
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

export default IdentifierExplanation;