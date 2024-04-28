import React from "react";

const channels24 = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13];
const channels5 = [36, 40, 44, 48, 52, 56, 60, 64, 100, 104, 108, 112, 116, 120, 124, 128, 132, 136, 140, 144, 149,
  153, 157, 161, 165, 169, 173, 177];
const channels6 = [1, 5, 9, 13, 17, 21, 25, 29, 33, 37, 41, 45, 49, 53, 57, 61, 65, 69, 73, 77, 81, 85, 89, 93, 97, 101,
  105, 109, 113, 117, 121, 125, 129, 133, 137, 141, 145, 149, 153, 157, 161, 165, 169, 173, 177, 181, 185, 189, 193,
  197, 201, 205, 209, 213, 217, 221, 225, 229, 233];

export default function TapDot11CoverageMap(props) {

  return (
      <table className="table table-sm ">
        <thead>
        <tr>
          <th>Band</th>
          <th>Channel</th>
          <th>20 MHz</th>
          <th>40- MHz</th>
          <th>40+ MHz</th>
          <th>80 MHz</th>
          <th>160 MHz</th>
          <th>320 MHz</th>
        </tr>
        </thead>
        <tbody>
          {channels24.map((channel, i) =>
              <tr key={i}>
                {i === 0 ? <td rowSpan={channels24.length}>2.4 GHz</td> : null}
                <td>{channel}</td>
                <td>x</td>
                <td>x</td>
                <td>x</td>
                <td>n/a</td>
                <td>n/a</td>
                <td>n/a</td>
              </tr>
          )}

          {channels5.map((channel, i) =>
              <tr key={i}>
                {i === 0 ? <td rowSpan={channels5.length}>5 GHz</td> : null}
                <td>{channel}</td>
                <td>x</td>
                <td>x</td>
                <td>x</td>
                <td>x</td>
                <td>{channel >= 132 && channel <= 144 ? "n/a" : "x"}</td>
                <td>n/a</td>
              </tr>
          )}

          {channels6.map((channel, i) =>
              <tr key={i}>
                {i === 0 ? <td rowSpan={channels6.length}>6 GHz</td> : null}
                <td>{channel}</td>
                <td>x</td>
                <td>x</td>
                <td>x</td>
                <td>x</td>
                <td>x</td>
                <td>x</td>
              </tr>
          )}
        </tbody>
      </table>
  )

}