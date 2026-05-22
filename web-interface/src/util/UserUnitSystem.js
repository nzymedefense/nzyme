export function temperatureInUserUnitSystem(celsius, user) {
  if (user.unit_system === "imperial") {
    return (celsius*1.8)+32;
  } else {
    return celsius;
  }
}

export function temperatureUserSymbol(user) {
  if (user.unit_system === "imperial") {
    return "F";
  } else {
    return "C";
  }
}

export function speedInUserUnitSystem(kmh, user) {
  if (user.unit_system === "imperial") {
    return kmh*0.621371;
  } else {
    return kmh;
  }
}

export function speedUserSymbol(user) {
  if (user.unit_system === "imperial") {
    return "mph";
  } else {
    return "km/h";
  }
}

export function distanceInUserUnitSystem(km, user) {
  if (user.unit_system === "imperial") {
    return km*0.621371;
  } else {
    return km;
  }
}

export function distanceUserSymbol(user) {
  if (user.unit_system === "imperial") {
    return "miles";
  } else {
    return "km";
  }
}