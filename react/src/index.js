import React from 'react'
import { Component } from 'react'
import { render } from 'react-dom'
import { MapContainer, MapConsumer, TileLayer, ZoomControl } from 'react-leaflet'
import { Button, Message } from 'semantic-ui-react'
import SockJsClient from 'react-stomp'

import { stateGeoJSON } from './data/data.js'
import { districtAKGeoJSON } from './data/districtAKGeoJSON.js'
import { districtVAGeoJSON } from './data/districtVAGeoJSON.js'
import { districtSCGeoJSON } from './data/districtSCGeoJSON.js'

import { State } from './js/components/geojson/state.js'
import { District } from './js/components/geojson/district.js'
import { Precinct } from './js/components/geojson/precinct.js'
import { JobDistrict } from './js/components/geojson/jobdistrict.js'
import { HeatMap } from './js/components/geojson/heatmap.js'
import { Sidebar } from './js/components/ui/sidebar.js'

import './css/index.css'
import 'semantic-ui-css/semantic.min.css'

import { convertEnumToString, stringifyNumber, getJavaState } from './js/helpers/stringHelper.js'
import { deleteJob, initiateJob, getStateData, getDistrictings, cancelJob } from './js/apis/axios.js'

class BiasMap extends Component {
  constructor(props) {
    super(props)
    this.state = {
      position: [38.305,-96.156],
      zoom: 4.75,
      stateDemographicStats: {},
      hoveredPrecinct: 'No precinct hovered',
      activeState: null,
      demographicContent: [],
      visibility: {
        state: true,
        district: false,
        precinct: false,
        random: false,
        random2: false,
      },
      currentMinority: null,
      jobHistory: {},
      precinct_geojsons: {},
      jobLabelContent: "No job has been started yet.",
      currentJobId: 0
    }

    this.showDemographics = this.showDemographics.bind(this)
    this.setStateBounds = this.setStateBounds.bind(this)
    this.setActiveState = this.setActiveState.bind(this)
    this.setVisibility = this.setVisibility.bind(this)
    this.addJobToHistory = this.addJobToHistory.bind(this)
    this.removeJobFromHistory = this.removeJobFromHistory.bind(this)
    this.modifyJobStatus = this.modifyJobStatus.bind(this)
    this.cancelHistoryJob = this.cancelHistoryJob.bind(this)
    this.setCurrentJobId = this.setCurrentJobId.bind(this)
    this.setCurrentMinority = this.setCurrentMinority.bind(this)

    this.stateBounds = []
    this.districtGeoJsons = [...districtAKGeoJSON, ...districtSCGeoJSON, ...districtVAGeoJSON]
  }

  setCurrentMinority(minority) {
    if(this.state.currentMinority === minority) {
      this.setState({
        currentMinority: null
      })
    }
    else {
      this.setState({
        currentMinority: null
      })
      setTimeout(() => this.setState({
        currentMinority: minority
      }), 20)
    }
  }

  setCurrentJobId(id) {
    this.setState({
      currentJobId: id
    })
  }

  cancelHistoryJob(id) {
    if(id in this.state.jobHistory) {
      cancelJob(
        {
          id: id
        },
        res => {
          if (res) {
            this.modifyJobStatus(id, 'Canceled')
          }
        },
        err => {
          console.log(err)
        }
      )
    }
  }

  modifyJobStatus(id, status) {
    if(id in this.state.jobHistory) {
      let newJobHistory = {...this.state.jobHistory}
      newJobHistory[id].status = status
      if (status === 'Done') {
        getDistrictings(
          {
            id: id
          },
          res => {
            newJobHistory[id].jobDistricts = res
            this.setState({
              jobHistory: newJobHistory
            })
          }
        )
      }
      else {
        this.setState({
          jobHistory: newJobHistory
        })
      }
    }
  }

  addJobToHistory(jobInfo) {
    jobInfo.state = this.state.activeState
    initiateJob(
      {
        plans: jobInfo.plans,
        populationVariance: jobInfo.populationVariance,
        compactness: jobInfo.compactness.toUpperCase(),
        state: getJavaState(jobInfo.state),
        isLocal: jobInfo.server === 'Local',
        demographic: jobInfo.groups.map(group => group.toUpperCase().replaceAll(" ", "_"))
      },
      res => {
        jobInfo.id = res
        this.setState({
          jobLabelContent: `Job #${res} has been started.`,
          jobHistory: {
            ...this.state.jobHistory,
            [res]: jobInfo
          }
        })
      },
      err => {
        console.log(err)
      }
    )
  }

  removeJobFromHistory(index) {
    deleteJob({ID: index})
    const { [index]: value, ...withoutIndex } = this.state.jobHistory
    this.setState({
      jobHistory: withoutIndex
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

  showDemographics(toShow, precinct, county, stats) {
    if (toShow === null) {
      this.setState({
        hoveredPrecinct: "No precinct hovered.",
        demographicContent: []
      })
      return 
    }
    if(stats) {
      for (let i = 0; i<stats.length; i++) {
        stats[i] = stringifyNumber(stats[i])
      }
    }
    if (toShow === 'all') {
      this.setState({
        hoveredPrecinct: precinct + ' - ' + county ,
        demographicContent: [
        `Total: VAP: ${stats[0]}, POP: ${stats[1]}`,
        `White: VAP: ${stats[2]}, POP: ${stats[3]} `,
        `Hispanic: VAP: ${stats[4]}, POP: ${stats[5]} `,
        `Black: VAP: ${stats[6]}, POP: ${stats[7]} `,
        `Asian: VAP: ${stats[8]}, POP: ${stats[9]} `,
        `Native American: VAP: ${stats[10]}, POP: ${stats[11]} `,
        `Hawaiian Pacific: VAP: ${stats[12]}, POP: ${stats[13]} `,
        `Other: VAP: ${stats[14]}, POP: ${stats[15]} `
        ]
      })
    }
    else {
      this.setState({
        hoveredPrecinct: precinct + ' - ' + county ,
        demographicContent: [
        `${toShow}: VAP: ${stats[0]}, POP: ${stats[1]}`,
        ]
      })
    }
  }

  setStateBounds(state, bounds) {
    this.stateBounds[state] = bounds
  }

  setPrecinctData(state) {
    if (!(state in this.state.precinct_geojsons)) {
      getStateData(
        { state: getJavaState(state) },
        res => {
          this.setState({
            precinct_geojsons: {
              ...this.state.precinct_geojsons,
              [state]: res
            }
          })
        },
        err => {
          console.log(err)
        }
      )
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
        <SockJsClient url='http://localhost:8080/webSocket/'
          topics={ ['/jobStatus'] }
          onMessage={ msg => { console.log(msg);this.modifyJobStatus(msg.id, convertEnumToString(msg.jobStatus)) } }
          ref={(client) => {this.clientRef = client}}
        />
        <Sidebar 
          jobHistory={ this.state.jobHistory }
          addJobToHistory={ this.addJobToHistory }
          removeJobFromHistory={ this.removeJobFromHistory }
          activeState={ this.state.activeState } 
          setActiveState={ this.setActiveState }
          setVisibility={ this.setVisibility }
          visibility={ this.state.visibility }
          jobLabelContent={ this.state.jobLabelContent }
          cancelJob={ this.cancelHistoryJob }
          setCurrentJobId={this.setCurrentJobId}
          currentMinority={this.state.currentMinority}
          setCurrentMinority={this.setCurrentMinority}
        />
        <div id='map'>
          <MapContainer
            center={this.state.position} 
            zoom={this.state.zoom}
            zoomControl={false}
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
                    setActiveState={this.setActiveState}
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
                    setActiveState={this.setActiveState}
                  />
              ))
              :
              null
            }
            {
              this.state.visibility.precinct ?
              (
                <Precinct 
                  key={this.state.activeState}
                  showDemographics={this.showDemographics}
                  data={this.state.precinct_geojsons[this.state.activeState]}
                />
              )
              :
              null
            }
            {
              this.state.visibility.random ? 
              <JobDistrict color="#B03060" data={this.state.jobHistory[this.state.currentJobId].jobDistricts.random}/>
              :
              null
            }
            {
              this.state.visibility.random2 ? 
              <JobDistrict color="#016936" data={this.state.jobHistory[this.state.currentJobId].jobDistricts.random2}/>
              :
              null
            }
            {
              this.state.currentMinority ? 
              <HeatMap minority={this.state.currentMinority} data={this.state.precinct_geojsons[this.state.activeState]} showDemographics={this.showDemographics}/>
              :
              null
            }
            <ZoomControl position='bottomright'/>
            <div className='leaflet-bottom leaflet-left'>
              <div className='leaflet-control'>
                <Message floating compact >
                  <Message.Header> {this.state.hoveredPrecinct} </Message.Header>
                  <Message.List items={this.state.demographicContent}/>
                </Message>
              </div>
            </div>
              <div className='leaflet-top leaflet-right'>
                <div className='leaflet-control'>
                  <Button 
                    onClick={() => this.map.setView(this.state.position, this.state.zoom)} 
                    icon='expand'
                    color='blue'
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