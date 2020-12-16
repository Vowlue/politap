import { Label, Header, Icon, Form, Divider, Message, Segment, Button, Grid, Popup, Accordion } from 'semantic-ui-react'
import Plot from 'react-plotly.js'
import { stringifyNumber } from '../../helpers/stringHelper.js'
import { getHistory, getBoxPlot } from '../../apis/axios.js'
import { useEffect, useState } from 'react'

const stateAbbrev = {
  'SC': 'South Carolina',
  'VA': 'Virginia',
  'AR': 'Arkansas'
}

const map = {'AR' : [.03,.17,.18,.21], 'SC': [.178,.182,.183,.229,.271,.272,.554], 'VA': [.05,.07,.10,.11,.12,.15,.16,.19,.20,.31,.54]}
    

const upperCaseString = stringList => {
  return stringList.map(word => word[0].toUpperCase() + word.substring(1)).join(' ')
}
function HistoryTab(props) {
  const calculatePlotData = plotData => {
    const stats = {
      min: [],
      max: [],
      median: [],
      q1: [],
      q3: []
    }
    
    for(let i = 0; i<plotData.length; i++) {
      let current = plotData[i]
      current.sort()
      stats.min.push(current[0])
      stats.max.push(current[current.length-1])
      const mid = Math.ceil(current.length / 2)
      stats.median.push(current.length % 2 === 0 ? (current[mid] + current[mid - 1]) / 2 : current[mid - 1])
      stats.q1.push(current[Math.floor(current.length*0.25)-1])
      stats.q3.push(current[Math.floor(current.length*0.75)-1])
    }
    return stats
  }
  
  const renderHistory = jobInfo => {
    const calculatePlot = (jobInfo) => {
      getBoxPlot({id: jobInfo.id},
      res => {
        plotData[jobInfo.id] = calculatePlotData(res)
        setPlotData(plotData)
      }, 
      err => console.log(err))
    }
    const panels = [
      {
        key: 'more-options',
        title: 'More Options',
        content: {
          content: (<div>
            <Button onClick={() => calculatePlot(jobInfo)}>Calculate</Button>
            {
              jobInfo.id in plotData ? 
              <Popup trigger={<Button color='teal' content='Box Plot' />}>
              <Popup.Content>
                <Plot
                    data={[
                      {
                        "type": "box",
                        "x": Array.from(new Array(plotData[jobInfo.id].q1.length),(val,index)=>index+1),
                        "q1": plotData[jobInfo.id].q1,
                        "median": plotData[jobInfo.id].median,
                        "q3": plotData[jobInfo.id].q3,
                        "lowerfence": plotData[jobInfo.id].min,
                        "upperfence": plotData[jobInfo.id].max,
                        "mean": map[jobInfo.state]
                      }
                    ]}
                    layout={ {} }
                  />
              </Popup.Content>
            </Popup>
            :
            null
            }
            <Header textAlign='center' as='div' size='small'>Job Districts</Header>
            <Button 
              color='red'
              basic={!props.visibility.random1} 
              onClick={() => {
                props.setDistrictings(jobInfo.id)
                props.setCurrentJobId(jobInfo.id)
                setTimeout(() => props.setVisibility('random1', !props.visibility.random1), 200)
              }}
              content="Random"
            />
            <Button 
              color='green'
              basic={!props.visibility.random2} 
              onClick={() => {
                props.setDistrictings(jobInfo.id)
                props.setCurrentJobId(jobInfo.id)
                setTimeout(() => props.setVisibility('random2', !props.visibility.random2), 200)
              }}
              content="Random"
            />
            <Button 
              color='purple'
              basic={!props.visibility.average} 
              onClick={() => {
                props.setDistrictings(jobInfo.id)
                props.setCurrentJobId(jobInfo.id)
                setTimeout(() => props.setVisibility('average', !props.visibility.average), 200)
              }}
              content="Average"
            />
            <Button 
              color='brown'
              basic={!props.visibility.extreme} 
              onClick={() => {
                props.setDistrictings(jobInfo.id)
                props.setCurrentJobId(jobInfo.id)
                setTimeout(() => props.setVisibility('extreme', !props.visibility.extreme), 200)
              }}
              content="Extreme"
            />
            </div>)}
      }
    ]
    return (
      <Segment key={jobInfo.id}>
        <Label onClick={() => props.removeJobFromHistory(jobInfo.id)} color='red' icon='x' corner='left'></Label>
        <Header textAlign='center' as='h3'>
          {`Job #${jobInfo.id}`}
          <Header.Subheader>
              <Label color='black' size='large'>
                <Icon name='star'></Icon>
                State
                <Label.Detail content={stateAbbrev[jobInfo.state]}> 
                </Label.Detail>
              </Label>
              <Label color='green' size='large'>
                Status
                <Label.Detail content={upperCaseString(jobInfo.status.toLowerCase().split('_'))}> 
                </Label.Detail>
              </Label>
          </Header.Subheader>
        </Header>
        <Grid columns='equal' divided padded>
          <Grid.Row>
            <Grid.Column>
              <Label size='large' color='teal' basic >
              Plans:
                <Label.Detail content={stringifyNumber(jobInfo.plans)}>
                </Label.Detail>
              </Label>
              <Label size='large' color='teal' basic >
              Population Variance:
                <Label.Detail content={jobInfo.populationVariance}>
                </Label.Detail>
              </Label>
              <Label  size='large' color='teal' basic >
              Compactness:
                <Label.Detail content={upperCaseString(jobInfo.compactness.toLowerCase().split('_'))}>
                </Label.Detail>
              </Label>
              <Label  size='large' color='teal' basic >
              Server:
                <Label.Detail content={jobInfo.plans <= 15 ? 'Local' : 'Seawulf'}>
                </Label.Detail>
              </Label>
            </Grid.Column>
            <Grid.Column stretched>
              <Label  size='large' color='teal' basic >
              Racial/Ethnic Groups:
                <Label.Detail content={jobInfo.demographics ? upperCaseString(jobInfo.demographics.toLowerCase().split('_')) : "None"}>
                </Label.Detail>
              </Label>
            </Grid.Column>
          </Grid.Row>
        </Grid>
        {
          jobInfo.status === "RUNNING" ?
          <Button as='div' labelPosition='left'>
            <Label as='div' color='red' basic> 
              <Icon name='wait' /> Waiting for job to finish
            </Label>
            <Button color='red' onClick={() => props.cancelJob(jobInfo.id)}>Cancel Job</Button>
          </Button>
          :
          <Accordion defaultActiveIndex={props.defaultIndex} panels={panels} />
        }
      </Segment>
    )
  }

  const [history, setHistory] = useState([])
  const [plotData, setPlotData] = useState({})

  useEffect(() => {
    getHistory(
      res => {
        setHistory(res)
      },
      err => {
        console.log(err)
      }
    )
  })
  

  return (
      <Form>
        <Message>
          <Header textAlign='center' as='h2'>
            Job History
            <Header.Subheader>
            View the history of generated jobs.
            </Header.Subheader>
          </Header>
        </Message>
        <Divider horizontal>
          <Header as='h4'>
            <Icon name='bars' />
            Jobs
          </Header>
        </Divider>
        <Segment style={{ overflow: 'auto', maxHeight: '64vh' }}>
          { 
            history.map(job => 
              renderHistory(job))
          }
        </Segment>
      </Form>
  )
}

export { HistoryTab }