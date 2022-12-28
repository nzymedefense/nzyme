import React, { useCallback, useEffect, useState } from 'react'
import DefaultValue from './DefaultValue'
import RestartRequired from './RestartRequired'
import ConfigurationInputField from './ConfigurationInputField'
import RetroService from '../../../services/RetroService'
import ConfigurationSubmitButton from './ConfigurationSubmitButton'
import ConfigurationCloseButton from './ConfigurationCloseButton'
import ConfigurationUpdateFailedWarning from './ConfigurationUpdateFailedWarning'
import ThreatLevelMidnight from './ThreatLevelMidnight'
import InputLabel from './InputLabel'

const retroService = new RetroService()

function ConfigurationModal (props) {
  const [inputDisabled, setInputDisabled] = useState(false)
  const [formDisabled, setFormDisabled] = useState(true)
  const [formSubmitting, setFormSubmitting] = useState(false)
  const [formSubmittedSuccessfully, setFormSubmittedSuccessfully] = useState(false)
  const [formSubmittedWithError, setFormSubmittedWithError] = useState(false)
  const [changeWarningAck, setChangeWarningAck] = useState(false)

  const [inputValue, setInputValue] = useState(props.config.value)

  useEffect(() => {
    const config = props.config

    if (props.changeWarning && !changeWarningAck) {
      setFormDisabled(true)
      return
    }

    if (inputValue === undefined || inputValue === props.config.value) {
      setFormDisabled(true)
    } else {
      if (config.constraints && config.constraints.length > 0) {
        for (const constraint of config.constraints) {
          const cData = constraint.data

          switch (constraint.type) {
            case 'STRING_LENGTH':
              setFormDisabled(inputValue.length < cData.min || inputValue.length > cData.max)
              break
            case 'NUMBER_RANGE':
              const numValue = parseInt(inputValue, 10)
              setFormDisabled(isNaN(numValue) || numValue < cData.min || numValue > cData.max)
              break
            case 'SIMPLE_BOOLEAN':
              setFormDisabled(!(inputValue === true || inputValue === false))
              break
            default:
              setFormDisabled(true)
          }
        }
      } else {
        setFormDisabled(false)
      }
    }
  }, [inputValue, changeWarningAck])

  const updateValue = useCallback(() => {
    setFormSubmittedWithError(false)
    setFormSubmitting(true)
    setFormDisabled(true)
    setInputDisabled(true)

    props.dbUpdateCallback({
      [props.config.key]: inputValue
    }, function () {
      setFormSubmitting(false)
      setFormDisabled(false)
      setFormSubmittedSuccessfully(true)
    }, function () {
      setFormSubmittedWithError(true)
      setFormSubmitting(false)
      setFormDisabled(false)
      setInputDisabled(false)
    })
  }, [inputValue])

  const resetOnCancel = useCallback(() => {
    setInputValue(props.config.value)
    setChangeWarningAck(false)
    setFormSubmittedWithError(false)
  }, [props.config])

  const resetOnFinish = useCallback(() => {
    setChangeWarningAck(false)
    setFormSubmittedSuccessfully(false)
    setInputDisabled(false)
    props.setLocalRevision(prevRev => prevRev + 1)
  }, [])

  return (
        <React.Fragment>
            <a href="web-interface/src/components/configuration/modal/ConfigurationModal#"
               data-bs-toggle="modal"
               data-bs-target={'#configuration-dialog-' + props.config.key}>
                Edit
            </a>

            <div className="modal configuration-dialog" id={'configuration-dialog-' + props.config.key}
                 data-bs-keyboard="false" data-bs-backdrop="static" tabIndex="-1"
                 aria-labelledby="staticBackdropLabel" aria-hidden="true">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">Edit Configuration Value</h5>
                        </div>

                        <div className="modal-body">
                            <InputLabel config={props.config} />

                            <ConfigurationInputField
                                type={props.config.value_type}
                                title={props.config.key_human_readable}
                                fieldKey={props.config.key}
                                value={inputValue}
                                setValue={setInputValue}
                                disabled={inputDisabled} />

                            <div className="form-text">
                                <DefaultValue value={props.config.default_value} />
                            </div>

                            <RestartRequired required={props.config.requires_restart} />
                            <ConfigurationUpdateFailedWarning failed={formSubmittedWithError} />
                            <ThreatLevelMidnight
                                enabled={props.changeWarning}
                                helpTag={props.config.help_tag}
                                configKey={props.config.key}
                                changeWarningAck={changeWarningAck}
                                setChangeWarningAck={setChangeWarningAck} />
                        </div>

                        <div className="modal-footer">
                            <ConfigurationCloseButton
                                submitting={formSubmitting}
                                submittedSuccessfully={formSubmittedSuccessfully}
                                onClick={resetOnCancel} />

                            <ConfigurationSubmitButton
                                onClick={updateValue}
                                disabled={formDisabled}
                                submitting={formSubmitting}
                                submittedSuccessfully={formSubmittedSuccessfully}
                                onFinishedClick={resetOnFinish} />
                        </div>
                    </div>
                </div>
            </div>
        </React.Fragment>
  )
}

export default ConfigurationModal
