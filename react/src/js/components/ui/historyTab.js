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

const upperCaseString = stringList => {
  return stringList.map(word => word[0].toUpperCase() + word.substring(1)).join(' ')
}

function HistoryTab(props) {
  const renderHistory = jobInfo => {
    const panels = [
      {
        key: 'more-options',
        title: 'More Options',
        content: {
          content: (<div>
            <Popup trigger={<Button color='teal' content='Box Plot' />}>
              <Popup.Content>
                <Plot
                    data={[
                      {
                        x: [1, 2, 3,4,5,6,7,8,9],
                        type: 'box',
                        marker: {color: 'red'},
                      }
                    ]}
                    layout={ {width: 320, height: 240, title: 'A Fancy Plot'} }
                  />
              </Popup.Content>
            </Popup>
            <Header textAlign='center' as='div' size='small'>Job Districts</Header>
            <Button onClick={() => getBoxPlot(jobInfo.id,
      res => {
        console.log(res)
      },
      err => {
        console.log(err)
      }
    )}>TEST BUTTON</Button>
            <Button 
              color='red'
              basic={!props.visibility.random} 
              onClick={() => {
                props.setCurrentJobId(jobInfo.id)
                props.setVisibility('random', !props.visibility.random)
              }}
              content="Random"
            />
            <Button 
              color='green'
              basic={!props.visibility.random2} 
              onClick={() => {
                props.setCurrentJobId(jobInfo.id)
                props.setVisibility('random2', !props.visibility.random2)
              }}
              content="Random"
            />
            <Button>Average</Button>
            <Button>Extreme</Button>
            </div>)}
      }
    ]
    return (
      <Segment key={jobInfo.id}>
        <Label onClick={() => props.removeJobFromHistory(jobInfo.id)} color='red' icon='x' corner='right'></Label>
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
  const [plotData, setPlotData] = useState([])

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