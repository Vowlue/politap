import { GeoJSON } from 'react-leaflet'

function Precinct(props) {
  return (
    <GeoJSON
      style={() => ({
        color: '#3afa00',
        weight: 0.5,
        fillColor: "#000000",
        fillOpacity: 0.3,
      })}
      data={props.data.features} 
    />
  )
}

export { Precinct }