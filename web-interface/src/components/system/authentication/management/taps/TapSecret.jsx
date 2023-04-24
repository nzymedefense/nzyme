import React, { useCallback, useState, useEffect } from 'react'

const STARS = '****************************************************************'

function TapSecret (props) {

  const plainSecret = props.secret;
  const onCycle = props.onCycle;

  const [secret, setSecret] = useState(STARS)
  const [buttonText, setButtonText] = useState('Show Secret')
  const [toggled, setToggled] = useState(false)

  const toggle = useCallback(() => {
    if (toggled) {
      setSecret(STARS)
      setToggled(false)
      setButtonText('Show Secret')
    } else {
      setSecret(plainSecret)
      setToggled(true)
      setButtonText('Hide Secret')
    }
  }, [toggled, plainSecret])

  useEffect(() => {
    if (toggled) {
      setSecret(plainSecret)
    }
  }, [toggled])

  return (
  <div className="tap-secret">
    <form className="form-floating">
      <input type="text" className="form-control" placeholder={secret} value={secret} readOnly={true} />
        <label htmlFor="floatingInputInvalid">Secret</label>
    </form>

    <div className="mt-2">
      <button className="btn btn-sm btn-primary" onClick={toggle}>{buttonText}</button>&nbsp;
      <button className="btn btn-sm btn-warning" onClick={onCycle}>Cycle Secret</button>
    </div>
  </div>
  )
}

export default TapSecret