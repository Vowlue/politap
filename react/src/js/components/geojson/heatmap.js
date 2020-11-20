import { GeoJSON } from 'react-leaflet'

const getStyle = () => {
    return {
        color: 'hsl(0, 100%, 30%)',
        weight: 0.3,
        fillOpacity: 0,
    }
}

function HeatMapPrecinct(props) {
  return (
    <GeoJSON
      style={getStyle}
      data={props.data} 
      onEachFeature={(feature, layer) => {
        const precinct = feature.properties.NAME10
        layer.on({
          mouseover: () => props.showDemographics(precinct, feature.properties.COUNTY, [feature.properties["Total VAP"], feature.properties.Total, 
          feature.properties["White"], feature.properties["White VAP"], feature.properties["Hispanic or Latino"], feature.properties["Hispanic or Latino VAP"], 
          feature.properties["Black or African American"], feature.properties["Black or African American VAP"], feature.properties["Asian"], feature.properties["Asian VAP"], 
          feature.properties["American Indian and Alaska Native"], feature.properties["American Indian and Alaska Native VAP"], feature.properties["Native Hawaiian and Other Pacific Islander"], feature.properties["Native Hawaiian and Other Pacific Islander VAP"], 
          feature.properties["Some Other Race"], feature.properties["Some Other Race VAP"]]),
          mouseout: () => props.showDemographics(null),
        })
      }}
    />
  )
}

export { HeatMapPrecinct }