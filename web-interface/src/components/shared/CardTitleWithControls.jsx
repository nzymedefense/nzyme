import React, {useState} from "react";
import TimeRangeSelector from "./timerange/TimeRangeSelector";
import AppliedTimeRange from "./timerange/AppliedTimeRange";
import LoadingSpinner from "../misc/LoadingSpinner";

import numeral from "numeral";
import LLMResponse from "./llm/LLMResponse";

function CardTitleWithControls(props) {

  const title = props.title;
  const disabled = props.disabled;

  const slim = props.slim;

  const setTimeRange = props.setTimeRange;
  const timeRange = props.timeRange;
  const urlKey = props.urlKey;

  const helpLink = props.helpLink;
  const internalLink = props.internalLink;
  const refreshAction = props.refreshAction;

  const smallTextParam = props.smallText;
  const hideTimeRange = props.hideTimeRange;
  const fixedAppliedTimeRange = props.fixedAppliedTimeRange;
  const doNotPersistTimeRange = props.doNotPersistTimeRange || false;
  const [timeRangeDialogOpened, setTimeRangeDialogOpened] = useState(false);

  const llmAction = props.llmAction;
  const llmIsProcessing = props.llmIsProcessing;
  const [showLlmDialog, setShowLlmDialog] = useState(false);
  const [selectedLlmModel, setSelectedLlmModel] = useState("granite-4-micro");

  const smallText = () => {
    if (!smallTextParam) {
      return null;
    }
    return <small>{smallTextParam}</small>
  }

  const timeRangeButton = () => {
    if (setTimeRange && timeRange) {
      return (
        <button className="btn card-title-option"
                title="Toggle Time Range Selector"
                disabled={disabled}
                onClick={(e) => {
                  e.preventDefault();
                  setTimeRangeDialogOpened(!timeRangeDialogOpened);
                }}>
          <i className="fa-regular fa-clock"></i>
        </button>
      )
    }
  }

  const helpLinkButton = () => {
    if (!helpLink) {
      return null;
    }

    if (disabled) {
      return (
        <button className="btn card-title-option"
                title="Help"
                disabled={true}
                onClick={(e) => {
                  e.preventDefault();
                }}>
          <i className="fa-solid fa-question"></i>
        </button>
      )
    }

    return (
      <a href={helpLink} className="card-title-option" title="Help" target="_blank">
        <i className="fa-solid fa-question"></i>
      </a>
    )
  }

  const internalLinkButton = () => {
    if (!internalLink) {
      return null;
    }

    if (disabled) {
      return (
        <button className="btn card-title-option"
                title="Navigate"
                disabled={true}
                onClick={(e) => {
                  e.preventDefault();
                }}>
          <i className="fa-solid fa-diamond-turn-right"></i>
        </button>
      )
    }

    return (
      <a href={internalLink} className="card-title-option" title="Navigate">
        <i className="fa-solid fa-diamond-turn-right"></i>
      </a>
    )
  }

  const refreshButton = () => {
    if (refreshAction) {
      return (
        <button className="btn card-title-option"
                title="Refresh"
                disabled={disabled}
                onClick={(e) => {
                  e.preventDefault();
                  refreshAction();
                }}>
          <i className="fa-solid fa-rotate"></i>
        </button>
      )
    }
  }

  const llmActionButton = () => {
    if (llmAction) {
      return (
        <button className="btn card-title-option"
                title={llmAction.title}
                disabled={disabled}
                onClick={(e) => {
                  e.preventDefault();
                  setShowLlmDialog(!showLlmDialog);
                }}>
          <i className="fa-solid fa-brain"></i>
        </button>
      )
    }
  }

  const llmDialog = () => {
    if (!showLlmDialog || !llmAction) {
      return null;
    }

    if (llmAction.result === null) {
      if (!llmIsProcessing) {
        return (
          <div className="llm-block mb-4">
            <div className="llm-block-meta">
              <span className="llm-block-label">// Generate Analysis</span>
            </div>

            <div className="llm-generate-row">
              <div className="input-group input-group-sm" style={{ maxWidth: "480px" }}>
                <select className="form-select"
                        value={selectedLlmModel}
                        onChange={(e) => setSelectedLlmModel(e.target.value)}>
                  <option value="granite-4-micro">IBM Granite 4.0 Micro (Ollama)</option>
                  <option value="claude-opus-4-7">Claude Opus 4.7</option>
                  <option value="mistral-large-3">Mistral Large 3</option>
                </select>
                <button className="btn btn-primary" onClick={llmAction.onClick}>
                  Generate
                </button>
              </div>
            </div>

            <div className="llm-block-footer">
              Select model and generate analysis.
            </div>
          </div>
        )
      } else {
        return (
          <div className="llm-block mb-4">
            <div className="llm-block-meta">
              <span className="llm-block-label">// Generate Analysis</span>
            </div>

            <div className="llm-block-body">
              <span className="llm-block-label blink">Please wait ...</span>
            </div>
          </div>
        )
      }

    } else {
      return (
        <div className="llm-block mb-4">
          <div className="llm-block-meta">
            <span className="llm-block-label">// Generated Analysis</span>
          </div>

          <div className="llm-block-body">
            <LLMResponse data={llmAction.result.response} type="DOT11_BSSID_TIMELINE_SUMMARY" />
          </div>

          <div className="llm-block-footer">
            <span>Generated output, Verify before acting.</span>
            <span className="llm-block-stats">
              <span>in: {numeral(llmAction.result.input_tokens).format("0,0")}</span>
              <span className="llm-block-stats-sep">·</span>
              <span>out: {numeral(llmAction.result.output_tokens).format("0,0")}</span>
            </span>
          </div>
        </div>
      )
    }
  }

  const timeRangeDialog = () => {
    if (!timeRangeDialogOpened) {
      return null;
    }

    return (
      <div className="mb-3">
        <TimeRangeSelector timeRange={timeRange}
                           setTimeRange={setTimeRange}
                           urlKey={urlKey}
                           doNotPersistTimeRange={doNotPersistTimeRange} />
      </div>
    );
  }

  const appliedTimeRange = () => {
    if (hideTimeRange || (!timeRange && !fixedAppliedTimeRange)) {
      return null;
    }

    return (
      <div className="row">
        <div className="col-12">
          <AppliedTimeRange timeRange={timeRange ? timeRange : fixedAppliedTimeRange}/>
        </div>
      </div>
    )
  }

  return (
    <div className="card-title" style={slim ? {marginBottom: 0} : {}}>
      <div className="row">
        <div className="col-10">
          <h3>{title} {smallText()}</h3>
        </div>

        <div className="col-2 text-end">
          {timeRangeButton()}
          {internalLinkButton()}
          {helpLinkButton()}
          {llmActionButton()}
          {refreshButton()}
        </div>
      </div>

      <div className="row">
        <div className="col-12">
          {timeRangeDialog()}
          {llmDialog()}
        </div>
      </div>

      {appliedTimeRange()}

      {slim ? null : <hr />}
    </div>
  )
}

export default CardTitleWithControls;