import { GeoJSON } from 'react-leaflet'

function JobDistrict(props) {
  return (
    <GeoJSON
      style={() => ({
        color: props.color,
        weight: 3,
        fillOpacity: 0,
      })}
      data={props.data} 
    />
  )
}

export { JobDistrict }