import { GeoJSON } from 'react-leaflet'


const findState = (FP) => {
  if (FP === "05")
    return "Arkansas"
  else if (FP === "51")
    return "Virginia"
  else
    return "South Carolina"
}

function Precinct(props) {
  return (
    <GeoJSON
      style={() => ({
        color: '#000142',
        weight: 0.2,
        fillOpacity: 0,
      })}
      data={props.data.features} 
      onEachFeature={(feature, layer) => {
        const state = findState(feature.properties.STATEFP10)
        layer.on({
          mouseover: () => props.showDemographics(state),
          mouseout: () => props.showDemographics(null),
        })
      }}
    />
  )
}

export { Precinct }