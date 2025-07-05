export default function validateEnum(values, value, ignoreCase = false) {
  if (ignoreCase) {
    return values.includes(value) || values.includes(value.toUpperCase())
  } else {
    return values.includes(value)
  }
}