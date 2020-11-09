import React from 'react'
import { Component } from 'react'
import { render } from 'react-dom'
import { MapContainer, MapConsumer, TileLayer, ZoomControl } from 'react-leaflet'
import { Button, Message } from 'semantic-ui-react'
import axios from 'axios'

import { stateGeoJSON } from './data/data.js'
import { districtAKGeoJSON } from './data/districtAKGeoJSON.js'
import { districtVAGeoJSON } from './data/districtVAGeoJSON.js'
import { districtSCGeoJSON } from './data/districtSCGeoJSON.js'

import { State } from './js/components/geojson/state.js'
import { District } from './js/components/geojson/district.js'
import { Precinct } from './js/components/geojson/precinct.js'
import { Sidebar } from './js/components/ui/sidebar.js'

import './css/index.css'
import 'semantic-ui-css/semantic.min.css'

class BiasMap extends Component {
  constructor(props) {
    super(props)
    this.state = {
      position: [38.305,-96.156],
      zoom: 5,
      hoveredState: 'No state hovered',
      activeState: null,
      demographicContent: '',
      visibility: {
        state: true,
        district: true,
        precinct: false
      },
      batchHistory: [],
      precinct_geojsons: {}
    }

    //bindings
    this.showDemographics = this.showDemographics.bind(this)
    this.setStateBounds = this.setStateBounds.bind(this)
    this.setActiveState = this.setActiveState.bind(this)
    this.setVisibility = this.setVisibility.bind(this)
    this.addBatchToHistory = this.addBatchToHistory.bind(this)
    this.removeBatchFromHistory = this.removeBatchFromHistory.bind(this)

    this.stateBounds = []
    this.districtGeoJsons = [...districtAKGeoJSON, ...districtSCGeoJSON, ...districtVAGeoJSON]
  }

  addBatchToHistory(batchInfo) {
    batchInfo.state = this.state.activeState
    this.setState({
      batchHistory: {
        ...this.state.batchHistory,
        [batchInfo.id]: batchInfo
      }
    })
  }

  removeBatchFromHistory(index) {
    const { [index]: value, ...withoutIndex } = this.state.batchHistory
    this.setState({
      batchHistory: withoutIndex
    });
  }

  setVisibility(component, visible) {
    this.setState({
      visibility: {
        ...this.state.visibility,
        [component]: visible
      }
    })
  }

  showDemographics(state) {
    this.setState({
      hoveredState: state
    })
  }

  setStateBounds(state, bounds) {
    this.stateBounds[state] = bounds
  }

  getJavaState(state) {
    return state.toUpperCase().replace(" ", "_")
  }

  setPrecinctData(state) {
    if (!(state in this.state.precinct_geojsons)) {
      axios.post(`http://localhost:8080/getStateData`, { state: this.getJavaState(state) })
      .then(res => {
        this.setState({
          precinct_geojsons: {
            ...this.state.precinct_geojsons,
            [state]: res.data
          }
        })
        this.setVisibility('precinct', true)
      })
    }
  }

  setActiveState(state) {
    this.map.fitBounds(this.stateBounds[state])
    this.setState({
      activeState: state
    })
    this.setPrecinctData(state)
  }

  render() {
    return (
      <div id='app_container'>
        <Sidebar 
          batchHistory={ this.state.batchHistory }
          addBatchToHistory={ this.addBatchToHistory }
          removeBatchFromHistory={ this.removeBatchFromHistory }
          activeState={ this.state.activeState } 
          setActiveState={ this.setActiveState }
          setVisibility={ this.setVisibility }
          visibility={ this.state.visibility }
        />
        <div id='map'>
          <MapContainer
            center={this.state.position} 
            zoom={this.state.zoom}
            zoomControl={false}
            maxZoom={10}
            minZoom={5}
            zoomSnap={0.10}
          >
            <MapConsumer>
              {(map) => {
                this.map = map
                return null
              }
            }
            </MapConsumer>
            <TileLayer
              url="https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}"
              attribution='Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, 
              <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, 
              Imagery © <a href="https://www.mapbox.com/">Mapbox</a>'
              id='vowlue/ckfraa3l42vrl19mvs2huqnw1'
              accessToken='pk.eyJ1Ijoidm93bHVlIiwiYSI6ImNrZnJhNnpsbzA0YmUycXRzdzNnbTZvN20ifQ.o1qotA_m4Uhdh3V7-TNLKQ'
              zoomOffset={-1}
              tileSize={512}
            />
            {
              this.state.visibility.state ? 
              stateGeoJSON.map((geojson, index) => 
                (
                  <State 
                    key={ index }
                    data={ geojson }
                    setStateBounds={ this.setStateBounds }
                  />
                )
              )
              :
              null
            }
            {
              this.state.visibility.district ?
              this.districtGeoJsons.map((geojson, index) => (
                  <District 
                    key={index}
                    data={geojson}
                    showDemographics={this.showDemographics}
                    setActiveState={this.setActiveState}
                  />
              ))
              :
              null
            }
            {
              this.state.visibility.precinct ?
              Object.keys(this.state.precinct_geojsons).map(state => {
                return (
                  <Precinct 
                    key={state}
                    data={this.state.precinct_geojsons[state]}
                  />
                )
              })
              :
              null
            }
            <ZoomControl position='bottomright'/>
            <div className='leaflet-top leaflet-left'>
              <div className='leaflet-control'>
                <Message icon='info circle' header={this.state.hoveredState} content={this.state.demographicContent}></Message>
              </div>
            </div>
              <div className='leaflet-top leaflet-right'>
                <div className='leaflet-control'>
                  <Button 
                    onClick={() => this.map.setView(this.state.position, this.state.zoom)} 
                    icon='expand'
                    color='instagram'
                  >
                  </Button>
                </div>
              </div>
          </MapContainer>
        </div>
      </div>
    )
  }
}

render(<BiasMap />, document.getElementById('main_container'))