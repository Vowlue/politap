import { GeoJSON } from 'react-leaflet'

const findState = (string) => {
  if (string.indexOf("Arkansas") > -1)
    return "Arkansas"
  else if (string.indexOf("Virginia") > -1)
    return "Virginia"
  else if (string.indexOf("South Carolina") > -1)
    return "South Carolina"
}

function District(props) {
  return (
    <GeoJSON
      style={() => ({
        color: '#0E6EB8',
        weight: 1.5,
        fillColor: "#0E6EB8",
        fillOpacity: 0.2,
      })}
      data={props.data} 
      onEachFeature={(feature, layer) => {
        const state = findState(feature.properties.District)
        layer.on({
          click: () => props.setActiveState(state)
        })
      }}
    />
  )
}

export { District }