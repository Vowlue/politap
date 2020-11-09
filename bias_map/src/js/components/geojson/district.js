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
        color: '#0422b0',
        weight: 1,
        fillColor: "#45a8de",
        fillOpacity: 0.5,
      })}
      data={props.data} 
      onEachFeature={(feature, layer) => {
        const state = findState(feature.properties.District)
        layer.on({
          mouseover: () => props.showDemographics(state),
          mouseout: () => props.showDemographics('No state hovered'),
          click: () => props.setActiveState(state)
        })
      }}
    />
  )
}

export { District }