export default function UavAltitude(props) {
  const uav = props.uav;
  let unit = props.unit;

  if (!unit || unit !== "feet" || unit !== "meters") {
    unit = "feet";
  }

  return (
      "foo"
  )

}