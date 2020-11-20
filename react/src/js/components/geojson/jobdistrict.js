import { GeoJSON } from 'react-leaflet'

function JobDistrict(props) {
  return (
    <GeoJSON
      style={() => ({
        color: '#0E6EB8',
        weight: 1.5,
        fillColor: "#0E6EB8",
        fillOpacity: 0.1,
      })}
      data={props.data} 
    />
  )
}

export { JobDistrict }