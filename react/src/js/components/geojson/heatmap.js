import { GeoJSON } from 'react-leaflet'

const colorMap = {
  "Black or African American": [271,100,50],
  "Hispanic or Latino": [357,78,44],
  "White": [24,100,50],
  "Asian": [120,100,45],
  "American Indian and Alaska Native": [168,100,50],
  "Native Hawaiian and Other Pacific Islander": [312,100,55],
  "Some Other Race": [0,33,50]
}

const adjustColor = (minority, adjustment) => {
  console.log(adjustment)
  const hslList = colorMap[minority]
  return `hsl(${hslList[0]},${hslList[1]}%,${hslList[2]+adjustment*40}%)`
}

function HeatMap(props) {
  return (
    <GeoJSON
      data={props.data} 
      onEachFeature={(feature, layer) => {

        layer.setStyle({
          fillColor: adjustColor(props.minority, 0.5-feature.properties[props.minority+" VAP"]/(feature.properties[props.minority] > 0 ? feature.properties[props.minority] : 1)),
          fillOpacity: 0.5,
          weight: 0.5,
        })
        layer.on({
          mouseover: () => props.showDemographics(props.minority, feature.properties.NAME10, feature.properties.COUNTY, [feature.properties[props.minority], feature.properties[props.minority+" VAP"]]),
          mouseout: () => props.showDemographics(null),
        })
      }}
    />
  )
}

export { HeatMap }