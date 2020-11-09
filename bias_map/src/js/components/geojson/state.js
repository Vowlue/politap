import { GeoJSON } from 'react-leaflet'

function State(props) {
  return (
    <GeoJSON
      style={() => ({
        color: '#1ca68f',
        weight: 0,
        fillColor: "#46dee0",
        fillOpacity: 0,
      })}
      data={props.data} 
      onEachFeature={(feature, layer) => {
        props.setStateBounds(feature.properties.NAME, layer._bounds)
      }}
    />
  )
}

export { State }