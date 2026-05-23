import React from 'react';

export default function LocationWeatherCondition({environment}) {

  const renderIndicator = (severity) => {
    let colorClass;
    if (severity == null) {
      colorClass = "text-muted";
    } else if (severity >= 5) {
      colorClass = "text-danger";
    } else if (severity >= 1) {
      colorClass = "text-warning";
    } else {
      colorClass = "text-success";
    }
    return <i className={`fa-solid fa-circle me-2 ${colorClass}`}></i>;
  };

  if (!environment || environment.condition == null) {
    return <span className="text-muted">{renderIndicator(null)}no condition data</span>;
  }

  return <span>{renderIndicator(environment.condition.severity)}{environment.condition.display_name}</span>;
}