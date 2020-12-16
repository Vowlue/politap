import { GeoJSON } from 'react-leaflet'

function State(props) {
  return (
    <GeoJSON
      style={() => ({
        color: '#1ca68f',
        weight: 2,
        fillColor: "#46dee0",
        fillOpacity: 0.1,
      })}
      data={props.data} 
      onEachFeature={(feature, layer) => {
        props.setStateBounds(feature.properties.NAME, layer._bounds)
        layer.on({
          click: () => props.setActiveState(feature.properties.NAME)
        })
      }}
    />
  )
}

export { State }