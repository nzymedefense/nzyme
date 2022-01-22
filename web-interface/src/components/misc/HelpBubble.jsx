import React from 'react'

class HelpBubble extends React.Component {
  render () {
    return (
            <a href={this.props.link} target="_blank" rel="nofollow noreferrer">
                <i className="far fa-question-circle" />
            </a>
    )
  }
}

export default HelpBubble
